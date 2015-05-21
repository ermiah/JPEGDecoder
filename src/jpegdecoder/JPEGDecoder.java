/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Nosrati
 */
public class JPEGDecoder
{

    int bitPointer = -1;    // not started yet
    byte curByte = 0;
    private int idctFunc;

    BufferedImage image;

    String chromaSubsampling = "";

    int restartInterval = 0;

    ArrayList<ColorComponent> colorComponents = new ArrayList<>();
    //ArrayList<short[][]> qTables = new ArrayList<>();
    ArrayList<QuantizationTable> qTables = new ArrayList<>();
    HashMap<Byte, HuffmanTable> huffmanTablesDC = new HashMap<>(),
            huffmanTablesAC = new HashMap<>();

    int[][] M_int = new int[8][8],
            MT_int = new int[8][8];

    int img_width, img_height;

    MCU[][] MCUs;

    boolean unableToDecode = false;
    public String mode = "";

    int mcuCount;

    public JPEGDecoder()
    {
        buildMTables();
        buildMTables_int();
    }

    Color[][] convertToRGB(int[][] Y, int[][] Cb, int[][] Cr)
    {
        int n = Y.length,
                m = Y[0].length;

        Color[][] rgbBlock = new Color[n][m];

        int r, g, b;
        int y, cr, cb;
        for (int i = 0; i < n; i++)
        {
            for (int j = 0; j < m; j++)
            {
                y = Y[i][j] + 128;
                cr = Cr[i][j] + 128;
                cb = Cb[i][j] + 128;

                r = (int) (y + 1.371 * (cr - 128));
                g = (int) (y - 0.698 * (cr - 128) - 0.336 * (cb - 128));
                b = (int) (y + 1.732 * (cb - 128));

                rgbBlock[i][j] = new Color(
                        Math.min(Math.max(0, r), 255),
                        Math.min(Math.max(0, g), 255),
                        Math.min(Math.max(0, b), 255));
            }
        }

        return rgbBlock;
    }

    public void decode(File f) throws FileNotFoundException, Exception
    {
        RandomAccessFile br = new RandomAccessFile(f, "r");
        logSB = new StringBuilder();

        JpegSegment jpegSegment = readSegment(br);
        logSB.append(jpegSegment.formattedOutput());
        logSB.append(jpegSegment.rawBytes());
        logSB.append("-----------------------------\n");
        
        while (jpegSegment != null && !(jpegSegment instanceof EOISegment))
        {            
            jpegSegment = readSegment(br);
            
            logSB.append(jpegSegment.formattedOutput());
            logSB.append(jpegSegment.rawBytes());
            logSB.append("-----------------------------\n");
        }
    }

    JpegSegment readSegment(RandomAccessFile br) throws Exception
    {
        byte[] marker = new byte[2];
        br.read(marker);

        if (marker[0] != (byte) 0xFF)
        {
            throw new Exception("Not a valid marker");
        }

        JpegSegment jpegSegment = null;

        switch (marker[1])
        {
            case (byte) 0xD8:  // SOI (Start of Image)
                jpegSegment = new SOISegment(br, (byte) 0xD8);

                break;

            case (byte) 0xE0:  // APP0
                jpegSegment = new App0Segment(br, (byte) 0xE0);

                break;

            case (byte) 0xE1:  // APPn
            case (byte) 0xE2:
            case (byte) 0xE3:
            case (byte) 0xE4:
            case (byte) 0xE5:
            case (byte) 0xE6:
            case (byte) 0xE7:
            case (byte) 0xE8:
            case (byte) 0xE9:
            case (byte) 0xEA:
            case (byte) 0xEB:
            case (byte) 0xEC:
            case (byte) 0xED:
            case (byte) 0xEE:
            case (byte) 0xEF:

            case (byte) 0xFE: // COM
                jpegSegment = new COMSegment(br, marker[1]);

                break;

            case (byte) 0xD9: //  EOI
                jpegSegment = new EOISegment(br, marker[1]);

                break;

            case (byte) 0xDB:  // DQT
                jpegSegment = new DQTSegment(br, (byte) 0xDB);

                qTables.addAll(((DQTSegment) jpegSegment).qTables);

                break;

            case (byte) 0xC4:  // DHT
                jpegSegment = new DHTSegment(br, (byte) 0xC4);

                huffmanTablesDC.putAll(((DHTSegment) jpegSegment).huffmanTablesDC);
                huffmanTablesAC.putAll(((DHTSegment) jpegSegment).huffmanTablesAC);

                break;

            case (byte) 0xC0:  // SOF0
                jpegSegment = new SOF0Segment(br, (byte) 0xC0);

                colorComponents.addAll(((SOF0Segment) jpegSegment).colorComponents);

                img_width = ((SOF0Segment) jpegSegment).img_width;
                img_height = ((SOF0Segment) jpegSegment).img_height;

                chromaSubsampling = ((SOF0Segment) jpegSegment).chromaSubsampling;
                mode = "baseline";

                break;

            case (byte) 0xC1:  // Sequential - Not Implemented
            case (byte) 0xC2:  // Progressive - Not Implemented
            case (byte) 0xC3:  // Lossless - Not Implemented
                jpegSegment = new SOFxSegment(br, marker[1]);

                unableToDecode = true;
                image = null;
                mode = ((SOFxSegment)jpegSegment).mode;

                break;
            case (byte) 0xDD:  // DRI

                jpegSegment = new DRISegment(br, marker[1]);
                restartInterval = ((DRISegment) jpegSegment).restartInterval;

                break;

            case (byte) 0xDA:  // SOS
                jpegSegment = new SOSSegment(br, marker[1], colorComponents);

                if (unableToDecode)
                {
                    dumpCompressedData(br);
                }
                else
                {
                    decodeMCUs(br);
                }

                break;
            default:
                jpegSegment = null;

                break;
        }

        return jpegSegment;
    }

