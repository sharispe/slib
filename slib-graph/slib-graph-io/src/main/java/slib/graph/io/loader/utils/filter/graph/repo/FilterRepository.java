/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.graph.io.loader.utils.filter.graph.repo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import slib.graph.io.loader.utils.filter.graph.Filter;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class FilterRepository {

    private static FilterRepository instance;
    public Map<String, Filter> filters;

    public static FilterRepository getInstance() {
        if (instance == null) {
            instance = new FilterRepository();
        }
        return instance;
    }

    private FilterRepository() {
        filters = new HashMap<String, Filter>();
    }

    /**
     *
     * @param name
     * @return the filter associated to the given name.
     */
    public Filter getFilter(String name) {
        return filters.get(name);
    }

    /**
     *
     * @return the filter loaded in the repository.
     */
    public Set<Filter> getFilters() {
        return new HashSet<Filter>(filters.values());
    }

    public void addFilter(Filter f) throws SLIB_Ex_Critic {
        if (filters.containsKey(f.getId())) {
            throw new SLIB_Ex_Critic("Duplicate filter " + f.getId());
        }
        filters.put(f.getId(), f);
    }

    public Filter containsFilter(String fname) throws SLIB_Ex_Critic {
        return filters.get(fname);
    }

    /**
     * Remove loaded filters
     */
    public void clear() {
        filters.clear();
    }
}
