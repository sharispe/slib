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
package slib.sglib.model.graph.impl.memory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import slib.sglib.model.graph.elements.impl.EdgeTyped;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.sglib.model.graph.weight.GWS;
import slib.sglib.model.graph.weight.impl.GWS_impl;
import slib.sglib.model.repo.impl.DataFactoryMemory;
import slib.utils.impl.SetUtils;

import slib.sglib.model.graph.elements.impl.VertexTyped;
import slib.sglib.model.graph.utils.Direction;

public class GraphMemory_Abstract extends NotifyingSailBase implements G {

    private DataFactoryMemory data;
    private HashMap<Value, V> vMapping;	// value Mapping
    private Set<V> vertices;
    private Set<E> edges;
    private HashMap<V, HashSet<E>> vertexOutEdges;
    private HashMap<V, HashSet<E>> vertexInEdges;
    private GWS ws;
    private URI uri;

    public GraphMemory_Abstract(URI uri) {

        this.uri = uri;
        data = DataFactoryMemory.getSingleton();


        ws = new GWS_impl();

        initDataStructures();
    }

    @Override
    public DataFactoryMemory getDataFactory() {
        return data;
    }

    private void initDataStructures() {

        vMapping = new HashMap<Value, V>();
        vertices = new HashSet<V>();
        edges = new HashSet<E>();
        vertexOutEdges = new HashMap<V, HashSet<E>>();
        vertexInEdges = new HashMap<V, HashSet<E>>();
    }

    @Override
    public Set<E> getE() {
        return edges;
    }

    @Override
    public Set<E> getE(URI t) {
        Set<E> edgesCol = new HashSet<E>();

        for (E e : edges) {
            if (t == null || e.getURI().equals(t)) {
                edgesCol.add(e);
            }
        }
        return edgesCol;
    }

    @Override
    public Set<E> getE(URI eType, Set<V> vertices, Direction dir) {

        Set<E> edgesCol = new HashSet<E>();

        if (dir == Direction.BOTH) {
            edgesCol = getE(eType, vertices, Direction.OUT);
            edgesCol.addAll(getE(eType, vertices, Direction.IN));
        } else if (dir == Direction.IN) {

            for (E e : edges) {
                if (vertices.contains(e.getTarget())
                        && (eType == null || eType.equals(e.getURI()))) {
                    edgesCol.add(e);
                }
            }
        } else if (dir == Direction.OUT) {

            for (V v : vertices) {
                for (E e : vertexOutEdges.get(v)) {
                    if (eType == null || eType.equals(e.getURI())) {
                        edgesCol.add(e);
                    }
                }
            }
        }
        return edgesCol;
    }

    @Override
    public Set<E> getE(Set<URI> types, V v, Direction dir) {

        Set<E> edgesCol = new HashSet<E>();

        if (dir == Direction.IN || dir == Direction.BOTH) {
            for (E e : vertexInEdges.get(v)) {
                if (types == null || types.contains(e.getURI())) {
                    edgesCol.add(e);
                }
            }
        }
        if (dir == Direction.OUT || dir == Direction.BOTH) {
            for (E e : vertexOutEdges.get(v)) {
                if (types == null || types.contains(e.getURI())) {
                    edgesCol.add(e);
                }
            }
        }
        return edgesCol;
    }

    public Set<E> getE(Set<URI> rset, Set<V> vertices, Direction dir) {

        Set<E> edgesCol = new HashSet<E>();

        for (V v : vertices) {
            edgesCol.addAll(getE(rset, v, dir));
        }

        return edgesCol;
    }

    @Override
    public Set<E> getE(URI t, V source, Direction dir) {
        return getE(((Set<URI>) SetUtils.buildSet(t)), source, dir);
    }

