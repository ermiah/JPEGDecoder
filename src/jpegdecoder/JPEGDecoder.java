package jpegdecoder;

import jpegdecoder.segments.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author Nosrati
 */
public class JPEGDecoder {

    public String mode = "";
    int bitPointer = -1;    // not started yet
    byte curByte = 0;
    BufferedImage image;

    List<JpegSegment> segments = new ArrayList<>();

    String chromaSubsampling = "";
    SOSSegment currentSos = null;

    int restartInterval = 0;

    List<ColorComponent> colorComponents = new ArrayList<>();
    List<QuantizationTable> qTables = new ArrayList<>();
    Map<Byte, HuffmanTable> huffmanTablesDC = new HashMap<>();
    Map<Byte, HuffmanTable> huffmanTablesAC = new HashMap<>();


    int img_width;
    int img_height;

    MCU[][] MCUs;

    boolean unableToDecode = false;
    int mcuCount;
    int[] lastDC;

    StringBuilder logSB;
    IDCT idct = new IDCT();

    public JPEGDecoder() {
    }

    Color[][] convertToRGB(int[][] Y, int[][] Cb, int[][] Cr) {
        int n = Y.length,
                m = Y[0].length;

        Color[][] rgbBlock = new Color[n][m];

        int r, g, b;
        int y, cr, cb;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
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

    public void decode(File f) throws Exception {
        RandomAccessFile br = new RandomAccessFile(f, "r");
        logSB = new StringBuilder();

        JpegSegment jpegSegment = readSegment(br);
        logSB.append(jpegSegment.formattedOutput());
        logSB.append(jpegSegment.rawBytes());
        logSB.append("-----------------------------\n");
        segments.add(jpegSegment);

        while (jpegSegment != null && !(jpegSegment instanceof EOISegment)) {
            jpegSegment = readSegment(br);
            segments.add(jpegSegment);

            String str = jpegSegment.formattedOutput();
            logSB.append(str);
            logSB.append(jpegSegment.rawBytes());
            logSB.append("-----------------------------\n");

            Logger.getLogger(getClass().getSimpleName()).info(str);
        }
    }

    JpegSegment readSegment(RandomAccessFile br) throws Exception {
        byte[] marker = new byte[2];
        br.read(marker);

        if (marker[0] != (byte) 0xFF) {
            throw new Exception("Not a valid marker");
        }

        JpegSegment jpegSegment = null;

        switch (marker[1]) {
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

                qTables.addAll(((DQTSegment) jpegSegment).getqTables());

                break;

            case (byte) 0xC4:  // DHT
                jpegSegment = new DHTSegment(br, (byte) 0xC4);

                huffmanTablesDC.putAll(((DHTSegment) jpegSegment).getHuffmanTablesDC());
                huffmanTablesAC.putAll(((DHTSegment) jpegSegment).getHuffmanTablesAC());

                break;

            case (byte) 0xC0:  // SOF0
                jpegSegment = new SOF0Segment(br, (byte) 0xC0);

                colorComponents.addAll(((SOF0Segment) jpegSegment).getColorComponents());

                img_width = ((SOF0Segment) jpegSegment).getImgWidth();
                img_height = ((SOF0Segment) jpegSegment).getImgHeight();

                chromaSubsampling = ((SOF0Segment) jpegSegment).getChromaSubsampling();
                mode = "baseline";

                break;

            case (byte) 0xC2:  // Progressive - Not Implemented
                jpegSegment = new SOF0Segment(br, (byte) 0xC0);

                colorComponents.addAll(((SOF0Segment) jpegSegment).getColorComponents());

                img_width = ((SOF0Segment) jpegSegment).getImgWidth();
                img_height = ((SOF0Segment) jpegSegment).getImgHeight();

                chromaSubsampling = ((SOF0Segment) jpegSegment).getChromaSubsampling();
                mode = "Progressive";

//                unableToDecode = true;
                break;
            case (byte) 0xC1:  // Sequential - Not Implemented
            case (byte) 0xC3:  // Lossless - Not Implemented
                jpegSegment = new SOFxSegment(br, marker[1]);

                unableToDecode = true;
                image = null;
                mode = ((SOFxSegment) jpegSegment).getMode();

                break;
            case (byte) 0xDD:  // DRI

                jpegSegment = new DRISegment(br, marker[1]);
                restartInterval = ((DRISegment) jpegSegment).getRestartInterval();

                break;

            case (byte) 0xDA:  // SOS
                jpegSegment = new SOSSegment(br, marker[1], colorComponents);
                currentSos = (SOSSegment) jpegSegment;

                if (unableToDecode) {
                    dumpCompressedData(br);
                } else {
                    if (mode.equals("") || mode.equals("baseline")) {
                        decodeMCUs(br);
                    } else if (mode.equals("Progressive")) {
                        if (currentSos.getSpectralStart() == 0 && currentSos.getSpectralEnd() == 0 &&
                                (currentSos.getSuccessiveApproximation() & 0xF0) == 0) {
                            // First DC Scan:
                            decodeMCUs(br);
                        } else {
                            dumpCompressedData(br);
                        }
                    }
                }

                break;
            default:
                jpegSegment = null;

                break;
        }

        return jpegSegment;
    }

    private void dumpCompressedData(RandomAccessFile br) throws IOException {
        boolean endOfSeg = false;

        var scan = new ArrayList<Byte>();
        scan.add((byte) 0xFF);
        scan.add((byte) 0xDA);
        byte b;
        while (!endOfSeg) {
            b = br.readByte();
            if (b == (byte) 0xFF) {
                byte next = br.readByte();
                br.seek(br.getFilePointer() - 1);

                if (next != (byte) 0x00) {
                    if (!(next >= (byte) 0xD0 && next <= (byte) 0xD7))    // RSTn
                    {               // not RSTn: end of segment
                        //Byte[] compressedData = new Byte[scan.size()];
                        //scan.toArray(compressedData);

                        logSB.append("<Compressed Data>\n").
                                //append(ByteArray.formatBytes(compressedData)).
                                        append("-------------------").
                                append("\n\n");

                        br.seek(br.getFilePointer() - 1);
                        endOfSeg = true;
                    } else {
                        scan.add(b);
                    }
                } else {
                    scan.add(b);
                }
            } else {
                scan.add(b);
            }
        }
    }

    int[][] dequantize(short[][] dataUnit, int qTableIndex) {
        short[][] qTable = qTables.get(qTableIndex).getArray();
        int[][] result = new int[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                result[i][j] = qTable[i][j] * dataUnit[i][j];
            }
        }
        return result;
    }

