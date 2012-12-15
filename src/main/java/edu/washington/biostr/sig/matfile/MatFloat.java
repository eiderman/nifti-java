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
 * <p> This is a MatVar that specifically is designed to store floats.  It
 * can only hold floats and floats with imaginary components.
 * </p>
 * @author Eider Moore
 * @version 1.0
 */
public class MatFloat
    extends MatVar
{
   private float[] data;
   private float[] imag;

   public MatFloat(String name, int[] dim, int imagf)
   {
      this(name, dim, imagf != 0);
   }

   /**
    * Create a new Matfloat.
    * @param name The name.
    * @param dim The dimensions.
    * @param imagf Whether or not to have imaginary numbers, if 0 we have no
    * imaginary numbers, otherwise we have imaginary numbers.
    */
   public MatFloat(String name, int[] dim, boolean imagf)
   {
      super(name, dim);
      int size = 1;
      for (int i = 0; i < dim.length; i++)
      {
         size *= dim[i];
      }
      data = new float[size];
      if (imagf)
      {
         imag = new float[size];
      }
   }

   /**
    * returns a float.  This is the most natural call for this function.
    * @param index the index as an array of ints
    * @return returns a float at the specified index.
    */
   public float getFloat(int[] index)
   {
      int i = getIndex(index);
      return data[i];
   }

   public double getDouble(int... i)
   {
      return getFloat(i);
   }

   public int getInt(int... i)
   {
      return (int) getFloat(i);
   }

   /**
    * Get the imaginary component at that index.
    * @param index The index as an array of ints.
    * @return Returns the imaginary component at the specified index.
    */
   public float getImag(int[] index)
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
    * returned is a Float.
    * @param loc The index.
    * @return The imaginary component at the specified index.
    */
   public Float getImaginary(int... loc)
   {
      if (containsImaginary())
      {
         return new Float(getImag(loc));
      }
      else
      {
         return new Float(0f);
      }
   }

   /**
    * Returns a Float with the value stored in index.
    * @param index the index of the value
    * @return a Float with the value.
    */
   public Float getData(int... index)
   {
      return getNumber(index);
   }

   /**
    * Returns a Float with the value stored in index.
    * @param index the index of the value
    * @return a Float with the value.
    */
   public Float getNumber(int... index)
   {
      Float answer = new Float(getFloat(index));
      return answer;
   }

   /**
    * Everything in a MatFloat can be returned as a number and none
    * can be returned as a MatVar.
    * @param index not used
    * @return true
    */
   public boolean isNumber(int... index)
   {
      return true;
   }

   /**
    * Everything in a MatFloat can be returned as a number and none
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
      return SINGLE;
   }

   public void putAll(float[] values)
   {
      this.data = values;
   }

   public void putAllImage(float[] values)
   {
      this.imag = values;
   }

   /**
    * Put a float at the corresponding index.
    * @param index The index where the float goes.
    * @param value The value at that index.
    */
   public void putValue(int[] index, float value)
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
   public void putImagValue(int[] index, float value)
   {
      int i = getIndex(index);
      imag[i] = value;
   }

   protected void setDouble(int index, double value)
   {
      data[index] = (float) value;
   }

   protected void setLong(int index, long value)
   {
      data[index] = value;
   }

   protected void setDoubleImag(int index, double value)
   {
      imag[index] = (float) value;
   }

   protected void setLongImag(int index, long value)
   {
      imag[index] = value;
   }
}