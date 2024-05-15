package nl.rug.jbi.search.ssap.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;


public class DirContainerTest {

    private static final Logger log = LogManager.getLogger(DirContainerTest.class);

    @Test
    public void testIsValid() {
        File validDir = new File("src/test/resources/Example_Directory");
        DirContainer dc = new DirContainer(validDir);
        assertTrue(dc.isValid());
    }

    @Test
    public void testForEachClass() {
        AtomicInteger callCount = new AtomicInteger();
        callCount.set(0);
        CallbackFunction<Void> callback = (is) -> {
            callCount.getAndIncrement();
        };

        DirContainer dc = new DirContainer(new File("src/test/resources/Example_Directory"));
        try {
            dc.forEachClass(callback);
        } catch (IOException e) {
            log.error("IO exception while testing DirContainer", e);
        }

        assertEquals(3, callCount.get());
    }

    @Test
    public void testGetClassStream() {
        String className = "src.test.resources.Example_Directory.Calculator";
        File project = new File("src/test/resources/Example_Directory");

        DirContainer dc = new DirContainer(project);

        InputStream result = null;
        try {
            result = dc.getClassStream(className);
        } catch (FileNotFoundException e) {
            log.error("File Not Found exception while testing DirContainer", e);
        }

        assertNotNull(result);
    }
}
