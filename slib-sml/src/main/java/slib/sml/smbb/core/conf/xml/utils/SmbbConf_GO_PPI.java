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
package slib.sml.smbb.core.conf.xml.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sml.smbb.core.SmbbCst;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;
import slib.utils.impl.Util;

/**
 *
 * @author seb
 */
public class SmbbConf_GO_PPI extends SmbbConf_GO {

    Logger logger = LoggerFactory.getLogger(SmbbConf_GO_PPI.class);
    private String knownRel;
    private String knownRelBase;
    private String prefixknownRel;
    private String prefixknownRelBase;
    private String outputPositiveRel;
    private String outputNegativeRel;
    private Integer setSize;
    private Integer min_annot;
    private String taxon;

    /**
     *
     */
    public SmbbConf_GO_PPI() {
        type = SmbbCst.type_GO_PPI;
    }

    /**
     * Build a {@link SmbbConf_GO_PPI} instance considering the given generic
     * configuration object
     *
     * @param genericConf a {@link Conf} instance containing the various fields
     * expected to be specified. Loaded parameter keys are : <ul> <li>
     * {@link SmbbCst#kb_id} </li> <li> {@link SmbbCst#graph_id} </li> <li>
     * {@link SmbbCst#knownRel} </li> <li> {@link SmbbCst#knownRelBase} </li>
     * <li> {@link SmbbCst#positiveRel} </li> <li> {@link SmbbCst#negativeRel}
     * </li> <li> {@link SmbbCst#setSize} </li> <li> {@link SmbbCst#taxon} </li>
     * <li> {@link SmbbCst#min_annot_size} </li> </ul>
     * @throws SLIB_Ex_Critic
     */
    public SmbbConf_GO_PPI(Conf genericConf) throws SLIB_Ex_Critic {

        this();

        this.kb_uri = (String) genericConf.getParam(SmbbCst.kb_uri);
        this.graph_uri = (String) genericConf.getParam(SmbbCst.graph_uri);




        this.knownRel = (String) genericConf.getParam(SmbbCst.knownRel);
        this.prefixknownRel = (String) genericConf.getParam(SmbbCst.prefixknownRel);

        this.knownRelBase = (String) genericConf.getParam(SmbbCst.knownRelBase);
        this.prefixknownRelBase = (String) genericConf.getParam(SmbbCst.prefixknownRelBase);

        this.outputPositiveRel = (String) genericConf.getParam(SmbbCst.positiveRel);
        this.outputNegativeRel = (String) genericConf.getParam(SmbbCst.negativeRel);
        this.taxon = (String) genericConf.getParam(SmbbCst.taxon);

        String setSize_s = (String) genericConf.getParam(SmbbCst.setSize);
        String min_annot_s = (String) genericConf.getParam(SmbbCst.min_annot_size);

        setSize = null;
        min_annot = null;

        if (setSize_s != null) {
            setSize = Util.stringToInteger(setSize_s);
        }

        if (min_annot_s != null) {
            min_annot = Util.stringToInteger(min_annot_s);
        }
    }

    @Override
    public boolean isValid() throws SLIB_Ex_Critic {

        super.isValid();

        if (setSize != null && setSize < 0) {
            Util.error("Invalid " + SmbbCst.setSize + " value " + setSize);
        }

        if (knownRel == null) {
            Util.error("Please specify a value for parameter " + SmbbCst.knownRel);
        }

        if (prefixknownRel == null) {
            prefixknownRel = "";
        }

        if (prefixknownRelBase == null) {
            prefixknownRelBase = "";
        }


        if (knownRelBase == null) {
            knownRelBase = knownRel;
            prefixknownRelBase = prefixknownRel;
        }



        if (outputPositiveRel == null) {
            Util.error("Please specify a value for parameter " + SmbbCst.positiveRel);
        }

        if (outputNegativeRel == null) {
            Util.error("Please specify a value for parameter " + SmbbCst.negativeRel);
        }

        if (kb_uri == null) {
            Util.error("Please specify a KB id (i.e " + SmbbCst.kb_uri + " parameter) check URIs ");
        }

        if (taxon == null) {
            Util.error("Please specify a Taxon id (i.e " + SmbbCst.taxon + " parameter) ");
        }



        return true;
    }

    public String toString() {

        String out = super.toString();
        out += "\nRelationhips Known      : " + knownRel;
        out += "\nRelationhips Known Base : " + knownRelBase;
        out += "\npositive relationships  : " + outputPositiveRel;
        out += "\nnegative relationships  : " + outputNegativeRel;
        out += "\nmin number of annotation: " + min_annot;
        out += "\nsize limit              : " + setSize;
        out += "\ntaxon id                : " + taxon;

        return out;
    }

    /**
     *
     * @return
     */
    public String getKnownRel() {
        return knownRel;
    }

    /**
     *
     * @return
     */
    public String getKnownRelURIprefix() {
        return prefixknownRel;
    }

    /**
     *
     * @return
     */
    public String getKnownRelBaseURIprefix() {
        return prefixknownRelBase;
    }

    /**
     *
     * @param knownRel
     */
    public void setKnownRel(String knownRel) {
        this.knownRel = knownRel;
    }

    /**
     *
     * @return
     */
    public String getKnownRelBase() {
        return knownRelBase;
    }

    /**
     *
     * @param knownRelBase
     */
    public void setKnownRelBase(String knownRelBase) {
        this.knownRelBase = knownRelBase;
    }

    /**
     *
     * @return
     */
    public String getOutputPositiveRel() {
        return outputPositiveRel;
    }

    /**
     *
     * @param positiveRel
     */
    public void setOutputPositiveRel(String positiveRel) {
        this.outputPositiveRel = positiveRel;
    }

    /**
     *
     * @return
     */
    public String getOutputNegativeRel() {
        return outputNegativeRel;
    }

    /**
     *
     * @param negativeRel
     */
    public void setOutputNegativeRel(String negativeRel) {
        this.outputNegativeRel = negativeRel;
    }

    /**
     *
     * @return
     */
    public Integer getSetSize() {
        return setSize;
    }

    /**
     *
     * @param setSize
     */
    public void setSetSize(Integer setSize) {
        this.setSize = setSize;
    }

    /**
     *
     * @return
     */
    public String getTaxon() {
        return taxon;
    }

    /**
     *
     * @return
     */
    public Integer getMin_annot() {
        return min_annot;
    }

    /**
     *
     * @param min_annot
     */
    public void setMin_annot(Integer min_annot) {
        this.min_annot = min_annot;
    }

    /**
     *
     * @param taxon
     */
    public void setTaxon(String taxon) {
        this.taxon = taxon;
    }
}
