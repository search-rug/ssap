package nl.rug.jbi.search.ssap;

import nl.rug.jbi.search.ssap.util.DirContainer;
import nl.rug.jbi.search.ssap.util.CallbackFunction;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MockedContainer extends DirContainer {

    private static final Logger log = LogManager.getLogger(MockedContainer.class);
    private Map<String, byte[]> classBytes;

    private String[] classNames;

    public MockedContainer(String[] classNames) {
        super(null); // No file needed for mocking
        this.classBytes = new HashMap<>();
        try {
            loadClasses(classNames);
        } catch (IOException e) {
            log.error("Error while loading classes for mocking", e);
        }
    }

    /**
     * Add classes to classBytes
     * @throws IOException
     */
    private void loadClasses(String[] classNames) throws IOException {
        for(String className : classNames) {
            classBytes.put(className, ClassGenerator.generateClass(className));
        }
    }

    @Override
    public InputStream getClassStream(String classname) {
        byte[] bytes = classBytes.get(classname);
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        }
        return null;
    }

    @Override
    public <A> void forEachClass(CallbackFunction<A> callback) throws IOException {
        for (Map.Entry<String, byte[]> entry : classBytes.entrySet()) {
            try (InputStream is = new ByteArrayInputStream(entry.getValue())) {
                callback.run(is);
            }
        }
    }

    @Override
    public Boolean isValid() {
        return true; // Always true for mocking
    }
}

