package jpegdecoder.segments;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Nosrati
 */
public class SOFxSegment extends JpegSegment {
    private String mode;

    public SOFxSegment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        super(raf, markerSecondByte);

        if (getMarkerSB() == (byte) 0xC1) {
            mode = "Sequential";
        } else if (getMarkerSB() == (byte) 0xC2) {
            mode = "Progressive";
        } else if (getMarkerSB() == (byte) 0xC3) {
            mode = "Lossless";
        }
    }

    @Override
    public String formattedOutput() throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("SOF").append(getMarkerSB() & 0x0F).append("\n\n");
        sb.append("Length: ").append(getLen());

        sb.append("\n").append(getMode()).append("\n");

        return sb.toString();
    }

    public String getMode() {
        return mode;
    }
}