    short readBit(RandomAccessFile br) throws IOException {
        if (bitPointer == -1)   // we have finished the previous byte, let's start the new one
        {
            curByte = br.readByte();

            if (curByte == (byte) 0xFF) {
                /*int nextByte = */
                br.readByte();

//                if (nextByte == 0)   // 0xFF00 ==> 0xFF
//                {
//                }
            }

            bitPointer = 7;
        }

        if (((1 << bitPointer--) & curByte) != 0) {
            return 1;
        } else {
            return 0;
        }
    }

    short readBits(RandomAccessFile br, int n) throws IOException {
        short bits = 0;
        for (int i = 0; i < n; i++) {
            bits = (short) ((bits << 1) | readBit(br));
        }

        return bits;
    }

    short extend(short bits, byte size) {
        short v = (short) (1 << size - 1);     // for 5 bits, v = 10000

        // if it's negative, we find -max (where max is the largest positive
        // number which can be made using "size" bits.) and we add it to "bits".
        // for example, if size = 5, the largest positive 5 bits number is
        // max = (11111)2 = 31.
        // -max is calculated by left shifting -1, size times, and adding 1.
        if (bits < v)   // "bits" manfie, chon bite akharesh 1 nist
        {
            return (short) ((-1 << size) + 1 + bits);   // difference = -max + bits
        } else {
            return bits;    // difference = bits
        }
    }

    byte decodeNextSymbol(RandomAccessFile br, HuffmanTable ht) throws Exception {
        short code = readBit(br);
        short len = 1;
        OutByte sym = new OutByte();

        while (!ht.getSymbol(code, len, sym)) {
            code = (short) ((code << 1) | readBit(br));
            len++;

            if (len > 16) {
                throw new Exception("Invalid Huffman Code");
            }
        }
        return sym.getData();

    }

