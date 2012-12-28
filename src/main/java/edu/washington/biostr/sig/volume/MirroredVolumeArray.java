package edu.washington.biostr.sig.volume;


/**
 * Support mirroring a given IndexedVolumeArray around the x data.  This is
 * useful for datasets that only contain half an image to save space and where 
 * the other half is a perfect mirror image (usually only the case in an atlas).
 * @author Eider Moore
 * @version 1.1
 */
public class MirroredVolumeArray extends IndexedVolumeArray
{
   final IndexedVolumeArray myva;
   int xx;
   int xx2;
   int mid;
   
   public MirroredVolumeArray(IndexedVolumeArray va)
   {
      super(va.getMaxX() * 2, va.getMaxY(), va.getMaxZ(), va.getMaxTime(), va.getMaxI5(), va.getIndex2Space());
      myva = va;
      xx = va.getMaxX();
      xx2 = xx * 2;
      mid = va.getMaxTime() * va.getMaxZ() * va.getMaxY() * va.getMaxX() / 2;
      if (myva == null)
         throw new IllegalArgumentException("VA Cannot be null! " + va);
      imageMax = va.getImageMax();
      imageMin = va.getImageMin();
   }

   private int myIndex(int index) {
      int mod = index % xx2;
      index = (index - mod) / 2;
      if (mod < xx) {
         return index + mod;
      } else {
         mod = xx2 - mod - 1;
         return index + mod;
      }
   }
   
   @Override
   public double getDouble(int index)
   {
      return myva.getDouble(myIndex(index));
   }

   @Override
   public int getInt(int index)
   {
      return myva.getInt(myIndex(index));
   }

   @Override
   public void setData(int index, double value)
   {
      myva.setData(myIndex(index), value);
   }


   @Override
   public void setData(int index, int value)
   {
      myva.setData(myIndex(index), value);
   }

   @Override
   public DataType getNaturalType()
   {
      return myva.getNaturalType();
   }

   @Override
   public DataType getType()
   {
      return myva.getType();
   }
   
   
   @Override
   public Object getDataArray() {
   	return myva.getDataArray();
   }

   public VolumeArray getBackingArray() {
	   return myva;
   }
   
}
