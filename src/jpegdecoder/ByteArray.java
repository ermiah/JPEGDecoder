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
public class ByteArray
{
    public static Byte[] toObjects(byte[] input)
    {
        Byte[] output = new Byte[input.length];

        int i = 0;
        for (byte b : input)
        {
            output[i++] = b;
        }

        return output;
    }

    public static byte[] toPrimitives(Byte[] input)
    {
        byte[] output = new byte[input.length];

        int i = 0;
        for (Byte b : input)
        {
            output[i++] = b;
        }

        return output;
    }
    
    public static String formatBytes(byte[] data)
    {
        if (data == null)
            return "";
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0; i < data.length; i++)
        {
            sb.append(String.format("%02X ", data[i]));

            if ((count + 1) % 16 == 0)
            {
                sb.append('\n');
            }
            count++;
        }
        return sb.toString();
    }
    
    public static String formatBytes(Byte[] data)
    {
        if (data == null)
            return "";
        StringBuilder sb = new StringBuilder();
        int count = 0;

        for (int i = 0; i < data.length; i++)
        {
            sb.append(String.format("%02X ", data[i]));

            if ((count + 1) % 16 == 0)
            {
                sb.append('\n');
            }
            count++;
        }
        return sb.toString();
    }
}
