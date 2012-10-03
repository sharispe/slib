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
import slib.tools.module.ModuleConf;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.impl.Util;

public class SmbbConf_GO implements ModuleConf {
	
	Logger   logger = LoggerFactory.getLogger(SmbbConf_GO.class);
	
	protected String  type;
	protected String  kb_uri;
	protected String  graph_uri;
	
	public SmbbConf_GO(){} 

	public boolean isValid() throws SGL_Ex_Critic {
		
		if(type == null)
			Util.error("Please specify a "+SmbbCst.type);
		if(kb_uri == null)
			Util.error("Please specify a "+SmbbCst.kb_id);
		if(graph_uri == null)
			Util.error("Please specify a "+SmbbCst.graph_uri);
		
		return true;
	}
	

	public String toString(){
		
		String out = "";
		out += "\nBenchmark type value    : "+type;
		out += "\nGraph id		          : "+graph_uri;
		out += "\nKB id		              : "+kb_uri;
			
		return out;
	}

	public String getKb_id() {
		return kb_uri;
	}

	public void setKb_id(String kb_id) {
		this.kb_uri = kb_id;
	}


	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getGraph_id() {
		return graph_uri;
	}

	public void setGraph_id(String graph_id) {
		graph_uri = graph_id;
	}
}
