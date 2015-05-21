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
class ColorComponent
{

    public String Name;

    public int Index;

    public int HorzFreq;

    public int VertFreq;

    public byte DCTableIndex;

    public byte ACTableIndex;

    public byte QTableIndex;


    public ColorComponent(int index)
    {
        Index = index;

        if (index == 1)
        {
            Name = "Y";
        }
        else if (index == 2)
        {
            Name = "Cb";
        }
        else if (index == 3)
        {
            Name = "Cr";
        }
    }

}
