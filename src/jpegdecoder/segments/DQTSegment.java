/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder.segments;

import jpegdecoder.QuantizationTable;
import jpegdecoder.ZigZagTable;
import jpegdecoder.segments.JpegSegment;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

/**
 *
 * @author Nosrati
 */
public class DQTSegment extends JpegSegment
{
    private byte tIndex;
    private byte tPrec;
    
    private final ArrayList<QuantizationTable> qTables = new ArrayList<>();

    public DQTSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);

        int remained = getLen() - 2;

        while (remained > 0)
        {
            byte t = getBr().readByte();
            tIndex = (byte) (t & 0x0F);
            tPrec = (byte) ((t & 0xF0) >> 4);
            remained--;

            short[] qTable = new short[64];
            for (int i = 0; i < 64; i++)
            {
                qTable[i] = (short) (tPrec == 0 ? getBr().readByte()
                        : (short) getBr().readUnsignedShort());
            }

            qTables.add(new QuantizationTable(ZigZagTable.convertToZigZagTable(qTable)));

            if (tPrec == 0)
            {
                remained -= 64;
            }
            else
            {
                remained -= 128;
            }
        }
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("DQT\n\n").append("Length: ").append(getLen()).append('\n');

        sb.append("\nTable Index: ").append(tIndex).append("\nPrecision: ").
                append(tPrec).append("\n");

        StringBuilder tableFields = new StringBuilder();
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                tableFields.append(String.format("%4d ",
                     qTables.get(qTables.size() - 1).getArray()[i][j]));
            }
            tableFields.append("\n");
        }

        sb.append(tableFields.toString()).append("\n");
        return sb.toString();
    }

    public ArrayList<QuantizationTable> getqTables() {
        return qTables;
    }
}
