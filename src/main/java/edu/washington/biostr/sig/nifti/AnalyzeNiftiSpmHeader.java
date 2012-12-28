/*
 * Java NIFTI/SPM Library
 * Copyright (C) 2006-2007 University of Washington
 * Author Eider Moore
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package edu.washington.biostr.sig.nifti;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;

import org.eiderman.util.Compression;
import org.eiderman.util.FileUtilities;

import edu.washington.biostr.sig.matfile.MatFileLoaderFactory;
import edu.washington.biostr.sig.matfile.MatVar;
import edu.washington.biostr.sig.matfile.MatfileLoader;
import edu.washington.biostr.sig.volume.ByteEncoder;

/**
 * This takes a ByteBuffer for the header of the file and returns various pieces
 * of useful information.  <br>
 * It gets the parameters on an as needed basis.<br>
 * It provides methods to parse the file that handle standard Analyze 7.5,
 * SPM's version and NIFTI 1.  It also will provide some convenience methods.
 * For example, getTransform() tries to handle this as a NIFTI file, then an SPM
 * file (if the flag is set) and finally as a standard Analyze 7.5 file.  I have
 * tried to make version specific functions in the comments with (NIFTI) or (SPM).
 * IMPLEMENTATION NOTE: Endian is determined by checking dim[0].  This value must
 * be in the range 1..7, so if it is not, then we know we have the
 * wrong endian.  I think this is the preferred method to check the byte order.
 * The image file should have the same byte order.<br>
 * @author Eider Moore
 * @version 1.0
 */
public class AnalyzeNiftiSpmHeader
{
   public final static int NIFTI_INTENT_VECTOR = 0;
   /**
    * Correlation coefficient R (1 param)<br>
    * p1 = degrees of freedom<br>
    * R/sqrt(1 - R * R) is t distributed with p1 DOF.
    */
   public final static int NIFTI_INTENT_CORREL = 2;
   /**
    * Student t statistic (1 param): p1 = DOF
    */
   public final static int NIFTI_INTENT_TTEST = 3;
   /**
    * Fisher F statistics (2 param): <br>
    * p1=numerator DOF, p2 = denominator DOF
    */
   public final static int NIFTI_INTENT_FTEST = 4;
   /**
    * Standard normal: Density = N(0, 1).
    */
   public final static int NIFTI_INTENT_ZSCORE = 5;
   /**
    * Chi squared (1 param): p1 = DOF<br>
    * Density(x) proportional to exp(-x/2) * x^(p1/2 - 1)
    */
   public final static int NIFTI_INTENT_CHISQ = 6;
   /**
    * Beta distribution (2 param) p1 = a, p2 = b
    * Density(x) proportional to x^(a-1) * (1 - x)^(b - 1)
    */
   public final static short NIFTI_INTENT_BETA = 7;
   public final static short NIFTI_INTENT_BINOM = 8;
   public final static short NIFTI_INTENT_GAMMA = 9;
   public final static short NIFTI_INTENT_POISSON = 10;
   public final static short NIFTI_INTENT_NORMAL = 11;
   public final static short NIFTI_INTENT_FTEST_NONC = 12;
   public final static short NIFTI_INTENT_CHISQ_NONC = 13;
   public final static short NIFTI_INTENT_LOGISTIC = 14;
   public final static short NIFTI_INTENT_LAPLACE = 15;
   public final static short NIFTI_INTENT_UNIFORM = 16;
   public final static short NIFTI_INTENT_TTEST_NONC = 17;
   public final static short NIFTI_INTENT_WEIBULL = 18;
   public final static short NIFTI_INTENT_CHI = 19;
   public final static short NIFTI_INTENT_INVGAUSS = 20;
   public final static short NIFTI_INTENT_EXTVAL = 21;
   public final static short NIFTI_INTENT_PVAL = 22;

   public final static short NIFTI_INTENT_ESTIMATE = 1001;
   public final static short NIFTI_INTENT_LABEL = 1002;
   public final static short NIFTI_INTENT_NEURONAME = 1003;
   public final static short NIFTI_INTENT_GENMATRIX = 1004;
   public final static short NIFTI_INTENT_SYMMATRIX = 1005;
   public final static short NIFTI_INTENT_DISPVECT = 1006;
   public final static short NIFTI_INTENT_VECTOR_OTHER = 1007;
   public final static short NIFTI_INTENT_POINTSET = 1008;
   public final static short NIFTI_INTENT_TRIANGLE = 1009;
   public final static short NIFTI_INTENT_QUATERNION = 1010;

   /**
    * Arbitraty coordinates
    */
   public final static short NIFTI_XFORM_UNKNOWN = 0;

   /**
    * Scanner-based anatomical coordinates.
    */
   public final static short NIFTI_XFORM_SCANNER_ANAT = 1;
   /**
    * Coordinates aligned to another file's or to anatomical truth.
    */
   public final static short NIFTI_XFORM_ALIGNED_ANAT = 2;
   /**
    * Coordinates in Talairach-Tournoux Atlas.  AC = (0,0,0)
    */
   public final static short NIFTI_XFORM_TALAIRACH = 3;
   /**
    * Coordinates in MNI 152 normalized.
    */
   public final static short NIFTI_XFORM_MNI_152 = 4;

   /**
    * none...
    */
   public final static short DT_NONE = 0;
   /**
    * I don't know either.
    */
   public final static short DT_UNKNOWN = 0;
   /**
    * binary 1 bit/voxel
    */
   public final static short DT_BINARY = 1;
   /**
    * in C an unsigned char, in Java this is an unsigned byte, but you will have
    * to unsign it yourself and map it to a short. 8 bits/voxel
    */
   public final static short DT_UNSIGNED_CHAR = 2;
   /**
    * A short 16 bits/voxel
    */
   public final static short DT_SIGNED_SHORT = 4;
   /**
    * An int, 32 bits/voxel
    */
   public final static short DT_SIGNED_INT = 8;
   /**
    * a single float 32 bits/voxel
    */
   public final static short DT_FLOAT = 16;
   /**
    * complex (2 floats) 64 bits/voxel
    */
   public final static short DT_COMPLEX = 32;
   /**
    * a double float 64 bits/voxel
    */
   public final static short DT_DOUBLE = 64;
   /**
    * RGB triple 24 bits/voxel
    */
   public final static short DT_RGB = 128;
   /**
    * I don't have a clue...
    */
   public final static short DT_ALL = 255;
   /**
    * in C an unsigned char, in Java this is an unsigned byte, but you will have
    * to unsign it yourself and map it to a short. 8 bits/voxel
    */
   public final static short DT_UINT8 = 2;
   /**
    * A short 16 bits/voxel
    */
   public final static short DT_INT16 = 4;
   /**
    * An int, 32 bits/voxel
    */
   public final static short DT_INT32 = 8;
   /**
    * a single float 32 bits/voxel
    */
   public final static short DT_FLOAT32 = 16;
   /**
    * complex (2 floats) 64 bits/voxel
    */
   public final static short DT_COMPLEX64 = 32;
   /**
    * a double float 64 bits/voxel
    */
   public final static short DT_FLOAT64 = 64;
   /**
    * RGB triple 24 bits/voxel.
    */
   public final static short DT_RGB24 = 128;
   /**
    * A java byte 8 bits/ voxel
    */
   public final static short DT_INT8 = 256;
   /**
    * A java char (as opposed to a c char)
    * 16 bits / voxel
    */
   public final static short DT_UINT16 = 512;
   /**
    * An unsigned int in C.  In java this will have to mapped to a long by your
    * 32 bits/voxel
    * code.
    */
   public final static short DT_UINT32 = 768;
   /**
    * A long 64 bits/ voxel
    */
   public final static short DT_INT64 = 1024;
   /**
    * An unsigned long 64 bits/ voxel.  In Java you will have to map this up.
    * The only data type above it that I know of is either a BigInteger or a double.
    * The trade off is on performance (a BigInteger object) vs precision ( a double)
    */
   public final static short DT_UINT64 = 1280;

