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
 
 
package slib.tools.sml.smbb.cli.conf.xml.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.smbb.core.SmbbCst;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.i.Conf;
import slib.utils.impl.Util;

public class SmbbConf_GO_EC extends SmbbConf_GO {

	Logger   logger = LoggerFactory.getLogger(SmbbConf_GO_EC.class);

	Long	random_number;
	String  ec2GO_mapping;
	String  out_genes_ec;
	String  out_genes_pair;
	String  out_genes_pair_ecsim;
	
	public SmbbConf_GO_EC(){
		type = SmbbCst.type_GO_EC;
	} 

	public SmbbConf_GO_EC(Conf sspBBConf_g) throws SGL_Ex_Critic {
		
		this();
		
		this.kb_uri 	   = (String) sspBBConf_g.getParam(SmbbCst.kb_id);
		this.graph_uri 	   = (String) sspBBConf_g.getParam(SmbbCst.graph_uri);
		this.ec2GO_mapping = (String) sspBBConf_g.getParam(SmbbCst.ec2GO_mapping);
		this.out_genes_ec   = (String) sspBBConf_g.getParam(SmbbCst.out_genes_ec);
		this.out_genes_pair = (String) sspBBConf_g.getParam(SmbbCst.out_genes_pair);
		this.out_genes_pair_ecsim  = (String) sspBBConf_g.getParam(SmbbCst.out_genes_pair_ecsim);
		
		String random_number_s 	 = (String) sspBBConf_g.getParam(SmbbCst.random_number);

		if(random_number_s != null)
			random_number = Util.stringToLong(random_number_s);
	}

	@Override
	public boolean isValid() throws SGL_Ex_Critic {

		logger.info(toString());

		super.isValid();

		if(ec2GO_mapping == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify "+SmbbCst.ec2GO_mapping+" parameter ");

		if(out_genes_ec == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify "+SmbbCst.out_genes_ec+" parameter ");

		if(out_genes_pair == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify "+SmbbCst.out_genes_pair+" parameter ");

		if(out_genes_pair_ecsim == null)
			Util.error("[Module "+SmbbCst.appName+"] Please specify "+SmbbCst.out_genes_pair_ecsim+" parameter ");

		return true;
	}


	public String toString(){

		String out = super.toString();
		out += "\nec2GO_mapping		  : "+ec2GO_mapping;
		out += "\nout_genes_ec		  : "+out_genes_ec;
		out += "\nout_genes_pair	  : "+out_genes_pair;
		out += "\nout_genes_pair_ecsim: "+out_genes_pair_ecsim;
		out += "\nrandom number		  : "+random_number;

		return out;
	}

	public Long getRandom_number() {
		return random_number;
	}

	public void setRandom_number(Long random_number) {
		this.random_number = random_number;
	}

	public String getEc2GO_mapping() {
		return ec2GO_mapping;
	}

	public void setEc2GO_mapping(String ec2go_mapping) {
		ec2GO_mapping = ec2go_mapping;
	}

	public String getOut_genes_ec() {
		return out_genes_ec;
	}

	public void setOut_genes_ec(String out_genes_ec) {
		this.out_genes_ec = out_genes_ec;
	}

	public String getOut_genes_pair() {
		return out_genes_pair;
	}

	public void setOut_genes_pair(String out_genes_pair) {
		this.out_genes_pair = out_genes_pair;
	}

	public String getOut_genes_pair_ecsim() {
		return out_genes_pair_ecsim;
	}

	public void setOut_genes_pair_ecsim(String out_genes_pair_ecsim) {
		this.out_genes_pair_ecsim = out_genes_pair_ecsim;
	}
	
	
}
