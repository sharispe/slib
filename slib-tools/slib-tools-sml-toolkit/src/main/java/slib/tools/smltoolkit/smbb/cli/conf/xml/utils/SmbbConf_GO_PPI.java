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
 
 
package slib.tools.smltoolkit.smbb.cli.conf.xml.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.smbb.core.SmbbCst;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.i.Conf;
import slib.utils.impl.Util;

public class SmbbConf_GO_PPI extends SmbbConf_GO {
	
	Logger   logger = LoggerFactory.getLogger(SmbbConf_GO_PPI.class);
	

	private String  knownRel;
	private String  knownRelBase;
	private String  outputPositiveRel;
	private String  outputNegativeRel;
	private Integer setSize;
	private Integer min_annot;
	private String  taxon;
	
	public SmbbConf_GO_PPI(){
		type = SmbbCst.type_GO_PPI;
	} 

	/**
	 * Build a {@link SmbbConf_GO_PPI} instance considering the given generic configuration object 
	 * @param genericConf a {@link Conf} instance containing the various fields expected to be specified.
	 * Loaded parameter keys are :
	 * <ul>
	 * 	<li> {@link SmbbCst#kb_id} </li>
	 * 	<li> {@link SmbbCst#graph_id} </li>
	 * 	<li> {@link SmbbCst#knownRel} </li>
	 * 	<li> {@link SmbbCst#knownRelBase} </li>
	 * 	<li> {@link SmbbCst#positiveRel} </li>
	 * 	<li> {@link SmbbCst#negativeRel} </li>
	 * 	<li> {@link SmbbCst#setSize} </li>
	 * 	<li> {@link SmbbCst#taxon} </li>
	 * <li> {@link SmbbCst#min_annot_size} </li>
	 * </ul>
	 * @throws SGL_Ex_Critic if an error occurs during object creation
	 */
	public SmbbConf_GO_PPI(Conf genericConf) throws SGL_Ex_Critic {
		
		this();
		
		String kb_id 	= (String) 		genericConf.getParam(SmbbCst.kb_id);
		String graph_id = (String) 		genericConf.getParam(SmbbCst.graph_uri);
		
		
		String knownRel 	 = (String) genericConf.getParam(SmbbCst.knownRel);
		String knownRelBase  = (String) genericConf.getParam(SmbbCst.knownRelBase);
		String positiveRel   = (String) genericConf.getParam(SmbbCst.positiveRel);
		String negativeRel   = (String) genericConf.getParam(SmbbCst.negativeRel);
		String setSize_s 	 = (String) genericConf.getParam(SmbbCst.setSize);
		String taxon 	 	 = (String) genericConf.getParam(SmbbCst.taxon);
		String min_annot_s 	 = (String) genericConf.getParam(SmbbCst.min_annot_size);
		
		Integer setSize = null;
		Integer minAnnot = null;
		
		if(setSize_s != null)
			setSize = Util.stringToInteger(setSize_s);
		
		if(min_annot_s != null)
			minAnnot = Util.stringToInteger(min_annot_s);
		
		this.graph_uri = graph_id ;
		this.kb_uri    = kb_id;
		this.knownRel = knownRel;
		this.knownRelBase = knownRelBase;
		this.outputPositiveRel = positiveRel;
		this.outputNegativeRel = negativeRel;
		this.setSize = setSize;
		this.min_annot = minAnnot;
		this.taxon = taxon;
	}

	@Override
	public boolean isValid() throws SGL_Ex_Critic {
		
		super.isValid();
		
		if(setSize != null && setSize < 0)
			Util.error("Invalid "+SmbbCst.setSize+" value "+setSize);
		
		if(knownRel == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify a value for parameter "+SmbbCst.knownRel);
		
		if(knownRelBase == null)
			knownRelBase = knownRel;
		
		if(outputPositiveRel == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify a value for parameter "+SmbbCst.positiveRel);
		
		if(outputNegativeRel == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify a value for parameter "+SmbbCst.negativeRel);
		
		if(kb_uri == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify a KB id (i.e "+SmbbCst.kb_id+" parameter) check URIs ");
		
		if(taxon == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify a Taxon id (i.e "+SmbbCst.taxon+" parameter) ");
		
		return true;
	}
	

	public String toString(){
		
		String out = super.toString();
		out += "\nRelationhips Known      : "+knownRel;
		out += "\nRelationhips Known Base : "+knownRelBase;
		out += "\npositive relationships  : "+outputPositiveRel;
		out += "\nnegative relationships  : "+outputNegativeRel;
		out += "\nmin number of annotation: "+min_annot;
		out += "\nsize limit              : "+setSize;
		out += "\ntaxon id                : "+taxon;
			
		return out;
	}

	public String getKnownRel() {
		return knownRel;
	}

	public void setKnownRel(String knownRel) {
		this.knownRel = knownRel;
	}

	public String getKnownRelBase() {
		return knownRelBase;
	}

	public void setKnownRelBase(String knownRelBase) {
		this.knownRelBase = knownRelBase;
	}

	public String getOutputPositiveRel() {
		return outputPositiveRel;
	}

	public void setOutputPositiveRel(String positiveRel) {
		this.outputPositiveRel = positiveRel;
	}

	public String getOutputNegativeRel() {
		return outputNegativeRel;
	}

	public void setOutputNegativeRel(String negativeRel) {
		this.outputNegativeRel = negativeRel;
	}

	public Integer getSetSize() {
		return setSize;
	}

	public void setSetSize(Integer setSize) {
		this.setSize = setSize;
	}

	public String getTaxon() {
		return taxon;
	}

	public Integer getMin_annot() {
		return min_annot;
	}

	public void setMin_annot(Integer min_annot) {
		this.min_annot = min_annot;
	}

	public void setTaxon(String taxon) {
		this.taxon = taxon;
	}
}
