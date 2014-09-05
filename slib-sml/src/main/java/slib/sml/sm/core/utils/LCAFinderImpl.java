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
package slib.sml.sm.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.extraction.rvf.RVF;
import slib.graph.algo.extraction.rvf.RVF_TAX;
import slib.graph.algo.traversal.classical.DFS;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.utils.WalkConstraintGeneric;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 * Dummy implementation of the LCAFinder interface (high algorithmic
 * complexity).
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class LCAFinderImpl implements LCAFinder {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    G graph;
    SM_Engine engine;
    
    public LCAFinderImpl(SM_Engine engine){
        this.engine = engine;
        graph = engine.getGraph();
    }
    
    

    private URI getNextUnvisited(List<URI> ancestorsOrdered, Map<URI, Boolean> visited) {
        for (int i = ancestorsOrdered.size() - 1; i >= 0; i--) {

            if (visited.get(ancestorsOrdered.get(i)).equals(Boolean.FALSE)) {
                return ancestorsOrdered.get(i);
            }

        }
        return null;
    }

    private boolean containsUnvisitedVertices(Map<URI, Boolean> visited) {
        for (URI v : visited.keySet()) {
            if (visited.get(v).equals(Boolean.FALSE)) {
                return true;
            }
        }
        return false;
    }

    private void printStackStatus(List<URI> ancestorsOrdered, Map<URI, Boolean> visited) {
        for (int i = ancestorsOrdered.size() - 1; i >= 0; i--) {

            System.out.println(ancestorsOrdered.get(i) + "\t" + visited.get(ancestorsOrdered.get(i)));

        }
    }

    @Override
    public Set<URI> getLCAs(URI a, URI b) throws SLIB_Exception {

        if (!graph.containsVertex(a)) {
            throw new SLIB_Ex_Critic("Graph " + graph.getURI() + " doesn't contain vertice " + a);
        } else if (!graph.containsVertex(b)) {
            throw new SLIB_Ex_Critic("Graph " + graph.getURI() + " doesn't contain vertice " + b);
        }


        Set<URI> lca = new HashSet<URI>();

        Set<URI> ancA = engine.getAncestorsInc(a);
        Set<URI> ancB = engine.getAncestorsInc(b);

        // Test if a (resp. b) subsumes b (resp. a)

        if (ancA.contains(b)) {
            lca = SetUtils.buildSet(b);
        } else if (ancB.contains(a)) {
            lca = SetUtils.buildSet(a);
        } else { // search the intersetion of the ancestors of the compared concepts


            Set<URI> intersection = SetUtils.intersection(engine.getAncestorsInc(a), engine.getAncestorsInc(b));
//        logger.info(union.toString());

            // topological sort of the union
            Set<URI> queries = SetUtils.buildSet(a);

            queries.add(b);
            WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT);
            DFS dfs = new DFS(graph, queries, wc);
            List<URI> to = dfs.getTraversalOrder();

            // We remove the queried vertices from the topological order
            to.remove(a);
            to.remove(b);

//        logger.debug("Traversal Order : " + to);

            List<URI> ancestorsOrdered = new ArrayList<URI>(to.size());

            for (URI v : to) {
                if (intersection.contains(v)) {
                    ancestorsOrdered.add(v);
                }
            }

//        logger.debug("Union size : " + union.size());
//        logger.debug("Ancestors ordered : " + ancestorsOrdered.size());

            // Create dataStructure which will help us to manage 
            // the visited vertices

            Map<URI, Boolean> isVisited = new HashMap<URI, Boolean>(ancestorsOrdered.size());
            for (URI v : ancestorsOrdered) {
                isVisited.put(v, Boolean.FALSE);
            }

            RVF rvf = new RVF_TAX(graph, Direction.OUT);

            // Bottom Up with removing
            while (containsUnvisitedVertices(isVisited)) {
                URI v = getNextUnvisited(ancestorsOrdered, isVisited);

//            logger.debug("Processing " + v);
                lca.add(v);
                isVisited.put(v, Boolean.TRUE);

                Set<URI> ancestorsV = rvf.getRV(v);

                for (URI anc : ancestorsV) {
                    isVisited.put(anc, Boolean.TRUE);
                }
//            printStackStatus(ancestorsOrdered, isVisited);
            }

        }
//        logger.info("Searching DCA: end");
//        logger.info("DCA ("+dca.size()+"): "+dca);
        return lca;
    }
}