    @Override
    public Set<E> getE(Set<URI> eTypes, V v, Set<VType> targetTypes, Direction dir) {

        Set<E> edgesCol = null;

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

        Set<E> edgesCol = null;

        if (targetType == null) {
            edgesCol = getE(eTypes, v, dir);
        } else {

            edgesCol = new HashSet<E>();

            if (dir == Direction.IN || dir == Direction.BOTH) {
                for (E e : vertexInEdges.get(v)) {
                    if (eTypes == null || eTypes.contains(e.getURI())) {
                        edgesCol.add(e);
                    }
                }
            } else if (dir == Direction.OUT || dir == Direction.BOTH) {
                for (E e : vertexInEdges.get(v)) {
                    if (eTypes == null || eTypes.contains(e.getURI())) {
                        edgesCol.add(e);
                    }
                }
            }
        }
        return edgesCol;
    }

    public Set<E> getE(V v, Direction dir) {

        Set<E> edgesCol = new HashSet<E>();

        if (dir == Direction.BOTH || dir == Direction.IN) {
            edgesCol.addAll(vertexInEdges.get(v));
        }

        if (dir == Direction.BOTH || dir == Direction.OUT) {
            edgesCol.addAll(vertexOutEdges.get(v));
        }

        return edgesCol;
    }

    public Set<E> getE(V v, VType type) {

        Set<E> edgesCol = new HashSet<E>();

        Set<E> edges = getE(v, Direction.BOTH);
        if (type == null) {
            edgesCol = edges;
        } else {
            for (E e : edges) {
                if (e.getURI().equals(type)) {
                    edgesCol.add(e);
                }
            }
        }
        return edgesCol;
    }

    public Set<E> getE(V v, Set<VType> types) {

        Set<E> edgesCol = new HashSet<E>();

        Set<E> edges = getE(v, Direction.BOTH);
        if (types == null) {
            edgesCol = edges;
        } else {
            for (E e : edges) {
                if (types.contains(e.getURI())) {
                    edgesCol.add(e);
                }
            }
        }
        return edgesCol;
    }

    public void addE(E e) {

        if (!edges.contains(e)) {
            addV(e.getSource());
            addV(e.getTarget());
            edges.add(e);
            vertexOutEdges.get(e.getSource()).add(e);
            vertexInEdges.get(e.getTarget()).add(e);
            data.getPredicateFactory().add(e.getURI());
        }
    }

    public Set<V> getV() {
        return vertices;
    }

    public void addE(V src, V target, URI type) {

        E e = new EdgeTyped(src, target, type);
        addE(e);
    }

    public void addEdges(Set<E> edges) {
        for (E e : edges) {
            addE(e);
        }
    }

    public void removeE(E e) {

        if (e == null) {
            return;
        }

        edges.remove(e);
        vertexOutEdges.get(e.getSource()).remove(e);
        vertexInEdges.get(e.getTarget()).remove(e);
    }

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

    public void removeE(Set<E> e) {

        for (E edge : e) {
            removeE(edge);
        }
    }

    public V addV(V v) {

        if (!vMapping.containsKey(v.getValue())) {
            vMapping.put(v.getValue(), v);
            vertices.add(v);
            vertexOutEdges.put(v, new HashSet<E>());
            vertexInEdges.put(v, new HashSet<E>());
            return v;
        }
        return vMapping.get(v.getValue());
    }

    public void addV(Set<V> vertices) {
        for (V v : vertices) {
            addV(v);
        }
    }

    public void removeV(V v) {




        Set<E> toRemove = new HashSet<E>();
        for (E e : vertexOutEdges.get(v)) {
            toRemove.add(e);
        }

        for (E e : vertexInEdges.get(v)) {
            toRemove.add(e);
        }

        for (E e : toRemove) {
            removeE(e);
        }

        vertexOutEdges.remove(v);
        vertexInEdges.remove(v);
        vertices.remove(v);
        vMapping.remove(v.getValue());
    }

    public void removeV(Set<V> setV) {
        for (V v : setV) {
            removeV(v);
        }
    }

