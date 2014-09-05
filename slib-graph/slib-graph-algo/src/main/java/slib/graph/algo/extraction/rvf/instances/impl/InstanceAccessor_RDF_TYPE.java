/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.graph.algo.extraction.rvf.instances.impl;

import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import slib.graph.algo.extraction.rvf.DescendantEngine;
import slib.graph.algo.extraction.rvf.instances.InstancesAccessor;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.Direction;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class InstanceAccessor_RDF_TYPE implements InstancesAccessor {

    G graph;
    DescendantEngine descendantsEngine;

    /**
     * @param graph
     * @param engine
     */
    public InstanceAccessor_RDF_TYPE(G graph, DescendantEngine engine) {
        this.graph = graph;
        this.descendantsEngine = engine;
    }

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

    public long getDirectInstancesNumber(URI v) {
        return graph.getV(v, RDF.TYPE, Direction.IN).size();
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
        for (URI d : descendantsEngine.getDescendantsExc(v)) {
            instances.addAll(getDirectInstances(d));
        }
        return instances;
    }
}
