/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Nosrati
 */
public class SOFxSegment extends JpegSegment
{
    public String mode;
    
    public SOFxSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);
        
        if (markerSB == (byte)0xC1)
        {
            mode = "Sequential";
        }
        else if (markerSB == (byte)0xC2)
        {
            mode = "Progressive";
        }
        else if (markerSB == (byte)0xC3)
        {
            mode = "Lossless";
        }
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append("SOF").append(markerSB & 0x0F);
        sb.append("Length: ").append(len);

        sb.append("\n").append(mode);
        
        return sb.toString();
    }

}
