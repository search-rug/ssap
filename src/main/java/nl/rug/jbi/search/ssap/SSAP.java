package nl.rug.jbi.search.ssap;

import com.thoughtworks.xstream.XStream;
import nl.rug.jbi.search.ssap.model.*;
import nl.rug.jbi.search.ssap.model.System;
import nl.rug.jbi.search.ssap.util.*;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;


import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import static java.lang.System.exit;

@Command(name = "ssap", mixinStandardHelpOptions = true, version = "ssap 2.0", description = "Identify 10 extra roles based on the information from the ssaFile for each pattern occurrence.")
public class SSAP implements Callable<Integer> {

    private static final Logger logger = LogManager.getLogger(SSAP.class);

    // xStream is used to read and write xml files
    private static XStream xStream;

    @Parameters(index = "0", description = "XML file from SSA tool")
    private File ssaFile;

    @Parameters(index = "1", description = "A .jar or Folder containing the project's .class files")
    private File project;

    @Option(names = {"-o", "--output"}, description = "Output XML file (default: write to the ssaFile)")
    private File outputFile;

    private static void initializeXStream() {
        xStream = new XStream();
        xStream.allowTypesByWildcard(new String[]{"nl.rug.**"});
        xStream.processAnnotations(Role.class);
        xStream.processAnnotations(Version.class);
        xStream.processAnnotations(Instance.class);
        xStream.processAnnotations(Pattern.class);
        xStream.processAnnotations(System.class);
    }

    @Override
    public Integer call() throws Exception {
        System ssa;
        ProjectContainer pc;
        Map<String, Set<String>> parents;

        // If ssaFile is valid, we extract the information from the XML into the ssa variable
        if (ssaFile.exists()) {
            if (ssaFile.getName().matches("(?i).*\\.xml$")) {
                try {
                    ssa = (System) xStream.fromXML(ssaFile);
                } catch (Exception e) {
                    logger.error("The xml file does not follow the expected format.");
                    return 2;
                }
            } else {
                logger.error(ssaFile.getName() + " isn't an XML file");
                return 2;
            }
        } else {
            logger.error(ssaFile.getName() + " doesn't exist");
            return 2;
        }

        // If project is either a directory or a .jar file, then
        if (project.exists()) {
            if (JarContainer.isValid(project)) {
                pc = new JarContainer(project);
            } else if (DirContainer.isValid(project)) {
                pc = new DirContainer(project);
            }
            else {
                logger.error("Invalid project argument: project must be a directory or .jar file ");
                return 2;
            }
        } else {
            logger.error(project.getName() + " doesn't exist");
            return 2;
        }

        logger.info("Incrementing SSA data...");

        try {
            parents = ProjectParser.getParentsMap(pc);
        } catch (IOException e) {
            logger.error("An error was encountered while reading the project", e);
            return 2;
        }

        Incrementor.incrementPatternList(ssa, parents, pc);

        if (!outputFileIsValid()) {
            String outputFilePath = ssaFile.getName().replaceAll("\\.[^.]*$", "") + ".ssap.xml";
            outputFile = new File(outputFilePath);
        }
        logger.info("Saving to " + outputFile.getCanonicalPath());
        try {
            FileWriter outputWriter = new FileWriter(outputFile);
            outputWriter.write(xStream.toXML(ssa));
            outputWriter.close();
        } catch (IOException e) {
            logger.error("There was an error with writing the output.");
            return 2;
        }
        return 0;
    }

    private boolean outputFileIsValid() {
        if (outputFile == null) {
            logger.info("No output file provided.");
            return false;
        }
        if (outputFile.exists() && outputFile.getName().matches("(?i).*\\.xml$") && outputFile.canWrite()) {
            return true;
        }
        logger.info("Provided output file is not a valid xml file.");
        return false;
    }

    public static void main(String[] args) {
        initializeXStream();
        int exitCode = new CommandLine(new SSAP()).execute(args);
        exit(exitCode);
    }
}
