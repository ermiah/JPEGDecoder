/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jpegdecoder.segments;

import jpegdecoder.ByteArray;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Nosrati
 */
public abstract class JpegSegment {
    private byte[] rawBytes;
    private DataInputStream br;
    private final RandomAccessFile raf;
    private short len;

    private final byte markerSB;

    protected JpegSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        markerSB = markerSecondByte;
        this.raf = raf;

        if (!(this instanceof SOISegment || this instanceof EOISegment)) {
            readBytes();

            br = new DataInputStream(new ByteArrayInputStream(getRawBytes()));
        }
    }

    private void readBytes() throws IOException {
        len = (short) getRaf().readUnsignedShort();
        rawBytes = new byte[getLen() - 2];   // 2 bytes for "len"
        getRaf().read(getRawBytes());
    }

    public String rawBytes() {
        return "FF " +
                String.format("%02X ", getMarkerSB()) +
                ByteArray.formatBytes(getRawBytes()) +
                "\n\n";
    }

    public abstract String formattedOutput() throws IOException;

    public byte[] getRawBytes() {
        return rawBytes;
    }

    public DataInputStream getBr() {
        return br;
    }

    public RandomAccessFile getRaf() {
        return raf;
    }

    public short getLen() {
        return len;
    }

    public byte getMarkerSB() {
        return markerSB;
    }
}
