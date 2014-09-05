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
package slib.graph.algo.utils;

/**
 * Enumeration used to represent the various types of actions which can be
 * applied to a graph.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public enum GActionType {

    /**
     * Perform a transitive reduction of the relationships RDFS.SUBCLASSOF
     * and/or RDF.TYPE - this treatment removes the relationships which can be
     * removed according to the transitivity of the predicate rdfs:subClassOf.
     * You can specify the type of relationships on which the treatment must be
     * performed using the parameter "target" with value:
     * <ul>
     * <li>CLASSES or rdfs:subClassOf or RDFS.SUBCLASSOF (upper or lower case)
     * to remove relationships rdfs:subClassOf which can be inferred </li>
     * <li>INSTANCES or rdf:type or RDF.TYPE (upper or lower case) to remove
     * relationships rdf:type which can be inferred </li>
     * <li>you can use both using a comma separator setting
     * "CLASSES,INSTANCES"</li>
     * </ul>
     */
    TRANSITIVE_REDUCTION,
    /**
     * Root the graph according to the rdfs:subClassOf relationship.
     *
     * For each URI x which is involved in a statement in which the predicate
     * rdfs:subClassOf is used, if no statement x rdfs:subClassOf y exists, x is
     * considered to refer to a root. In this treatment, for each root x a
     * statement x rdfs:subClassOf new_root is created. The value of new_root
     * can be defined automatically or manually see below.
     *
     * The root URI can be specified using the parameter "root_uri":
     * <ul>
     * <li>the value must refer to the URI to consider for the root. It can be
     * an URI which is not already used in the graph.</li>
     * <li>"__FICTIVE__" as value will be substituted by OWL.THING, i.e. refers
     * to the OWL vocabulary in the Sesame API</li>
     * </ul>
     */
    REROOTING,
    /**
     * Remove of the set of vertices composing the graph.
     *
     * Accepted parameters are:
     *
     * <ul>
     *
     * <li> regex: specify a REGEX in Java syntax which will be used to test if
     * the value associated to a vertex makes it eligible to be removed. If the
     * value match the REGEX, the vertex will be removed </li>
     *
     * <li> vocabulary: Remove all the vertices associated to the vocabularies
     * specified. Accepted vocabularies flag are RDF, RDFS, OWL. Several
     * vocabularies can be specified using comma separator. </li>
     *
     * <li> file_uris: specify a list of files containing URIs corresponding to
     * the vertices to remove. Multiple files can be specified using comma
     * separator. </li>
     *
     * </ul>
     *
     */
    VERTICES_REDUCTION,
    /**
     * Can be used to substitute the predicate of all the triplets with a
     * specific predicate.
     *
     * parameters expected:
     * <ul>
     * <li>old_uri: the URI predicate to replace</li>
     * <li>new_uri: the new URI predicate</li>
     * </ul>
     * You can use RDFS.SUBCLASSOF to refer to
     * http://www.w3.org/2000/01/rdf-schema#subClassOf
     *
     */
    PREDICATE_SUBSTITUTE
}
