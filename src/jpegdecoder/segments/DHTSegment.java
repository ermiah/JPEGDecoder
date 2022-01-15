package jpegdecoder.segments;

import jpegdecoder.ByteArray;
import jpegdecoder.HuffmanTable;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author Nosrati
 */
public class DHTSegment extends JpegSegment
{
    private byte tIndex;
    private byte tClass;
    private byte[] codeLengths;
    private byte[] symbols;

    private List<HuffmanTable> ht = new ArrayList<>();

    private Map<Byte, HuffmanTable> huffmanTablesDC = new HashMap<>();
    private Map<Byte, HuffmanTable> huffmanTablesAC = new HashMap<>();

    public DHTSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);

        int remained = getLen() - 2;

        while (remained > 0)
        {
            byte d = getBr().readByte();
            tIndex = (byte) (d & 0xF);
            tClass = (byte) ((d & 0xF0) >> 4);
            remained--;

            int cnt = 0;

            codeLengths = new byte[16];
            getBr().read(getCodeLengths());

            for (int i = 0; i < 16; i++)
            {
                byte Li = getCodeLengths()[i];
                cnt += Li;
            }

            remained -= 16;

            symbols = new byte[cnt];
            getBr().read(getSymbols());

            remained -= cnt;

            HuffmanTable dht = new HuffmanTable();
            dht.setHuffmanCodes(generateHuffmanCodes(ByteArray.toObjects(getCodeLengths()), ByteArray.toObjects(getSymbols())));

            if (gettClass() == 0)
            {
                getHuffmanTablesDC().put(gettIndex(), dht);
            }
            else
            {
                getHuffmanTablesAC().put(gettIndex(), dht);
            }

            getHt().add(dht);    // for formatOutput
        }
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("DHT\n\n").append("Length: ").append(getLen()).append('\n');

        sb.append("\nTable Index: ").append(gettIndex()).append("\nTable Class: ").
                append(gettClass() == 0 ? "DC" : "AC").append("\nCode Counts: ");

        for (int i = 0; i < 16; i++)
        {
            byte Li = getCodeLengths()[i];
            sb.append(Li).append(" ");

        }

        sb.append("\nCode Values: ");

        for (int i = 0; i < getSymbols().length; i++)
        {
            byte Vi = getSymbols()[i];
            sb.append(String.format("%02X ", Vi));
        }

        sb.append("\n\nSymbol | Huffman Codes\n---------------------\n");

        for (HuffmanTable dht : getHt())
        {
            for (Entry<Integer, Byte> kvp : dht.getHuffmanCodes().entrySet())
            {
                Integer length_code = kvp.getKey();
                byte symbol = kvp.getValue();

                short length = (short) ((length_code & 0xFFFF0000) >> 16);
                short code = (short) (length_code & 0x0000FFFF);

                sb.append(String.format("%02X", symbol)).append(" | ").
                        append(String.format("%" + length + "s",
                                        Integer.toBinaryString(code)).replace(' ', '0')).append('\n');
            }

            sb.append("\n\n");
        }

        return sb.toString();
    }

    LinkedHashMap<Integer, Byte> generateHuffmanCodes(Byte[] codeLengths, Byte[] symbols)
    {
        short code = 0;    

        LinkedHashMap<Integer, Byte> huffmanCodes = new LinkedHashMap<>();

        int nSymbol = 0;

        for (int bits = 1; bits <= 16; bits++)
        {
            for (int i = 0; i < codeLengths[bits - 1]; i++)
            {

                int length_code = (int) ((bits << 16) | (code));

                huffmanCodes.put(length_code, symbols[nSymbol++]);
                code++; 
            }

            code <<= 1; 
        }

        return huffmanCodes;
    }

    public byte gettIndex() {
        return tIndex;
    }

    public byte gettClass() {
        return tClass;
    }

    public byte[] getCodeLengths() {
        return codeLengths;
    }

    public byte[] getSymbols() {
        return symbols;
    }

    public List<HuffmanTable> getHt() {
        return ht;
    }

    public Map<Byte, HuffmanTable> getHuffmanTablesDC() {
        return huffmanTablesDC;
    }

    public Map<Byte, HuffmanTable> getHuffmanTablesAC() {
        return huffmanTablesAC;
    }
}
