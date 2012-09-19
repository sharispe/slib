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
 
 
package slib.indexer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.openrdf.model.URI;

import slib.sglib.model.repo.impl.DataRepository;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;
import slib.utils.impl.OBOconstants;

public class IndexerOBO extends Indexer<URI>{
	
	
	DataRepository df = DataRepository.getSingleton();
	
	boolean onTermSpec = false;
	String currentURI  = null;
	String currentName = null;
	
	
	
	public  IndexerOBO(String filepath, URI uriBase) throws SGL_Exception {

		try {

			FileInputStream fstream = new FileInputStream(filepath);
			DataInputStream in 		= new DataInputStream(fstream);
			BufferedReader br 		= new BufferedReader(new InputStreamReader(in));

			String line;

			boolean metadataLoaded = false;

			String flag,value;

			while ((line = br.readLine()) != null)   {

				flag  = null;
				value = null;

				line = line.trim();

				if(!metadataLoaded){ // loading OBO meta data

					if(line.equals(OBOconstants.TERM_FLAG) || line.equals(OBOconstants.TYPEDEF_FLAG)){

						metadataLoaded = true;

						// check format-version 
//						if(!format_version.equals(format_parser))
//							throw new SGTK_Exception_Warning("Parser of format-version "+format_parser+" used to load OBO version "+format_version);

						if(line.equals(OBOconstants.TERM_FLAG))
							onTermSpec = true;
					}
				}
				else{
					if(onTermSpec){ // loading [Term]

						checkLine(line);

						if(onTermSpec){

							String[] data = getData(line, ":");

							if(data.length < 2)
								continue;

							flag  = data[0];
							value = buildValue(data,1,":");
							value = removeComment(value);

							if( flag.equals(OBOconstants.TERM_ID_FLAG) ){ // id
								currentURI = uriBase.toString()+value;
							}
							else if( flag.equals(OBOconstants.NAME_FLAG) ){ // is_a
								currentName = value;
							}
						}
					}
					
				}
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new SGL_Ex_Critic(e.getMessage());
		}

	}
	
	private String[] getData(String line,String regex) {

		String data_prec[] = line.split("!"); // remove comment
		String data[] = data_prec[0].split(regex);

		for (int i = 0; i < data.length; i++)
			data[i] = data[i].trim();
		return data;
	}
	
	
	private String removeComment(String value) {
		return value.split("!")[0].trim();
	}

	private String buildValue(String[] data, int from, String glue) {
		String value = "";
		for (int i = from; i < data.length; i++) {

			if(i!=from)
				value += glue;

			value += data[i];
		}
		return value;
	}


	
	private void checkLine(String line) throws SGL_Ex_Critic {

		if(line.equals(OBOconstants.TERM_FLAG)){
			handleTerm();
			onTermSpec = true;
		}
	}
	
	private void handleTerm() throws SGL_Ex_Critic {

		if(onTermSpec){

			addValue(df.createURI(currentURI), currentName);
			
			currentURI  = null;
			currentName = null;
		}
	}
		
	
	public Object valuesOf(URI keyURI) {
		return mapping.get(keyURI)+" "+"["+keyURI.getLocalName()+"]";
	}
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws SGL_Exception {
		String path = System.getProperty("user.dir")+"/data/graph/obo/gene_ontology_ext.obo";
		Indexer<URI> i = new IndexerOBO(path,null);
	}
}
