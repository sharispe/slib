package slib.sglib.algo.utils;

import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;

/**
 *
 * @author seb
 */
public class GAction extends ParametrableImpl implements CheckableValidity{

	/**
     *
     */
    public GActionType type;
	
	/**
     *
     * @param type
     */
    public GAction(GActionType type){
		this.type = type;
	}

	/**
     *
     * @return
     */
    public boolean isValid() {
		return GActionValidator.valid(this);
	}
}
