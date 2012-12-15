/**
 * Copyright (C) 2006  Eider Moore
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
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.eiderman.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * This is made available under the terms of the LGPL.  Please visit: 
 * http://www.gnu.org/copyleft/lesser.html for more information.<br><br>
 * This class provides some helpful utilities for reading data and
 * finding files.
 * @author Eider Moore
 */
public class FileUtilities {

    /**
     * No compression is supported.
     */
    public static final int COMPRESSION_NONE = 0;
    /**
     * The file may be GZIP compressed.
     */
    public static final int COMPRESSION_GZIP = 1;

    private static ClassLoader resourceResolver = FileUtilities.class.getClassLoader();

    public static ClassLoader getResourceResolver() {
		return resourceResolver;
	}
    
    public static void setResourceResolver(ClassLoader resourceResolver) {
		FileUtilities.resourceResolver = resourceResolver;
	}
    
    /**
     * Use a base URL to find every file with the same name and extensions
     * from the list.  If a url doesn't exist, it is left out of the result.
     * @param url
     * @param extensions
     * @return A set of URI's referencing files with the same path/name as url and extensions from the list.
     * @throws URISyntaxException If we cannot convert the url to a uri
     * @throws IOException if the given url doesn't exist
     */
    public static URI[] findURIs(URL url, String[] extensions) throws URISyntaxException, IOException {
    	return toURI(findURLs(url, extensions));
    }

    public static URL[] findJarURLs(URL url, String[] extensions, ClassLoader loader) {
    	String path = url.getPath();
    	String external = url.toExternalForm();
    	external = external.substring(0, external.lastIndexOf('.'));
    	int index0 = path.indexOf("!/");
    	if (index0 < 0) {
    		index0 = 0;
    	} else {
    		index0 += 2;
    	}
    	List<URL> result = new ArrayList<URL>();
    	String name = path.substring(index0, path.lastIndexOf('.') + 1);
    	for (String ext : extensions) {
    		URL u = loader.getResource(name + ext);
    		if (u != null) {
    			result.add(u);
    		} else {
    			try {
					u = new URL(external);
					if (touchURL(u)) {
						result.add(u);
					}
				} catch (MalformedURLException e) {
					// skip
				}
    		}
    	}
    	return result.toArray(new URL[result.size()]);
	}

    public static URI[] findJarURIs(URL url, String[] extensions, ClassLoader loader) throws URISyntaxException {
    	URL[] urls = findJarURLs(url, extensions, loader);
    	return toURI(urls);
	}

    public static URI[] toURI(URL[] urls) throws URISyntaxException {
    	URI[] uris = new URI[urls.length];
    	for (int i = 0; i < urls.length; i++) {
    		uris[i] = urls[i].toURI();
    	}
    	return uris;
    }
    
    public static URL[] toURL(URI[] uris) throws MalformedURLException {
    	
    	URL[] urls = new URL[uris.length];
    	for (int i = 0; i < urls.length; i++) {
    		urls[i] = toURL(uris[i]);
    	}
    	return urls;
    }
    
