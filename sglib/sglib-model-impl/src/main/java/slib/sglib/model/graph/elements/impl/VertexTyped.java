package slib.sglib.model.graph.elements.impl;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.utils.impl.OProperty;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Query;

public class VertexTyped extends OProperty implements V{


	protected G g;
	protected Value value;
	protected VType type;



	public VertexTyped(G g, Value v, VType type){
		this.g = g;
		this.value = v;
		
		if(type == null)
			type = VType.UNDEFINED;
		
		this.type = type;
	}

	public String stringValue() {
		return value.stringValue();
	}

	public VType getType() {
		return type;
	}

	/**
	 * If the specified type is null
	 * {@value VType#UNDEFINED} is used
	 */
	public void setType(VType type) {
		
		if(type == null)
			type = VType.UNDEFINED;
		
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VertexTyped other = (VertexTyped) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Iterable<Edge> getEdges(Direction direction, String... labels) {

		Iterable<Edge> edges = null;
		
		if(direction.equals(Direction.OUT)){
			return (Iterable) g.getE(buildUris(labels), this, Direction.OUT);
		}
		else if(direction.equals(Direction.IN)){
			return (Iterable) g.getE(buildUris(labels), this, Direction.IN);
		}
		else if(direction.equals(Direction.BOTH)){
			return (Iterable) g.getE(buildUris(labels), this, Direction.BOTH);
		}
		return edges;

	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Iterable<com.tinkerpop.blueprints.Vertex> getVertices(
			Direction direction, String... labels) {
		
		return (Iterable) g.getV(this, buildUris(labels),direction);
	}

	public Query query() {
		throw new UnsupportedOperationException("Not supported yet.");

	}

	public Object getId() {
		return value;
	}
	
	private Set<URI> buildUris(String[] labels){
		
		Set<URI> uris = null;

		if(labels.length != 0){
			uris = new HashSet<URI>();
			for(String l : labels)
				uris.add(g.getDataFactory().createURI(l));
		}
		return uris;
	}
	
	public String toString(){
		return value.stringValue();
	}
}
