package nl.rug.jbi.search.ssap.util;

import java.io.*;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Container for .class files in a .jar.
 *
 * @author Andrei Dumitriu,  Daniel Feitosa
 */
public class JarContainer extends ProjectContainer{
    private final JarFile jar;
    public JarContainer(File project) throws IOException {
        super(project);
        jar = new JarFile(project);
    }

    @Override
    public Boolean isValid() {
        return isValid(project);
    }

    private static Boolean isValid(File project) {
        return project.isFile() && project.getName().matches("(?i).*\\.jar$");
    }

    @Override
    public <A> void forEachClass(CallbackFunction<A> callback) throws IOException {
        for (Iterator<JarEntry> it = jar.entries().asIterator(); it.hasNext(); ) {
            JarEntry entry = it.next();
            callback.run(getClassStream(entry.getName()));
        }
    }

    @Override
    public InputStream getClassStream(String classname) throws IOException {
        return jar.getInputStream(jar.getJarEntry(classname));
    }

    @Override
    public void close() throws IOException {
        jar.close();
    }
}
