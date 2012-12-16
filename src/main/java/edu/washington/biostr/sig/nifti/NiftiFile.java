/*
 * Java NIFTI/SPM Library
 * Copyright (C) 2006 Eider Moore
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;

import org.eiderman.util.Compression;
import org.eiderman.util.FileUtilities;

/**
 * The NiftiFile knows how to load Nifti, Analyze 7.5 and 
 * SPM's Analyze 7.5 files.  This class is not thread safe.
 * @author Eider Moore
 */
public class NiftiFile
{
   AnalyzeNiftiSpmHeader header;
   /**
    * A double[16] that encodes a tranform from index to mm.
    */
   double[] transform;
   Object myData;
   SoftReference<Object> softData;
   URL hdr;
   URL img;
   URL mat;
   URL atlas;
   final int maxX;
   final int maxY;
   final int maxZ;
   final int maxT;
   final int maxI5;

   public NiftiFile(URL file) throws IOException, URISyntaxException
   {
      this(file.getPath().contains(".nii") ? new URL[] {file} : 
         FileUtilities.findURLs(file, new String[] {"hdr", "img", "mat", "imgz", "img.gz", "atlas.xml"}));
   }

   public NiftiFile(String fileResource, ClassLoader c) throws IOException, URISyntaxException
   {
      this(fileResource.endsWith(".nii") ? new URL[] {c.getResource(fileResource)} : 
         FileUtilities.findURLs(fileResource, new String[] {"hdr", "img", "mat", "imgz", "img.gz", "atlas.xml"}, c));
   }

   public NiftiFile(File file) throws IOException
   {
      this(file.getName().contains(".nii") ? new URL[] {file.toURI().toURL()} : 
         FileUtilities.findURLs(file, new String[] {"hdr", "img", "mat", "imgz", "img.gz", "atlas.xml"}));      
   }

   public NiftiFile(URI[] uri) throws IOException
   {
      this(FileUtilities.toURL(uri));
   }

   
   
   /**
    * Load the nifti file from the set of urls
    * @param uri The urls, must include the img and hdr or the nii file and may include a mat file.
    * @throws IOException
    */
   public NiftiFile(URL... uri) throws IOException
   {
      for (int i = 0; i < uri.length; i++)
      {
         String file = uri[i].getPath();
         if (file.contains(".hdr"))
         {
            hdr = uri[i];
         }
         else if (file.contains(".img"))
         {
            img = uri[i];
         }
         else if (file.contains(".mat"))
         {
            mat = uri[i];
         }
         else if (file.contains(".nii"))
         {
            img = uri[i];
            hdr = uri[i];
         }
         else if (file.contains("atlas.xml"))
         {
            atlas = uri[i];
         }
      }
      if (atlas == null)
      {
         try
         {
            // look up the atlas
            URL[] u = FileUtilities.findURLs(hdr, new String[] {"atlas.xml"});
            if (u.length > 0)
               atlas = u[0];
         }
         catch (URISyntaxException e)
         {
            e.printStackTrace();
         }
      }
      header = AnalyzeNiftiSpmHeader.loadHeader(hdr);
      short[] dim = header.getDim();
      maxX = dim[0] >= 1 ? dim[1] : 1;
      maxY = dim[0] >= 2 ? dim[2] : 1;
      maxZ = dim[0] >= 3 ? dim[3] : 1;
      maxT = dim[0] >= 4 ? dim[4] : 1;
      maxI5 = dim[0] >= 5 ? dim[5] : 1;
   }   


   public AnalyzeNiftiSpmHeader getHeader()
   {
      return header;  
   }

   /**
    * @return The 4x4 transform from index space to mm as a single double array.
    */
   public double[] getTransform()
   {
      if (transform == null)
         transform = makeTransform();
      return transform;
   }

