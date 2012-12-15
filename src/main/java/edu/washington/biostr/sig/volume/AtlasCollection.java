package edu.washington.biostr.sig.volume;

import edu.washington.biostr.sig.volume.AtlasElement;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

/**
 * Hold the AtlasElements in a collection that is used to organize and display
 * the data.
 * @author Eider Moore
 * @version 1.2
 */
public class AtlasCollection extends TreeSet<AtlasElement> {
    private String name;

    /**
     * Create a new structure.
     * @param name The name that will be displayed
     */
    public AtlasCollection(String name) {
        super();
        this.name = name;
    }
    
    /**
     * Get the name of this set.
     * @return The name that is displayed.
     */
    public String getName() {
        return name;
    }
}
