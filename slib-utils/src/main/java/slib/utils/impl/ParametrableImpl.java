package slib.utils.impl;

import java.util.HashMap;
import java.util.Map;
import slib.utils.i.Parametrable;

/**
 * @author Sébastien Harispe
 */
public class ParametrableImpl implements Parametrable {

    private Map<String, Object> params;

    
    public ParametrableImpl() {
    }

    /**
     *
     * @param name
     * @param o
     */
    @Override
    public void addParameter(String name, Object o) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        params.put(name, o);
    }

    /**
     *
     * @param name
     * @return the value associated to the key.
     */
    @Override
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
    @Override
    public void removeParameter(String name) {
        if (existsParam(name)) {
            params.remove(name);
        }
    }

    /**
     *
     */
    @Override
    public void clear() {
        params = null;
    }

    /**
     *
     * @param pname
     * @return true if the parameter exists.
     */
    @Override
    public boolean existsParam(String pname) {
        return (params != null && params.containsKey(pname));
    }

    /**
     * Can be null
     *
     * @return the map of parameters and associated values.
     */
    @Override
    public Map<String, Object> getParams() {
        return params;
    }
}