   /**
    * Sample a single voxel.
    * @param x
    * @param y
    * @param z
    * @param t
    * @param i5
    * @return The value at that voxel as a double
    */
   public double sample(int x, int y, int z, int t, int i5) throws IOException
   {

      int index = getIndex(x, y, z, t, i5);
      if (index < 0)
         return 0;
      Object data = getDataNoLoad();
      if (data != null)
      {
         if (data instanceof byte[])
         {
            byte[] arr = (byte[]) data;
            return arr[index];
         }
         else if (data instanceof char[])
         {
            char[] arr = (char[]) data;
            return new Integer(arr[index]);
         }
         else if (data instanceof short[])
         {
            short[] arr = (short[]) data;
            return arr[index];
         }
         else if (data instanceof int[])
         {
            int[] arr = (int[]) data;
            return arr[index];
         }
         else if (data instanceof long[])
         {
            long[] arr = (long[]) data;
            return arr[index];
         }
         else if (data instanceof float[])
         {
            float[] arr = (float[]) data;
            return arr[index];
         }
         else if (data instanceof double[])
         {
            double[] arr = (double[]) data;
            return arr[index];
         }
         else
         {
            throw new IllegalArgumentException("Not valid!");
         }
      }
      else
      {
         // sample individual voxel...
         InputStream in = img.openStream();
         int offset = index * header.getBitpix() / 8 + 
         (header.isSingleNIFTIFile() ? (int) header.getVoxOffset() : 0);
         in.skip(offset);
         byte[] b = new byte[32];
         in.read(b);
         ByteBuffer buf = ByteBuffer.wrap(b);
         buf.order(header.getEndian());
         return header.readNext(buf);
      }      
   }

   /**
    * Sample a single voxel.
    * @param x
    * @param y
    * @param z
    * @param t
    * @param i5
    * @return The value at that voxel as a number
    */
   public Number sampleNumber(int x, int y, int z, int t, int i5) throws IOException
   {
      int index = getIndex(x, y, z, t, i5);
      if (index < 0)
         return 0;
      Object data = getDataNoLoad();
      if (data != null)
      {
         if (data instanceof byte[])
         {
            byte[] arr = (byte[]) data;
            return arr[index];
         }
         else if (data instanceof char[])
         {
            char[] arr = (char[]) data;
            return new Integer(arr[index]);
         }
         else if (data instanceof short[])
         {
            short[] arr = (short[]) data;
            return arr[index];
         }
         else if (data instanceof int[])
         {
            int[] arr = (int[]) data;
            return arr[index];
         }
         else if (data instanceof long[])
         {
            long[] arr = (long[]) data;
            return arr[index];
         }
         else if (data instanceof float[])
         {
            float[] arr = (float[]) data;
            return arr[index];
         }
         else if (data instanceof double[])
         {
            double[] arr = (double[]) data;
            return arr[index];
         }
         else
         {
            throw new IllegalArgumentException("Not valid!");
         }
      }
      else
      {
         // sample individual voxel...
         InputStream in = img.openStream();
         int offset = index * header.getBitpix() / 8 + 
         (header.isSingleNIFTIFile() ? (int) header.getVoxOffset() : 0);
         in.skip(offset);
         byte[] b = new byte[32];
         in.read(b);
         ByteBuffer buf = ByteBuffer.wrap(b);
         buf.order(header.getEndian());
         return (Number) header.readNextObj(buf);
      }
   }

   /**
    * Get the specified index
    * @param x
    * @param y
    * @param z
    * @param t
    * @param i5
    * @return
    */
   public int getIndex(int x, int y, int z, int t, int i5)
   {
      if (x < 0 || y <0 || z < 0 || t< 0 || i5 < 0 || x >= maxX ||
            y >= maxY || z >= maxZ || t >= maxT || i5 >= maxI5)
         return -1;
      return (((i5 * maxT + t) * maxZ + z) * maxY + y) * maxX + x;
   }

   /**
    * Make the 4x4 transform.  
    * @return
    */
   private double[] makeTransform()
   {
      return header.getTransform(true);
   }

   /**
    * Get the array as an array of signed numbers.  
    * They may be double[], float[], int[], long[], short[], char[], or byte[].
    * In the future other types may be supported.
    * @return a signed array representing the units.
    * @throws IOException if anything goes wrong
    */
   public Object getArray() throws IOException
   {
      Object data = getDataNoLoad();
      if (data == null)
         data = getData();
      return data;
   }

