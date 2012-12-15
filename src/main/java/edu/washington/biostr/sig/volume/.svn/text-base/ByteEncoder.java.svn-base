package edu.washington.biostr.sig.volume;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;

/**
 * Handle writing numbers in the specified endianness.
 * @author Eider Moore
 * @version 1.0
 */
public class ByteEncoder {

    ByteOrder endian;
    OutputStream out;

    /**
     * 
     * @param out The stream to which to send written numbers
     * @param endian The endianness (Big or Little)
     */
    public ByteEncoder(OutputStream out, ByteOrder endian) {
        this.out = out;
        this.endian = endian;
    }

    /**
     * 
     * @return The output target
     */
    public OutputStream getOut() {
        return out;
    }

    /**
     * 
     * @return The endianess
     */
    public ByteOrder getEndian() {
        return endian;
    }

    /**
     * Convenience method for encoding a long into a byte stream.
     * @param value
     * @param out
     * @throws IOException
     */
    public void write(long value) throws IOException {
        if (endian.equals(ByteOrder.BIG_ENDIAN)) {
            out.write((byte) ((value >> 56) & 0xff));
            out.write((byte) ((value >> 48) & 0xff));
            out.write((byte) ((value >> 40) & 0xff));
            out.write((byte) ((value >> 32) & 0xff));
            out.write((byte) ((value >> 24) & 0xff));
            out.write((byte) ((value >> 16) & 0xff));
            out.write((byte) ((value >> 8) & 0xff));
            out.write((byte) (value & 0xff));
        } else {
            out.write((byte) (value & 0xff));
            out.write((byte) ((value >> 8) & 0xff));
            out.write((byte) ((value >> 16) & 0xff));
            out.write((byte) ((value >> 24) & 0xff));
            out.write((byte) ((value >> 32) & 0xff));
            out.write((byte) ((value >> 40) & 0xff));
            out.write((byte) ((value >> 48) & 0xff));
            out.write((byte) ((value >> 56) & 0xff));
        }
    }

    /**
     * Convenience method for encoding an int into a byte stream.
     * @param value
     * @param out
     * @throws IOException
     */
    public void write(int value) throws IOException {
        if (endian.equals(ByteOrder.BIG_ENDIAN)) {
            out.write((byte) ((value >> 24) & 0xff));
            out.write((byte) ((value >> 16) & 0xff));
            out.write((byte) ((value >> 8) & 0xff));
            out.write((byte) (value & 0xff));
        } else {
            out.write((byte) (value & 0xff));
            out.write((byte) ((value >> 8) & 0xff));
            out.write((byte) ((value >> 16) & 0xff));
            out.write((byte) ((value >> 24) & 0xff));
        }
    }

    public void write(byte value) throws IOException {
        out.write(value);
    }

    /**
     * Convenience method for encoding a short into a byte stream.
     * @param value
     * @param out
     * @throws IOException
     */
    public void write(short value) throws IOException {
        if (endian.equals(ByteOrder.BIG_ENDIAN)) {
            out.write((byte) ((value >> 8) & 0xff));
            out.write((byte) (value & 0xff));
        } else {
            out.write((byte) (value & 0xff));
            out.write((byte) ((value >> 8) & 0xff));
        }
    }

    /**
     * Convenience method for encoding a char into a byte stream.
     * @param value
     * @param out
     * @throws IOException
     */
    public void write(char value) throws IOException {
        if (endian.equals(ByteOrder.BIG_ENDIAN)) {
            out.write((byte) ((value >> 8) & 0xff));
            out.write((byte) (value & 0xff));
        } else {
            out.write((byte) (value & 0xff));
            out.write((byte) ((value >> 8) & 0xff));
        }
    }

    /**
     * Convenience method for encoding a float into a byte stream.
     * @param value
     * @param out
     * @throws IOException
     */
    public void write(float value) throws IOException {
        write(Float.floatToIntBits(value));
    }

    /**
     * Convenience method for encoding a double into a byte stream.
     * @param value
     * @param out
     * @throws IOException
     */
    public void write(double value) throws IOException {
        write(Double.doubleToLongBits(value));
    }

    @Override
    protected void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public void close() throws IOException {
        out.flush();
        out.close();
    }
}
