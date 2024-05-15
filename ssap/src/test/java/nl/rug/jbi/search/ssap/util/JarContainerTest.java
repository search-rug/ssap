package nl.rug.jbi.search.ssap.util;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class JarContainerTest {

    private static final Logger log = LoggerFactory.getLogger(JarContainerTest.class);

    private JarContainer getContainer(File jar) {
        try{
            return new JarContainer(jar);
        } catch (IOException e) {
            log.error("IO exception while testing JarContainer", e);
            return null;
        }
    }

    @Test
    public void testIsValid() {
        File jar = new File("src/test/resources/Example_Project.jar");
        JarContainer jc = getContainer(jar);

        assertTrue(jc.isValid());
    }

    @Test
    public void testForEachClass() {
        AtomicInteger callCount = new AtomicInteger();
        callCount.set(0);

        CallbackFunction<Void> callback = (is) -> {
            callCount.getAndIncrement();
        };

        File jar = new File("src/test/resources/Example_Project.jar");
        JarContainer jc = getContainer(jar);
        try {
            assert jc != null;
            jc.forEachClass(callback);
        } catch (IOException e) {
            log.error("IO exception while testing JarContainer", e);
        }

        assertEquals(3, callCount.get());
    }

    @Test
    public void testGetClassStream() {
        File jar = new File("src/test/resources/Example_Project.jar");
        JarContainer jc = getContainer(jar);

        String className = "Main.class";

        InputStream result = null;
        try {
            assert jc != null;
            result = jc.getClassStream(className);
        } catch (IOException e) {
            log.error("IO exception while testing JarContainer", e);
        }

        assertNotNull(result);
    }
}
