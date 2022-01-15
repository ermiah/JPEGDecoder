package jpegdecoder;

public class IDCT {
    private float[][] M = new float[8][8],
            MT = new float[8][8];
    private float cos1 = (float) (Math.sqrt(2) * Math.cos(1.0 * Math.PI / 16)),
            cos2 = (float) (Math.sqrt(2) * Math.cos(2.0 * Math.PI / 16)),
            cos3 = (float) (Math.sqrt(2) * Math.cos(3.0 * Math.PI / 16)),
            cos4 = (float) (Math.sqrt(2) * Math.cos(4.0 * Math.PI / 16)),
            cos5 = (float) (Math.sqrt(2) * Math.cos(5.0 * Math.PI / 16)),
            cos6 = (float) (Math.sqrt(2) * Math.cos(6.0 * Math.PI / 16)),
            cos7 = (float) (Math.sqrt(2) * Math.cos(7.0 * Math.PI / 16)),
            sqrt2_2 = (float) (1.0 / Math.sqrt(2.0));

    private int[][] M_int = new int[8][8];
    private int[][] MT_int = new int[8][8];

//    private int idctFunc;

    public IDCT() {
        buildMTables();
        buildMTables_int();
    }

    public int[][] invDCT(int[][] matrix) {
        // old code:
//        switch (idctFunc) {
//            case 0:
//                return invDCT2(matrix);
//            case 1:
//                return IDCT2_M(matrix);
//        }

        return invDCT2(matrix);
    }

