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
package slib.utils.i;

import java.util.HashMap;
import java.util.Map;
import slib.utils.ex.SLIB_Ex_Critic;


/**
 * Generic Configuration class used to build object storing a configuration as
 * key value pairs. {@link Conf} objects are defined by a map storing pair of
 * keys and values in which the key is a {@link String} corresponding to the
 * flag of the parameter and the value is an {@link Object} corresponding to the
 * value associated to the related key.
 *
 * @author Sebastien Harispe
 *
 */
public class Conf {

    Map<String, Object> params;

    /**
     * Return the value associated to the given parameter as a{@link String}.
     *
     * @param p the flag of the parameter you want to retrieve the value.
     * @return the value associated to the given parameter flag or null if no
     * parameter corresponding to the queried flag is stored.
     */
    public String getParamAsString(String p) {
        if (params == null) {
            return null;
        }

        return (String) params.get(p);
    }

    /**
     * Return the value associated to the given parameter as an {@link Object}.
     *
     * @param p the flag of the parameter you want to retrieve the value.
     * @return the value associated to the given parameter flag or null if no
     * parameter corresponding to the queried flag is stored.
     */
    public Object getParam(String p) {
        if (params == null) {
            return null;
        }

        return params.get(p);
    }

    /**
     * Return the value associated to the given parameter as a double value.
     *
     * @param p the flag of the parameter
     * @return the value associated to the given key as a double
     * @throws SLIB_Ex_Critic  
     */
    public double getParamAsDouble(String p) throws SLIB_Ex_Critic {
        String pval = getParam(p).toString();
        double val;
        try {
            val = Double.parseDouble(pval);
        } catch (Exception e) {
            throw new SLIB_Ex_Critic("Error converting " + p + " parameter to numeric value");
        }
        return val;
    }

    /**
     * Return true if the given parameter is stored in the current
     * configuration.
     *
     * @param p the parameter flag
     * @return true if the configuration map contains a key mapping the given
     * flag.
     */
    public boolean containsParam(String p) {
        return (getParam(p) != null);
    }

    /**
     * Add the parameter to the configuration
     * Override existing parameter if one exists.
     * @param p the parameter flag
     * @param v the value associated
     * @return the configuration object from which the method is called (auto complete feature)
     */
    public Conf addParam(String p, Object v) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        params.put(p, v);
        return this;
    }

  

    /**
     * Remove the entry corresponding to the given key if one is stored.
     *
     * @param p the key to process.
     */
    public void removeParam(String p) {
        if (containsParam(p)) {
            params.remove(p);
        }
    }

    /**
     * Getter of the Map storing the entry loaded in the configuration.
     *
     * @return the loaded map.
     */
    public Map<String, Object> getParams() {
        return params;
    }

    /**
     * Set the current map to the given.
     *
     * @param params the new map defining the configuration.
     */
    public void setParams(HashMap<String, Object> params) {
        this.params = params;
    }

    /**
     * Check if the configuration contains at least one parameter.
     *
     * @return true if at least a parameter is loaded.
     */
    public boolean containsParams() {
        return (params != null && params.size() != 0);
    }

    @Override
    public String toString() {
        String out = "";

        if (params != null) {
            for (String s : params.keySet()) {
                out += s + "\t" + params.get(s).toString() + "\n";
            }

        } else {
            out = "empty";
        }
        return out;
    }
}
