package edu.washington.biostr.sig.volume;

import java.util.Collection;
import java.util.Collections;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Encapsulate an atlas volume.  This uses an integer based IndexedVolumeArray
 * as the data set that backs this one.  It also uses AtlasElements for coloring
 * and to translate integers into structures. In most cases this will act like an
 * RGB volume.   Interpolation is always Nearest neighbor since interpolating 2
 * structures would be ill defined.
 * @author Eider Moore
 * @version 1.1
 */
public class IndexedAtlasVolumeArray extends IndexedVolumeArray {

    private IndexedVolumeArray backing;
    private Int2ObjectMap<AtlasElement> atlas;
    private Map<String, AtlasElement> abbrev2atlas;
    private Map<String, AtlasElement> id2atlas;
    private Collection<AtlasElement> atlasE;

    private Iterable<AtlasCollection> collections;
    
    /**
     * Create an atlas from the given integer volume and the mappings in <code>atlas</code>
     * @param backing The source data
     * @param atlas The integer to structure mapping.
     */
    public IndexedAtlasVolumeArray(VolumeArray backing, Int2ObjectMap<AtlasElement> atlas) {
        super(backing.getMaxX(), backing.getMaxY(), backing.getMaxZ(),
                backing.getMaxTime(), backing.getMaxI5(),
                backing.getIndex2Space());
        if (!backing.getNaturalType().equals(DataType.TYPE_INT)) {
            throw new IllegalArgumentException("Atlases must have an integer type!");
        }
        this.backing = (IndexedVolumeArray) backing;
        this.atlas = atlas;
        this.atlasE = Collections.unmodifiableCollection(atlas.values());
        this.abbrev2atlas = new HashMap<String, AtlasElement>(atlas.size() * 3 / 2);
        for (AtlasElement ae : atlas.values()) {
            abbrev2atlas.put(ae.getAbbreviation(), ae);
        }
        this.id2atlas = new HashMap<String, AtlasElement>(atlas.size() * 3 / 2);
        for (AtlasElement ae : atlas.values()) {
            id2atlas.put(ae.getUniqueId(), ae);
        }
    }
    
    /**
     * Create an atlas from the given integer volume and the mappings in <code>atlas</code>
     * @param backing The source data
     * @param atlas The structures
     */
    public IndexedAtlasVolumeArray(VolumeArray backing, Collection<AtlasElement> atlas) {
        super(backing.getMaxX(), backing.getMaxY(), backing.getMaxZ(),
                backing.getMaxTime(), backing.getMaxI5(),
                backing.getIndex2Space());
        if (!backing.getNaturalType().equals(DataType.TYPE_INT)) {
            throw new IllegalArgumentException("Atlases must have an integer type!");
        }
        this.atlas = new Int2ObjectOpenHashMap<AtlasElement>();
        for (AtlasElement ae : atlas) {
            this.atlas.put(ae.getInt(), ae);
        }
        this.backing = (IndexedVolumeArray) backing;
        this.atlasE = Collections.unmodifiableCollection(this.atlas.values());
        this.abbrev2atlas = new HashMap<String, AtlasElement>(atlas.size() * 3 / 2);
        for (AtlasElement ae : atlas) {
            abbrev2atlas.put(ae.getAbbreviation(), ae);
        }
    }

    /**
     * assign or reassign key to ae.
     * @param key the key is the value that represents the atlas element in the volume.
     * @param ae The atlas element to associate with key
     */
    public void assignAtlasElement(int key, AtlasElement ae) {
    	if (ae == null) {
    		ae = atlas.remove(key);
    	} else {
	        atlas.put(key, ae);
	        abbrev2atlas.put(ae.getAbbreviation(), ae);
	        id2atlas.put(ae.getUniqueId(), ae);
    	}
    }
    
    /**
     * Lookup an atlas element based on the abbreviation.
     * @param abbreviation The abbreviation
     * @return  The corresponding AtlasElement.
     */
    public AtlasElement lookupAbbreviation(String abbreviation) {
        return abbrev2atlas.get(abbreviation);
    }

