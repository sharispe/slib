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
package slib.tools.ontofocus.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.indexer.IndexHash;
import slib.sglib.algo.graph.reduction.dag.GraphReduction_DAG_Ranwez_2011;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.io.plotter.GraphPlotter_Graphviz;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sglib.model.voc.SLIBVOC;
import slib.sglib.utils.WalkConstraintGeneric;
import slib.tools.ontofocus.core.utils.QueryEntryURI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.ex.SLIB_Ex_Warning;
import slib.utils.impl.QueryEntry;
import slib.utils.impl.QueryFileIterator;
import slib.utils.impl.SetUtils;

/**
 * @author Harispe Sébastien
 */
public class OntoFocus {

    Logger logger = LoggerFactory.getLogger(OntoFocus.class);
    URIFactory factory;
    G graph;
    Set<URI> taxonomicPredicates; // predicate which will be considered to extract the 'taxonomy' , e.g. rdfs:subclassof
    Set<URI> relationshipsToAdd; // predicate of the relationships to add at post-processing
    URI rootURI; // the URI of the root
    boolean isVirtualRoot; // virtual roots are removed after the reduction
    private boolean showLabels = true;
    IndexHash index;
    boolean applyTR = false;
    GraphReduction_DAG_Ranwez_2011 gRed;
    GAction trClass;
    Set<URI> urisToInclude = new HashSet<URI>();; // URIs which must be in all reductions

    public OntoFocus(URIFactory factory, G graph, Set<URI> taxonomicPredicates, Set<URI> relationshipsToAdd, IndexHash index, boolean applyTR) throws SLIB_Ex_Critic, SLIB_Exception{
        this(factory, graph, taxonomicPredicates, relationshipsToAdd, index, applyTR, null);
    }
            
    public OntoFocus(URIFactory factory, G graph, Set<URI> taxonomicPredicates, Set<URI> relationshipsToAdd, IndexHash index, boolean applyTR, Set<URI> urisToInclude) throws SLIB_Ex_Critic, SLIB_Exception {

        this.factory = factory;
        this.graph = graph;
        this.taxonomicPredicates = taxonomicPredicates;
        this.relationshipsToAdd = relationshipsToAdd;
        this.index = index;
        this.applyTR = applyTR;
        
        if(urisToInclude != null){
            this.urisToInclude.addAll(urisToInclude);
        }

        rootIfRequired();


        trClass = new GAction(GActionType.TRANSITIVE_REDUCTION);
        trClass.addParameter("target", "CLASSES");

        gRed = new GraphReduction_DAG_Ranwez_2011(graph, taxonomicPredicates, relationshipsToAdd, true);
    }

    /**
     * Detect the root(s) of the graph according to the taxonomic predicate
     * considered, throw an error if any found and create a virtual root if
     * multiple roots have been detected.
     *
     * @throws SLIB_Ex_Critic
     */
    private void rootIfRequired() throws SLIB_Ex_Critic {

        ValidatorDAG validator = new ValidatorDAG();

        Map<URI, Direction> walkRulesTaxonomy = new HashMap<URI, Direction>();
        for (URI p : taxonomicPredicates) {
            // we consider that all specified predicates must be traversed using dir.out
            walkRulesTaxonomy.put(p, Direction.OUT);
        }

        Set<URI> roots = validator.getDAGRoots(graph, new WalkConstraintGeneric(walkRulesTaxonomy));


        if (roots.isEmpty()) {
            throw new SLIB_Ex_Critic("Cannot detect a root, i.e. a node without outgoing taxonomical relationships... Are you sure the graph if acyclic considering the specified taxonomic relationship(s)?");
        } else if (roots.size() == 1) {
            rootURI = roots.iterator().next();
            isVirtualRoot = false;
        } else {

            rootURI = SLIBVOC.THING_OWL;
            int attempt = 0;
            while (graph.containsVertex(rootURI)) {
                if (attempt == 1000) {
                    throw new SLIB_Ex_Critic("Cannot generate a root URI");
                }// highly improbable
                rootURI = factory.createURI(SLIBVOC.THING_OWL.toString() + "_" + ((int) Math.random() * 100000));
            }
            graph.addV(rootURI);
            isVirtualRoot = true;

            for (URI r : roots) { // we define the new root
                graph.addE(r, RDFS.SUBCLASSOF, rootURI);
            }
            logger.info("Original roots: " + roots);
            logger.info("virtual root created. (" + rootURI + ")");
        }
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
                uris.add(factory.createURI(a.trim(), true));
            }

            q = new QueryEntryURI(queryEntry.getKey(), uris);
        }
        return q;
    }

    public void execQueryFromFile(String queryFile, String outPrefix) throws Exception {

        QueryFileIterator qloader = new QueryFileIterator(queryFile);
        int i = 0;
        QueryEntry e;
        QueryEntryURI query;

        while (qloader.hasNext()) {

            e = qloader.next();

            if (e.isValid()) {

                query = loadQueryURI(e);



                if (query.isValid()) {

                    Set<URI> urisQuery = query.getValue();

                    if (!urisToInclude.isEmpty()) {
                        urisQuery.addAll(urisToInclude);
                    }


                    try {
                        i++;
                        logger.info("Reduction " + i + " " + query.getKey());

                        URI guri_reduction = factory.createURI(graph.getURI() + "_reduction_" + i);
                        G graph_reduction = performReduction(guri_reduction, urisQuery);

                        logger.info(graph_reduction.toString());

                        // We remove the uris which have been artificially added to the query
                        Set<URI> nurisQuery = urisQuery;
                        
                        if (!urisToInclude.isEmpty()) {
                            nurisQuery = new HashSet<URI>(urisQuery);
                            nurisQuery.removeAll(urisToInclude);
                            nurisQuery.addAll(SetUtils.intersection(urisToInclude, urisQuery));
                        }
                        String gviz = GraphPlotter_Graphviz.plot(graph_reduction, urisQuery, showLabels, false, index);

                        String out = e.getKey() + ".dot";

                        if (outPrefix != null) {
                            out = outPrefix + "_" + out;
                        }

                        flushResultOnFile(gviz, out);
                        logger.debug(gviz);
                        logger.info("Consult result : " + out);

                    } catch (Exception ex) {
                        System.err.println("Error processing entry " + e.getKey() + " : " + ex.getMessage());

                        if (!(ex instanceof SLIB_Ex_Warning)) {
                            throw ex;
                        }
                    }
                }
            }
        }
        qloader.close();
    }

    public G performReduction(URI guri_reduction, Set<URI> urisQuery) throws SLIB_Ex_Critic, SLIB_Ex_Warning {

        G graph_reduction = new GraphMemory(guri_reduction);
        gRed.exec(urisQuery, graph_reduction);

        logger.debug("Apply transitive reduction: " + applyTR);

        if (applyTR) {
            GraphActionExecutor.applyAction(factory, trClass, graph_reduction);
        }
        if (isVirtualRoot) {
            graph_reduction.removeV(rootURI);
        }

        return graph_reduction;


    }
}
