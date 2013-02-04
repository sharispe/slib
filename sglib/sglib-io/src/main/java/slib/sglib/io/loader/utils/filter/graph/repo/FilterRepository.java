package slib.sglib.io.loader.utils.filter.graph.repo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import slib.sglib.io.loader.utils.filter.graph.Filter;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe
 */
public class FilterRepository {

    private static  FilterRepository instance;
    public Map<String,Filter> filters;

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
     * @return
     */
    public Filter getFilter(String name) {
        return filters.get(name);
    }

    /**
     *
     * @return
     */
    public Set<Filter> getFilters() {
        return new HashSet<Filter>(filters.values());
    }

    
    public void addFilter(Filter f) throws SLIB_Ex_Critic {
        if(filters.containsKey(f.getId())){
            throw new SLIB_Ex_Critic("Duplicate filter "+f.getId());
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
