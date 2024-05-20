package nl.rug.jbi.search.ssap.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import java.util.HashSet;
import java.util.List;

@XStreamAlias("instance")
public class Instance {

    @XStreamOmitField
    private long id;

    @XStreamOmitField
    private static long currId = -1;

    @XStreamImplicit
    public List<Role> roleList;
    @XStreamImplicit
    private List<Version> versionList;

    private Instance (List<Role> roleList, List<Version> versionList) {
        this.roleList = roleList;
        currId++;
        this.id = currId;
        this.versionList = versionList;
    }

    public static void resetId () {
        currId = -1;
    }

    @Override
    public boolean equals (Object obj) {
        if (obj.getClass() == Instance.class) {
            return roleList.size() == ((Instance) obj).roleList.size() &&
                    new HashSet<>(roleList).containsAll(((Instance) obj).roleList);
        }
        return false;
    }

    @Override
    public int hashCode () {
        return roleList.hashCode();
    }
}
