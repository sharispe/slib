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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;

/**
 * In memory implementation of {@link G}
 *
 * @author Harispe Sébastien
 */
public class GraphMemory implements G {

    private URIFactory factory;
    private Set<URI> uris;
    private Set<E> edges;
    private Map<URI, Set<E>> vertexOutEdges;
    private Map<URI, Set<E>> vertexInEdges;
    private URI uri;

    /**
     * Create a graph loaded in memory.
     *
     * @param uri
     */
    public GraphMemory(URI uri) {
        this(URIFactoryMemory.getSingleton(), uri);
    }

    /**
     * @param factory
     * @param uri
     */
    public GraphMemory(URIFactory factory, URI uri) {

        this.uri = uri;
        this.factory = factory;
        uris = new HashSet<URI>();
        edges = new HashSet<E>();
        vertexOutEdges = new HashMap<URI, Set<E>>();
        vertexInEdges = new HashMap<URI, Set<E>>();
    }

    @Override
    public URIFactory getURIFactory() {
        return factory;
    }

    @Override
    public Set<E> getE() {
        return Collections.unmodifiableSet(edges);
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
        return Collections.unmodifiableSet(edgesCol);
    }

    @Override
    public Set<E> getE(Set<URI> types, URI v, Direction dir) {

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
    public Set<E> getE(URI t, URI v, Direction dir) {
        
        if(v == null){
            return getE(t);
        }

        Set<E> edgesCol = new HashSet<E>();

        if ((dir == Direction.IN || dir == Direction.BOTH || dir == null) && vertexInEdges.containsKey(v)) {

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
        if ((dir == Direction.OUT || dir == Direction.BOTH || dir == null) && vertexOutEdges.containsKey(v)) {

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
        return Collections.unmodifiableSet(edgesCol);
    }

    @Override
    public Set<E> getE(URI v, Direction dir) {

        if (v == null) {
            return getE();
        }

        Set<E> edgesCol = new HashSet<E>();

        if ((dir == Direction.IN || dir == Direction.BOTH || dir == null) && vertexInEdges.containsKey(v)) {
            edgesCol.addAll(vertexInEdges.get(v));
        }

        if ((dir == Direction.OUT || dir == Direction.BOTH || dir == null) && vertexOutEdges.containsKey(v)) {
            edgesCol.addAll(vertexOutEdges.get(v));
        }

        return Collections.unmodifiableSet(edgesCol);
    }

    @Override
    public void addE(E e) {

        if (!edges.contains(e)) {
            URI s = e.getSource();
            URI o = e.getTarget();

            addV(s);
            addV(o);
            edges.add(e);

            if (!vertexOutEdges.containsKey(s)) {
                vertexOutEdges.put(s, new HashSet<E>());
            }
            if (!vertexInEdges.containsKey(o)) {
                vertexInEdges.put(s, new HashSet<E>());
            }

            vertexOutEdges.get(s).add(e);
            vertexInEdges.get(o).add(e);
        }
    }

    @Override
    public Set<URI> getV() {
        return Collections.unmodifiableSet(uris);
    }

    @Override
    public void addE(URI src, URI predicate, URI target) {
        // null parameters will throw an IllegalArgumentException at egde creation
        addE(new Edge(src, predicate, target));
    }

    @Override
    public void addEdges(Set<E> edges) {
        if(edges == null)
            return;
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
    public void addV(URI v) {
        if(v == null){
            throw new IllegalArgumentException("The URI must not be null");
        }
        uris.add(v);
    }

    @Override
    public void addV(Set<URI> vertices) {
        if(vertices == null){
            return;
        }
        uris.addAll(vertices);
    }

    @Override
    public void removeV(URI v) {
        
        if(v == null){
            return;
        }

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
        uris.remove(v);
    }

    @Override
    public void removeV(Set<URI> setV) {
        if(setV == null){
            return;
        }
        for (URI v : setV) {
            removeV(v);
        }
    }

    @Override
    public boolean containsVertex(URI v) {
        return uris.contains(v);
    }

    @Override
    public int getNumberVertices() {
        return uris.size();
    }

    @Override
    public int getNumberEdges() {
        return edges.size();
    }

    @Override
    public Set<E> getE(Set<URI> c) {

        if (c == null || c.isEmpty()) {
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
        edges.clear();
        uris.clear();
        vertexInEdges.clear();
        vertexOutEdges.clear();
    }

    @Override
    public URI getURI() {
        return uri;
    }

    @Override
    public Set<URI> getV(URI v, URI buildUri, Direction dir) {

        Set<URI> vert = new HashSet<URI>();

        if ((dir == Direction.OUT || dir == Direction.BOTH) && vertexOutEdges.containsKey(v)) {

            for (E e : vertexOutEdges.get(v)) {

                if (buildUri == null || buildUri.equals(e.getURI())) {
                    vert.add(e.getTarget());
                }
            }
        }
        if ((dir == Direction.IN || dir == Direction.BOTH) && vertexInEdges.containsKey(v)) {

            for (E e : vertexInEdges.get(v)) {
                if (buildUri == null || buildUri.equals(e.getURI())) {
                    vert.add(e.getSource());
                }
            }
        }
        return vert;
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

        if (!uris.isEmpty()) {
            exURiVertex = "{e.g. " + uris.iterator().next().toString() + "}";
        }

        out += "Vertices\n";
        out += "\tTotal   : " + uris.size() + "  " + exURiVertex + "\n";
        out += "Edges 	  : " + edges.size() + "\n\n";


        return out;
    }

    @Override
    public Set<E> getE(URI v, WalkConstraints wc) {
        Set<E> valid = new HashSet<E>();
        for (E e : getE(v, Direction.OUT)) {
            if (wc.getAcceptedWalks_DIR_OUT().contains(e.getURI())) {
                valid.add(e);
            }
        }
        for (E e : getE(v, Direction.IN)) {
            if (wc.getAcceptedWalks_DIR_IN().contains(e.getURI())) {
                valid.add(e);
            }
        }
        return valid;
    }

    @Override
    public Set<URI> getV(URI v, WalkConstraints wc) {
        Set<URI> valid = new HashSet<URI>();
        for (E e : getE(v, Direction.OUT)) {
            if (wc.getAcceptedWalks_DIR_OUT().contains(e.getURI())) {
                valid.add(e.getTarget());
            }
        }
        for (E e : getE(v, Direction.IN)) {
            if (wc.getAcceptedWalks_DIR_IN().contains(e.getURI())) {
                valid.add(e.getSource());
            }
        }
        return valid;
    }
}
