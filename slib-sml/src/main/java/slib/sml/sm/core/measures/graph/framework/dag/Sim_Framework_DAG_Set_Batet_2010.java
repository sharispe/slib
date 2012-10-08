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
 
 
package slib.sml.sm.core.measures.graph.framework.dag;

import java.util.Set;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.utils.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.impl.SetUtils;

/**
 * ﻿1. Batet M, Sanchez D, Valls A: An ontology-based measure to compute semantic similarity in biomedicine. 
 * Journal of biomedical informatics 2010, 44:118-125.
 * 
 * IC is normalize considering spirit formulated in given by Faria and al in order to produce results [0,1].
 * ﻿Faria D, Pesquita C, Couto FM, Falcão A: Proteinon: A web tool for protein semantic similarity. 2007.
 * 
 * @author Sebastien Harispe
 */
public class Sim_Framework_DAG_Set_Batet_2010 extends Sim_Framework_DAG_Set_abstract{
	
	
	@Override
	public double sim(V a, V b, SM_Engine c,SMconf conf) {
		
		int nbV = c.getNbVertices();
		Set<V> ancA = c.getAncestors(a);
		Set<V> ancB = c.getAncestors(b);
		
		return sim(nbV,ancA, ancB);
	}
	
	@Override
	public double sim(Set<V> setA, Set<V> setB, SM_Engine c,SMconf conf) {
		
		int nbV = c.getNbVertices();
		Set<V> ancA = c.getAncestors(setA);
		Set<V> ancB = c.getAncestors(setB);
		
		return sim(nbV,ancA, ancB);
	}
	
	
	
	public double sim(Set<V> ancA, Set<V> ancB, SMconf conf) {
		
		
		Set<V> interSecAncestors = SetUtils.intersection(ancA, ancB);
		Set<V> unionAncestors    = SetUtils.union(ancA, ancB);
		
		double num = unionAncestors.size() - interSecAncestors.size();
		
		
		
		double batet;
		
		if(num == 0)
			batet = 1;
		else{
			batet = - Math.log( (double) num / unionAncestors.size());
			batet = batet / Math.log(unionAncestors.size());
		}
		return batet;
	}
	
	/**
	 * Normalized version
	 * @param nbVertices
	 * @param a
	 * @param b
	 * @return
	 */
	public double sim(int nbVertices, Set<V> a, Set<V> b) {
		
		Set<V> interSecAncestors = SetUtils.intersection(a, b);
		Set<V> unionAncestors    = SetUtils.union(a, b);
		
		double num = unionAncestors.size() - interSecAncestors.size();
		double batet;
		
		if(num == 0)
			batet = 1;
		else{
			
			batet = - Math.log( (double) num / unionAncestors.size());
			batet = batet / Math.log(nbVertices);
		}
		return batet;
	}




}
