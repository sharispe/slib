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
package slib.graph.model.impl.graph.elements;

import org.openrdf.model.URI;
import slib.graph.model.graph.elements.E;

/**
 * Implementation of the {@link E} interface.
 * @see E
 * 
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Edge implements E {

    private final URI source;
    private final URI target;
    private final URI predicate;

    /**
     * Create an edge considering the given arguments.
     *
     * @param source the source of the edge, must not be null
     * @param predicate the predicate (type) of the edge, must not be null
     * @param target the target of the edge, must not be null
     */
    public Edge(URI source, URI predicate, URI target) {
        this.source = source;
        this.predicate = predicate;
        this.target = target;

        if (source == null || predicate == null || target == null) {
            throw new IllegalArgumentException("Error creating edge, subject predicate and object must not be null, specified values " + toString());
        }
    }

    @Override
    public URI getSource() {
        return source;
    }

    @Override
    public URI getTarget() {
        return target;
    }

    @Override
    public URI getURI() {
        return predicate;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.source != null ? this.source.hashCode() : 0);
        hash = 79 * hash + (this.target != null ? this.target.hashCode() : 0);
        hash = 79 * hash + (this.predicate != null ? this.predicate.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Edge other = (Edge) obj;
        if (this.source != other.source && (this.source == null || !this.source.equals(other.source))) {
            return false;
        }
        if (this.target != other.target && (this.target == null || !this.target.equals(other.target))) {
            return false;
        }
        if (this.predicate != other.predicate && (this.predicate == null || !this.predicate.equals(other.predicate))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return source.stringValue() + " -- " + predicate.stringValue() + " -- " + target.stringValue();
    }
}
