package slib.sglib.io.conf;

import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.URI;
import slib.sglib.algo.graph.utils.GAction;
import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;

/**
 *
 * @author seb
 */
public class GraphConf extends ParametrableImpl implements CheckableValidity, Comparable<Object> {

    URI uri;
    List<GDataConf> data;
    List<GAction> actions;

    /**
     *
     */
    public GraphConf() {
        data = new LinkedList<GDataConf>();
        actions = new LinkedList<GAction>();
    }

    /**
     *
     * @param uri
     */
    public GraphConf(URI uri) {
        this();
        this.uri = uri;
    }

    @Override
    public boolean isValid() {

        if (uri == null) {
            return false;
        }

        for (GDataConf dataFile : data) {
            if (!dataFile.isValid()) {
                return false;
            }
        }

        for (GAction action : actions) {
            if (!action.isValid()) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     * @param conf
     */
    public void addGDataConf(GDataConf conf) {
        data.add(conf);
    }

    /**
     *
     * @param conf
     */
    public void addGAction(GAction conf) {
        actions.add(conf);
    }

    /**
     *
     * @return the URI associated to the graph the configuration is associated
     * to.
     */
    public URI getUri() {
        return uri;
    }

    /**
     *
     * @param uri
     */
    public void setUri(URI uri) {
        this.uri = uri;
    }

    /**
     *
     * @return the list of data configuration associated to the graph
     * configuration.
     */
    public List<GDataConf> getData() {
        return data;
    }

    /**
     *
     * @param data
     */
    public void setData(List<GDataConf> data) {
        this.data = data;
    }

    /**
     *
     * @return the list of actions associated to the configuration.
     */
    public List<GAction> getActions() {
        return actions;
    }

    /**
     *
     * @param actions
     */
    public void setActions(List<GAction> actions) {
        this.actions = actions;
    }

    @Override
    public int compareTo(Object o) {
        return 1;
    }
}
