package jpegdecoder;

/**
 * @author Nosrati
 */
public class QuantizationTable {
    private final short[][] qTable;

    public QuantizationTable(short[][] qt) {
        qTable = qt;
    }

    public short[][] getArray() {
        return qTable;
    }
}
