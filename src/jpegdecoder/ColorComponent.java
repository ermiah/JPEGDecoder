/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

/**
 * @author Nosrati
 */
public class ColorComponent {

    private String name;

    private int index;

    private int horzFreq;

    private int vertfreq;

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

    public int getVertfreq() {
        return vertfreq;
    }

    public void setVertfreq(int vertfreq) {
        this.vertfreq = vertfreq;
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
