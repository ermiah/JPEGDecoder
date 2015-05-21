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
public class SOF0Segment extends JpegSegment
{

    ArrayList<ColorComponent> colorComponents = new ArrayList<>();
    int img_width, img_height;
    byte numOfComps;
    byte sampPrec;
    String chromaSubsampling = "";

    public SOF0Segment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);

        sampPrec = br.readByte();
        img_height = (short) br.readUnsignedShort();
        img_width = (short) br.readUnsignedShort();
        numOfComps = br.readByte();

        for (int i = 0; i < numOfComps; i++)
        {
            byte comp = br.readByte();
            String compString = "";
            if (comp == 1)
            {
                compString = "Y";
            }
            else if (comp == 2)
            {
                compString = "Cb";
            }
            else if (comp == 3)
            {
                compString = "Cr";
            }

            byte Freq = br.readByte();
            byte horzFreq = (byte) ((Freq & 0xF0) >> 4);
            byte vertFreq = (byte) (Freq & 0x0F);

            byte qTable = br.readByte();

            ColorComponent cc = new ColorComponent(comp);
            cc.HorzFreq = horzFreq;
            cc.VertFreq = vertFreq;
            cc.QTableIndex = qTable;

            colorComponents.add(cc);

        }

        if (numOfComps == 1)                    // Y    .   .
        {
            chromaSubsampling = "1:0:0";
        }
        else if (numOfComps == 3)
        {
            if (colorComponents.get(0).HorzFreq == 1)   // Y      Cb        Cr 
            {
                chromaSubsampling = "4:4:4";
            }
            else if (colorComponents.get(0).HorzFreq == 2
                    && colorComponents.get(0).VertFreq == 1)  // Y Y    Cb ..     Cr ..
            {
                chromaSubsampling = "4:2:2";
            }
            else if (colorComponents.get(0).HorzFreq == 2
                    && colorComponents.get(0).VertFreq == 2)
            {
                ColorComponent cc = colorComponents.get(1); //  Cb
                if (cc.HorzFreq == 1)
                {                                   // Y Y  Y Y  Cb ..  Cb ..   Cr ..   Cr ..
                    chromaSubsampling = "4:2:0";    // Y Y  Y Y  .. ..  .. ..   .. ..   .. ..
                }
            }
        }

    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("SOF0\n\n");
        sb.append("Baseline DCT (Huffman)\n" + "Length: ").append(len).
                append('\n' + "Precision (Bits/Pixel per Component): ").append(sampPrec).
                append('\n' + "Height: ").append(img_height).
                append('\n' + "Width: ").append(img_width).
                append('\n' + "Number of Components: ").
                append(numOfComps).append('\n');

        if (numOfComps == 1)                    // Y    .   .
        {
            chromaSubsampling = "1:0:0";
            sb.append("\nChroma Subsampling: ").
                    append(chromaSubsampling).append("\nY    .   .\n\n");
        }
        else if (numOfComps == 3)
        {
            if (colorComponents.get(0).HorzFreq == 1)   // Y      Cb        Cr 
            {
                chromaSubsampling = "4:4:4";
                sb.append("\nChroma Subsampling: ").append(chromaSubsampling).
                        append("\nY      Cb        Cr\n\n");
            }
            else if (colorComponents.get(0).HorzFreq == 2
                    && colorComponents.get(0).VertFreq == 1)  // Y Y    Cb ..     Cr ..
            {
                chromaSubsampling = "4:2:2";
                sb.append("\nChroma Subsampling: ").append(chromaSubsampling).
                        append("\nY Y    Cb ..     Cr ..\n\n");
            }
            else if (colorComponents.get(0).HorzFreq == 2
                    && colorComponents.get(0).VertFreq == 2)
            {
                ColorComponent cc = colorComponents.get(1); //  Cb
                if (cc.HorzFreq == 1)
                {                                   // Y Y  Y Y  Cb ..  Cb ..   Cr ..   Cr ..
                    chromaSubsampling = "4:2:0";    // Y Y  Y Y  .. ..  .. ..   .. ..   .. ..
                    sb.append("\nChroma Subsampling: ").append(chromaSubsampling).
                            append("\nY Y  Y Y  Cb ..  Cb ..   Cr ..   Cr ..\nY Y  Y Y  .. ..  .. ..   .. ..   .. ..\n\n");
                }
                /*else if (cc.VertFreq == 2)  //?
                 {                                   // Y Y  Y Y  Cb ..  .. ..   Cr ..   .. ..
                 chromaSubsampling = "4:1:1";    // Y Y  Y Y  Cb ..  .. ..   Cr ..   .. ..
                 s += "\nChroma Subsampling: " + chromaSubsampling +
                 "\nY Y  Y Y  Cb ..  .. ..   Cr ..   .. ..\nY Y  Y Y  Cb ..  .. ..   Cr ..   .. ..\n\n";
                 }*/
            }

        }
        return sb.toString();
    }
}
