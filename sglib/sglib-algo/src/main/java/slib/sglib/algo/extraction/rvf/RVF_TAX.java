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
 
 
package slib.sglib.algo.extraction.rvf;

import java.util.Map;
import java.util.Set;

import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.utils.WalkConstraintTax;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.utils.ex.SLIB_Ex_Critic;


/**
 * Object of this class can be used to retrieve the vertices reachable from a particular
 * vertex of an acyclic graph considering particular relationships i.e. EdgeTypes 
 * 
 * @author Sebastien Harispe
 *
 */
public class RVF_TAX extends RVF_DAG{


	Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * @param g		the graph
	 * @param eType the type of relationships admitted during traversal.
	 */
	public RVF_TAX(G g, Direction dir){
		super(g, new WalkConstraintTax(RDFS.SUBCLASSOF,dir));
	}

	
	public Set<V> getRVClass(V v) {
		return getRV(v);
	}

	/**
         * Compute all reachable vertices for all vertices
         * Exclusive process: note that the set of reachable vertices from V does not contain V
         * @return
         * @throws SLIB_Ex_Critic 
         */
	public Map<V, Set<V>> getAllRVClass() throws SLIB_Ex_Critic {
		return getAllVertices();
	}
}
