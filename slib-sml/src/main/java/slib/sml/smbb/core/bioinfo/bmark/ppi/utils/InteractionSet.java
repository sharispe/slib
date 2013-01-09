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
 
 
package slib.sml.smbb.core.bioinfo.bmark.ppi.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author seb
 */
public class InteractionSet {
	
	/**
     *
     */
    public ArrayList<String> interactors;
	/**
     *
     */
    public HashSet<Interaction> interactions;
	
	/**
     *
     */
    public InteractionSet(){
		interactors  = new ArrayList<String>();
		interactions = new HashSet<Interaction>();
	}
	
	/**
     *
     * @param a_id
     * @param b_id
     */
    public void addInteraction(Integer a_id, Integer b_id){
		interactions.add(new Interaction(a_id, b_id));
	}

	/**
     *
     * @param a_id
     * @param b_id
     * @return
     */
    public boolean containsInteraction(Integer a_id, Integer b_id){
		
		for(Interaction p:interactions){
			if(p.a == a_id && p.b == b_id)
				return true;
		}
		return false;
	}
	
	
	/**
     *
     * @return
     */
    public HashMap<Integer, HashSet<Integer>> buildFastIndex(){
		
		HashMap<Integer, HashSet<Integer>> index = new HashMap<Integer, HashSet<Integer>>();
		for(Interaction p:interactions){
			
			if(!index.containsKey(p.a))
				index.put(p.a, new HashSet<Integer>());
			
			if(!index.containsKey(p.b))
				index.put(p.b, new HashSet<Integer>());
			
			index.get(p.a).add(p.b);
			index.get(p.b).add(p.a);
		}
		return index;
	}

}
