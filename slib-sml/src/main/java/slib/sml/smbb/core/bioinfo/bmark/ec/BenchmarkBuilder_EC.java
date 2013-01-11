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
 
 
package slib.sml.smbb.core.bioinfo.bmark.ec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.entity.EntityBasic;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_EC;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * http://www.biomedcentral.com/1471-2105/11/588/table/T3
 * @author Sebastien Harispe
 *
 */
public class BenchmarkBuilder_EC {


	Logger logger = LoggerFactory.getLogger(BenchmarkBuilder_EC.class);

	G g;

	HashMap<V, HashSet<String>> vertex2ec_mapping;
	HashMap<V, HashSet<String>> prot2ec_mapping;
	String ec2go_mapping;

	DataFactoryMemory uriManager = DataFactoryMemory.getSingleton();

	Long random;

	String outGeneEcAnnots;
	String outGenePairs;
	String outGenePairsScores;
	
	InstancesAccessor instancesAccessor;


	/**
     *
     * @param graph
     * @param instancesAccessor
     * @param conf
     * @throws SLIB_Exception
     */
    public void generateBenchmark(
			G graph, 
			InstancesAccessor instancesAccessor,
			SmbbConf_GO_EC conf) throws SLIB_Exception{

		this.g = graph;
		this.instancesAccessor = instancesAccessor;
		
		this.ec2go_mapping 	 = conf.getEc2GO_mapping();
		this.random 		 = conf.getRandom_number();
		this.outGeneEcAnnots = conf.getOut_genes_ec();
		this.outGenePairs 	 = conf.getOut_genes_pair();
		this.outGenePairsScores = conf.getOut_genes_pair_ecsim();

		logger.info("Building EC benchmark considering mapping: "+ec2go_mapping);
		logger.info(conf.toString());
		
		loadVertex2EC_mapping();
		
		if(vertex2ec_mapping.size() == 0){
			throw new SLIB_Ex_Critic("Mapping results is empty, processed aborted");
			
		}
		generateProtMapping();
		generateGeneECAnnotationFile();
		generateComparisonFile();
	}



	private void generateGeneECAnnotationFile() throws SLIB_Ex_Critic {

		logger.debug("generating Gene EC annotations");

		try{
			FileWriter fstream = new FileWriter(outGeneEcAnnots);
			BufferedWriter out = new BufferedWriter(fstream);
			String outs;

			for(Entry<V,HashSet<String>> e : prot2ec_mapping.entrySet()){

				outs = null;
				for(String s : e.getValue()){
					if(outs == null)
						outs = s;
					else
						outs += ","+s;
				}
				outs = ((URI) e.getKey().getValue()).getLocalName()+"\t"+outs+"\n";
				out.write(outs);
			}
			out.close();


		}catch (IOException e){//Catch exception if any
			throw new SLIB_Ex_Critic(e.getMessage());
		}
	}


	private void generateComparisonFile() throws SLIB_Ex_Critic {

		logger.debug("computing EC scores & Generating output files...");
		
		EntityBasic[] entities = (EntityBasic[]) prot2ec_mapping.keySet().toArray(new EntityBasic[0]);
		long nbComparison = ((entities.length*(entities.length-1))/2);
		logger.debug("Total : "+nbComparison);
		Set<Long> randomIds = null;

		if(random != null){
			logger.debug("generating "+random+" comparisons");
			randomIds = new HashSet<Long>();

			Random generator = new Random();
			for (int i = 0; i < random; i++) {

				long randomIndex = (long) (generator.nextDouble()*nbComparison);
				if(randomIds.contains(randomIndex)){
					i--;
					continue;
				}
				randomIds.add(randomIndex);
			}
		}

		try{
			FileWriter fstream    = new FileWriter(outGenePairs);
			FileWriter fstreamBis = new FileWriter(outGenePairsScores);
			BufferedWriter outGenePairsFile = new BufferedWriter(fstream);
			BufferedWriter outGenePairsScoresFile = new BufferedWriter(fstreamBis);
			
			long count = 0;
			String out;
			
			outGenePairsScoresFile.write("e1\te2\tec_score\n");

			for (int i = 0; i < entities.length; i++) {
				for (int j = i; j < entities.length; j++) {

					if(randomIds == null || (randomIds!=null && randomIds.contains(count)) ){
						
						out = entities[i].getURI().getLocalName()+"\t"+entities[j].getURI().getLocalName();
						outGenePairsFile.write(out+"\n");
						
						int simEc = computeSimEc(entities[i],entities[j]);
						
						outGenePairsScoresFile.write(out+"\t"+simEc+"\n");
					}
					count++;
				}	
			}
			outGenePairsScoresFile.close();
			outGenePairsFile.close();
		}catch (IOException e){
			throw new SLIB_Ex_Critic(e.getMessage());
		}
	}		

