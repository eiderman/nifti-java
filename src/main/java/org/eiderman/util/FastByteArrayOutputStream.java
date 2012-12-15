package org.eiderman.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author eider
 */
public class FastByteArrayOutputStream extends OutputStream {

    byte[] array;
    int index;

    public FastByteArrayOutputStream(int size) {
        array = new byte[size];
    }

    @Override
    public void write(int v) throws IOException {
        if (index == array.length) {
            resize(array.length * 2);
        }
        array[index++] = (byte) (v & 0xff);
    }

    @Override
    public void write(byte[] bytes, int offset, int len) throws IOException {
        if (index + len >= array.length) {
            resize((index + len) * 2);
        }
        for (int i = 0; i < len; i++) {
            array[i + index] = bytes[offset + i];
        }
        index += len;
    }

    
    
    public byte[] getArray() {
        return array;
    }
    
    public void trim() {
        resize(index);
    }

    private void resize(int size) {
        if (size == array.length)
            return;//lazy boy!
        byte[] nArr = new byte[size];
        System.arraycopy(array, 0, nArr, 0, index);
        array = nArr;
    }
    
    public int getSize() {
        return index;
    }
}
