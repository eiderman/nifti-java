package edu.washington.biostr.sig.nifti;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eiderman.util.FileUtilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.biostr.sig.volume.AtlasCollection;
import edu.washington.biostr.sig.volume.AtlasElement;
import edu.washington.biostr.sig.volume.IndexedAtlasVolumeArray;
import edu.washington.biostr.sig.volume.VolumeArray;

public class AtlasIO {
  public static Document makeAtlasXML(IndexedAtlasVolumeArray atlas) {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .newDocument();
      Element root = doc.createElement("Atlas");
      for (AtlasElement ae : atlas.getAtlas()) {
        root.appendChild(ae.toXML(doc));
      }
      doc.appendChild(root);
      return doc;
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Load a collection file and create the collections. Collections are ways to
   * organize data that are not mutually exclusive and are not hierarchical.
   * 
   * @param collectionFile
   *          the url to the file
   * @param atlas
   *          The avaliable atlas elements
   * @return an ordererd list of AtlasCollections
   * @throws java.io.IOException
   */
  public static Iterable<AtlasCollection> getAtlasCollections(URL collectionFile,
      Collection<AtlasElement> atlas) throws IOException {
    try {
      Map<String, AtlasElement> index = new HashMap<String, AtlasElement>();
      for (AtlasElement ae : atlas) {
        index.put(ae.getAbbreviation(), ae);
        index.put(ae.getUniqueId(), ae);
      }
      List<AtlasCollection> result = new ArrayList<AtlasCollection>();
      if (FileUtilities.touchURL(collectionFile)) {
        Document doc = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder().parse(collectionFile.openStream());
        NodeList nl = doc.getDocumentElement().getElementsByTagName(
            "Collection");
        for (int i = 0; i < nl.getLength(); i++) {
          Element e = (Element) nl.item(i);
          NodeList children = e.getElementsByTagName("Element");
          AtlasCollection collection = new AtlasCollection(
              e.getAttribute("name"));
          for (int j = 0; j < children.getLength(); j++) {
            Element child = (Element) children.item(j);
            String id = child.getAttribute("id");
            if (id == null || id.length() == 0) {
              // not very efficient
              id = child.getAttribute("abbrev");
            }
            AtlasElement ae = index.get(id);
            if (ae != null) {
              collection.add(ae);
            }
          }
          result.add(collection);
        }
      }
      // add the residual
      AtlasCollection other = new AtlasCollection(
          result.isEmpty() ? "Structures" : "Other Structures");
      other.addAll(atlas);
      for (AtlasCollection ac : result) {
        other.removeAll(ac);
      }
      if (other.size() > 0) {
        result.add(other);
      }
      return result;
    } catch (SAXException e) {
      throw new IllegalStateException(e);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }
  
  /**
   * Load an atlas volume
   * 
   * @param atlas
   *          The url to the atlas.xml index file
   * @param img
   *          The backing array with the atlas data
   * @return an IndexedAtlasVolumeArray.
   * @throws java.io.IOException
   */
  public static Collection<AtlasElement> loadAtlas(URL atlas)
      throws IOException {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(atlas.openStream());
      NodeList nl = doc.getDocumentElement().getElementsByTagName("Area");
      Int2ObjectMap<AtlasElement> atlasElements = new Int2ObjectOpenHashMap<AtlasElement>();
      for (int i = 0; i < nl.getLength(); i++) {
        Element e = (Element) nl.item(i);
        AtlasElement ae = new AtlasElement(e);
        atlasElements.put(ae.getInt(), ae);
      }
      return atlasElements.values();
    } catch (SAXException e) {
      throw new IllegalStateException(e);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }



  /**
   * Load an atlas volume
   * 
   * @param atlas
   *          The url to the atlas.xml index file
   * @param img
   *          The backing array with the atlas data
   * @return an IndexedAtlasVolumeArray.
   * @throws java.io.IOException
   */
  public static IndexedAtlasVolumeArray loadAtlas(URL atlas, VolumeArray img)
      throws IOException {
    try {
      Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
          .parse(atlas.openStream());
      NodeList nl = doc.getDocumentElement().getElementsByTagName("Area");
      Int2ObjectMap<AtlasElement> atlasElements = new Int2ObjectOpenHashMap<AtlasElement>();
      for (int i = 0; i < nl.getLength(); i++) {
        Element e = (Element) nl.item(i);
        AtlasElement ae = new AtlasElement(e);
        atlasElements.put(ae.getInt(), ae);
      }
      
      addOptionalCollections(atlas, img);
      
      return new IndexedAtlasVolumeArray(img, atlasElements);
    } catch (SAXException e) {
      throw new IllegalStateException(e);
    } catch (ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
  }

  private static void addOptionalCollections(URL atlas, VolumeArray img) throws IOException {

    try {
      URL[] colUrl = FileUtilities.findURLs(atlas,
          new String[] { "collection.xml" });
      if (colUrl.length == 1) {
        URL uri = colUrl[0];
        IndexedAtlasVolumeArray atlasArray = (IndexedAtlasVolumeArray) img;
        atlasArray.setCollections(getAtlasCollections(uri,
            atlasArray.getAtlas()));
      }
    } catch (MalformedURLException ex) {
      // no collection, oh well...
    } catch (URISyntaxException ex) {
      // no collection, oh well...
    }
  }

  
}
