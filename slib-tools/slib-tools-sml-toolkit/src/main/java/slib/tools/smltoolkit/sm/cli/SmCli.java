/*

 Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

 This software is a computer program whose purpose is to 
 process semantic graphs.

 This software is governed by the CeCILL  license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 */
package slib.tools.smltoolkit.sm.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.algo.graph.extraction.rvf.instances.impl.InstanceAccessor_RDF_TYPE;
import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.repo.GraphRepositoryMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.GraphRepository;
import slib.sglib.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.tools.module.XmlTags;
import slib.tools.smltoolkit.SmlModuleCLI;
import slib.tools.smltoolkit.sm.cli.conf.xml.loader.Sm_XMLConfLoader;
import slib.tools.smltoolkit.sm.cli.conf.xml.utils.Sm_XML_Cst;
import slib.tools.smltoolkit.sm.cli.utils.ActionParamsUtils;
import slib.tools.smltoolkit.sm.cli.utils.ActionsParams;
import slib.tools.smltoolkit.sm.cli.utils.ConceptToConcept_Thread;
import slib.tools.smltoolkit.sm.cli.utils.EntityToEntity_Thread;
import slib.tools.smltoolkit.sm.cli.utils.FileWriterUtil;
import slib.tools.smltoolkit.sm.cli.utils.QueryConceptsIterator;
import slib.tools.smltoolkit.sm.cli.utils.SmCmdHandler;
import slib.tools.smltoolkit.sm.cli.utils.ThreadResultsQueryLoader;
import slib.utils.ex.SLIB_Exception;
import slib.utils.i.Conf;
import slib.utils.impl.QueryEntry;
import slib.utils.impl.QueryFileIterator;
import slib.utils.impl.QueryIterator;
import slib.utils.impl.Util;
import slib.utils.threads.PoolWorker;
import slib.utils.threads.ThreadManager;

/**
 *
 * @author Sébastien Harispe
 */
public class SmCli implements SmlModuleCLI {

    Logger logger = LoggerFactory.getLogger(SmCli.class);
    public Sm_XMLConfLoader conf;
    public SM_Engine simManager;
    InstancesAccessor iAccessor;
    public ActionsParams NO_ANNOTATION_ACTION = ActionsParams.EXCLUDE;
    public ActionsParams NOT_FOUND_ACTION = ActionsParams.STOP;
    public double NO_ANNOTATION_SCORE = 0;
    public double NOT_FOUND_SCORE = 0;
    public boolean QUIET = false;
    URIFactory factory = URIFactoryMemory.getSingleton();
    G graph;
    int SIZE_BENCH = 2000;
    boolean CACHE_PAIRWISE_RESULTS = false;

    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    @Override
    public void execute(String[] args) throws SLIB_Exception {
        SmCmdHandler c = new SmCmdHandler(args);
        if (c.xmlConfFile != null) {
            execute(c.xmlConfFile);
        } else {
            String profileconf = "/tmp/sml-xmlconf.xml";
            logger.debug("Writing profile configuration to " + profileconf);
            FileWriterUtil.writeToFile(profileconf, c.xmlConfAsString);
            execute(profileconf);
            logger.debug("A profile has been executed");
        }
    }

