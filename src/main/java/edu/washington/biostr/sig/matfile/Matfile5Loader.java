/*
 * Matfile Library (Pure Java MatLab file decoder)
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
package edu.washington.biostr.sig.matfile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.GZIPInputStream;

/**
 * <p>Title: Brain Visualizer</p>
 * <p>Description: Displays and allows manipulation of 3d brain models</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Structural Informatics Group</p>
 * <p>Version 4 Matfiles are stored in much simpler format.  It is a continuous
 * stream of matrices, each with a 20 byte header (5 4 byte integers).
 * The header is in the following format: The first int is divided into four
 * parts: MOPT with M being the thousands digit (encoding, ie little endian),
 * O being the hundreds digit (always 0), P being the 10's digit (the data type)
 * and T is the matrix type (1's digit).  The second int is the number of rows,
 * the third is the number of columns, the fourth is whether there is imaginary
 * data or not and the fifth is the name length.</p><br>
 * <p>Currently only full numeric matrices are supported</p>
 * @author Eider Moore
 * @version 1.0
 */

class Matfile5Loader
    extends MatfileLoader
{
   public static final int miINT8 = 1;
   public static final int miUINT8 = 2;
   public static final int miINT16 = 3;
   public static final int miUINT16 = 4;
   public static final int miINT32 = 5;
   public static final int miUINT32 = 6;

   public static final int miSINGLE = 7;
   public static final int miDOUBLE = 9;
   public static final int miINT64 = 12;
   public static final int miUINT64 = 13;
   public static final int miMATRIX = 14;
   public static final int miCOMPRESSED = 15;
   public static final int miUTF8 = 16;
   public static final int miUTF16 = 17;
   public static final int miUTF32 = 18;

   public static final int mxCELL_CLASS = 1;
   public static final int mxSTRUCT_CLASS = 2;
   public static final int mxOBJECT_CLASS = 3;
   public static final int mxCHAR_CLASS = 4;
   public static final int mxSPARSE_CLASS = 5;
   public static final int mxDOUBLE_CLASS = 6;
   public static final int mxSINGLE_CLASS = 7;
   public static final int mxINT8_CLASS = 8;
   public static final int mxUINT8_CLASS = 9;
   public static final int mxINT16_CLASS = 10;
   public static final int mxUINT16_CLASS = 11;
   public static final int mxINT32_CLASS = 12;
   public static final int mxUINT32_CLASS = 13;

   String description;
   int version;

   private ByteBuffer bBuf;

   protected Matfile5Loader(ByteBuffer bBuf)
   {
      this.bBuf = bBuf;
      checkEndianAndType();
   }

   /**
    * Try and set the endian value.  We do this by checking if whether or not
    * the last 2 bytes of the header (bytes 127-8) are IM or MI and if the 2
    * bytes before that (bytes 125-6) indicate if this is a version 5 file.
    */
   private void checkEndianAndType()
   {
      bBuf.rewind();

      bBuf.position(0);
      byte[] desc = new byte[116];
      for (int i = 0; i < desc.length; i++)
      {
         desc[i] = bBuf.get();
      }
      description = new String(desc);
      // subsys data offset.  I don't know how to use it, so we will skip the
      // next 8 bytes
      bBuf.position(bBuf.position() + 8);
      // version number.  0x0100 = v5
      version = bBuf.getShort();

      byte endian1 = bBuf.get();
      byte endian2 = bBuf.get();

      if ( (endian1 == 'M') && (endian2 == 'I'))
      {
         bBuf.order(ByteOrder.BIG_ENDIAN);
      }
      else if ( (endian1 == 'I') && (endian2 == 'M'))
      {
         bBuf.order(ByteOrder.LITTLE_ENDIAN);
      }

   }

   /**
    * This gets the next element in a mat v5 file.  Sparse matrices,
    * objects and structs are not supported yet.
    * @return The MatVar of the appropriate type that corresponds to the
    * next element in the array.
    */
   protected MatVar loadNext()
   {
      if (bBuf.position() < (bBuf.limit()))
      {
         return process(bBuf);
      }
      else
      {
         return null;
      }
   }

   private MatVar process(ByteBuffer buf)
   {
      int[] tags;
      tags = readDataElementTags(buf);
      // we only support arrays for returning matvars, but we also handle
      // decompression.
      switch (tags[0])
      {

         case miMATRIX:
            return readArray(buf, tags[1]);
         case miCOMPRESSED:
            ByteBuffer uncompressed = deCompress(buf, tags[1]);
            return process(uncompressed);
         case miINT8:
         case miUINT8:
         case miINT16:
         case miUINT16:
         case miINT32:
         case miUINT32:
         case miSINGLE:
         case miDOUBLE:
         case miINT64:
         case miUINT64:
         case miUTF8:
         case miUTF16:
         case miUTF32:
         default:
            throw new IllegalArgumentException(
                "Only returning arrays are supported");

      }
   }

   /**
    * Read buf from the current position and determine the values in the
    * element's tag.  The tag can be either 4 bytes or 8 bytes, so this takes
    * that into account.  It leaves the buf so the current position is
    * pointed at the start of the data area.  The mark is also moved to the
    * start of the tag.
    * @return a 3 element array of integers.  The first element is the type and the second is the number of bytes of data and the third is the number of bytes in the tag.
    */
   public int[] readDataElementTags(ByteBuffer buf)
   {
      // check the first 2 bytes to see if they are 0.
      // if they are 0, it is a long data type.
      int[] answer = new int[3];
      buf.mark();

      // get the first 4 bytes.  If the first 2 bytes in the 4 are not 0,
      // then the it is a compressed tag.
      int type = buf.getInt();
      int size;
      if (type > 0xFFFF)
      {
         answer[2] = 4;
         buf.reset();
         type = buf.getShort();
         size = buf.getShort();
      }
      else
      {
         answer[2] = 8;
         size = buf.getInt();
      }
      answer[0] = type;
      answer[1] = size;
      return answer;
   }

   public ByteBuffer deCompress(final ByteBuffer buf, final int byteSize)
   {
      try
      {
         byte[] temp = new byte[byteSize];
         byte[] answer = new byte[byteSize * 2];

         buf.get(temp);
         ByteArrayInputStream stream = new ByteArrayInputStream(temp);

         GZIPInputStream gzip = new GZIPInputStream(stream);

         // look this up
         int cur = 0;
         int loc = 0;
         int end = answer.length;
         boolean readMore = true;
         while (readMore)
         {
            cur = gzip.read(answer, loc, end - loc);
            if (cur >= 0)
            {
               loc += cur;
               byte[] newAns = new byte[answer.length + byteSize];
               System.arraycopy(answer, 0, newAns, 0, answer.length);
               answer = newAns;
            }
            else
            {
               readMore = false;
            }
         }

         ByteBuffer answerB = ByteBuffer.wrap(answer);
         answerB.rewind();
         return answerB;
      }
      catch (IOException ex)
      {
         throw new IllegalArgumentException("Incorrectly compressed file");
      }
   }

   /**
    * 
    * @param buf
    * @param byteSize
    * @return
    */
   // some variables are not currently used but may help people extend this class.
   @SuppressWarnings("unused")
   public MatVar readArray(ByteBuffer buf, int byteSize)
   {
      int[] tags;
      Object real;
      Object imag = null;
      int[] dim;
      int type;
      int[] arrFlags;
      int realType;
      int imagType = -1;

      // read the flags (8 byte... 2 undefined, 1 flag, 1 class, 4 undefined)
      tags = readDataElementTags(buf);
      arrFlags = (int[]) readData(buf, tags[0], tags[1], tags[2]);
      // for the flags, the 5th position = complex, 6th = global, 7th= logical
      int flags = arrFlags[0] >> 8;
      boolean complex = (flags & 0x8) > 0;
      // These aren't used, but are part of the specification.   Anyone who is going to 
      // expand this will appreciate having them available.
      boolean global = (flags & 0x4) > 0;
      boolean logical = (flags & 0x2) > 0;

      type = arrFlags[0] & 0xFF;

      // dimension array
      tags = readDataElementTags(buf);
      dim = (int[]) readData(buf, tags[0], tags[1], tags[2]);

      // array name
      tags = readDataElementTags(buf);
      String name;
      Object chars = readData(buf, tags[0], tags[1], tags[2]);
      if (chars instanceof char[])
      {
         name = new String( (char[]) chars);
      }
      else
      {
         name = new String( (byte[]) chars);
      }

      MatVar answer;

      // real part
      tags = readDataElementTags(buf);
      real = readData(buf, tags[0], tags[1], tags[2]);
      realType = tags[0];
      // imaginary part
      if (complex)
      {
         tags = readDataElementTags(buf);
         imag = readData(buf, tags[0], tags[1], tags[2]);
         imagType = tags[0];
      }
      answer = setArray(name, dim, type, complex, real, realType, imag,
                        imagType);
      return answer;
   }

   private MatVar setArray(String name, int[] dim, int type, boolean complex,
                           Object real, int realType,
                           Object imag, int imagType)
   {
      MatVar answer;
      switch (type)
      {
         case mxCELL_CLASS:
         case mxSTRUCT_CLASS:
         case mxOBJECT_CLASS:
         case mxCHAR_CLASS:
         case mxSPARSE_CLASS:
            throw new UnsupportedOperationException("Bad array class.");
         case mxDOUBLE_CLASS:
            answer = new MatDouble(name, dim, complex);
            break;
         case mxSINGLE_CLASS:
            answer = new MatFloat(name, dim, complex);
            break;
         case mxINT8_CLASS:
            answer = new MatInt8(name, dim, complex);
            break;
         case mxUINT8_CLASS:
            answer = new MatIntU8(name, dim, complex);
            break;
         case mxINT16_CLASS:
            answer = new MatInt16(name, dim, complex);
            break;
         case mxUINT16_CLASS:
            answer = new MatIntU16(name, dim, complex);
            break;
         case mxINT32_CLASS:
            answer = new MatInt32(name, dim, complex);
            break;
         case mxUINT32_CLASS:
            answer = new MatIntU32(name, dim, complex);
            break;
         default:
            throw new UnsupportedOperationException("Bad array class: " + type);
      }

      int length;
      switch (realType)
      {
         case miDOUBLE:
            length = ( (double[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setDouble(i, ( (double[]) real)[i]);
            }
            break;
         case miSINGLE:
            length = ( (float[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setDouble(i, ( (float[]) real)[i]);
            }
            break;
         case miINT8:
            length = ( (byte[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setLong(i, ( (byte[]) real)[i]);
            }
            break;
         case miUINT8:
            length = ( (byte[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setLong(i, ( (byte[]) real)[i] & 0xff);
            }
            break;
         case miINT16:
            length = ( (short[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setLong(i, ( (short[]) real)[i]);
            }
            break;
         case miUINT16:
            length = ( (char[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setLong(i, ( (char[]) real)[i]);
            }
            break;
         case miINT32:
            length = ( (int[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setLong(i, ( (int[]) real)[i]);
            }
            break;
         case miUINT32:
            length = ( (int[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setLong(i, ( (int[]) real)[i] & 0xffffffffL);
            }
            break;
         case miINT64:
            length = ( (long[]) real).length;
            for (int i = 0; i < length; i++)
            {
               answer.setLong(i, ( (long[]) real)[i]);
            }
            break;
         case miUINT64:
            length = ( (long[]) real).length;
            for (int i = 0; i < length; i++)
            {
               long l = ( (long[]) real)[i];
               answer.setDouble(i, l < 0 ? l + 18446744073709551616.0 : l);
            }
            break;
      }

      if (complex)
      {
         switch (imagType)
         {
            case miDOUBLE:
               length = ( (double[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setDoubleImag(i, ( (double[]) imag)[i]);
               }
               break;
            case miSINGLE:
               length = ( (float[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setDoubleImag(i, ( (float[]) imag)[i]);
               }
               break;
            case miINT8:
               length = ( (byte[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setLongImag(i, ( (byte[]) imag)[i]);
               }
               break;
            case miUINT8:
               length = ( (byte[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setLongImag(i, ( (byte[]) imag)[i] + 0x100);
               }
               break;
            case miINT16:
               length = ( (short[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setLongImag(i, ( (short[]) imag)[i]);
               }
               break;
            case miUINT16:
               length = ( (short[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setLongImag(i, ( (short[]) imag)[i] + 0x10000);
               }
               break;
            case miINT32:
               length = ( (int[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setLongImag(i, ( (int[]) imag)[i]);
               }
               break;
            case miUINT32:
               length = ( (int[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setLongImag(i, ( (int[]) imag)[i] + 0x100000000L);
               }
               break;
            case miINT64:
               length = ( (long[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setLongImag(i, ( (long[]) imag)[i]);
               }
               break;
            case miUINT64:
               length = ( (long[]) imag).length;
               for (int i = 0; i < length; i++)
               {
                  answer.setDoubleImag(i,
                                       ( (long[]) imag)[i] + 18446744073709551616.0);
               }
               break;
         }
      }

      return answer;
   }

   /**
    * This allows us to read a data element that is not compressed or an array.
    * It reads the data in buf from the current position and leaves the bBuf
    * in the position at the end of the data.  After reading the data, it
    * returns an array of the data.  Because this doesn't support all
        * data elements, you should read the tags first and include the size and type
    * in this function.  If the type is unknown, specify type as -1.
    * This however is not recomended because it will return null if the type
    * is an array or compressed.
    * @param start This is the location in bBuf where the data is located.
    * @return
    */
   private Object readData(ByteBuffer buf, int type, int byteSize, int tagSize)
   {
      int[] ansI;
      short[] ansS;
      byte[] ansB;
      long[] ansL;
      double[] ansD;
      char[] ansC;
      int shift = (8 - ( (byteSize + tagSize) % 8)) % 8;

      switch (type)
      {
         case miDOUBLE:
            ansD = new double[byteSize / 8];
            for (int i = 0; i < ansD.length; i++)
            {
               ansD[i] = buf.getDouble();
            }

            buf.position(buf.position() + shift);
            return ansD;
         case miSINGLE:
            float[] ansF = new float[byteSize / 4];
            for (int i = 0; i < ansF.length; i++)
            {
               ansF[i] = buf.getFloat();
            }

            buf.position(buf.position() + shift);
            return ansF;
         case miINT8:
         case miUINT8:
            ansB = new byte[byteSize];
            for (int i = 0; i < ansB.length; i++)
            {
               ansB[i] = buf.get();
            }

            buf.position(buf.position() + shift);
            return ansB;
         case miINT16:
            ansS = new short[byteSize / 2];
            for (int i = 0; i < ansS.length; i++)
            {
               ansS[i] = buf.getShort();
            }

            buf.position(buf.position() + shift);
            return ansS;
         case miUINT16:
            ansC = new char[byteSize / 2];
            for (int i = 0; i < ansC.length; i++)
            {
               ansC[i] = buf.getChar();
            }

            buf.position(buf.position() + shift);
            return ansC;
         case miINT32:
         case miUINT32:
            ansI = new int[byteSize / 4];
            for (int i = 0; i < ansI.length; i++)
            {
               ansI[i] = buf.getInt();
            }

            buf.position(buf.position() + shift);
            return ansI;
         case miINT64:
         case miUINT64:
            ansL = new long[byteSize / 8];
            for (int i = 0; i < ansL.length; i++)
            {
               ansL[i] = buf.getLong();
            }

            buf.position(buf.position() + shift);
            return ansL;
         case miUTF8:
            ansC = new char[byteSize];
            for (int i = 0; i < ansC.length; i++)
            {
               ansC[i] = (char) buf.get();
            }

            buf.position(buf.position() + shift);
            return ansC;
         case miUTF16:
            ansC = new char[byteSize / 2];
            for (int i = 0; i < ansC.length; i++)
            {
               ansC[i] = buf.getChar();
            }

            buf.position(buf.position() + shift);
            return ansC;
         case miUTF32:
            ansC = new char[byteSize / 4];
            for (int i = 0; i < ansC.length; i++)
            {
               ansC[i] = (char) buf.getInt();
            }

            buf.position(buf.position() + shift);
            return ansC;
      }
      return null;
   }
}
