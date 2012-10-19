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
 
 
package slib.sml.smbb.core.bioinfo.bmark.seqsim.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import slib.sglib.model.repo.impl.DataFactoryMemory;
import slib.utils.impl.IO_RuntimeException;


/**
 * Expect an input tabular file without header
 * protA_id	protB_id	blast_score
 * protA_id	protA_id	blast_score
 * ...
 * 
 * 
 * @author Sebastien Harispe
 *
 */
public class BlastScoreLoader implements Iterator<BlastScoreEntry> {

	BufferedReader  br;
	DataInputStream in;
	String filepath;
	String line;
	DataFactoryMemory df = DataFactoryMemory.getSingleton();

	public BlastScoreLoader(String filepath) throws IOException{

		FileInputStream fstream = new FileInputStream(filepath);
		in	 = new DataInputStream(fstream);
		br 	 = new BufferedReader(new InputStreamReader(in));

		line = br.readLine();
	}

	public boolean hasNext() {
		if(line == null){
			try {
				in.close();
			} catch (IOException e) {
				throw new IO_RuntimeException("Error reading file "+filepath+" original "+e.getMessage());
			}
		}

		return line != null;
	}

	/**
	 * Return null if the line is not well formed
	 */
	public BlastScoreEntry next() {

		BlastScoreEntry entry = null;

		try {

			String[] csvRow = line.split("\t");
			String protA_s = null;
			String protB_s = null;
			double score = 0;

			if(csvRow.length == 3){
				protA_s = csvRow[0];
				protB_s = csvRow[1];
				
				try{
					score = Double.parseDouble(csvRow[2]);
				}
				catch(NumberFormatException e){
					line = br.readLine();
					return null;
				}
				entry = new BlastScoreEntry(protA_s,protB_s,score);
				
			}
			line = br.readLine();

		} catch (IOException e) {
			throw new IO_RuntimeException("Error reading file "+filepath+" original "+e.getMessage());
		}
		return entry;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public void close() throws IOException{

		if(br != null)
			br.close();
	}

	public ArrayList<BlastScoreEntry> nextValids(int nbValues) {

		ArrayList<BlastScoreEntry> bench = new ArrayList<BlastScoreEntry>();
		int c = 0;

		while(c < nbValues && hasNext()){
			BlastScoreEntry e = next();
			bench.add(e);
			c++;
		}
		return bench;
	}

}