    /**
     * Execute from configuration file
     *
     * @param confFile
     * @throws SLIB_Exception
     */
    public void execute(String confFile) throws SLIB_Exception {

        conf = new Sm_XMLConfLoader(confFile);


        // Load the graph and perform required graph treatments
        GraphLoaderGeneric.load(conf.generic.getGraphConfs());

        logger.info("Retrieving the graph " + conf.graphURI);

        URI graphURI = factory.createURI(conf.graphURI);

        GraphRepository graphRepo = GraphRepositoryMemory.getSingleton();

        if (!graphRepo.isGraphRegistred(graphURI)) {
            Util.error("No graph associated to the uri " + conf.graphURI + " was loaded...");
        }

        graph = graphRepo.getGraph(graphURI);

        logger.info("Graph information:\n" + graph.toString());

        simManager = new SM_Engine(graph);


        for (ICconf icConf : conf.gConfICs) {
            simManager.computeIC(icConf);
        }


        if (conf.getCachePairwiseResults() == null) {
            simManager.setCachePairwiseResults(CACHE_PAIRWISE_RESULTS);
        } else {
            simManager.setCachePairwiseResults(conf.getCachePairwiseResults());
        }

        if (conf.getBenchSize() != null) {
            SIZE_BENCH = conf.getBenchSize();
        }



        if (conf.isQuiet()) {
            QUIET = true;
        } else {
            QUIET = false;
        }


        logger.info("quiet      : " + QUIET);
        logger.info("Bench size : " + SIZE_BENCH);
        logger.info("Number of threads allowed: " + ThreadManager.getSingleton().getCapacity());



        // check if measure evaluated require DAG structure
        if (requireDAG()) {
            logger.info("checking DAG property");
            ValidatorDAG dagVal = new ValidatorDAG();
            boolean rootedGraph = dagVal.containsRootedTaxonomicDag(graph);

            if (!rootedGraph) {
                logger.error("Multiple root detected please specify a root or use root=\"FICTIVE\"");
            }
        }

        iAccessor = new InstanceAccessor_RDF_TYPE(graph);

        computeQueries();

        logger.info("process done");
    }

