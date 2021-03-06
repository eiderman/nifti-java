/*
 * Java NIFTI/SPM Library
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
package edu.washington.biostr.sig.nifti;

/**
 * This contains various static utility functions that are useful for nifti files.
 * @author eider
 */
public class Util
{
   /**
    * Turn an unsigned byte[] into a signed short[].  This is necessary because
    * Java doesn't really support unsigned bytes, ints or longs, but some file
    * formats do.  When this returns, the file type will by ImageData.TYPE_SHORT
    * @param array
    * @return
    */
   public static short[] sign(byte[] unsigned)
   {
      short[] signed = new short[unsigned.length];
      for (int i = 0; i < unsigned.length; i++)
      {
         signed[i] = (short) (unsigned[i] & 0xFF);
      }
      return signed;
   }

   public static char[] sign(short[] unsigned)
   {
      char[] signed = new char[unsigned.length];
      for (int i = 0; i < unsigned.length; i++)
      {
         signed[i] = (char) (unsigned[i] & 0xFFFF);
      }
      return signed;
   }

   /**
    * Turn an unsigned int[] into a signed long[].  This is necessary because
    * Java doesn't really support unsigned bytes, ints or longs, but some file
    * formats do.  When this returns, the file type will by ImageData.TYPE_LONG
    * @param array
    * @return
    */
   public static long[] sign(int[] unsigned)
   {
      long[] signed = new long[unsigned.length];
      for (int i = 0; i < unsigned.length; i++)
      {
         signed[i] = unsigned[i] & 0xffffffffL;
      }
      return signed;
   }

   /**
        * Turn an unsigned long[] into a signed double[].  This is necessary because
    * Java doesn't really support unsigned bytes, ints or longs, but some file
    * formats do.  We will lose some of the details that are provided, but java
    * doesn't have a native integer format that is longer than long.  The only
    * other option really is to use an Object like BigInteger.  However, this
    * would cause a lot of overhead.  If you need all of the information in a
    * long, do a custom BigInteger.  It may require writing your own ImageData
    * class.  When this returns, the file type will by ImageData.TYPE_DOUBLE
    * @see ImageData
    * @param unsigned The unsigned array of integers
    * @return Java has no primitive that works, so use a double to sign it.
    */
   public static double[] sign(long[] unsigned)
   {
      double[] signed = new double[unsigned.length];
      for (int i = 0; i < unsigned.length; i++)
      {
         if (unsigned[i] < 0)
         {
            signed[i] = unsigned[i] + 18446744073709551616.0;
         }
         else
         {
            signed[i] = unsigned[i];
         }
      }
      return signed;
   }

   /**
    * Take a byte[], short[], int[] or long[] and call the appropriate
    * sign function.  Return the next type up.  As a convenience, if char[] is
    * the input, it just returns that array as it is the only unsigned type.
    * @throws IllegalArgumentException if the type is not a 1d array of integer primitives.
    * @param unsigned
    * @return
    */
   public static Object sign(Object unsigned)
   {
      if (unsigned instanceof char[])
      {
         return unsigned;
      }
      else if (unsigned instanceof byte[])
      {
         return sign( (byte[]) unsigned);
      }
      else if (unsigned instanceof short[])
      {
         return sign( (short[]) unsigned);
      }
      else if (unsigned instanceof int[])
      {
         return sign( (int[]) unsigned);
      }
      else if (unsigned instanceof long[])
      {
         return sign( (long[]) unsigned);
      }
      else
      {
         throw new IllegalArgumentException(
             "not a 1d array of an integer primitive");
      }
   }  
}
