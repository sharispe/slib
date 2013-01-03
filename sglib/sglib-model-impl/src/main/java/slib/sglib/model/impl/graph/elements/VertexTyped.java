package slib.sglib.model.impl.graph.elements;

import org.openrdf.model.Value;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.utils.impl.OProperty;


public class VertexTyped extends OProperty implements V {

    protected G g;
    protected Value value;
    protected VType type;

    public VertexTyped(G g, Value v, VType type) {
        this.g = g;
        this.value = v;

        if (type == null) {
            type = VType.UNDEFINED;
        }

        this.type = type;
    }

    public String stringValue() {
        return value.stringValue();
    }

    public VType getType() {
        return type;
    }

    /**
     * If the specified type is null Undefined is used
     */
    public void setType(VType type) {

        if (type == null) {
            type = VType.UNDEFINED;
        }

        this.type = type;
    }

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
        VertexTyped other = (VertexTyped) obj;
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
