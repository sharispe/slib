package slib.utils.i;

import java.util.Map;

/**
 *
 * @author seb
 */
public interface Parametrable{

	/**
     *
     * @param name
     * @param o
     */
    public void addParameter(String name, Object o);
	/**
     *
     * @param name
     * @return
     */
    public Object getParameter(String name);
	/**
     *
     * @param pname
     * @return
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
     * @return
     */
    public Map<String,Object> getParams();
}