    private void dumpCompressedData(RandomAccessFile br) throws IOException
    {
        boolean endOfSeg = false;

        ArrayList<Byte> scan = new ArrayList<>();
        scan.add((byte) 0xFF);
        scan.add((byte) 0xDA);
        byte b;
        while (!endOfSeg)
        {
            b = br.readByte();
            if (b == (byte)0xFF)
            {
                byte next = br.readByte();
                br.seek(br.getFilePointer() - 1);

                if (next != (byte)0x00)
                {
                    if (!(next >= (byte)0xD0 && next <= (byte)0xD7))    // RSTn
                    {               // not RSTn: end of segment
                        //Byte[] compressedData = new Byte[scan.size()];
                        //scan.toArray(compressedData);
                        
                        logSB.append("<Compressed Data>\n").
                                //append(ByteArray.formatBytes(compressedData)).
                                append("-------------------").
                                append("\n\n");

                        br.seek(br.getFilePointer() - 1);
                        endOfSeg = true;
                    }
                    else
                    {
                        scan.add(b);
                    }
                }
                else
                {
                    scan.add(b);
                }
            }
            else
            {
                scan.add(b);
            }
        }
    }

    int[][] dequantize(short[][] dataUnit, int qTableIndex)
    {
        short[][] qTable = qTables.get(qTableIndex).getArray();
        int[][] result = new int[8][8];

        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                result[i][j] = qTable[i][j] * dataUnit[i][j];
            }
        }
        return result;
    }

    short readBit(RandomAccessFile br) throws IOException
    {
        if (bitPointer == -1)   // we have finished the previous byte, let's start the new one
        {
            curByte = br.readByte();

            if (curByte == (byte) 0xFF)
            {
                /*int nextByte = */br.readByte();

//                if (nextByte == 0)   // 0xFF00 ==> 0xFF
//                {
//                }  
            }

            bitPointer = 7;
        }

        if (((1 << bitPointer--) & curByte) != 0)
        {
            return 1;
        }
        else
        {
            return 0;
        }
    }

    short readBits(RandomAccessFile br, int n) throws IOException
    {
        short bits = 0;
        for (int i = 0; i < n; i++)
        {
            bits = (short) ((bits << 1) | readBit(br));
        }

        return bits;
    }

    short Extend(short bits, byte size)
    {
        short v = (short) (1 << size - 1);     // for 5 bits, v = 10000

        // if it's negative, we find -max (where max is the largest positive
        // number which can be made using "size" bits.) and we add it to "bits". 
        // for example, if size = 5, the largest positive 5 bits number is
        // max = (11111)2 = 31. 
        // -max is calculated by left shifting -1, size times, and adding 1. 
        if (bits < v)   // "bits" manfie, chon bite akharesh 1 nist
        {
            return (short) ((-1 << size) + 1 + bits);   // difference = -max + bits
        }
        else
        {
            return (short) bits;    // difference = bits
        }
    }

    byte decodeNextSymbol(RandomAccessFile br, HuffmanTable ht) throws Exception
    {
        short code = readBit(br);
        short len = 1;
        OutByte sym = new OutByte();

        while (!ht.getSymbol(code, len, sym))
        {
            code = (short) ((code << 1) | readBit(br));
            len++;

            if (len > 16)
            {
                throw new Exception("Invalid Huffman Code");
            }
        }
        return sym.data;

    }

    int[] lastDC;

    float[][] M = new float[8][8],
            MT = new float[8][8];

    private void buildMTables()
    {
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                if (i == 0)
                {
                    M[i][j] = (float) (1 / Math.sqrt(8));
                    MT[j][i] = M[i][j];
                }
                else
                {
                    M[i][j] = (float) (0.5 * Math.cos((2 * j + 1) * i * Math.PI / 16));
                    MT[j][i] = M[i][j];
                }
            }
        }
    }

    private int[][] IDCT2_M(int[][] G)
    {
        // IDCT(G) = (MT)GM
        float[][] GM = new float[8][8];
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                for (int k = 0; k < 8; k++)
                {
                    GM[i][j] += G[i][k] * M[k][j];
                }
            }
        }

        float[][] MTGM = new float[8][8];

        int[][] P = new int[8][8];

        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                for (int k = 0; k < 8; k++)
                {
                    MTGM[i][j] += MT[i][k] * GM[k][j];
                }
                P[i][j] = (int) (MTGM[i][j] + 0.5);  //rounding
            }
        }

        return P;
    }

    private void buildMTables_int()
    {
        for (int i = 0; i < 8; i++)
        {
            for (int j = 0; j < 8; j++)
            {
                if (i == 0)
                {
                    M_int[i][j] = (int) ((1 / Math.sqrt(8)) * (1 << 11));    //M_int_ij = X * 2^11
                    MT_int[j][i] = M_int[i][j];
                }
                else
                {
                    M_int[i][j] = (int) ((0.5 * Math.cos((2 * j + 1) * i * Math.PI / 16)) * (1 << 11));
                    MT_int[j][i] = M_int[i][j];
                }
            }
        }
    }

    float cos1 = (float) (Math.sqrt(2) * Math.cos(1.0 * Math.PI / 16)),
            cos2 = (float) (Math.sqrt(2) * Math.cos(2.0 * Math.PI / 16)),
            cos3 = (float) (Math.sqrt(2) * Math.cos(3.0 * Math.PI / 16)),
            cos4 = (float) (Math.sqrt(2) * Math.cos(4.0 * Math.PI / 16)),
            cos5 = (float) (Math.sqrt(2) * Math.cos(5.0 * Math.PI / 16)),
            cos6 = (float) (Math.sqrt(2) * Math.cos(6.0 * Math.PI / 16)),
            cos7 = (float) (Math.sqrt(2) * Math.cos(7.0 * Math.PI / 16)),
            sqrt2_2 = (float) (1.0 / Math.sqrt(2.0));

    int[][] invDCT2(int[][] input)
    {
        int[][] output = new int[8][8];

        float a0, a1, a2, a3, a4, a5, a6, a7, // A = [a0, a1, a2, a3, a4, a5, a6, a7],
                b0, b1, b2, b3, b4, b5, b6, b7,
                c0, c1, c2, c3, c4, c5, c6, c7,
                d0, d1, d2, d3, d4, d5, d6, d7,
                e0, e1, e2, e3, e4, e5, e6, e7,
                f0, f1, f2, f3, f4, f5, f6, f7;

        float[][] temp = new float[8][8]; // input x M;

        // IDCT(X) = 1/8 MT * X * M
        //      temp = X * M => IDCT(X) = 1/8 MT * temp
        //      BT * AT = (AB)T (T = taranahade)
        // IDCT(X) = MT * temp = ((temp)T * M)T

        for (int row = 0; row < 8; row++)
        {
            a0 = input[row][0];
            a1 = input[row][4];
            a2 = input[row][2];
            a3 = input[row][6];
            a4 = input[row][1];
            a5 = input[row][5];
            a6 = input[row][3];
            a7 = input[row][7];

            b0 = a0 + a1;
            b1 = a0 - a1;
            b2 = a2 * cos6 - a3 * cos2;
            b3 = a2 * cos2 + a3 * cos6;
            b4 = a4 * cos7 - a7 * cos1;
            b5 = a5 * cos3 - a6 * cos5;
            b6 = a5 * cos5 + a6 * cos3;
            b7 = a4 * cos1 + a7 * cos7;

            c0 = b0;
            c1 = b1;
            c2 = b2;
            c3 = b3;
            c4 = b4 + b5;
            c5 = b5 - b4;
            c6 = b7 - b6;
            c7 = b6 + b7;

            d0 = c0;
            d1 = c1;
            d2 = c2;
            d3 = c3;
            d4 = c4;
            d5 = c5 + c6;
            d6 = c6 - c5;
            d7 = c7;

            e0 = d0;
            e1 = d1;
            e2 = d2;
            e3 = d3;
            e4 = d4;
            e5 = d5 * sqrt2_2;
            e6 = d6 * sqrt2_2;
            e7 = d7;

            f0 = e0 + e3;
            f1 = e1 + e2;
            f2 = e1 - e2;
            f3 = e0 - e3;
            f4 = e4;
            f5 = e5;
            f6 = e6;
            f7 = e7;

            temp[row][0] = f0 + f7;
            temp[row][1] = f1 + f6;
            temp[row][2] = f2 + f5;
            temp[row][3] = f3 + f4;
            temp[row][4] = f3 - f4;
            temp[row][5] = f2 - f5;
            temp[row][6] = f1 - f6;
            temp[row][7] = f0 - f7;
        }

        for (int col = 0; col < 8; col++)
        {
            a0 = temp[0][col];
            a1 = temp[4][col];
            a2 = temp[2][col];
            a3 = temp[6][col];
            a4 = temp[1][col];
            a5 = temp[5][col];
            a6 = temp[3][col];
            a7 = temp[7][col];

            b0 = a0 + a1;
            b1 = a0 - a1;
            b2 = a2 * cos6 - a3 * cos2;
            b3 = a2 * cos2 + a3 * cos6;
            b4 = a4 * cos7 - a7 * cos1;
            b5 = a5 * cos3 - a6 * cos5;
            b6 = a5 * cos5 + a6 * cos3;
            b7 = a4 * cos1 + a7 * cos7;

            c0 = b0;
            c1 = b1;
            c2 = b2;
            c3 = b3;
            c4 = b4 + b5;
            c5 = b5 - b4;
            c6 = b7 - b6;
            c7 = b6 + b7;

            d0 = c0;
            d1 = c1;
            d2 = c2;
            d3 = c3;
            d4 = c4;
            d5 = c5 + c6;
            d6 = c6 - c5;
            d7 = c7;

            e0 = d0;
            e1 = d1;
            e2 = d2;
            e3 = d3;
            e4 = d4;
            e5 = d5 * sqrt2_2;
            e6 = d6 * sqrt2_2;
            e7 = d7;

            f0 = e0 + e3;
            f1 = e1 + e2;
            f2 = e1 - e2;
            f3 = e0 - e3;
            f4 = e4;
            f5 = e5;
            f6 = e6;
            f7 = e7;

            output[0][col] = clip((f0 + f7) / 8.0);
            output[1][col] = clip((f1 + f6) / 8.0);
            output[2][col] = clip((f2 + f5) / 8.0);
            output[3][col] = clip((f3 + f4) / 8.0);
            output[4][col] = clip((f3 - f4) / 8.0);
            output[5][col] = clip((f2 - f5) / 8.0);
            output[6][col] = clip((f1 - f6) / 8.0);
            output[7][col] = clip((f0 - f7) / 8.0);
        }
        return output;
    }

    int clip(double a)
    {
        if (a < -128)
        {
            return -128;
        }
        else if (a > 128)
        {
            return 128;
        }
        else
        {
            return (int) (a + 0.5);
        }
    }

    void decodeMCUs(RandomAccessFile br) throws Exception
    {
        resetDecoder();

        int MCU_width = colorComponents.get(0).HorzFreq * 8;    // HFreq_Y * 8 pixels (e.g. 2 * 8 pixel)
        int MCU_height = colorComponents.get(0).VertFreq * 8;   // VFreq_Y * 8 pixels

        int n_hMCUs = (int) Math.ceil((img_width + 0.0) / MCU_width);
        int n_vMCUs = (int) Math.ceil((img_height + 0.0) / MCU_height);

        MCUs = new MCU[n_hMCUs][n_vMCUs];

        image = new BufferedImage(img_width, img_height, BufferedImage.TYPE_3BYTE_BGR);

        for (int mcuy = 0; mcuy < n_vMCUs; mcuy++)
        {
            for (int mcux = 0; mcux < n_hMCUs; mcux++)
            {
                MCUs[mcux][mcuy] = new MCU();

                for (int nComp = 0; nComp < colorComponents.size(); nComp++)
                {
                    for (int vDataUnit = 0; vDataUnit < colorComponents.get(nComp).VertFreq; vDataUnit++)
                    {
                        for (int hDataUnit = 0; hDataUnit < colorComponents.get(nComp).HorzFreq; hDataUnit++)
                        {
                            // DC:
                            int differenceDC = 0;
                            short bits = 0;

                            byte magnitude = decodeNextSymbol(br,
                                    huffmanTablesDC.get(colorComponents.get(nComp).DCTableIndex));
                            bits = readBits(br, magnitude);
                            differenceDC = Extend(bits, magnitude);
                            lastDC[nComp] = lastDC[nComp] + differenceDC;

                            short[] coefs = new short[64];

                            coefs[0] = (short) lastDC[nComp];

                            int i = 1;
                            while (i < 64)
                            {
                                byte value = decodeNextSymbol(br,
                                        huffmanTablesAC.get(colorComponents.get(nComp).ACTableIndex));

                                byte lowBits = (byte) (0x0F & value);
                                byte n = (byte) (0xF0 & value);
                                byte highBits = (byte) ((0xF0 & value) >>> 4);

                                if (lowBits != 0)
                                {
                                    i += highBits;  // number of 0 coefficients before real bytes

                                    bits = readBits(br, lowBits);
                                    if (i < 64)   // ????
                                    {
                                        coefs[i] = Extend(bits, lowBits);
                                    }

                                    i++;
                                }
                                else    // F0 ya 00
                                {
                                    if (highBits == 0xF)
                                    {
                                        i += 16;
                                    }
                                    else if (highBits == 0) // EOB
                                    {
                                        break;
                                    }
                                }
                            }

                            short[][] coefsZigZag = ZigZagTable.convertToZigZagTable(coefs);

                            int[][] deqTable = dequantize(coefsZigZag,
                                    colorComponents.get(nComp).QTableIndex);

                            int[][] idctTable = null;
                            switch (idctFunc)
                            {
                                case 0:
                                    idctTable = invDCT2(deqTable);
                                    break;
                                case 1:
                                    idctTable = IDCT2_M(deqTable);
                                    break;

                            }

                            MCUs[mcux][mcuy].components.add(idctTable);

                        }
                    }
                }

                if (restartInterval != 0 && ++mcuCount % restartInterval == 0)
                {
                    if (mcuCount < n_hMCUs * n_vMCUs)   // any more MCUs? (if not, it's not RST)
                    {
                        resetDCDifference(br);
                    }
                }

                Color[][] rgbTable = upSampleAndConvertToRGB(MCUs[mcux][mcuy]);

                for (int i = 0; i < MCU_width; i++)
                {
                    for (int j = 0; j < MCU_height; j++)
                    {
                        int X = mcux * MCU_width + i,
                                Y = mcuy * MCU_height + j;

                        if (X < img_width && Y < img_height)
                        {
                            int col = toIntARGB(rgbTable[j][i]);
                            image.setRGB(X, Y, col);
                        }
                    }
                }
            }
        }
    }

    private void resetDCDifference(RandomAccessFile br) throws Exception
    {
        lastDC = new int[colorComponents.size()];    // reset lastDC to 0

        bitPointer = -1;
        curByte = 0;

        byte[] marker = new byte[2];
        br.read(marker);
        if (marker[0] != 0xFF || !(marker[1] >= 0xD0 && marker[1] <= 0xD7))
        {
            throw new Exception("Restart Marker Error");
        }

    }

    Color[][] upSampleAndConvertToRGB(MCU mcu)
    {
        Color[][] rgbTable = null;
        switch (chromaSubsampling)
        {
            case "1:0:0":
                rgbTable = convertToRGB(mcu.components.get(0), new int[8][8], new int[8][8]);
                break;
            case "4:4:4":
                rgbTable = convertToRGB(mcu.components.get(0), mcu.components.get(1),
                        mcu.components.get(2));
                break;
            case "4:2:2":
                int[][] YTable = new int[8][16],
                 CbTable = new int[8][16],
                 CrTable = new int[8][16];

                for (int i = 0; i < 8; i++)
                {
                    for (int j = 0; j < 8; j++)
                    {
                        YTable[i][j] = mcu.components.get(0)[i][j];         // Y0
                        YTable[i][j + 8] = mcu.components.get(1)[i][j];       // Y1
                        CbTable[i][2 * j] = CbTable[i][2 * j + 1] = mcu.components.get(2)[i][j];    // Cb
                        CrTable[i][2 * j] = CrTable[i][2 * j + 1] = mcu.components.get(3)[i][j];    // Cr
                    }
                }

                rgbTable = convertToRGB(YTable, CbTable, CrTable);
                break;
            case "4:2:0":
                YTable = new int[16][16];
                CbTable = new int[16][16];
                CrTable = new int[16][16];

                for (int i = 0; i < 8; i++)
                {
                    for (int j = 0; j < 8; j++)
                    {
                        YTable[i][j] = mcu.components.get(0)[i][j];          // Y00
                        YTable[i][j + 8] = mcu.components.get(1)[i][j];      // Y01
                        YTable[i + 8][j] = mcu.components.get(2)[i][j];     // Y10
                        YTable[i + 8][j + 8] = mcu.components.get(3)[i][j]; // Y11
                        CbTable[2 * i][2 * j]
                                = CbTable[2 * i][2 * j + 1]
                                = CbTable[2 * i + 1][2 * j]
                                = CbTable[2 * i + 1][2 * j + 1] = mcu.components.get(4)[i][j];    // Cb

                        CrTable[2 * i][2 * j]
                                = CrTable[2 * i][2 * j + 1]
                                = CrTable[2 * i + 1][2 * j]
                                = CrTable[2 * i + 1][2 * j + 1] = mcu.components.get(5)[i][j];    // Cr

                    }
                }

                rgbTable = convertToRGB(YTable, CbTable, CrTable);
                break;
            /*case "4:1:1":

             break;*/           
        }

        return rgbTable;
    }
    
    void resetDecoder()
    {
        curByte = 0;
        bitPointer = -1;
        lastDC = new int[colorComponents.size()];
        unableToDecode = false;
        mcuCount = 0;
    }

    int toIntARGB(Color clr)
    {
        return (clr.getRed() << 16) | (clr.getGreen() << 8) | clr.getBlue();
    }

    public Image getMCUInfo(int X, int Y)
    {
        if (image != null)
        {
            int MCU_width = colorComponents.get(0).HorzFreq * 8;    // HFreq_Y * 8 pixels (e.g. 2 * 8 pixels)
            int MCU_height = colorComponents.get(0).VertFreq * 8;   // VFreq_Y * 8 pixels

            int mcux = X / MCU_width;
            int mcuy = Y / MCU_height;

            logSB = new StringBuilder();
            logSB.append("MCU[").append(mcux).append(", ").append(mcuy).append("]\n\n");

            MCU mcu = MCUs[mcux][mcuy];

            for (int i = 0; i < mcu.dataUnits.size(); i++)
            {
                logSB.append("\n\nComponent: ").append(i).append("\nDequantized Values:\n");

                for (int ii = 0; ii < 8; ii++)
                {
                    for (int jj = 0; jj < 8; jj++)
                    {
                        logSB.append(String.format("%5d", mcu.dataUnits.get(i)[ii][jj]));
                    }
                    logSB.append("\n\n");
                }

                logSB.append("\n\nIDCT Values:\n");
                for (int ii = 0; ii < 8; ii++)
                {
                    for (int jj = 0; jj < 8; jj++)
                    {
                        logSB.append(String.format("%5d", mcu.components.get(i)[ii][jj]));
                    }

                    logSB.append("\n\n");
                }
            }
            Color[][] rgbTable = upSampleAndConvertToRGB(mcu);

            BufferedImage bmp = new BufferedImage(MCU_width * 16, MCU_height * 16,
                    BufferedImage.TYPE_3BYTE_BGR);

            Graphics g = bmp.getGraphics();

            for (int i = 0; i < MCU_width; i++)
            {
                for (int j = 0; j < MCU_height; j++)
                {
                    g.setColor(rgbTable[j][i]);

                    g.fillRect(i * 16, j * 16, i * 16 + 16, j * 16 + 16);

                    g.setColor(Color.BLACK);
                    g.drawRect(i * 16, j * 16, i * 16 + 16, j * 16 + 16);
                }
            }

            g.setColor(Color.BLACK);
            g.drawRect(0, 0, MCU_width * 16 - 1, MCU_height * 16 - 1);

            g.dispose();
            return bmp;

        }

        return null;
    }

    StringBuilder logSB;

    public String getLastLog()
    {
        if (logSB != null)
        {
            return logSB.toString();
        }
        else
        {
            return "";
        }
    }
}
