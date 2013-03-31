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
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;

/**
 *
 * @author Harispe Sébastien
 */
public class GraphMemory implements G {

    private URIFactory factory;
    private Set<URI> uris;	// value Mapping
    private Set<E> edges;
    private Map<URI, HashSet<E>> vertexOutEdges;
    private Map<URI, HashSet<E>> vertexInEdges;
    private URI uri;

    /**
     * Create a graph loaded in memory with an in memory DataFactory associated
     * to it.
     *
     * @param uri
     */
    public GraphMemory(URI uri) {
        this(URIFactoryMemory.getSingleton(), uri);
    }

    /**
     *
     * @param factory
     * @param uri
     */
    public GraphMemory(URIFactory factory, URI uri) {

        this.uri = uri;
        this.factory = factory;
        init();
    }

    @Override
    public URIFactory getURIFactory() {
        return factory;
    }

    private void init() {

        uris = new HashSet<URI>();
        edges = new HashSet<E>();
        vertexOutEdges = new HashMap<URI, HashSet<E>>();
        vertexInEdges = new HashMap<URI, HashSet<E>>();
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
    public Set<E> getE(URI v, Direction dir) {

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
        }
    }

    @Override
    public Set<URI> getV() {
        return new HashSet<URI>(uris);
    }

    @Override
    public void addE(URI src, URI predicate, URI target) {
        addE(new Edge(src, predicate, target));
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
    public void addV(URI v) {
        uris.add(v);
    }

    @Override
    public void addV(Set<URI> vertices) {
        for (URI v : vertices) {
            addV(v);
        }
    }

    @Override
    public void removeV(URI v) {

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
        for (URI v : setV) {
            removeV(v);
        }
    }

    @Override
    public boolean containsEdge(URI v1, URI v2, Direction dir) {

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
    public boolean containsEdge(URI v1, URI v2, Direction dir, URI type) {

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
    public boolean containsVertex(URI v) {
        return uris.contains(v);
    }

   
    @Override
    public long getNumberVertices() {
        return uris.size();
    }

    @Override
    public long getNumberEdges() {
        return edges.size();
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
    public URI getURI() {
        return uri;
    }

    @Override
    public Set<URI> getV(URI v, Set<URI> buildUris, Direction dir) {

        Set<URI> vSelected = new HashSet<URI>();

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
    public Set<URI> getV(URI v, URI buildUri, Direction dir) {

        Set<URI> vert = new HashSet<URI>();

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
        for(E e : getE(v, Direction.OUT)){
            if(wc.getAcceptedWalks_DIR_OUT().contains(e.getURI())){
                valid.add(e);
            }
        }
        for(E e : getE(v, Direction.IN)){
            if(wc.getAcceptedWalks_DIR_IN().contains(e.getURI())){
                valid.add(e);
            }
        }
        return valid;
    }

    @Override
    public Set<URI> getV(URI v, WalkConstraints wc) {
        Set<URI> valid = new HashSet<URI>();
        for(E e : getE(v, Direction.OUT)){
            if(wc.getAcceptedWalks_DIR_OUT().contains(e.getURI())){
                valid.add(e.getTarget());
            }
        }
        for(E e : getE(v, Direction.IN)){
            if(wc.getAcceptedWalks_DIR_IN().contains(e.getURI())){
                valid.add(e.getSource());
            }
        }
        return valid;
    }
}
