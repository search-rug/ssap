package nl.rug.jbi.search.ssap.util;

import java.io.InputStream;

/**
 * Represents the container of a Java project, from which one can access the project's .class files.
 *
 * @author Andrei Dumitriu, Daniel Feitosa
 */

public interface CallbackFunction<A> {
    A run(InputStream callback);
}
