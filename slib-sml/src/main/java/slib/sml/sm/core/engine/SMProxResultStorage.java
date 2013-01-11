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
package slib.sml.sm.core.engine;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.impl.ResultStack;

/**
 *
 * @author seb
 */
public class SMProxResultStorage {

    Map<ICconf, ResultStack<V, Double>> metrics_results;
    Map<SMconf, ConcurrentHashMap<V, ResultStack<V, Double>>> pairwise_results;
    Map<V, ConcurrentHashMap<V, Double>> shortestPath;
    Map<V, Set<V>> ancestors;
    Map<V, Set<V>> descendants;
    Map<V, Set<V>> reachableLeaves;
    ResultStack<V, Long> nbPathLeadingToAllVertices;
    // Depth
    ResultStack<V, Integer> maxDepths;
    ResultStack<V, Integer> minDepths;
    Integer maxDepth;
    /**
     *
     */
    public ResultStack<V, Long> nbOccurrencePropagatted;

    /**
     *
     */
    public SMProxResultStorage() {
        this.clearCache();
    }

    /**
     *
     */
    public void clearCache() {

        metrics_results = new ConcurrentHashMap<ICconf, ResultStack<V, Double>>();
        ancestors = new ConcurrentHashMap<V, Set<V>>();
        descendants = new ConcurrentHashMap<V, Set<V>>();
        reachableLeaves = new ConcurrentHashMap<V, Set<V>>();
        shortestPath = new ConcurrentHashMap<V, ConcurrentHashMap<V, Double>>();
        pairwise_results = new ConcurrentHashMap<SMconf, ConcurrentHashMap<V, ResultStack<V, Double>>>();
        nbOccurrencePropagatted = new ResultStack<V, Long>();

        nbPathLeadingToAllVertices = null;
        maxDepths = null;
        minDepths = null;
        maxDepth = null;
    }
}
