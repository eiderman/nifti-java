package edu.washington.biostr.sig.volume.surface;

import javax.vecmath.Point3f;


/**
 * Perform a marching tetrahedron.  This results in a smoother, but much larger
 * and more redundant surface than marching cubes.
 * @author Eider Moore
 * @version 1
 */
public class MarchingTetrahedron extends MarchingIsosurface
{
   public void doCube(
         Point3f p0, double v0, 
         int id0, Point3f p1,
         double v1, int id1,
         Point3f p2, double v2, 
         int id2, Point3f p3, 
         double v3, int id3, 
         Point3f p4, double v4, 
         int id4, Point3f p5,
         double v5, int id5, Point3f p6, double v6, int id6, Point3f p7, double v7, int id7, double isoLow, double isoHigh)
   {      
      doTet(p0, v0, p5, v5, p1, v1, p6, v6, isoLow, isoHigh, id0, id5, id1, id6);
      doTet(p0, v0, p1, v1, p2, v2, p6, v6, isoLow, isoHigh, id0, id1, id2, id6);
      doTet(p0, v0, p2, v2, p3, v3, p6, v6, isoLow, isoHigh, id0, id2, id3, id6);
      doTet(p0, v0, p3, v3, p7, v7, p6, v6, isoLow, isoHigh, id0, id3, id7, id6);
      doTet(p0, v0, p7, v7, p4, v4, p6, v6, isoLow, isoHigh, id0, id7, id4, id6);
      doTet(p0, v0, p4, v4, p5, v5, p6, v6, isoLow, isoHigh, id0, id4, id5, id6);
   }
   
