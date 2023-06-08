package nl.rug.jbi.search.ssap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Container for .class files in a directory.
 *
 * @author Andrei Dumitriu, Daniel Feitosa
 */
public class DirContainer extends ProjectContainer {

    protected DirContainer(File project) {
        super(project);
    }

    @Override
    public Boolean isValid() {
        return isDirValid(project);
    }

    @Override
    public InputStream getClassStream(String classname) {
        String suffix = classname.replaceAll("\\.", "/") + ".class";
        return new FileInputStream()
    }

    @Override
    public void close() throws Exception {

    }

    private static Boolean isDirValid(File project) {
        return project.isDirectory();
    }
}
