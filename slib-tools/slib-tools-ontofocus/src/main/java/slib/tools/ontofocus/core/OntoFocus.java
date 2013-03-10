/*

 sCopyright or © or Copr. Ecole des Mines d'Alès (2012) 

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
package slib.tools.ontofocus.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.indexer.IndexHash;
import slib.indexer.obo.IndexerOBO;
import slib.sglib.algo.graph.reduction.dag.GraphReduction_DAG_Ranwez_2011;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.algo.graph.utils.RooterDAG;
import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.plotter.GraphPlotter_Graphviz;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.impl.graph.elements.Vertex;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.impl.voc.SLIBVOC;
import slib.sglib.model.repo.DataFactory;
import slib.tools.ontofocus.cli.utils.OntoFocusCmdHandlerCst;
import slib.tools.ontofocus.core.utils.OntoFocusConf;
import slib.tools.ontofocus.core.utils.QueryEntryURI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.ex.SLIB_Ex_Warning;
import slib.utils.impl.QueryEntry;
import slib.utils.impl.QueryFileIterator;

/**
 *
 * @author seb
 */
public class OntoFocus {

    Logger logger = LoggerFactory.getLogger(OntoFocus.class);
    DataFactory factory;
    G baseGraph;
    OntoFocusConf c;
    Set<URI> admittedRels;
    Set<URI> relationshipsToAdd;
    URI rootURI;
    private boolean showLabels = true; // TODO add to configuration parameters

    /**
     *
     */
    public OntoFocus() {
        factory = DataFactoryMemory.getSingleton();
    }

    /**
     *
     * @param c
     * @throws SLIB_Exception
     * @throws IOException
     */
    public void excecute(OntoFocusConf c) throws SLIB_Exception, IOException {

        this.c = c;

        // Load the Graph ------------------------------------------------------------
        URI uriGraph = factory.createURI("http://graph/");
        GraphConf conf = new GraphConf(uriGraph);
        conf.addGDataConf(new GDataConf(c.format, c.ontoFile));



        baseGraph = GraphLoaderGeneric.load(conf);



        logger.info(baseGraph.toString());

        IndexHash index = new IndexerOBO().buildIndex(factory, c.ontoFile, baseGraph.getURI().stringValue());

        root();
        loadEtypeConf(); // Load edge Types


        GraphReduction_DAG_Ranwez_2011 gRed = new GraphReduction_DAG_Ranwez_2011(factory, baseGraph, rootURI, admittedRels, relationshipsToAdd, true);

        

        GAction trClass = null;
        // Configuration of the transitive reduction if required
        if (c.transitiveReductionClass) {
            trClass = new GAction(GActionType.TRANSITIVE_REDUCTION);
            trClass.addParameter("target", "CLASSES");
        }
        
        // load query URI  & check query size -------------------

        QueryFileIterator qloader = new QueryFileIterator(c.queryFile);

        int i = 0;

        while (qloader.hasNext()) {

            QueryEntry e = qloader.next();

            if (e.isValid()) {
                QueryEntryURI query = loadQueryURI(e);

                if (query.isValid()) {

                    try {
                        i++;
                        logger.info("Reduction " + i + " " + query.getKey());

                        Set<V> queryAsV = new HashSet<V>();

                        for (URI u : query.getValue()) {
                            queryAsV.add(baseGraph.getV(u));
                        }

                        URI guri_reduction = factory.createURI(baseGraph.getURI() + "_reduction_" + i);
                        G graph_reduction = new GraphMemory(guri_reduction);
                        gRed.exec(query.getValue(), graph_reduction);

                        // Configuration of the transitive reduction if required
                        if (c.transitiveReductionClass) {
                            GraphActionExecutor.applyAction(factory, trClass, graph_reduction);
                        }

                        logger.info(graph_reduction.toString());

                        String gviz = GraphPlotter_Graphviz.plot(graph_reduction, queryAsV, showLabels, index);



                        if (c.out == null) {
                            logger.info(gviz);
                        } else {

                            String out = c.out + "_" + e.getKey() + ".dot";

                            flushResultOnFile(gviz, out);

                            logger.info("Consult result : " + out);
                            logger.info("Number of URI loaded : " + factory.getURIs().size());
                        }

                        //UtilDebug.exit(this);
                    } catch (Exception ex) {
                        System.err.println("Error processing entry " + e.getKey() + " : " + ex.getMessage());

                        if (!(ex instanceof SLIB_Ex_Warning)) {
                            ex.printStackTrace();
                            System.exit(-1);
                        }
                    }
                }
            }
        }
        qloader.close();
    }

