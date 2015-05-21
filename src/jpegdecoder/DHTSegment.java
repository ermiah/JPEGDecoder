/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author Nosrati
 */
public class DHTSegment extends JpegSegment
{

    byte tIndex;
    byte tClass;
    byte[] codeLengths;
    byte[] symbols;

    List<HuffmanTable> ht = new ArrayList<HuffmanTable>();

    HashMap<Byte, HuffmanTable> huffmanTablesDC = new HashMap<>(),
            huffmanTablesAC = new HashMap<>();

    public DHTSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException
    {
        super(raf, markerSecondByte);

        int remained = len - 2;

        while (remained > 0)
        {
            byte d = br.readByte();
            tIndex = (byte) (d & 0xF);
            tClass = (byte) ((d & 0xF0) >> 4);
            remained--;

            int cnt = 0;

            codeLengths = new byte[16];
            br.read(codeLengths);

            for (int i = 0; i < 16; i++)
            {
                byte Li = codeLengths[i];
                cnt += Li;
            }

            remained -= 16;

            symbols = new byte[cnt];
            br.read(symbols);

            remained -= cnt;

            HuffmanTable dht = new HuffmanTable();
            dht.HuffmanCodes
                    = generateHuffmanCodes(ByteArray.toObjects(codeLengths), ByteArray.toObjects(symbols));

            if (tClass == 0)
            {
                huffmanTablesDC.put(tIndex, dht);
            }
            else
            {
                huffmanTablesAC.put(tIndex, dht);
            }

            ht.add(dht);    // for formatOutput
        }
    }

    @Override
    public String formattedOutput() throws IOException
    {
        StringBuilder sb = new StringBuilder();
        sb.append("DHT\n\n").append("Length: ").append(len).append('\n');

        sb.append("\nTable Index: ").append(tIndex).append("\nTable Class: ").
                append(tClass == 0 ? "DC" : "AC").append("\nCode Counts: ");

        for (int i = 0; i < 16; i++)
        {
            byte Li = codeLengths[i];
            sb.append(Li).append(" ");

        }

        sb.append("\nCode Values: ");

        for (int i = 0; i < symbols.length; i++)
        {
            byte Vi = symbols[i];
            sb.append(String.format("%02X ", Vi));
        }

        sb.append("\n\nSymbol | Huffman Codes\n---------------------\n");

        for (HuffmanTable dht : ht)
        {
            for (Entry<Integer, Byte> kvp : dht.HuffmanCodes.entrySet())
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
}
