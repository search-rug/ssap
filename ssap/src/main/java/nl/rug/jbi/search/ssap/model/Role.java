package nl.rug.jbi.search.ssap.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("role")
public class Role {

    @XStreamAsAttribute
    public final String name;
    @XStreamAsAttribute
    public final String element;

    public Role(String element, String name) {
        this.element = element;
        this.name = name;
    }
}
