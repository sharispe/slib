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


package slib.sml.sm.core.metrics.ic.topo;

import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;

import slib.sglib.algo.traversal.classical.DFS;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.utils.SM_manager;
import slib.utils.ex.SGL_Exception;
import slib.utils.impl.ResultStack;

import com.tinkerpop.blueprints.Direction;


/**
 * Implementation of the 2012 Mazandu et al. IC.
 * 
 * Gaston K. Mazandu and Nicola J. Mulder, 
 * “A Topology-Based Metric for Measuring Term Similarity in the Gene Ontology,” 
 * Advances in Bioinformatics, vol. 2012, Article ID 975783, 17 pages, 2012. 
 * doi:10.1155/2012/975783
 * http://www.hindawi.com/journals/abi/2012/975783/
 * 
 * @author Sebastien Harispe
 */
public class ICi_mazandu_2012 implements ICtopo{

	public ResultStack<V,Double> compute( SM_manager manager) throws SGL_Exception{

		ResultStack<V,Double> results = new ResultStack<V,Double>(this.getClass().getSimpleName());

		V root = manager.getRoot();

		Set<URI> goToSubclass   = manager.getGoToSuperClassETypes();

		G g = manager.getGraph();

		DFS dfs = new DFS(g, root, goToSubclass,Direction.IN);
		List<V> topoOrder = dfs.getTraversalOrder();

		boolean isRoot = true;
		for(int i = topoOrder.size()-1; i >= 0; i--){
			
			V v = topoOrder.get(i); 

			double mu = 0;
			if(isRoot){
				isRoot = false;
				mu = 1.;
			}
			else{
				Set<E> edgesToparents = g.getE(goToSubclass, v,Direction.OUT);


				for(E e : edgesToparents){

					V parent = e.getTarget();
					double nbChildrenParent = g.getE(goToSubclass, parent,Direction.IN).size();
					double muParent 		= results.get(parent);

					mu += muParent/nbChildrenParent;
				}
			}
			results.add(v, mu);
		}
		
		return results;
	}

	public ResultStack<V,Double> compute(IC_Conf_Topo conf, SM_manager manager)
			throws SGL_Exception {
		return compute(manager);
	}
}
