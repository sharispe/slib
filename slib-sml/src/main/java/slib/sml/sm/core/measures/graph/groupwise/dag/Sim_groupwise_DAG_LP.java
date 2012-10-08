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
 
 
package slib.sml.sm.core.measures.graph.groupwise.dag;

import java.util.Set;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.utils.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SGL_Exception;
import slib.utils.impl.ResultStack;
import slib.utils.impl.SetUtils;

/**
 * 
 * 1. Gentleman R: Visualizing and distances using GO. Retrieved Jan. 10th 2007.
 * http://www.bioconductor.org/packages/release/bioc/vignettes/GOstats/inst/doc/GOvis.pdf
 * 
 * Extract : 
 * For simLP the similarity measure is the depth of the longest shared path from the
 * root node. Two genes that are both quite specific and similar should have long shared
 * paths, while those that have less in common should have relatively short shared paths.
 * 
 * We understand the depth of the most profound lca of each entity couples
 * 
 * @author seb
 */
public class Sim_groupwise_DAG_LP extends Sim_groupwise_DAG_abstract{

	public double sim(Set<V> setA, Set<V> setB, SM_Engine rc, SMconf conf) throws SGL_Exception {
		
		Set<V> ancestors_setA = rc.getAncestors(setA);
		Set<V> ancestors_setB = rc.getAncestors(setB);
		
		
		Set<V> commonAncestors = SetUtils.intersection(ancestors_setA, ancestors_setB);
		
		// search max depth in commonAncestors
		
		ResultStack<V,Integer> maxDepths = rc.getMaxDepths();
		int maxDepth = 0;
		
		for (V r : commonAncestors) {
			if(maxDepths.get(r) > maxDepth)
				maxDepth = maxDepths.get(r);
		}
		return maxDepth;
	}
}

