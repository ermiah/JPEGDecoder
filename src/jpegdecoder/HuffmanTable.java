/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.util.Map;

/**
 * @author Nosrati
 */
public class HuffmanTable {
    private Map<Integer, Byte> huffmanCodes;

    public boolean getSymbol(short code, short len, OutByte sym) {
        sym.setData((byte)0);

        int lengthCode = (len << 16) | code;

        if (getHuffmanCodes().containsKey(lengthCode)) {
            sym.setData(getHuffmanCodes().get(lengthCode));
            return true;
        }
        return false;
    }

    public Map<Integer, Byte> getHuffmanCodes() {
        return huffmanCodes;
    }

    public void setHuffmanCodes(Map<Integer, Byte> huffmanCodes) {
        this.huffmanCodes = huffmanCodes;
    }
}