    public boolean containsEdge(V v1, V v2, Direction dir) {

        if (dir == Direction.OUT || dir == Direction.BOTH) {
            for (E e : vertexOutEdges.get(v1)) {

                if (e.getTarget().equals(v2)) {
                    return true;
                }
            }
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {
            for (E e : vertexInEdges.get(v1)) {

                if (e.getSource().equals(v2)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsEdge(V source, V target, Direction dir, URI type) {

        if (dir == Direction.OUT || dir == Direction.BOTH) {
            for (E e : vertexOutEdges.get(source)) {

                if (e.getTarget().equals(target)
                        && (type == null || e.getURI().equals(type))) {
                    return true;
                }
            }
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {
            for (E e : vertexInEdges.get(source)) {

                if (e.getSource().equals(target)
                        && (type == null || e.getURI().equals(type))) {
                    return true;
                }
            }
        }
        return false;
    }

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

    public boolean containsVertex(V v) {
        return vMapping.containsKey(v.getValue());
    }

    public V getV(Value value) {
        return vMapping.get(value);
    }

    public long getNumberVertices() {
        return vMapping.size();
    }

    public long getNumberEdges() {
        return edges.size();
    }

    public boolean containsVertex(Value uri) {
        return vMapping.containsKey(uri);
    }

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

    public void setEdgeTypeWeight(URI eType, double w, boolean propagate) {

        for (E e : edges) {

            if (e.getURI().equals(eType)) {
                ws.setWeight(e, w);
            }
        }
    }

    /**
     * help the garbage collector
     */
    public void clear() {

        vMapping = null;
        edges = null;
        vertexOutEdges = null;

        initDataStructures();
    }

    public double getEdgeTypeWeight(URI eType) {
        return ws.getWeight(eType);
    }

    public double getEdgeWeight(E e) {
        return ws.getWeight(e);
    }

    public void setEdgeWeight(E e, double w) {
        ws.setWeight(e, w);
    }

    public GWS getWeightingScheme() {
        return ws;
    }

    public void setWeightingScheme(GWS ws) {
        this.ws = ws;
    }

    public Set<V> getV(VType type) {

        if (type == null) {
            return this.vertices;
        }

        Set<V> vertices = new HashSet<V>();

        for (V v : this.vertices) {
            if (v.getType().equals(type)) {
                vertices.add(v);
            }
        }
        return vertices;
    }

    public Set<V> getV(Set<VType> types) {

        if (types == null) {
            return this.vertices;
        }

        Set<V> vertices = new HashSet<V>();

        for (V v : this.vertices) {
            if (types.contains(v.getType())) {
                vertices.add(v);
            }
        }
        return vertices;

    }

    public Set<V> getVClass() {

        Set<V> vertices = new HashSet<V>();

        for (V v : this.vertices) {
            if (v.getType() != null && v.getType() == VType.CLASS) {
                vertices.add(v);
            }
        }
        return vertices;
    }

    public long getNumberVClass() {
        return getVClass().size();
    }

    public Set<V> getV_NoEdgeType(URI edgeType, Direction dir) {
        return getV_NoEdgeType(SetUtils.buildSet(edgeType), dir);
    }

    public Set<V> getV_NoEdgeType(Set<URI> edgeTypes, Direction dir) {
        return getV_NoEdgeType(null, edgeTypes, dir);
    }

    public Set<V> getV_NoEdgeType(VType type, Set<URI> eTypes, Direction dir) {

        Set<V> valid = new HashSet<V>();

        Set<V> vertices = getV(type);

        if (dir == Direction.OUT || dir == Direction.BOTH) {

            for (V v : vertices) {
                HashSet<E> edges = vertexOutEdges.get(v);

                boolean isValid = true;

                for (E e : edges) {
                    if (eTypes == null
                            || eTypes.contains(e.getURI())) {

                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    valid.add(v);
                }
            }
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {

            for (V v : vertices) {
                HashSet<E> edges = vertexInEdges.get(v);

                boolean isValid = true;

                for (E e : edges) {
                    if (eTypes == null
                            || eTypes.contains(e.getURI())) {

                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    valid.add(v);
                }
            }
        }
        return valid;
    }

    public void info() {
        System.out.println(uri);
        System.out.println("Vertices: " + getV().size());
        System.out.println("Edges: " + getE().size());
    }

    public URI getURI() {
        return uri;
    }

    public Set<V> getV(V v, Set<URI> buildUris, Direction dir) {

        Set<V> vertices = new HashSet<V>();

        if (dir == Direction.OUT || dir == Direction.BOTH) {

            for (E e : vertexOutEdges.get(v)) {
                if (buildUris == null || buildUris.contains(e.getURI())) {
                    vertices.add(e.getTarget());
                }
            }
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {

            for (E e : vertexInEdges.get(v)) {
                if (buildUris == null || buildUris.contains(e.getURI())) {
                    vertices.add(e.getTarget());
                }
            }
        }
        return vertices;
    }

    @Override
    public Set<V> getV(V v, URI buildUri, Direction dir) {

        Set<V> vert = new HashSet<V>();

        if (dir == Direction.OUT || dir == Direction.BOTH) {

            for (E e : vertexOutEdges.get(v)) {
                if (buildUri == null || buildUri.equals(e.getURI())) {
                    vert.add(e.getTarget());
                }
            }
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {

            for (E e : vertexInEdges.get(v)) {
                if (buildUri == null || buildUri.equals(e.getURI())) {
                    vert.add(e.getTarget());
                }
            }
        }
        return vert;
    }

    public Set<E> getE(V v, WalkConstraints wc) {
        Set<E> validEdges = getE(wc.getAcceptedWalks_DIR_IN(), v, wc.getAcceptedVTypes(), Direction.IN);
        validEdges.addAll(getE(wc.getAcceptedWalks_DIR_OUT(), v, wc.getAcceptedVTypes(), Direction.OUT));
        return validEdges;
    }

    /**
     * TODO optimize to avoid source/target lookup
     */
    public Set<V> getV(V v, WalkConstraints wc) {
        Set<E> validEdges = getE(wc.getAcceptedWalks_DIR_IN(), v, wc.getAcceptedVTypes(), Direction.IN);
        validEdges.addAll(getE(wc.getAcceptedWalks_DIR_OUT(), v, wc.getAcceptedVTypes(), Direction.OUT));

        Set<V> validV = new HashSet<V>();

        for (E e : validEdges) {

            V t = null;
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

        String out = uri.toString() + "\n";

        String exURiVertex = "";

        if (!vMapping.isEmpty()) {
            exURiVertex = "{e.g. " + vMapping.keySet().iterator().next().toString() + "}";
        }

        out += "Vertices\n";
        out += "\tTotal   : " + vMapping.size() + "  " + exURiVertex + "\n";
        out += "\tClasses : " + getVClass().size() + "  \n";
        out += "Edges 	  : " + edges.size() + "\n\n";


        for (URI e : data.getPredicateFactory().getURIs()) {

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
     */
    @Override
    public boolean isWritable() throws SailException {
        return true; // ?
    }

    @Override
    public ValueFactory getValueFactory() {
        return data;
    }

    @Override
    protected NotifyingSailConnection getConnectionInternal() throws SailException {
        return new GSailConnection(this);
    }

    @Override
    protected void shutDownInternal() throws SailException {
        // do nothing ?
    }

    @Override
    public synchronized V createVertex(Value val) {
        if (!containsVertex(val)) {
            V v = new VertexTyped(this, val, VType.CLASS);
            addV(v);
            return v;
        } else {
            return getV(val);
        }

    }

    @Override
    public V createVertex(Value val, VType type) {
        if (!containsVertex(val)) {
            V v = new VertexTyped(this, val, type);
            addV(v);
            return v;
        } else {
            return getV(val);
        }
    }
}
