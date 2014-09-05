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
package slib.graph.algo.extraction.utils;

import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.utils.WalkConstraintGeneric;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Object used to root a graph taking into consideration edge types defining
 * roots.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class RooterDAG {

    /**
     * Add a unique root to a DAG (underlying DAG) composed of multiple roots.
     * <br/> The processed graph must be/contains a DAG according to a specified
     * set of edge type here called EtypeDAG. In other words the reduction of
     * the given graph build only considering the edges (and the involved nodes)
     * of a type contained in EtypeDAG must be a DAG. This reduction of the
     * given graph is here called the underlying DAG (uDAG). Roots are defined
     * as all terminal vertices of the uDAG according to invEtypeDAG the inverse
     * of set of edge type composing EtypeDAG i.e. vertices of uDAG without out
     * edges of a type contained in invEtypeDAG. i.e. vertices without out edges
     * of type EtypeRoot. uDAG is then ensured to be connected <a
     * href="http://en.wikipedia.org/wiki/Connectivity_(graph_theory)"> (more
     * about) </a> after processing if uDAG is already rooted no treatment
     * performed, specified root is not added and the current root URI is
     * returned <br/> <br/>
     *
     * Example <br/> <br/> input G a cyclic graph containing multiple taxonomic
     * graphs (DAG considering SUPERCLASSOF/SUBCLASSOF relationships) <br/>
     * EtypeDAG = SUPERCLASSOF <br/>
     *
     * uDAG the unconnected graph composed of the underlying taxonomic graphs
     * <br/> invEtypeDAG = SUBCLASSOF <br/> roots : all uDAG graphs which are
     * not subsumed by a vertex <br/> process -> add a subsuming vertex
     * (rootURI) for each vertices in roots if the set of root is upper than 1
     * (no unique root) <br/>
     *
     * @param g the graph to root
     * @param wc the constraints associated to the walk.
     * @param checkUnderlyingDAG if true DAG conformity of the underlying graph
     * induced by etypeDAG is checked. If checkUnderlyingDAG is set to true and
     * uDAG DAG property is not validated an critical exception is
     * thrown.
     * @param rootUri 
     * @return the URI of the root
     * @throws SLIB_Ex_Critic  
     *
     */
    public static URI rootUnderlyingDAG(G g, URI rootUri, WalkConstraint wc, boolean checkUnderlyingDAG) throws SLIB_Ex_Critic {

        Logger logger = LoggerFactory.getLogger(RooterDAG.class);

        ValidatorDAG validator = new ValidatorDAG();

        if (checkUnderlyingDAG && !validator.isDag(g, wc)) {
            throw new SLIB_Ex_Critic("Error during rerooting: "
                    + "Underlying graph build from  contraint " + wc + ""
                    + "is not a DAG");
        }

        URI rootURI_;

        // roots are considered as vertices 
        // - with out edge of type etypeDAG (vertices contained in uDAG)
        // - without out edges of type invEtypeDAG
        Set<URI> roots = new ValidatorDAG().getDAGRoots(g, wc);

        int nbRoot = roots.size();

        if (nbRoot == 1) {
            logger.info("Rooting skipped : Graph already rooted");
            rootURI_ = roots.iterator().next();
        } else {
            logger.info("Number of roots detected: " + roots.size());
            g.addV(rootUri);
            
            if(roots.contains(rootUri)){
                roots.remove(rootUri);
            }

            rootURI_ = rootUri;
            // add Root -> oldRoots relationships

            long c = 0;

            for (URI v : roots) {
                c++;
                g.addE(v, RDFS.SUBCLASSOF, rootUri);
            }
            
            
            logger.info("Rooting performed using " + rootURI_ + " as root " + c + " edges created");
            logger.debug(" Contains Rooted taxonomic DAG " + validator.containsTaxonomicDagWithUniqueRoot(g));
        }

        return rootURI_;
    }


    /**
     * Root the underlying taxonomic DAG of the specified graph.
     *
     * @param g 
     * @param rootUri 
     * @return the URI of the root
     * @throws SLIB_Ex_Critic 
     */
    public static URI rootUnderlyingTaxonomicDAG(G g, URI rootUri) throws SLIB_Ex_Critic {

        Logger logger = LoggerFactory.getLogger(RooterDAG.class);
        logger.info("Rooting taxonomic Graph using " + rootUri);
        ValidatorDAG validator = new ValidatorDAG();

        if (!validator.containsTaxonomicDagWithUniqueRoot(g)) {
            return rootUnderlyingDAG(g, rootUri, new WalkConstraintGeneric(RDFS.SUBCLASSOF,Direction.OUT),true);
        } else {
            return validator.getUniqueTaxonomicRoot(g);
        }
    }
}