   /**
    * Eliminate any cached data that may take up memory.  Many method calls
    * will require going back to the source for more data.
    */
   public void minimizeFootprint()
   {
      myData = null;
   }

   /**
    * Get the data as an array array of doubles.  This will likely
    * require casting the data and may require a new allocation of the
    * array every time it is called.
    * @return an array of doubles.
    * @throws IOException if anything goes wrong
    */
   public double[] getDoubleArray() throws IOException
   {
      Object data = getArray();
      double[] answer;
      if (data instanceof byte[])
      {
         byte[] arr = (byte[]) data;
         answer = new double[arr.length];
         for (int i = 0; i < arr.length; i++)
         {
            answer[i] = arr[i];
         }
      }
      else if (data instanceof char[])
      {
         char[] arr = (char[]) data;
         answer = new double[arr.length];
         for (int i = 0; i < arr.length; i++)
         {
            answer[i] = arr[i];
         }
      }
      else if (data instanceof short[])
      {
         short[] arr = (short[]) data;
         answer = new double[arr.length];
         for (int i = 0; i < arr.length; i++)
         {
            answer[i] = arr[i];
         }
      }
      else if (data instanceof int[])
      {
         int[] arr = (int[]) data;
         answer = new double[arr.length];
         for (int i = 0; i < arr.length; i++)
         {
            answer[i] = arr[i];
         }
      }
      else if (data instanceof long[])
      {
         long[] arr = (long[]) data;
         answer = new double[arr.length];
         for (int i = 0; i < arr.length; i++)
         {
            answer[i] = arr[i];
         }
      }
      else if (data instanceof float[])
      {
         float[] arr = (float[]) data;
         answer = new double[arr.length];
         for (int i = 0; i < arr.length; i++)
         {
            answer[i] = arr[i];
         }
      }
      else if (data instanceof double[])
      {
         answer = (double[]) data;
      }
      else
      {
         throw new IllegalStateException("Can't deal with " + data.getClass());
      }
      return answer;
   }

   public URL getHdr()
   {
      return hdr;
   }

   public URL getAtlas()
   {
      return atlas;
   }

   public URL getImg()
   {
      return img;
   }

   public URL getMat()
   {
      return mat;
   }

   /**
    * Get the data as a ByteBuffer.  The proper Endian will be set.
    * @return A Bytebuffer, check the header for the type of data.
    * @throws IOException
    */
   public ByteBuffer getDataBuffer() throws IOException
   {   
      ByteBuffer buf;
      InputStream stream = getImageStream();
      buf = FileUtilities.getBuffer(stream, getNumBytes(), null);

      buf.order(header.getEndian());
      stream.close();
      return buf;
   }

   /**
    * Get the data as a byte[].
    * @return A byte[]
    * @throws IOException
    */
   public byte[] getDataBytes() throws IOException
   {   
      byte[] buf;
      InputStream stream = getImageStream();
      buf = FileUtilities.getBytes(stream, getNumBytes(), null);

      stream.close();
      return buf;
   }

   private InputStream getImageStream() throws IOException
   {
      InputStream stream = img.openStream();
      stream = FileUtilities.getStream(stream, EnumSet.allOf(Compression.class));
      int offset = (int) header.getVoxOffset();
      if (header.isSingleNIFTIFile())
      {
         if (offset < 348)
            offset = 348;
         // skip to data
         stream.skip(offset);
      }
      else
      {
         if (offset < 0)
            offset = 0;
         stream.skip(offset);
      }
      return stream;
   }

   private Object getDataNoLoad()
   {
      Object data = softData != null ? softData.get() : null;
      return data;
   }

   private int readChunk(InputStream stream, byte[] buf) throws IOException
   {
      int len = 0;
      while (len < buf.length)
      {
         int read = stream.read(buf, len, buf.length - len);
         if (read < 0)
            return len;
         else
            len += read;
      }
      return len;
   }

