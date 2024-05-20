package nl.rug.jbi.search.ssap.model;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
@XStreamAlias("version")
public class Version {
    @XStreamAsAttribute
    private final Integer order;
    @XStreamAsAttribute
    private final String name;

    private Version(Integer order, String name) {
        this.order = order;
        this.name = name;
    }
}
