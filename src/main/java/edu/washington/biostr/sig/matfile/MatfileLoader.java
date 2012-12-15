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

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;

import org.eiderman.util.Compression;
import org.eiderman.util.FileUtilities;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class MatfileLoader
{
   protected HashMap<String, MatVar> vars;
   protected MatfileLoader loader;

   protected MatfileLoader()
   {
   }

   public MatfileLoader(URL file) throws IOException
   {
      vars = new HashMap<String, MatVar>();
      URLConnection connection = file.openConnection();
      ByteBuffer bBuf = FileUtilities.getBuffer(connection.getInputStream(),
            connection.getContentLength(), EnumSet.allOf(Compression.class));

      checkMatfileLevel(bBuf);
   }

   private void checkMatfileLevel(ByteBuffer bBuf)
   {
      boolean v4 = false;
      for (int i = 0; i < 4; i++)
      {
         byte b = bBuf.get();
         if (b == 0)
         {
            v4 = true;
         }
      }

      bBuf.rewind();
      if (v4)
      {
         loader = new Matfile4Loader(bBuf);
      }
      else
      {
         loader = new Matfile5Loader(bBuf);
      }

   }

   /**
    * If you want to rely on calling get(String) then you have to fill the data
    * by calling fillVariables.
    */
   public void fillVariables()
   {
      for (MatVar var = getNext(); var != null; var = getNext())
      {
      }
   }

   /**
    * Get the next variable or null if there are no more variables.
    * @return
    */
   public final MatVar getNext()
   {
      MatVar var = loadNext();
      if (var != null)
      {
         String name = var.getName();
         vars.put(name, var);
      }
      return var;
   }

   protected MatVar loadNext()
   {
      return loader.loadNext();
   }

   public final MatVar get(String name)
   {
      MatVar v;
      while ((v = vars.get(name)) == null)
      {
         if (getNext() == null)
            return null;
      }
      return v;
   }

   public final String[] getNames()
   {
      String[] names = new String[vars.size()];
      Iterator it = vars.keySet().iterator();
      int i = 0;
      while (it.hasNext())
      {
         names[i] = (String) it.next();
         i++;
      }
      return names;
   }
}