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
 * <p> This is a MatVar that specifically is designed to store Int16s.  It
 * can only hold shorts and shorts with imaginary components.
 * </p>
 * @author Eider Moore
 * @version 1.0
 */
public class MatInt16
    extends MatVar
{
   private short[] data;
   private short[] imag;

   public MatInt16(String name, int[] dim, int imagf)
   {
      this(name, dim, imagf != 0);
   }

   /**
    * Create a new MatInt16.
    * @param name The name.
    * @param dim The dimensions.
    * @param imagf Whether or not to have imaginary numbers, if 0 we have no
    * imaginary numbers, otherwise we have imaginary numbers.
    */
   public MatInt16(String name, int[] dim, boolean imagf)
   {
      super(name, dim);
      int size = 1;
      for (int i = 0; i < dim.length; i++)
      {
         size *= dim[i];
      }
      data = new short[size];
      if (imagf)
      {
         imag = new short[size];
      }
   }

   /**
    * Returns a Short.  This is the most natural call for this function.
    * @param Index the index as an array of ints
    * @return Returns a Short at the specified index.
    */
   public int getInt(int... index)
   {
      return getShort(index);
   }

   public double getDouble(int... index)
   {
      return getShort(index);
   }

   public short getShort(int[] index)
   {
      int i = getIndex(index);
      return data[i];
   }

   /**
    * Get the imaginary component at that index.
    * @param index The index as an array of ints.
    * @return Returns the imaginary component at the specified index.
    */
   public short getImag(int[] index)
   {
      int i = getIndex(index);
      return imag[i];
   }

   /**
    * Whether or not it contains imaginary numbers.
    * @return true if this object contains imaginary numbers.
    */
   public boolean containsImaginary()
   {
      return (imag != null);
   }

   /**
    * Get the imaginary component at the location.  The value that is
    * returned is a Short.
    * @param loc The index.
    * @return The imaginary component at the specified index.
    */
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
    * Returns a Short with the value stored in index.
    * @param index the index of the value
    * @return a Short with the value.
    */
   public Short getData(int... index)
   {
      return getNumber(index);
   }

   /**
    * Returns a Short with the value stored at the corresponding index.
    * @param index the index of the value
    * @return a Short with the value.
    */
   public Short getNumber(int... index)
   {
      Short answer = new Short(getShort(index));
      return answer;
   }

   /**
    * Everything in a MatInt16 can be returned as a number and none
    * can be returned as a MatVar.
    * @param index not used
    * @return true
    */
   public boolean isNumber(int... index)
   {
      return true;
   }

   /**
    * Everything in a MatInt16 can be returned as a number and none
    * can be returned as a MatVar.
    * @param index not used
    * @return nothing
    * @throws UnsupportedOperationException always.
    */
   public MatVar getMatVar(int... index)
   {
      throw new java.lang.UnsupportedOperationException();
   }

   public int type()
   {
      return INT16;
   }

   public void putAll(short[] values)
   {
      this.data = values;
   }

   public void putAllImage(short[] values)
   {
      this.imag = values;
   }

   /**
    * Put a short at the corresponding index.
    * @param index The index where the short goes.
    * @param value The value at that index.
    */
   public void putValue(int[] index, short value)
   {
      int i = getIndex(index);
      data[i] = value;
   }

   /**
    * Put an imaginary value at the corresponding index.  If imaginary numbers
    * aren't supported, a NullPointerException is thrown.
    * @param index The index where the imaginary number goes.
    * @param value The value to place.
    * @throws NullPointerException if imaginaries aren't supported.
    */
   public void putImagValue(int[] index, short value)
   {
      int i = getIndex(index);
      imag[i] = value;
   }

   protected void setDouble(int index, double value)
   {
      data[index] = (short) value;
   }

   protected void setLong(int index, long value)
   {
      data[index] = (short) value;
   }

   protected void setDoubleImag(int index, double value)
   {
      imag[index] = (short) value;
   }

   protected void setLongImag(int index, long value)
   {
      imag[index] = (short) value;
   }

}