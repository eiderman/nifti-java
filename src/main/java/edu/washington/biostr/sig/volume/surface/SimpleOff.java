package edu.washington.biostr.sig.volume.surface;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StreamTokenizer;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Provide a simple interface for reading and writing
 * off, smf and simple obj files to and from Surface objects.  The name has gradually
 * evolved to be wrong, but so it goes.
 * @author Eider Moore
 * @version 1
 */
public class SimpleOff {

    private static String readLine(InputStream in) throws IOException {
        StringBuilder s = new StringBuilder();
        int i = in.read();
        if (i < 0) {
            return null;
        }
        while (i >= 0 && i != '\n') {
            s.append((char) i);
            i = in.read();
        }
        return s.toString();
    }

    /**
     * This reads text very poorly.  It ignores scientific notation.
     * @param in
     * @return The Surface from <code>in</code>
     * @throws IOException
     */
    public static Surface readOff(InputStream in) throws IOException {
        // read header
        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        String head = readLine(in);
        if (head.matches(".{0,2}OFF.+BINARY.*")) {
            boolean readColors = head.startsWith("C");
            boolean readNormals = head.charAt(0) == 'N' || head.charAt(1) == 'N';
            int pSize = readInt(in);
            int fSize = readInt(in);
            readInt(in); // skip edges
            float[] vertices = new float[pSize * 3];
            float[] normals = readNormals ? new float[pSize * 3] : null;
            float[] colors = readColors ? new float[pSize * 3] : null;
            int[] faces = new int[fSize * 3];

            // read faces
            for (int i = 0; i < pSize; i++) {
                vertices[i * 3] = Float.intBitsToFloat(readInt(in));
                vertices[i * 3 + 1] = Float.intBitsToFloat(readInt(in));
                vertices[i * 3 + 2] = Float.intBitsToFloat(readInt(in));
                if (readNormals) {
                    normals[i * 3] = Float.intBitsToFloat(readInt(in));
                    normals[i * 3 + 1] = Float.intBitsToFloat(readInt(in));
                    normals[i * 3 + 2] = Float.intBitsToFloat(readInt(in));
                }
                if (readColors) {
                    colors[i * 3] = Float.intBitsToFloat(readInt(in));
                    colors[i * 3 + 1] = Float.intBitsToFloat(readInt(in));
                    colors[i * 3 + 2] = Float.intBitsToFloat(readInt(in));
                    readInt(in); // we currently ignore alpha
                }
            }

            for (int i = 0; i < faces.length; i += 3) {
                int len = readInt(in);
                if (len == 1) {
                    readInt(in); // skip face
                    
                    System.err.println("Skipping Face " + i + ", it is a point.");
                } else if (len != 3) {
                    throw new IOException("Face " + i + " is not a triangle, 3 != " + len);
                } else {
                    faces[i] = readInt(in);
                    faces[i + 1] = readInt(in);
                    faces[i + 2] = readInt(in);
                }
                // ignore color
                int numColors = readInt(in);
                while (numColors > 0) {
                    readInt(in);
                    numColors--;
                }
            }
            return new Surface(faces, vertices, normals, colors);
        } else if (head.matches(".{0,2}OFF.*")) {
            boolean readColors = head.startsWith("C");
            boolean readNormals = head.charAt(0) == 'N' || head.charAt(1) == 'N';
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StreamTokenizer tokenizer = new StreamTokenizer(reader);
            tokenizer.commentChar('#');
            tokenizer.eolIsSignificant(false);

            tokenizer.nextToken();
            int pSize = (int) Math.round(tokenizer.nval);
            tokenizer.nextToken();
            int fSize = (int) Math.round(tokenizer.nval);
            tokenizer.nextToken();

            System.out.println(pSize);
            System.out.println(fSize);

            float[] vertices = new float[pSize * 3];
            float[] normals = readNormals ? new float[pSize * 3] : null;
            float[] colors = readColors ? new float[pSize * 3] : null;
            int[] faces = new int[fSize * 3];

            // read faces
            for (int i = 0; i < pSize; i++) {
                tokenizer.nextToken();
                vertices[i * 3] = (float) tokenizer.nval;
                tokenizer.nextToken();
                vertices[i * 3 + 1] = (float) tokenizer.nval;
                tokenizer.nextToken();
                vertices[i * 3 + 2] = (float) tokenizer.nval;
                if (readNormals) {
                    tokenizer.nextToken();
                    normals[i * 3] = (float) tokenizer.nval;
                    tokenizer.nextToken();
                    normals[i * 3 + 1] = (float) tokenizer.nval;
                    tokenizer.nextToken();
                    normals[i * 3 + 2] = (float) tokenizer.nval;
                }
                if (readColors) {
                    tokenizer.nextToken();
                    colors[i * 3] = (float) tokenizer.nval;
                    tokenizer.nextToken();
                    colors[i * 3 + 1] = (float) tokenizer.nval;
                    tokenizer.nextToken();
                    colors[i * 3 + 2] = (float) tokenizer.nval;
                    tokenizer.nextToken(); // ignore alpha
                }
            }

            for (int i = 0; i < faces.length; i += 3) {
                tokenizer.nextToken();
                int len = (int) Math.round(tokenizer.nval);
                if (len == 1) {
                    tokenizer.nextToken();
                    System.err.println("Skipping Face " + i + ", it is a point.");
                } else if (len != 3) {
                    throw new IOException("Face " + i + " is not a triangle, 3 != " + len + " line " + tokenizer.lineno());
                } else {
                    tokenizer.nextToken();
                    faces[i] = (int) Math.round(tokenizer.nval);
                    tokenizer.nextToken();
                    faces[i + 1] = (int) Math.round(tokenizer.nval);
                    tokenizer.nextToken();
                    faces[i + 2] = (int) Math.round(tokenizer.nval);
                }
            }
            return new Surface(faces, vertices, normals, colors);
        } else {
            throw new IOException(head + " is not a binary off file!");
        }
    }

