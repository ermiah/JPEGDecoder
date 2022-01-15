/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder.segments;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * @author Nosrati
 */
public class App0Segment extends JpegSegment {

    public App0Segment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        super(raf, markerSecondByte);
    }

    @Override
    public String formattedOutput() throws IOException {
        byte[] identifier = new byte[5];

        getBr().read(identifier);

        String sIdentifier = new String(identifier, StandardCharsets.US_ASCII).substring(0, 4); // JFIF\0

        byte verMaj = getBr().readByte();
        byte verMin = getBr().readByte();
        byte units = getBr().readByte();
        short xdens = (short) getBr().readUnsignedShort();
        short ydens = (short) getBr().readUnsignedShort();
        byte xthum = getBr().readByte();
        byte ythum = getBr().readByte();

        String unitsStr = "";
        if (units == 0) {
            unitsStr = "NONE";
        } else if (units == 1) {
            unitsStr = "Pixels/Inch";
        } else if (units == 2) {
            unitsStr = "Pixels/cm";
        }

        return "APP0\n\n" +
                "Length: " + getLen() +
                '\n' + "Identifier: " + sIdentifier +
                '\n' + "Version: " +
                verMaj + ".0" + verMin +
                '\n' + "Units: " + unitsStr +
                '\n' + "X Density: " + xdens +
                '\n' + "Y Density: " + ydens +
                '\n' + "Thumbnail Width: " + xthum +
                '\n' + "Thumbnail Height: " + ythum + "\n";
    }
}
