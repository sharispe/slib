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
 

package slib.sml.sm.core.measures.framework.impl.set;

import java.util.Set;

import org.openrdf.model.Resource;

import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sml.sm.core.measures.framework.core.engine.GraphRepresentation;
import slib.sml.sm.core.utils.SM_Engine;
import slib.utils.ex.SLIB_Exception;

/**
 * Graph representation as a set of {@link VClass} corresponding to the set of {@link VClass}
 * contained in the graph induced by the inclusive ancestors of a {@link V}.
 * If the given {@link Resource} is a {@link VClass} the graph representation corresponds to the set of
 * vertices containing the inclusive ancestors of the given class.
 * 
 * @author Sebastien Harispe
 *
 */
public class GraphRepresentationAsSet extends GraphRepresentation {
	
	public Set<V> anc;
	
	/**
	 * 
	 * @param resource the {@link Resource} considered.
	 * @param mnger the {@link SM_Engine} loaded for the processed graph
	 * @throws SGL_Exception
	 */
	public GraphRepresentationAsSet(V resource,SM_Engine mnger) throws SLIB_Exception{
		super(resource);
		
		if(resource.getType().equals( VType.CLASS))
			anc = mnger.getAncestors(resource);
	}
	

	public boolean support(V object) {
			return true;
	}
	
	public String toString(){
		return anc.toString();
	}


	



}