   /**
    * A long double (128 bits/voxel).  This will have to be represented by a
    * BigDecimal in Java (as far as I know).
    */
   public final static short DT_FLOAT128 = 1536;

   /**
    * A double pair (128 bits/voxel)
    */
   public final static short DT_COMPLEX128 = 1792;

   /**
    * A long double pair (256 bits/voxel)
    */
   public final static short DT_COMPLEX256 = 2048;


   // units codes for xyzt_units
   public static final byte NIFTI_UNITS_UNKNOWN = 0;
   public static final byte NIFTI_UNITS_METER   = 1;
   public static final byte NIFTI_UNITS_MM      = 2;
   public static final byte NIFTI_UNITS_MICRON  = 3;
   public static final byte NIFTI_UNITS_SEC     = 8;
   public static final byte NIFTI_UNITS_MSEC   = 16;
   public static final byte NIFTI_UNITS_USEC   = 24;
   public static final byte NIFTI_UNITS_HZ     = 32;
   public static final byte NIFTI_UNITS_PPM    = 40;

   // slice order codes for slice_code
   public static final short NIFTI_SLICE_SEQ_INC =  1;
   public static final short NIFTI_SLICE_SEQ_DEC =  2;
   public static final short NIFTI_SLICE_ALT_INC =  3;
   public static final short NIFTI_SLICE_ALT_DEC =  4;
   
   
   protected ByteBuffer header;
   protected int offset;
   private int niftiVersion = 0;
   private boolean niiFile;
   private boolean spm;

   private URL file;
   /**
    * Create a header with 0 offset and not SPM compatible.
    * @param header
    */
   public AnalyzeNiftiSpmHeader(ByteBuffer header, URL file)
   {
      this(header, 0, false, file);
   }
   
   /**
    * Create a blank, editable Nifti header.  This header will be appropriate for 
    * a 2 file solution.  Use resetBuffer() to change this to a single file.
    * @param endian Specify the byte order.  The header must have the same order as the img file.
    */
   public AnalyzeNiftiSpmHeader(ByteOrder endian)
   {
      this(endian, false);
   }

   /**
    * Create a blank, editable Nifti header.
    * @param endian Specify the byte order.  The header must have the same order as the img file.
    * @param singleFile TODO
    */
   public AnalyzeNiftiSpmHeader(ByteOrder endian, boolean singleFile)
   {
      resetBuffer(endian, singleFile);
   }

   /**
    * Create the header from the ByteBuffer.  Because the bytebuffer may contain
    * other information, the offset is the first position of the header.<br>
    * The header is automatically changed to the correct Endian value.<br>
    * If a file is both spm and NIFTI compatible (which I think are mutually
    * exclusive)then NIFTI should win and wins in any code within this file.
    * @param header
    * @param offset
    * @param spm Whether or not this is an SPM compatible file.
    */
   public AnalyzeNiftiSpmHeader(ByteBuffer header, int offset, boolean spm, URL file)
   {
      this.file = file;
      this.spm = spm;
      this.header = header;
      this.offset = offset;
      header.order(ByteOrder.LITTLE_ENDIAN);
      short dim = header.getShort(offset + 40);
      if ((dim < 0) || (dim > 7))
      {
         header.order(ByteOrder.BIG_ENDIAN);
      }
      // determine the nifti version
      byte[] magic = getMagic();
      if ((magic[0] == 'n') && (magic[3] == '\0') &&
          ((magic[1] == '+') || (magic[1] == 'i')))
      {
         niiFile = magic[1] == '+';
         niftiVersion = Integer.parseInt(new String(magic, 2, 1));
      }
      else
      {
         niftiVersion = 0;
      }
   }
   
   
   public static AnalyzeNiftiSpmHeader loadHeader(URL hdr) throws IOException
   {
      URLConnection connection = hdr.openConnection();
      InputStream stream = connection.getInputStream();
      stream = FileUtilities.getStream(stream, EnumSet.allOf(Compression.class));
      byte[] bytes = new byte[348];
      int total = 0;
      int headerLength = 348;
      while (total < headerLength)
      {
         int v = stream.read(bytes);
         if (v == -1)
            throw new IOException("Invalid Header, less than 348 bytes");
         total += v;
         // TODO read the extensions into the header.
      }
      stream.close();
      return new AnalyzeNiftiSpmHeader(ByteBuffer.wrap(bytes), 0, true, hdr);
   }

   /**
    * Return the nifti version or 0 if this is not a nifti file.
    * @return
    */
   public int getNIFTIversion()
   {
      return niftiVersion;
   }

   private int getLength16(int len) {
	   if (len % 16 == 0) {
		   return len;
	   } else {
		   return 16 * (len / 16 + 1);
	   }
   }
   
   private void pad(OutputStream dest, int len) throws IOException {
	   for (int i = 0; i < len; i++) {
		   dest.write(0);
	   }
   }
   
   public void write(OutputStream dest, byte[][] ext) throws IOException
   {
      int totalLength = 352;
      for (byte[] b : ext) {
    	  int extLength = getLength16(8 + b.length);
    	  totalLength += extLength;
      }
      setVoxOffset(isSingleNIFTIFile() ? totalLength : 0);
      setExtended((byte) ext.length);
      byte[] result = new byte[352];
      
      header.position(0);
      header.get(result, 0, Math.min(result.length, header.limit()));
      dest.write(result);
      ByteEncoder enc = new ByteEncoder(dest, getEndian());
      for (byte[] b : ext) {
    	  int extLength = getLength16(8 + b.length);
    	  enc.write(extLength);
    	  enc.write(0);
    	  dest.write(b);
    	  pad(dest, extLength - 8 - b.length);
      }
   }

   public boolean hasExtended() {
	   if (header.limit() > 348) {
		   return header.get(348) != 0;
	   } else {
		   return false;
	   }
   }
   
   public void setExtended(byte length) {
	   if (length == 0)
		   return;
	   else if (header.limit() > 348) {
		   header.put(348, length);
	   } else {
		   throw new IllegalArgumentException();
	   }
   }

   public void write(OutputStream dest) throws IOException
   {
	   write(dest, new byte[0][]);
   }
   
   public boolean isSPM()
   {
      return spm;
   }

   public boolean isNIFTI()
   {
      return niftiVersion > 0;
   }

   /**
    * Return whether or not this is a single file nifti.  This is not valid if
    * NIFTI version is 0.
    * @return
    */
   public boolean isSingleNIFTIFile()
   {
      return niiFile;
   }

   public ByteOrder getEndian()
   {
      return header.order();
   }

   /**
    * Get the size of the header.  In NIFTI and all Analyze files I have seen,
    * this is always 348.  It starts at position 0.
    * @return
    */
   public int getSizeofHdr()
   {
      return header.getInt(offset);
   }

