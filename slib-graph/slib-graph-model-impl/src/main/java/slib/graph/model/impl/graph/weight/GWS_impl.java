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
package slib.graph.model.impl.graph.weight;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.weight.GWS;

/**
 * In memory Graph Weighting Scheme (GWS) implementation of {@link GWS}
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class GWS_impl implements GWS {

    double defaultWeight = 1;
    Map<URI, Double> eTypeWeights;
    Map<E, Double> eWeights;

    /**
     * Create a weighting scheme with the default weight set to 1.
     */
    public GWS_impl() {
    }

    /**
     * Create a weighting scheme with a specific default weight.
     *
     * @param defaultWeight
     */
    public GWS_impl(double defaultWeight) {
        this.defaultWeight = defaultWeight;
    }

    @Override
    public double getDefaultWeight() {
        return defaultWeight;
    }

    @Override
    public void setDefaultWeight(double w) {
        defaultWeight = w;
    }

    @Override
    public boolean existsWeight(E e) {
        if (eWeights != null && eWeights.containsKey(e)) {
            return true;
        }
        return false;
    }

    @Override
    public double getWeight(E e) {

        Double w;

        if (eWeights == null) {

            if (eTypeWeights == null) {
                return defaultWeight;
            } else {
                w = eTypeWeights.get(e.getURI());
                if (w == null) {
                    return defaultWeight;
                }
            }
        } else {
            w = eWeights.get(e);
        }

        if (w == null) {
            return defaultWeight;
        }

        return w;
    }

    @Override
    public void setWeight(E e, double w) {
        if (eWeights == null) {
            eWeights = new HashMap<E, Double>();
        }
        eWeights.put(e, w);
    }

    @Override
    public boolean existsWeight(URI e) {
        if (eTypeWeights != null && eTypeWeights.containsKey(e)) {
            return true;
        }
        return false;
    }

    @Override
    public Double getWeight(URI e) {
        Double w;

        w = eTypeWeights.get(e);

        if (w == null) {
            return defaultWeight;
        }

        return w;
    }

    @Override
    public void setWeight(URI e, double w) {

        if (eTypeWeights == null) {
            eTypeWeights = new HashMap<URI, Double>();
        }
        eTypeWeights.put(e, w);
    }

    @Override
    public String toString() {

        String out = "WS: Configuration\ndefaultWeight " + defaultWeight;
        if (eWeights == null) {
            out += "\neWeights undefined";
        } else {
            out += "\neWeights size " + eWeights.size();
        }

        if (eTypeWeights == null) {
            out += "\neTypeWeights undefined";
        } else {
            out += "\neTypeWeights size " + eTypeWeights.size();
        }

        return out;
    }
}
