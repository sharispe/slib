package slib.tools.smltoolkit;

import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author seb
 */
public interface SmlModuleCLI {

	/**
     *
     * @param args
     * @throws SLIB_Exception
     */
    public void execute(String[] args)  throws SLIB_Exception; 
}
