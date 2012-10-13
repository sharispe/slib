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
 
 
package slib.utils.impl;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import slib.utils.ex.SLIB_Ex_Critic;

public class ResultStack<X,N extends Number> {
	
	String label;
	Map<X, N> values;
	double max;
	
	public ResultStack(){
		this("undef");
	}
	
	public ResultStack(Map<X,N> map){
		this();
		
		for(Entry<X,N> e : map.entrySet())
			add(e.getKey(), e.getValue());
	}
	
	/**
	 * @param label Method used 
	 */
	public ResultStack(String label){
		this.label = label;
		
		values = new ConcurrentHashMap<X, N>();
		max = -Double.MAX_VALUE;
	}
	
	public void add(X v, N val){
		values.put(v, val);
		
		if(val.doubleValue() > max)
			max = val.doubleValue();
	}
	
	/**
	 * Return the value associated to the given key
	 * @param v the key queried
	 * @return the double value associated to the queried key
	 * @throws SGL_Exception_Dev if the queried value is not found
	 */
	public N get(X v) throws SLIB_Ex_Critic{
		
		N val = values.get(v);
		
		if(val == null)
			throw new SLIB_Ex_Critic("Error in "+ResultStack.class.getName()+" Try to access a value associated to a non existing key "+v+" \nResultStack info :"+toString());
		return val;
	}
	
	/**
	 * Return the value associated to the given key
	 * @param v the key queried
	 * @return the double value associated to the queried key
	 * @throws SGL_Exception_Dev if the queried value is not found
	 */
	public boolean containsKey(X v) throws SLIB_Ex_Critic{
		return values.containsKey(v);
	}
	
	public void remove(X v){
		
		N val = values.get(v);
		
		if(val != null){
			
			values.remove(v);
			
			if(max == val.doubleValue())
				searchMax();
		}
	}
	
	public double getMax(){
		return max;
	}
	
	public double getMin(){
		
		double min = Double.MAX_VALUE;
		
		for(X v: values.keySet()){
			
			double tmpVal = values.get(v).doubleValue();
			
			if(min > tmpVal)
				min = tmpVal;
		}
		return min;
	}
	
	public double getMinSupNil(){
		
		double min = Double.MAX_VALUE;
		
		for(X v: values.keySet()){
			
			double tmpVal = values.get(v).doubleValue();
			
			if(min > tmpVal && tmpVal>0)
				min = tmpVal;
		}
		return min;
	}

	private void searchMax(){
		
		max = -Double.MAX_VALUE;;
		
		double tmpVal = -Double.MAX_VALUE;
		
		for(X v: values.keySet()){
			
			tmpVal = values.get(v).doubleValue();
			
			if(max < tmpVal)
				max = tmpVal;
		}
	}

	public String getLabel() {
		return label;
	}

	public Map<X, N> getValues() {
		return values;
	}
	
	public Set<X> keySet(){
		return values.keySet();
	}
	
	public int size(){
		return values.keySet().size();
	}
	
	public Set<Entry<X,N>> entrySet(){
		return values.entrySet();
	}
	
	public void printValues(){
		for(X v: values.keySet())
			System.out.println(v+"\t"+values.get(v));
	}
	
	public String toString(){
		String out = "label : "+label+"\n";
		out += "values "+values.size();
		out += "\nmax "+getMax();
		out += "\nmin "+getMin();
		out += "\nmin (0 excluded)"+getMinSupNil();
		
		return out;
		
	}
}
