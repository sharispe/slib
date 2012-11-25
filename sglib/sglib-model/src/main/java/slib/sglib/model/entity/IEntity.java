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
package slib.sglib.model.entity;

import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;

import slib.sglib.model.graph.elements.V;

/**
 * Defines the generic interface of an Entity i.e representation of a generic
 * element (e.g. document, protein...) annotated by graph vertices. As an Entity
 * can be linked to graph vertex a through explicit semantic relationships
 * (graph edges) the Entity class extends the V interface. A common example of
 * entity linked to the semantic graph is encountered when IEntity is used in
 * order to represent instance of class represented in the graph. Each entities
 * are then characterized by a unique URI. A set of annotations (also associated
 * to vertices) characterized the Entity. The annotations can be semantic linked
 * to the Entity if a semantic relationship is expressed or not.
 *
 * Nevertheless an Entity can also be an element not linked to a particular
 * class represented in the semantic graph.
 *
 * This interface can be extended to associate particular information to the
 * Entity.
 *
 * @author Sebastien Harispe
 *
 */
public interface IEntity {

    /**
     * To retrieve the annotations characterizing the entity
     *
     * @return a Set of annotation objects
     */
    public Map<V, URI> getAnnotMapping();

    /**
     * To retrieve the annotations characterizing the entity
     *
     * @return a Set of annotation objects
     */
    public Set<V> getAnnotations(URI puri);

    /**
     * Remove a particular Annotation associated to the Entity
     *
     * @param annotationURI identifier of the annotation to remove to the entity
     * collection
     * @boolean return true if the annotation was removed
     */
    public void removeAnnotations(URI puri);

    public void addAnnotation(V v, URI puri);

    /**
     * Remove a particular Annotation associated to the Entity
     *
     * @param annotationURI identifier of the annotation to remove to the entity
     * collection
     * @boolean return true if the annotation was removed
     */
    public void removeAnnotations(V v);

    public URI getURI();

    /**
     * Vertex equals method must delegate to {@link URI#equals(Object)}
     *
     * @param o an Object
     * @return true if the entity is equals to the given object considering it
     * given {@link URI} if one exists
     */
    @Override
    public boolean equals(Object o);
}
