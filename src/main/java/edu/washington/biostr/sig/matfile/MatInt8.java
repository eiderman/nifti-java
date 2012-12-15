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

/**
 * <p>Title: Brain Visualizer</p>
 * <p>Description: Displays and allows manipulation of 3d brain models</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Structural Informatics Group</p>
 * @author Eider Moore
 * @version 1.0
 */

public class MatInt8
    extends MatVar
{
   private byte[] data;
   private byte[] imag;

   public MatInt8(String name, int[] dim, int imagf)
   {
      this(name, dim, imagf != 0);
   }

   public MatInt8(String name, int[] dim, boolean imagf)
   {
      super(name, dim);
      int size = 1;
      for (int i = 0; i < dim.length; i++)
      {
         size *= dim[i];
      }
      data = new byte[size];
      if (imagf)
      {
         imag = new byte[size];
      }
   }

   /**
    * returns the int at that location converted to a positive integer.  We
    * need to increase the size to a short in order to pull this off.
    * This is the most natural call for this class.
    * @param index the index as an array of ints
    * @return returns a int at the specified index.
    */
   public byte getByte(int[] index)
   {
      int i = getIndex(index);
      byte num = data[i];
      return num;
   }

   public int getInt(int... i)
   {
      return getByte(i);
   }

   public double getDouble(int... i)
   {
      return getByte(i);
   }

   public byte getImag(int[] index)
   {
      int i = getIndex(index);
      byte num = imag[i];
      return num;
   }

   public boolean containsImaginary()
   {
      return (imag != null);
   }

   public Byte getImaginary(int... loc)
   {
      if (containsImaginary())
      {
         return new Byte(getImag(loc));
      }
      else
      {
         return new Byte( (byte) 0);
      }
   }

   /**
    * Returns a Double with the value stored in index.
    * @param index the index of the value
    * @return a Double with the value.
    */
   public Byte getData(int... index)
   {
      return getNumber(index);
   }

   /**
    * Returns a Integer with the value stored in index.
    * @param index the index of the value
    * @return a Double with the value.
    */
   public Byte getNumber(int... index)
   {
      Byte answer = new Byte(getByte(index));
      return answer;
   }

   /**
    * Everything in a MatInt32 can be returned as a number and none
    * can be returned as a MatVar.
    * @param index not used
    * @return true
    */
   public boolean isNumber(int... index)
   {
      return true;
   }

   public MatVar getMatVar(int... index)
   {
      throw new java.lang.UnsupportedOperationException();
   }

   public int type()
   {
      return UINT8;
   }

   public void putAll(byte[] values)
   {
      this.data = values;
   }

   public void putAllImage(byte[] values)
   {
      this.imag = values;
   }

   public void putValue(int[] index, byte value)
   {
      int i = getIndex(index);
      data[i] = value;
   }

   public void putImagValue(int[] index, byte value)
   {
      int i = getIndex(index);
      imag[i] = value;
   }

   protected void setDouble(int index, double value)
   {
      data[index] = (byte) value;
   }

   protected void setLong(int index, long value)
   {
      data[index] = (byte) value;
   }

   protected void setDoubleImag(int index, double value)
   {
      imag[index] = (byte) value;
   }

   protected void setLongImag(int index, long value)
   {
      imag[index] = (byte) value;
   }

}
