package nl.rug.jbi.search.ssap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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

    protected DirContainer(File project) {
        super(project);
    }

    @Override
    public Boolean isValid() {
        return isDirValid(project);
    }

    @Override
    public <A> void forEachClass(CallbackFunction<A> callback) throws FileNotFoundException {
        Iterator<File> fileIterator = iterateFiles(project, new SuffixFileFilter(".class"), TrueFileFilter.TRUE);
        for (Iterator<File> it = fileIterator; it.hasNext(); ) {
            File file = it.next();
            callback.run(new FileInputStream(file));
        }
    }

    @Override
    public InputStream getClassStream(String classname) throws FileNotFoundException {
        String suffix = classname.replaceAll("\\.", "/") + ".class";
        return new FileInputStream(FileUtils.listFiles(project, new ScalaPathSuffixFilter(suffix), TrueFileFilter.TRUE).iterator().next());
    }

    @Override
    public void close() {}

    private static Boolean isDirValid(File project) {
        return project.isDirectory();
    }
}
