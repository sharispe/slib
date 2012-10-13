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
 
 
package slib.sml.sme.discriminative_power;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.sme.utils.SymmetricResultStack;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.BigFileReader;

public class DiscriminativePowerComputer {

	Logger logger = LoggerFactory.getLogger(DiscriminativePowerComputer.class);

	Pattern p_tab   = Pattern.compile("\t");
	Pattern p_comma = Pattern.compile(",");

	HashMap<String, HashSet<String>> clanProteins;  // key: clan id value: set of protein id
	HashMap<String, String> protClanMapping ; 		// key: prot id value: clan id

	LinkedHashMap<String, SymmetricResultStack> methResults;

	public static double max_value_default = 1000;
	double max_value = max_value_default;

	public DiscriminativePowerComputer() {
		

	}


	public void compute(String clansfile, String protComparison,
			Double max_value,String out) throws SLIB_Ex_Critic {
		
		if(max_value != null)
			this.max_value = max_value;
		
		loadClans(clansfile);
		loadingScore(protComparison);
		computeAllDiscriminativePower(out);
	}


	private void loadClans(String clansFile) throws SLIB_Ex_Critic {
		logger.info("Loading Clans information from: "+clansFile);

		protClanMapping = new HashMap<String, String>();
		clanProteins 	= new HashMap<String, HashSet<String>>();


		try {

			BigFileReader file = new BigFileReader(clansFile);

			while(file.hasNext())   {

				String[] data = p_tab.split(file.nextTrimmed());

				if(data.length != 2)
					continue;

				String clanID  	 = data[0];
				String uniprotKBs = data[1];

				if(clanProteins.containsKey(clanID))
					throw new SLIB_Ex_Critic("Duplicate clan specification: "+clanID);

				clanProteins.put(clanID, new HashSet<String>());

				String[] protIDS = p_comma.split(uniprotKBs);

				for(String p : protIDS){
					protClanMapping.put(p, clanID);
					clanProteins.get(clanID).add(p);
				}
			}
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new SLIB_Ex_Critic(e.getMessage());
		}
		logger.info(protClanMapping.size()+" clans loaded");
		
		if(protClanMapping.size() == 0)
			throw new SLIB_Ex_Critic("Cannot perform treatments without clans loaded");
	}

	public void loadingScore(String infile) throws SLIB_Ex_Critic{
		logger.info("Loading Sims  from: "+infile);

		ArrayList<String> methodsFlags = null;

		methResults = new LinkedHashMap<String, SymmetricResultStack>();
		try {

			BigFileReader file = new BigFileReader(infile);
			String strLine;
			boolean header = false;

			while(file.hasNext())   {
				strLine = file.nextTrimmed();
				String[] data = p_tab.split(strLine);

				if(!strLine.isEmpty() && !header && data.length >= 3){ // HEADER
					header = true;
					for (int i = 2; i < data.length; i++) { // initialize method results
						methResults.put(data[i], new SymmetricResultStack(data[i]));
					}
					methodsFlags = new ArrayList<String>(methResults.keySet());
				}
				else if(methodsFlags != null && data.length == methodsFlags.size()+2){

					for (int i = 0; i < methodsFlags.size(); i++) {
						methResults.get(methodsFlags.get(i)).addResult(data[0], data[1], Double.parseDouble(data[i+2]));
					}
				}
			}
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
			throw new SLIB_Ex_Critic(e.getMessage());
		}
	}

	public double getResult(String methID, String o1, String o2) throws SLIB_Ex_Critic{

		if(methResults.containsKey(methID)){
			return methResults.get(methID).getSim(o1, o2);
		}
		throw new SLIB_Ex_Critic("Cannot find result stack for "+methID);
	}

	public double computeIntraSetSim(String methID, String clan) throws SLIB_Ex_Critic{

		ArrayList<String> allProtClan = new ArrayList<String>(clanProteins.get(clan));

		double sum = 0;

		for (String p:allProtClan) {

			for(String p1:allProtClan)
				sum += getResult(methID, p, p1);
		}
		double intraSetSim = sum/(Math.pow(allProtClan.size(), 2));
		
		logger.debug("Intra Set "+methID+"\t"+clan+"\t"+intraSetSim);
		return intraSetSim;
	}

	public double computeInterSetSim(String methID, String clan_a, String clan_b) throws SLIB_Ex_Critic{

		ArrayList<String> allProtClan_a = new ArrayList<String>(clanProteins.get(clan_a));
		ArrayList<String> allProtClan_b = new ArrayList<String>(clanProteins.get(clan_b));

		double sum = 0;

		for (String p:allProtClan_a) {

			for(String p1:allProtClan_b)
				sum += getResult(methID, p, p1);
		}

		double interSetSim = sum/(allProtClan_a.size() * allProtClan_b.size());

		logger.debug("Inter Set "+methID+"\t"+clan_a+"\t"+clan_b+"\t"+interSetSim);
		return interSetSim;
	}

	public double computeDiscriminativePower(String methID,String clan) throws SLIB_Ex_Critic{

		int 	p  = clanProteins.keySet().size()-1;
		double intraSetSim = computeIntraSetSim(methID, clan);
		double interSetSim = 0;

		for (String c: clanProteins.keySet()) {
			if(!c.equals(clan))
				interSetSim += computeInterSetSim(methID, c, clan);

		}

		if(intraSetSim == 0)
			return 0;
		else if(interSetSim == 0)
			return max_value;

		double dp = ((p-1) * intraSetSim) /interSetSim;
		return dp;
	}

	public void computeAllDiscriminativePower(String outfile) throws SLIB_Ex_Critic{

		try{
			FileWriter fstream = new FileWriter(outfile);
			BufferedWriter out = new BufferedWriter(fstream);
			
			String header = "Clan";

			for(String m: methResults.keySet())
				header += "\t"+m;
			header += "\n";
			
			out.write(header);

			for(String clan : clanProteins.keySet()){

				String s = clan;

				for(String m: methResults.keySet())
					s+= "\t"+computeDiscriminativePower(m, clan);
				s+="\n";
				out.write(s);
			}
			out.close();
			
			logger.info("discriminative powers calculated, consult: "+outfile);
		}catch (Exception e){
			throw new SLIB_Ex_Critic(e.getMessage());
		}
	}

	public static void main(String[] args) throws SLIB_Ex_Critic {

		String dir = System.getProperty("user.dir");

		String protComparison = dir+"/modules/smf/sme/data/bioinfo/result_examples/pfam_proteins_comparison_human_results.txt";
		String clansfile	  = dir+"/modules/smf/sme/data/bioinfo/result_examples/pfam_clan_prot_human.csv";
		String out 			  = dir+"/modules/smf/sme/data/bioinfo/result_examples/pfam_proteins_comparison_human_results_discriminative_power.txt";

		double max_value = 200;
	
		DiscriminativePowerComputer c = new DiscriminativePowerComputer();
		c.compute(clansfile, protComparison, max_value,out);

	}


}