    private void buildMTables() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0) {
                    M[i][j] = (float) (1 / Math.sqrt(8));
                    MT[j][i] = M[i][j];
                } else {
                    M[i][j] = (float) (0.5 * Math.cos((2 * j + 1) * i * Math.PI / 16));
                    MT[j][i] = M[i][j];
                }
            }
        }
    }

    private int[][] IDCT2_M(int[][] G) {
        // IDCT(G) = (MT)GM
        float[][] GM = new float[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    GM[i][j] += G[i][k] * M[k][j];
                }
            }
        }

        float[][] MTGM = new float[8][8];

        int[][] P = new int[8][8];

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                for (int k = 0; k < 8; k++) {
                    MTGM[i][j] += MT[i][k] * GM[k][j];
                }
                P[i][j] = (int) (MTGM[i][j] + 0.5);  //rounding
            }
        }

        return P;
    }

    private void buildMTables_int() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (i == 0) {
                    M_int[i][j] = (int) ((1 / Math.sqrt(8)) * (1 << 11));    //M_int_ij = X * 2^11
                    MT_int[j][i] = M_int[i][j];
                } else {
                    M_int[i][j] = (int) ((0.5 * Math.cos((2 * j + 1) * i * Math.PI / 16)) * (1 << 11));
                    MT_int[j][i] = M_int[i][j];
                }
            }
        }
    }

    private int[][] invDCT2(int[][] input) {
        int[][] output = new int[8][8];

        float a0, a1, a2, a3, a4, a5, a6, a7, // A = [a0, a1, a2, a3, a4, a5, a6, a7],
                b0, b1, b2, b3, b4, b5, b6, b7,
                c0, c1, c2, c3, c4, c5, c6, c7,
                d0, d1, d2, d3, d4, d5, d6, d7,
                e0, e1, e2, e3, e4, e5, e6, e7,
                f0, f1, f2, f3, f4, f5, f6, f7;

        float[][] temp = new float[8][8]; // input x M;

        // IDCT(X) = 1/8 MT * X * M
        //      temp = X * M => IDCT(X) = 1/8 MT * temp
        //      BT * AT = (AB)T (T = taranahade)
        // IDCT(X) = MT * temp = ((temp)T * M)T

        for (int row = 0; row < 8; row++) {
            a0 = input[row][0];
            a1 = input[row][4];
            a2 = input[row][2];
            a3 = input[row][6];
            a4 = input[row][1];
            a5 = input[row][5];
            a6 = input[row][3];
            a7 = input[row][7];

            b0 = a0 + a1;
            b1 = a0 - a1;
            b2 = a2 * cos6 - a3 * cos2;
            b3 = a2 * cos2 + a3 * cos6;
            b4 = a4 * cos7 - a7 * cos1;
            b5 = a5 * cos3 - a6 * cos5;
            b6 = a5 * cos5 + a6 * cos3;
            b7 = a4 * cos1 + a7 * cos7;

            c0 = b0;
            c1 = b1;
            c2 = b2;
            c3 = b3;
            c4 = b4 + b5;
            c5 = b5 - b4;
            c6 = b7 - b6;
            c7 = b6 + b7;

            d0 = c0;
            d1 = c1;
            d2 = c2;
            d3 = c3;
            d4 = c4;
            d5 = c5 + c6;
            d6 = c6 - c5;
            d7 = c7;

            e0 = d0;
            e1 = d1;
            e2 = d2;
            e3 = d3;
            e4 = d4;
            e5 = d5 * sqrt2_2;
            e6 = d6 * sqrt2_2;
            e7 = d7;

            f0 = e0 + e3;
            f1 = e1 + e2;
            f2 = e1 - e2;
            f3 = e0 - e3;
            f4 = e4;
            f5 = e5;
            f6 = e6;
            f7 = e7;

            temp[row][0] = f0 + f7;
            temp[row][1] = f1 + f6;
            temp[row][2] = f2 + f5;
            temp[row][3] = f3 + f4;
            temp[row][4] = f3 - f4;
            temp[row][5] = f2 - f5;
            temp[row][6] = f1 - f6;
            temp[row][7] = f0 - f7;
        }

        for (int col = 0; col < 8; col++) {
            a0 = temp[0][col];
            a1 = temp[4][col];
            a2 = temp[2][col];
            a3 = temp[6][col];
            a4 = temp[1][col];
            a5 = temp[5][col];
            a6 = temp[3][col];
            a7 = temp[7][col];

            b0 = a0 + a1;
            b1 = a0 - a1;
            b2 = a2 * cos6 - a3 * cos2;
            b3 = a2 * cos2 + a3 * cos6;
            b4 = a4 * cos7 - a7 * cos1;
            b5 = a5 * cos3 - a6 * cos5;
            b6 = a5 * cos5 + a6 * cos3;
            b7 = a4 * cos1 + a7 * cos7;

            c0 = b0;
            c1 = b1;
            c2 = b2;
            c3 = b3;
            c4 = b4 + b5;
            c5 = b5 - b4;
            c6 = b7 - b6;
            c7 = b6 + b7;

            d0 = c0;
            d1 = c1;
            d2 = c2;
            d3 = c3;
            d4 = c4;
            d5 = c5 + c6;
            d6 = c6 - c5;
            d7 = c7;

            e0 = d0;
            e1 = d1;
            e2 = d2;
            e3 = d3;
            e4 = d4;
            e5 = d5 * sqrt2_2;
            e6 = d6 * sqrt2_2;
            e7 = d7;

            f0 = e0 + e3;
            f1 = e1 + e2;
            f2 = e1 - e2;
            f3 = e0 - e3;
            f4 = e4;
            f5 = e5;
            f6 = e6;
            f7 = e7;

            output[0][col] = clip((f0 + f7) / 8.0);
            output[1][col] = clip((f1 + f6) / 8.0);
            output[2][col] = clip((f2 + f5) / 8.0);
            output[3][col] = clip((f3 + f4) / 8.0);
            output[4][col] = clip((f3 - f4) / 8.0);
            output[5][col] = clip((f2 - f5) / 8.0);
            output[6][col] = clip((f1 - f6) / 8.0);
            output[7][col] = clip((f0 - f7) / 8.0);
        }
        return output;
    }

    private int clip(double a) {
        if (a < -128) {
            return -128;
        } else if (a > 128) {
            return 128;
        } else {
            return (int) (a + 0.5);
        }
    }
}
