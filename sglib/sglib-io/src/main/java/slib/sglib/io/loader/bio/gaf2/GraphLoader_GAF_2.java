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
package slib.sglib.io.loader.bio.gaf2;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.GraphLoader;
import slib.sglib.io.loader.utils.filter.graph.Filter;
import slib.sglib.io.loader.utils.filter.graph.FilterGraph;
import slib.sglib.io.loader.utils.filter.graph.gaf2.FilterGraph_GAF2;
import slib.sglib.io.loader.utils.filter.graph.gaf2.FilterGraph_GAF2_cst;
import slib.sglib.io.loader.utils.filter.graph.repo.FilterRepository;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.impl.EdgeTyped;
import slib.sglib.model.graph.elements.impl.VertexTyped;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.repo.impl.DataFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.BigFileReader;

/**
 * GAF 2 parser used to construct a graph from a GAF 2.0
 * annotation file
 *
 * The parser consider considering GAF 2.0 specification provided at
 * http://wiki.geneontology.org/index.php/GO_Consortium_web_GAF2.0_documentation
 * http://www.geneontology.org/GO.format.gaf-2_0.shtml
 *
 * TODO - Manage multiple organism specification : taxon:1|taxon:1000
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
    private G g;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    DataFactoryMemory data = DataFactoryMemory.getSingleton();
    String gNS;
    Pattern colon = Pattern.compile(":");

    @Override
    public G load(GraphConf conf) throws SLIB_Exception {

        return GraphLoaderGeneric.load(conf);
    }

    /**
     * Method used to load an annotation repository considering a specific
     * configuration and a potential mapping restriction i.e. a {@link G}
     * containing the concepts to consider. A {@link FilterKB_GAF2} can be
     * associated to the {@link KBConf} / {@link KBConf_GAF2} object in order to
     * define restrictions to consider during the parsing (e.g. taxons, Evidence
     * Code, origin knowledge base)
     *
     * @param conf a {@link KBConf} object defining a configuration. If the
     * configuration file define a {@link Filter} of class
     * {@link FilterKB_GAF2}, it will be evaluated during the parsing.
     * @param graph a graph defining the concepts to consider, can be set to
     * null if no mapping restriction have to be take into account If a graph is
     * specified only annotation corresponding a graph Node will be loaded.
     *
     * @return a knowledge base
     *
     * @throws SGL_Ex_Critic
     */
    @Override
    public void populate(GDataConf conf, G graph) throws SLIB_Ex_Critic {

        if (graph == null) {
            throw new SLIB_Ex_Critic("Cannot process Null Graph");
        }

        logger.info("Populate graph " + graph.getURI());
        this.g = graph;

        process(conf);
    }

    private void process(GDataConf conf) throws SLIB_Ex_Critic {


        gNS = g.getURI().getNamespace();


        Set<FilterGraph> filters = new HashSet<FilterGraph>();

        String filtersAsStrings = (String) conf.getParameter("filters");

        if (filtersAsStrings != null) {
            String[] filterNames = filtersAsStrings.split(",");
            FilterRepository filtersRepo = FilterRepository.getInstance();

            for (String fname : filterNames) {
                filters.add(filtersRepo.getFilter(fname));
            }
        }


        FilterGraph_GAF2 filter = null;
        HashSet<String> taxons = null;
        HashSet<String> excludedEC = null;

        if (filters != null) {

            for (FilterGraph f : filters) {

                if (f instanceof FilterGraph_GAF2) {

                    if (filter != null) {
                        throw new SLIB_Ex_Critic("Two filters " + FilterGraph_GAF2_cst.TYPE + " have been specified. Only one admitted");
                    } else {
                        filter = (FilterGraph_GAF2) f;
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


        HashMap<URI, HashSet< V>> entitiesAnnots;
        entitiesAnnots = new HashMap<URI, HashSet< V>>();


        logger.info("file location : " + fileLocation);





        int existsQualifier = 0; // a qualifier exists for the annotation
        int not_found = 0; // the annotation is not found on the loaded graph
        int eC_restriction = 0; // excluded due to evidence code restriction
        int taxonsRestriction = 0;


        logger.info("Loading...");

        String uriPrefix = (String) conf.getParameter("prefix");
        if (uriPrefix == null) {
            uriPrefix = g.getURI().getNamespace();
        }
        
        logger.info("Using prefix: "+uriPrefix);

        DataFactoryMemory uriManager = DataFactoryMemory.getSingleton();

        boolean validHeader = false;
        String line, qualifier, gotermURI, evidenceCode, taxon_ids;

        try {

            BigFileReader file = new BigFileReader(fileLocation);

            while (file.hasNext()) {


                line = file.nextTrimmed();



                if (line.startsWith("!")) {

                    String[] data = line.split(":");

                    if (data.length == 2) {
                        String flag = data[0].trim().substring(1);
                        String version = data[1].trim();

                        if (flag.equals("gaf-version") && (version.equals("2") || version.equals("2.0"))) {
                            validHeader = true;
                        }
                    }
                } else if (validHeader) {

                    String[] data = p_tab.split(line);



                    URI entityID = uriManager.createURI(uriPrefix + data[DB_OBJECT_ID]);
                    qualifier = data[QUALIFIER];
                    gotermURI = buildURI(data[GOID]);
                    evidenceCode = data[EVIDENCE_CODE];
                    taxon_ids = data[TAXON];


                    // check if Evidence Code is valid
                    if (excludedEC == null || (excludedEC != null && EvidenceCodeRules.areValid(excludedEC, evidenceCode))) {


                        //System.out.println(data[protID]+" ->  "+qualifier+"  "+gotermid);

                        // We do not consider go term associated with a qualifier 
                        // e.g. NOT, contributes_to ...
                        // TODO take into consideration this information !
                        if (qualifier.isEmpty()) {

                            V term = null;

                            URI uriNode = uriManager.createURI(gotermURI);

                            if (g.containsVertex(uriNode)) {
                                term = g.getV(uriNode);
                            }

                            if (term != null) {

                                // Check if annotation was already loaded 
                                // only considering the object pointed by 
                                // the annotation.

                                boolean exists = false;

                                if (entitiesAnnots.containsKey(entityID)
                                        && entitiesAnnots.get(entityID).contains(term)) {

                                    exists = true;
                                }

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
                                if (!exists && valid) {

                                    if (!entitiesAnnots.containsKey(entityID)) {
                                        entitiesAnnots.put(entityID, new HashSet< V>());
                                    }

                                    entitiesAnnots.get(entityID).add(term);
                                } else if (valid == false) {
                                    taxonsRestriction++;
                                }
                            } else {
                                not_found++;
                                logger.debug("Cannot found Vertex " + uriNode);
                            }
                        } else {
                            existsQualifier++;
                        }
                    } else {
                        eC_restriction++;
                    }
                }
            }
            file.close();

        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e);
        }

        if (!validHeader) {
            throw new SLIB_Ex_Critic("Invalid header for GAF-2 file " + fileLocation + "\nExpecting \"!gaf-version: 2.0\" as first line");
        }

        logger.info("\tExcluded  - Taxons restriction  		  : " + taxonsRestriction);
        logger.info("\tExcluded  - Evidence Code restriction  : " + eC_restriction);
        logger.info("\tExcluded  - Contains qualifier 	      : " + existsQualifier);
        logger.info("\tNot found unexisting term in the graph :	" + not_found);


        // Build Instance
        long vnumber = 0;
        long vedges = 0;

        for (Entry<URI, HashSet<V>> entry : entitiesAnnots.entrySet()) {

            URI instanceURI = entry.getKey();

            V i = new VertexTyped(g, instanceURI, VType.INSTANCE);

            Set<V> annotations = entry.getValue();

            g.addV(i);

            for (V v : annotations) {
                g.addE(new EdgeTyped(i, v, RDF.TYPE));
            }

            vnumber++;
            vedges += annotations.size();
        }

        logger.info("Number of Instance loaded 	  	: " + vnumber);
        logger.info("Number of Annotation loaded 	: " + vedges);
    }

    private String buildURI(String value) throws SLIB_Ex_Critic {

        String info[] = getDataColonSplit(value);


        if (info != null && info.length == 2) {

            String ns = data.getNamespace(info[0]);
            if (ns == null) {
                throw new SLIB_Ex_Critic("No namespace associated to prefix " + info[0] + ". Cannot load " + value + ", please load required namespace prefix");
            }

            return ns + info[1];
        } else {
            return gNS + value;
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
