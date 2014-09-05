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
package slib.tools.ontofocus.cli;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.indexer.IndexHash;
import slib.indexer.obo.IndexerOBO;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.conf.GraphConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.tools.module.CmdHandler;
import slib.tools.ontofocus.cli.utils.OntoFocusCmdHandlerCst;
import slib.tools.ontofocus.core.OntoFocus;
import slib.tools.ontofocus.core.utils.OntoFocusCst;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public final class OntoFocusCmdHandler extends CmdHandler {

    public String ontoFile = null;
    public String outprefix = null;
    public String queryFile = null;
    public String incR = null;
    public boolean addR = false;
    public boolean transitiveReductionClass = false;
    Set<URI> predicatesToAdd;
    public GFormat format = OntoFocusCmdHandlerCst.format_default;
    static Logger logger = LoggerFactory.getLogger(OntoFocusCmdHandler.class);
    static Pattern colon = Pattern.compile(":");
    private HashMap<String, String> uriPrefixes;

    @Override
    public String toString(){
        String out = "";
        out += "onto        : '"+ontoFile+"'\n";
        out += "onto format : '"+format+"'\n";
        out += "query file  : '"+queryFile+"'\n";
        out += "outprefix   : '"+outprefix+"'\n";
        out += "URI prefixes: '"+uriPrefixes+"'\n";
        out += "addR        : '"+addR+"'\n";
        out += "tr          : '"+transitiveReductionClass+"'\n";
        out += "incR        : '"+incR+"'\n";
        
        return out;
    }
    
    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    public OntoFocusCmdHandler(String[] args) throws SLIB_Exception {

        super(new OntoFocusCst(), new OntoFocusCmdHandlerCst());
        processArgs(args);
    }

    @Override
    public void processArgs(String[] args) throws SLIB_Ex_Critic {
        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (line.hasOption("help")) {
                ending(null, true);
            } else {

                if (line.hasOption("addR")) {
                    addR = true;
                }

                if (line.hasOption("tr")) {
                    transitiveReductionClass = true;
                }


                //-- Ontology file
                if (line.hasOption("onto")) {
                    ontoFile = line.getOptionValue("onto");
                } else {
                    ending(OntoFocusCmdHandlerCst.errorNoOntology, true);
                }

                //-- Output file prefix
                if (line.hasOption("outprefix")) {
                    outprefix = line.getOptionValue("outprefix");
                }

                //-- focus file
                if (line.hasOption("queries")) {
                    queryFile = line.getOptionValue("queries");
                } else {
                    ending(OntoFocusCmdHandlerCst.errorNoQueries, true);
                }


                //-- incR
                if (line.hasOption("incR")) {
                    incR = line.getOptionValue("incR");
                }

                //-- format
                if (line.hasOption("format")) {
                    String formatAsString = line.getOptionValue("format");
                    format = GFormat.valueOf(formatAsString);
                }

                //-- prefixes
                if (line.hasOption("prefixes")) { // expected value such as GO=http://graph1/,DO=http://graph2

                    uriPrefixes = new HashMap<String,String>();
                    String prefixesAsString = line.getOptionValue("prefixes");

                    String[] prefixesKeyValue = prefixesAsString.split(",");
                    for (String pKeyValue : prefixesKeyValue) {

                        String[] data = pKeyValue.split("=");

                        if (data.length != 2) {
                            throw new SLIB_Ex_Critic("Cannot load prefix expressed in '" + pKeyValue + "'");
                        }

                        String prefix = data[0];
                        String value = data[1];
                        
                        

                        boolean loaded = URIFactoryMemory.getSingleton().loadNamespacePrefix(prefix, value);
                        if(loaded){
                            uriPrefixes.put(prefix, value);
                        }
                    }
                }
            }

            //-- prefixes
            predicatesToAdd = new HashSet<URI>();
            if (line.hasOption("finclude")) { // GO:XXXXX,GO:XXXXXX they must be loaded after prefixes
                String fincludeAsString = line.getOptionValue("finclude");

                String[] fincludeAsStringTab = fincludeAsString.split(",");
                
                for (String uri : fincludeAsStringTab) {
                    predicatesToAdd.add(buildURIFromString(URIFactoryMemory.getSingleton(), uri));
                }
            }

        } catch (ParseException exp) {
            ending(OntoFocusCmdHandlerCst._appCmdName + " Parsing failed.  Reason: " + exp.getMessage(), true);
        }
        
        logger.debug(toString());
    }

    public static URI buildURIFromString(URIFactory factory, String string) throws SLIB_Ex_Critic {


        if (string.isEmpty()) {
            return null;
        }

        String data[] = colon.split(string, 2);
        data[0] = data[0].trim();

        if (data.length > 1) {
            data[1] = data[1].trim();
        }

        URI uri;

        if (data.length == 2 && factory.getNamespace(data[0]) != null) {

            String ns = factory.getNamespace(data[0]);
            if (ns == null) {
                throw new SLIB_Ex_Critic("No namespace associated to prefix " + data[0] + ". Cannot load " + string + ", please load required namespace prefix");
            }

            uri = factory.getURI(ns + data[1]);
        } else {
            uri = factory.getURI(string);
        }
        return uri;
    }

    public static void execCommandLine(String[] args) throws SLIB_Ex_Critic, SLIB_Exception, Exception {
        // Parse conlfiguration from the command line
        OntoFocusCmdHandler c = new OntoFocusCmdHandler(args);
        
        logger.info("Configuration:\n"+c.toString());

        // Load the graph and map some parameters to elements of the graph
        URIFactory uriFactory = URIFactoryMemory.getSingleton();
        //uriFactory.loadNamespacePrefix("GO", "http://go/");

        URI uriGraph = uriFactory.getURI("http://graph/");
        GraphConf gconf = new GraphConf(uriGraph);
        gconf.addGDataConf(new GDataConf(c.format, c.ontoFile));
        G graph = GraphLoaderGeneric.load(gconf);
        
        


        /*
         * Load the information related to the predicate to consider for the
         * reduction (those considered as taxonomic relationships), and the
         * predicates which composed the graph.
         */

        Set<URI> taxonomicPredicates = new HashSet<URI>(),
                relationshipsToAdd = new HashSet<URI>(),
                existingPredicate = new HashSet<URI>();

        taxonomicPredicates.add(RDFS.SUBCLASSOF);


        // load other relationships
        // retrieve all types of relationships
        for (E e : graph.getE()) {
            if (!existingPredicate.contains(e.getURI())) {
                existingPredicate.add(e.getURI());
            }
        }

        if (c.incR != null) {

            String[] uriS = c.incR.split(OntoFocusCmdHandlerCst.incR_Separator);

            for (String predicateAsString : uriS) {

                URI uriPredicate = buildURIFromString(uriFactory, predicateAsString);

                if (!existingPredicate.contains(uriPredicate)) {
                    String existingPredicateLog = "";
                    for (URI p : existingPredicate) {
                        existingPredicateLog += p + "\n";
                    }
                    throw new SLIB_Ex_Critic("Cannot resolve predicate: '" + predicateAsString + "'\n" + existingPredicateLog);
                }
                logger.debug("include relationship '" + uriPredicate + "'");
                taxonomicPredicates.add(uriPredicate);
            }
        }


        relationshipsToAdd.addAll(taxonomicPredicates);

        if (c.addR) {
            relationshipsToAdd.addAll(existingPredicate);
        }


        logger.info("Admitted predicate: " + taxonomicPredicates.size());
        for (URI p : taxonomicPredicates) {
            logger.info("\t- " + p);
        }


        // Load Index if required
        IndexHash index = null;
        if (c.format.equals(GFormat.OBO)) {
            logger.info("Loading index");
            index = new IndexerOBO().buildIndex(uriFactory, c.ontoFile, graph.getURI().stringValue());
        }


        // Create the ontofocus object
        OntoFocus p = new OntoFocus(uriFactory, graph, taxonomicPredicates, relationshipsToAdd, c.predicatesToAdd);
        p.execQueryFromFile(c.queryFile, c.outprefix,index,c.transitiveReductionClass,true);
    }

    public static void main(String[] args) {

        try {

            OntoFocusCmdHandler.execCommandLine(args);

        } catch (Exception e) {
            logger.info("Ooops: " + e.getMessage());
            e.printStackTrace();
            logger.info("Please report this error");
        }
    }
}
