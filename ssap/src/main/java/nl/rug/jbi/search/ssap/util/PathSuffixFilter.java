package nl.rug.jbi.search.ssap.util;

import java.io.File;
import java.io.IOException;

public class PathSuffixFilter {
    public String suffix;

    public PathSuffixFilter(final String suffix) {
        this.suffix = suffix;
    }

    public Boolean accept(File file) throws IOException {
        return file.getCanonicalPath().endsWith(suffix);
    }
}