    public static void saveOff(Surface s, OutputStream o) throws IOException {
        boolean saveColor = s.colors != null;
        boolean saveNormals = s.normals != null;
        String header = "OFF BINARY\n";
        if (saveNormals) {
            header = "N" + header;
        }
        if (saveColor) {
            header = "C" + header;
        }
        o.write(header.getBytes("UTF-8"));
        writeInt(s.getPoints().length / 3, o);
        writeInt(s.getFaces().length / 3, o);
        writeInt(0, o);
//      writeInt(s.getNormals().length, o);
        //write vertices
        for (int i = 0; i < s.getPoints().length; i += 3) {
            writeInt(Float.floatToIntBits(s.getPoints()[i]), o);
            writeInt(Float.floatToIntBits(s.getPoints()[i + 1]), o);
            writeInt(Float.floatToIntBits(s.getPoints()[i + 2]), o);
            if (saveNormals) {
                writeInt(Float.floatToIntBits(s.getNormals()[i]), o);
                writeInt(Float.floatToIntBits(s.getNormals()[i + 1]), o);
                writeInt(Float.floatToIntBits(s.getNormals()[i + 2]), o);
            }
            if (saveColor) {
                writeInt(Float.floatToIntBits(s.getColors()[i]), o);
                writeInt(Float.floatToIntBits(s.getColors()[i + 1]), o);
                writeInt(Float.floatToIntBits(s.getColors()[i + 2]), o);
                writeInt(Float.floatToIntBits(1f), o);
            }
        }
        for (int i = 0; i < s.getFaces().length; i += 3) {
            writeInt(3, o);
            writeInt(s.getFaces()[i], o);
            writeInt(s.getFaces()[i + 1], o);
            writeInt(s.getFaces()[i + 2], o);
            writeInt(0, o);
        }
        o.flush();
        o.close();
    }

    static void writeInt(int i, OutputStream o) throws IOException {
        o.write(i >>> 24);
        o.write(i >>> 16);
        o.write(i >>> 8);
        o.write(i);
    }

    static int readInt(InputStream in) throws IOException {
        int i;
        i = in.read() << 24;
        i |= in.read() << 16;
        i |= in.read() << 8;
        i |= in.read();
        return i;
    }
    
    public static void saveObj(Surface s, OutputStream o) throws IOException{
    	OutputStreamWriter writer = new OutputStreamWriter(o);
    	for (int i = 0; i < s.getPoints().length; i += 3) {
    		writer.write("v ");
    		writer.write(String.format("%.4f ", s.getPoints()[i]));
    		writer.write(String.format("%.4f ", s.getPoints()[i + 1]));
    		writer.write(String.format("%.4f ", s.getPoints()[i + 2]));
    		writer.write("\n");
    	}
    	if (s.getNormals() != null) {
	    	for (int i = 0; i < s.getNormals().length; i += 3) {
	    		writer.write("vn ");
	    		writer.write(String.format("%.4f ", s.getNormals()[i]));
	    		writer.write(String.format("%.4f ", s.getNormals()[i + 1]));
	    		writer.write(String.format("%.4f ", s.getNormals()[i + 2]));
	    		writer.write("\n");
	    	}
    	}
       	for (int i = 0; i < s.getFaces().length; i += 3) {
    		writer.write("f ");
    		for (int j = 0; j < 3; j++) {
    			int face = s.getFaces()[i + j] + 1;
	    		if (s.getNormals() != null) {
		    		writer.write(String.format("%d//%d ", face, face));
	    		} else {
		    		writer.write(String.format("%d ", face));
	    		}
    		}
    		writer.write("\n");
    	}    	
    	writer.close();
    }

