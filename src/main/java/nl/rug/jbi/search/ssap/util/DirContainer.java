package nl.rug.jbi.search.ssap.util;

import java.io.*;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import static org.apache.commons.io.FileUtils.iterateFiles;

/**
 * Container for .class files in a directory.
 *
 * @author Andrei Dumitriu, Daniel Feitosa
 */
public class DirContainer extends ProjectContainer {

    public DirContainer(File project) {
        super(project);
    }

    @Override
    public Boolean isValid() {
        return isValid(project);
    }

    @Override
    public <A> void forEachClass(CallbackFunction<A> callback) throws IOException {
        Iterator<File> fileIterator = iterateFiles(project, new SuffixFileFilter(".class"), TrueFileFilter.TRUE);
        for (Iterator<File> it = fileIterator; it.hasNext(); ) {
            File file = it.next();
            callback.run(new FileInputStream(file));
        }
    }

    @Override
    public InputStream getClassStream(String classname) throws FileNotFoundException {
        String suffix = classname.replaceAll("\\.", "/") + ".class";
        return new FileInputStream(FileUtils.listFiles(project, new PathSuffixFilter(suffix), TrueFileFilter.TRUE).iterator().next());
    }

    @Override
    public void close() {}

    public static Boolean isValid(File project) {
        return project.isDirectory();
    }
}
