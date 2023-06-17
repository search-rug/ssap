package nl.rug.jbi.search.ssap.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;


public abstract class ProjectContainer implements AutoCloseable {

    public final File project;

    public ProjectContainer(File project) {
        this.project = project;

        if(!isValid()) {
            throw new IllegalArgumentException(project + " isn't a valid project");
        }
    }

    public abstract Boolean isValid();

    public abstract <A> void forEachClass (CallbackFunction<A> callback) throws IOException;

    public abstract InputStream getClassStream(String classname) throws IOException;
}
