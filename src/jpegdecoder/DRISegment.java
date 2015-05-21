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
public class DRISegment extends JpegSegment
{

    int restartInterval;

    public DRISegment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);

        restartInterval = (short) br.readUnsignedShort();
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("DRI").append("Length: ").append(len).
                append("\nRestart Interval (Number of MCUs before resetting the DC difference): ").append(restartInterval);
        
        return sb.toString();
    }

}
