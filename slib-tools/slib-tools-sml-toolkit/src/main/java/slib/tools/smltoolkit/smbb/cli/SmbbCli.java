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


package slib.tools.smltoolkit.smbb.cli;


import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.algo.graph.extraction.rvf.instances.impl.InstanceAccessor_RDF_TYPE;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sml.smbb.core.SmbbCst;
import slib.sml.smbb.core.bioinfo.bmark.ec.BenchmarkBuilder_EC;
import slib.sml.smbb.core.bioinfo.bmark.kegg.BenchmarkBuilder_Kegg;
import slib.sml.smbb.core.bioinfo.bmark.pfam.BenchmarkBuilder_Pfam;
import slib.sml.smbb.core.bioinfo.bmark.ppi.BenchmarkBuilder_Interact;
import slib.tools.smltoolkit.SmlModuleCLI;
import slib.tools.smltoolkit.smbb.cli.conf.xml.loader.Smbb_XMLConfLoader;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Util;

/**
 * Command Line Interface of the SML Semantic Measure Benchmark Builder (SMBB) module.
 * This module is used to build benchmarks considering a given configuration.
 * Various types of benchmarks can be build please see the dedicated documentation
 * for more information.
 * 
 * @author Sebastien Harispe
 *
 */
public class SmbbCli implements SmlModuleCLI {

	Logger logger = LoggerFactory.getLogger(SmbbCli.class);

	Smbb_XMLConfLoader conf;

	G g;

	/**
     *
     */
    public SmbbCli(){}

	/**
	 * Load the module considering the given configuration file path.
	 * 
         * @param confFile the path of the configuration file to take into account
         * @throws SLIB_Exception  
	 */
	public SmbbCli(String confFile) throws SLIB_Exception{
		execute(confFile);
	}


	/**
     *
     * @param args
     * @throws SLIB_Exception
     */
    @Override
    public void execute(String[] args) throws SLIB_Exception {
		SmbbCmdHandler cfgLoader = new SmbbCmdHandler(args);
		execute(cfgLoader.xmlConfFile);
	}


	/**
	 * Process the configuration file
	 * @param confFile
	 * @throws SGL_Exception
	 */
	private void execute(String confFile) throws SLIB_Exception {

		conf = new Smbb_XMLConfLoader(confFile);

		// Load the graph and perform required graph treatments
		GraphLoaderGeneric.load(conf.generic.getGraphConfs());

		logger.info("Retrieving the graph "+conf.getGraphURI());

		URI  graphURI = DataFactoryMemory.getSingleton().createURI(conf.getGraphURI());
		g = DataFactoryMemory.getSingleton().getGraph(graphURI);

		if(g == null)
			Util.error("No graph associated to the uri "+conf.getGraphURI()+" was loaded...");
		
		// Add possibility to tune the type of accessor used;
		InstancesAccessor iAccessor = new InstanceAccessor_RDF_TYPE(g);

		logger.info("Generating "+conf.getType()+" benchmark");
		logger.info("Configuration information");


		if(conf.getType().equals(SmbbCst.type_GO_PPI)){

			BenchmarkBuilder_Interact.generateBenchmark(g, iAccessor, conf.sspBBConf_PPI);
		}
		else if(conf.getType().equals(SmbbCst.type_GO_PFAM)){
			BenchmarkBuilder_Pfam benchBuilder = new BenchmarkBuilder_Pfam();
			benchBuilder.generateBenchmark(g, iAccessor, conf.sspBBConf_Pfam);
		}
		else if(conf.getType().equals(SmbbCst.type_GO_EC)){

			BenchmarkBuilder_EC benchBuilder = new BenchmarkBuilder_EC();
			benchBuilder.generateBenchmark(g, iAccessor, conf.sspBBConf_EC);
		}
		else if(conf.getType().equals(SmbbCst.type_GO_KEGG)){

			BenchmarkBuilder_Kegg benchBuilder = new BenchmarkBuilder_Kegg();
			benchBuilder.generateBenchmark(g, iAccessor, conf.sspBBConf_KEGG);
		}
		else{
			throw new UnsupportedOperationException("Cannot perform treatment for type "+conf.getType());
		}

		logger.info("Treatment done");
	}


	/**
     *
     * @param args
     */
    @SuppressWarnings("unused")
	public static void main(String[] args) {

		String conf = System.getProperty("user.dir")+"/studies/modules/sml/smbb/conf/bioinfo";

		// old conf
		//		confFile = pref+"BenchMarkBuilderPPI_human.xml";
		//		confFile = pref+"BenchMarkBuilderPPI_sgd.xml";
		//		confFile = pref+"BenchMarkBuilderPfam_human.xml";
		//		confFile = pref+"BenchMarkBuilderEC_human.xml";

		conf += "human/smbb_PPI_human_IEA_bp.xml";
		//		conf += "scerevisiae/ppi/smbb_PPI_scerevisiae_mf.xml";

		conf = "/tmp/tmp_bench_output/GO_EC_FULL_TODEL//conf/smf_smbb_conf.xml";
		try {
			SmbbCli launcher = new SmbbCli(conf);
		} 
		catch (SLIB_Exception e) {
			e.printStackTrace();
		}
	}

}
