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
package slib.graph.io.loader.bio.gaf2;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.io.loader.utils.filter.graph.Filter;
import slib.graph.io.loader.utils.filter.graph.gaf2.FilterGraph_GAF2;
import slib.graph.io.loader.utils.filter.graph.gaf2.FilterGraph_GAF2_cst;
import slib.graph.io.loader.utils.filter.graph.repo.FilterRepository;
import slib.graph.model.graph.G;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.BigFileReader;

/**
 * GAF 2 parser used to construct a graph from a GAF 2.0 annotation file
 *
 * The parser consider considering GAF 2.0 specification provided at
 * http://wiki.geneontology.org/index.php/GO_Consortium_web_GAF2.0_documentation
 * http://www.geneontology.org/GO.format.gaf-2_0.shtml
 *
 * TODO - Manage multiple organism specification : taxon:1|taxon:1000
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphLoader_GAF_2 implements GraphLoader {

    public final static int DB = 0;
    public final static int DB_OBJECT_ID = 1;
    public final static int DB_OBJECT_SYMBOL = 2;
    public final static int QUALIFIER = 3;
    public final static int GOID = 4;
    public final static int REFERENCE = 5;
    public final static int EVIDENCE_CODE = 6;
    public final static int WITH = 7;
    public final static int ASPECT = 8;
    public final static int DB_OBJECT_NAME = 9;
    public final static int DB_OBJECT_SYNONYM = 10;
    public final static int DB_OBJECT_TYPE = 11;
    public final static int TAXON = 12;
    public final static int DATE = 13;
    public final static int ASSIGNED_BY = 14;
    public final static int ANNOTATION_XP = 15;
    public final static int GENE_PRODUCT_ISOFORM = 16;
    private G graph;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    URIFactoryMemory factory = URIFactoryMemory.getSingleton();
    String prefixUriInstance;
    String defaultURIprefix;
    Pattern colon = Pattern.compile(":");

    /**
     * Method used to load an annotation repository considering a specific
     * configuration and a potential mapping restriction i.e. a {@link G}
     * containing the concepts to consider. A {@link FilterGraph_GAF2} can be
     * associated to a configuration in order to define restrictions to consider
     * during the parsing (e.g. taxons, Evidence Code, origin knowledge base)
     *
     * @param conf object defining a configuration. If the configuration file
     * define a {@link Filter} {@link FilterGraph_GAF2}, it will be evaluated
     * during the parsing.
     * @param graph a graph defining the concepts to consider, can be set to
     * null if no mapping restriction have to be take into account If a graph is
     * specified only annotation corresponding a graph Node will be loaded.
     * @throws SLIB_Ex_Critic
     *
     */
    @Override
    public void populate(GDataConf conf, G graph) throws SLIB_Ex_Critic {

        logger.info("-------------------------------------");
        logger.info("Loading data using GAF2 loader.");
        logger.info("-------------------------------------");

        if (graph == null) {
            throw new SLIB_Ex_Critic("Cannot process Null Graph");
        }

        logger.info("GAF 2 loader populates graph " + graph.getURI());
        this.graph = graph;

        process(conf);
        logger.info("-------------------------------------");
    }

    private void process(GDataConf conf) throws SLIB_Ex_Critic {

        prefixUriInstance = (String) conf.getParameter("prefix");
        if (prefixUriInstance == null) {
            prefixUriInstance = graph.getURI().getNamespace();
        }

        logger.info("Instance URIs will be prefixed by: " + prefixUriInstance);

        defaultURIprefix = prefixUriInstance;
        logger.info("Default URI prefix is set to: " + prefixUriInstance);

        Set<Filter> filters = new HashSet<Filter>();

        String filtersAsStrings = (String) conf.getParameter("filters");

        if (filtersAsStrings != null) {
            String[] filterNames = filtersAsStrings.split(",");

            FilterRepository filtersRepo = FilterRepository.getInstance();

            for (String fname : filterNames) {
                Filter f = filtersRepo.getFilter(fname);
                if (f == null) {
                    throw new SLIB_Ex_Critic("Cannot locate filter associated to id " + fname);
                }
                filters.add(f);
            }
        }

        FilterGraph_GAF2 filter = null;
        Set<String> taxons = null;
        Set<String> excludedEC = null;

        if (!filters.isEmpty()) {

            for (Filter f : filters) {

                if (f instanceof FilterGraph_GAF2) {

                    if (filter != null) {
                        throw new SLIB_Ex_Critic("Two filters " + FilterGraph_GAF2_cst.TYPE + " have been specified. Only one admitted");
                    } else {

                        filter = (FilterGraph_GAF2) f;
                        logger.info("Filtering according to filter " + filter.getId() + "\ttype" + filter.getType());

                        taxons = filter.getTaxons();
                        excludedEC = filter.getExcludedEC();
                    }
                }
            }
        }

        Pattern p_tab = Pattern.compile("\t");
        Pattern p_taxid = null;

        String fileLocation = conf.getLoc();

        if (taxons != null) {
            p_taxid = Pattern.compile(".?taxon:(\\d+).?");
        }

        int countEntities = 0;
        int countAnnotsLoaded = 0;

        logger.info("file location : " + fileLocation);

        int existsQualifier = 0; // a qualifier exists for the annotation
        int not_found = 0; // the annotation is not found on the loaded graph
        int eC_restriction = 0; // excluded due to evidence code restriction
        int taxonsRestriction = 0;

        logger.info("Loading...");

        URIFactory uriManager = URIFactoryMemory.getSingleton();

        boolean validHeader = false;
        String line, qualifier, gotermURIstring, evidenceCode, taxon_ids;
        int c = 0;

        try {

            BigFileReader file = new BigFileReader(fileLocation);
            String[] data;
            URI uriGOterm, entityID;

            while (file.hasNext()) {

                line = file.nextTrimmed();

                if (line.startsWith("!")) {

                    data = line.split(":");

                    if (data.length == 2) {
                        String flag = data[0].trim().substring(1);
                        String version = data[1].trim();

                        if (flag.equals("gaf-version") && (version.equals("2") || version.equals("2.0"))) {
                            validHeader = true;
                        }
                    }
                } else if (validHeader) {

                    data = p_tab.split(line);

                    entityID = uriManager.getURI(prefixUriInstance + data[DB_OBJECT_ID]);
                    gotermURIstring = buildURI(data[GOID]);
                    qualifier = data[QUALIFIER];
                    evidenceCode = data[EVIDENCE_CODE];
                    taxon_ids = data[TAXON];

                    // check if Evidence Code is valid
                    if (excludedEC == null || EvidenceCodeRules.areValid(excludedEC, evidenceCode)) {

                        // We do not consider go term associated with a qualifier 
                        // e.g. NOT, contributes_to ...
                        // TODO take into consideration this information !
                        if (qualifier.isEmpty()) {

                            uriGOterm = uriManager.getURI(gotermURIstring);

                            if (graph.containsVertex(uriGOterm)) { // if the annotation is in the graph

                                boolean valid = true;

                                if (p_taxid != null) {

                                    Matcher m = p_taxid.matcher(taxon_ids);
                                    valid = false;

                                    while (m.find() && !valid) {

                                        if (taxons != null && taxons.contains(m.group(1))) {
                                            valid = true;
                                        }
                                    }
                                }
                                if (valid) {

                                    if (!graph.containsVertex(entityID)) {
                                        graph.addV(entityID);
                                        countEntities++;
                                    }
                                    graph.addE(entityID, RDF.TYPE, uriGOterm);
                                    countAnnotsLoaded++;
                                } else {
                                    taxonsRestriction++;
                                }
                            } else {
                                not_found++;
                                logger.debug("Cannot found GO term " + uriGOterm);
                            }
                        } else {
                            existsQualifier++;
                        }
                    } else {
                        eC_restriction++;
                    }
                }
                c++;

                if (c % 1000000 == 0) {
                    logger.info(c + " GAF entries processed");
                }
            }
            file.close();

        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e);
        }

        if (!validHeader) {
            throw new SLIB_Ex_Critic("Invalid header for GAF-2 file " + fileLocation + "\nExpecting \"!gaf-version: 2.0\" as first line");
        }

        logger.info("\tExcluded  - Taxons restriction         : " + taxonsRestriction);
        logger.info("\tExcluded  - Evidence Code restriction  : " + eC_restriction);
        logger.info("\tExcluded  - Contains qualifier 	      : " + existsQualifier);
        logger.info("\tNot found unexisting term in the graph :	" + not_found);

        logger.info("Number of Instance loaded 	  	: " + countEntities);
        logger.info("Number of Annotation loaded 	: " + countAnnotsLoaded);
        logger.info("GAF2 Loader done.");
    }

    private String buildURI(String value) throws SLIB_Ex_Critic {

        String info[] = getDataColonSplit(value);

        if (info != null && info.length == 2) {

            String ns = factory.getNamespace(info[0]);
            if (ns == null) {
                throw new SLIB_Ex_Critic("No namespace associated to prefix " + info[0] + ". Cannot load " + value + ", please load required namespace prefix");
            }

            return ns + info[1];
        } else {
            return defaultURIprefix + value;
        }
    }

    private String[] getDataColonSplit(String line) {

        if (line.isEmpty()) {
            return null;
        }

        String data[] = colon.split(line);
        data[0] = data[0].trim();

        if (data.length > 1) {
            data[1] = data[1].trim();
        }
        return data;
    }
}
