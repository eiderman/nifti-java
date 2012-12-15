package edu.washington.biostr.sig.volume;

import java.awt.Color;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Encapsulate most of the information needed to specify an entry in an Atlas.<br>
 * The major information is a unique id, color, abbreviation, the key in the atlas
 * and a full name.  It can also support arbitrary information through the use
 * of a dictionary called info.
 * @author Eider Moore
 * @version 1.1
 */
public class AtlasElement implements Serializable, Comparable<AtlasElement> {

    private final String abbreviation;
    private int color;
    private final String uniqueId;
    private final Map<String, String> info;
    private final int value;
    private final String name;
    private String surfaceFile;

    /**
     * Parse the XML element of the form:<br>
     * <code>
     * &lt;Area abbrev="itps" id="H79" name="intraparietal sulcus"&gt;<br>
     * &nbsp;&lt;color b="0" g="0" r="255"/&gt;<br>
     * &lt;/Area&gt;<br>
     * </code>
     * @param xml
     */
    public AtlasElement(Element xml) {
        abbreviation = xml.getAttribute("abbrev");
        uniqueId = xml.getAttribute("id");
        info = new HashMap<String, String>();
        NamedNodeMap nnm = xml.getAttributes();
        value = Integer.parseInt(xml.getAttribute("value"));
        String name = xml.getAttribute("name");
        if (name == null) {
            this.name = abbreviation;
        } else {
            this.name = name;
        }

        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            info.put(n.getNodeName(), n.getNodeValue());
        }
        Element color = (Element) xml.getElementsByTagName("color").item(0);
        Color c = new Color(
                Integer.parseInt(color.getAttribute("r")),
                Integer.parseInt(color.getAttribute("g")),
                Integer.parseInt(color.getAttribute("b")));
        this.color = c.getRGB();
        NodeList surfaces = xml.getElementsByTagName("Surface");
        if (surfaces.getLength() > 0) {
            surfaceFile = surfaces.item(0).getTextContent().trim();
        }
    }
    
    /**
     * Create an XML Element for this node.  it will be the same format as 
     * that supported by the constructor.
     * @param doc
     * @return A new element
     */
    public Element toXML(Document doc) {
        Element xml = doc.createElement("Area");
        xml.setAttribute("abbrev", abbreviation);
        xml.setAttribute("id", uniqueId);
        NamedNodeMap nnm = xml.getAttributes();
        xml.setAttribute("value", Integer.toString(value));
        if (name != null)
            xml.setAttribute("name", name);

        for (Map.Entry<String, String> entry : info.entrySet()) {
            xml.setAttribute(entry.getKey(), entry.getValue());
        }
        Element c = doc.createElement("color");
        Color color = new Color(this.color);
        c.setAttribute("r", Integer.toString(color.getRed()));
        c.setAttribute("g", Integer.toString(color.getGreen()));
        c.setAttribute("b", Integer.toString(color.getBlue()));
        xml.appendChild(c);
        if (surfaceFile != null && surfaceFile.length() > 0) {
            Element surface = doc.createElement("Surface");
            surface.appendChild(doc.createTextNode(surfaceFile));
            xml.appendChild(surface);
        }
        return xml;
    }

    /**
     * Create an AtlasElement for a new Structure.
     * @param value The value in the volume that is keyed to this element.
     * @param abbreviation The preferred abbreviation for this structure.
     * @param color The preferred color.
     * @param name The name of this structure.
     * @param uniqueId A unique id, idealily it should be translatable to a structure in some common atlas.
     * @param info The dictionary of optional information.
     */
    public AtlasElement(int value,
            final String abbreviation, final int color, final String name,
            final String uniqueId, final Map<String, String> info) {
        this.value = value;
        this.name = name;
        this.abbreviation = abbreviation;
        this.color = color;
        this.uniqueId = uniqueId;
        this.info = new HashMap<String, String>(info);
    }

    /**
     * @return Get the color expressed as an int with ARGB (8 bits per channel).
     */
    public int getColor() {
        return color;
    }

    /**
     * @return the abbreviation.
     */
    public String getAbbreviation() {
        return abbreviation;
    }

    @Override
    public String toString() {
        return abbreviation;
    }

    /**
     * 
     * @return The unique id.
     */
    public String getUniqueId() {
        return uniqueId;
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((uniqueId == null) ? 0 : uniqueId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AtlasElement other = (AtlasElement) obj;
        if (uniqueId == null) {
            if (other.uniqueId != null) {
                return false;
            }
        } else if (!uniqueId.equals(other.uniqueId)) {
            return false;
        }
        return true;
    }

    /**
     * 
     * @param key A key in the dictonary.
     * @return The value in the dictonary for key.
     */
    public String getInfo(String key) {
        return info.get(key);
    }

    /**
     * 
     * @return The value used to represent this in the volume.
     */
    public int getInt() {
        return value;
    }

    public int compareTo(AtlasElement o) {
        int v = name.compareToIgnoreCase(o.name);
        if (v == 0) {
            v = name.compareTo(o.name);
            if (v == 0) {
                v = abbreviation.compareTo(o.abbreviation);
                if (v == 0) {
                    return value - o.value;
                } else {
                    return v;
                }
            } else {
                return v;
            }
        } else {
            return v;
        }
    }

    /**
     * 
     * @return The long name for this structure.
     */
    public String getName() {
        return name;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getSurfaceFile() {
        return surfaceFile;
    }

    public void setSurfaceFile(String surface) {
        this.surfaceFile = surface;
    }
    
    
}
