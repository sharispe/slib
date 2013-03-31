package slib.sglib.algo.graph.extraction.rvf.instances.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import slib.sglib.algo.graph.extraction.rvf.DescendantEngine;
import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.utils.Direction;

/**
 *
 * @author seb
 */
public class InstanceAccessor_RDF_TYPE implements InstancesAccessor {

    G graph;
    DescendantEngine descendantsEngine;

    /**
     * @param graph
     */
    public InstanceAccessor_RDF_TYPE(G graph) {
        this.graph = graph;
        this.descendantsEngine = new DescendantEngine(graph);
    }

    @Override
    public Set<URI> getDirectInstances(URI v) {
        return graph.getV(v, RDF.TYPE, Direction.IN);
    }

    @Override
    public long getInstancesNumber(URI v) {
        // TODO Auto-generated method stub Aug 30, 2012
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<URI, Long> getInferredInstancesNumberMapping() {
        // TODO Auto-generated method stub Aug 30, 2012
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public long getDirectInstancesNumber(URI v) {
        return graph.getV(v, RDF.TYPE, Direction.IN).size();
    }

    @Override
    public Map<URI, Long> getDirectInstancesNumberMapping() {
        // TODO Auto-generated method stub Aug 30, 2012
        throw new UnsupportedOperationException("Not supported yet.");

    }

    @Override
    public Set<URI> getInstances() {
        throw new UnsupportedOperationException("To implement");
    }

    @Override
    public Set<URI> getDirectClass(URI v) {
        return graph.getV(v, RDF.TYPE, Direction.OUT);
    }

    @Override
    public Set<URI> getInstances(URI v) {
        
        
        Set<URI> instances = new HashSet<URI>();
        
        instances.addAll(getDirectInstances(v));
        for(URI d : descendantsEngine.getDescendantsExc(v)){
            instances.addAll(getDirectInstances(d));
        }
        return instances;
    }
}
