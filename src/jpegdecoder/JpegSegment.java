/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jpegdecoder;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Nosrati
 */
public abstract class JpegSegment
{
    byte[] rawBytes;
    DataInputStream br;
    RandomAccessFile raf;
    short len;
    
    byte markerSB;
    
    public JpegSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        markerSB = markerSecondByte;
        this.raf = raf;
        
        if (!(this instanceof SOISegment || this instanceof EOISegment))
        {
            readBytes();
            
            br = new DataInputStream(new ByteArrayInputStream(rawBytes));
        }
    }
    
    private void readBytes() throws IOException
    {
        len = (short) raf.readUnsignedShort();
        rawBytes = new byte[len - 2];   // 2 bytes for "len"
        raf.read(rawBytes);
    }

    public String rawBytes()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("FF ").append(String.format("%02X ", markerSB));
        
        sb.append(ByteArray.formatBytes(rawBytes));
        sb.append("\n\n");
        return sb.toString();
    }
    
    public abstract String formattedOutput() throws IOException;
}
