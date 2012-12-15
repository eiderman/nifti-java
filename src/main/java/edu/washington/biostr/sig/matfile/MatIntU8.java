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

public class MatIntU8
    extends MatVar
{
   public final short MAX = 0x100;
   private byte[] data;
   private byte[] imag;

   public MatIntU8(String name, int[] dim, int imagf)
   {
      this(name, dim, imagf != 0);
   }

   public MatIntU8(String name, int[] dim, boolean imagf)
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
   public short getUByte(int[] index)
   {
      int i = getIndex(index);
      short num = data[i];
      if (num > 0)
      {
         return num;
      }
      else
      {
         // the maximum value is 2^8 = MAX
         // I'm assuming 2's compliment
         num = (short) (MAX + num);
         return num;
      }
   }

   public int getInt(int... i)
   {
      return getUByte(i);
   }

   public double getDouble(int... i)
   {
      return getUByte(i);
   }

   public short getImag(int[] index)
   {
      int i = getIndex(index);
      short num = imag[i];
      if (num > 0)
      {
         return num;
      }
      else
      {
         // the maximum value is 2^8 = 256
         // I'm assuming 2's compliment
         num = (short) (MAX + num);
         return num;
      }
   }

   public boolean containsImaginary()
   {
      return (imag != null);
   }

   public Short getImaginary(int... loc)
   {
      if (containsImaginary())
      {
         return new Short(getImag(loc));
      }
      else
      {
         return new Short( (short) 0);
      }
   }

   /**
    * Returns a Double with the value stored in index.
    * @param index the index of the value
    * @return a Double with the value.
    */
   public Short getData(int... index)
   {
      return getNumber(index);
   }

   /**
    * Returns a Integer with the value stored in index.
    * @param index the index of the value
    * @return a Double with the value.
    */
   public Short getNumber(int... index)
   {
      Short answer = new Short(getUByte(index));
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
      int temp = ( (int) value) % MAX - MAX;
      data[index] = (byte) temp;
   }

   protected void setLong(int index, long value)
   {
      int temp = ( (int) value) % MAX - MAX;
      data[index] = (byte) temp;
   }

   protected void setDoubleImag(int index, double value)
   {
      int temp = ( (int) value) % MAX - MAX;
      imag[index] = (byte) temp;
   }

   protected void setLongImag(int index, long value)
   {
      int temp = ( (int) value) % MAX - MAX;
      imag[index] = (byte) temp;
   }

}