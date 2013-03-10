/*

 Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

 This software is a computer program whose purpose is to 
 process semantic graphs.

 This software is governed by the CeCILL  license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 */
package slib.sglib.model.impl.graph.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.NotifyingSailBase;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.graph.elements.Vertex;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.utils.impl.SetUtils;

/**
 *
 * @author Harispe Sébastien
 */
public class GraphMemory extends NotifyingSailBase implements G {

    private DataFactory factory;
    private Map<Value, V> vMapping;	// value Mapping
    private Set<E> edges;
    private Map<V, HashSet<E>> vertexOutEdges;
    private Map<V, HashSet<E>> vertexInEdges;
    private URI uri;

    /**
     * Create a graph loaded in memory with an in memory DataFactory associated
     * to it.
     *
     * @param uri
     */
    public GraphMemory(URI uri) {
        this(DataFactoryMemory.getSingleton(), uri);
    }

    /**
     *
     * @param factory
     * @param uri
     */
    public GraphMemory(DataFactory factory, URI uri) {

        this.uri = uri;
        this.factory = factory;
        init();
    }

    @Override
    public DataFactory getDataFactory() {
        return factory;
    }

    private void init() {

        vMapping = new HashMap<Value, V>();
        edges = new HashSet<E>();
        vertexOutEdges = new HashMap<V, HashSet<E>>();
        vertexInEdges = new HashMap<V, HashSet<E>>();
        factory.addGraph(this);
    }

    @Override
    public Set<E> getE() {
        return edges;
    }

    @Override
    public Set<E> getE(URI t) {

        if (t == null) {
            return getE();
        }

        Set<E> edgesCol = new HashSet<E>();

        for (E e : edges) {
            if (e.getURI().equals(t)) {
                edgesCol.add(e);
            }
        }
        return edgesCol;
    }

