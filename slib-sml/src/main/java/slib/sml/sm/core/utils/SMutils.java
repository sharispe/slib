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
package slib.sml.sm.core.utils;

import java.util.Map;
import org.openrdf.model.URI;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to define utility methods.
 *
 * @author Harispe Sébastien
 */
public class SMutils {

    /**
     * Used to generate a {@link SLIB_Ex_Critic} for a pairwise comparison
     * considering the given parameters.
     *
     * @param pairwiseConf the pairwise measure configuration to consider
     * @param a the first vertex
     * @param b the second vertex
     * @param value
     * @throws SLIB_Ex_Critic
     */
    public static void throwArithmeticCriticalException(SMconf pairwiseConf, URI a, URI b, Object value) throws SLIB_Ex_Critic {

        throw new SLIB_Ex_Critic("Critical error. \n"
                + "A result produced by the pairwise measure " + pairwiseConf.flag + " was not a number " + value + " (NaN/Infinity). \n"
                + "Pairwise measure in use " + pairwiseConf.flag + " id=" + pairwiseConf.id + ". \n"
                + "Classes compared " + a + " vs " + b + ". \n"
                + "This issue can be encountred if an infinite value have been detected.\n"
                + "Please report the bug to the developpers\n");
    }

    /**
     * Used to generate a {@link SLIB_Ex_Critic} for a groupwise comparison
     * considering the given parameters.
     *
     * @param m groupwise measure configuration
     * @param p pairwise measure configuration
     * @param e1 the first entity compared
     * @param e2 the second entity compared
     * @param value the value obtained during the comparison
     * @throws SLIB_Ex_Critic
     */
    public static void throwArithmeticCriticalException(SMconf m, SMconf p, URI e1,
            URI e2, Object value) throws SLIB_Ex_Critic {
        throw new SLIB_Ex_Critic("Critical error. \n"
                + "A result produced by the measure " + m.flag + " was not a number " + value + " (NaN/infinity). \n"
                + "Pairwise measure in use " + p.flag + " id=" + p.id + ". \n"
                + "Entities compared " + e1 + " vs " + e2 + ". \n"
                + "This issue can be encountred if an infinite value have been detected.\n"
                + "Please report the bug to the developpers\n");
    }

    public static <X> Double getMinStrictPositiveDouble(Map<X, Double> m) {
        double max = 0;

        for (Map.Entry<X, Double> e : m.entrySet()) {
            if (e.getValue() > max) {
                max = e.getValue();
            }
        }
        if (max != 0) {
            return max;
        } else {
            return null;
        }
    }
    
    
}