   /**
    * Return the 10 byte data_type field (not to be confused with datatype).
    * This starts at position 4.
    * @return
    */
   public byte[] getDataType()
   {
      byte[] result = new byte[10];
      header.position(4);
      header.get(result, offset, result.length);
      return result;
   }
   
   public long readNextInteger(ByteBuffer img)
   {
      switch (getDatatype())
      {
         case DT_DOUBLE:
            return (long) img.getDouble();
         case DT_FLOAT:
            return (long) img.getFloat();
         case DT_INT8:
            return img.get();
         case DT_INT16:
            return img.getShort();
         case DT_INT32:
            return img.getInt();
         case DT_INT64:
            return img.getLong();
         case DT_UINT8:
            return img.get() & 0xff;
         case DT_UINT16:
            return img.getChar();
         case DT_UINT32:
            return (long) img.getInt() & 0xffFFffFFl;
         case DT_RGB24:
         {
            return assembleRGB(img);
         }
         default:
            throw new IllegalArgumentException("Cannot map " + getDTName(getDatatype()));
      }      
   }
   
   /**
    * Read the next value from the image buffer and return it as a
    * double.  This doesn't support all data types.  RGB is returned as an
    * integer that represents ARGB (8 bits each)
    * @param img A buffer that contains data pulled directly from the image file.
    * @return the double representation.
    */
   public double readNext(ByteBuffer img)
   {
      switch (getDatatype())
      {
         case DT_DOUBLE:
            return img.getDouble();
         case DT_FLOAT:
            return img.getFloat();
         case DT_INT8:
            return img.get();
         case DT_INT16:
            return img.getShort();
         case DT_INT32:
            return img.getInt();
         case DT_INT64:
            return img.getLong();
         case DT_UINT8:
            return img.get() & 0xff;
         case DT_UINT16:
            return img.getChar();
         case DT_UINT32:
            return (long) img.getInt() & 0xffFFffFFl;
         case DT_RGB24:
         {
            return assembleRGB(img);
         }
         default:
            throw new IllegalArgumentException("Cannot map " + getDTName(getDatatype()));
      }
   }
   
   public static int assembleRGB(ByteBuffer img)
   {
      byte r = img.get();
      byte g = img.get();
      byte b = img.get();
      int result = (0xff << 24) | (r << 16) | (g << 8) | b;
      return result;
   }
   
   /**
    * Read the next value as an object.  RGB are returned as Integers, 
    * most values are numbers.  This will eventually support complex numbers
    * and maybe long doubles.
    * @param buffer
    * @return
    */
   public Object readNextObj(ByteBuffer img)
   {
      // Use alot of autoboxing
      switch (getDatatype())
      {
         case DT_DOUBLE:
            return img.getDouble();
         case DT_FLOAT:
            return img.getFloat();
         case DT_INT8:
            return img.get();
         case DT_INT16:
            return img.getShort();
         case DT_INT32:
            return img.getInt();
         case DT_INT64:
            return img.getLong();
         case DT_UINT8:
            return img.get() & 0xff;
         case DT_UINT16:
            return new Integer(img.getChar());
         case DT_UINT32:
            return (long) img.getInt() & 0xffFFffFFl;
         case DT_RGB:
            return assembleRGB(img);
         default:
            throw new IllegalArgumentException("Cannot map " + getDTName(getDatatype()));
      }
      
   }
   
   /**
    * Get the data type name.
    * @param dt
    * @return
    */
   public String getDTName(int dt)
   {
      switch (dt)
      {
         case DT_ALL:
            return "all";
         case DT_BINARY:
            return "binary";
         case DT_COMPLEX64:
            return "complex float";
         case DT_COMPLEX128:
            return "complex double";
         case DT_COMPLEX256:
            return "complex double double";
         case DT_FLOAT32:
            return "single";
         case DT_FLOAT64:
            return "double";
         case DT_FLOAT128:
            return "double double";
         case DT_INT8:
            return "signed byte";
         case DT_INT16:
            return "signed short";
         case DT_INT32:
            return "signed integer";
         case DT_INT64:
            return "signed long";
         case DT_NONE:
            return "none";
         case DT_RGB24:
            return "RGB";
         case DT_UINT8:
            return "unsigned byte";
         case DT_UINT16:
            return "unsigned short";
         case DT_UINT32:
            return "unsigned integer";
         case DT_UINT64:
            return "unsigned long";
         default:
            return "I have no clue";
      }
   }

