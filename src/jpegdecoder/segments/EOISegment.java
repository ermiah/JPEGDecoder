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
