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

import java.util.List;


/**
 * Interface defining the generic behavior 
 * a Query iterator must follows.
 * 
 * The Query iterator thus enables to iterate through the full set of queries 
 * contained in a particular repository (in file, in memory collection).
 * 
 * @author Sebastien Harispe
 */
public interface QueryIterator{
	
	

	/**
	 * @return the next available {@link QueryEntry}.
	 * Also return null if the row repository do not 
	 * correspond to a well formed entry.
	 */
	public QueryEntry next();
	
	/**
	 * Return a List containing the next valid queries.
	 * If the number of the next queries is lower than the requested.
	 * number of queries only the available number of queries is returned.
	 *  
	 * @param nbValues the number of values the query list must contains.
	 * @return a List of {@link QueryEntry}
	 * @throws Exception if query access can throw an exception. 
	 */
	public List<QueryEntry> nextValids(int nbValues) throws Exception;

	/**
	 * @return boolean return true if a query is available else either
	 */
	public boolean hasNext();

	
	/**
	 * @return a long corresponding to the number of query available in the number of 
	 * queries available in the repository
	 * @throws Exception
	 */
	public long getNumberQueries() throws Exception;
	
	/**
	 * Close the opened repository
	 * @throws Exception
	 */
	public void close() throws Exception;
}
