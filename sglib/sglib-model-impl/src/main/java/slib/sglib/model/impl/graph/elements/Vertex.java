package slib.sglib.model.impl.graph.elements;

import org.openrdf.model.Value;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;


/**
 * @author Harispe Sébastien
 */
public class Vertex implements V {

    /**
     * The value associated to the vertex
     */
    protected Value value;
    
    /**
     * The type associated to the vertex
     */
    protected VType type;

    /**
     * @param v
     * @param type
     */
    public Vertex(Value v, VType type) {
        this.value = v;

        if (type == null) {
            type = VType.UNDEFINED;
        }
        this.type = type;
    }


    @Override
    public VType getType() {
        return type;
    }

    /**
     * If the specified type is null Undefined is used
     * @param type 
     */
    @Override
    public void setType(VType type) {

        if (type == null) {
            type = VType.UNDEFINED;
        }

        this.type = type;
    }

    @Override
    public Value getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Vertex other = (Vertex) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return value.stringValue();
    }
}
