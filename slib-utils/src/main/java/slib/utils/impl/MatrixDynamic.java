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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import slib.utils.ex.SGL_Ex_Critic;


public class MatrixDynamic<N extends Number>{
	
	HashMap<Object,Integer> columnIndex;
	HashMap<Object,Integer> rowIndex;
	
	ArrayList<ArrayList<N>> matrix;
	
	public MatrixDynamic(Set<Object> columResources,Set<Object> rowResources){
		
		columnIndex = new HashMap<Object, Integer>(columResources.size());
		rowIndex 	= new HashMap<Object, Integer>(rowIndex.size());
		
		matrix = new ArrayList<ArrayList<N>>(columResources.size());
		
		int id = 0;
		
		for(Object rc: columResources){
			columnIndex.put(rc, id);
			ArrayList<N> column = new ArrayList<N>(rowResources.size());
			
			for (int i = 0; i < rowResources.size(); i++)
				column.add(null);
			matrix.add(column);
			id++;
		}
		
		id = 0;
		for(Object rc: rowResources){
			rowIndex.put(rc, id);
			id++;
		}
	}
	
	public ArrayList<N> getColumn(Object r){
		return matrix.get(columnIndex.get(r));
	}
	
	public ArrayList<N> getRow(Object r){
		
		if(!rowIndex.keySet().contains(r))
			return null;
		
		ArrayList<N> row = new ArrayList<N>(columnIndex.keySet().size());
		for(Object c: columnIndex.keySet())
			row.add(getValue(c, r));
		
		return row;
	}
	
	public N getValue(Object colResource,Object rowResource){
		return matrix.get(columnIndex.get(colResource)).get(rowIndex.get(rowResource));
	}
	
	public void setValue(Object colResource,Object rowResource, N value){
		matrix.get(columnIndex.get(colResource)).set(rowIndex.get(rowResource),value);
	}
	
	public boolean isInRowIndex(Object r){
		return rowIndex.keySet().contains(r);
	}
	
	public boolean isInColumnIndex(Object r){
		return columnIndex.keySet().contains(r);
	}
	
	public boolean isInIndexed(Object r){
		return isInRowIndex(r) || isInColumnIndex(r);
	}
	
	public int getNbColumns(){
		return columnIndex.size();
	}
	
	public int getNbRows(){
		return rowIndex.size();
	}
	
	public void addColumn(Object r) throws SGL_Ex_Critic{
		if(columnIndex.containsKey(r))
			throw new SGL_Ex_Critic("Column "+r+"already exists");
		
		int id = columnIndex.size()+1;
		columnIndex.put(r, id);
		
		ArrayList<N> column = new ArrayList<N>(rowIndex.size());
			
		for (int i = 0; i < rowIndex.size(); i++)
			column.add(null);
		
		matrix.add(column);
	}
	
	public void addRow(Object r) throws SGL_Ex_Critic{
		if(rowIndex.containsKey(r))
			throw new SGL_Ex_Critic("Row "+r+"already exists");
		
		int id = rowIndex.size()+1;
		rowIndex.put(r, id);
			
		for (int i = 0; i < columnIndex.size(); i++)
			matrix.get(i).add(null);
	}
	
	public boolean isSquare(){
		return columnIndex.size() == rowIndex.size();
	}
}
