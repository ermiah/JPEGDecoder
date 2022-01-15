package jpegdecoder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nosrati
 */
public class MCU {

    private final List<int[][]> dataUnits = new ArrayList<>();

    private final List<int[][]> components = new ArrayList<>();

    public List<int[][]> getDataUnits() {
        return dataUnits;
    }

    public List<int[][]> getComponents() {
        return components;
    }
}