   /**
    * Process the tetrahedron and add the data to points and faces.
    * @param p0
    * @param v0
    * @param p1
    * @param v1
    * @param p2
    * @param v2
    * @param p3
    * @param v3
    * @param id0 TODO
    * @param id1 TODO
    * @param id2 TODO
    * @param iso
    * @param points
    * @param faces
    */
   private void doTet(
         Point3f p0, double v0, 
         Point3f p1, double v1,
         Point3f p2, double v2,
         Point3f p3, double v3, 
         double isoLow, double isoHigh, int id0, int id1, int id2, int id3)
   {
      int triindex = 0;
      if (v0 < isoLow || v0 > isoHigh)
         triindex |= 1;
      if (v1 < isoLow || v1 > isoHigh) 
         triindex |= 2;
      if (v2 < isoLow || v2 > isoHigh)
         triindex |= 4;
      if (v3 < isoLow || v3 > isoHigh) 
         triindex |= 8;
      
      // we can have the front or back and need to reverse some.
      boolean front = false;

      Point3f tri0;
      Point3f tri1;
      Point3f tri2;
      Point3f tri3;
      int mid0, mid1, mid2, mid3;
      switch (triindex) {
         case 0x00:
         case 0x0F:
            break;
//          1       { 0,  3,  2, -1, -1, -1, -1},
//          14      { 2,  3,  0, -1, -1, -1, -1},                
         case 0x0E:
            front = !front;
         case 0x01:
            tri0 = interp(p0, v0, p1, v1, isoLow);
            tri1 = interp(p0, v0, p3, v3, isoLow);
            tri2 = interp(p0, v0, p2, v2, isoLow);
            mid0 = getId(id0, id1);
            mid1 = getId(id0, id3);
            mid2 = getId(id0, id2);
            if (front)
            {
               addTriangle(tri0, tri1, tri2, mid0, mid1, mid2);
            }
            else
            {
               addTriangle(tri2, tri1, tri0, mid2, mid1, mid0);
            }
            break;
//          2       { 0,  1,  4, -1, -1, -1, -1},
//          13      { 4,  1,  0, -1, -1, -1, -1},
         case 0x0D:
            front = !front;
         case 0x02:
            tri0 = interp(p0, v0, p1, v1, isoLow);
            tri1 = interp(p1, v1, p2, v2, isoLow);
            tri2 = interp(p1, v1, p3, v3, isoLow);
            mid0 = getId(id0, id1);
            mid1 = getId(id1, id2);
            mid2 = getId(id1, id3);
            if (front)
            {
               addTriangle(tri0, tri1, tri2, mid0, mid1, mid2);
            }
            else
            {
               addTriangle(tri2,tri1,tri0, mid2, mid1, mid0);               
            }
            break;
//          3       { 1,  4,  2,  2,  4,  3, -1},
//          12      { 3,  4,  2,  2,  4,  1, -1},
         case 0x0C:
            front = !front;
         case 0x03:
            tri0 = interp(p1, v1, p2, v2, isoLow);
            tri1 = interp(p1, v1, p3, v3, isoLow);
            tri2 = interp(p0, v0, p2, v2, isoLow);
            tri3 = interp(p0, v0, p3, v3, isoLow);
            mid0 = getId(id1, id2);
            mid1 = getId(id1, id3);
            mid2 = getId(id0, id2);
            mid3 = getId(id0, id3);
            if (front)
            {
               // face 1
               addTriangle(tri0,tri1,tri2, mid0, mid1, mid2);
               // face 2
               addTriangle(tri2, tri1, tri3, mid2, mid1, mid3);
            }
            else
            {
               // face 1
               addTriangle(tri2,tri1,tri0, mid2, mid1, mid0);
               // face 2
               addTriangle(tri3,tri1,tri2, mid3, mid1, mid2);               
            }
            break;
//          4       { 1,  2,  5, -1, -1, -1, -1},
//          11      { 5,  2,  1, -1, -1, -1, -1},
         case 0x0B:
            front = !front;
         case 0x04:
            tri0 = interp(p1, v1, p2, v2, isoLow);
            tri1 = interp(p0, v0, p2, v2, isoLow);
            tri2 = interp(p2, v2, p3, v3, isoLow);
            mid0 = getId(id1, id2);
            mid1 = getId(id0, id2);
            mid2 = getId(id2, id3);
            if (front)
            {
               addTriangle(tri0,tri1,tri2, mid0, mid1, mid2);
            }
            else
            {
               addTriangle(tri2,tri1,tri0, mid2, mid1, mid0);               
            }
            break;
//          5       { 0,  3,  5,  0,  5,  1, -1},
//          10      { 1,  5,  0,  5,  3,  0, -1},
         case 0x0A:
            front = !front;
         case 0x05:            
            tri0 = interp(p0, v0, p1, v1, isoLow);
            tri1 = interp(p0, v0, p3, v3, isoLow);
            tri2 = interp(p2, v2, p3, v3, isoLow);
            tri3 = interp(p1, v1, p2, v2, isoLow);
            mid0 = getId(id0, id1);
            mid1 = getId(id0, id3);
            mid2 = getId(id2, id3);
            mid3 = getId(id1, id2);
            if (front)
            {
               // face 1
               addTriangle(tri0,tri1,tri2, mid0, mid1, mid2);
               // face 2
               addTriangle(tri0,tri2,tri3, mid0, mid2, mid3);
            }
            else
            {
               // face 1
               addTriangle(tri2,tri1,tri0, mid2, mid1, mid0);
               // face 2
               addTriangle(tri3,tri2,tri0, mid3, mid2, mid0);               
            }
            break;
//          6       { 0,  2,  5,  0,  5,  4, -1},
//          9       { 4,  5,  0,  5,  2,  0, -1},
         case 0x09:
            front = !front;
         case 0x06:
            tri0 = interp(p0, v0, p1, v1, isoLow);
            tri1 = interp(p0, v0, p2, v2, isoLow);
            tri2 = interp(p2, v2, p3, v3, isoLow);
            tri3 = interp(p1, v1, p3, v3, isoLow);
            mid0 = getId(id0, id1);
            mid1 = getId(id0, id2);
            mid2 = getId(id2, id3);
            mid3 = getId(id1, id3);
            if (front)
            {
               // face 1
               addTriangle(tri0,tri1,tri2, mid0, mid1, mid2);
               // face 2
               addTriangle(tri0,tri2,tri3, mid0, mid2, mid3);
            }
            else
            {
               // face 1
               addTriangle(tri2,tri1,tri0, mid2, mid1, mid0);
               // face 2
               addTriangle(tri3,tri2,tri0, mid3, mid2, mid0);
            }
            break;
//          7       { 5,  4,  3, -1, -1, -1, -1},
//          8       { 3,  4,  5, -1, -1, -1, -1},
         case 0x08:
            front = !front;
         case 0x07:
            tri0 = interp(p2, v2, p3, v3, isoLow);
            tri1 = interp(p1, v1, p3, v3, isoLow);
            tri2 = interp(p0, v0, p3, v3, isoLow);
            mid0 = getId(id2, id3);
            mid1 = getId(id1, id3);
            mid2 = getId(id0, id3);
            //add the face and increment
            if (front)
            {
               addTriangle(tri0,tri1,tri2, mid0, mid1, mid2);
            }
            else
            {
               addTriangle(tri2,tri1,tri0, mid2, mid1, mid0);
            }
            break;
      }
   }
}
