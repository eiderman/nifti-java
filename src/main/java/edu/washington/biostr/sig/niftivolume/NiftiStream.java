package edu.washington.biostr.sig.niftivolume;

import java.io.IOException;

import edu.washington.biostr.sig.nifti.AnalyzeNiftiSpmHeader;
import edu.washington.biostr.sig.volume.ByteEncoder;

/**
 * This takes a header and is used to write out a Nifti file piecemeal.
 * It can only be used in order and automatically handles data conversions.
 * @author eider
 */
public class NiftiStream
{
   AnalyzeNiftiSpmHeader header;
   
   ByteEncoder out;

   
   public NiftiStream(AnalyzeNiftiSpmHeader header2, ByteEncoder imge)
   {
      this.header = header2;
      this.out = imge;
   }

   public void write(double value) throws IOException
   {
      switch (header.getDatatype())
      {
         case AnalyzeNiftiSpmHeader.DT_FLOAT64:
         {
            out.write(value);
            break;
         }
         case AnalyzeNiftiSpmHeader.DT_FLOAT32:
         {
            out.write((float) value);
            break;
         }
         default:
         {
            write((long) value);
            break;   
         }
      }
   }
   
   public void write(long value) throws IOException
   {
      switch (header.getDatatype())
      {
         case AnalyzeNiftiSpmHeader.DT_INT8:
            out.write((byte) value);
            break;
         case AnalyzeNiftiSpmHeader.DT_UINT8:
            out.write((byte) (value & 0xff));
            break;
         case AnalyzeNiftiSpmHeader.DT_INT16:
            out.write((short) value);
            break;
         case AnalyzeNiftiSpmHeader.DT_INT32:
            out.write((int) value);
            break;
         case AnalyzeNiftiSpmHeader.DT_INT64:
            out.write((long) value);
            break;
         case AnalyzeNiftiSpmHeader.DT_UINT16:
            out.write((char) value);
            break;
         case AnalyzeNiftiSpmHeader.DT_RGB24:
            out.write((byte) ((value) & 0xff));
            out.write((byte) ((value >> 8) & 0xff));
            out.write((byte) ((value >> 16) & 0xff));
            break;
         case AnalyzeNiftiSpmHeader.DT_FLOAT64:
         case AnalyzeNiftiSpmHeader.DT_FLOAT128:
            write((double) value);
            break;
         default:
            throw new IllegalArgumentException("Unsuppored Datatype: " + header.getDatatype());
      }
   }
   
   public void close() throws IOException
   {
      out.close();
   }
   
}