    private void computeQueries() throws SLIB_Exception {

        logger.info("Start computation of " + conf.gConfQueries.size() + " queries");

        try {
            for (Conf gconf : conf.gConfQueries) {

                String id = (String) gconf.getParam(XmlTags.ID_ATTR);
                String type = (String) gconf.getParam(XmlTags.TYPE_ATTR);
                String infile = (String) gconf.getParam(XmlTags.FILE_ATTR);
                String output = (String) gconf.getParam(XmlTags.OUTPUT_ATTR);
                String uri_prefix = (String) gconf.getParam(XmlTags.URI_PREFIX_ATTR);
                String noAnnotsConf_s = (String) gconf.getParam(Sm_XML_Cst.OPT_NO_ANNOTS_ATTR);
                String notFound_s = (String) gconf.getParam(Sm_XML_Cst.OPT_NOT_FOUND_ATTR);

                if (notFound_s != null) {
                    NOT_FOUND_ACTION = ActionParamsUtils.getAction(notFound_s);
                    if (NOT_FOUND_ACTION == ActionsParams.SET) {
                        NOT_FOUND_SCORE = ActionParamsUtils.getSetValue(notFound_s);
                    }
                }

                if (noAnnotsConf_s != null) {
                    NO_ANNOTATION_ACTION = ActionParamsUtils.getAction(noAnnotsConf_s);
                    if (NO_ANNOTATION_ACTION == ActionsParams.SET) {
                        NO_ANNOTATION_SCORE = ActionParamsUtils.getSetValue(noAnnotsConf_s);
                    }
                }
                System.out.println(notFound_s + "\t" + NOT_FOUND_ACTION);
                System.out.println(noAnnotsConf_s + "\t" + NO_ANNOTATION_ACTION);

               

                if (uri_prefix == null) {
                    uri_prefix = "";
                }

                logger.info("Query :" + id);

                logger.info("Not Found : " + NOT_FOUND_ACTION);

                if (NOT_FOUND_ACTION == ActionsParams.SET) {
                    logger.info("score associated to entries for which an element is not found in the knowledge base: " + NOT_FOUND_SCORE);
                }

                logger.info("No Annotations : " + NO_ANNOTATION_ACTION);

                if (NO_ANNOTATION_ACTION == ActionsParams.SET) {
                    logger.info("score associated to entities with no annotations : " + NO_ANNOTATION_SCORE);
                }


                // require file
                if (type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC) || type.equals(Sm_XML_Cst.QUERIES_TYPE_OTOO)) {

                    QueryIterator qloader = new QueryFileIterator(infile, uri_prefix);

                    if (type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC)) {
                        perform_cTOc(qloader, output);
                    } else if (type.equals(Sm_XML_Cst.QUERIES_TYPE_OTOO)) {
                        perform_oTOo(qloader, output);
                    }
                } else if (type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC_FULL)) {

                    QueryIterator qloader = new QueryConceptsIterator(simManager.getClasses());

                    perform_cTOc(qloader, output);

                } else {
                    throw new UnsupportedOperationException(type + " is not a supported " + XmlTags.TYPE_ATTR + " of queries");
                }
            }
        } catch (Exception e) {
            throw new SLIB_Exception(e);
        }
    }

    /**
     * @param queryFile
     * @param output
     * @throws SGL_Exception
     */
    private void perform_oTOo(QueryIterator qloader, String output) throws SLIB_Exception {


        logger.info("Starting computing query " + Sm_XML_Cst.QUERIES_TYPE_OTOO);

        ThreadManager threadManager = ThreadManager.getSingleton();
        PoolWorker poolWorker = null;

        try {


            long queries_number = qloader.getNumberQueries();
            logger.info("Number of query " + queries_number);

            FileWriter fstream = new FileWriter(output);
            BufferedWriter file = new BufferedWriter(fstream);

            // Build Header
            String header = Sm_XML_Cst.E1_ATTR + "\t" + Sm_XML_Cst.E1_ATTR;

            int nbMeasures = conf.gConfGroupwise.size();

            for (SMconf m : conf.gConfGroupwise) {
                header += "\t" + m.label;
            }

            file.write(header + "\n");

            int skipped = 0;
            int setValue = 0;

            // Compute measure results

            poolWorker = threadManager.getMaxLoadPoolWorker();

            long count = 0;

            List<Future<ThreadResultsQueryLoader>> results = new ArrayList<Future<ThreadResultsQueryLoader>>();


            while (qloader.hasNext()) {

//                Thread.sleep(100);// To create thread wave
//                logger.debug("Await Free Resource, load " + poolWorker.getLoad() + "/" + poolWorker.getCapacity());
                poolWorker.awaitFreeResource();

                List<QueryEntry> queriesBench = qloader.nextValids(SIZE_BENCH);

                EntityToEntity_Thread callable = new EntityToEntity_Thread(poolWorker, queriesBench, this, nbMeasures);

                poolWorker.addTask();
//                logger.debug("- Adding Thread task " + poolWorker.getLoad() + "/" + poolWorker.getCapacity());
                Future<ThreadResultsQueryLoader> future = poolWorker.getPool().submit(callable);

                results.add(future);

                // process results
                Iterator<Future<ThreadResultsQueryLoader>> it = results.iterator();
                while (it.hasNext()) {

                    Future<ThreadResultsQueryLoader> r = it.next();

                    if (r.isDone()) {

                        ThreadResultsQueryLoader rez = r.get();

                        file.write(rez.buffer.toString());

                        skipped += rez.getSkipped();
                        setValue += rez.getSetValue();

                        count += rez.getJobSize();
                        logger.info("- " + count + " / " + queries_number + "\tskipped " + skipped + "\tsetted results " + setValue);

                        it.remove();
                    }
                }
            }
            qloader.close();

            poolWorker.shutdown();

            // process results
            Iterator<Future<ThreadResultsQueryLoader>> it = results.iterator();
            while (it.hasNext()) {

                Future<ThreadResultsQueryLoader> r = it.next();

                if (r.isDone()) {
                    ThreadResultsQueryLoader rez = r.get();

                    file.write(rez.buffer.toString());

                    skipped += rez.getSkipped();
                    setValue += rez.getSetValue();

                    count += rez.getJobSize();
                    logger.info("- " + count + " / " + queries_number + "\tskipped " + skipped + "\tsetted results " + setValue);

                    it.remove();
                }
            }

            file.close();
            logger.info("skipped:" + skipped + "/" + queries_number + "(" + skipped * 100 / queries_number + "%)");
            logger.info("setted :" + setValue + "/" + queries_number + "(" + setValue * 100 / queries_number + "%)");
            logger.info("consult:" + output);

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }

            throw new SLIB_Exception(e);
        } finally {
            if (poolWorker != null) {
                poolWorker.forceShutdown();
            }
        }
    }

    private void perform_cTOc(QueryIterator qloader, String output) throws SLIB_Exception {


        logger.info("Starting computing query " + Sm_XML_Cst.QUERIES_TYPE_CTOC);

        ThreadManager threadManager = ThreadManager.getSingleton();
        PoolWorker poolWorker = null;

        try {


            long queries_number = qloader.getNumberQueries();
            logger.info("Number of query " + queries_number);



            FileWriter fstream = new FileWriter(output);
            BufferedWriter file = new BufferedWriter(fstream);

            // Build Header
            String header = Sm_XML_Cst.C1_ATTR + "\t" + Sm_XML_Cst.C1_ATTR;


            for (SMconf m : conf.gConfPairwise) {
                header += "\t" + m.label;
            }

            file.write(header + "\n");

            int skipped = 0;
            int setValue = 0;

            // Compute measure results

            poolWorker = threadManager.getMaxLoadPoolWorker();

            long count = 0;

            List<Future<ThreadResultsQueryLoader>> results = new ArrayList<Future<ThreadResultsQueryLoader>>();


            while (qloader.hasNext()) {

//                Thread.sleep(100);// To create thread wave
//				logger.debug("Await Free Ressource, load "+poolWorker.getLoad()+"/"+poolWorker.getCapacity());
                poolWorker.awaitFreeResource();

                List<QueryEntry> queriesBench = qloader.nextValids(SIZE_BENCH);

                ConceptToConcept_Thread callable = new ConceptToConcept_Thread(poolWorker, queriesBench, this);



                poolWorker.addTask();
//				logger.debug("- Adding Thread task "+poolWorker.getLoad()+"/"+poolWorker.getCapacity());
                Future<ThreadResultsQueryLoader> future = poolWorker.getPool().submit(callable);

                results.add(future);

                // process results
                Iterator<Future<ThreadResultsQueryLoader>> it = results.iterator();
                while (it.hasNext()) {

                    Future<ThreadResultsQueryLoader> r = it.next();

                    if (r.isDone()) {

                        ThreadResultsQueryLoader rez = r.get();

                        file.write(rez.buffer.toString());

                        skipped += rez.getSkipped();
                        setValue += rez.getSetValue();

                        count += rez.getJobSize();
                        logger.info("- " + count + " / " + queries_number + "\tskipped " + skipped + "\tsetted results " + setValue);

                        it.remove();
                    }
                }
            }
            qloader.close();

            poolWorker.shutdown();

            // process results
            Iterator<Future<ThreadResultsQueryLoader>> it = results.iterator();
            while (it.hasNext()) {

                Future<ThreadResultsQueryLoader> r = it.next();

                if (r.isDone()) {
                    ThreadResultsQueryLoader rez = r.get();

                    file.write(rez.buffer.toString());

                    skipped += rez.getSkipped();
                    setValue += rez.getSetValue();

                    count += rez.getJobSize();
                    logger.info("- " + count + " / " + queries_number + "\tskipped " + skipped + "\tsetted results " + setValue);

                    it.remove();
                }
            }

            file.close();
            logger.info("skipped:" + skipped + "/" + queries_number + "(" + skipped * 100 / queries_number + "%)");
            logger.info("setted :" + setValue + "/" + queries_number + "(" + setValue * 100 / queries_number + "%)");
            logger.info("consult:" + output);

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }

            throw new SLIB_Exception(e);
        } finally {
            if (poolWorker != null) {
                poolWorker.forceShutdown();
            }
        }
    }

    /**
     *
     * @param icID
     * @return
     */
    public ICconf getICconf(String icID) {

        for (ICconf g : conf.gConfICs) {

            if (g.getId().equals(icID)) {
                return g;
            }
        }
        return null;
    }

    private boolean requireDAG() {
        for (SMconf c : conf.gConfPairwise) {
            int mApproach = SMConstants.getPairwiseApproach(c.flag);

            if (SMConstants.requireDAG(mApproach)) {
                return true;
            }

        }
        return false;
    }

    /**
     *
     * @return
     */
    public G getGraph() {
        return graph;
    }

    /**
     *
     * @return
     */
    public InstancesAccessor getiAccessor() {
        return iAccessor;
    }
}
