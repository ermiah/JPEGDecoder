/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

/**
 * @author Nosrati
 */
class YCbCr {
    private byte Y;
    private byte Cb;
    private byte Cr;

    public byte getY() {
        return Y;
    }

    public void setY(byte y) {
        Y = y;
    }

    public byte getCb() {
        return Cb;
    }

    public void setCb(byte cb) {
        Cb = cb;
    }

    public byte getCr() {
        return Cr;
    }

    public void setCr(byte cr) {
        Cr = cr;
    }
}
