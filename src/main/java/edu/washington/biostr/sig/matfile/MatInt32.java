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
 * <p> This is a MatVar that specifically is designed to store Int32s.  It
 * can only hold integers and integers with imaginary components.
 * </p>
 * @author Eider Moore
 * @version 1.0
 */
public class MatInt32
    extends MatVar
{
   private int[] data;
   private int[] imag;

   public MatInt32(String name, int[] dim, int imagf)
   {
      this(name, dim, imagf != 0);
   }

   /**
    * Create a new MatInt32.
    * @param name The name.
    * @param dim The dimensions.
    * @param imagf Whether or not to have imaginary numbers, if 0 we have no
    * imaginary numbers, otherwise we have imaginary numbers.
    */
   public MatInt32(String name, int[] dim, boolean imagf)
   {
      super(name, dim);
      int size = 1;
      for (int i = 0; i < dim.length; i++)
      {
         size *= dim[i];
      }
      data = new int[size];
      if (imagf)
      {
         imag = new int[size];
      }
   }

   /**
    * Returns a integer.  This is the most natural call for this function.
    * @param Index the index as an array of ints
    * @return Returns a integer at the specified index.
    */
   public int getInt(int... index)
   {
      int i = getIndex(index);
      return data[i];
   }

   public double getDouble(int... i)
   {
      return getInt(i);
   }

   /**
    * Get the imaginary component at that index.
    * @param index The index as an array of ints.
    * @return Returns the imaginary component at the specified index.
    */
   public int getImag(int[] index)
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
    * returned is a integer.
    * @param loc The index.
    * @return The imaginary component at the specified index.
    */
   public Integer getImaginary(int... loc)
   {
      if (containsImaginary())
      {
         return new Integer(getImag(loc));
      }
      else
      {
         return new Integer(0);
      }
   }

   /**
    * Returns a integer with the value stored in index.
    * @param index the index of the value
    * @return a integer with the value.
    */
   public Integer getData(int... index)
   {
      return getNumber(index);
   }

   /**
    * Returns a integer with the value stored at the corresponding index.
    * @param index the index of the value
    * @return a integer with the value.
    */
   public Integer getNumber(int... index)
   {
      Integer answer = new Integer(getInt(index));
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

   /**
    * Everything in a MatInt32 can be returned as a number and none
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
      return INT32;
   }

   public void putAll(int[] values)
   {
      this.data = values;
   }

   public void putAllImage(int[] values)
   {
      this.imag = values;
   }

   /**
    * Put a integer at the corresponding index.
    * @param index The index where the integer goes.
    * @param value The value at that index.
    */
   public void putValue(int[] index, int value)
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
   public void putImagValue(int[] index, int value)
   {
      int i = getIndex(index);
      imag[i] = value;
   }

   protected void setDouble(int index, double value)
   {
      data[index] = (int) value;
   }

   protected void setLong(int index, long value)
   {
      data[index] = (int) value;
   }

   protected void setDoubleImag(int index, double value)
   {
      imag[index] = (int) value;
   }

   protected void setLongImag(int index, long value)
   {
      imag[index] = (int) value;
   }

}