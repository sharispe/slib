package slib.utils.impl;

import java.util.HashMap;
import java.util.Map;
import slib.utils.i.Parametrable;

/**
 *
 * @author seb
 */
public class ParametrableImpl implements Parametrable {

    private Map<String, Object> params;

    /**
     *
     */
    public ParametrableImpl() {
    }

    /**
     *
     * @param name
     * @param o
     */
    public void addParameter(String name, Object o) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        params.put(name, o);
    }

    /**
     *
     * @param name
     * @return
     */
    public Object getParameter(String name) {
        if (params == null) {
            return null;
        }
        return params.get(name);
    }

    /**
     *
     * @param name
     */
    public void removeParameter(String name) {
        if (existsParam(name)) {
            params.remove(name);
        }
    }

    /**
     *
     */
    public void clear() {
        params = null;
    }

    /**
     *
     * @param pname
     * @return
     */
    public boolean existsParam(String pname) {
        return (params != null && params.containsKey(pname));
    }

    /**
     * Can be null
     * @return 
     */
    public Map<String, Object> getParams() {
        return params;
    }
}