	/**
     * Use a base File to find every file with the same name and extensions
     * from the list.  If a url doesn't exist, it is left out of the result.
     * @param file
     * @param extensions
     * @return A set of URI's referencing files with the same path/name as url and extensions from the list.
     */
    public static URI[] findURIs(File file, String[] extensions) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf('.');
        name = name.substring(0, lastIndex + 1);
        File dir = file.getParentFile();
        ArrayList<URI> answer = new ArrayList<URI>(extensions.length);
        for (String ext : extensions) {
            File cur = new File(dir, name + ext);
            if (cur.exists()) {
                answer.add(cur.toURI());
            }
        }
        // strip off a double extension
        name = name.substring(0, name.lastIndexOf('.', lastIndex) + 1);
        for (String ext : extensions) {
            File cur = new File(dir, name + ext);
            if (cur.exists()) {
                answer.add(cur.toURI());
            }
        }
        return answer.toArray(new URI[answer.size()]);
    }
    

    /**
     * Use a base URL to find every file with the same name and extensions
     * from the list.  If a url doesn't exist, it is left out of the result.
     * @param url
     * @param extensions
     * @return A set of URI's referencing files with the same path/name as url and extensions from the list.
     * @throws URISyntaxException If we cannot convert the url to a uri.  This won't really be thrown, but it is backwards compatible.
     * @throws IOException if the given url doesn't exist
     */
    public static URL[] findURLs(URL url, String[] extensions) throws URISyntaxException, IOException {
        try {
			if (url.getProtocol().contains("file")) {
			    return findURLs(new File(url.toURI()), extensions);
			} else if (url.getProtocol().contains("jar")) {
				return findJarURLs(url, extensions, resourceResolver);
			}
			String name = url.getPath();
			int i = name.lastIndexOf('/');
			if (i < 0) {
				System.err.println(url);
				i = 0;
			}
			name = name.substring(i, name.lastIndexOf('.') + 1);
			ArrayList<URL> answer = new ArrayList<URL>(extensions.length);
			for (String ext : extensions) {
			    boolean exists;
			    URL cur;
			    try {
			        cur = new URL(url, name + ext);
			        exists = touchURL(cur);
			    } catch (MalformedURLException e) {
			        exists = false;
			        cur = null;
			        e.printStackTrace();
			    }
			    if (exists) {
			        answer.add(cur);
			    }
			}
			if (answer.isEmpty()) {
			    // check if the original URL is wrong
			    url.openConnection().connect();
			}
			return answer.toArray(new URL[answer.size()]);
		} catch (URISyntaxException e) {
			throw new MalformedURLException(e.getMessage());
		}
    }

    /**
     * Use a base File to find every file with the same name and extensions
     * from the list.  If a url doesn't exist, it is left out of the result.
     * @param file
     * @param extensions
     * @return A set of URI's referencing files with the same path/name as url and extensions from the list.
     */
    public static URL[] findURLs(File file, String[] extensions) throws MalformedURLException {
    	return toURL(findURIs(file, extensions));
    }

    /**
     * Check to see if a url points to valid data.  This should be correct most
     * of the time, but may be wrong in some rare cases (usually caused by 
     * improperly configured servers).  In the end, it will not be an error
     * to read from this url (assuming the server or file don't go down).
     * @param url
     * @return true if the file exists, false if it appears to not exist.
     */
    public static boolean touchURL(URL url) {
        try {
            URLConnection connect = url.openConnection();
            if (connect instanceof HttpURLConnection) {
                return ((HttpURLConnection) connect).getResponseCode() == HttpURLConnection.HTTP_OK;
            } else {
                return connect.getContentLength() > 0;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Read an input stream into bytes.  This supports automatic decompression.  If
     * your file may legitimately begin with the magic number (2 bytes: [31, 139])
     * and you know it isn't gzipped, set supportedCompression null (also set it to null
     * if you already have the proper stream).<br>
     * <b> The stream is closed at the end of this method</b>
     * @param in The input stream
     * @param estimatedSize an estimated size.  this is more efficient if accurate.
     * @param supportedCompression An enum set of compression types or null for none
     * @return a byte[] with all of the data.
     * @throws IOException
     */
    public static byte[] getBytes(InputStream in, int estimatedSize,
            EnumSet<Compression> supportedCompression) throws IOException {
        in = getStream(in, supportedCompression);

        if (estimatedSize <= 0) {
            estimatedSize = 1024 * 1024;
        } // one meg
        FastByteArrayOutputStream out =
                new FastByteArrayOutputStream(estimatedSize > 0 ? estimatedSize : 1024 * 1024);
        byte[] b = new byte[1024 * 4];
        int len;
        while ((len = in.read(b)) != -1) {
            out.write(b, 0, len);
        }
        in.close();
        out.trim();
        return out.getArray();
    }

    /**
     * Read an input stream into bytes.  This supports automatic decompression.  If
     * your file may legitimately begin with the magic number (2 bytes: [31, 139])
     * and you know it isn't gzipped, set supportedCompression to null (also set it to null
     * if you already have the proper stream).<br>
     * <b> The stream is closed at the end of this method</b><br>
     * @param in The input stream
     * @param estimatedSize an estimated size.  this is more efficient if accurate.
     * @param supportedCompression An enum set of compression types or null for none
     * @return a ByteBuffer with all of the data.
     * @throws IOException
     */
    public static ByteBuffer getBuffer(InputStream in, int estimatedSize,
            EnumSet<Compression> supportedCompression) throws IOException {
        
        return ByteBuffer.wrap(getBytes(in, estimatedSize, supportedCompression));
    }

    /**
     * Check that in is buffered and check if gzip is supported, return the proper stream
     * that is buffered or decodes the compression.
     * @param in
     * @param supportedCompression An enum set of compression types or null for none
     * @return Either a stream to decompress the data or a buffered stream
     * @throws IOException
     */
    public static InputStream getStream(InputStream in, EnumSet<Compression> supportedCompression) throws IOException {
        if (!(in instanceof BufferedInputStream)) {
            in = new BufferedInputStream(in);
        }
        if (supportedCompression != null) {
            for (Compression comp : supportedCompression) {
                if (comp.isCompressed(in)) {
                    return comp.wrap(in);
                }
            }
        }

        return in;
    }

	public static URL[] findURLs(String fileResource, String[] extensions, ClassLoader c) {
        String name = fileResource.substring(0, fileResource.lastIndexOf('.'));
        ArrayList<URL> answer = new ArrayList<URL>(extensions.length);
        for (String ext : extensions) {
        	URL u = c.getResource(name + ext);
            if (u != null) {
                answer.add(u);
            }
        }
        return answer.toArray(new URL[answer.size()]);
	}
	
	   
	   /**
	    * Mac started generating illegal jar urls for resources, this is an attempted workaround.
	    * @param u
	    * @return
	    * @throws MalformedURLException
	    */
	   public static URL toURL(URI u) throws MalformedURLException {
		   try {
			return u.toURL();
		} catch (Exception e) {
			if (u.toString().contains("jar")) {
		    	
			String path = u.toString();
		    	int index0 = path.indexOf("!/");
		    	if (index0 < 0) {
		    		index0 = path.indexOf(":") + 1;
		    	} else {
		    		index0 += 2;
		    	}
		    	String name = path.substring(index0);
		    	return resourceResolver.getResource(name);
			} else {
				System.err.println(u);
				if (e instanceof MalformedURLException)
					throw (MalformedURLException) e;
				else if (e instanceof RuntimeException) {
					throw (RuntimeException) e;
				} else {
					throw new IllegalStateException(e);
				}
			}
		}
	   }
}
