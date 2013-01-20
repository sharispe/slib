package slib.sglib.io.loader.csv;

import slib.sglib.model.graph.elements.type.VType;

/**
 *
 * @author seb
 */
public class CSV_Mapping {

    int id;
    String prefix;
    VType type;

    /**
     *
     * @param id
     * @param type
     * @param prefix
     */
    public CSV_Mapping(int id, VType type, String prefix) {
        this.id = id;
        this.type = type;
        this.prefix = prefix;
    }
}