    void decodeMCUs(RandomAccessFile br) throws Exception {
        resetDecoder();

        int MCU_width = colorComponents.get(0).getHorzFreq() * 8;    // HFreq_Y * 8 pixels (e.g. 2 * 8 pixel)
        int MCU_height = colorComponents.get(0).getVertFreq() * 8;   // VFreq_Y * 8 pixels

        int n_hMCUs = (int) Math.ceil((img_width + 0.0) / MCU_width);
        int n_vMCUs = (int) Math.ceil((img_height + 0.0) / MCU_height);

        MCUs = new MCU[n_hMCUs][n_vMCUs];

        image = new BufferedImage(img_width, img_height, BufferedImage.TYPE_3BYTE_BGR);

        for (int mcuy = 0; mcuy < n_vMCUs; mcuy++) {
            for (int mcux = 0; mcux < n_hMCUs; mcux++) {
                MCUs[mcux][mcuy] = new MCU();

                for (int nComp = 0; nComp < colorComponents.size(); nComp++) {
                    for (int vDataUnit = 0; vDataUnit < colorComponents.get(nComp).getVertFreq(); vDataUnit++) {
                        for (int hDataUnit = 0; hDataUnit < colorComponents.get(nComp).getHorzFreq(); hDataUnit++) {
                            decodeMCU(br, MCUs[mcux][mcuy], nComp);
                        }
                    }
                }

                if (restartInterval != 0 && ++mcuCount % restartInterval == 0) {
                    if (mcuCount < n_hMCUs * n_vMCUs) {  // any more MCUs? (if not, it's not RST)
                        resetDCDifference(br);
                    }
                }

                drawMCU(MCU_width, MCU_height, mcuy, mcux);
            }
        }
    }

    private void decodeMCU(RandomAccessFile br, MCU mcUs, int nComp) throws Exception {
        short[] coefs = new short[64];

        if (mode.equals("") || mode.equals("baseline")) {
            findDCCoefficient(br, nComp, coefs);
            findACCoefficients(br, nComp, coefs);
        }
        if (mode.equals("Progressive")) {
            findDCCoefficient(br, nComp, coefs);
            for (int i = 1; i < 64; i++) coefs[i] = 0;
        }

        // If we wanted to see how our image would look like if there were no AC coefficents:
        // for (int i = 1; i < 64; i++) coefs[i] = 0;

        short[][] coefsZigZag = ZigZagTable.convertToZigZagTable(coefs);
        int[][] deqTable = dequantize(coefsZigZag, colorComponents.get(nComp).getqTableIndex());
        int[][] idctTable = idct.invDCT(deqTable);

        mcUs.getComponents().add(idctTable);
    }

    private void findACCoefficients(RandomAccessFile br, int nComp, short[] coefs) throws Exception {
        int i = 1;
        while (i < 64) {
            byte value = decodeNextSymbol(br,
                    huffmanTablesAC.get(colorComponents.get(nComp).getActableindex()));

            byte lowBits = (byte) (0x0F & value);
            byte n = (byte) (0xF0 & value);
            byte highBits = (byte) ((0xF0 & value) >>> 4);

            if (lowBits != 0) {
                i += highBits;  // number of 0 coefficients before real bytes

                short bits = readBits(br, lowBits);
                if (i < 64) {  // TODO ????
                    coefs[i] = extend(bits, lowBits);
                }

                i++;
            } else {   // F0 or 00
                if (highBits == 0xF) {
                    i += 16;
                } else if (highBits == 0) { // EOB

                    break;
                }
            }
        }
    }

    private void findDCCoefficient(RandomAccessFile br, int nComp, short[] coefs) throws Exception {
        // DC:
        int dcDifference = 0;
        short bits = 0;

        byte bitCount = decodeNextSymbol(br,
                huffmanTablesDC.get(colorComponents.get(nComp).getDctableindex()));
        bits = readBits(br, bitCount);
        dcDifference = extend(bits, bitCount);
        lastDC[nComp] = lastDC[nComp] + dcDifference;
        coefs[0] = (short) lastDC[nComp];

        if (mode.equals("Progressive")) {
            coefs[0] <<= currentSos.getSuccessiveApproximation();
        }
    }

    private void drawMCU(int MCU_width, int MCU_height, int mcuy, int mcux) {
        Color[][] rgbTable = upSampleAndConvertToRGB(MCUs[mcux][mcuy]);

        for (int i = 0; i < MCU_width; i++) {
            for (int j = 0; j < MCU_height; j++) {
                int x = mcux * MCU_width + i;
                int y = mcuy * MCU_height + j;

                if (x < img_width && y < img_height) {
                    int col = toIntARGB(rgbTable[j][i]);
                    image.setRGB(x, y, col);
                }
            }
        }
    }

    private void resetDCDifference(RandomAccessFile br) throws Exception {
        lastDC = new int[colorComponents.size()];    // reset lastDC to 0

        bitPointer = -1;
        curByte = 0;

        byte[] marker = new byte[2];
        br.read(marker);
        if (marker[0] != 0xFF || !(marker[1] >= 0xD0 && marker[1] <= 0xD7)) {
            throw new Exception("Restart Marker Error");
        }

    }

