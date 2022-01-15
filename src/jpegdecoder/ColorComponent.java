package jpegdecoder;

/**
 * @author Nosrati
 */
public class ColorComponent {

    private String name;

    private int index;

    private int horzFreq;

    private int vertFreq;

    private byte dctableindex;

    private byte actableindex;

    private byte qTableIndex;


    public ColorComponent(int index) {
        this.setIndex(index);

        if (index == 1) {
            setName("Y");
        } else if (index == 2) {
            setName("Cb");
        } else if (index == 3) {
            setName("Cr");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getHorzFreq() {
        return horzFreq;
    }

    public void setHorzFreq(int horzFreq) {
        this.horzFreq = horzFreq;
    }

    public int getVertFreq() {
        return vertFreq;
    }

    public void setVertFreq(int vertFreq) {
        this.vertFreq = vertFreq;
    }

    public byte getDctableindex() {
        return dctableindex;
    }

    public void setDctableindex(byte dctableindex) {
        this.dctableindex = dctableindex;
    }

    public byte getActableindex() {
        return actableindex;
    }

    public void setActableindex(byte actableindex) {
        this.actableindex = actableindex;
    }

    public byte getqTableIndex() {
        return qTableIndex;
    }

    public void setqTableIndex(byte qTableIndex) {
        this.qTableIndex = qTableIndex;
    }
}
