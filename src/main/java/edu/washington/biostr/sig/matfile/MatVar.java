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
 * <p>Title: Matfile Library</p>
 * <p>Description: Displays and allows manipulation of 3d brain models</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Structural Informatics Group</p>
 * <p>No math is included with the MatVar.  Some simple math is provided in 
 * the class MatMath.</p>
 * <p>The internal structures are 1d arrays of a single data type.
 * These arrays can then be accessed as if they are of any dimensionality
 * using the appropriate methods.  The dimensions can be accessed via the
 * dim array.</p>
 * <b>Note</b>
 * To make this easier to use, most index methods use varargs.  These will work
 * just fine for most light work.  However for large iterations you will probably 
 * benefit by creating and reusing an int[] as it will reduce object creation.
 * <p>
 * Not all of the functions are needed to be supported by all of the
 * sub classes, just those that can be done.</p>
 * @author Eider Moore
 * @version 1.0
 */

public abstract class MatVar
{
   // These are all duplicated here and in the native file for the
   // NativeMatFileLoader, if you change any values here, make sure to
   // change them there.
   public static final int INT8 = 1;
   public static final int UINT8 = 2;
   public static final int INT16 = 3;
   public static final int UINT16 = 4;
   public static final int INT32 = 5;
   public static final int UINT32 = 6;
   public static final int SINGLE = 7;
   public static final int DOUBLE = 9;
   public static final int INT64 = 12;
   public static final int UINT64 = 13;
   public static final int MATRIX = 14;
   public static final int TEXT = 15;
   public static final int BOOLEAN = 30;

   /**
    * Every matlab variable has a name.
    */
   protected String name;
   /**
    * They also all have dimensions.  These are kept as an array where the
    * dimensions are dim[0]xdim[1]...
    */
   protected int[] dim;

   /**
    * Create a matlab variable with a certain name and dimensions.
    * @param name The name of the variable.
    * @param dim The array that defines the dimensions
    */
   public MatVar(String name, int[] dim)
   {
      this.name = name;
      this.dim = dim;
   }

   /**
    * Get the type of this array.  This returns an int that is one of those
    * listed above.
    * @return an int representing the type of this MatVar
    */
   public abstract int type();

   /**
    * Some arrays contain an imaginary component.  By default we assume they
    * don't but this can be overidden by implementing classes to reflect
    * their situations.
    * @return Whether or not an imaginary component exists.
    */
   public boolean containsImaginary()
   {
      return false;
   }

   /**
    * Get the imaginary component corresponding to the given location.
    * On arrays that do not have an imaginary component, this throws
    * an UnsupportedOperationException.  By default this exception is always
    * thrown.
    * @param loc The location of the specified component.
        * @return a Number corresponding to the imaginary component at the location.
    * @throws UnsupportedOperationException if the type does not support
    * imaginary numbers.
    */
   public Number getImaginary(int... loc)
   {
      throw new java.lang.UnsupportedOperationException();
   }

   /**
    * Get the data at the specified location.
    * @param loc The location of interest.
    * @return An Object representing the data.
    */
   public abstract Object getData(int... loc);

   /**
    * Get an object of type number at the specified location.  Not all types
    * support numbers, those that don't at all may throw
    * UnsupportedOperationException.  Other subclasses may not have a number
    * at the specified location, in which case a class cast exception will
    * be thrown.  Use isNumber() to check if a location contains a number.
    * A location either contains a number or another MatVar.
    * @param loc the location to inspect
    * @return the number object at that location.
    * @throws UnsupportedOperationException if numbers are not supported by
    * this data type.
    */
   public abstract Number getNumber(int... loc);

   /**
    * Get an object of type MatVar at the specified location.  Not all types
    * support MatVar's, those that don't at all may throw
    * UnsupportedOperationException.  Other subclasses may not have a MatVar
    * at the specified location, in which case a class cast exception will
    * be thrown.  Use isNumber() to check if a location contains a MatVar.
    * A location either contains a number or another MatVar.
    * @param loc the location to inspect
    * @return the MatVar object at that location.the type does not support
    * other matvars.
    * @throws UnsupportedOperationException if
    */
   public abstract MatVar getMatVar(int... loc);

   /**
    * returns true if the location contains a number.  returns false if the
    * location contains a matvar.
    * @param loc the location to inspect
    * @return true if the location contains a number.
    */
   public abstract boolean isNumber(int... loc);

   /**
    * Get the dimensions in each directions of the MatVar.  The length is the
    * dimensionality and each component is the number of entries in that
    * direction.
    * @return The dimensions of the object.
    */
   public int[] getDim()
   {
      return dim;
   }

   public double getDouble(int... loc)
   {
      return getNumber(loc).doubleValue();
   }

   public int getInt(int... loc)
   {
      return getNumber(loc).intValue();
   }

   /**
    * Get the name of the variable.
    * @return The name of the variable.
    */
   public String getName()
   {
      return name;
   }

   /**
    * Just return the name of the variable.
    * @return The name.
    */
   public String toString()
   {
      return name;
   }

   /**
    * Convert an int[] index into the actual index into the 1d array.
    * @param index The index in terms of an array.
    * @return The index into the corresponding 1d array.
    */
   protected final int getIndex(int[] index)
   {
      int i = 0;
      for (int j = index.length - 1; j > 0; j--)
      {
         i = (i + index[j]) * dim[j - 1];
      }
      i += index[0];
      return i;
   }

   /**
    * This is designed for setting floating point values.  It is mostly used
    * to keep Matfile5Loader simple.
    * @param index
    * @param value
    */
   protected abstract void setDouble(int index, double value);

   /**
    * This is designed for setting integer point values.  It is mostly used
    * to keep Matfile5Loader simple.  It takes a long because a long is
    * guaranteed for all data types to contain at least the minimum amound of
    * data.  However, if the data type is shorter (which is very likely),
    * it will be cast to that datatype.
    * @param index
    * @param value
    */
   protected abstract void setLong(int index, long value);

   /**
    * This is designed for setting floating point values.  It is mostly used
    * to keep Matfile5Loader simple.
    * @param index
    * @param value
    */
   protected abstract void setDoubleImag(int index, double value);

   /**
    * This is designed for setting integer point values.  It is mostly used
    * to keep Matfile5Loader simple.  It takes a long because a long is
    * guaranteed for all data types to contain at least the minimum amound of
    * data.  However, if the data type is shorter (which is very likely),
    * it will be cast to that datatype.
    * @param index
    * @param value
    */
   protected abstract void setLongImag(int index, long value);

}