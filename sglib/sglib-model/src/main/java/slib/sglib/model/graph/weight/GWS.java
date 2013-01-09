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
 
 
package slib.sglib.model.graph.weight;

import org.openrdf.model.URI;
import slib.sglib.model.graph.elements.E;



/**
 * Object representing a Graph Weighting Scheme containing : <br/>
 * 
 * - a default weight which is return each time a non specified weight is queried (e.g. set to 1). <br/><br/>
 * 
 * - weights associated to each EdgeType founded in the edges it contains.
 * If no weight is explicitly specified for the edge type default weight will be returned. 
 * Note the possibility to check if a weigh is explicitly associated to an edge type.<br/><br/>
 * 
 * - weights associated to each edges.
 * if no weight is explicitly specified to an edge, edge type weight or default weight 
 * is returned depending if corresponding edge type weight is specified.
 * Note the possibility to check if a weigh is explicitly associated to an edge.
 * 
 * @author Sebastien Harispe
 *
 */
public interface GWS {
	
	/**
     *
     * @return
     */
    double getDefaultWeight();
	/**
     *
     * @param w
     */
    void setDefaultWeight(double w);
	
	/**
     *
     * @param e
     * @return
     */
    boolean existsWeight(E e);
	/**
     *
     * @param e
     * @return
     */
    double getWeight(E e);
	/**
     *
     * @param e
     * @param w
     */
    void   addWeight(E e, double w);
	/**
     *
     * @param e
     * @param w
     */
    void   setWeight(E e, double w);
	
	/**
     *
     * @param e
     * @return
     */
    boolean existsWeight(URI e);
	/**
     *
     * @param e
     * @return
     */
    double getWeight(URI e);
	/**
     *
     * @param e
     * @param w
     */
    void addWeight(URI e, double w);
	
	
	/**
	 * 
         * Must propagate the modification are propagated to each edge of the specified predicate.
         * 
	 * @param e
	 * @param w
	 */
	void   setWeight(URI e, double w);

}
