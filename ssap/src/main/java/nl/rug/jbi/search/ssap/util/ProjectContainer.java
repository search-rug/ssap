package nl.rug.jbi.search.ssap.util;

import javax.security.auth.callback.Callback;
import java.io.File;
import java.io.InputStream;

/**
 * Represents the container of a Java project, from which one can access the project's .class files.
 *
 * @author Andrei Dumitriu, Daniel Feitosa
 */
public abstract class ProjectContainer implements AutoCloseable {

    public final File project;

    protected ProjectContainer(File project) {
        this.project = project;

        if(!isValid()) {
            throw new IllegalArgumentException(project + " isn't a valid project");
        }
    }

    public abstract Boolean isValid();

    // forEachClass

    public abstract InputStream getClassStream(String classname);
}