    /**
     * Lookup an atlas element based on the unique id
     * @param id The unique id.
     * @return  The corresponding AtlasElement.
     */
    public AtlasElement lookupId(String id) {
        return id2atlas.get(id);
    }

    /**
     * Get a default way to group the elements.
     * @return An ordered list of collections.
     */
    public Iterable<AtlasCollection> getCollections() {
        if (collections == null) {
            AtlasCollection ac = new AtlasCollection("Structures");
            ac.addAll(getAtlas());
            collections = Collections.singletonList(ac);
        }
        return collections;
    }

    /**
     * Set the collection list to be collections. 
     * @param collections The list of collections.
     */
    public void setCollections(Iterable<AtlasCollection> collections) {
        this.collections = collections;
    }

    /**
     * Get a key that has not been used between 1 and max_value.
     * @param max_value The maximum value to search until.
     * @return The unused key.
     * @throws IndexOutOfBoundsException if there are no gaps.
     */
    public int getUnusedKey(int max_value) throws IndexOutOfBoundsException {
        // guess that these have been assigned from 1 to size
        for (int i = atlas.size() + 1; i < max_value; i++) {
            if (!atlas.containsKey(i))
                return i;
        }
        // search for any gaps
        for (int i = 0; i < atlas.size() + 1; i++) {
            if (!atlas.containsKey(i))
                return i;
        }
        
        throw new IndexOutOfBoundsException("Maximum size reached.");
    }

    /**
     * Get the structure for the given key.
     * @param key the key is the value that represents the atlas element in the volume.
     * @return The atlas element if it is assigned or null if not
     */
    public AtlasElement lookup(int key) {
        return atlas.get(key);
    }

    @Override
    public double getDouble(int index) {
        return getInt(index);
    }

    @Override
    public int getInt(int index) {
        int b = backing.getInt(index);
        if (b == 0) {
            return 0;
        } else {
            return atlas.get(b).getColor();
        }
    }

    @Override
    public Object mmGet(float x, float y, float z, int time, int i5) {
        int v = backing.mmGetAsInt(x, y, z, time, i5);
        return atlas.get(v);
    }

    @Override
    public void setData(int index, double value) {
        backing.setData(index, value);
    }

    @Override
    public void setData(int index, int value) {
        backing.setData(index, value);
    }

    @Override
    public DataType getNaturalType() {
        return DataType.TYPE_RGB;
    }

    @Override
    public DataType getType() {
        return DataType.TYPE_ATLAS;
    }

    @Override
    public VolumeArray map(VolumeFunction filter) {
        return backing.map(filter);
    }

    /**
     * Get the volume used as source data for this one.
     * @return The underlying integer volume
     */
    public IndexedVolumeArray getBacking() {
        return backing;
    }

    /**
     * Get all of the specified structures in this atlas.
     * @return All the structures
     */
    public Collection<AtlasElement> getAtlas() {
        return atlasE;
    }

    @Override
    public int getValueVoxelsInt(float x, float y, float z, int time, int i5,
            Interpolation in) {
        return getInt(quickRoundPositive(x), quickRoundPositive(y), quickRoundPositive(z), time, i5);
    }
    
    public void cleanupAtlasElements() {
    	IntSet set = new IntOpenHashSet();
    	IndexedIterator it = backing.iterator();
    	while (it.hasNext()) {
    		set.add(it.nextInt());
    	}
    	Iterator<AtlasElement> ait = atlas.values().iterator();
    	while (ait.hasNext()) {
    		AtlasElement ae = ait.next();
    		if (!set.contains(ae.getInt())) {
    			ait.remove();
    			abbrev2atlas.remove(ae.getAbbreviation());
    			id2atlas.remove(ae.getUniqueId());
    		}
    	}
    }
    
    @Override
    public Object getDataArray() {
    	return backing.getDataArray();
    }
}
