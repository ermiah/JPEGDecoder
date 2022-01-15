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
public class EOISegment extends JpegSegment {

    public EOISegment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        super(raf, markerSecondByte);
    }

    @Override
    public String formattedOutput() throws IOException {
        return "EOI\n";
    }

}
