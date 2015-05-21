/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Nosrati
 */
public class SOSSegment extends JpegSegment
{

    byte compCount;
    byte[] compId;
    byte[] huffTables;
    byte[] DCHuff;
    byte[] ACHuff;

    public SOSSegment(RandomAccessFile raf, byte markerSecondByte, ArrayList<ColorComponent> colorComponents) throws IOException
    {
        super(raf, markerSecondByte);
        compCount = br.readByte();

        compId = new byte[compCount];
        huffTables = new byte[compCount];
        DCHuff = new byte[compCount];
        ACHuff = new byte[compCount];

        for (int i = 0; i < compCount; i++)
        {
            compId[i] = br.readByte();
            huffTables[i] = br.readByte();
            DCHuff[i] = (byte) (huffTables[i] >> 4);
            ACHuff[i] = (byte) (huffTables[i] & 0x0F);

            for (ColorComponent cc : colorComponents)
            {
                if (cc.Index == compId[i])
                {
                    cc.DCTableIndex = DCHuff[i];
                    cc.ACTableIndex = ACHuff[i];
                    break;
                }
            }
        }
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        sb.append("SOS").append("Length: ").append(len).
                append("\nComponent Count: ").append(compCount);

        for (int i = 0; i < compCount; i++)
        {
            sb.append("\n\nComponent ID: ").append(compId[i]).
                    append("\nDC Huffman Table: ").append(DCHuff[i]).
                    append("\nAC Huffman Table: ").append(ACHuff[i]).append('\n');
        }

        sb.append("\nSpectral Selection Start: ").append(br.readByte()).
                append("\nSpectral Selection End: ").append(br.readByte()).
                append("\nSuccessive Approximation: ").append(br.readByte()).
                append('\n');
        
        return sb.toString();
    }

}
