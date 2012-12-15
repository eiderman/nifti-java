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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * Hold the compression types that FileUtilities knows how to check
 * for and decode (by wrapping in a special inputstream)
 * @author Eider Moore
 */
public enum Compression {
// ZIP7 (new int[] {'7', 'z', 0xBC, 0xAF, 0x27, 0x1C}, new String[] {".7z"}),
// BZIP (new int[] {'B', 'Z'}, new String[] {".bz"}),
    GZIP(new int[]{31, 139}, new String[]{".gz", "z"});
    int[] magic;
    String[] ext;

    /**
     * The magic number starts a stream to flag it.
     * @param magicNumber
     */
    Compression(int[] magicNumber, String[] ext) {
        this.ext = ext;
        this.magic = magicNumber;
    }

    /**
     * Check if a inputstream is compressed
     * @param in a markable stream
     * @return true if it is compressed
     * @throws IOException
     */
    public boolean isCompressed(InputStream in) throws IOException {

        if (!in.markSupported()) {
            throw new IllegalArgumentException("In must support marking!");
        }
        in.mark(magic.length);
        for (int i = 0; i < magic.length; i++) {
            if (magic[i] != in.read()) {
                in.reset();
                return false;
            }
        }
        in.reset();
        return true;
    }

    /**
     * Wrap in so that we can just read it as a standard stream.
     * @param in
     * @return the wrapped stream.
     * @throws IOException
     */
    public InputStream wrap(InputStream in) throws IOException {
        switch (this) {
            case GZIP:
                return new GZIPInputStream(in);
            default:
                throw new AssertionError("Unknown op: " + this);
        }
    }

    public String[] getExtensionModifiers() {
        return ext;
    }
}
