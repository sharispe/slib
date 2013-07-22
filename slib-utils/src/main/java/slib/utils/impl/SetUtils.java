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
package slib.utils.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Sébastien Harispe
 */
public class SetUtils {

    /**
     * Compute the union of two collections
     *
     * @param <X>
     * @param a
     * @param b
     * @return the union
     */
    public static <X> Set<X> union(Collection<X> a, Collection<X> b) {

        Set<X> union = new HashSet<X>(a);
        union.addAll(b);
        return union;
    }

    /**
     * Compute the intersection of two collections
     *
     * @param <X>
     * @param anc_setA
     * @param anc_setB
     * @return the intersection
     */
    public static <X> Set<X> intersection(Collection<X> anc_setA, Collection<X> anc_setB) {
        Set<X> inter = new HashSet<X>(anc_setA);
        inter.retainAll(anc_setB);
        return inter;
    }

    /**
     *
     * @param <X>
     * @param nbOccurrence
     * @return the maximum number as specified as a value of the map.
     */
    public static <X> double getMax(HashMap<X, Integer> nbOccurrence) {

        double max = -Double.MAX_VALUE;

        for (Entry<X, Integer> e : nbOccurrence.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
            }
        }
        return max;
    }

    /**
     * Build a Set<X> containing the given object of type X. If the object is
     * null, null is returned
     *
     * @param <X>
     * @param o
     * @return a Set<X> containing the given object of type X or null
     */
    public static <X> Set<X> buildSet(X o) {
        if (o == null) {
            return null;
        }

        Set<X> set = new HashSet<X>();
        set.add(o);
        return set;
    }
}
