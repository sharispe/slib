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
package slib.tools.smltoolkit.sm.cli.core.utils;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe
 */
public class ActionParamsUtils {

    /**
     * Try to associate an {@link ActionsParams} to the given String. Note that
     * set=XXXX will match {@link ActionsParams#SET} action.
     *
     * @param actionAsString the action as String
     * @return the action associated to the string if any
     * @throws SLIB_Ex_Critic if no action can be associated to the given input
     * String
     */
    public static ActionsParams getAction(String actionAsString) throws SLIB_Ex_Critic {

        actionAsString = actionAsString.trim();
        String[] data = actionAsString.split("=");
        ActionsParams action;
        try {
            action = ActionsParams.valueOf(data[0].toUpperCase());

        } catch (IllegalArgumentException e) {
            String validActions = "";
            for (ActionsParams value : ActionsParams.values()) {
                validActions += value.name() + " ";
            }

            throw new SLIB_Ex_Critic("Cannot associated the key '" + data[0] + "' in " + actionAsString + " to a valid action " + validActions);
        }

        return action;

    }

    public static Double getSetValue(String noAnnotsConf_s) throws SLIB_Ex_Critic {

        Double val;

        noAnnotsConf_s = noAnnotsConf_s.trim();

        ActionsParams a = getAction(noAnnotsConf_s);
        if (a == ActionsParams.SET) {
            String[] data = noAnnotsConf_s.split("=");
            if (data.length != 2) {
                throw new SLIB_Ex_Critic("Error parsing " + noAnnotsConf_s + ", expecting SET=<numeric value> e.g. SET=0");
            } else {
                try {
                    val = Double.parseDouble(data[1]);
                } catch (NumberFormatException e) {
                    throw new SLIB_Ex_Critic("Error parsing string " + noAnnotsConf_s + "." + data[1] + " cannot be parse to a Double value " + e.getMessage());
                }
            }
        } else {
            throw new SLIB_Ex_Critic("Error processing string " + noAnnotsConf_s + ". The given action is not a " + ActionsParams.SET + " action but a " + a);
        }
        return val;

    }
}