    @Override
    public Set<E> getE(Set<URI> types, V v, Direction dir) {

        Set<E> edgesCol = new HashSet<E>();

        if ((dir == Direction.IN || dir == Direction.BOTH) && vertexInEdges.containsKey(v)) {

            if (types == null) {
                edgesCol.addAll(vertexInEdges.get(v));
            } else {
                for (E e : vertexInEdges.get(v)) {
                    if (types.contains(e.getURI())) {
                        edgesCol.add(e);
                    }
                }
            }
        }
        if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v)) {

            if (types == null) {
                edgesCol.addAll(vertexOutEdges.get(v));
            } else {
                for (E e : vertexOutEdges.get(v)) {
                    if (types.contains(e.getURI())) {
                        edgesCol.add(e);
                    }
                }
            }
        }
        return edgesCol;
    }

    @Override
    public Set<E> getE(URI t, V v, Direction dir) {

        Set<E> edgesCol = new HashSet<E>();

        if ((dir == Direction.IN || dir == Direction.BOTH) && vertexInEdges.containsKey(v)) {

            if (t == null) {
                edgesCol.addAll(vertexInEdges.get(v));
            } else {
                for (E e : vertexInEdges.get(v)) {
                    if (e.getURI().equals(t)) {
                        edgesCol.add(e);
                    }
                }
            }
        }
        if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v)) {

            if (t == null) {
                edgesCol.addAll(vertexOutEdges.get(v));
            } else {
                for (E e : vertexOutEdges.get(v)) {
                    if (e.getURI().equals(t)) {
                        edgesCol.add(e);
                    }
                }
            }
        }
        return edgesCol;
    }

    @Override
    public Set<E> getE(Set<URI> eTypes, V v, Set<VType> targetTypes, Direction dir) {

        Set<E> edgesCol;

        if (targetTypes == null) {
            edgesCol = getE(eTypes, v, dir);
        } else {

            edgesCol = new HashSet<E>();

            if (dir == Direction.BOTH || dir == Direction.OUT) {
                for (E e : getE(eTypes, v, Direction.OUT)) {
                    if (targetTypes.contains(e.getTarget().getType())) {
                        edgesCol.add(e);
                    }
                }
            }
            if (dir == Direction.BOTH || dir == Direction.IN) {
                for (E e : getE(eTypes, v, Direction.IN)) {
                    if (targetTypes.contains(e.getSource().getType())) {
                        edgesCol.add(e);
                    }
                }
            }
        }

        return edgesCol;
    }

    @Override
    public Set<E> getE(URI t, V source, VType targetType, Direction dir) {
        return getE(SetUtils.buildSet(t), source, SetUtils.buildSet(targetType), dir);
    }

    @Override
    public Set<E> getE(Set<URI> eTypes, V v, VType targetType, Direction dir) {

        Set<E> edgesCol;

        if (targetType == null) {
            edgesCol = getE(eTypes, v, dir);
        } else {

            edgesCol = new HashSet<E>();

            if ((dir == Direction.IN || dir == Direction.BOTH) && vertexInEdges.containsKey(v)) {
                for (E e : vertexInEdges.get(v)) {
                    if (eTypes == null || eTypes.contains(e.getURI())) {
                        edgesCol.add(e);
                    }
                }
            } else if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v)) {
                for (E e : vertexOutEdges.get(v)) {
                    if (eTypes == null || eTypes.contains(e.getURI())) {
                        edgesCol.add(e);
                    }
                }
            }
        }
        return edgesCol;
    }

    @Override
    public Set<E> getE(V v, Direction dir) {

        Set<E> edgesCol = new HashSet<E>();

        if ((dir == Direction.BOTH || dir == Direction.IN) && vertexInEdges.containsKey(v)) {
            edgesCol.addAll(vertexInEdges.get(v));
        }

        if ((dir == Direction.BOTH || dir == Direction.OUT) && vertexOutEdges.containsKey(v)) {
            edgesCol.addAll(vertexOutEdges.get(v));
        }

        return edgesCol;
    }

    @Override
    public void addE(E e) {

        if (!edges.contains(e)) {
            addV(e.getSource());
            addV(e.getTarget());
            edges.add(e);

            if (!vertexOutEdges.containsKey(e.getSource())) {
                vertexOutEdges.put(e.getSource(), new HashSet<E>());
            }
            if (!vertexInEdges.containsKey(e.getTarget())) {
                vertexInEdges.put(e.getTarget(), new HashSet<E>());
            }

            vertexOutEdges.get(e.getSource()).add(e);
            vertexInEdges.get(e.getTarget()).add(e);

            factory.getPredicateFactory().add(e.getURI());
        }
    }

    @Override
    public Set<V> getV() {
        return new HashSet<V>(vMapping.values());
    }

    @Override
    public void addE(V src, V target, URI type) {
        addE(new Edge(src, target, type));
    }

    @Override
    public void addE(Value src, Value target, URI type) {

        V srcV = vMapping.get(src);
        V targetV = vMapping.get(target);

        if (srcV == null) {
            throw new IllegalArgumentException("Graph " + this.getURI() + " doesn't contain a vertex associated to value " + src);
        } else if (targetV == null) {
            throw new IllegalArgumentException("Graph " + this.getURI() + " doesn't contain a vertex associated to value " + target);
        }

        E e = new Edge(srcV, targetV, type);
        addE(e);
    }

    @Override
    public void addEdges(Set<E> edges) {
        for (E e : edges) {
            addE(e);
        }
    }

    @Override
    public void removeE(E e) {

        if (e == null) {
            return;
        }

        if (edges.remove(e)) {
            vertexOutEdges.get(e.getSource()).remove(e);
            vertexInEdges.get(e.getTarget()).remove(e);
        }
    }

    @Override
    public void removeE(URI t) {

        Iterator<E> iter = edges.iterator();
        while (iter.hasNext()) {
            E c = iter.next();

            if (c.getURI().equals(t)) {
                vertexOutEdges.get(c.getSource()).remove(c);
                vertexInEdges.get(c.getTarget()).remove(c);
                iter.remove();
            }
        }
    }

    @Override
    public void removeE(Set<E> e) {

        for (E edge : e) {
            removeE(edge);
        }
    }

    @Override
    public V addV(V v) {

        if (!vMapping.containsKey(v.getValue())) {
            vMapping.put(v.getValue(), v);
            return v;
        }
        return vMapping.get(v.getValue());
    }

    @Override
    public void addV(Set<V> vertices) {
        for (V v : vertices) {
            addV(v);
        }
    }

    @Override
    public void removeV(V v) {

        Set<E> toRemove = new HashSet<E>();

        if (vertexOutEdges.containsKey(v)) {
            for (E e : vertexOutEdges.get(v)) {
                toRemove.add(e);
            }
            vertexOutEdges.remove(v);
        }
        if (vertexInEdges.containsKey(v)) {
            for (E e : vertexInEdges.get(v)) {
                toRemove.add(e);
            }
            vertexInEdges.remove(v);
        }
        removeE(toRemove);
        vMapping.remove(v.getValue());
    }

    @Override
    public void removeV(Set<V> setV) {
        for (V v : setV) {
            removeV(v);
        }
    }

    @Override
    public boolean containsEdge(V v1, V v2, Direction dir) {

        if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v1)) {
            for (E e : vertexOutEdges.get(v1)) {

                if (e.getTarget().equals(v2)) {
                    return true;
                }
            }
        }
        if ((dir == Direction.IN || dir == Direction.BOTH) && vertexInEdges.containsKey(v1)) {
            for (E e : vertexInEdges.get(v1)) {

                if (e.getSource().equals(v2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsEdge(V v1, V v2, Direction dir, URI type) {

        if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v1)) {
            for (E e : vertexOutEdges.get(v1)) {

                if (e.getTarget().equals(v2)
                        && (type == null || e.getURI().equals(type))) {
                    return true;
                }
            }
        }
        if ((dir == Direction.IN || dir == Direction.BOTH) && vertexInEdges.containsKey(v1)) {
            for (E e : vertexInEdges.get(v1)) {

                if (e.getSource().equals(v2)
                        && (type == null || e.getURI().equals(type))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean containsEdgeOfType(URI t) {

        if (t == null) {
            return edges.isEmpty() == false;
        }

        for (E e : edges) {
            if (e.getURI().equals(t)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsVertex(V v) {
        return vMapping.containsKey(v.getValue());
    }

    @Override
    public V getV(Value value) {
        return vMapping.get(value);
    }

    @Override
    public long getNumberVertices() {
        return vMapping.size();
    }

    @Override
    public long getNumberEdges() {
        return edges.size();
    }

    @Override
    public boolean containsVertex(Value value) {
        return vMapping.containsKey(value);
    }

    @Override
    public Set<E> getE(Set<URI> c) {

        if (c == null) {
            return getE();
        }

        HashSet<E> edgesCol = new HashSet<E>();

        for (E e : edges) {
            if (c.contains(e.getURI())) {
                edgesCol.add(e);
            }
        }
        return edgesCol;
    }

    public void clear() {
        init();
    }

    @Override
    public Set<V> getV(VType type) {

        if (type == null) {
            return getV();
        }

        Set<V> vs = new HashSet<V>();

        for (V v : vMapping.values()) {
            if (v.getType().equals(type)) {
                vs.add(v);
            }
        }
        return vs;
    }

    @Override
    public Set<V> getV(Set<VType> types) {

        if (types == null) {
            return getV();
        }

        Set<V> vSel = new HashSet<V>();

        for (V v : vMapping.values()) {
            if (types.contains(v.getType())) {
                vSel.add(v);
            }
        }
        return vSel;

    }

    @Override
    public Set<V> getVClass() {

        Set<V> vSel = new HashSet<V>();

        for (V v : vMapping.values()) {
            if (v.getType() != null && v.getType() == VType.CLASS) {
                vSel.add(v);
            }
        }
        return vSel;
    }

    @Override
    public long getNumberVClass() {
        return getVClass().size();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public Set<V> getV(V v, Set<URI> buildUris, Direction dir) {

        Set<V> vSelected = new HashSet<V>();

        if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v)) {

            for (E e : vertexOutEdges.get(v)) {
                if (buildUris == null || buildUris.contains(e.getURI())) {
                    vSelected.add(e.getTarget());
                }
            }
        }
        if ((dir == Direction.IN || dir == Direction.BOTH)  && vertexInEdges.containsKey(v)) {

            for (E e : vertexInEdges.get(v)) {
                if (buildUris == null || buildUris.contains(e.getURI())) {
                    vSelected.add(e.getSource());
                }
            }
        }
        return vSelected;
    }

    @Override
    public Set<V> getV(V v, URI buildUri, Direction dir) {

        Set<V> vert = new HashSet<V>();

        if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v)) {

            for (E e : vertexOutEdges.get(v)) {

                if (buildUri == null || buildUri.equals(e.getURI())) {
                    vert.add(e.getTarget());
                }
            }
        }
        if ((dir == Direction.IN || dir == Direction.BOTH) &&  vertexInEdges.containsKey(v)) {

            for (E e : vertexInEdges.get(v)) {
                if (buildUri == null || buildUri.equals(e.getURI())) {
                    vert.add(e.getSource());
                }
            }
        }
        return vert;
    }

    @Override
    public Set<E> getE(V v, WalkConstraints wc) {
        Set<E> validEdges = getE(wc.getAcceptedWalks_DIR_IN(), v, wc.getAcceptedVTypes(), Direction.IN);
        validEdges.addAll(getE(wc.getAcceptedWalks_DIR_OUT(), v, wc.getAcceptedVTypes(), Direction.OUT));
        return validEdges;
    }

    /**
     * TODO optimize to avoid source/target lookup
     */
    @Override
    public Set<V> getV(V v, WalkConstraints wc) {
        Set<E> validEdges = getE(wc.getAcceptedWalks_DIR_IN(), v, wc.getAcceptedVTypes(), Direction.IN);
        validEdges.addAll(getE(wc.getAcceptedWalks_DIR_OUT(), v, wc.getAcceptedVTypes(), Direction.OUT));

        Set<V> validV = new HashSet<V>();

        for (E e : validEdges) {

            V t;
            if (v.equals(e.getTarget())) {
                t = e.getSource();
            } else {
                t = e.getTarget();
            }

            if (wc.respectConstaints(t)) {
                validV.add(t);
            }
        }

        return validV;
    }

    @Override
    public String toString() {

        String out = "";

        if (uri == null) {
            out += "Uri undefined\n";
        } else {
            out += uri.toString() + "\n";
        }
        String exURiVertex = "";

        if (!vMapping.isEmpty()) {
            exURiVertex = "{e.g. " + vMapping.keySet().iterator().next().toString() + "}";
        }

        out += "Vertices\n";
        out += "\tTotal   : " + vMapping.size() + "  " + exURiVertex + "\n";
        out += "\tClasses : " + getVClass().size() + "  \n";
        out += "Edges 	  : " + edges.size() + "\n\n";


        for (URI e : factory.getPredicateFactory().getURIs()) {

            long size = getE(e).size();
            if (size != 0) {
                out += "\t" + e + " " + size + "\n";
            }
        }
        return out;
    }

    /**
     * *************************************************************************************
     * NotifySail Interface
     * *************************************************************************************
     * @return
     * @throws SailException
     */
    @Override
    public boolean isWritable() throws SailException {
        return true;
    }

    @Override
    public ValueFactory getValueFactory() {
        return factory;
    }

    @Override
    protected NotifyingSailConnection getConnectionInternal() throws SailException {
        return new GSailConnection(this);
    }

    @Override
    protected void shutDownInternal() throws SailException {
        // do nothing 
    }

    @Override
    public synchronized V createVertex(Value val) {
        if (!containsVertex(val)) {
            V v = new Vertex(val, VType.CLASS);
            addV(v);
            return v;
        } else {
            return getV(val);
        }

    }

    @Override
    public V createVertex(Value val, VType type) {
        if (!containsVertex(val)) {
            V v = new Vertex(val, type);
            addV(v);
            return v;
        } else {
            return getV(val);
        }
    }
}
