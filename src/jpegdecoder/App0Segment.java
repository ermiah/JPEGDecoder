/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Nosrati
 */
public class App0Segment extends JpegSegment
{

    public App0Segment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);
    }

    @Override
    public String formattedOutput() throws IOException
    {
        byte[] identifier = new byte[5];

        br.read(identifier);

        String sIdentifier = "";        
        try
        {
            sIdentifier = new String(identifier, "US-ASCII").
                    substring(0, 4); // JFIF\0
        }
        catch (UnsupportedEncodingException ex)
        {
            Logger.getLogger(App0Segment.class.getName()).log(Level.SEVERE, null, ex);
        }

        byte verMaj = br.readByte();
        byte verMin = br.readByte();
        byte units = br.readByte();
        short Xdens = (short) br.readUnsignedShort();
        short Ydens = (short) br.readUnsignedShort();
        byte Xthum = br.readByte();
        byte Ythum = br.readByte();

        String unitsStr = "";
        if (units == 0)
        {
            unitsStr = "NONE";
        }
        else if (units == 1)
        {
            unitsStr = "Pixels/Inch";
        }
        else if (units == 2)
        {
            unitsStr = "Pixels/cm";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("APP0\n\n");
        sb.append("Length: ").append(len).
                append('\n' + "Identifier: ").append(sIdentifier).
                append('\n' + "Version: ").
                append(verMaj).append(".0").append(verMin).
                append('\n' + "Units: ").append(unitsStr).
                append('\n' + "X Density: ").append(Xdens).
                append('\n' + "Y Density: ").append(Ydens).
                append('\n' + "Thumbnail Width: ").append(Xthum).
                append('\n' + "Thumbnail Height: ").append(Ythum);
        
        return sb.toString();
    }

}
