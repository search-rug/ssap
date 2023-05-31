package nl.rug.jbi.search.ssap.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.util.List;

@XStreamAlias("system")
public class System {
    @XStreamImplicit
    private List<Pattern> patternList;

    private System (List<Pattern> patternList) {
        this.patternList = patternList;
    }
}
