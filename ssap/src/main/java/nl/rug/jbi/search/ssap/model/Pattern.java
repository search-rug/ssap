package nl.rug.jbi.search.ssap.model;

import com.thoughtworks.xstream.annotations.XStreamImplicit;
import nl.rug.jbi.search.ssap.util.Constants;

import java.util.List;
import java.util.Objects;

public class Pattern {
    public final String name;
    @XStreamImplicit
    public List<Instance> instanceList;

    private Pattern (String name, List<Instance> instanceList) {
        this.name = name;
        this.instanceList = instanceList;
    }

    public String getName () {
        return this.name;
    }

    public String getGroup (String name) {
        switch (name) {
            case Constants.FACTORY_METHOD:
            case Constants.SINGLETON:
            case Constants.PROTOTYPE:
                return Constants.CREATIONAL;
            case Constants.ADAPTER_COMMAND: return Constants.STRUCTURAL_BEHAVIORAL;
            case Constants.COMPOSITE:
            case Constants.DECORATOR:
            case Constants.PROXY:
            case Constants.PROXY2:
                return Constants.STRUCTURAL;
            case Constants.OBSERVER:
            case Constants.STATE_STRATEGY:
            case Constants.TEMPLATE_METHOD:
            case Constants.VISITOR:
                return Constants.BEHAVIORAL;
            default: return "";
        }
    }

    @Override
    public boolean equals (Object obj) {
        if (Pattern.class == obj.getClass()) {
            return Objects.equals(((Pattern) obj).name, name);
        }
        return false;
    }

    @Override
    public int hashCode () {
        return name.hashCode();
    }
}
