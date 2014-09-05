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
package slib.graph.model.graph.weight;

import org.openrdf.model.URI;
import slib.graph.model.graph.elements.E;

/**
 * Object representing a Graph Weighting Scheme containing :
 *
 * <ul>
 * <li> a default weight which is return each time a non specified weight is
 * queried (e.g. set to 1). </li>
 * <li>weights associated to each type (predicate). If no weight is explicitly
 * specified for the edge type default weight will be returned. </li>
 * <li>weights associated to each edges. if no weight is explicitly specified to
 * an edge, edge type weight or default weight is returned depending if
 * corresponding edge type weight is specified.
 * </li>
 * </ul>
 *
 *
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public interface GWS {

    /**
     * The default weight associated to an edge
     *
     * @return the default weight
     */
    double getDefaultWeight();

    /**
     * Set the weight defined by default
     *
     * @param w the new default weight
     */
    void setDefaultWeight(double w);

    /**
     * Check if a specific weight is specified for this edge. This method will
     * return false no specific weight is specified, even if a weight is
     * specified for the edges with the same URIs as the specified edge.
     *
     * @param e the edge we want to check if a weight is specified for.
     * @return true if a weight is explicitly defined for the edge.
     */
    boolean existsWeight(E e);

    /**
     * Access to the weight of the edge. The value return is the first from
     * below:
     * <ul>
     *  <li>The specific weight associated to the edge</li>
     *  <li>The specific weight associated to predicate (type) of the edge</li>
     *  <li>The default weight</li>
     * </ul>
     *
     * @param e the edge
     * @return the weight associated to the edge
     */
    double getWeight(E e);

    /**
     * Set the weight for an edge
     *
     * @param e the edge
     * @param w the weight
     */
    void setWeight(E e, double w);

    /**
     * Check if a weight is defined for a specific predicate.
     *
     * @param predicate
     * @return true if a default weight exists for this predicate
     */
    boolean existsWeight(URI predicate);

    /**
     * Access to the weight defined for a specific predicate.
     *
     * @param predicate the predicate
     * @return the default weight associated to the predicate. Null if no weight
     * is associated to the predicate.
     */
    Double getWeight(URI predicate);

    /**
     * Set the default weight for the specified URI. 
     * @param e the default URI
     * @param w the weight
     */
    void setWeight(URI e, double w);
}
