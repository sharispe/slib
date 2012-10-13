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
 
 
package slib.sml.smutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.BigFileReader;

/**
 * Used to results merge files
 * Comment start by !
 * consider first line as header (except those starting by !)
 * 
 * TODO add clusters to boost treatments
 * @author Sebastien Harispe
 *
 */
public class ResultsMerger {

	Logger logger = LoggerFactory.getLogger(ResultsMerger.class);

	private String file_a;
	private String file_b;

	private String tmp_file_a_prefix = "SGL_FILE_A_TMP";
	private String tmp_file_b_prefix = "SGL_FILE_B_TMP";

	ArrayList<String> header_a;
	ArrayList<String> header_b;

	HashMap<String, Long> values_a_index;
	HashMap<String, Long> values_b_index;

	long file_number_a = 0;
	long file_number_b = 0;

	HashMap<String, String> values_a;
	HashMap<String, String> values_b;

	String tmp_dir = "/tmp/";
	
	public static int splitSize_default = 100; // file size
	int splitSize = splitSize_default;


	public void process(String file_a, String file_b, String out) throws SLIB_Ex_Critic {

		logger.info("Merging results");
		logger.info("file A: "+file_a);
		logger.info("file B: "+file_b);


		values_a = new HashMap<String, String>();
		values_b = new HashMap<String, String>();

		this.file_a= file_a;
		this.file_b= file_b;

		loadData(true);
		loadData(false);

		checkEntrySetCoherency(values_a.keySet(),values_b.keySet());
		ArrayList<String> newHeader 					 = mergeHeaders();
		HashMap<String, String> newValues = mergeValues();

		values_a = null;
		values_b = null;

		try{
			FileWriter fstream = new FileWriter(out);
			BufferedWriter outbuff = new BufferedWriter(fstream);


			outbuff.write(implodeArrayList(newHeader)+"\n");

			for (Entry<String, String> e : newValues.entrySet()) {
				String outs = e.getKey()+"\t"+e.getValue()+"\n";
				outbuff.write(outs);
			}

			outbuff.close();
		}catch (Exception e){
			throw new SLIB_Ex_Critic("Error: " + e.getMessage());
		}

		logger.info("output: "+out);
	}

	private void checkEntrySetCoherency(Set<String> entries_A,Set<String> entries_B) throws SLIB_Ex_Critic {

		logger.info("Check entry coherencies");

		long count = 0;
		for (String q : entries_A) {
			if(!entries_B.contains(q)){
				logger.info("!!! "+q);
				count++;
			}
		}
		logger.info(count+" entries contained in file "+file_a+" not found in file "+file_b);
		long error = count;
		count = 0;
		for (String q : entries_B) {
			if(!entries_A.contains(q)){
				logger.info("!!! "+q);
				count++;
			}
		}
		logger.info(count+" entries contained in file "+file_b+" not found in file "+file_a);
		error += count;

		if(error != 0)
			throw new SLIB_Ex_Critic("Incoherencies detected see rows prefixed by  !!! above ");

	}

	public void processLarge(String file_a, String file_b, String output,String tmp_dir, Integer split_size) throws SLIB_Ex_Critic {


		values_a_index = new HashMap<String, Long>();
		values_b_index = new HashMap<String, Long>();

		this.file_a= file_a;
		this.file_b= file_b;
		
		this.tmp_dir = tmp_dir;
		
		if(split_size != null)
			this.splitSize = split_size;
		
		

		logger.info("Merging results");
		logger.info("file A 	: "+file_a);
		logger.info("file B 	: "+file_b);
		logger.info("tmp dir	: "+tmp_dir);
		logger.info("output		: "+output);
		logger.info("split size : "+splitSize);



		loadDataIndex(true);
		loadDataIndex(false);

		checkEntrySetCoherency(values_a_index.keySet(),values_b_index.keySet());
		ArrayList<String> newHeader = mergeHeaders();

		values_a = null;
		values_b = null;

		logger.info("Populating new file...");

		try{
			FileWriter fstream = new FileWriter(output);
			BufferedWriter outbuff = new BufferedWriter(fstream);


			outbuff.write(implodeArrayList(newHeader)+"\n");

			String outs, line_tmp;
			String[] data_tmp;

			long line_nb = 0;

			for (Entry<String, Long> e : values_a_index.entrySet()) {

				line_nb++;

				if(line_nb%1000 == 0)
					logger.info(line_nb+"/"+values_a_index.size());

				String qentry = e.getKey();
				Long file_a_index = e.getValue();

				if(!values_b_index.containsKey(qentry))
					throw new SLIB_Ex_Critic("Cannot locate entry corresponding to query "+qentry);

				Long file_b_index = values_b_index.get(qentry);	

				outs = e.getKey()+"\t";


				line_tmp = getLine(true,file_a_index);
				data_tmp = line_tmp.split("\t");
				outbuff.write(outs+implodeArray(Arrays.copyOfRange(data_tmp, 2, data_tmp.length))+"\t");

				line_tmp = getLine(false,file_b_index);
				data_tmp = line_tmp.split("\t");
				outbuff.write(implodeArray(Arrays.copyOfRange(data_tmp, 2, data_tmp.length))+"\n");

			}

			outbuff.close();
		}catch (Exception e){
			e.printStackTrace();
			throw new SLIB_Ex_Critic("Error: " + e.getMessage());
		}

		removeTmpFiles();

		logger.info("output: "+output);
	}