   /**
    * Get the 18 byte db_name field.  It starts at position 14.
    * @return
    */
   public byte[] getDBName()
   {
      byte[] result = new byte[18];
      header.position(14);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * Get the extents field.  It starts at position 32.
    * @return
    */
   public int getExtents()
   {
      return header.getInt(offset + 32);
   }

   /**
    * Get the session_error field.  It starts at position 36.
    * @return
    */
   public short getSessionError()
   {
      return header.getShort(offset + 36);
   }

   /**
    * get the regular field.  It starts at position 38
    * @return
    */
   public byte getRegular()
   {
      return header.get(offset + 38);
   }

   /**
    * Get the dim_info field (NIFTI) used for MRI slice ordering.  This starts at 39.
    */
   public byte getDimInfo()
   {
      return header.get(offset + 39);
   }

   /**
    * Get the hkey_un0 field.  This starts at 39.
    * @return
    */
   public byte getHKeyUn0()
   {
      return header.get(offset + 39);
   }

   /**
    * Get the dim field.  This contains the data array dimensions.  Starts at 40.
    * @return
    */
   public short[] getDim()
   {
      short[] result = new short[8];
      int off = offset + 40;
      for (int i = 0; i < result.length; i++)
      {
         result[i] = header.getShort(off + i * 2);
      }
      return result;
   }

   /**
    * Get unused8 from position 56.
    * @return
    */
   public short getUnused8()
   {
      return header.getShort(offset + 56);
   }

   /**
    * Get unused9 from position 58.
    * @return
    */
   public short getUnused9()
   {
      return header.getShort(offset + 58);
   }


   /**
    * Get intent_p1 (NIFTI) from position 56.
    * @return
    */
   public float getIntentP1()
   {
      return header.getFloat(offset + 56);
   }

   /**
    * Get unused10 from position 60.
    * @return
    */
   public short getUnused10()
   {
      return header.getShort(offset + 60);
   }

   /**
    * Get unused11 from position 62.
    * @return
    */
   public short getUnused11()
   {
      return header.getShort(offset + 62);
   }


   /**
    * Get intent_p2 (NIFTI) from position 60.
    * @return
    */
   public float getIntentP2()
   {
      return header.getFloat(offset + 60);
   }

   /**
    * Get unused12 from position 64.
    * @return
    */
   public short getUnused12()
   {
      return header.getShort(offset + 64);
   }

   /**
    * Get unused13 from position 66.
    * @return
    */
   public short getUnused13()
   {
      return header.getShort(offset + 66);
   }

   /**
    * Get intent_p3 (NIFTI) from position 64.
    * @return
    */
   public float getIntentP3()
   {
      return header.getFloat(offset + 64);
   }

   /**
    * Get unused11 from position 68.
    * @return
    */
   public short getUnused14()
   {
      return header.getShort(offset + 68);
   }

   /**
    * Get intent_code (NIFTI) from position 68.  The intent code should be one of the
    * NIFTI_INTENT variables if the file is well formatted for this version.
    * @return
    */
   public short getIntentCode()
   {
      return header.getShort(offset + 68);
   }

   private short dt = DT_NONE;
   /**
    * Get datatype, which is not data_type (stupid, I know) from position 70.
    * @return
    */
   public short getDatatype()
   {
      if (dt == DT_NONE)
         dt = header.getShort(offset + 70);
      return dt;
   }

   /**
    * get the bitpix field (Number of bits/voxel) from position 72.
    * @return
    */
   public short getBitpix()
   {
      return header.getShort(offset + 72);
   }

   /**
    * get the first slice index.  This is field slice_start from 74
    * @return
    */
   public short getSliceStart()
   {
      return header.getShort(offset + 74);
   }

   /**
    * get dim_un0 from 74.
    * @return
    */
   public short getDimUn0()
   {
      return header.getShort(offset + 74);
   }

   /**
    * get pixdim float[8] (grid spacings) from 76.
    * @return
    */
   public float[] getPixdim()
   {
      float[] result = new float[8];
      int off = offset + 76;
      for (int i = 0; i < result.length; i++)
      {
         result[i] = header.getFloat(off + i * 4);
      }
      return result;
   }

   /**
    * Get vox_offset from 108.  This is the offset into the .nii file in NIFTI.
    * @return
    */
   public float getVoxOffset()
   {
      return header.getFloat(offset + 108);
   }

   /**
    * get funused1 from 112
    * @return
    */
   public float getFunused1()
   {
      return header.getFloat(offset + 112);
   }

   /**
    * get funused2 from 116
    * @return
    */
   public float getFunused2()
   {
      return header.getFloat(offset + 116);
   }

   /**
    * get funused3 from 120
    * @return
    */
   public float getFunused3()
   {
      return header.getFloat(offset + 120);
   }

   /**
    * get scl_slope (NIFTI) from 112.  This is the slope for data scaling.
    * @return
    */
   public float getSclSlope()
   {
      return header.getFloat(offset + 112);
   }

   /**
    * get scl_offset (NIFTI) from 116.  This is the offset for data scaling.
    * @return
    */
   public float getSclOffset()
   {
      return header.getFloat(offset + 116);
   }

   /**
    * Get slice_end (NIFTI) which is the last slice index.  Position 120.
    * @return
    */
   public short getSliceEnd()
   {
      return header.getShort(offset + 120);
   }

   /**
    * get slice_code (NIFTI) which is the slice timing order.  Position 122.
    * @return
    */
   public byte getSliceCode()
   {
      return header.get(offset + 122);
   }

   /**
    * get xyzt_units.  These are the units for pixdim[1..4].  Position 123
    * @return
    */
   public byte getXyztUnits()
   {
      return header.get(offset + 123);
   }

   /**
    * get cal_max at 124.  This is the max display intensity.
    * @return
    */
   public float getCalMax()
   {
      return header.getFloat(offset + 124);
   }

   /**
    * get cal_min at 128.  This is the min display intensity.
    * @return
    */
   public float getCalMin()
   {
      return header.getFloat(offset + 128);
   }

   /**
    * get slice_duration (NIFTI) at 132.  This is time for 1 slice.
    * @return
    */
   public float getSliceDuration()
   {
      return header.getFloat(offset + 132);
   }

   /**
    * get compressed from 132.
    * @return
    */
   public float getCompressed()
   {
      return header.getFloat(offset + 132);
   }

   /**
    * Get toffset (NIFTI) from 136.  This is the time axis shift.
    * @return
    */
   public float getToffset()
   {
      return header.getFloat(offset + 136);
   }

   /**
    * get glmax from 140
    * @return
    */
   public int getGlmax()
   {
      return header.getInt(offset + 140);
   }

   /**
    * get glmin from 140
    * @return
    */
   public int getGlmin()
   {
      return header.getInt(offset + 144);
   }

   /**
    * get a byte[80] for desrip from 148.  This is any text you want.
    * @return
    */
   public byte[] getDescrip()
   {
      byte[] result = new byte[80];
      header.position(148);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get a byte[24] for aux_file from 224.  auxilary filename.
    * @return
    */
   public byte[] getAuxFile()
   {
      byte[] result = new byte[24];
      header.position(228);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get the orient from position 252
    * @return
    */
   public byte getOrient()
   {
      return header.get(offset + 252);
   }

   /**
    * get a byte[10] for originator from 253
    * @return
    */
   public byte[] getOriginator()
   {
      byte[] result = new byte[10];
      header.position(253);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get a byte[10] for generated from 263
    * @return
    */
   public byte[] getGenerated()
   {
      byte[] result = new byte[10];
      header.position(263);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get a byte[10] for scannum from 273
    * @return
    */
   public byte[] getScannum()
   {
      byte[] result = new byte[10];
      header.position(273);
      header.get(result, offset, result.length);
      return result;
   }


   /**
    * get a byte[10] for patient_id from 283
    * @return
    */
   public byte[] getPatientId()
   {
      byte[] result = new byte[10];
      header.position(283);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get a byte[10] for exp_date from 293
    * @return
    */
   public byte[] getExpDate()
   {
      byte[] result = new byte[10];
      header.position(293);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get a byte[10] for exp_time from 303
    * @return
    */
   public byte[] getExpTime()
   {
      byte[] result = new byte[10];
      header.position(303);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get a byte[3] for hist_un0 from position 313
    */
   public byte[] getHistUn0()
   {
      byte[] result = new byte[3];
      header.position(313);
      header.get(result, offset, result.length);
      return result;

   }

   /**
    * get views from 316
    * @return
    */
   public int getViews()
   {
      return header.getInt(offset + 316);
   }

   /**
    * get vols_added from 320
    * @return
    */
   public int getVolsAdded()
   {
      return header.getInt(offset + 320);
   }

   /**
    * get start_field from 324
    * @return
    */
   public int getStartField()
   {
      return header.getInt(offset + 324);
   }

   /**
    * get field_skip from 328
    * @return
    */
   public int getFieldSkip()
   {
      return header.getInt(offset + 328);
   }

   /**
    * get omin from 336
    * @return
    */
   public int getOMin()
   {
      return header.getInt(offset + 336);
   }

   /**
    * get omax from 332
    * @return
    */
   public int getOMax()
   {
      return header.getInt(offset + 332);
   }

   /**
    * get smin from 344
    * @return
    */
   public int getSMin()
   {
      return header.getInt(offset + 344);
   }

   /**
    * get smax from 340
    * @return
    */
   public int getSMax()
   {
      return header.getInt(offset + 340);
   }

   /**
    * Get qform_code from 252 (NIFTI).  This is an NIFTI_XFORM_* code.
    * @return
    */
   public short getQformCode()
   {
      return header.getShort(offset + 252);
   }

   /**
    * Get sform_code from 254 (NIFTI).  This is an NIFTI_XFORM_* code.
    * @return
    */
   public short getSformCode()
   {
      return header.getShort(offset + 254);
   }

   /**
    * Get quatern_c from 256 (NIFTI).  This is the quaternion b param.
    * @return
    */
   public float getQuaternB()
   {
      return header.getFloat(offset + 256);
   }

   /**
    * Get quatern_d from 260(NIFTI).  This is the quaternion c param.
    * @return
    */
   public float getQuaternC()
   {
      return header.getFloat(offset + 260);
   }

   /**
    * Get quatern_d from 264 (NIFTI).  This is the quaternion d param.
    * @return
    */
   public float getQuaternD()
   {
      return header.getFloat(offset + 264);
   }

   /**
    * Get qoffset_x from 268 (NIFTI).  This is the quaternion z shift.
    * @return
    */
   public float getQoffsetX()
   {
      return header.getFloat(offset + 268);
   }

   /**
    * Get qoffset_y from 272 (NIFTI).  This is the quaternion z shift.
    * @return
    */
   public float getQoffsetY()
   {
      return header.getFloat(offset + 272);
   }

   /**
    * Get qoffset_z from 276 (NIFTI). This is the quaternion z shift.
    * @return
    */
   public float getQoffsetZ()
   {
      return header.getFloat(offset + 276);
   }

   /**
    * Get srow_x (NIFTI). This is 1st row affine transform.  Position 280.
    * @return
    */
   public float[] getSrowX()
   {
      float[] result = new float[4];
      int off = offset + 280;
      for (int i = 0; i < result.length; i++)
      {
         result[i] = header.getFloat(off + i * 4);
      }
      return result;
   }

   /**
    * Get srow_y (NIFTI). This is 2nd row affine transform.  Position 296.
    * @return
    */
   public float[] getSrowY()
   {
      float[] result = new float[4];
      int off = offset + 296;
      for (int i = 0; i < result.length; i++)
      {
         result[i] = header.getFloat(off + i * 4);
      }
      return result;
   }

   /**
    * Get srow_z (NIFTI). This is 3rd row affine transform.  Position 312.
    * @return
    */
   public float[] getSrowZ()
   {
      float[] result = new float[4];
      int off = offset + 312;
      for (int i = 0; i < result.length; i++)
      {
         result[i] = header.getFloat(off + i * 4);
      }
      return result;
   }

   /**
    * Get the byte[16] for intent_name (NIFTI) from 328.  This is the 'name' or meaning
    * of the data.
    * @return
    */
   public byte[] getIntentName()
   {
      byte[] result = new byte[16];
      header.position(328);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * get the 4 bytes for magic (NIFTI).  This must be ni1\0 or n+1\0, but 1
    * may change with versions of NIFTI.  Position 344.
    * @return
    */
   public byte[] getMagic()
   {
      byte[] result = new byte[4];
      header.position(344);
      header.get(result, offset, result.length);
      return result;
   }

   /**
    * Get the 3 byte origin (SPM) from position 253
    * @return
    */
   public short[] getOrigin()
   {
      short[] result = new short[3];
      int off = offset + 253;
      for (int i = 0; i < result.length; i++)
      {
         result[i] = header.getShort(off + i * 2);
      }
      return result;
   }

   /**
    * Get the scale factor (SPM) from funused1
    * @return
    */
   public float getScaleFactor()
   {
      return getFunused1();
   }

   /**
    * Get a transform that can be used.  In the case of SPM files, external code
    * should read the mat file and this should only be used as a default.
    * Only NIFTI files have a standard coordinate system option.  The transform is
    * 0 indexed (ie x goes from [0, xMax) or the same for y and z)
    * @param standard Get the space in a standard coordinate system if it exists.
    * @return
    */
   public double[] getTransform(boolean standard)
   {
      if (isNIFTI())
      {
         if (standard && (getQformCode() > 0) || (getSformCode() == 0))
         {
            return getQTrans();
         }
         else if (getSformCode() > 0)
         {
            return getSTrans();
         }
      }
      else if (isSPM())
      {
         return getSPMTrans();
      }
      return getOldTrans();
   }

   /**
    * This is the last ditch, least preferred method for getting the transform.
    * It is only for compatibility with previous versions.
    * @return
    */
   public double[] getOldTrans()
   {
      double[] trans = new double[16];
      float[] pixdim = getPixdim();
      trans[0] = pixdim[1];
      trans[3] = pixdim[2];
      trans[10] = pixdim[3];
      trans[15] = 1;
      return trans;
   }

   
   public double[] getSPMTrans()
   {
      try
      {
         URL mat = new URL(file.toString().replaceAll("\\.hdr", ".mat"));
         if (FileUtilities.touchURL(mat))
         {
            MatVar var = null;
            MatfileLoader loader = MatFileLoaderFactory.getLoader(mat);
            loader.fillVariables();
            var = loader.get("M");
            double[] matrixElements = new double[16];
            int[] dim = var.getDim();
            if ((dim.length != 2) || (dim[0] != 4) || (dim[1] != 4)) {
               return null;
            }
            for (int j = 0; j < dim[1]; j++) {
               for (int i = 0; i < dim[0]; i++) {
                  int[] loc = new int[2];
                  loc[0] = i;
                  loc[1] = j;
                  matrixElements[i * dim[1] + j] = var.getDouble(loc);
               }
            }
            
            double[] p0 = {0, 0, 0};
            double[] p1 = {1, 1, 1};
            transform(matrixElements, p0);
            transform(matrixElements, p1);
            
            // now change this to 0-indexed instead of 1-indexed
            matrixElements[3] = matrixElements[3] + (p1[0] - p0[0]);
            matrixElements[7] = matrixElements[7] + (p1[1] - p0[1]);
            matrixElements[11] = matrixElements[11] + (p1[2] - p0[2]);

            return matrixElements;
         }
         else
         {
            return getDefaultSPMTrans();
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
         return getDefaultSPMTrans();
      }
   }
   
   public static double[] transform(double[] matrix4x4, double[] point)
   {
      double m00 = matrix4x4[0];
      double m01 = matrix4x4[1];
      double m02 = matrix4x4[2];
      double m03 = matrix4x4[3];
      double m10 = matrix4x4[4];
      double m11 = matrix4x4[5];
      double m12 = matrix4x4[6];
      double m13 = matrix4x4[7];
      double m20 = matrix4x4[8];
      double m21 = matrix4x4[9];
      double m22 = matrix4x4[10];
      double m23 = matrix4x4[11];
      float x = (float)(m00 * (double)point[0] + m01 * (double)point[1] + m02 * (double)point[2] + m03);
      float y = (float)(m10 * (double)point[0] + m11 * (double)point[1] + m12 * (double)point[2] + m13);
      point[2] = (float)(m20 * (double)point[0] + m21 * (double)point[1] + m22 * (double)point[2] + m23);
      point[0] = x;
      point[1] = y;
      return point;
   }
   
   /**
    * Get the transform assuming this is an SPM file.
    * @return
    */
   public double[] getDefaultSPMTrans()
   {
      double[] trans = new double[16];
      float[] pixdim = getPixdim();
      //  Fall back to header information, implementing the same logic as SPM (file spm_get_space.m):
// This code was originally put in place by Andrew Poliakov, but has been
// slightly edited when it was moved to this file.
         short[] centralVoxel = getOrigin();
//       % If imagename.mat exists, and this contains matrix "M", then this is used.
//       % Otherwise, the image is assumed to be transverse, with the origin of the
//       % space found in the ORIGIN field of the header.
//       % If this ORIGIN field is set to [0 0 0], then the origin is assumed to be
//       % at the centre (center) of the volume.
         if (centralVoxel[0] == 0.0 && centralVoxel[1] == 0.0 &&
             centralVoxel[2] == 0.0)
         {
            short[] dim = getDim();
            // round down instead of up since we are 0-indexed unlike matlab
            centralVoxel[0] = (short) ( (dim[1] - 1) / 2);
            centralVoxel[1] = (short) ( (dim[2] - 1) / 2);
            centralVoxel[2] = (short) ( (dim[3] - 1) / 2);
         }
//         if all(vox == 0), vox = [1 1 1]; end;
         if (pixdim[1] == 0.0 && pixdim[2] == 0.0 &&
             pixdim[3] == 0.0)
         {
            pixdim[1] = 1.0f;
            pixdim[2] = 1.0f;
            pixdim[3] = 1.0f;
         }

//         off = -vox.*origin;
//         M   = [vox(1) 0 0 off(1) ; 0 vox(2) 0 off(2) ; 0 0 vox(3) off(3) ; 0 0 0 1];
         trans[0] = pixdim[1];
         trans[3] = -pixdim[1] * centralVoxel[0];
         trans[5] = pixdim[2];
         trans[7] = -pixdim[2] * centralVoxel[1];
         trans[10] = pixdim[3];
         trans[11] = -pixdim[3] * centralVoxel[2];
         trans[15] = 1.0;
         return trans;
   }

   /**
    * Get the transform from Q in NIFTI.  This is the quaternion or method 2 in
    * the file.  If this file follows the specification, then this will be on
    * the original grid or close to it.  It will only be valid if qform_code > 0.
    * @return
    */
   public double[] getQTrans()
   {
      double[] trans = new double[16];
      float[] pixdim = getPixdim();
      float b = getQuaternB();
      float c = getQuaternC();
      float d = getQuaternD();
      double a = Math.sqrt(1 - b*b - c*c - d*d);
      trans[0] = pixdim[1] * (a*a+b*b-c*c-d*d);
      trans[1] = pixdim[2] * (2*b*c-2*a*d);
      trans[2] = pixdim[3] * (2*b*d+2*a*c);
      trans[3] = getQoffsetX();
      trans[4] = pixdim[1] * (2*b*c+2*a*d);
      trans[5] = pixdim[2] * (a*a+c*c-b*b-d*d);
      trans[6] = pixdim[3] * (2*c*d-2*a*b);
      trans[7] = getQoffsetY();
      trans[8] = pixdim[1] * (2*b*d-2*a*c);
      trans[9] = pixdim[2] * (2*c*d+2*a*b);
      trans[10] = pixdim[3] * (a*a+d*d-c*c-b*b);
      trans[11] = getQoffsetZ();
      trans[15] = 1.0;
      return trans;
   }

   /**
    * Get the NIFTI s tranform (Method 3). This is designed to represent the
    * grid in some standard space.  It will only be valid if sform_code > 0.
    * @return
    */
   public double[] getSTrans()
   {
      double[] trans = new double[16];
      float[] srow_x = getSrowX();
      float[] srow_y = getSrowY();
      float[] srow_z = getSrowZ();
      trans[0] = srow_x[0];
      trans[1] = srow_x[1];
      trans[2] = srow_x[2];
      trans[3] = srow_x[3];
      trans[4] = srow_y[0];
      trans[5] = srow_y[1];
      trans[6] = srow_y[2];
      trans[7] = srow_y[3];
      trans[8] = srow_z[0];
      trans[9] = srow_z[1];
      trans[10] = srow_z[2];
      trans[11] = srow_z[3];
      trans[15] = 1.0;
      return trans;
   }

   /**
    * Return the 10 byte data_type field (not to be confused with datatype).
    * This starts at position 4.
    * @return
    */
   public void setDataType(byte[] dataType)
   {
      header.position(4);
      header.put(dataType, offset, dataType.length);
   }

   /**
    * set the 18 byte db_name field.  It starts at position 14.
    * @return
    */
   public void setDBName(byte[] dbName)
   {
      header.position(14);
      header.put(dbName, offset, dbName.length);
   }

   /**
    * set the extents field.  It starts at position 32.
    * @return
    */
   public void setExtents(int extents)
   {
      header.putInt(offset + 32, extents);
   }

   /**
    * set the session_error field.  It starts at position 36.
    * @return
    */
   public void setSessionError(short sessionError)
   {
      header.putShort(offset + 36, sessionError);
   }

   /**
    * set the regular field.  It starts at position 38
    * @return
    */
   public void setRegular(byte regular)
   {
      header.put(offset + 38, regular);
   }

   /**
    * set the dim_info field (NIFTI) used for MRI slice ordering.  This starts at 39.
    */
   public void setDimInfo(byte dimInfo)
   {
      header.put(offset + 39, dimInfo);
   }

   /**
    * set the hkey_un0 field.  This starts at 39.
    * @return
    */
   public void setHKeyUn0(byte hkeyUn0)
   {
      header.put(offset + 39, hkeyUn0);
   }

   /**
    * set the dim field.  This contains the data array dimensions.  Starts at 40.
    * @return
    */
   public void setDim(short[] dim)
   {
      if (dim.length > 8)
         throw new IllegalArgumentException("Dim can't be greater than 8.");
      int off = offset + 40;
      int i = 0;
      for (i = 0; i < dim.length; i++)
      {
         header.putShort(off + i * 2, dim[i]);
      }
      while (i < 8)
      {
         header.putShort(off + i * 2, (short) 0);
         i++;
      }
   }

   /**
    * set unused8 from position 56.
    * @return
    */
   public void setUnused8(short unused8)
   {
      header.putShort(offset + 56, unused8);
   }

   /**
    * set unused9 from position 58.
    * @return
    */
   public void setUnused9(short unused9)
   {
      header.putShort(offset + 58, unused9);
   }


   /**
    * set intent_p1 (NIFTI) from position 56.
    * @return
    */
   public void setIntentP1(float intentP1)
   {
      header.putFloat(offset + 56, intentP1);
   }

   /**
    * set unused10 from position 60.
    * @return
    */
   public void setUnused10(short unused10)
   {
      header.putShort(offset + 60, unused10);
   }

   /**
    * set unused11 from position 62.
    * @return
    */
   public void setUnused11(short unused11)
   {
      header.putShort(offset + 62, unused11);
   }


   /**
    * set intent_p2 (NIFTI) from position 60.
    * @return
    */
   public void setIntentP2(float intentP2)
   {
      header.putFloat(offset + 60, intentP2);
   }

   /**
    * set unused12 from position 64.
    * @return
    */
   public void setUnused12(short unused12)
   {
      header.putShort(offset + 64, unused12);
   }

   /**
    * set unused13 from position 66.
    * @return
    */
   public void setUnused13(short unused13)
   {
      header.putShort(offset + 66, unused13);
   }

   /**
    * set intent_p3 (NIFTI) from position 64.
    * @return
    */
   public void setIntentP3(float intentP3)
   {
      header.putFloat(offset + 64, intentP3);
   }

   /**
    * set unused11 from position 68.
    * @return
    */
   public void setUnused14(short unused14)
   {
      header.putShort(offset + 68, unused14);
   }

   /**
    * set intent_code (NIFTI) from position 68.  The intent code should be one of the
    * NIFTI_INTENT variables if the file is well formatted for this version.
    * @return
    */
   public void setIntentCode(short intentCode)
   {
      header.putShort(offset + 68, intentCode);
   }

   /**
    * set datatype, which is not data_type (stupid, I know) from position 70.
    * 
    * Automatically setBitPix()
    * @return
    */
   public void setDatatype(short datatype)
   {
      dt = datatype;
      header.putShort(offset + 70, datatype);
      switch (datatype)
      {
         case DT_NONE:
            setBitpix((short) 0);
            break;
         case DT_BINARY:
            setBitpix((short) 1);
            break;
         case DT_UINT8:
         case DT_INT8:
            setBitpix((short) 8);
            break;
         case DT_INT16:
         case DT_UINT16:
            setBitpix((short) 16);
            break;
         case DT_INT32:
         case DT_FLOAT32:
         case DT_UINT32:
            setBitpix((short) 32);
            break;
         case DT_COMPLEX64:
         case DT_FLOAT64:
         case DT_INT64:
         case DT_UINT64:
            setBitpix((short) 64);
            break;
         case DT_RGB24:
            setBitpix((short) 24);
            break;
         case DT_FLOAT128:
         case DT_COMPLEX128:
            setBitpix((short) 128);
            break;
         case DT_COMPLEX256:
            setBitpix((short) 256);
            break;    
         default:
            System.err.println("Unrecognized data type: " + datatype);   
      }
   }

   /**
    * set the bitpix field (Number of bits/voxel) from position 72.
    * @return
    */
   public void setBitpix(short bitpix)
   {
      header.putShort(offset + 72, bitpix);
   }

   /**
    * set the first slice index.  This is field slice_start from 74
    * @return
    */
   public void setSliceStart(short sliceStart)
   {
      header.putShort(offset + 74, sliceStart);
   }

   /**
    * set dim_un0 from 74.
    * @return
    */
   public void setDimUn0(short dimUn0)
   {
      header.putShort(offset + 74, dimUn0);
   }

   /**
    * set pixdim float[8] (grid spacings) from 76.
    * @return
    */
   public void setPixdim(float[] pixDim)
   {
      if (pixDim.length != 8)
         throw new IllegalArgumentException("Bad pixDim, length=" + pixDim.length);
      int off = offset + 76;
      for (int i = 0; i < pixDim.length; i++)
      {
         header.putFloat(off + i * 4, pixDim[i]);
      }
   }

   /**
    * set vox_offset from 108.  This is the offset into the .nii file in NIFTI.
    * @return
    */
   public void setVoxOffset(float voxOffset)
   {
      header.putFloat(offset + 108, voxOffset);
   }

   /**
    * set funused1 from 112
    * @return
    */
   public void setFunused1(float funused1)
   {
      header.putFloat(offset + 112, funused1);
   }

   /**
    * set funused2 from 116
    * @return
    */
   public void setFunused2(float funused2)
   {
      header.putFloat(offset + 116, funused2);
   }

   /**
    * set funused3 from 120
    * @return
    */
   public void setFunused3(float funused3)
   {
      header.putFloat(offset + 120, funused3);
   }

   /**
    * set scl_slope (NIFTI) from 112.  This is the slope for data scaling.
    * @return
    */
   public void setSclSlope(float sclSlope)
   {
      header.putFloat(offset + 112, sclSlope);
   }

   /**
    * set scl_offset (NIFTI) from 116.  This is the offset for data scaling.
    * @return
    */
   public void setSclOffset(float sclOffset)
   {
      header.putFloat(offset + 116, sclOffset);
   }

   /**
    * set slice_end (NIFTI) which is the last slice index.  Position 120.
    * @return
    */
   public void setSliceEnd(short sliceEnd)
   {
      header.putShort(offset + 120, sliceEnd);
   }

   /**
    * set slice_code (NIFTI) which is the slice timing order.  Position 122.
    * @return
    */
   public void setSliceCode(byte sliceCode)
   {
      header.put(offset + 122, sliceCode);
   }

   /**
    * set xyzt_units.  These are the units for pixdim[1..4].  Position 123
    * @return
    */
   public void setXyztUnits(byte xyztUnits)
   {
      header.put(offset + 123, xyztUnits);
   }

   /**
    * set cal_max at 124.  This is the max display intensity.
    * @return
    */
   public void setCalMax(float calMax)
   {
      header.putFloat(offset + 124, calMax);
   }

   /**
    * set cal_min at 128.  This is the min display intensity.
    * @return
    */
   public void setCalMin(float calMin)
   {
      header.putFloat(offset + 128, calMin);
   }

   /**
    * set slice_duration (NIFTI) at 132.  This is time for 1 slice.
    * @return
    */
   public void setSliceDuration(float sliceDuration)
   {
      header.putFloat(offset + 132, sliceDuration);
   }

   /**
    * set compressed from 132.
    * @return
    */
   public void setCompressed(float compressed)
   {
      header.putFloat(offset + 132, compressed);
   }

   /**
    * set toffset (NIFTI) from 136.  This is the time axis shift.
    * @return
    */
   public void setToffset(float toffset)
   {
      header.putFloat(offset + 136, toffset);
   }

   /**
    * set glmax from 140
    * @return
    */
   public void setGlmax(int glmax)
   {
      header.putInt(offset + 140, glmax);
   }

   /**
    * set glmin from 140
    * @return
    */
   public void setGlmin(int glmin)
   {
      header.putInt(offset + 144, glmin);
   }

   /**
    * set a byte[80] for desrip from 148.  This is any text you want.
    * @return
    */
   public void setDescrip(byte[] descrip)
   {
      header.position(148);
      header.put(descrip, offset, descrip.length);
   }

   /**
    * set a byte[24] for aux_file from 224.  auxilary filename.
    * @return
    */
   public void setAuxFile(byte[] auxFile)
   {
      header.position(228);
      header.put(auxFile, offset, auxFile.length);
   }

   /**
    * set the orient from position 252
    * @return
    */
   public void setOrient(byte orient)
   {
      header.put(offset + 252, orient);
   }

   /**
    * set a byte[10] for originator from 253
    * @return
    */
   public void setOriginator(byte[] originator)
   {
      header.position(253);
      header.put(originator, offset, originator.length);
   }

   /**
    * set a byte[10] for generated from 263
    * @return
    */
   public void setGenerated(byte[] generated)
   {
      header.position(263);
      header.put(generated, offset, generated.length);
   }

   /**
    * set a byte[10] for scannum from 273
    * @return
    */
   public void setScannum(byte[] scannum)
   {
      header.position(273);
      header.put(scannum, offset, scannum.length);
   }


   /**
    * set a byte[10] for patient_id from 283
    * @return
    */
   public void setPatientId(byte[] patientId)
   {
      header.position(283);
      header.put(patientId, offset, patientId.length);
   }

   /**
    * set a byte[10] for exp_date from 293
    * @return
    */
   public void setExpDate(byte[] expDate)
   {
      header.position(293);
      header.put(expDate, offset, expDate.length);
   }

   /**
    * set a byte[10] for exp_time from 303
    * @return
    */
   public void setExpTime(byte[] expTime)
   {
      header.position(303);
      header.put(expTime, offset, expTime.length);
   }

   /**
    * set a byte[3] for hist_un0 from position 313
    */
   public void setHistUn0(byte[] hist)
   {
      header.position(313);
      header.put(hist, offset, hist.length);
   }

   /**
    * set views from 316
    * @return
    */
   public void setViews(int views)
   {
      header.putInt(offset + 316, views);
   }

   /**
    * set vols_added from 320
    * @return
    */
   public void setVolsAdded(int volsAdded)
   {
      header.putInt(offset + 320, volsAdded);
   }

   /**
    * set start_field from 324
    * @return
    */
   public void setStartField(int field)
   {
      header.putInt(offset + 324, field);
   }

   /**
    * set field_skip from 328
    * @return
    */
   public void setFieldSkip(int fieldSkip)
   {
      header.putInt(offset + 328, fieldSkip);
   }

   /**
    * set omin from 336
    * @return
    */
   public void setOMin(int omin)
   {
      header.putInt(offset + 336, omin);
   }

   /**
    * set omax from 332
    * @return
    */
   public void setOMax(int omax)
   {
      header.putInt(offset + 332, omax);
   }

   /**
    * set smin from 344
    * @return
    */
   public void setSMin(int smin)
   {
      header.putInt(offset + 344, smin);
   }

   /**
    * set smax from 340
    * @return
    */
   public void setSMax(int smax)
   {
      header.putInt(offset + 340, smax);
   }

   /**
    * set qform_code from 252 (NIFTI).  This is an NIFTI_XFORM_* code.
    * @return
    */
   public void setQformCode(short qformCode)
   {
      header.putShort(offset + 252, qformCode);
   }

   /**
    * set sform_code from 254 (NIFTI).  This is an NIFTI_XFORM_* code.
    * @return
    */
   public void setSformCode(short sformCode)
   {
      header.putShort(offset + 254, sformCode);
   }

   /**
    * set quatern_c from 256 (NIFTI).  This is the quaternion b param.
    * @return
    */
   public void setQuaternB(float quaternB)
   {
      header.putFloat(offset + 256, quaternB);
   }

   /**
    * set quatern_d from 260(NIFTI).  This is the quaternion c param.
    * @return
    */
   public void setQuaternC(float quaternC)
   {
      header.putFloat(offset + 260, quaternC);
   }

   /**
    * set quatern_d from 264 (NIFTI).  This is the quaternion d param.
    * @return
    */
   public void setQuaternD(float quaternD)
   {
      header.putFloat(offset + 264, quaternD);
   }

   /**
    * set qoffset_x from 268 (NIFTI).  This is the quaternion z shift.
    * @return
    */
   public void setQoffsetX(float qoffsetX)
   {
      header.putFloat(offset + 268, qoffsetX);
   }

   /**
    * set qoffset_y from 272 (NIFTI).  This is the quaternion z shift.
    * @return
    */
   public void setQoffsetY(float qoffsetY)
   {
      header.putFloat(offset + 272, qoffsetY);
   }

   /**
    * set qoffset_z from 276 (NIFTI). This is the quaternion z shift.
    * @return
    */
   public void setQoffsetZ(float qoffsetZ)
   {
      header.putFloat(offset + 276, qoffsetZ);
   }

   /**
    * set srow_x (NIFTI). This is 1st row affine transform.  Position 280.
    * @return
    */
   public void setSrowX(float[] srowX)
   {
      int off = offset + 280;
      for (int i = 0; i < srowX.length; i++)
      {
         header.putFloat(off + i * 4, srowX[i]);
      }
   }

   /**
    * set srow_y (NIFTI). This is 2nd row affine transform.  Position 296.
    * @return
    */
   public void setSrowY(float[] srowY)
   {
      int off = offset + 296;
      for (int i = 0; i < srowY.length; i++)
      {
         header.putFloat(off + i * 4, srowY[i]);
      }
   }

   /**
    * set srow_z (NIFTI). This is 3rd row affine transform.  Position 312.
    * @return
    */
   public void setSrowZ(float[] srowZ)
   {
      int off = offset + 312;
      for (int i = 0; i < srowZ.length; i++)
      {
         header.putFloat(off + i * 4, srowZ[i]);
      }
   }

   /**
    * set the byte[16] for intent_name (NIFTI) from 328.  This is the 'name' or meaning
    * of the data.
    * @return
    */
   public void setIntentName(byte[] intentName)
   {
      header.position(328);
      header.put(intentName, offset, intentName.length);
   }

   /**
    * set the 4 bytes for magic (NIFTI).  This varies depending on whether the file
    * is a single file or 2  files.  The result is ni1\0 or n+1\0, but 1
    * may change with versions of NIFTI.  Position 344.
    * @return
    */
   public void setMagic(boolean singleFile)
   {
      byte[] magic;
      niiFile = singleFile;
      if (singleFile)
      {
         magic = new byte[] {'n', '+', '1', 0};
      }
      else
      {
         magic = new byte[] {'n', 'i', '1', 0};         
      }
      header.position(344);
      header.put(magic, offset, magic.length);
   }

   /**
    * set the 3 byte origin (SPM) from position 253
    * @return
    */
   public void setOrigin(short[] origin)
   {
      int off = offset + 253;
      for (int i = 0; i < origin.length; i++)
      {
         header.putShort(off + i * 2, origin[i]);
      }
   }

   /**
    * set the scale factor (SPM) from funused1
    * @return
    */
   public void setScaleFactor(float scaleFactor)
   {
      setFunused1(scaleFactor);
   }
   
   /**
    * set the NIFTI s tranform (Method 3). This is designed to represent the
    * grid in some standard space.  It will only be valid if sform_code > 0.
    * @return
    */
   public void setSTrans(double[] trans)
   {
      if (trans.length != 12 && trans.length != 16)
         throw new IllegalArgumentException("trans's length must be 12 or 16");
      float[] srow_x = new float[4];
      float[] srow_y = new float[4];
      float[] srow_z = new float[4];
      srow_x[0] = (float) trans[0];
      srow_x[1] = (float) trans[1];
      srow_x[2] = (float) trans[2];
      srow_x[3] = (float) trans[3];
      srow_y[0] = (float) trans[4];
      srow_y[1] = (float) trans[5];
      srow_y[2] = (float) trans[6];
      srow_y[3] = (float) trans[7];
      srow_z[0] = (float) trans[8];
      srow_z[1] = (float) trans[9];
      srow_z[2] = (float) trans[10];
      srow_z[3] = (float) trans[11];
      setSrowX(srow_x);
      setSrowY(srow_y);
      setSrowZ(srow_z);
   }

   /**
    * Reset the buffer to the initial state for Nifti.<br>
    * Be careful with this method, it will throw out all information in 
    * the buffer.  However it will also ensure that the NIFTI file is well formed
    * if you are starting from a blank state.
    * @param endian the ByteOrder for this array.
    * @param singleFile true if you intend to save this as a single nii file
    */
   public void resetBuffer(ByteOrder endian, boolean singleFile)
   {
      header = ByteBuffer.wrap(new byte[352]);
      offset = 0;
      spm = false;
      header.order(endian);
      setMagic(singleFile);
      header.putInt(0, 348);
      if (singleFile)
         setVoxOffset(352);
      else
         setVoxOffset(0);
      setSTrans(new double[] 
                           {1, 0, 0, 0,
                            0, 1, 0, 0,
                            0, 0, 1, 0,
                            0, 0, 0, 1});
      setSformCode((short) NIFTI_XFORM_SCANNER_ANAT);
      setPixdim(new float[] {1, 0, 0, 0, 0, 0, 0, 0});      
   }
}