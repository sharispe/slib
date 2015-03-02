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
package com.github.sharispe.slib.dsm.utils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * Utility functions used to manipulate Maps Some of the source code has been
 * adapted from Adapted from
 * http://stackoverflow.com/questions/109383/how-to-sort-a-mapkey-value-on-the-values-in-java
 */
public class MapUtils {

    /**
     * Sort a map by value (increasing order)
     *
     *
     * @param <K>
     * @param <V>
     * @param map
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {

        return sortByValue(map, new Comparator<Entry<K, V>>() {

            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

    }

    /**
     * Sort a map by value (decreasing order)
     *
     * @param <K>
     * @param <V>
     * @param map
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDecreasing(Map<K, V> map) {

        return sortByValue(map, new Comparator<Entry<K, V>>() {

            @Override
            public int compare(Entry<K, V> o1, Entry<K, V> o2) {
                return -(o1.getValue()).compareTo(o2.getValue());
            }
        });
    }

    /**
     * Sort a map by value (increasing order)
     *
     * @param <K>
     * @param <V>
     * @param map
     * @param comparator
     * @return
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, Comparator<Entry<K, V>> comparator) {

        List<Entry<K, V>> list = new LinkedList(map.entrySet());

        Collections.sort(list, comparator);

        Map<K, V> result = new LinkedHashMap();
        for (Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static <K, V> Map<V, K> revert(Map<K, V> map) {
        Map<V, K> maprevert = new HashMap();
        for (Map.Entry<K, V> e : map.entrySet()) {
            maprevert.put(e.getValue(), e.getKey());
        }
        return maprevert;
    }

    public static <K, V> void toFile(Map<K, V> res, String output) throws IOException {
        int c = 0;
        try (PrintWriter writer = new PrintWriter(output, "UTF-8")) {
            for (Map.Entry<K, V> e : res.entrySet()) {
                writer.println(c+"\t"+e.getKey().toString() + "\t" + e.getValue().toString());
                c++;
            }
        }
    }
}
