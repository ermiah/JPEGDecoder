package jpegdecoder.segments;

import jpegdecoder.ColorComponent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Nosrati
 */
public class SOF0Segment extends JpegSegment {

    private final ArrayList<ColorComponent> colorComponents = new ArrayList<>();
    private final int imgWidth;
    private final int imgHeight;
    private final byte numOfComps;
    private final byte sampPrec;
    private String chromaSubsampling = "";

    public SOF0Segment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        super(raf, markerSecondByte);

        sampPrec = getBr().readByte();
        imgHeight = (short) getBr().readUnsignedShort();
        imgWidth = (short) getBr().readUnsignedShort();
        numOfComps = getBr().readByte();

        for (int i = 0; i < getNumOfComps(); i++) {
            byte comp = getBr().readByte();
            String compString = "";
            if (comp == 1) {
                compString = "Y";
            } else if (comp == 2) {
                compString = "Cb";
            } else if (comp == 3) {
                compString = "Cr";
            }

            byte freq = getBr().readByte();
            byte horzFreq = (byte) ((freq & 0xF0) >> 4);
            byte vertFreq = (byte) (freq & 0x0F);

            byte qTable = getBr().readByte();

            ColorComponent cc = new ColorComponent(comp);
            cc.setHorzFreq(horzFreq);
            cc.setVertFreq(vertFreq);
            cc.setqTableIndex(qTable);

            getColorComponents().add(cc);

        }

        if (getNumOfComps() == 1)                    // Y    .   .
        {
            chromaSubsampling = "1:0:0";
        } else if (getNumOfComps() == 3) {
            if (getColorComponents().get(0).getHorzFreq() == 1)   // Y      Cb        Cr
            {
                chromaSubsampling = "4:4:4";
            } else if (getColorComponents().get(0).getHorzFreq() == 2
                    && getColorComponents().get(0).getVertFreq() == 1)  // Y Y    Cb ..     Cr ..
            {
                chromaSubsampling = "4:2:2";
            } else if (getColorComponents().get(0).getHorzFreq() == 2
                    && getColorComponents().get(0).getVertFreq() == 2) {
                ColorComponent cc = getColorComponents().get(1); //  Cb
                if (cc.getHorzFreq() == 1) {                                   // Y Y  Y Y  Cb ..  Cb ..   Cr ..   Cr ..
                    chromaSubsampling = "4:2:0";    // Y Y  Y Y  .. ..  .. ..   .. ..   .. ..
                }
            }
        }

    }

    @Override
    public String formattedOutput() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("SOF0\n\n");
        sb.append("Baseline DCT (Huffman)\n" + "Length: ").append(getLen()).
                append('\n' + "Precision (Bits/Pixel per Component): ").append(getSampPrec()).
                append('\n' + "Height: ").append(getImgHeight()).
                append('\n' + "Width: ").append(getImgWidth()).
                append('\n' + "Number of Components: ").
                append(getNumOfComps()).append('\n');

        if (getNumOfComps() == 1)                    // Y    .   .
        {
            chromaSubsampling = "1:0:0";
            sb.append("\nChroma Subsampling: ").
                    append(getChromaSubsampling()).append("\nY    .   .\n\n");
        } else if (getNumOfComps() == 3) {
            if (getColorComponents().get(0).getHorzFreq() == 1)   // Y      Cb        Cr
            {
                chromaSubsampling = "4:4:4";
                sb.append("\nChroma Subsampling: ").append(getChromaSubsampling()).
                        append("\nY      Cb        Cr\n\n");
            } else if (getColorComponents().get(0).getHorzFreq() == 2
                    && getColorComponents().get(0).getVertFreq() == 1)  // Y Y    Cb ..     Cr ..
            {
                chromaSubsampling = "4:2:2";
                sb.append("\nChroma Subsampling: ").append(getChromaSubsampling()).
                        append("\nY Y    Cb ..     Cr ..\n\n");
            } else if (getColorComponents().get(0).getHorzFreq() == 2
                    && getColorComponents().get(0).getVertFreq() == 2) {
                ColorComponent cc = getColorComponents().get(1); //  Cb
                if (cc.getHorzFreq() == 1) {                                   // Y Y  Y Y  Cb ..  Cb ..   Cr ..   Cr ..
                    chromaSubsampling = "4:2:0";    // Y Y  Y Y  .. ..  .. ..   .. ..   .. ..
                    sb.append("\nChroma Subsampling: ").append(getChromaSubsampling()).
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

    public List<ColorComponent> getColorComponents() {
        return colorComponents;
    }

    public int getImgWidth() {
        return imgWidth;
    }

    public int getImgHeight() {
        return imgHeight;
    }

    public byte getNumOfComps() {
        return numOfComps;
    }

    public byte getSampPrec() {
        return sampPrec;
    }

    public String getChromaSubsampling() {
        return chromaSubsampling;
    }
}
