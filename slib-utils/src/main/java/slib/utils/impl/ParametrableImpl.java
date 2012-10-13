package slib.utils.impl;

import java.util.HashMap;
import java.util.Map;
import slib.utils.i.Parametrable;

public class ParametrableImpl implements Parametrable {

    private Map<String, Object> params;

    public ParametrableImpl() {
    }

    public void addParameter(String name, Object o) {
        if (params == null) {
            params = new HashMap<String, Object>();
        }
        params.put(name, o);
    }

    public Object getParameter(String name) {
        if (params == null) {
            return null;
        }
        return params.get(name);
    }

    public void removeParameter(String name) {
        if (existsParam(name)) {
            params.remove(name);
        }
    }

    public void clear() {
        params = null;
    }

    public boolean existsParam(String pname) {
        return (params != null && params.containsKey(pname));
    }

    /**
     * Can be null
     */
    public Map<String, Object> getParams() {
        return params;
    }
}
