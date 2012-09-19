package slib.sglib.algo.utils;

import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;

public class GAction extends ParametrableImpl implements CheckableValidity{

	public GActionType type;
	
	public GAction(GActionType type){
		this.type = type;
	}

	public boolean isValid() {
		return GActionValidator.valid(this);
	}
}
