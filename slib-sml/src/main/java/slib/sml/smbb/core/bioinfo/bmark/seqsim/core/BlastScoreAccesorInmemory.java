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

import java.io.IOException;
import java.util.HashMap;

import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.impl.QueryEntry;

public class BlastScoreAccesorInmemory implements IblastScoreAcessor{

	HashMap<QueryEntry, Double> scoreStorage;
	
	public BlastScoreAccesorInmemory(String filePath) throws IOException{
		
		scoreStorage = new HashMap<QueryEntry, Double>();
		
		BlastScoreLoader loader = new BlastScoreLoader(filePath);
		while(loader.hasNext()){
			
			BlastScoreEntry e = loader.next();
			if(e != null)
				scoreStorage.put(new QueryEntry(e.protA,e.protB), e.blastScore);
		}
		
		for(QueryEntry q : scoreStorage.keySet())
			System.out.println(q+"\t"+scoreStorage.get(q));
	}
	
	public double getBlastScore(String protA, String protB) throws SGL_Ex_Critic {
		
		QueryEntry qentry = new QueryEntry(protA,protB);
		
		if(!scoreStorage.containsKey(qentry))
			throw new SGL_Ex_Critic("Cannot locate blast score associated to entry "+protA+" "+protB);
		
		return scoreStorage.get(qentry);
	}
}
