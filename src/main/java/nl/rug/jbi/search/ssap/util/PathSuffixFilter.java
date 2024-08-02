package nl.rug.jbi.search.ssap.util;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.filefilter.AbstractFileFilter;

public class PathSuffixFilter extends AbstractFileFilter {
    public String suffix;

    public PathSuffixFilter(final String suffix) {
        this.suffix = suffix;
    }

    public boolean accept(File file) {
        try {
            return file.getCanonicalPath().endsWith(suffix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
