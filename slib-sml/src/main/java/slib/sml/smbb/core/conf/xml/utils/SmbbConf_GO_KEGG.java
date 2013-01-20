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
public class SmbbConf_GO_KEGG extends SmbbConf_GO {

	Logger   logger = LoggerFactory.getLogger(SmbbConf_GO_KEGG.class);

	Integer min_annot;
	String  taxon;
	String  pathway_clusters;
	String  kegg_index;
	String  out_pair_file;
	String  outputPositiveRel;
	String  outputNegativeRel;
	String  indexOut;
	
	/**
     *
     */
    public SmbbConf_GO_KEGG(){
		type = SmbbCst.type_GO_KEGG;
	} 

	/**
     *
     * @param sspBBConf_g
     * @throws SLIB_Ex_Critic
     */
    public SmbbConf_GO_KEGG(Conf sspBBConf_g) throws SLIB_Ex_Critic {

		this();

		
		String kb_id 		 = (String) sspBBConf_g.getParam(SmbbCst.kb_uri);
		String graph_id 	 = (String) sspBBConf_g.getParam(SmbbCst.graph_uri);
		String kegg_index    = (String) sspBBConf_g.getParam(SmbbCst.kegg_index);
		String pathway_clusters = (String) sspBBConf_g.getParam(SmbbCst.pathway_clusters);
		String taxon 	 	 = (String) sspBBConf_g.getParam(SmbbCst.taxon);
		String min_annot_s 	 = (String) sspBBConf_g.getParam(SmbbCst.min_annot_size);
		String out_pair_file = (String) sspBBConf_g.getParam(SmbbCst.out_pair_file);
		String positiveRel   = (String) sspBBConf_g.getParam(SmbbCst.positiveRel);
		String negativeRel   = (String) sspBBConf_g.getParam(SmbbCst.negativeRel);
		String indexOut    = (String) sspBBConf_g.getParam(SmbbCst.index_out);
		
		
		Integer minAnnot = null;

		if(min_annot_s != null)
			minAnnot = Util.stringToInteger(min_annot_s);

		this.graph_uri 	  = graph_id ;
		this.kb_uri    	  = kb_id;
		this.kegg_index   = kegg_index;
		this.min_annot 	  = minAnnot;
		this.taxon 		  = taxon;
		this.out_pair_file = out_pair_file;
		this.outputPositiveRel = positiveRel;
		this.outputNegativeRel = negativeRel;
		this.pathway_clusters = pathway_clusters;
		this.indexOut = indexOut;
	}

	@Override
	public boolean isValid() throws SLIB_Ex_Critic {

		logger.info(toString());

		super.isValid();

		if(taxon == null)
			Util.error("Please specify a Taxon id (i.e "+SmbbCst.taxon+" parameter) ");
		
		if(kegg_index == null)
			Util.error("Please specify "+SmbbCst.kegg_index+" parameter ");
		
		if(out_pair_file == null)
			Util.error("Please specify "+SmbbCst.out_pair_file+" parameter ");
		
		if(outputPositiveRel == null)
			Util.error("Please specify a value for parameter "+SmbbCst.positiveRel);
		
		if(outputNegativeRel == null)
			Util.error("Please specify a value for parameter "+SmbbCst.negativeRel);
		
		if(pathway_clusters == null)
			Util.error("Please specify "+SmbbCst.pathway_clusters+" parameter ");
		
		if(indexOut == null)
			Util.error("Please specify "+SmbbCst.index_out+" parameter ");
		
		return true;
	}


	public String toString(){

		String out = super.toString();
		out += "\nkegg_index: "+kegg_index;
		out += "\npathway_clusters : "+pathway_clusters;
		out += "\nmin number of annotation: "+min_annot;
		out += "\ntaxon id                : "+taxon;
		out += "\nout_pair_file           : "+out_pair_file;
		out += "\npositive relationships  : "+outputPositiveRel;
		out += "\nnegative relationships  : "+outputNegativeRel;
		out += "\nindexOut  : "+indexOut;

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
    public String getKeggIndex(){
		return this.kegg_index;
	}
	
	/**
     *
     * @param index
     */
    public void setKeggIndex(String index) {
		this.kegg_index = index;
	}
	
	/**
     *
     * @return
     */
    public String getPathwayCluster(){
		return this.pathway_clusters;
	}
	
	/**
     *
     * @param index
     */
    public void setPathwayCluster(String index) {
		this.pathway_clusters = index;
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
    public String getIndexFile() {
		return indexOut;
	}
	

}