	private int computeSimEc(EntityBasic e1, EntityBasic e2) {
		int sim = 0;

		for(String s1 : prot2ec_mapping.get(e1)){

			String[]digitEc1 = getDigits(s1);

			for(String s2 : prot2ec_mapping.get(e2)){

				int sim_tmp = 0;
				String[]digitEc2 = getDigits(s2);

				for (int i = 0; i < digitEc1.length; i++) {
					if(digitEc2.length > i){
						if(digitEc1[i].equals(digitEc2[i])){
							sim_tmp++;
						}
						else
							break;
					}
				}
				if(sim_tmp > sim)
					sim = sim_tmp;
			}
		}
		return sim;
	}


	/**
	 * e.g. EC:2.4.99.1
	 * @param s
	 * @return
	 */
	private String[] getDigits(String s) {

		String[] data = s.split(":");
		String[] digits = data[1].split("\\.");
		return digits;
	}



	private void generateProtMapping() throws SLIB_Exception {

		prot2ec_mapping = new HashMap<V, HashSet<String>>();

		SM_Engine dm = new SM_Engine(g);

		for(V e : instancesAccessor.getInstances()){
			Set<V> annots = instancesAccessor.getDirectClass(e);
			Set<V> annots_prop = dm.getAncestorsInc(annots);
			HashSet<String> ecCodes = getEcCodes(annots_prop);

			if(ecCodes.size() > 1){
				prot2ec_mapping.put(e, ecCodes);
			}
		}
	}



	private HashSet<String> getEcCodes(Set<V> annots_prop) {

		HashSet<String> ecs = new HashSet<String>();
		for(V v : annots_prop){
			if(vertex2ec_mapping.containsKey(v))
				ecs.addAll(vertex2ec_mapping.get(v));
		}
		return ecs;
	}



	private void loadVertex2EC_mapping() throws SLIB_Exception {

		logger.info("Loading mapping... ");

		vertex2ec_mapping = new HashMap<V, HashSet<String>>();
		int not_considered = 0;
		int total = 0;

		try{
			FileInputStream fstream = new FileInputStream(ec2go_mapping);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			while ((strLine = br.readLine()) != null)   {

				if(!strLine.startsWith("!") && !strLine.isEmpty()){
					String[] data = strLine.split("\\s>\\s");
					if(data.length == 2){
						String ec   = data[0].trim();
						String go_s = data[1];

						String[] data_sub = go_s.split(";");

						if(data_sub.length == 2){ 
							String go_term = data_sub[1].trim();
							
							
							URI go_term_uri = uriManager.createURI(go_term);
							V v = g.getV(go_term_uri);

							if(v != null){
								if(!vertex2ec_mapping.containsKey(v))
									vertex2ec_mapping.put(v,new HashSet<String>());
								vertex2ec_mapping.get(v).add(ec);
							}
							else{
								not_considered++;
							}
							total++;
						}
					}
				}
			}
			in.close();
		}catch (IOException e){//Catch exception if any
			throw new SLIB_Ex_Critic("Error: " + e.getMessage());
		}

		//		for(V v : vertex2ec_mapping.keySet())
		//			logger.debug(v+" "+vertex2ec_mapping.get(v).size());

		logger.info("total    : "+total);
		logger.info("excluded : "+not_considered+" (not found in the graph or deleted due to subgraph restriction)");
	}

}
