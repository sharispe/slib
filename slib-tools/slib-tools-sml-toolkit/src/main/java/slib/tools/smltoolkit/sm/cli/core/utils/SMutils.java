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
package slib.tools.smltoolkit.sm.cli.core.utils;

import org.openrdf.model.URI;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author seb
 */
public class SMutils {

    /**
     *
     * @param pairwiseConf
     * @param a
     * @param b
     * @param value
     * @throws SLIB_Ex_Critic
     */
    public static void throwArithmeticCriticalException(SMconf pairwiseConf, URI a, URI b, Object value) throws SLIB_Ex_Critic {

        throw new SLIB_Ex_Critic("Critical error. \n"
                + "A result produced by the pairwise measure " + pairwiseConf.getFlag() + " was not a number " + value + " (NaN/Infinity). \n"
                + "Pairwise measure in use " + pairwiseConf.getFlag() + " id=" + pairwiseConf.getId() + ". \n"
                + "Classes compared " + a + " vs " + b + ". \n"
                + "This issue can be encountred if an infinite value have been detected.\n"
                + "Please report the bug to the developpers\n");
    }

    /**
     *
     * @param m
     * @param p
     * @param e1
     * @param e2
     * @param value
     * @throws SLIB_Ex_Critic
     */
    public static void throwArithmeticCriticalException(SMconf m, SMconf p, URI e1,
            URI e2, Object value) throws SLIB_Ex_Critic {
        throw new SLIB_Ex_Critic("Critical error. \n"
                + "A result produced by the measure " + m.getFlag() + " was not a number " + value + " (NaN/infinity). \n"
                + "Pairwise measure in use " + p.getFlag() + " id=" + p.getId() + ". \n"
                + "Entities compared " + e1 + " vs " + e2 + ". \n"
                + "This issue can be encountred if an infinite value have been detected.\n"
                + "Please report the bug to the developpers\n");
    }
}
