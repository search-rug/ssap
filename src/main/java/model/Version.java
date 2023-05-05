package main.java.model;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import scala.Int;

public class Version {
    private Integer order;
    private String name;
    private Version(Integer order, String name) {
        this.order = order;
        this.name = name;
    }

    /**
     *
     * @param element XML element with the attributes "order" and "name"
     * @return Version object with values of "order" and "name"
     */
    public Version fromXML(Element element) {
        String orderString = element.getAttribute("order");
        String name = element.getAttribute("name");
        Integer order = 0;
        try {
            order = Integer.valueOf(orderString);
        } catch (NumberFormatException exception) {
            exception.printStackTrace();
            System.out.println("order attribute is not a number; setting to default value 0");
        }
        return new Version(order, name);
    }

    public Element toXML(Document doc) {
        Element version = doc.createElement("version");
        version.setAttribute("order", order.toString());
        version.setAttribute("name", name);
        return version;
    }
}

