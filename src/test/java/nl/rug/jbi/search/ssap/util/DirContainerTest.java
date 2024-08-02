package nl.rug.jbi.search.ssap.util;

import org.junit.Test;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;


public class DirContainerTest {

    @Test
    public void testIsValid() {
        File validDir = new File("src/test/resources/Example_Directory");
        DirContainer dc = new DirContainer(validDir);
        assertTrue(dc.isValid());
    }

    @Test
    public void testForEachClass() throws IOException {
        AtomicInteger callCount = new AtomicInteger();
        callCount.set(0);
        CallbackFunction<Void> callback = (is) -> {
            callCount.getAndIncrement();
        };

        DirContainer dc = new DirContainer(new File("src/test/resources/Example_Directory"));
        dc.forEachClass(callback);

        assertEquals(3, callCount.get());
    }

    @Test
    public void testGetClassStream() throws FileNotFoundException {
        String className = "src.test.resources.Example_Directory.Calculator";
        File project = new File("src/test/resources/Example_Directory");

        DirContainer dc = new DirContainer(project);

        InputStream result = dc.getClassStream(className);;

        assertNotNull(result);
    }
}
