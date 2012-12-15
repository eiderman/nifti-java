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

public class MatText
    extends MatVar
{
   private char[] data;

   public MatText(String name, int[] dim)
   {
      super(name, dim);
      int size = 1;
      for (int i = 0; i < dim.length; i++)
      {
         size *= dim[i];
      }
      data = new char[size];
   }

   /**
    * returns a double.  This is the most natural call for this function.
    * @param index the index as an array of ints
    * @return returns a double at the specified index.
    */
   public char getChar(int[] index)
   {
      int i = getIndex(index);
      return data[i];
   }

   /**
    * Returns a Double with the value stored in index.
    * @param index the index of the value
    * @return a Double with the value.
    */
   public Character getData(int... index)
   {
      Character answer = new Character(getChar(index));
      return answer;
   }

   /**
    * Returns a Short with the value stored in index.
    * @param index the index of the value
    * @return a Double with the value.
    */
   public Short getNumber(int... index)
   {
      Short answer = new Short( (short) getChar(index));
      return answer;
   }

   /**
    * Everything in a MatChar can be returned as a number and none
    * can be returned as a MatVar.
    * @param index not used
    * @return trues
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
      return TEXT;
   }

   public void putValue(int[] index, double value)
   {
      int i = getIndex(index);
      char c = (char) (byte) value;
      data[i] = c;
   }

   protected void setDouble(int index, double value)
   {
      // not valid for this type
   }

   protected void setLong(int index, long value)
   {
      // not valid for this type
   }

   protected void setDoubleImag(int index, double value)
   {
      // not valid for this type
   }

   protected void setLongImag(int index, long value)
   {
      // not valid for this type
   }
}