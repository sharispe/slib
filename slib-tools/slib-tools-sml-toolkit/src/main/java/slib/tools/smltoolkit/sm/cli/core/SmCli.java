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
package slib.tools.smltoolkit.sm.cli.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Future;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.extraction.rvf.instances.InstancesAccessor;
import slib.graph.algo.extraction.rvf.instances.impl.InstanceAccessor_RDF_TYPE;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.model.graph.G;
import slib.graph.model.impl.repo.GraphRepositoryMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.GraphRepository;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.tools.module.XmlTags;
import slib.tools.smltoolkit.SmlModuleCLI;
import slib.tools.smltoolkit.sm.cli.conf.xml.loader.Sm_XMLConfLoader;
import slib.tools.smltoolkit.sm.cli.conf.xml.utils.Sm_XML_Cst;
import slib.tools.smltoolkit.sm.cli.core.utils.ActionParamsUtils;
import slib.tools.smltoolkit.sm.cli.core.utils.ActionsParams;
import slib.tools.smltoolkit.sm.cli.core.utils.calc.ConceptToConcept_Thread;
import slib.tools.smltoolkit.sm.cli.core.utils.calc.EntityToEntity_Thread;
import slib.tools.smltoolkit.sm.cli.core.utils.FileWriterUtil;
import slib.tools.smltoolkit.sm.cli.core.utils.QueryConceptsIterator;
import slib.tools.smltoolkit.sm.cli.core.utils.SMQueryParam;
import slib.tools.smltoolkit.sm.cli.core.cmd.SmCmdHandler;
import slib.tools.smltoolkit.sm.cli.core.utils.calc.ThreadResultsQueryLoader;
import slib.utils.ex.SLIB_Ex_Critic;
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
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SmCli implements SmlModuleCLI {

    Logger logger = LoggerFactory.getLogger(SmCli.class);
    public Sm_XMLConfLoader conf;
    public SM_Engine simManager;
    InstancesAccessor iAccessor;
    URIFactory factory = URIFactoryMemory.getSingleton();
    G graph;
    // Default parameters
    private final ActionsParams NO_ANNOTATION_ACTION = ActionsParams.EXCLUDE;
    private final ActionsParams NOT_FOUND_ACTION = ActionsParams.STOP;
    private final double NO_ANNOTATION_SCORE = 0;
    private final double NOT_FOUND_SCORE = 0;
    public boolean QUIET = false;
    private final boolean OUTPUT_BASE_NAME = true;
    private int SIZE_BENCH = 5000;
    private boolean CACHE_PAIRWISE_RESULTS = false;

    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    @Override
    public void execute(String[] args) throws SLIB_Exception {
        
        SmCmdHandler c = new SmCmdHandler();
        
        c.processArgs(args);
        
        if (c.xmlConfFile != null) {
            execute(c.xmlConfFile);
        } else {
            
            String profileconf = System.getProperty("user.dir") + "/sml-xmlconf.xml";
            logger.info("Writing profile configuration to " + profileconf);
            FileWriterUtil.writeToFile(profileconf, c.xmlConfAsString);
            execute(profileconf);
            logger.info("A profile has been executed");
            logger.info("The XML configuration can be retrieved at " + profileconf);
        }
    }

    /**
     * Execute from configuration file
     *
     * @param confFile
     * @throws SLIB_Exception
     */
    public void execute(String confFile) throws SLIB_Exception {

        logger.info("---------------------------------------------------------------");
        logger.info(" Processing XML configuration " + confFile);
        logger.info("---------------------------------------------------------------");

        conf = new Sm_XMLConfLoader(confFile);


        // Load the graph and perform required graph treatments
        GraphLoaderGeneric.load(conf.generic.getGraphConfs());

        logger.info("Retrieving the graph " + conf.graphURI);

        URI graphURI = factory.getURI(conf.graphURI);

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


        logger.info("---------------------------------------------------------------");
        logger.info(" Global parameters");
        logger.info("---------------------------------------------------------------");
        logger.info("quiet      : " + QUIET);
        logger.info("Bench size : " + SIZE_BENCH);
        logger.info("Number of threads allowed: " + ThreadManager.getSingleton().getCapacity());
        logger.info("---------------------------------------------------------------");


        // check if the evaluated measure requires the graph to be a DAG
        if (requireDAG()) {
            logger.info("checking DAG property");
            ValidatorDAG dagVal = new ValidatorDAG();
            boolean rootedGraph = dagVal.containsTaxonomicDagWithUniqueRoot(graph);

            if (!rootedGraph) {
                logger.error("Multiple root detected please specify a root or use root=\"FICTIVE\"");
            }
        }

        iAccessor = new InstanceAccessor_RDF_TYPE(graph);

        computeQueries();

        logger.info("process done");
        logger.info("---------------------------------------------------------------");
    }

    private void computeQueries() throws SLIB_Exception {

        logger.info("---------------------------------------------------------------");
        logger.info("Start computation of " + conf.gConfQueries.size() + " queries");

        try {
            for (Conf gconf : conf.gConfQueries) {

                String id = (String) gconf.getParam(XmlTags.ID_ATTR);
                String type = (String) gconf.getParam(XmlTags.TYPE_ATTR);
                String infile = (String) gconf.getParam(XmlTags.FILE_ATTR);
                String output = (String) gconf.getParam(XmlTags.OUTPUT_ATTR);
                String uri_prefix = (String) gconf.getParam(XmlTags.URI_PREFIX_ATTR);
                String use_uri_prefix = (String) gconf.getParam(Sm_XML_Cst.USE_URI_PREFIX_ATTR);
                String use_uri_prefix_output = (String) gconf.getParam(Sm_XML_Cst.USE_URI_PREFIX_OUTPUT_ATTR);
                String noAnnotsConf_s = (String) gconf.getParam(Sm_XML_Cst.OPT_NO_ANNOTS_ATTR);
                String notFound_s = (String) gconf.getParam(Sm_XML_Cst.OPT_NOT_FOUND_ATTR);
                String outputBaseName_s = (String) gconf.getParam(Sm_XML_Cst.OUTPUT_BASENAME_ATT);

                boolean outputBasedName = OUTPUT_BASE_NAME;
                ActionsParams noAnnotAction = NO_ANNOTATION_ACTION;
                ActionsParams noFoundAction = NOT_FOUND_ACTION;
                double noAnnotationScore = NO_ANNOTATION_SCORE;
                double noFoundScore = NOT_FOUND_SCORE;
                boolean useLoadedURIprefixes = Util.stringToBoolean(use_uri_prefix);
                boolean useLoadedURIprefixesOutput = Util.stringToBoolean(use_uri_prefix_output);


                if (outputBaseName_s != null) {
                    outputBasedName = Util.stringToBoolean(outputBaseName_s);
                }

                if (notFound_s
                        != null) {
                    noFoundAction = ActionParamsUtils.getAction(notFound_s);
                    if (noFoundAction == ActionsParams.SET) {
                        noFoundScore = ActionParamsUtils.getSetValue(notFound_s);
                    }
                }
                if (noAnnotsConf_s
                        != null) {
                    noAnnotAction = ActionParamsUtils.getAction(noAnnotsConf_s);
                    if (noAnnotAction == ActionsParams.SET) {
                        noAnnotationScore = ActionParamsUtils.getSetValue(noAnnotsConf_s);
                    }
                }
                if (uri_prefix == null) {
                    uri_prefix = "";
                } else if (useLoadedURIprefixes) {
                    // conflict those two parameters cannot be used togethers
                    throw new SLIB_Ex_Critic("Error loading query " + id + ", parameters " + XmlTags.URI_PREFIX_ATTR + " and " + Sm_XML_Cst.USE_URI_PREFIX_ATTR + " cannot be used togethers. Consult documentation");
                }


                SMQueryParam queryParam = new SMQueryParam(id);
                queryParam.setNoAnnotAction(noAnnotAction)
                        .setNoAnnotationScore(noAnnotationScore)
                        .setNoFoundAction(noFoundAction)
                        .setNoFoundScore(noFoundScore)
                        .setOutputBaseName(outputBasedName)
                        .setInfile(infile)
                        .setOutfile(output)
                        .setType(type)
                        .setUseLoadedURIprefixes(useLoadedURIprefixes)
                        .setUseLoadedURIprefixesOutput(useLoadedURIprefixesOutput);


                logger.info(
                        "---------------------------------------------------------------");
                logger.info(
                        "Query :" + queryParam.getId());
                logger.info(
                        "---------------------------------------------------------------");
                logger.info(queryParam.toString());


                // require file
                if (type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC)
                        || type.equals(Sm_XML_Cst.QUERIES_TYPE_OTOO)) {

                    QueryIterator qloader = new QueryFileIterator(infile, uri_prefix);

                    if (type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC)) {
                        
                        perform_cTOc(qloader, queryParam);
                    } else if (type.equals(Sm_XML_Cst.QUERIES_TYPE_OTOO)) {
                        perform_oTOo(qloader, queryParam);
                    }
                } else if (type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC_FULL)) {

                    QueryIterator qloader = new QueryConceptsIterator(simManager.getClasses());

                    perform_cTOc(qloader, queryParam);

                } else {
                    throw new UnsupportedOperationException(type + " is not a supported " + XmlTags.TYPE_ATTR + " of queries");
                }
            }
        } catch (IOException e) {
            throw new SLIB_Exception(e);
        } catch (SLIB_Exception e) {
            throw new SLIB_Exception(e);
        } catch (UnsupportedOperationException e) {
            throw new SLIB_Exception(e);
        }
    }

    private void perform_oTOo(QueryIterator qloader, SMQueryParam queryParam) throws SLIB_Exception {


        logger.info("Starting computing query " + Sm_XML_Cst.QUERIES_TYPE_OTOO);

        ThreadManager threadManager = ThreadManager.getSingleton();
        PoolWorker poolWorker = null;

        try {


            long queryNumber = qloader.getNumberQueries();
            logger.info("Number of query ~" + queryNumber);

            long queryNumberLogStep = queryNumber / 10; // we log 10 times
            int nbLogStep = 0;

            FileWriter fstream = new FileWriter(queryParam.getOutfile());
            BufferedWriter file = new BufferedWriter(fstream);

            // Build Header
            String header = Sm_XML_Cst.E1_ATTR + "\t" + Sm_XML_Cst.E2_ATTR;

            int nbMeasures = conf.gConfGroupwise.size();

            for (SMconf m : conf.gConfGroupwise) {
                header += "\t" + m.getLabel();
            }

            file.write(header + "\n");

            int skipped = 0;
            int setValue = 0;

            // Compute measure results

            poolWorker = threadManager.getMaxLoadPoolWorker();

            long count = 0;

            List<Future<ThreadResultsQueryLoader>> results = new ArrayList<Future<ThreadResultsQueryLoader>>();

            logger.info("processing queries...");

            while (qloader.hasNext()) {

//                Thread.sleep(100);// To create thread wave
//                logger.debug("Await Free Resource, load " + poolWorker.getLoad() + "/" + poolWorker.getCapacity());
                poolWorker.awaitFreeResource();

                List<QueryEntry> queriesBench = qloader.nextValids(SIZE_BENCH);

                EntityToEntity_Thread callable = new EntityToEntity_Thread(poolWorker, queriesBench, this, nbMeasures, queryParam);

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
                        if (!QUIET && count > nbLogStep * queryNumberLogStep) {
                            logger.info("- " + count + " / ~" + queryNumber + "\tskipped " + skipped + "\tsetted results " + setValue);
                            nbLogStep++;
                        }

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
                    if (!QUIET) {
                        logger.info("- " + count + " / ~" + queryNumber + "\tskipped " + skipped + "\tsetted results " + setValue);
                    }

                    it.remove();
                }
            }

            file.close();
            logger.info(count+" queries considered");
            logger.info("skipped:" + skipped + "/" + count + "(" + skipped * 100 / count + "%)");
            logger.info("setted :" + setValue + "/" + count + "(" + setValue * 100 / count + "%)");
            logger.info("consult:" + queryParam.getOutfile());

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

    private void perform_cTOc(QueryIterator qloader, SMQueryParam queryParam) throws SLIB_Exception {


        logger.info("Starting computing query " + Sm_XML_Cst.QUERIES_TYPE_CTOC);

        ThreadManager threadManager = ThreadManager.getSingleton();
        PoolWorker poolWorker = null;

        try {


            long queryNumber = qloader.getNumberQueries();
            logger.info("Number of query ~" + queryNumber);

            long queryNumberLogStep = queryNumber / 10; // we log 10 times
            int nbLogStep = 0;



            FileWriter fstream = new FileWriter(queryParam.getOutfile());
            BufferedWriter file = new BufferedWriter(fstream);

            // Build Header
            String header = Sm_XML_Cst.C1_ATTR + "\t" + Sm_XML_Cst.C2_ATTR;


            for (SMconf m : conf.gConfPairwise) {
                header += "\t" + m.getLabel();
            }

            file.write(header + "\n");

            int skipped = 0;
            int setValue = 0;

            // Compute measure results

            poolWorker = threadManager.getMaxLoadPoolWorker();

            long count = 0;

            List<Future<ThreadResultsQueryLoader>> results = new ArrayList<Future<ThreadResultsQueryLoader>>();

            logger.info("processing queries...");

            while (qloader.hasNext()) {

//                Thread.sleep(100);// To create thread wave
//				logger.debug("Await Free Ressource, load "+poolWorker.getLoad()+"/"+poolWorker.getCapacity());
                poolWorker.awaitFreeResource();

                List<QueryEntry> queriesBench = qloader.nextValids(SIZE_BENCH);

                ConceptToConcept_Thread callable = new ConceptToConcept_Thread(poolWorker, queriesBench, this, queryParam);



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
                        if (!QUIET && count > nbLogStep * queryNumberLogStep) {
                            logger.info("- " + count + " / ~" + queryNumber + "\tskipped " + skipped + "\tsetted results " + setValue);
                            nbLogStep++;
                        }
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
                    if (!QUIET) {
                        logger.info("- " + count + " / ~" + queryNumber + "\tskipped " + skipped + "\tsetted results " + setValue);
                    }
                    it.remove();
                }
            }

            file.close();
            logger.info(count+" queries considered");
            logger.info("skipped:" + skipped + "/" + count + " (" + skipped * 100 / count + "%)");
            logger.info("setted :" + setValue + "/" + count + " (" + setValue * 100 / count + "%)");
            logger.info("consult:" + queryParam.getOutfile());

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
     * @return the IC configuration associated to a given ID.
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
            int mApproach = SMConstants.getPairwiseApproach(c.getFlag());

            if (SMConstants.requireDAG(mApproach)) {
                return true;
            }

        }
        return false;
    }

    /**
     *
     * @return the graph
     */
    public G getGraph() {
        return graph;
    }

    /**
     *
     * @return the object used to retrieve the instances.
     */
    public InstancesAccessor getiAccessor() {
        return iAccessor;
    }
}
