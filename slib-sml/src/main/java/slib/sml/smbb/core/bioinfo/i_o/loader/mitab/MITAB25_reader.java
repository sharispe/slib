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
 
 
package slib.sml.smbb.core.bioinfo.i_o.loader.mitab;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.smbb.core.bioinfo.bmark.ppi.utils.InteractionSet;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.impl.BigFileReader;


/**
 * format: http://code.google.com/p/psimi/wiki/PsimiTabFormat
 * ftp://ftp.no.embnet.org/irefindex/data/archive/release_9.0/psimi_tab/MITAB2.6/
 * @author seb
 *
 */
public class MITAB25_reader {

	Logger logger = LoggerFactory.getLogger(MITAB25_reader.class);
	public HashMap<String, Integer> interactorsID = new HashMap<String, Integer>();

	private final int ID_INTER_A = 0;
	private final int ID_INTER_B = 1;
	private final int ID_TAXON_A = 9;
	private final int ID_TAXON_B = 10;

	Pattern p_tab   = Pattern.compile("\t");
	Pattern p_colon = Pattern.compile(":");
	Pattern p_pipe  = Pattern.compile("\\|");
	
	
	//TODO add configuration parameters to be able to configure this parameters
	Pattern p_taxid 	  = MiTAB25_Cst.p_taxid;
	Pattern p_uniprotKBid = MiTAB25_Cst.p_uniprotKBid;
	


	public InteractionSet load(String filePath) throws SGL_Ex_Critic{
		return load(filePath,"",null,null);
	}

	public InteractionSet load(String filePath,String uriprefix, String flagTaxon_A,String flagTaxon_B) throws SGL_Ex_Critic{

		InteractionSet ppiSet = new InteractionSet();

		logger.info("loading MITAB25 from "+filePath);
		
		interactorsID = new HashMap<String, Integer>();

		try {

			BigFileReader file = new BigFileReader(filePath);

			boolean header = true;

			int c_tot = 0;

			while(file.hasNext())   {

				String[] data = p_tab.split(file.nextTrimmed());

				if(!header){

					boolean valid = true;

					if( (flagTaxon_A != null && flagTaxon_B != null) ){

						String tA = extractValue(data[ID_TAXON_A],p_taxid);
						String tB = extractValue(data[ID_TAXON_B],p_taxid);


						if(tA == null || tB == null || !tA.equals(flagTaxon_A) || !tB.equals(flagTaxon_B))
							valid = false;
					}

					if(valid){
						
						String i_a = extractValue(data[ID_INTER_A],p_uniprotKBid);
						String i_b = extractValue(data[ID_INTER_B],p_uniprotKBid);

						
						if(i_a != null && i_b != null){
							
							i_a = uriprefix + i_a;
							i_b = uriprefix + i_b;
							
							Integer i_a_id = interactorsID.get(i_a);
							Integer i_b_id = interactorsID.get(i_b);
							
							if(i_a_id == null){
								i_a_id = ppiSet.interactors.size();
								ppiSet.interactors.add(i_a);
								interactorsID.put(i_a, i_a_id);
							}
							
							if(i_b_id == null){
								
								if(! i_a.equals(i_b)){
									i_b_id = ppiSet.interactors.size();
									ppiSet.interactors.add(i_b);
									interactorsID.put(i_b, i_b_id);
								}
								else{
									i_b_id = i_a_id;
								}
							}
							ppiSet.addInteraction(i_a_id, i_b_id);
						}
					}
					c_tot++;
				}
				else if(header){
					header = false;
				}
			}
			file.close();
			
			
			logger.info("Number of interaction loaded: "+ppiSet.interactions.size()+"/"+c_tot);
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new SGL_Ex_Critic(e.getMessage());
		}
		return ppiSet;
	}
	
	public void clean(){
		interactorsID = null;
	}

	private String extractValue(String string,Pattern p) {

		Matcher m = p.matcher(string);
		if( m.matches() && m.groupCount() == 1)
			return m.group(1);

		return null;
	}

	public HashSet<String> retrieveAllTaxons(String filePath){

		logger.info("loading "+filePath);
		HashSet<String> taxons = new HashSet<String>();

		try {

			FileInputStream fstream = new FileInputStream(filePath);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br  = new BufferedReader(new InputStreamReader(in));
			String strLine;

			boolean header = true;

			while ((strLine = br.readLine()) != null)   {

				String[] data = strLine.split("\t");

				if(!header){

					if(!taxons.contains(data[ID_TAXON_A]))
						taxons.add(data[ID_TAXON_A]);

					if(!taxons.contains(data[ID_TAXON_B]))
						taxons.add(data[ID_TAXON_B]);
				}
				else if(header){
					header = false;
				}
			}
			in.close();

			logger.info("Number of taxon : "+taxons.size());


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return taxons;
	}

}

