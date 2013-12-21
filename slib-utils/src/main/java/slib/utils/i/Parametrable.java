package slib.utils.i;

import java.util.Map;

/**
 *
 * @author Sébastien Harispe
 */
public interface Parametrable {

    /**
     *
     * @param name
     * @param o
     */
    public void addParameter(String name, Object o);

    /**
     *
     * @param name
     * @return the parameter associated to the given key.
     */
    public Object getParameter(String name);

    /**
     *
     * @param pname
     * @return true if a parameter is associated to the given key.s
     */
    public boolean existsParam(String pname);

    /**
     *
     * @param name
     */
    public void removeParameter(String name);

    /**
     *
     */
    public void clear();

    /**
     *
     * @return all parameters
     */
    public Map<String, Object> getParams();
}
