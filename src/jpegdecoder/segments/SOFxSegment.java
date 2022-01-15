/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder.segments;

import jpegdecoder.segments.JpegSegment;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Nosrati
 */
public class SOFxSegment extends JpegSegment
{
    private String mode;
    
    public SOFxSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);
        
        if (getMarkerSB() == (byte)0xC1)
        {
            mode = "Sequential";
        }
        else if (getMarkerSB() == (byte)0xC2)
        {
            mode = "Progressive";
        }
        else if (getMarkerSB() == (byte)0xC3)
        {
            mode = "Lossless";
        }
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("SOF").append(getMarkerSB() & 0x0F);
        sb.append("Length: ").append(getLen());

        sb.append("\n").append(getMode());
        
        return sb.toString();
    }

    public String getMode() {
        return mode;
    }
}
