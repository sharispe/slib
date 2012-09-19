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


package slib.sml.smbb.core.bioinfo.bmark.kegg;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.repo.impl.DataRepository;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_KEGG;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;
import slib.utils.impl.BigFileReader;

/**
 * http://www.biomedcentral.com/1471-2105/11/588/table/T3
 * @author Sebastien Harispe
 *
 */
public class BenchmarkBuilder_Kegg {


	Logger logger = LoggerFactory.getLogger(this.getClass());

	G g;
	Integer min_annot;

	HashMap<String, HashSet<URI>> clustersEntitiesUris; 
	String[] pathway_clusters;
	String kegg_index;
	HashSet<String> taxonIDs;

	InstancesAccessor instancesAccessor;

	Pattern p_tab   = Pattern.compile("\t");
	Pattern p_comma = Pattern.compile(",");

	DataRepository uriManager = DataRepository.getSingleton();


	public void generateBenchmark(
			G graph, 
			InstancesAccessor instancesAccessor,
			SmbbConf_GO_KEGG conf) throws SGL_Exception{

		
		this.instancesAccessor = instancesAccessor;
		this.g = graph;
		this.pathway_clusters 	= p_comma.split(conf.getPathwayCluster());
		this.kegg_index 		= conf.getKeggIndex();
		this.taxonIDs 	   		= new HashSet<String>(Arrays.asList(conf.getTaxon()));
		this.min_annot 			= conf.getMin_annot();



		if(pathway_clusters.length == 0)
			throw new SGL_Ex_Critic("Invalid number of cluster specified "+pathway_clusters.length);

		logger.info("Building Kegg benchmark considering clusters: "+Arrays.toString(pathway_clusters));
		logger.info(conf.toString());
		
		HashMap<String, Set<String>> clusters = loadClusters(g.getURI().toString());


		// Clean clusters considering annotation size restrictions
		clustersEntitiesUris = new HashMap<String, HashSet<URI>>();

		for(Entry<String, Set<String>> e : clusters.entrySet()){

			String clusterID = e.getKey();
			clustersEntitiesUris.put(clusterID, new HashSet<URI>());

			for(String p : e.getValue()){

				V inst = g.getV(g.getDataRepository().createURI(p));
				if(inst != null){
					if(min_annot == null || instancesAccessor.getDirectClass(inst).size() >= min_annot.intValue())
						clustersEntitiesUris.get(clusterID).add((URI)inst.getValue());
				}
			}
			logger.info(clusterID+" populating: "+clustersEntitiesUris.get(clusterID).size()+" on "+e.getValue().size());
		}
		buildIndexFile(conf.getIndexFile());
		buildPairwiseEvalFile(conf.getOut_pair_file());
		buildBinaryClassifierEvalFile(conf.getOutputPositiveRel(),conf.getOutputNegativeRel());
	}


	private void buildIndexFile(String indexFile) throws SGL_Ex_Critic   {

		try{
			FileWriter fstream = new FileWriter(indexFile);
			BufferedWriter out = new BufferedWriter(fstream);


			for(Entry<String, HashSet<URI>> e : clustersEntitiesUris.entrySet()){

				String clusterID = e.getKey();
				HashSet<URI> uris = e.getValue();

				String uriString = "";
				int i = 0;

				for (URI u : uris) {

					if(i != 0)
						uriString += ",";
					uriString += u.getLocalName();
					i++;
				}

				out.write(clusterID+"\t"+uriString+"\n");
				logger.info(clusterID+"\t"+uriString);
			}
			out.close();
			
			logger.info("Reduced index file generated at "+indexFile);
		}catch (Exception e){
			throw new SGL_Ex_Critic(e.getMessage());
		}

	}


	private HashMap<String, Set<String>> loadClusters(String entityNamespace) throws SGL_Ex_Critic {


		HashMap<String, Set<String>> clusters = new HashMap<String, Set<String>>();

		for(String c : pathway_clusters)
			clusters.put(c, new HashSet<String>());
		try {
			BigFileReader file = new BigFileReader(kegg_index);

			while(file.hasNext())   {
				String[] data = p_tab.split(file.nextTrimmed());

				if(data.length != 2)
					continue;

				// Only process lines corresponding to the loaded clusters
				if(clusters.containsKey(data[0]))
					clusters.get(data[0]).addAll(Arrays.asList(p_comma.split(data[1])));
			}
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new SGL_Ex_Critic(e.getMessage());
		}
		return clusters;
	}


