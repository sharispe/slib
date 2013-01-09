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

import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;

/**
 *
 * @author seb
 */
public class SMconf extends Conf {

    /**
     *
     */
    public String id;
    /**
     *
     */
    public String flag;
    /**
     *
     */
    public String className;
    /**
     *
     */
    public String label;
    /**
     *
     */
    public ICconf icConf;
    /**
     *
     */
    public String pairwise_measure_id;
    /**
     *
     */
    public String representation;
    /**
     *
     */
    public OperatorConf operator;

    /**
     *
     * @param id
     * @param flag
     * @throws SLIB_Ex_Critic
     */
    public SMconf(String id, String flag) throws SLIB_Ex_Critic {

        this.id = id;
        this.flag = flag;
        this.label = id;

        init();
    }

    /**
     *
     * @param id
     * @param flag
     * @param label
     * @throws SLIB_Ex_Critic
     */
    public SMconf(String id, String flag, String label) throws SLIB_Ex_Critic {

        this.id = id;
        this.flag = flag;
        this.label = label;

        init();
    }

    /**
     *
     * @param id
     * @param flag
     * @param icConf
     * @throws SLIB_Ex_Critic
     */
    public SMconf(String id, String flag, ICconf icConf) throws SLIB_Ex_Critic {

        this.id = id;
        this.flag = flag;
        this.label = id;
        this.icConf = icConf;
        init();
    }

    /**
     *
     * @param id
     * @param flag
     * @param label
     * @param icConf
     * @throws SLIB_Ex_Critic
     */
    public SMconf(String id, String flag, String label, ICconf icConf) throws SLIB_Ex_Critic {

        this.id = id;
        this.flag = flag;
        this.label = label;
        this.icConf = icConf;
        init();
    }

    /**
     *
     * @param id
     * @param flag
     * @param label
     * @param icConf
     * @param representation
     * @param operator
     * @throws SLIB_Ex_Critic
     */
    public SMconf(String id, String flag, String label, ICconf icConf, String representation, OperatorConf operator) throws SLIB_Ex_Critic {

        this.id = id;
        this.flag = flag;
        this.label = label;
        this.icConf = icConf;
        this.representation = representation;
        this.operator = operator;
        init();
    }

    private void init() throws SLIB_Ex_Critic {

        this.className = SMConstants.semanticMeasureClassName(this.flag);

        if (this.className == null) {
            throw new SLIB_Ex_Critic("Cannot resolve Semantic measure associated to flag: " + flag);
        }
    }

    /**
     *
     * @return
     */
    public ICconf getICconf() {
        return icConf;
    }

    /**
     *
     * @param ic
     */
    public void setICconf(ICconf ic) {
        this.icConf = ic;
    }

    /**
     *
     * @return
     */
    public String getRepresentation() {
        return representation;
    }

    /**
     *
     * @param representation
     */
    public void setRepresentation(String representation) {
        this.representation = representation;
    }

    /**
     *
     * @return
     */
    public OperatorConf getOperator() {
        return operator;
    }

    /**
     *
     * @param operator
     */
    public void setOperator(OperatorConf operator) {
        this.operator = operator;
    }

    /**
     *
     * @return
     */
    public String getPairwise_measure_id() {
        return pairwise_measure_id;
    }

    /**
     *
     * @param pairwise_measure_id
     */
    public void setPairwise_measure_id(String pairwise_measure_id) {
        this.pairwise_measure_id = pairwise_measure_id;
    }

    public String toString() {
        String out = "id : " + id + "\n";
        out += "flag : " + flag + "\n";
        out += "className : " + className + "\n";
        out += "label : " + label + "\n";
        out += "icConf : \n" + icConf + "\n";
        out += "Pairwise measure id : \n" + pairwise_measure_id + "\n";
        out += "representation : " + representation + "\n";
        out += "operator : \n" + operator + "\n";

        out += "Extra parameters : " + super.toString();
        return out;
    }
}
