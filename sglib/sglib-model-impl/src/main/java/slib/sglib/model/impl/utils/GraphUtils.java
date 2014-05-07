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
package slib.sglib.model.impl.utils;

import org.openrdf.model.URI;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphUtils {

    /**
     * Build a triplet of URIs considering the given subject-predicate-object
     * objects (for instance, specified as URI or String objects). The URIs
     * which compose the triplet can be specified as URI objects, as String
     * objects or using any objects for which the toString method returns a
     * valid URI.
     *
     * @param s the URI which refers to the subject of the triplet (possibly as
     * a String)
     * @param p the URI which refers to the predicate of the triplet (possibly
     * as a String)
     * @param o the URI which refers to the object of the triplet (possibly as a
     * String)
     * @return the Edge
     */
    public static E buildTriplet(Object s, Object p, Object o) {
        return buildTriplet(URIFactoryMemory.getSingleton(), s, p, o);
    }

    /**
     * Build a triplet of URIs considering the given subject-predicate-object
     * objects as String objects.
     *
     * @param s the URI which refers to the subject of the triplet
     * @param p the URI which refers to the predicate of the triplet
     * @param o the URI which refers to the object of the triplet
     * @return the Edge
     */
    public static E buildTriplet(String s, String p, String o) {
        return buildTriplet(URIFactoryMemory.getSingleton(), s, p, o);
    }

    /**
     * Build a triplet of URIs considering the given subject-predicate-object
     * objects as String objects.
     *
     * @param f the factory which will be used to generate the URIs
     * @param s the URI which refers to the subject of the triplet
     * @param p the URI which refers to the predicate of the triplet
     * @param o the URI which refers to the object of the triplet
     * @return the Edge
     */
    public static E buildTriplet(URIFactory f, String s, String p, String o) {
        return new Edge(f.getURI(s), f.getURI(p), f.getURI(o));
    }

    /**
     * Build a triplet of URIs considering the given subject-predicate-object
     * objects (for instance, specified as URI or String objects). The URIs
     * which compose the triplet can be specified as URI objects, as String
     * objects or using any objects for which the toString method returns a
     * valid URI.
     *
     * @param f the factory used to build the URIs if necessary
     * @param s the URI which refers to the subject of the triplet (possibly as
     * a String)
     * @param p the URI which refers to the predicate of the triplet (possibly
     * as a String)
     * @param o the URI which refers to the object of the triplet (possibly as a
     * String)
     * @return the Edge
     */
    public static E buildTriplet(URIFactory f, Object s, Object p, Object o) {
        URI sURI = s instanceof URI ? (URI) s : f.getURI(s.toString());
        URI pURI = p instanceof URI ? (URI) p : f.getURI(p.toString());
        URI oURI = o instanceof URI ? (URI) o : f.getURI(o.toString());
        return new Edge(sURI, pURI, oURI);
    }

    /**
     * Add a triplet to the given graph. The URIs which compose the triplet can
     * be specified as URI objects, as String objects or using any objects for
     * which the toString method returns a valid URI.
     *
     * @param graph
     * @param s the URI which refers to the subject of the triplet (possibly as
     * a String)
     * @param p the URI which refers to the predicate of the triplet (possibly
     * as a String)
     * @param o the URI which refers to the object of the triplet (possibly as a
     * String)
     */
    public static void addTriplet(G graph, Object s, Object p, Object o) {
        graph.addE(buildTriplet(s, p, o));
    }

    /**
     * Add a triplet to the given graph. The URIs which compose the triplet are
     * specified as String objects.
     *
     * @param graph
     * @param s the URI which refers to the subject of the triplet
     * @param p the URI which refers to the predicate of the triplet
     * @param o the URI which refers to the object of the triplet
     */
    public static void addTriplet(G graph, String s, String p, String o) {
        graph.addE(buildTriplet(s, p, o));
    }

}