	private void removeTmpFiles() {

		logger.info("Delete tmp files");

		String fileSeparator = System.getProperty("file.separator");
		String fname;
		File f;
		String filePathName = tmp_file_a_prefix;

		for (int i = 0; i < file_number_a; i++) {
			fname = tmp_dir+fileSeparator+filePathName+"_"+i;
			f = new File(fname);
			f.delete();
		}

		filePathName = tmp_file_b_prefix;

		for (int i = 0; i < file_number_b; i++) {
			fname = tmp_dir+fileSeparator+filePathName+"_"+i;
			f = new File(fname);
			f.delete();
		}


	}

	public String getLine(boolean isFile_A, Long file_index) throws SLIB_Ex_Critic {

		String out = null;
		long file_nb 	= file_index/splitSize;
		long lineNumber = file_index%splitSize;

		String filePathName = tmp_file_a_prefix;

		if(!isFile_A)
			filePathName = tmp_file_b_prefix;

		//		logger.debug(filePathName+" "+file_index+" "+file_nb+" "+lineNumber);

		String fileSeparator = System.getProperty("file.separator");

		try{
			BufferedReader br	    = new BufferedReader(new FileReader(tmp_dir+fileSeparator+filePathName+"_"+file_nb));
			String line;

			long countLine = 0;
			while ((line = br.readLine()) != null)   {
				//				System.out.println(countLine);
				if(countLine == lineNumber){
					out = line.trim();
					break;
				}
				countLine++;
			}
			br.close();

		}catch (IOException e){//Catch exception if any
			throw new SLIB_Ex_Critic(e.getMessage());
		}
		return out;
	}

	public static String implodeArray(String[] inputArray) {

		String output = "";


		StringBuilder sb = new StringBuilder();
		sb.append(inputArray[0]);

		for (int i=1; i<inputArray.length; i++) {
			sb.append("\t");
			sb.append(inputArray[i]);
		}

		output = sb.toString();
		return output;
	}

	public static String implodeArrayList(ArrayList<String> inputArray) {

		String output = "";


		StringBuilder sb = new StringBuilder();
		sb.append(inputArray.get(0));

		for (int i=1; i<inputArray.size(); i++) {
			sb.append("\t");
			sb.append(inputArray.get(i));
		}

		output = sb.toString();
		return output;
	}


	private HashMap<String, String> mergeValues() throws SLIB_Ex_Critic {

		HashMap<String, String> newValues = new HashMap<String, String>();

		Set<Entry<String, String>> eSet = values_a.entrySet();

		for (Entry<String, String> e : eSet) {

			String q = e.getKey();
			String vb = values_b.get(q);

			if(vb != null){

				String newVals = e.getValue();
				newVals += "\t"+vb;
				newValues.put(q,newVals);

			}
			else{
				throw new SLIB_Ex_Critic("Cannot locate entry "+q+" in file "+file_b);
			}
		}
		return newValues;
	}


	private void checkDuplicateHeaderFields() throws SLIB_Ex_Critic {
		for (int i = 2; i < header_a.size(); i++) {
			for (int j = 2; j < header_b.size(); j++) {
				if(header_a.get(i).equals(header_b.get(j))){
					throw new SLIB_Ex_Critic("Duplicate header fields");
				}
			}
		}
	}

	private ArrayList<String> mergeHeaders() throws SLIB_Ex_Critic{

		logger.info("Merging headers");
		checkDuplicateHeaderFields();

		ArrayList<String> newHeader = new ArrayList<String>();

		for (int i = 0; i < header_a.size(); i++)
			newHeader.add(header_a.get(i));
		for (int j = 2; j < header_b.size(); j++) 
			newHeader.add(header_b.get(j));

		return newHeader;
	}


