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
 
 
package slib.sml.smbb.core.bioinfo.bmark.pfam;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sml.smbb.core.conf.xml.utils.SmbbConf_GO_Pfam;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.BigFileReader;

/**
 * http://www.biomedcentral.com/1471-2105/11/588/table/T3
 * @author Sebastien Harispe
 *
 */
public class BenchmarkBuilder_Pfam {


	Logger logger = LoggerFactory.getLogger(this.getClass());

	String[] pFamClans;
	String filePFamClans;
	HashSet<String> taxonIDs;
	G g;
	InstancesAccessor instancesAccessor;
	
	Integer min_annot;

	HashMap<String, HashSet<URI>> clanProtUris; // key clan label values prot uris


	Pattern p_tab   = Pattern.compile("\t");
	Pattern p_comma = Pattern.compile(",");

	DataFactoryMemory uriManager = DataFactoryMemory.getSingleton();


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
			SmbbConf_GO_Pfam conf) throws SLIB_Exception{
		
		this.g = graph;
		this.instancesAccessor = instancesAccessor;

		this.pFamClans 	   	= conf.getpFamClans().split(",");
		this.filePFamClans 	= conf.getpFamKB_index();
		this.taxonIDs 	   	= new HashSet<String>(Arrays.asList(conf.getTaxon()));
		this.min_annot 		= conf.getMin_annot();

		clanProtUris = new HashMap<String, HashSet<URI>>();

		if(pFamClans.length == 0)
			throw new SLIB_Ex_Critic("Invalid number of pFam clans "+pFamClans.length);

		logger.info("Building pFam benchmark considering clans: "+Arrays.toString(pFamClans));
		logger.info(conf.toString());
		
		HashMap<String, ArrayList<String>> pFamClansProteins = loadClans(g.getURI().toString());

		for(Entry<String, ArrayList<String>> e : pFamClansProteins.entrySet()){
			
			String clan = e.getKey();
			clanProtUris.put(clan, new HashSet<URI>());
			

			for(String p : e.getValue()){
				
				V inst = g.getV(g.getDataFactory().createURI(p));
				
				if(inst != null){
					if(min_annot == null || instancesAccessor.getDirectClass(inst).size() >= min_annot.intValue())
						clanProtUris.get(clan).add((URI)inst.getValue());
				}
			}
			logger.info(e.getKey()+" populating: "+clanProtUris.get(clan).size()+" on "+e.getValue().size());
		}
		
		buildPairwiseEvalFile(conf.getOut_pair_file());
		buildClanProtURIs(conf.getOut_clan_prot());
		buildBinaryClassifierEvalFile(conf.getOutputPositiveRel(),conf.getOutputNegativeRel());
	}


	private HashMap<String, ArrayList<String>> loadClans(String entityNamespace) throws SLIB_Ex_Critic {

		
		HashMap<String, ArrayList<String>> pFamClansProteins = new HashMap<String, ArrayList<String>>();

		for(String c : pFamClans)
			pFamClansProteins.put(c, new ArrayList<String>());


		try {

			BigFileReader file = new BigFileReader(filePFamClans);

			while(file.hasNext())   {
				
				
				String[] data = p_tab.split(file.nextTrimmed());

				if(data.length != 4)
					continue;
				
				

				String uniprotKB_id  = entityNamespace+data[0];
				
				
				
				String taxonAsString = data[1];
				String clansAsString = data[3];
				
				String[] taxonIDs_current = p_comma.split(taxonAsString);

				boolean valid = true;

				if(taxonIDs != null){

					valid = false;

					for(String c : taxonIDs_current){

						if(taxonIDs.contains(c)){
							valid = true;
							break;
						}
					}
				}
				
				if(valid){
					String[] clans = p_comma.split(clansAsString);

					for(String c : clans){
						if(pFamClansProteins.containsKey(c) && !pFamClansProteins.get(c).contains(uniprotKB_id)){
							pFamClansProteins.get(c).add(uniprotKB_id);
						}
					}
				}
			}
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new SLIB_Ex_Critic(e.getMessage());
		}
		

		return pFamClansProteins;
	}

	/**
     *
     * @return
     */
    public HashMap<String, HashSet<URI>> getClanProteinURIs(){
		return clanProtUris;
	}

	/**
     *
     * @param outfile
     * @throws SLIB_Ex_Critic
     */
    public void buildClanProtURIs(String outfile) throws SLIB_Ex_Critic{

		try{
			FileWriter fstream = new FileWriter(outfile);
			BufferedWriter out = new BufferedWriter(fstream);

			for (Entry<String, HashSet<URI>> e : clanProtUris.entrySet()) {

				out.write(e.getKey()+"\t");

				int c = 0;
				for (URI uri:e.getValue()) {

					if(c !=0)
						out.write(",");
					out.write(uri.getLocalName().toString());
					c++;
				}

				out.write("\n");

			}

			out.close();
		}catch (Exception e){
			throw new SLIB_Ex_Critic(e.getMessage());
		}
		logger.info("Clan info generated see: "+outfile);
	}

	/**
     *
     * @param outfile
     * @throws SLIB_Ex_Critic
     */
    public void buildPairwiseEvalFile(String outfile) throws SLIB_Ex_Critic{

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
			throw new SLIB_Ex_Critic(e.getMessage());
		}
		logger.info("comparison file generated see: "+outfile);
	}

	/**
     *
     * @return
     */
    public HashSet<URI> getAllClanProteinURIs(){
		HashSet<URI> uris = new HashSet<URI>();

		for(HashSet<URI> set : clanProtUris.values())
			uris.addAll(set);

		return uris;
	}

	private void buildPostiveEvalFile(String outFile) throws SLIB_Ex_Critic {


		try{
			FileWriter fstream = new FileWriter(outFile);
			BufferedWriter out = new BufferedWriter(fstream);

			for(HashSet<URI> set : clanProtUris.values()){

				for (URI u1: set) {
					for (URI u2: set)
						out.write(u1.getLocalName().toString()+"\t"+u2.getLocalName().toString()+"\n");
				}
			}
			out.close();
		}catch (Exception e){
			throw new SLIB_Ex_Critic(e.getMessage());
		}
		logger.info("Positive comparison generated see: "+outFile);
	}
	
	private void buildNegativeEvalFile(String outFile,int nb) throws SLIB_Ex_Critic {


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
				throw new SLIB_Ex_Critic(mess);
			}
		}catch (Exception e){
			throw new SLIB_Ex_Critic(e.getMessage());
		}
		logger.info("Negative comparison generated see: "+outFile);
	}

	private HashSet<String> getClans(URI protUri) {
		HashSet<String> clans = new HashSet<String>();
		
		for (Entry<String,HashSet<URI>> e : clanProtUris.entrySet()) {
			if(e.getValue().contains(protUri))
				clans.add(e.getKey());
		}
		
		return clans;
	}

	private int getNbPostiveEval() throws SLIB_Ex_Critic {

		int count = 0;
		for(HashSet<URI> set : clanProtUris.values())
			count+= set.size()*set.size();
		return count;
	}


	/**
     *
     * @param protPositiveComparison
     * @param protNegativeComparison
     * @throws SLIB_Ex_Critic
     */
    public void buildBinaryClassifierEvalFile(String protPositiveComparison,String protNegativeComparison) throws SLIB_Ex_Critic {

		buildPostiveEvalFile(protPositiveComparison);

		int nbNegative = getNbPostiveEval();
		buildNegativeEvalFile(protNegativeComparison,nbNegative);
		

	}

}
