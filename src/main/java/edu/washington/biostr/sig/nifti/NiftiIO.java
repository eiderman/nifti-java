package edu.washington.biostr.sig.nifti;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteOrder;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Matrix4d;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.washington.biostr.sig.nifti.AnalyzeNiftiSpmHeader;
import edu.washington.biostr.sig.nifti.NiftiFile;
import edu.washington.biostr.sig.niftivolume.NiftiStream;
import edu.washington.biostr.sig.volume.AtlasCollection;
import edu.washington.biostr.sig.volume.AtlasElement;
import edu.washington.biostr.sig.volume.ByteEncoder;
import edu.washington.biostr.sig.volume.IndexedAtlasVolumeArray;
import edu.washington.biostr.sig.volume.RGBIndexedVolumeArray;
import edu.washington.biostr.sig.volume.UnsignedByteIndexedVolumeArray;
import edu.washington.biostr.sig.volume.VolumeArray;
import edu.washington.biostr.sig.volume.VolumeArrayFactory;
import it.unimi.dsi.fastutil.io.FastBufferedOutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eiderman.util.FileUtilities;

/**
 * Handle IO for converting Nifti files into volumes. This can save the volume
 * or load it. It also handles our xml special Atlas files for loading (you must
 * explicitly save them though).
 * 
 * @author Eider Moore
 */
public class NiftiIO {

  public static class VolumePair {
    private final AnalyzeNiftiSpmHeader header;
    private final VolumeArray array;

    public VolumePair(AnalyzeNiftiSpmHeader header, VolumeArray array) {
      super();
      this.header = header;
      this.array = array;
    }

    public VolumeArray getArray() {
      return array;
    }

    public AnalyzeNiftiSpmHeader getHeader() {
      return header;
    }
  }

  /**
   * @param hdr
   *          The header file, the image and maybe mat file will be
   *          automatically found.
   * @return {AnalyzeNiftiSpmHeader, VolumeArray}
   * @throws IOException
   */
  public static VolumePair load(URL hdr) throws IOException, URISyntaxException {
    NiftiFile file = new NiftiFile(hdr);
    return load(file);
  }

  /**
   * @param hdr
   *          The header file, the image and maybe mat file will be
   *          automatically found.
   * @return {AnalyzeNiftiSpmHeader, VolumeArray}
   * @throws IOException
   */
  public static VolumePair load(File hdr) throws IOException {
    NiftiFile file = new NiftiFile(hdr);
    return load(file);
  }

  /**
   * @param file
   *          A NiftiFile
   * @return {AnalyzeNiftiSpmHeader, VolumeArray}
   * @throws IOException
   */
  public static VolumePair load(NiftiFile file) throws IOException {
    AnalyzeNiftiSpmHeader header = file.getHeader();

    Matrix4d index2space = new Matrix4d(file.getTransform());

    VolumeArray img;
    short[] dim = header.getDim();
    if (header.getDatatype() == AnalyzeNiftiSpmHeader.DT_RGB24) {
      int[] data = (int[]) file.getArray();
      img = new RGBIndexedVolumeArray(dim[1], dim[2], dim[3], dim[4] == 0 ? 1
          : dim[4], dim[5] == 0 ? 1 : dim[5], index2space, data);

    } else if (header.getDatatype() != AnalyzeNiftiSpmHeader.DT_UINT8) {
      byte[] b = file.getDataBytes();
      img = new UnsignedByteIndexedVolumeArray(dim[1], dim[2], dim[3],
          dim[4] == 0 ? 1 : dim[4], dim[5] == 0 ? 1 : dim[5], index2space, b);
    } else {
      Object data = file.getArray();
      img = VolumeArrayFactory.getVolumeDataBuffer(index2space, data, dim[1],
          dim[2], dim[3], dim[4] == 0 ? 1 : dim[4], dim[5] == 0 ? 1 : dim[5]);
    }

    if ((header.getIntentCode() == AnalyzeNiftiSpmHeader.NIFTI_INTENT_LABEL || header
        .getIntentCode() == AnalyzeNiftiSpmHeader.NIFTI_INTENT_NEURONAME)
        && file.getAtlas() != null) {
      img = AtlasIO.loadAtlas(file.getAtlas(), img);
    }
    return new VolumePair(header, img);
  }



  /**
   * @param files
   *          the files to load (hdr and img or nii).
   * @return {AnalyzeNiftiSpmHeader, VolumeArray}
   * @throws IOException
   */
  public static VolumePair load(URL... files) throws IOException {
    NiftiFile file = new NiftiFile(files);
    return load(file);
  }

  /**
   * Save a VolumeArray to a file.
   * 
   * @param arr
   *          the array
   * @param hdr
   *          the target for the hdr file (or the nii for a single file)
   * @param img
   *          the target for the img file (or null for a single file)
   * @param sformCode
   *          The transform code, see Nifti Documentation
   * @param intent
   *          The intent code, see Nifti Documentation
   * @throws java.io.IOException
   */
  public static void save(VolumeArray arr, File hdr, File img, short sformCode,
      short intent) throws IOException {
    save(arr, new FileOutputStream(hdr), img == null ? null
        : new FileOutputStream(img), sformCode, intent);
  }

