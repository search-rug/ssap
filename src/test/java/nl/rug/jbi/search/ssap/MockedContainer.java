package nl.rug.jbi.search.ssap;

import nl.rug.jbi.search.ssap.util.DirContainer;
import nl.rug.jbi.search.ssap.util.CallbackFunction;


import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MockedContainer extends DirContainer {

    private Map<String, byte[]> classBytes;

    private String[] classNames;

    public MockedContainer(String[] classNames) throws IOException {
        super(null); // No file needed for mocking
        this.classBytes = new HashMap<>();
        loadClasses(classNames);
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

