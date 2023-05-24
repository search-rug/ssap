package nl.rug.jbi.search.ssap.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("role")
public class Role {

    @XStreamAsAttribute
    private final String element;
    @XStreamAsAttribute
    private final String name;

    private Role(String element, String name) {
        this.element = element;
        this.name = name;
    }
}
