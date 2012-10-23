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
package slib.sglib.algo.utils;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.validator.dag.ValidatorDAG;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 * Object used to root a graph taking into consideration edge types defining
 * roots {@link URICommon#UNIVERSAL_ROOT} can be used as a generic root.
 *
 * @author Sebastien Harispe
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
     * @param etypeDAG the set of edge types used to detect the underlying DAG.
     * The inverse of etypeDAG is used to detect uDAG roots.
     * @param checkUnderlyingDAG if true DAG conformity of the underlying graph
     * induced by etypeDAG is checked. If checkUnderlyingDAG is set to true and
     * uDAG DAG property is not validated an {@link SGL_Ex_Critic} exception is
     * thrown.
     * @param rootURI the URI of the vertex to consider as root if rooting
     * requires to be performed. If the URI is not associated to graph vertex
     * the vertex is added. If the vertex already exists impact of using it on
     * uDAG DAG property is not evaluated. If you are not inspired, note that
     * {@link URICommon#UNIVERSAL_ROOT} can be used as root.
     * @return the URI of the uDAG root vertex.
     *
     * @throws SGL_Ex_Critic
     */
    public static URI rootUnderlyingDAG(G g, Set<URI> etypeDAG, boolean checkUnderlyingDAG, URI rootUri, Direction dir) throws SLIB_Ex_Critic {

        Logger logger = LoggerFactory.getLogger(RooterDAG.class);

        ValidatorDAG validator = new ValidatorDAG();

        if (checkUnderlyingDAG && !validator.isDag(g, etypeDAG, dir)) {
            throw new SLIB_Ex_Critic("Error during rerooting: "
                    + "Underlying graph build from the set of edge types " + etypeDAG + ""
                    + "is not a DAG");
        }

        URI rootURI_ = null;

        // roots are considered as vertices 
        // - with out edge of type etypeDAG (vertices contained in uDAG)
        // - without out edges of type invEtypeDAG
        Set<V> roots = new ValidatorDAG().getDAGRoots(g, etypeDAG, dir);

        int nbRoot = roots.size();

        if (nbRoot == 1) {
            logger.info("Rooting skipped : Graph already rooted");
            rootURI_ = (URI) roots.iterator().next();
        } else {
            logger.info("Number of roots detected: " + roots.size());
            V root = g.createVertex(rootUri);

            // add Root -> oldRoots relationships

            for (URI type : etypeDAG) {
                for (V v : roots) {
                    g.addE(v, root, type);
                }
            }

            logger.info("Rooting performed");
        }
        return rootURI_;
    }

    /**
     * Shortcut of {@link RooterDAG#rootUnderlyingDAG(G, Set, boolean, URI)}
     * only considering an edgeType
     *
     * @see RooterDAG#rootUnderlyingDAG(G, Set, boolean, URI)
     * @throws SGL_Ex_Critic
     */
    public static URI rootUnderlyingDAG(G g, URI eType, boolean checkUnderlyingDAG, URI rootUri, Direction dir) throws SLIB_Ex_Critic {
        return rootUnderlyingDAG(g, SetUtils.buildSet(eType), checkUnderlyingDAG, rootUri, dir);
    }

    /**
     * Shortcut of {@link RooterDAG#rootUnderlyingDAG(G, Set, boolean, URI)}
     * only considering Taxonomic relationships i.e SUBCLASSOF/SUPERCLASSOF
     *
     * @see RooterDAG#rootUnderlyingDAG(G, Set, boolean, URI)
     * @throws SGL_Ex_Critic
     */
    public static URI rootUnderlyingTaxonomicDAG(G g, URI rootUri) throws SLIB_Ex_Critic {

        Logger logger = LoggerFactory.getLogger(RooterDAG.class);
        logger.info("Rooting taxonomic Graph");
        ValidatorDAG validator = new ValidatorDAG();

        if (!validator.containsRootedTaxonomicDag(g)) {
            return rootUnderlyingDAG(g, RDFS.SUBCLASSOF, true, rootUri, Direction.OUT);
        } else {
            return (URI) validator.getRootedTaxonomicDAGRoot(g).getValue();
        }
    }
}
