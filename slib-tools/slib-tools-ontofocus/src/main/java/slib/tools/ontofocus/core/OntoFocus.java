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
package slib.tools.ontofocus.core;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.indexer.IndexHash;
import slib.graph.algo.reduction.dag.GraphReduction_DAG_Ranwez_2011;
import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.io.plotter.GraphPlotter_Graphviz;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.repo.URIFactory;
import slib.tools.ontofocus.core.utils.QueryEntryURI;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.ex.SLIB_Ex_Warning;
import slib.utils.impl.QueryEntry;
import slib.utils.impl.QueryFileIterator;
import slib.utils.impl.SetUtils;

/**
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class OntoFocus {

    Logger logger = LoggerFactory.getLogger(OntoFocus.class);
    URIFactory factory;
    G graph;
    Set<URI> taxonomicPredicates; // predicate which will be considered to extract the 'taxonomy' , e.g. rdfs:subclassof
    Set<URI> predicatesToAdd; // predicates of the relationships to add at post-processing, i.e., all relationships of the specified types between pairs of nodes contained in the Cartesian product of the set of nodes which compose the reduction will be added
    GraphReduction_DAG_Ranwez_2011 gRed;
    GAction trClass;
    Set<URI> urisToInclude = new HashSet<URI>();

    /**
     *
     * @param factory The factory used to generate the URIs
     * @param graph The graph in which the reduction must be performed
     * @param taxonomicPredicates the predicates to consider as taxonomic
     * relationships
     * @param predicateToAdd predicates of the relationships to add at
     * post-processing, i.e., all relationships of the specified types which are
     * specified between pairs of nodes contained in the Cartesian product of
     * the set of nodes which compose the reduction will be added
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Exception
     */
    public OntoFocus(URIFactory factory, G graph, Set<URI> taxonomicPredicates, Set<URI> predicateToAdd) throws SLIB_Ex_Critic, SLIB_Exception {
        this(factory, graph, taxonomicPredicates, predicateToAdd, null);
    }

    public OntoFocus(URIFactory factory, G graph, Set<URI> taxonomicPredicates, Set<URI> predicatesToAdd, Set<URI> urisToInclude) throws SLIB_Ex_Critic, SLIB_Exception {

        this.factory = factory;
        this.graph = graph;
        this.taxonomicPredicates = taxonomicPredicates;
        this.predicatesToAdd = predicatesToAdd;

        if (urisToInclude != null) {
            this.urisToInclude.addAll(urisToInclude);
        }

//        rootIfRequired();


        trClass = new GAction(GActionType.TRANSITIVE_REDUCTION);
        trClass.addParameter("target", "CLASSES");

        logger.debug("Taxonomic Predicates: " + this.taxonomicPredicates);
        logger.debug("Predicates to add: " + this.predicatesToAdd);
        logger.debug("URIs to include (force): " + this.urisToInclude);
        gRed = new GraphReduction_DAG_Ranwez_2011(graph, taxonomicPredicates, predicatesToAdd, true);
    }

    private void flushResultOnFile(String gviz, String outfile) throws IOException {

        FileWriter fstream = new FileWriter(outfile);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(gviz);
        out.close();

    }

    private QueryEntryURI loadQueryURI(G graph, QueryEntry queryEntry) throws SLIB_Ex_Critic {


        QueryEntryURI q = new QueryEntryURI();

        if (queryEntry != null) {

            Set<URI> uris = new HashSet<URI>();

            String[] annot = queryEntry.getValue().split(",");

            for (String a : annot) {
                URI u = factory.getURI(a.trim(), true);
                if (graph.containsVertex(u)) {
                    uris.add(u);
                } else {
                    logger.info("!!! URI " + u + " cannot be found in the graph");
                }
            }

            q = new QueryEntryURI(queryEntry.getKey(), uris);
        }
        return q;
    }

    public void execQueryFromFile(String queryFile, String outPrefix, IndexHash index, boolean applyTR, boolean showLabels) throws Exception {

        logger.debug("Query file: " + queryFile);
        logger.debug("output prefix: '" + outPrefix + "'");
        QueryFileIterator qloader = new QueryFileIterator(queryFile);
        int i = 0;
        QueryEntry e;
        QueryEntryURI query;
        
        int entries_skipped = 0;
        int entries_performed = 0;
        int entries_total = 0;

        while (qloader.hasNext()) {

            e = qloader.next();

            if (e != null && e.isValid()) {
                
                entries_total++;

                query = loadQueryURI(graph, e);

                if (query.isValid()) {

                    i++;
                    logger.info("Reduction " + i + " " + query.getKey() + "\tnb:" + query.getValue().size());

                    Set<URI> urisQuery = query.getValue();

                    try {

                        if (query.getValue().size() < 2) {
                            entries_skipped++;
                            throw new SLIB_Ex_Warning("!!! skipped, size of annotation set < 2 ... size="+query.getValue().size());
                        }


                        URI guri_reduction = factory.getURI(graph.getURI() + "_reduction_" + i);
                        G graph_reduction = performReduction(guri_reduction, urisQuery, applyTR);

                        logger.info(graph_reduction.toString());

                        // We remove the uris which have been artificially added to the query
                        Set<URI> nurisQuery = new HashSet<URI>(urisQuery);

                        if (!urisToInclude.isEmpty()) {
                            nurisQuery.removeAll(urisToInclude);
                            nurisQuery.addAll(SetUtils.intersection(urisToInclude, urisQuery));
                        }
                        String gviz = GraphPlotter_Graphviz.plot(factory, graph_reduction, nurisQuery, showLabels, false, index);

                        String out = e.getKey() + ".dot";

                        if (outPrefix != null) {
                            out = outPrefix + "_" + out;
                        }

                        flushResultOnFile(gviz, out);
                        logger.debug(gviz);
                        logger.info("Consult result : " + out);
                        
                        entries_performed++;

                    } catch (Exception ex) {
                        System.err.println("Error processing entry " + e.getKey() + " : " + ex.getMessage());

                        if (!(ex instanceof SLIB_Ex_Warning)) {
                            throw ex;
                        }
                    }
                }
            }
        }
        logger.info("skipped   : "+entries_skipped+"/"+entries_total);
        logger.info("performed : "+entries_performed+"/"+entries_total);
        qloader.close();
    }

    public G performReduction(URI guri_reduction, Set<URI> urisQuery, boolean applyTR) throws SLIB_Ex_Critic, SLIB_Ex_Warning {

        G graph_reduction = new GraphMemory(guri_reduction);
        Set<URI> queries = new HashSet<URI>(urisQuery);
        if (!urisToInclude.isEmpty()) {
            queries.addAll(urisToInclude);
        }

        gRed.exec(queries, graph_reduction);

        logger.debug("Apply transitive reduction: " + applyTR);

        if (applyTR) {
            GraphActionExecutor.applyAction(factory, trClass, graph_reduction);
        }

        return graph_reduction;


    }
}