    Color[][] upSampleAndConvertToRGB(MCU mcu) {
        Color[][] rgbTable = null;
        switch (chromaSubsampling) {
            case "1:0:0":
                rgbTable = convertToRGB(mcu.getComponents().get(0), new int[8][8], new int[8][8]);
                break;
            case "4:4:4":
                rgbTable = convertToRGB(mcu.getComponents().get(0), mcu.getComponents().get(1),
                        mcu.getComponents().get(2));
                break;
            case "4:2:2":
                int[][] YTable = new int[8][16],
                        CbTable = new int[8][16],
                        CrTable = new int[8][16];

                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        YTable[i][j] = mcu.getComponents().get(0)[i][j];         // Y0
                        YTable[i][j + 8] = mcu.getComponents().get(1)[i][j];       // Y1
                        CbTable[i][2 * j] = CbTable[i][2 * j + 1] = mcu.getComponents().get(2)[i][j];    // Cb
                        CrTable[i][2 * j] = CrTable[i][2 * j + 1] = mcu.getComponents().get(3)[i][j];    // Cr
                    }
                }

                rgbTable = convertToRGB(YTable, CbTable, CrTable);
                break;
            case "4:2:0":
                YTable = new int[16][16];
                CbTable = new int[16][16];
                CrTable = new int[16][16];

                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        YTable[i][j] = mcu.getComponents().get(0)[i][j];          // Y00
                        YTable[i][j + 8] = mcu.getComponents().get(1)[i][j];      // Y01
                        YTable[i + 8][j] = mcu.getComponents().get(2)[i][j];     // Y10
                        YTable[i + 8][j + 8] = mcu.getComponents().get(3)[i][j]; // Y11
                        CbTable[2 * i][2 * j]
                                = CbTable[2 * i][2 * j + 1]
                                = CbTable[2 * i + 1][2 * j]
                                = CbTable[2 * i + 1][2 * j + 1] = mcu.getComponents().get(4)[i][j];    // Cb

                        CrTable[2 * i][2 * j]
                                = CrTable[2 * i][2 * j + 1]
                                = CrTable[2 * i + 1][2 * j]
                                = CrTable[2 * i + 1][2 * j + 1] = mcu.getComponents().get(5)[i][j];    // Cr

                    }
                }

                rgbTable = convertToRGB(YTable, CbTable, CrTable);
                break;
            /*case "4:1:1":

             break;*/
        }

        return rgbTable;
    }

    void resetDecoder() {
        curByte = 0;
        bitPointer = -1;
        lastDC = new int[colorComponents.size()];
        unableToDecode = false;
        mcuCount = 0;
    }

    int toIntARGB(Color clr) {
        return (clr.getRed() << 16) | (clr.getGreen() << 8) | clr.getBlue();
    }

    public Image getMCUInfo(int X, int Y) {
        if (image != null) {
            int MCU_width = colorComponents.get(0).getHorzFreq() * 8;    // HFreq_Y * 8 pixels (e.g. 2 * 8 pixels)
            int MCU_height = colorComponents.get(0).getVertFreq() * 8;   // VFreq_Y * 8 pixels

            int mcux = X / MCU_width;
            int mcuy = Y / MCU_height;

            logSB = new StringBuilder();
            logSB.append("MCU[").append(mcux).append(", ").append(mcuy).append("]\n\n");

            MCU mcu = MCUs[mcux][mcuy];

            for (int i = 0; i < mcu.getDataUnits().size(); i++) {
                logSB.append("\n\nComponent: ").append(i).append("\nDequantized Values:\n");

                for (int ii = 0; ii < 8; ii++) {
                    for (int jj = 0; jj < 8; jj++) {
                        logSB.append(String.format("%5d", mcu.getDataUnits().get(i)[ii][jj]));
                    }
                    logSB.append("\n\n");
                }

                logSB.append("\n\nIDCT Values:\n");
                for (int ii = 0; ii < 8; ii++) {
                    for (int jj = 0; jj < 8; jj++) {
                        logSB.append(String.format("%5d", mcu.getComponents().get(i)[ii][jj]));
                    }

                    logSB.append("\n\n");
                }
            }
            Color[][] rgbTable = upSampleAndConvertToRGB(mcu);

            BufferedImage bmp = new BufferedImage(MCU_width * 16, MCU_height * 16,
                    BufferedImage.TYPE_3BYTE_BGR);

            Graphics g = bmp.getGraphics();

            for (int i = 0; i < MCU_width; i++) {
                for (int j = 0; j < MCU_height; j++) {
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

    public String getLastLog() {
        if (logSB != null) {
            return logSB.toString();
        } else {
            return "";
        }
    }
}
