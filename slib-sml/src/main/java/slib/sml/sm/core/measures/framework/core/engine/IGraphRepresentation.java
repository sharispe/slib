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

package slib.sml.sm.core.measures.framework.core.engine;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import slib.sglib.model.graph.elements.V;



/**
 * Interface of a Graph Representation associated to a {@link V}
 * As an example a concept can be seen as the graph induced by its inclusive ancestors
 * or only by the set of vertices corresponding to its ancestors.
 * 
 * @author Sebastien Harispe
 *
 */
public interface IGraphRepresentation{

	/**
	 * Evaluate if the given graph representation supports the given Resource i.e.
	 * if the resource can be represented by this graph representation. 
	 * Some graph representation are only supported for V.
	 * 
	 * @param resource the {@link Resource} to evaluate
	 * @return true if the given {@link Resource} can be represented by the {@link IGraphRepresentation}
	 */
	boolean support(V resource);
	
	/**
	 * Get the resource associated to the graph representation
	 * @return the {@link Resource} associated to the {@link GraphRepresentation}
	 */
	V getResource();
	
	/**
	 * Get the URI of the resource associated to the graph representation
	 * @return the {@link URI} of the {@link Resource} associated to the {@link GraphRepresentation}
	 */
	Value getResourceValue();
}
