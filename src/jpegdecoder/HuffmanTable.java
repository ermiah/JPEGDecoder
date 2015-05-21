/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jpegdecoder;

import java.util.LinkedHashMap;

/**
 *
 * @author Nosrati
 */
class HuffmanTable
{   
    public LinkedHashMap<Integer, Byte> HuffmanCodes;

    //TODO
    public boolean getSymbol(short code, short len, OutByte sym)
    {
        sym.data = 0;

        int length_code = (int) ((len << 16) | code);

        if (HuffmanCodes.containsKey(length_code))
        {
            sym.data = HuffmanCodes.get(length_code);
            return true;
        }
        return false;
    }
}
