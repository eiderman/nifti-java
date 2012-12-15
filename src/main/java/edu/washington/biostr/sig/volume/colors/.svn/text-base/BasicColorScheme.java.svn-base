package edu.washington.biostr.sig.volume.colors;

import java.awt.Color;

/**
 * Generate a color scheme from black to the given color.
 * @author Eider Moore
 * @version 1.0
 */

public class BasicColorScheme
    extends ColorScheme
{
   Color baseColor;
   String name;

   /**
    * Make a grayscale scheme.
    */
   public BasicColorScheme()
   {
      baseColor = Color.WHITE;
   }

   /**
    * Make a scheme with the final color of baseColor
    * @param baseColor the final color.
    */
   public BasicColorScheme(Color baseColor)
   {
      setAlpha(1);
      this.baseColor = baseColor;
   }

   /**
    * Make a scheme with the final color of baseColor
    * @param baseColor the final color.
    * @param name The name for this color scheme (returned by toString)
    */
   public BasicColorScheme(Color baseColor, String name)
   {
      setAlpha(1);
      this.name = name;
      this.baseColor = baseColor;
   }

   /**
    * Make a grayscale scheme with the given level of opacity.
    * @param alpha the level of opacity (between 0 and 1)
    */
   public BasicColorScheme(double alpha)
   {
      setAlpha(alpha);
      baseColor = Color.WHITE;
   }

   /**
    * Make a scheme with the final color of baseColor
    * @param baseColor the final color.
    * @param name The name for toString
    * @param alpha The level of opacity.
    */
   public BasicColorScheme(Color baseColor, String name, double alpha)
   {
      setAlpha(alpha);
      this.name = name;
      this.baseColor = baseColor;
   }

   /**
    * Make a scheme with the final color of baseColor
    * @param baseColor the final color.
    * @param alpha The level of opacity.
    */
   public BasicColorScheme(Color baseColor, double alpha)
   {
      setAlpha(alpha);
      this.baseColor = baseColor;
   }

   public int[] getRGB(double value)
   {
      value = Math.abs(value);
      int[] rgb = new int[4];
      rgb[0] = (int) Math.round(value * baseColor.getRed());
      rgb[1] = (int) Math.round(value * baseColor.getGreen());
      rgb[2] = (int) Math.round(value * baseColor.getBlue());

      if (value > 0)
      {
         rgb[3] = alpha;
      }
      else
      {
         rgb[3] = 0;
      }
      return rgb;
   }

   public String toString()
   {
      if (name != null)
      {
         return name;
      }
      else
      {

         return "Base Color: " +
             "(" + baseColor.getRed() +
             ", " + baseColor.getGreen() +
             ", " + baseColor.getBlue() + ")";
      }
   }

   public boolean equals(Object parm1)
   {
      if (parm1 instanceof BasicColorScheme)
      {
         BasicColorScheme other = (BasicColorScheme) parm1;
         return (other.alpha == alpha) && (other.baseColor.equals(baseColor));
      }
      return false;
   }

   public int hashCode()
   {
      int code = 17;
      code = baseColor.hashCode() + code * 37;
      code = code + 37 * (int) Double.doubleToLongBits(alpha);
      return code;
   }
}