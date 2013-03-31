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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openrdf.model.URI;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMconf;

/**
 *
 * @author seb
 */
public class SMProxResultStorage {

    Map<ICconf, Map<URI, Double>> metrics_results;
    Map<SMconf, ConcurrentHashMap<URI, Map<URI, Double>>> pairwise_results;
    Map<URI, ConcurrentHashMap<URI, Double>> shortestPath;
    Map<URI, Set<URI>> ancestorsInc;
    Map<URI, Set<URI>> descendantsInc;
    Map<URI, Set<URI>> reachableLeaves;
    Map<URI, Integer> nbPathLeadingToAllVertices;
    Map<URI, Integer> allNbReachableLeaves;
    // Depth
    Map<URI, Integer> maxDepths;
    Map<URI, Integer> minDepths;
    Integer maxDepth;
    /**
     *
     */
    public Map<URI, Integer> nbOccurrencePropagatted;

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

        metrics_results = new ConcurrentHashMap<ICconf, Map<URI, Double>>();
        ancestorsInc = new ConcurrentHashMap<URI, Set<URI>>();
        descendantsInc = new ConcurrentHashMap<URI, Set<URI>>();
        reachableLeaves = new ConcurrentHashMap<URI, Set<URI>>();
        shortestPath = new ConcurrentHashMap<URI, ConcurrentHashMap<URI, Double>>();
        pairwise_results = new ConcurrentHashMap<SMconf, ConcurrentHashMap<URI, Map<URI, Double>>>();
        nbOccurrencePropagatted = new HashMap<URI, Integer>();

        // do not inialize
        nbPathLeadingToAllVertices = null;
        maxDepths = null;
        minDepths = null;
        maxDepth = null;
        allNbReachableLeaves = null;
    }
}
