/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jpegdecoder;

/**
 *
 * @author Nosrati
 */
public class QuantizationTable
{
    private short[][] qTable;
    
    public QuantizationTable(short[][] qt)
    {
        qTable = qt;
    }

    public short[][] getArray()
    {
        return qTable;
    }
}
