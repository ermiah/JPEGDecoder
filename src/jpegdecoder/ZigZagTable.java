package jpegdecoder;

/**
 * @author Nosrati
 */
public class ZigZagTable {
    public static short[][] convertToZigZagTable(short[] A) {
        int rows = (short) Math.sqrt(A.length);

        short[][] B = new short[rows][rows];
        int i = 0, j = 0;
        boolean topRight = true;

        for (int n = 0; n < A.length; n++) {
            B[i][j] = A[n];

            if (topRight) {
                i--;
                j++;
            } else {
                i++;
                j--;
            }

            if (i < 0)  // we've hit the left wall
            {
                i = 0;
                topRight = false;
            }
            if (i > rows - 1)   // the bottom wall
            {
                i = rows - 1;
                topRight = true;
                j += 2; // the bottom triangle
            }
            if (j < 0) {
                j = 0;
                topRight = true;
            }
            if (j > rows - 1) {
                j = rows - 1;
                topRight = false;
                i += 2; // the bottom triangle
            }
        }

        return B;
    }
}
