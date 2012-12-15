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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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

class Matfile4Loader
extends MatfileLoader
{
   public static final int LITTLE_ENDIAN = 0;
   public static final int BIG_ENDIAN = 1;
   public static final int VAX_D_FLOAT = 2;
   public static final int VAX_G_FLOAT = 3;
   public static final int CRAY = 4;
   public static final int P_DOUBLE = 0;
   public static final int P_SINGLE = 1;
   public static final int P_32 = 2;
   public static final int P_16 = 3;
   public static final int P_U16 = 4;
   public static final int P_8 = 5;
   public static final int T_FULL = 0;
   public static final int T_TEXT = 1;
   public static final int T_SPARSE = 2;
   
   private ByteBuffer bBuf;
   
   protected Matfile4Loader(ByteBuffer bBuf)
   {
      this.bBuf = bBuf;
      setEndian();
   }
   
   /**
    * Try and set the endian value.  We do this by checking if the first 4
    * bytes are in the appropriate range.
    */
   private void setEndian()
   {
      bBuf.order(ByteOrder.LITTLE_ENDIAN);
      bBuf.rewind();
      
      int type = bBuf.getInt();
      // it is saved in native machine format so this could be in a different
      // byte order then what we read it in as.  And because we read it as
      // a 4 byte int, this could really affect how we display it.
      // If the number is greater than the maximum that it should be then
      // we must have read it in the wrong format.  I only support
      // Big Endian and Little Endian
      // It should never exceed 4052
      if (type > 4052)
      {
         bBuf.rewind();
         bBuf.order(ByteOrder.BIG_ENDIAN);
         type = bBuf.getInt();
         // double check that the number is valid.
         if ( (type / 1000) != BIG_ENDIAN)
         {
            throw new IllegalArgumentException("Invalid file format");
         }
      }
      else
      {
         // double check that the number is valid.
         if ( (type / 1000) != LITTLE_ENDIAN)
         {
            throw new IllegalArgumentException("Invalid file format");
         }
      }
      bBuf.position(0);
   }
   
   /**
    * This gets the next element in a mat v4 file.  Every element should be
    * stored as an array in this format, so we assume that we can see the
    * array header.  This does not support reading of sparse matrices yet.
    * @return The MatVar of the appropriate type that corresponds to the
    * next element in the array.
    */
   // some variables are not used but will help people to extend this without reading the spec.
   @SuppressWarnings("unused")
   protected MatVar loadNext()
   {
      if (bBuf.remaining() <= 20)
      {
         return null;
      }
      //assume that we point to the begining of the header of an array
      // O is always 0 and M should be useless since we already know the
      // endian value.
      int MOPT = bBuf.getInt();
      // M is not used, but is part of the spec.
      int M = MOPT / 1000;
      int P = (MOPT / 10) % 10;
      int T = MOPT % 10;
      
      int rows = bBuf.getInt();
      int cols = bBuf.getInt();
      int imagf = bBuf.getInt();
      int namLength = bBuf.getInt();
      
      // last char is always \0, so we should just ignore this because java
      // doesn't require strings to be terminated with that char.
      byte[] name = new byte[namLength - 1];
      for (int i = 0; i < (namLength - 1); i++)
      {
         name[i] = bBuf.get();
      }
      bBuf.get();
      
      int[] dim = new int[2];
      dim[0] = rows;
      dim[1] = cols;
      
      switch (T)
      {
         case T_FULL:
            return readFullArray(name, dim, P, imagf);
         case T_TEXT:
            return readTextArray(name, dim, P);
         case T_SPARSE:
            throw new java.lang.UnsupportedOperationException(
            "Sparse matrix not supported");
         default:
            throw new java.lang.UnsupportedOperationException(
            "invalid array type");
         
      }
      
   }
   
   /**
    * Read in the text array.  According to the matlab v4 file specification,
    * text arrays are stored as floating point numbers with values between
    * 0 and 255, so this only supports reading singles and doubles.
    * Returns a MatText value for the next array.
    */
   private MatText readTextArray(byte[] name, int[] dim, int P)
   {
      MatText data = new MatText(new String(name), dim);
      switch (P)
      {
         case P_DOUBLE:
            
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  data.putValue(index, bBuf.getDouble());
               }
            }
            return data;
         case P_SINGLE:
            
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  data.putValue(index, bBuf.getFloat());
               }
            }
            return data;
         default:
            throw new java.lang.UnsupportedOperationException(
            "Invalid data type");
      }
      
   }
   
   /**
    * Read the next argument, which is a full numeric array.  This supports
    * reading arrays with or without imaginary components.  Most of the
    * parameters require information found in the header, which has already
    * been read.
    * @param name The name of the array in the form that it was stored in in
    * the file.
    * @param dim The dimensions of the array.
    * @param P The datatype.
    * @param imag Whether it contains any imaginary components.
    * @return The MatVar that is stored in the array.
    */
   private MatVar readFullArray(byte[] name, int[] dim, int P, int imag)
   {
      switch (P)
      {
         case P_DOUBLE:
            MatDouble dataD = new MatDouble(new String(name), dim, imag);
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  dataD.putValue(index, bBuf.getDouble());
               }
            }
            if (imag != 0)
            {
               for (int i = 0; i < dim[1]; i++)
               {
                  for (int j = 0; j < dim[0]; j++)
                  {
                     int[] index = new int[2];
                     index[0] = j;
                     index[1] = i;
                     dataD.putImagValue(index, bBuf.getDouble());
                  }
               }
            }
            return dataD;
         case P_SINGLE:
            MatFloat dataF = new MatFloat(new String(name), dim, imag);
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  dataF.putValue(index, bBuf.getFloat());
               }
            }
            if (imag != 0)
            {
               for (int i = 0; i < dim[1]; i++)
               {
                  for (int j = 0; j < dim[0]; j++)
                  {
                     int[] index = new int[2];
                     index[0] = j;
                     index[1] = i;
                     dataF.putImagValue(index, bBuf.getFloat());
                  }
               }
            }
            return dataF;
         case P_32:
            MatInt32 data32 = new MatInt32(new String(name), dim, imag);
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  data32.putValue(index, bBuf.getInt());
               }
            }
            if (imag != 0)
            {
               for (int i = 0; i < dim[1]; i++)
               {
                  for (int j = 0; j < dim[0]; j++)
                  {
                     int[] index = new int[2];
                     index[0] = j;
                     index[1] = i;
                     data32.putImagValue(index, bBuf.getInt());
                  }
               }
            }
            return data32;
         case P_16:
            MatInt16 data16 = new MatInt16(new String(name), dim, imag);
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  data16.putValue(index, bBuf.getShort());
               }
            }
            if (imag != 0)
            {
               for (int i = 0; i < dim[1]; i++)
               {
                  for (int j = 0; j < dim[0]; j++)
                  {
                     int[] index = new int[2];
                     index[0] = j;
                     index[1] = i;
                     data16.putImagValue(index, bBuf.getShort());
                  }
               }
            }
            return data16;
         case P_U16:
            MatIntU16 dataU16 = new MatIntU16(new String(name), dim, imag);
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  dataU16.putValue(index, bBuf.getChar());
               }
            }
            if (imag != 0)
            {
               for (int i = 0; i < dim[1]; i++)
               {
                  for (int j = 0; j < dim[0]; j++)
                  {
                     int[] index = new int[2];
                     index[0] = j;
                     index[1] = i;
                     dataU16.putImagValue(index, bBuf.getChar());
                  }
               }
            }
            return dataU16;
         case P_8:
            MatIntU8 dataU8 = new MatIntU8(new String(name), dim, imag);
            // they are stored as a list of rows, so we read them in as
            // such.  This looks kind of backwards, but it works.
            for (int i = 0; i < dim[1]; i++)
            {
               for (int j = 0; j < dim[0]; j++)
               {
                  int[] index = new int[2];
                  index[0] = j;
                  index[1] = i;
                  dataU8.putValue(index, bBuf.get());
               }
            }
            if (imag != 0)
            {
               for (int i = 0; i < dim[1]; i++)
               {
                  for (int j = 0; j < dim[0]; j++)
                  {
                     int[] index = new int[2];
                     index[0] = j;
                     index[1] = i;
                     dataU8.putImagValue(index, bBuf.get());
                  }
               }
            }
            return dataU8;
         default:
            throw new java.lang.UnsupportedOperationException(
            "Invalid data type");
         
      }
      
   }
}