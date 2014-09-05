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
package slib.graph.algo.extraction.rvf;

import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.Direction;
import slib.graph.utils.WalkConstraintGeneric;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Object of this class can be used to retrieve the vertices reachable from a
 * particular vertex of an acyclic graph considering taxonomic relationships.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class RVF_TAX extends RVF_DAG {

    
    
    public RVF_TAX(G g, Direction dir) {
        super(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, dir));
    }

    /**
     * Compute the set of vertices corresponding to classes which can be reach from the given vertex.
     * Exclusive process: the focused vertex will NOT be included in the set of
     * reachable vertices.
     * @param v the vertex of interest
     * @return the set of classes which can be reached from the selected vertex
     */
    public Set<URI> getRVClass(URI v) {
        return getRV(v);
    }

    /**
     * Compute for all vertices of the loaded graph, the set of vertices corresponding to classes which can be reach from them.
     * Exclusive process: a vertex will NOT be included in the set of reachable vertices it can reach.
     * @return the map storing, for each vertex, the set of reachable vertices respecting the above constraint.
     * @throws SLIB_Ex_Critic  
     */
    public Map<URI, Set<URI>> getAllRVClass() throws SLIB_Ex_Critic {
        return getAllRV();
    }
}
