package slib.sglib.algo.graph.utils;

import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;

/**
 * Class used to represent an action which is applicable to a graph.
 *
 * @author Harispe Sébastien
 */
public class GAction extends ParametrableImpl implements CheckableValidity {

    /**
     * The type of action
     */
    public GActionType type;

    /**
     * Build an instance of GAction considering the given type.
     * @param type the type of action
     */
    public GAction(GActionType type) {
        this.type = type;
    }

    /**
     * Check if the action is valid with regards to the parameters required to be specified.
     * @return true if the configuration is valid, false either
     */
    @Override
    public boolean isValid() {
        return GActionValidator.valid(this);
    }
}
