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
package slib.sml.sm.core.metrics.ic.utils;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 *
 * Class used to defined static methods utility.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class IcUtils {

    /**
     * Search the Most Informative Concept Shared between two sets, regarding
     * the given metric. This method is particularly suited to the search of the
     * Most Informative Common Ancestor, considering the input sets as the
     * inclusive ancestors of the concepts for which we search the MICAS.
     *
     * @param a
     * @param b
     * @param setA the first set
     * @param setB the second set
     * @param icScores the metric result to consider to evaluate the specificity
     * of a concept
     * @return the Most Informative concept regarding the given metric results,
     * null if none is found
     * @throws SLIB_Exception
     */
    public static URI searchMICA(
            URI a,
            URI b,
            Set<URI> setA,
            Set<URI> setB,
            Map<URI, Double> icScores) throws SLIB_Exception {

        URI mica = null;

        if (a.equals(b)) {
            mica = a;
        } else {

            Set<URI> intersec = SetUtils.intersection(setA, setB);



            if (intersec.isEmpty()) {
                throw new SLIB_Ex_Critic("Error detecting the common ancestors with the maximal IC\nSearching a max from an empty collection, be sure the compare concepts are locate under the specified root...");
            } else if (icScores == null) {
                throw new SLIB_Ex_Critic("Empty IC result stack... Treatment cannot be performed");
            }

            Iterator<URI> it = intersec.iterator();
            double max = -Double.MAX_VALUE;

            while (it.hasNext()) {


                URI v = it.next();

                if (mica == null || max < icScores.get(v)) {
                    max = icScores.get(v);
                    mica = v;
                }
            }
        }
//        System.out.println("\t" + a + "\t" + b+"\t"+mica);
        return mica;
    }

    /**
     * Search the IC of Most Informative Concept Shared between two sets,
     * regarding the given metric. This method is particularly suited to the
     * search of IC of the Most Informative Common Ancestor, considering the
     * input sets as the inclusive ancestors of the concepts for which we search
     * the MICAS.
     *
     * @param a
     * @param b
     * @param setA the first set
     * @param setB the second set
     * @param icScores the metric result to consider to evaluate the specificity
     * of a concept
     * @return the IC of the Most Informative concept regarding the given metric
     * results, null if none is found
     *
     * @throws SLIB_Exception
     */
    public static Double searchMax_IC_MICA(
            URI a,
            URI b,
            Set<URI> setA,
            Set<URI> setB,
            Map<URI, Double> icScores) throws SLIB_Exception {

        URI mica = searchMICA(a, b, setA, setB, icScores);


        return icScores.get(mica);
    }

    /**
     * Search the IC of Less Informative Concept Shared between two sets,
     * regarding the given metric.
     *
     * @param setA the first set
     * @param setB the second set
     * @param icScores the metric result to consider to evaluate the specificity
     * of a concept
     * @return the IC of the Less Informative concept regarding the given metric
     * results, null if none is found
     *
     * @throws SLIB_Exception
     */
    public static Double searchMin_pOc_MICA(
            Set<URI> setA,
            Set<URI> setB,
            Map<URI, Double> icScores) throws SLIB_Exception {

        Set<URI> intersec = SetUtils.intersection(setA, setB);

        Double min = null;

        if (!intersec.isEmpty() && icScores != null) {

            Iterator<URI> it = intersec.iterator();
            min = icScores.get(it.next());
            URI v;
            while (it.hasNext()) {
                v = it.next();
                if (min > icScores.get(v)) {
                    min = icScores.get(v);
                }
            }
        }
        return min;
    }
}
