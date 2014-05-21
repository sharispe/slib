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

import slib.sml.sm.core.utils.SMConstants;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public abstract class ICconf extends Conf {

    /**
     * ID used to represent the IC.
     */
    private final String id;
    /**
     * label used to plot IC values.
     */
    private final String label;
    /**
     * Flag corresponding to the IC computation method.
     */
    private final String flag;
    /**
     * Class name of the computation method.
     */
    private String className;
    /**
     * Define if the IC is corpus-based (extrinsic).
     */
    private boolean isCorpusBased;

    /**
     * Builder of the IC configuration.
     *
     * @param icFlag the flag of the IC
     * @throws SLIB_Ex_Critic
     */
    public ICconf(String icFlag) throws SLIB_Ex_Critic {
        this.id = icFlag;
        this.label = icFlag;
        this.flag = icFlag;

        validate();
    }

    /**
     * Advanced builder of an instance of IC configuration.
     *
     * @param id the ID used to represent the IC (must be unique)
     * @param label the label used to plot IC values
     * @param flag the flag corresponding to the IC
     * @throws SLIB_Ex_Critic
     */
    public ICconf(String id, String label, String flag) throws SLIB_Ex_Critic {
        this.id = id;
        this.label = label;
        this.flag = flag;

        validate();

    }

    /**
     * Method used to validate the configuration specified with regard to
     * constraints/properties to respect/ensure.
     *
     * @throws SLIB_Ex_Critic
     */
    private void validate() throws SLIB_Ex_Critic {

        if (SMConstants.SIM_PAIRWISE_DAG_NODE_IC_ANNOT.containsKey(flag)) {

            className = SMConstants.SIM_PAIRWISE_DAG_NODE_IC_ANNOT.get(flag);
            this.isCorpusBased = true;
        } else if (SMConstants.SIM_PAIRWISE_DAG_NODE_IC_INTRINSIC.containsKey(flag)) {

            className = SMConstants.SIM_PAIRWISE_DAG_NODE_IC_INTRINSIC.get(flag);
            this.isCorpusBased = false;
        } else {
            throw new SLIB_Ex_Critic("Unknown IC Flag " + flag);
        }
    }

    /**
     * Getter of the id.
     * @return the id associated to the configuration
     */
    public String getId() {
        return id;
    }

    /**
     * Getter of the label.
     * @return the label associated to the configuration.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Getter of the flag.
     * @return the flag associated to the configuration.
     */
    public String getFlag() {
        return flag;
    }

    /**
     * Getter of the class name.
     * @return the class name associated to the configuration.
     */
    public String getClassName() {
        return className;
    }

    /**
     * @return true if the IC configuration is associated to an IC which is corpus based.
     */
    public boolean isCorpusBased() {
        return isCorpusBased;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ICconf other = (ICconf) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String out = "id : " + id + "\n";
        out += "label : " + label + "\n";
        out += "corpusBased : " + isCorpusBased + "\n";
        out += "flag : " + flag + "\n";
        out += super.toString();

        return out;
    }
}
