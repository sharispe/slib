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
package slib.tools.smltoolkit.sm.cli.core.utils.calc;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.graph.algo.extraction.rvf.instances.InstancesAccessor;
import slib.graph.model.graph.G;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.tools.smltoolkit.sm.cli.conf.xml.utils.Sm_XML_Cst;
import slib.tools.smltoolkit.sm.cli.core.SmCli;
import slib.tools.smltoolkit.sm.cli.core.utils.ActionsParams;
import slib.tools.smltoolkit.sm.cli.core.utils.SMQueryParam;
import slib.tools.smltoolkit.sm.cli.core.utils.SMutils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.QueryEntry;
import slib.utils.threads.PoolWorker;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class EntityToEntity_Thread implements Callable<ThreadResultsQueryLoader> {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    PoolWorker poolWorker;
    int skipped = 0;
    int setValue = 0;
    Collection<QueryEntry> queriesBench;
    InstancesAccessor iAccessor;
    SMQueryParam queryParam;
    SmCli sspM;
    G g;
    private int nbMeasures;

    /**
     *
     * @param poolWorker
     * @param queriesBench
     * @param sspM
     * @param nbMeasures
     * @param queryParam
     */
    public EntityToEntity_Thread(PoolWorker poolWorker, Collection<QueryEntry> queriesBench, SmCli sspM, int nbMeasures, SMQueryParam queryParam) {

        this.poolWorker = poolWorker;
        this.queriesBench = queriesBench;
        this.sspM = sspM;
        this.nbMeasures = nbMeasures;
        this.g = sspM.getGraph();
        this.iAccessor = sspM.getiAccessor();
        this.queryParam = queryParam;
    }

    @Override
    public ThreadResultsQueryLoader call() throws Exception {

        ThreadResultsQueryLoader results = null;
        try {

            results = new ThreadResultsQueryLoader(queriesBench.size());


            URIFactory factory = URIFactoryMemory.getSingleton();

            String uriE1s, uriE2s, ids_pairs;
            StringBuilder tmp_buffer = new StringBuilder();

            boolean printBaseName = queryParam.isOutputBaseName();
            boolean useLoadedPrefixes = queryParam.isUseLoadedURIprefixes();
            boolean useLoadedPrefixesOutput = queryParam.isUseLoadedURIprefixesOutput();

            URI e1, e2;
            Set<URI> setE1, setE2;
            double sim;
            

            for (QueryEntry q : queriesBench) {

                // flush and clear tmp_buffer
                // this must be done as under some condition their is no guaranty
                // the iteration will be skipped using a continue statement. 
                results.buffer.append(tmp_buffer);
                tmp_buffer.delete(0, tmp_buffer.length());

                uriE1s = q.getKey();
                uriE2s = q.getValue();



                try {
                    e1 = factory.getURI(uriE1s, useLoadedPrefixes);
                    e2 = factory.getURI(uriE2s, useLoadedPrefixes);

                } catch (IllegalArgumentException e) {
                    throw new SLIB_Ex_Critic("Query file contains an invalid URI: " + e.getMessage());
                }

                if (printBaseName) {
                    if (useLoadedPrefixesOutput) {
                        ids_pairs = factory.shortURIasString(e1) + "\t" + factory.shortURIasString(e2);
                    } else {
                        ids_pairs = uriE1s + "\t" + uriE2s;
                    }
                } else {
                    ids_pairs = e1.getLocalName() + "\t" + e2.getLocalName();
                }


                if (!g.containsVertex(e1) || !g.containsVertex(e2)) {


                    if (queryParam.getNoFoundAction() == ActionsParams.SET) {

                        setValue++;
                        tmp_buffer.append(ids_pairs);
                        for (int i = 0; i < nbMeasures; i++) {
                            tmp_buffer.append("\t").append(queryParam.getNoFoundScore());
                        }

                        tmp_buffer.append("\n");

                        //results.buffer.append(tmp_buffer);

                    } else if (queryParam.getNoFoundAction() == ActionsParams.EXCLUDE) {

                        skipped++;

                    } else if (queryParam.getNoFoundAction() == ActionsParams.STOP) {
                        if (!g.containsVertex(e1)) {
                            throw new SLIB_Ex_Critic("Cannot locate " + e1 + " in " + g.getURI());
                        }
                        if (!g.containsVertex(e2)) {
                            throw new SLIB_Ex_Critic("Cannot locate " + e2 + " in " + g.getURI());
                        }
                    }
                    if (!sspM.QUIET) {
                        logger.info(queryParam.getNoFoundAction() + " " + e1 + " (FOUND = " + g.containsVertex(e1) + ") / " + e2 + " (FOUND = " + g.containsVertex(e2) + ")");
                    }
                    continue;
                }


                setE1 = iAccessor.getDirectClass(e1);
                setE2 = iAccessor.getDirectClass(e2);

                if (setE1.isEmpty() || setE2.isEmpty()) {

                    if (queryParam.getNoAnnotAction() == ActionsParams.SET) {
                        setValue++;

                        tmp_buffer.append(ids_pairs);
                        for (int i = 0; i < nbMeasures; i++) {
                            tmp_buffer.append("\t").append(queryParam.getNoAnnotationScore());
                        }

                        tmp_buffer.append("\n");

                        results.buffer.append(tmp_buffer);
                    } else if (queryParam.getNoAnnotAction() == ActionsParams.EXCLUDE) {

                        skipped++;

                    } else if (queryParam.getNoAnnotAction() == ActionsParams.STOP) {
                        throw new SLIB_Ex_Critic("Stop the execution because an entry contains an element without annotations "
                                + e1 + " (annot size = " + setE1.size() + ") / " + e2 + " (annot size = " + setE2.size() + ")."
                                + " You can exclude those entries or set a value, please consult the documentation");
                    }

                    if (!sspM.QUIET) {
                        logger.info(queryParam.getNoAnnotAction() + " " + e1 + " (annot size = " + setE1.size() + ") / " + e2 + " (annot size = " + setE2.size() + ")");
                    }
                    continue;
                }
                
                tmp_buffer.append(ids_pairs);

                for (SMconf m : sspM.conf.gConfGroupwise) {

                    if (SMConstants.SIM_GROUPWISE_ADD_ON.containsKey(m.getFlag())) {

                        String pm_id = m.getParamAsString(Sm_XML_Cst.PAIRWISE_MEASURE_ATTR);
                        SMconf pm_conf = null;

                        for (SMconf p : sspM.conf.gConfPairwise) {
                            if (pm_id.equals(p.getId())) {
                                pm_conf = p;
                                break;
                            }
                        }
                        if (pm_conf == null) {
                            throw new SLIB_Ex_Critic("Cannot locate configuration associated to pairwise measure " + pm_id);
                        }

                        sim = sspM.simManager.compare(m, pm_conf, setE1, setE2);

                        tmp_buffer.append("\t").append(sim);

                        if (Double.isNaN(sim) || Double.isInfinite(sim)) {
                            SMutils.throwArithmeticCriticalException(m, pm_conf, e1, e2, sim);
                        }
                    } else {

                        sim = sspM.simManager.compare(m, setE1, setE2);

                        if (Double.isNaN(sim) || Double.isInfinite(sim)) {
                            SMutils.throwArithmeticCriticalException(m, e1, e2, sim);
                        }

                        tmp_buffer.append("\t");
                        tmp_buffer.append(sim);
                    }
                }
                tmp_buffer.append("\n");
            }
            results.buffer.append(tmp_buffer);
            results.setSetValue(setValue);
            results.setSkipped(skipped);

        } catch (SLIB_Ex_Critic e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new Exception(e);
        } finally {
            poolWorker.taskComplete();
        }
        return results;
    }
}