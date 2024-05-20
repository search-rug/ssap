package nl.rug.jbi.search.ssap.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class JarContainerTest {

    @Test
    public void testIsValid() throws IOException {
        File jar = new File("src/test/resources/Example_Project.jar");
        JarContainer jc = new JarContainer(jar);

        assertTrue(jc.isValid());
    }

    @Test
    public void testForEachClass() throws IOException {
        AtomicInteger callCount = new AtomicInteger();
        callCount.set(0);

        CallbackFunction<Void> callback = (is) -> {
            callCount.getAndIncrement();
        };

        File jar = new File("src/test/resources/Example_Project.jar");
        JarContainer jc = new JarContainer(jar);

        assertNotNull(jc);
        jc.forEachClass(callback);
        assertEquals(3, callCount.get());
    }

    @Test
    public void testGetClassStream() throws IOException {
        File jar = new File("src/test/resources/Example_Project.jar");
        JarContainer jc = new JarContainer(jar);
        assertNotNull(jc);

        String className = "Main.class";

        InputStream result  = jc.getClassStream(className);
        assertNotNull(result);
    }
}
