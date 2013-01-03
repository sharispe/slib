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
 
 
package slib.sglib.model.impl.graph.weight;

import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.URI;

import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.weight.GWS;


/**
 * In memory Graph Weighting Scheme (GWS) implementation of {@link GWS}
 * 
 * @author Sebastien Harispe
 *
 */
public class GWS_impl implements GWS{

	double defaultWeight = 1;
	Map<URI, Double> eTypeWeights;
	Map<E, Double> eWeights;
	
	public GWS_impl(){}

	public GWS_impl(double defaultWeight){
		this.defaultWeight = defaultWeight;
	}

	public double getDefaultWeight() {
		return defaultWeight;
	}

	public void setDefaultWeight(double w) {
		defaultWeight = w;	
	}
	
	public boolean existsWeight(E e) {
		if(eWeights != null && eWeights.containsKey(e))
			return true;
		return false;
	}

	public void addWeight(E e,double w) {
		if(eWeights == null)
			eWeights = new HashMap<E, Double>();
		eWeights.put(e, w);
	}

	public double getWeight(E e){

		Double w;

		if(eWeights == null){
			
			if(eTypeWeights == null)
				return defaultWeight;
			else{
				w = eTypeWeights.get(e.getURI());
				if(w == null)
					return defaultWeight;
			}
		}
		else
			w = eWeights.get(e);

		if(w == null)
			return defaultWeight;

		return w;
	}

	public void setWeight(E e, double w) {
		if(eWeights == null)
			eWeights = new HashMap<E, Double>();
		eWeights.put(e, w);
	}
	
	public boolean existsWeight(URI e) {
		if(eTypeWeights != null && eTypeWeights.containsKey(e))
			return true;
		return false;
	}

	public void addWeight(URI e,double w) {
		if(eTypeWeights == null)
			eTypeWeights = new HashMap<URI, Double>();
		eTypeWeights.put(e,w);
	}

	public double getWeight(URI e){
		Double w;

		w = eTypeWeights.get(e);

		if(w == null)
			return defaultWeight;

		return w;
	}

	public void setWeight(URI e, double w) {
		
		addWeight(e,w);
		
		// As edge type weight is set we remove loaded weight
		// the edge type weight will then be considered instead
		removeLoadedETypeWeight(e); 
	}

	/**
	 * Remove the weight associated to the edge of the corresponding eType
	 * @param etype the type to consider
	 */
	private void removeLoadedETypeWeight(URI etype) {
		if(eWeights != null){

			for(E e : eWeights.keySet()){
				if(e.getURI().equals(etype))
					eWeights.remove(e);
			}

		}
	}
	
	public String toString(){
		
		String out = "WS: Configuration\ndefaultWeight "+defaultWeight;
		if(eWeights == null)
			out += "\neWeights undefined";
		else
			out += "\neWeights size "+eWeights.size();
		
		if(eTypeWeights == null)
			out += "\neTypeWeights undefined";
		else
			out += "\neTypeWeights size "+eTypeWeights.size();

		return out;
	}

}
