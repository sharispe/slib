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
package slib.tools.module;

import java.util.Arrays;

import slib.graph.io.loader.utils.filter.graph.Filter;
import slib.graph.io.loader.utils.filter.graph.FilterCst;
import slib.graph.io.loader.utils.filter.graph.gaf2.FilterGraph_GAF2;
import slib.graph.io.loader.utils.filter.graph.gaf2.FilterGraph_GAF2_cst;
import slib.graph.io.loader.utils.filter.graph.metrics.FilterGraph_Metrics;
import slib.graph.io.loader.utils.filter.graph.metrics.FilterGraph_Metrics_cst;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;
import slib.utils.impl.Util;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class FilterBuilderGeneric {

    /**
     *
     * @param gconf
     * @return a filter
     * @throws SLIB_Ex_Critic
     */
    public static Filter buildFilter(Conf gconf) throws SLIB_Ex_Critic {

        Filter f = null;

        String id = (String) gconf.getParam(XmlTags.ID_ATTR);
        String type = (String) gconf.getParam(XmlTags.TYPE_ATTR);


        if (id == null) {
            Util.error("Missing " + XmlTags.ID_ATTR + " in a filter specification");
        }

        if (type == null) {
            Util.error("Missing " + XmlTags.TYPE_ATTR + " in a filter specification");
        }

        if (!supportType(type)) {
            Util.error("Unsupported Filter type " + type + " found in filter '" + id + "' specification");
        }

        if (type.equals(FilterGraph_GAF2_cst.TYPE)) {
            f = new FilterGraph_GAF2(gconf);
        } else if (type.equals(FilterGraph_Metrics_cst.TYPE)) {
            f = new FilterGraph_Metrics(gconf);
        } else // do not pass 
        {
            throw new UnsupportedOperationException("Sorry, filter is not taking into account, please repor the issue");
        }

        return f;
    }

    /**
     *
     * @param type
     * @return true if the type of filter is supported
     */
    public static boolean supportType(String type) {
        return Arrays.asList(FilterCst.supportedTypes).contains(type);
    }
}
