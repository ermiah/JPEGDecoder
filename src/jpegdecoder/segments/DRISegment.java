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
public class DRISegment extends JpegSegment {

    private int restartInterval;

    public DRISegment(RandomAccessFile raf, byte markerSecondByte) throws IOException {
        super(raf, markerSecondByte);

        setRestartInterval((short) getBr().readUnsignedShort());
    }

    @Override
    public String formattedOutput() throws IOException {
        return "DRI Length: " +
                getLen() +
                "\nRestart Interval (Number of MCUs before resetting the DC difference): " +
                getRestartInterval();
    }

    public int getRestartInterval() {
        return restartInterval;
    }

    public void setRestartInterval(int restartInterval) {
        this.restartInterval = restartInterval;
    }
}
