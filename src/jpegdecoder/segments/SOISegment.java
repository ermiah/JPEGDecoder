package jpegdecoder.segments;

import jpegdecoder.segments.JpegSegment;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Nosrati
 */
public class SOISegment extends JpegSegment {
    public SOISegment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        super(raf, markerSecondByte);
    }

    @Override
    public String formattedOutput() {
        return "SOI\n";
    }

}