    /**
     * Not a real obj....
     * @param s
     * @param o
     * @throws IOException
     */
    public static void saveSphereSmoothObj(Surface s, OutputStream o) throws IOException{
    	OutputStreamWriter writer = new OutputStreamWriter(o);
       	// don't know what the header means
    	writer.write(String.format("P 0.3 0.7 0.5 100 1\n%d\n", s.getPoints().length / 3));
    	for (int i = 0; i < s.getPoints().length; i += 3) {
    		writer.write(String.format("%.4f ", s.getPoints()[i]));
    		writer.write(String.format("%.4f ", s.getPoints()[i + 1]));
    		writer.write(String.format("%.4f ", s.getPoints()[i + 2]));
    		writer.write("\n");
    	}
    	if (s.getNormals() != null) {
	    	for (int i = 0; i < s.getNormals().length; i += 3) {
	    		writer.write(String.format("%.4f ", s.getNormals()[i]));
	    		writer.write(String.format("%.4f ", s.getNormals()[i + 1]));
	    		writer.write(String.format("%.4f ", s.getNormals()[i + 2]));
	    		writer.write("\n");
	    	}
    	}
    	writer.write(String.format("%d 0 1\n", s.getFaces().length / 3));
       	for (int i = 0; i < s.getFaces().length; i+=3) {
       		writer.write(String.format("%d %d %d\n", s.getFaces()[i], s.getFaces()[i + 1], s.getFaces()[i + 2]));
    	}    	
		writer.write("\n");
    	writer.close();
    }

    public static Surface readSphereSmoothObj(InputStream in) throws IOException{
    	// read the lines
    	String line;
    	int numPoints = 0;
    	int numFaces = 0;
    	line = readLine(in); // skip the header
    	line = readLine(in);
    	numPoints = Integer.parseInt(line);
    	float[] points = new float[numPoints * 3];
    	for (int i = 0; i < numPoints; i++) {
    		do {
        		line = readLine(in).trim();    			
    		} while (line.length() == 0);
    		String[] p = line.split("\\s+");
    		points[i] = Float.parseFloat(p[0]);
    		points[i+1] = Float.parseFloat(p[1]);
    		points[i+2] = Float.parseFloat(p[2]);
    	}
    	float[] normals = new float[numPoints * 3];
    	for (int i = 0; i < numPoints; i++) {
    		do {
        		line = readLine(in).trim();    			
    		} while (line.length() == 0);
    		String[] p = line.split("\\s+");
    		normals[i] = Float.parseFloat(p[0]);
    		normals[i+1] = Float.parseFloat(p[1]);
    		normals[i+2] = Float.parseFloat(p[2]);
    	}
		do {
    		line = readLine(in).trim();    			
		} while (line.length() == 0);
    	numFaces = Integer.parseInt(line);
		int[] faces = new int[numFaces];
    	for (int i = 0; i < numFaces; i++) {
        	do {
        		line = readLine(in).trim();    			
    		} while (line.length() == 0);
        	faces[i] = Integer.parseInt(line);
    	}
    	return new Surface(faces, points, normals);
	}

    public static void saveOffText(Surface s, OutputStream o) throws IOException {
        DecimalFormat formatter = new DecimalFormat("0.########");
        OutputStreamWriter writer = new OutputStreamWriter(o);

        boolean saveColor = s.colors != null;
        boolean saveNormals = s.normals != null;
        String header = "NOFF\n";
        if (saveNormals) {
            header = "N" + header;
        }
        if (saveColor) {
            header = "C" + header;
        }

        writer.write(header);
        writer.write(s.getPoints().length / 3 + " " +
                s.getFaces().length / 3 + " 0\n");
        //write vertices
        for (int i = 0; i < s.getPoints().length; i += 3) {
            writer.write(formatter.format(s.getPoints()[i]) + " ");
            writer.write(formatter.format(s.getPoints()[i + 1]) + " ");
            writer.write(formatter.format(s.getPoints()[i + 2]) + " ");
            if (saveNormals) {
                writer.write(formatter.format(s.getNormals()[i]) + " ");
                writer.write(formatter.format(s.getNormals()[i + 1]) + " ");
                writer.write(formatter.format(s.getNormals()[i + 2]) + " ");
            }
            if (saveColor) {
                writer.write(formatter.format(s.getColors()[i]) + " ");
                writer.write(formatter.format(s.getColors()[i + 1]) + " ");
                writer.write(formatter.format(s.getColors()[i + 2]) + " ");
            }
            writer.write("\n");
        }
        for (int i = 0; i < s.getFaces().length; i += 3) {
            writer.write("3 ");
            writer.write(s.getFaces()[i] + " ");
            writer.write(s.getFaces()[i + 1] + " ");
            writer.write(s.getFaces()[i + 2] + "\n");
        }
        writer.flush();
        o.close();
    }

