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
 
 
package slib.tools.smltoolkit.smbb.cli.conf.xml.loader;


import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import slib.sml.smbb.core.SmbbCst;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_EC;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_KEGG;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_PPI;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_Pfam;
import slib.tools.module.GenericConfBuilder;
import slib.tools.module.XML_ModuleConfLoader;
import slib.tools.smltoolkit.smbb.cli.conf.xml.utils.SmbbConfXmlCst;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.i.Conf;
import slib.utils.impl.Util;


public class Smbb_XMLConfLoader extends XML_ModuleConfLoader {

	
	
	Logger   logger = LoggerFactory.getLogger(Smbb_XMLConfLoader.class);
	
	public Conf sspBBConf_g;
	
	/**
	 * TODO remove this multiple configuration object
	 * Only consider one {@link SmbbConf_GO} object which
	 * could be casted in order to be processed
	 */
	public SmbbConf_GO_PPI  sspBBConf_PPI;
	public SmbbConf_GO_Pfam sspBBConf_Pfam;
	public SmbbConf_GO_EC 	sspBBConf_EC;
	public SmbbConf_GO_KEGG sspBBConf_KEGG;
	
	String type     = null;
	String kb_id    = null;
	String graphURI = null;
	
	Document document;

	public Smbb_XMLConfLoader(String confFile) throws SLIB_Ex_Critic{
		
		super(confFile);
		
		logger.info("Loading "+SmbbCst.appName+" Configuration ");

		try {
			// Load and validate XML configuration specification

			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
			document = parser.parse(new File( confFile )); 

			NodeList sspbb_tags = document.getElementsByTagName(SmbbConfXmlCst.tag);

			Element graph_spec_e = null;

			// Check number of graph specification + critical attributes
			if(	sspbb_tags.getLength() == 1 && sspbb_tags.item(0) instanceof Element ){
				graph_spec_e = (Element) sspbb_tags.item(0);
				loadInfo( graph_spec_e );
			}
			else
				Util.error(SmbbConfXmlCst.ERROR_NB_TAG);
			
			
			
			loadSspBBconf();
			
			
			if(type.equals(SmbbCst.type_GO_PPI)){
				sspBBConf_PPI = new SmbbConf_GO_PPI(sspBBConf_g);
				sspBBConf_PPI.isValid();
			}
			else if(type.equals(SmbbCst.type_GO_PFAM)){
				sspBBConf_Pfam = new SmbbConf_GO_Pfam(sspBBConf_g);
				sspBBConf_Pfam.isValid();
			}
			else if(type.equals(SmbbCst.type_GO_EC)){
				sspBBConf_EC = new SmbbConf_GO_EC(sspBBConf_g);
				sspBBConf_EC.isValid();
			}
			else if(type.equals(SmbbCst.type_GO_KEGG)){
				sspBBConf_KEGG = new SmbbConf_GO_KEGG(sspBBConf_g);
				sspBBConf_KEGG.isValid();
			}
			else{
				throw new UnsupportedOperationException("Cannot load configuration for unknown test '"+type+"', please correct type attribute for tag "+SmbbConfXmlCst.tag);
			}
		
			
		} catch (Exception e) {
			throw new SLIB_Ex_Critic(e);
		}
		
		logger.info("configuration loaded");
	}
	
	
	private void loadSspBBconf() throws SLIB_Ex_Critic {
		
		type 	 = (String) sspBBConf_g.getParam(SmbbCst.type);
		graphURI = (String) sspBBConf_g.getParam(SmbbCst.graph_uri);
		kb_id 	 = (String) sspBBConf_g.getParam(SmbbCst.kb_id);
		
		logger.info("Benchmark type : "+type);
	}


	private void loadInfo(Element item) throws SLIB_Ex_Critic {
		sspBBConf_g = GenericConfBuilder.build(item);
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	


	public String getKb_id() {
		return kb_id;
	}


	public void setKb_id(String kb_id) {
		this.kb_id = kb_id;
	}



	public String getGraphURI() {
		return graphURI;
	}


	@SuppressWarnings("unused")
	public static void main(String[] args) {

		String pref = System.getProperty("user.dir")+"/modules/smf/smbb/conf/bioinfo/";

//		String exec_config = pref+"BenchMarkBuilderPPI_human.xml";
		String exec_config = pref+"BenchMarkBuilderKEGG_human.xml";
		try {
			Smbb_XMLConfLoader cfgLoader = new Smbb_XMLConfLoader(exec_config);

		} catch (SLIB_Exception e) {
			e.printStackTrace();
		}
	}
}