  /**
   * Generate a header taking information from that array.
   * 
   * @param array
   *          The source array
   * @param singleFile
   *          whether to make a header for an nii file or an img/hdr pair.
   * @param sformCode
   *          The transform code, see Nifti Documentation
   * @param intent
   *          The intent code, see Nifti Documentation
   * @return a new header.
   */
  public static AnalyzeNiftiSpmHeader generateHeader(VolumeArray array,
      boolean singleFile, short sformCode, short intent) {
    AnalyzeNiftiSpmHeader header = new AnalyzeNiftiSpmHeader(
        ByteOrder.BIG_ENDIAN, singleFile);

    if (sformCode == 0) {
      sformCode = AnalyzeNiftiSpmHeader.NIFTI_XFORM_SCANNER_ANAT;
    }

    double[] trans = getTrans(array.getIndex2Space());
    header.setSTrans(trans);
    header.setDatatype((short) array.getType().getValue());

    header.setDim(new short[] { 5, (short) array.getMaxX(),
        (short) array.getMaxY(), (short) array.getMaxZ(),
        (short) array.getMaxTime(), (short) array.getMaxI5(), 0, 0 });
    double scale1 = trans[0] + trans[4] + trans[8];
    double scale2 = trans[1] + trans[5] + trans[9];
    double scale3 = trans[2] + trans[6] + trans[10];
    header.setPixdim(new float[] { 5, (float) Math.abs(scale1),
        (float) Math.abs(scale2), (float) Math.abs(scale3), 1, 1, 0, 0 });
    header.setSformCode(sformCode);
    header.setIntentCode(intent);
    header
        .setXyztUnits((byte) (AnalyzeNiftiSpmHeader.NIFTI_UNITS_MM | AnalyzeNiftiSpmHeader.NIFTI_UNITS_SEC));
    header.setCalMax((float) array.getImageMax());
    header.setCalMin((float) array.getImageMin());
    return header;
  }

  /**
   * Save a VolumeArray to a file.
   * 
   * @param arr
   *          the array
   * @param hdr
   *          the target for the hdr file (or the nii for a single file)
   * @param img
   *          the target for the img file (or null for a single file)
   * @param sformCode
   *          The transform code, see Nifti Documentation
   * @param intent
   *          The intent code, see Nifti Documentation
   * @throws java.io.IOException
   */
  public static void save(VolumeArray array, OutputStream hdr,
      OutputStream img, short sformCode, short intent) throws IOException {
    AnalyzeNiftiSpmHeader header = generateHeader(array, img == null,
        sformCode, intent);
    save(header, array, hdr, img);
  }

  /**
   * Save a VolumeArray to a file.
   * 
   * @param header
   *          The header file to use
   * @param arr
   *          the array
   * @param hdr
   *          the target for the hdr file (or the nii for a single file)
   * @param img
   *          the target for the img file (or null for a single file)
   * @throws java.io.IOException
   */
  public static void save(AnalyzeNiftiSpmHeader header, VolumeArray array,
      OutputStream hdr, OutputStream img) throws IOException {
    if (array instanceof IndexedAtlasVolumeArray) {
      array = ((IndexedAtlasVolumeArray) array).getBacking();
    }
    if (!header.isSingleNIFTIFile() && img == null) {
      throw new IllegalArgumentException("An img stream must be present for a "
          + "multi file header.");
    }
    hdr = new FastBufferedOutputStream(hdr);
    header.write(hdr);

    ByteEncoder imge;
    // now start writing the data
    if (img == null) {
      imge = new ByteEncoder(hdr, header.getEndian());
    } else {
      imge = new ByteEncoder(new FastBufferedOutputStream(img),
          header.getEndian());
    }
    array.write(imge);
    hdr.flush();
    if (img != null) {
      imge.getOut().flush();
      img.flush();
      img.close();
    }
    hdr.close();
  }

  /**
   * Save a VolumeArray as a stream. This allows manual control over saving and
   * is useful for compositing an array or otherwise not keeping everything in
   * memory. It outputs the header and returns an object appropriate for
   * streaming the rest.
   * 
   * @param header
   *          the header to save.
   * @param hdr
   *          the target for the hdr file (or the nii for a single file)
   * @param img
   *          the target for the img file (or null for a single file)
   * @return A NiftiStream that handles data conversions.
   * @throws java.io.IOException
   */
  public static NiftiStream saveAsStream(AnalyzeNiftiSpmHeader header,
      OutputStream hdr, OutputStream img) throws IOException {
    if (!header.isSingleNIFTIFile() && img == null) {
      throw new IllegalArgumentException("An img stream must be present for a "
          + "multi file header.");
    }
    hdr = new BufferedOutputStream(hdr, 1024 * 8);
    header.write(hdr);
    hdr.flush();
    ByteEncoder imge;
    // now start writing the data
    if (img == null) {
      imge = new ByteEncoder(hdr, header.getEndian());
    } else {
      imge = new ByteEncoder(new BufferedOutputStream(img, 1024 * 8),
          header.getEndian());
    }
    return new NiftiStream(header, imge);
  }

  private static double[] getTrans(Matrix4d mat) {
    double[] trans = new double[16];
    double[] row = new double[4];
    for (int i = 0; i < 4; i++) {
      mat.getRow(i, row);
      for (int j = 0; j < 4; j++) {
        trans[4 * i + j] = row[j];
      }
    }
    return trans;
  }
}