    public static void saveSMFText(Surface s, OutputStream o) throws IOException {
        DecimalFormat formatter = new DecimalFormat("0.########");
        OutputStreamWriter writer = new OutputStreamWriter(o);
        //write vertices
        for (int i = 0; i < s.getPoints().length; i += 3) {
            writer.write("v " + formatter.format(s.getPoints()[i]) + " ");
            writer.write(formatter.format(s.getPoints()[i + 1]) + " ");
            writer.write(formatter.format(s.getPoints()[i + 2]) + "\n");
        }
        for (int i = 0; i < s.getFaces().length; i += 3) {
            writer.write("f ");
            writer.write((s.getFaces()[i] + 1) + " ");
            writer.write((s.getFaces()[i + 1] + 1) + " ");
            writer.write((s.getFaces()[i + 2] + 1) + "\n");
        }
        if (s.getColors() != null) {
            for (int i = 0; i < s.getColors().length; i += 3) {
                writer.write("c " + formatter.format(s.getColors()[i]) + " ");
                writer.write(formatter.format(s.getColors()[i + 1]) + " ");
                writer.write(formatter.format(s.getColors()[i + 2]) + "\n");
            }
        }
        writer.flush();
        o.close();
    }

    /**
     * A simple converter call as:<br>
     * java -cp MindSeer.jar edu.washington.biostr.sig.volume.surface.SimpleOff input output<br>
     * This use the extension to convert.  Supported extensions are:
     * <ul>
     * <li>obj - wavefront</li>
     * <li>smf</li>
     * <li>sobj - surface smooth's obj format</li>
     * <li>off - geomview's off format.  Off outputs are automatically binary</li>
     * </ul>
     * 
     * @param args
     */
    public static void main(String[] args) {
        try {
        	String in = args[0];
        	String out = args[1];
        	Surface s;
        	if (in.endsWith("obj")) {
        		s = readObj(new FileInputStream(in));
        	} else if (in.endsWith("off")) {
        		s = readOff(new FileInputStream(in));
        	} else if (in.endsWith("sobj")) {
        		s = readSphereSmoothObj(new FileInputStream(in));
        	} else if (in.endsWith("smf")) {
        		s = readSMF(new FileInputStream(in));
        	} else {
        		throw new IllegalArgumentException("Unsupported format for: " + in);
        	}
        	
        	if (out.endsWith("obj")) {
        		saveObj(s, new FileOutputStream(out));
        	} else if (out.endsWith("off")) {
        		saveOff(s, new FileOutputStream(out));
        	} else if (out.endsWith("sobj")) {
        		saveSphereSmoothObj(s, new FileOutputStream(out));
        	} else if (out.endsWith("smf")) {
        		saveSMFText(s, new FileOutputStream(out));
        	} else {
        		throw new IllegalArgumentException("Unsupported format for: " + out);
        	}
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static Surface readObj(InputStream in) throws IOException {
    	String line;
    	int gcount = 0;
    	int maxface = -1;
        ArrayList<float[]> points = new ArrayList<float[]>(100000);
        ArrayList<int[]> faces = new ArrayList<int[]>(100000);
        ArrayList<float[]> normals = new ArrayList<float[]>(100000);
        int linecount = 0;
    	while ((line = readLine(in)) != null) {
    		linecount++;
    		line = line.trim();
    		if (line.startsWith("#") || line.length() == 0) {
    			continue;
    		}
    		String[] values = line.split("\\s+");
    		if (values[0].equalsIgnoreCase("v")) {
    			float[] p = new float[] {
    					Float.parseFloat(values[1]),
    					Float.parseFloat(values[2]),
    					Float.parseFloat(values[3])};    					
    			points.add(p);
    		} else if (values[0].equalsIgnoreCase("vn")) {
    			float[] n = new float[] {
    					Float.parseFloat(values[1]),
    					Float.parseFloat(values[2]),
    					Float.parseFloat(values[3])};    					
    			normals.add(n);
    		} else if (values[0].equalsIgnoreCase("f")) {
    			if (values.length != 4) {
    				System.err.println(linecount);
    				throw new IllegalArgumentException("Only triangles supported");
    			}
    			int[] face = new int[3];
    			for (int i = 0; i < 3; i++) {
    				String[] faceS = values[i + 1].split("/");
    				int myface = Integer.parseInt(faceS[0]); 
    				face[i] =  myface - 1;
    				maxface = Math.max(myface, maxface);
    				// check to make sure the values are "legal"
    				if (faceS.length == 3) {
    					if (Integer.parseInt(faceS[2]) - 1 != face[i]) {
    	    				System.err.println(linecount);
    						throw new IllegalArgumentException("Normals and faces must line up!");
    					}
    				}
    			}
    			faces.add(face);
    		} else if (values[0].equals("g"))  {
    			gcount++;
    			if (gcount > 1){
    				System.err.println(linecount);
    				throw new IllegalArgumentException("Only one Surface is supported");
    			}
    		}

    	}
    	System.out.println(maxface);
        // make the float
        float[] v = new float[points.size() * 3];
        for (int i = 0; i < points.size(); i++) {
            v[i * 3] = points.get(i)[0];
            v[i * 3 + 1] = points.get(i)[1];
            v[i * 3 + 2] = points.get(i)[2];
        }        // make the float
        float[] n = new float[normals.size() * 3];
        for (int i = 0; i < normals.size(); i++) {
            n[i * 3] = normals.get(i)[0];
            n[i * 3 + 1] = normals.get(i)[1];
            n[i * 3 + 2] = normals.get(i)[2];
        }
        int[] f = new int[faces.size() * 3];
        for (int i = 0; i < faces.size(); i++) {
            f[i * 3] = faces.get(i)[0];
            f[i * 3 + 1] = faces.get(i)[1];
            f[i * 3 + 2] = faces.get(i)[2];
        }
        Surface s = new Surface(f, v, n.length != 0 ? n : null);
        s.makeNormals();
        return s;
    	
    }
    
    public static Surface readSMF(InputStream in) throws IOException {
        ArrayList<float[]> points = new ArrayList<float[]>(100000);
        ArrayList<float[]> colors = new ArrayList<float[]>(100000);
        ArrayList<int[]> faces = new ArrayList<int[]>(100000);
        // read each line
        String line;
        do {
            line = readLine(in);
            if (line != null) {
                line = line.trim();
                if (line.startsWith("v")) {
                    String[] p = line.split("\\s");
                    points.add(new float[]{Float.parseFloat(p[1]),
                        Float.parseFloat(p[2]), Float.parseFloat(p[3])
                    });
                } else if (line.startsWith("f")) {
                    String[] p = line.split("\\s");
                    faces.add(new int[]{Integer.parseInt(p[1]) - 1,
                        Integer.parseInt(p[2]) - 1,
                        Integer.parseInt(p[3]) - 1
                    });
                }if (line.startsWith("c")) {
                    String[] p = line.split("\\s");
                    colors.add(new float[]{Float.parseFloat(p[1]),
                        Float.parseFloat(p[2]), Float.parseFloat(p[3])
                    });
                } 
            }
        } while (line != null);
        // make the float
        float[] v = new float[points.size() * 3];
        for (int i = 0; i < points.size(); i++) {
            v[i * 3] = points.get(i)[0];
            v[i * 3 + 1] = points.get(i)[1];
            v[i * 3 + 2] = points.get(i)[2];
        }
        float[] c;
        if (colors.isEmpty()) {
        	c = null;
        } else {
        	c =  new float[colors.size() * 3];
            for (int i = 0; i < colors.size(); i++) {
                c[i * 3] = colors.get(i)[0];
                c[i * 3 + 1] = colors.get(i)[1];
                c[i * 3 + 2] = colors.get(i)[2];
            }
        }
        int[] f = new int[faces.size() * 3];
        for (int i = 0; i < faces.size(); i++) {
            f[i * 3] = faces.get(i)[0];
            f[i * 3 + 1] = faces.get(i)[1];
            f[i * 3 + 2] = faces.get(i)[2];
        }
        Surface s = new Surface(f, v, null, c);
        s.makeNormals();
        return s;
    }
}