	public void buildPairwiseEvalFile(String outfile) throws SGL_Ex_Critic{

		ArrayList<URI> uris = new ArrayList<URI>(getAllClanProteinURIs());

		try{
			FileWriter fstream = new FileWriter(outfile);
			BufferedWriter out = new BufferedWriter(fstream);

			for (int i = 0; i < uris.size(); i++) {
				for (int j = i; j < uris.size(); j++)
					out.write(uris.get(i).getLocalName().toString()+"\t"+uris.get(j).getLocalName().toString()+"\n");
			}

			out.close();
		}catch (Exception e){
			throw new SGL_Ex_Critic(e.getMessage());
		}
		logger.info("comparison file generated see: "+outfile);
	}

	public HashSet<URI> getAllClanProteinURIs(){
		HashSet<URI> uris = new HashSet<URI>();

		for(HashSet<URI> set : clustersEntitiesUris.values())
			uris.addAll(set);

		return uris;
	}

	private void buildPostiveEvalFile(String outFile) throws SGL_Ex_Critic {


		try{
			FileWriter fstream = new FileWriter(outFile);
			BufferedWriter out = new BufferedWriter(fstream);

			for(HashSet<URI> set : clustersEntitiesUris.values()){

				for (URI u1: set) {
					for (URI u2: set)
						out.write(u1.getLocalName().toString()+"\t"+u2.getLocalName().toString()+"\n");
				}
			}
			out.close();
		}catch (Exception e){
			throw new SGL_Ex_Critic(e.getMessage());
		}
		logger.info("Positive comparison generated see: "+outFile);
	}

	private void buildNegativeEvalFile(String outFile,int nb) throws SGL_Ex_Critic {


		ArrayList<URI> uris = new ArrayList<URI>(getAllClanProteinURIs());

		logger.info("Try to generate "+nb+" comparisons");

		try{
			FileWriter fstream = new FileWriter(outFile);
			BufferedWriter out = new BufferedWriter(fstream);

			Random r = new Random();
			int attempts = 0;
			int allowedAttemps = (uris.size()*uris.size())*4;

			while(nb > 0 && attempts < allowedAttemps){

				int i = r.nextInt(uris.size());
				int j = r.nextInt(uris.size());

				URI uri_i = uris.get(i);
				URI uri_j = uris.get(j);

				HashSet<String> clans_i = getClans(uri_i);
				HashSet<String> clans_j = getClans(uri_j);

				// search overlap i.e check uris are not part of the same clan
				boolean valid = true;
				for(String c1: clans_i){
					for(String c2: clans_j){
						if(c1.equals(c2)){
							valid = false;
							break;
						}
					}
				}

				if(valid){
					out.write(uri_i.getLocalName().toString()+"\t"+uri_j.getLocalName().toString()+"\n");
					nb--;
				}
				attempts++;
			}
			out.close();

			if(nb > 0){
				String mess = "Despite the numerous attempts ("+allowedAttemps+"), the program fails " +
						"to generate a negative set of pairwise comparison" +
						"... the selected clans overlap is to high. ";
				throw new SGL_Ex_Critic(mess);
			}
		}catch (Exception e){
			throw new SGL_Ex_Critic(e.getMessage());
		}
		logger.info("Negative comparison generated see: "+outFile);
	}

	private HashSet<String> getClans(URI protUri) {
		HashSet<String> clans = new HashSet<String>();

		for (Entry<String,HashSet<URI>> e : clustersEntitiesUris.entrySet()) {
			if(e.getValue().contains(protUri))
				clans.add(e.getKey());
		}

		return clans;
	}

	private int getNbPostiveEval() throws SGL_Ex_Critic {

		int count = 0;
		for(HashSet<URI> set : clustersEntitiesUris.values())
			count+= set.size()*set.size();
		return count;
	}


	public void buildBinaryClassifierEvalFile(String protPositiveComparison,String protNegativeComparison) throws SGL_Ex_Critic {

		buildPostiveEvalFile(protPositiveComparison);

		int nbNegative = getNbPostiveEval();
		buildNegativeEvalFile(protNegativeComparison,nbNegative);


	}
}
