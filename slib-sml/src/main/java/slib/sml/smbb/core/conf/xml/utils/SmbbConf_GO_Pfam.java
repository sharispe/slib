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
public class SmbbConf_GO_Pfam extends SmbbConf_GO {

	Logger   logger = LoggerFactory.getLogger(SmbbConf_GO_Pfam.class);

	Integer min_annot;
	String  taxon;
	String  pFamKB_index;
	String  pFamClans;
	String  out_pair_file;
	String  out_clan_prot;
	String  outputPositiveRel;
	String  outputNegativeRel;
	
	/**
     *
     */
    public SmbbConf_GO_Pfam(){
		type = SmbbCst.type_GO_PFAM;
	} 

	/**
     *
     * @param sspBBConf_g
     * @throws SLIB_Ex_Critic
     */
    public SmbbConf_GO_Pfam(Conf sspBBConf_g) throws SLIB_Ex_Critic {

		this();

		
		String kb_id 		 = (String) sspBBConf_g.getParam(SmbbCst.kb_uri);
		String graph_id 	 = (String) sspBBConf_g.getParam(SmbbCst.graph_uri);
		String pFamKB_index  = (String) sspBBConf_g.getParam(SmbbCst.pFamKb_index);
		String pFamClans  	 = (String) sspBBConf_g.getParam(SmbbCst.pFamClans);
		String taxon 	 	 = (String) sspBBConf_g.getParam(SmbbCst.taxon);
		String min_annot_s 	 = (String) sspBBConf_g.getParam(SmbbCst.min_annot_size);
		String out_pair_file = (String) sspBBConf_g.getParam(SmbbCst.out_pair_file);
		String out_clan_prot = (String) sspBBConf_g.getParam(SmbbCst.out_clan_prot);
		String positiveRel   = (String) sspBBConf_g.getParam(SmbbCst.positiveRel);
		String negativeRel   = (String) sspBBConf_g.getParam(SmbbCst.negativeRel);
		
		
		Integer minAnnot = null;

		if(min_annot_s != null)
			minAnnot = Util.stringToInteger(min_annot_s);

		this.graph_uri 	  = graph_id ;
		this.kb_uri    	  = kb_id;
		this.pFamKB_index = pFamKB_index;
		this.pFamClans 	  = pFamClans;
		this.min_annot 	  = minAnnot;
		this.taxon 		  = taxon;
		this.out_pair_file = out_pair_file;
		this.out_clan_prot = out_clan_prot;
		this.outputPositiveRel = positiveRel;
		this.outputNegativeRel = negativeRel;
	}

	@Override
	public boolean isValid() throws SLIB_Ex_Critic {

		logger.info(toString());

		super.isValid();

		if(taxon == null)
			Util.error("Please specify a Taxon id (i.e "+SmbbCst.taxon+" parameter) ");
		
		if(pFamKB_index == null)
			Util.error("Please specify "+SmbbCst.pFamKb_index+" parameter ");
		
		if(pFamClans == null)
			Util.error("Please specify "+SmbbCst.pFamClans+" parameter ");
		
		if(out_pair_file == null)
			Util.error("Please specify "+SmbbCst.out_pair_file+" parameter ");
		
		if(out_clan_prot == null)
			Util.error("Please specify "+SmbbCst.out_clan_prot+" parameter ");
		
		if(outputPositiveRel == null)
			Util.error("Please specify a value for parameter "+SmbbCst.positiveRel);
		
		if(outputNegativeRel == null)
			Util.error("Please specify a value for parameter "+SmbbCst.negativeRel);
		
		
		return true;
	}


	public String toString(){

		String out = super.toString();
		out += "\npFam KB index: "+pFamKB_index;
		out += "\npFam Clans: "+pFamClans;
		out += "\nmin number of annotation: "+min_annot;
		out += "\ntaxon id                : "+taxon;
		out += "\nout_pair_file           : "+out_pair_file;
		out += "\nout_clan_prot           : "+out_clan_prot;
		out += "\npositive relationships  : "+outputPositiveRel;
		out += "\nnegative relationships  : "+outputNegativeRel;

		return out;
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

	/**
     *
     * @return
     */
    public String getpFamKB_index() {
		return pFamKB_index;
	}

	/**
     *
     * @param pFamKB_index
     */
    public void setpFamKB_index(String pFamKB_index) {
		this.pFamKB_index = pFamKB_index;
	}

	/**
     *
     * @return
     */
    public String getpFamClans() {
		return pFamClans;
	}

	/**
     *
     * @param pFamClans
     */
    public void setpFamClans(String pFamClans) {
		this.pFamClans = pFamClans;
	}

	/**
     *
     * @return
     */
    public String getOut_pair_file() {
		return out_pair_file;
	}

	/**
     *
     * @param out_pair_file
     */
    public void setOut_pair_file(String out_pair_file) {
		this.out_pair_file = out_pair_file;
	}

	/**
     *
     * @return
     */
    public String getOut_clan_prot() {
		return out_clan_prot;
	}

	/**
     *
     * @param out_clan_prot
     */
    public void setOut_clan_prot(String out_clan_prot) {
		this.out_clan_prot = out_clan_prot;
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
	

}
