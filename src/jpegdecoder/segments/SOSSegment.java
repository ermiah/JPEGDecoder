package jpegdecoder.segments;

import jpegdecoder.ColorComponent;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Nosrati
 */
public class SOSSegment extends JpegSegment
{
    private byte compCount;
    private byte[] compId;
    private byte[] huffTables;
    private byte[] DCHuff;
    private byte[] ACHuff;

    public SOSSegment(RandomAccessFile raf, byte markerSecondByte, ArrayList<ColorComponent> colorComponents) throws IOException
    {
        super(raf, markerSecondByte);
        setCompCount(getBr().readByte());

        setCompId(new byte[getCompCount()]);
        setHuffTables(new byte[getCompCount()]);
        setDCHuff(new byte[getCompCount()]);
        setACHuff(new byte[getCompCount()]);

        for (int i = 0; i < getCompCount(); i++)
        {
            getCompId()[i] = getBr().readByte();
            getHuffTables()[i] = getBr().readByte();
            getDCHuff()[i] = (byte) (getHuffTables()[i] >> 4);
            getACHuff()[i] = (byte) (getHuffTables()[i] & 0x0F);

            for (ColorComponent cc : colorComponents)
            {
                if (cc.getIndex() == getCompId()[i])
                {
                    cc.setDctableindex(getDCHuff()[i]);
                    cc.setActableindex(getACHuff()[i]);
                    break;
                }
            }
        }
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();

        sb.append("SOS").append("Length: ").append(getLen()).
                append("\nComponent Count: ").append(getCompCount());

        for (int i = 0; i < getCompCount(); i++)
        {
            sb.append("\n\nComponent ID: ").append(getCompId()[i]).
                    append("\nDC Huffman Table: ").append(getDCHuff()[i]).
                    append("\nAC Huffman Table: ").append(getACHuff()[i]).append('\n');
        }

        sb.append("\nSpectral Selection Start: ").append(getBr().readByte()).
                append("\nSpectral Selection End: ").append(getBr().readByte()).
                append("\nSuccessive Approximation: ").append(getBr().readByte()).
                append('\n');
        
        return sb.toString();
    }

    public byte getCompCount() {
        return compCount;
    }

    public void setCompCount(byte compCount) {
        this.compCount = compCount;
    }

    public byte[] getCompId() {
        return compId;
    }

    public void setCompId(byte[] compId) {
        this.compId = compId;
    }

    public byte[] getHuffTables() {
        return huffTables;
    }

    public void setHuffTables(byte[] huffTables) {
        this.huffTables = huffTables;
    }

    public byte[] getDCHuff() {
        return DCHuff;
    }

    public void setDCHuff(byte[] DCHuff) {
        this.DCHuff = DCHuff;
    }

    public byte[] getACHuff() {
        return ACHuff;
    }

    public void setACHuff(byte[] ACHuff) {
        this.ACHuff = ACHuff;
    }
}
