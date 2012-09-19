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
 
 
package slib.sml.sme.utils;

import java.util.HashMap;

import slib.utils.ex.SGL_Ex_Critic;

public class SymmetricResultStack {

	String methods;
	HashMap<String, HashMap<String, Double>> results;
	
	
	public SymmetricResultStack(String meth) {
		this.methods = meth;
		results = new HashMap<String, HashMap<String,Double>>();
	}
	
	public void addResult(String o1, String o2, Double score){
		
		if(results.containsKey(o1)){
			results.get(o1).put(o2, score);
		}
		else if(results.containsKey(o2)){
			results.get(o2).put(o1, score);
		}
		else {
			results.put(o1, new HashMap<String, Double>());
			results.get(o1).put(o2, score);
		}
	}
	
	public double getSim(String o1, String o2) throws SGL_Ex_Critic{
		
		if(results.containsKey(o1) && results.get(o1).containsKey(o2)){
			return results.get(o1).get(o2);
		}
		else if(results.containsKey(o2) && results.get(o2).containsKey(o1)){
			return results.get(o2).get(o1);
		}
		else {
			throw new SGL_Ex_Critic("Result not found for comparison "+o1+" / "+o2);
		}
	}

}