    private void root() throws SLIB_Ex_Critic {

        if (c.rootURI == null) {
            rootURI = RooterDAG.rootUnderlyingTaxonomicDAG(baseGraph, SLIBVOC.THING_OWL);
        } else {
//			if(data.vTypes.containsLinkedURI(c.rootURI, baseGraph)){// TODO check URI exists
            rootURI = factory.createURI(c.rootURI);


            V root;

            if (baseGraph.containsVertex(rootURI)) {
                root = baseGraph.getV(rootURI);
            } else {
                root = new Vertex(rootURI, VType.CLASS);
            }

            if (!new ValidatorDAG().isUniqueRootedTaxonomicDag(baseGraph, root)) {
                logger.info("Graph reduction required - applying REROOTING action");
                GAction reroot = new GAction(GActionType.REROOTING);
                reroot.addParameter("root_uri", rootURI.stringValue());
                GraphActionExecutor.applyAction(factory, reroot, baseGraph);
            }
//			}
//			else
//				throw new SGL_Exception_Critical("Cannot locate "+c.rootURI+" in "+baseGraph.getURI());
        }
    }

    private void loadEtypeConf() throws SLIB_Ex_Critic {

        // load specialization relationship
        admittedRels = new HashSet<URI>();
        admittedRels.add(RDFS.SUBCLASSOF);

        // load other relationships

        if (c.incR != null) {

            String[] uriS = c.incR.split(OntoFocusCmdHandlerCst.incR_Separator);

            for (String s : uriS) {

                String uriRel = baseGraph.getURI() + s;

//				if(!data.containsLinkedValues( uriRel, baseGraph))					
//					logger.debug("[Edge Type NOT FOUND] exclude relationship '"+s+"'");
//				else{
                URI eType = factory.getPredicateFactory().createPURI(uriRel);
                logger.debug("include relationship '" + eType + "'");
                admittedRels.add(eType);
//				}
            }
        }

        // load direct relationship type to consider
        Set<URI> relationshipsToAdd = new HashSet<URI>();

        for (URI eType : admittedRels) {
            relationshipsToAdd.add(eType);
        }

        if (c.addR) {
            HashSet<URI> nativeEtypes = new HashSet<URI>();

            for (URI eType : factory.getPredicateFactory().getURIs()) {
//				if(eType.isNative()) // modified sesame-blueprints refactoring
                nativeEtypes.add(eType);
            }
            relationshipsToAdd.addAll(nativeEtypes);
        }

        logger.debug("" + admittedRels);

//		relationshipsToAdd = data.eTypes.getEtypeNative(baseGraph); // modified sesame-blueprints refactoring
    }

    private void flushResultOnFile(String gviz, String outfile) throws IOException {

        FileWriter fstream = new FileWriter(outfile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(gviz);
        out.close();

    }

    private QueryEntryURI loadQueryURI(QueryEntry queryEntry) throws SLIB_Ex_Critic {


        QueryEntryURI q = new QueryEntryURI();

        if (queryEntry != null) {

            Set<URI> uris = new HashSet<URI>();

            String[] annot = queryEntry.getValue().split(",");

            for (String a : annot) {
//				boolean containAnnot = data.containsLinkedValues(baseGraph.getURI()+a,baseGraph);
//				if(containAnnot)
                uris.add(factory.createURI(a));
            }

            q = new QueryEntryURI(queryEntry.getKey(), uris);
        }
        return q;
    }
}
