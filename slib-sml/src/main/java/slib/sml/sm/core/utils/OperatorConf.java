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
package slib.sml.sm.core.utils;

import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.utils.i.Conf;

/**
 *
 * Configuration of an Operator
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class OperatorConf extends Conf {

    /**
     * the flag associated to the configuration.
     */
    public String flag;
    /**
     * the unique id identifying the configuration.
     */
    public String id;
    /**
     * the IC configuration associated to the operator configuration.
     */
    public ICconf ic;

    /**
     * Builder of an instance of configuration.
     *
     * @param flag the flag characterizing the operator which is associated to the configuration.
     * @param id the unique id of the configuration.
     */
    public OperatorConf(String flag, String id) {
        this.flag = flag;
        this.id = id;
    }

    /**
     * Builder of an instance of configuration.
     *
     * @param flag the flag characterizing the operator which is associated to the configuration.
     * @param id the unique id of the configuration.
     * @param ic the IC configuration associated to the Operator
     */
    public OperatorConf(String flag, String id, ICconf ic) {
        this.flag = flag;
        this.id = id;
        this.ic = ic;
    }

    @Override
    public String toString() {
        String out = "id : " + id + "\n";
        out += "flag : " + flag + "\n";
        out += "icConf : \n" + ic + "\n";

        out += "Extra parameters Operator: " + super.toString();
        return out;
    }
}