	private void loadData(boolean isFile_A) throws SLIB_Ex_Critic{

		String filePath=file_a;

		if(!isFile_A)
			filePath = file_b;

		ArrayList<String> header = new ArrayList<String>();
		HashMap<String, String> values = new HashMap<String, String>();

		try{
			FileInputStream fstream = new FileInputStream(filePath);
			DataInputStream in 		= new DataInputStream(fstream);
			BufferedReader br	    = new BufferedReader(new InputStreamReader(in));
			String line;
			long countLine = 0;



			boolean headerb = false;
			while ((line = br.readLine()) != null)   {

				line = line.trim();
				countLine++;

				if(!line.startsWith("!") && !line.isEmpty()){

					String[] data = line.split("\t");

					if(headerb == false){
						headerb = true;
						header = new ArrayList<String>(Arrays.asList(data));
						if(data.length < 2 )
							throw new SLIB_Ex_Critic("Corrupted file "+filePath+", result line "+countLine+" header must contains at least two fields");
					}
					else{
						if(data.length != header.size())
							throw new SLIB_Ex_Critic("Corrupted file "+filePath+", result line "+countLine+" contains abnormal number of values considering header");
						else{
							String entry = data[0]+"\t"+data[1];
							if(values.containsKey(entry))
								throw new SLIB_Ex_Critic("Duplicate row "+entry+" line "+countLine);
							String[] vals = Arrays.copyOfRange(data, 2, data.length);
							values.put(entry, implodeArray(vals));
						}
					}
				}
			}
			in.close();
		}catch (IOException e){//Catch exception if any
			throw new SLIB_Ex_Critic(e.getMessage());
		}

		if(isFile_A){
			header_a = header;
			values_a = values;
		}
		else{
			header_b = header;
			values_b = values;
		}
	}

	private void loadDataIndex(boolean isFile_A) throws SLIB_Ex_Critic{


		String filePath=file_a;
		String filePathName = tmp_file_a_prefix;

		if(!isFile_A){
			filePath = file_b;
			filePathName = tmp_file_b_prefix;
		}

		logger.info("Creating index for file "+filePath);

		ArrayList<String> header = new ArrayList<String>();
		HashMap<String, Long> values = new HashMap<String, Long>();

		int file_nb = 0;

		try{
			String line;
			long countLine = 0;
			long countLineFile = 0;

			String fileSeparator = System.getProperty("file.separator");

			FileWriter fstream;
			BufferedWriter outbuff = null;

			boolean headerb = false;

			BigFileReader reader = new BigFileReader(filePath);
			
			while(reader.hasNext()){
			

				if(countLineFile == 0){
					fstream = new FileWriter(tmp_dir+fileSeparator+filePathName+"_"+file_nb);
					outbuff = new BufferedWriter(fstream);

					if(isFile_A)
						file_number_a++;
					else
						file_number_b++;
				}

				line = reader.nextTrimmed();


				if(!line.startsWith("!") && !line.isEmpty()){

					String[] data = line.split("\t");

					if(headerb == false){
						headerb = true;
						header = new ArrayList<String>(Arrays.asList(data));
						if(data.length < 2 )
							throw new SLIB_Ex_Critic("Corrupted file "+filePath+", result line "+(countLine+1)+" header must contains at least two fields");
					}
					else{
						if(data.length != header.size())
							throw new SLIB_Ex_Critic("Corrupted file "+filePath+", result line "+(countLine+1)+" contains abnormal number of values considering header");
						else{
							String entry = data[0]+"\t"+data[1];
							if(values.containsKey(entry))
								throw new SLIB_Ex_Critic("Duplicate row "+entry+" line "+(countLine+1));
							values.put(entry, countLine);
						}
					}
				}
				outbuff.write(line+"\n");

				countLine++;
				countLineFile++;

				if(countLineFile == splitSize){
					outbuff.close();

					file_nb++;
					countLineFile = 0;
					outbuff = null;
				}


			}
			reader.close();

			if(outbuff != null)
				outbuff.close();

		}catch (IOException e){//Catch exception if any
			throw new SLIB_Ex_Critic(e.getMessage());
		}

		if(isFile_A){
			header_a = header;
			values_a_index = values;
		}
		else{
			header_b = header;
			values_b_index = values;
		}
	}



	@SuppressWarnings("unused")
	public static void main(String[] args) {

		ResultsMerger merger = new ResultsMerger();

		String file1 	 = "/home/seb/development/java/workspace/SGL.0.0.1/modules/ssps/ssp_e/data/bioinfo/result_examples/out_genes_pair_ecsim_mf.csv";
		String file1_bis = "/home/seb/development/java/workspace/SGL.0.0.1/modules/ssps/ssp_e/data/bioinfo/result_examples/out_genes_pair_ecsim_mf_2.csv";
		String file2 	 = "/home/seb/development/java/workspace/SGL.0.0.1/modules/ssps/ssp_e/data/bioinfo/result_examples/out_genes_pair_ec_mf_results.csv";
		String out 		 = "/home/seb/development/java/workspace/SGL.0.0.1/modules/ssps/ssp_e/data/bioinfo/result_examples/out_genes_pair_ec_mf_results_merge.csv";

		try {
			//			System.out.println(merger.getLine(true, (long)74999));
			merger.processLarge(file1,file2,out,"/tmp",100);
		} 
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.out.println("Try to merge using large file parameter");
		}
		catch (SLIB_Ex_Critic e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}



