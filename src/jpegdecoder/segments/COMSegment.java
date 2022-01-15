/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder.segments;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Nosrati
 */
public class COMSegment extends JpegSegment {

    public COMSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        super(raf, markerSecondByte);
    }

    @Override
    public String formattedOutput() throws IOException {
        StringBuilder sb = new StringBuilder();
        int readBytes = 0;

        if (getMarkerSB() == 0xFE) {
            sb.insert(0, "COM\n");
        } else {
            sb.insert(0, "APP" + (getMarkerSB() & 0x0F)).append("\n\n");
        }

        // read the string:
        byte b = getBr().readByte();
        readBytes++;

        sb.append((char) b);

        while (b != (byte) '\0' && readBytes < getLen() - 2)    // 2 bytes: len
        {
            b = getBr().readByte();
            readBytes++;

            sb.append((char) b);
        }

        sb.append('\n').append("Length: ").append(getLen()).append("\n\n");

        return sb.toString();
    }
}