   private int getNumVoxels()
   {
      int v = 1;
      short[] dim = header.getDim();
      for (int i = 1, size = dim[0]; i <= size; i++)
      {
         v *= dim[i];
      }
      return v;
   }

   private int getNumBytes()
   {
      int estimatedSize = header.getBitpix() / 8;
      if (estimatedSize <= 0)
      {
         estimatedSize = 1;
      }
      return estimatedSize * getNumVoxels() + (int) (header.getVoxOffset() + 348);
   }

   private Object getData() throws IOException
   {
      InputStream stream = getImageStream();

      // read from the stream in big chunks
      byte[] bbuf = new byte[header.getBitpix() * 1024 * 100];
      ByteBuffer buf = ByteBuffer.wrap(bbuf);
      buf.order(header.getEndian());
      Object arr;

      int numVoxels = getNumVoxels();
      switch (header.getDatatype())
      {
         case 2:
            arr = new short[numVoxels];
            break;
         case 4:
            arr = new short[numVoxels];
            break;
         case AnalyzeNiftiSpmHeader.DT_UINT16:
            arr = new char[numVoxels];
            break;
         case 8:
            arr = new int[numVoxels];
            break;
         case 16:
            arr = new float[numVoxels];
            break;
         case 64:
            arr = new double[numVoxels];
            break;
         case AnalyzeNiftiSpmHeader.DT_INT8:
            arr = new byte[numVoxels];
            break;
         case AnalyzeNiftiSpmHeader.DT_RGB24:
            arr = new int[numVoxels];
            break;
         default:
            throw new java.lang.IllegalArgumentException("Unsupported type " + header.getDTName(header.getDatatype()));
      } 

      int i = 0;
      while (i < numVoxels)
      {
         buf.position(0);
         int total = readChunk(stream, bbuf);
         if (total == 0)
         {
            System.err.println("Wrong length for file.");
            break;
         }
         buf.limit(total);
         while (buf.hasRemaining())
         {            

            switch (header.getDatatype())
            {
               case AnalyzeNiftiSpmHeader.DT_UINT8:
               {
                  short[] myarr = (short[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = buf.get();
                     i++;
                  }
                  break;
               }
               case AnalyzeNiftiSpmHeader.DT_INT16:
               {
                  short[] myarr = (short[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = buf.getShort();
                     i++;
                  }
                  break;
               }
               case AnalyzeNiftiSpmHeader.DT_UINT16:
               {
                  char[] myarr = (char[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = buf.getChar();
                     i++;
                  }
                  break;
               }
               case AnalyzeNiftiSpmHeader.DT_INT32:
               {
                  int[] myarr = (int[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = buf.getInt();
                     i++;
                  }
                  break;
               }
               case AnalyzeNiftiSpmHeader.DT_FLOAT32:
               {
                  float[] myarr = (float[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = buf.getFloat();
                     i++;
                  }
                  break;
               }
               case AnalyzeNiftiSpmHeader.DT_FLOAT64:
               {
                  double[] myarr = (double[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = buf.getDouble();
                     i++;
                  }
                  break;
               }
               case AnalyzeNiftiSpmHeader.DT_INT8:
               {
                  byte[] myarr = (byte[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = buf.get();
                     i++;
                  }
                  break;
               }
               case AnalyzeNiftiSpmHeader.DT_RGB24:
               {
                  int[] myarr = (int[]) arr;
                  while (buf.hasRemaining()) {
                     myarr[i] = AnalyzeNiftiSpmHeader.assembleRGB(buf);
                     i++;
                  }
                  break;
               }
               default:
                  throw new java.lang.IllegalArgumentException("Unsupported type " + header.getDTName(header.getDatatype()));
            } 
         }
      }
      myData = arr;
      softData = new SoftReference<Object>(myData);
      return myData;
   }
   
   /**
    * 
    * @param index
    * @return
    */
   public Iterator<byte[]> getExtendedHeaders() {
	   if (header.hasExtended()) {
		   throw new UnsupportedOperationException("Not yet implemented");
	   } else {
		   return Collections.<byte[]>emptySet().iterator();
	   }
   }
}
