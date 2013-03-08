/*
 * 
 * Copyright or © or Copr. Ecole des Mines d'Alès (2012) 
 * LGI2P research center
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 * 
 */
package slib.sglib.algo.graph.extraction.rvf.instances.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import slib.sglib.algo.graph.extraction.rvf.RVF_TAX;
import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class InstancesAccessorGeneric implements InstancesAccessor {

    /**
     *
     */
    public G graph;
    /**
     *
     */
    public RVF_TAX taxFinder;
    /**
     *
     */
    public Map<V, Set<V>> instancesToClasses; // direct, i.e. without inference
    /**
     *
     */
    public Map<V, Set<V>> classesToInstances; // direct, i.e. without inference
    /**
     *
     */
    public Map<V, Set<V>> descendants;

    /**
     *
     * @param graph
     * @param instancesToDirectClasses
     * @throws SLIB_Ex_Critic
     */
    public InstancesAccessorGeneric(G graph, Map<V, Set<V>> instancesToDirectClasses) throws SLIB_Ex_Critic {
        this.graph = graph;
        this.instancesToClasses = instancesToDirectClasses;

        taxFinder = new RVF_TAX(graph, Direction.IN);
        descendants = taxFinder.getAllRVClass();

        // build inverse index
        classesToInstances = new HashMap<V, Set<V>>();
        for (V i : instancesToClasses.keySet()) {
            for (V c : instancesToClasses.get(i)) {
                if (!classesToInstances.containsKey(c)) {
                    classesToInstances.put(c, new HashSet<V>());
                }
                classesToInstances.get(c).add(i);
            }
        }
    }

    @Override
    public Set<V> getInstances() {
        return instancesToClasses.keySet();
    }

    @Override
    public Set<V> getInstances(V v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Set<V> getDirectInstances(V v) {
        return classesToInstances.get(v);
    }

    @Override
    public Set<V> getDirectClass(V v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getInstancesNumber(V v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<V, Long> getInferredInstancesNumberMapping() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getDirectInstancesNumber(V v) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<V, Long> getDirectInstancesNumberMapping() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
