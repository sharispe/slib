/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.graph.io.conf;

import java.util.LinkedList;
import java.util.List;
import org.openrdf.model.URI;
import slib.graph.algo.utils.GAction;
import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
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
