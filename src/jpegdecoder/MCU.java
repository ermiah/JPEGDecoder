/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
