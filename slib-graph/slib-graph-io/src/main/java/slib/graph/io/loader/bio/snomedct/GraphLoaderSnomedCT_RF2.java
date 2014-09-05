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
package slib.graph.io.loader.bio.snomedct;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.graph.elements.Edge;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphLoaderSnomedCT_RF2 implements GraphLoader {

    public final static String ARG_CONCEPT_FILE = "concept_file";
    public final static String ARG_RELATIONSHIP_FILE = "relationship_file";
    public final static String ARG_PREFIX = "prefix";
    public final static String ARG_LOAD_INACTIVE_CONCEPTS = "load_inactive_concepts";
    public final static String ARG_LOAD_INACTIVE_RELATIONSHIPS = "load_inactive_relationships";
    public final static String ID_SUBCLASSOF_SNOMED = "116680003";
    
    
    Map<String, URI> conceptMap = new HashMap<String, URI>();
    Logger logger = LoggerFactory.getLogger(this.getClass());
    Pattern p_tab = Pattern.compile("\\t");
    URIFactory repo = URIFactoryMemory.getSingleton();
    /**
     *
     */
    public static Map<String, URI> idMapping = new HashMap<String, URI>() {
        {
            put(ID_SUBCLASSOF_SNOMED, RDFS.SUBCLASSOF);
        }
    ;
    };
    
    // Concept file columns
    private final int CONCEPT_ID = 0;
    private final int CONCEPT_ACTIVE = 2;
    private final int CONCEPT_DATE = 1;
    // Relationships file columns
    private final int RELATIONSHIP_ID = 0;
    private final int RELATIONSHIP_DATE = 1;
    private final int RELATIONSHIP_ACTIVE = 2;
    private final int RELATIONSHIP_SOURCE_CONCEPT_ID = 4;
    private final int RELATIONSHIP_TARGET_CONCEPT_ID = 5;
    private final int RELATIONSHIP_TYPE_ID = 7;
    private boolean LOAD_ONLY_ACTIVE_CONCEPTS = true;
    private boolean LOAD_ONLY_ACTIVE_RELATIONSHIPS = true;


    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        /*
         * Loading concepts:
         * Filtering on active field [2] only consider those with a value equal to 1; 
         */

        String concept_file = (String) conf.getParameter(ARG_CONCEPT_FILE);
        String relationship_file = (String) conf.getParameter(ARG_RELATIONSHIP_FILE);
        String prefix = (String) conf.getParameter(ARG_PREFIX);

        if (conf.existsParam(ARG_LOAD_INACTIVE_CONCEPTS)) {
            String load_inactive_concepts = conf.getParameter(ARG_LOAD_INACTIVE_CONCEPTS).toString();

            if (load_inactive_concepts.equalsIgnoreCase("true")) {
                LOAD_ONLY_ACTIVE_CONCEPTS = false;
            }
        }

        if (conf.existsParam(ARG_LOAD_INACTIVE_RELATIONSHIPS)) {
            String load_inactive_relationships = conf.getParameter(ARG_LOAD_INACTIVE_RELATIONSHIPS).toString();

            if (load_inactive_relationships.equalsIgnoreCase("true")) {
                LOAD_ONLY_ACTIVE_RELATIONSHIPS = false;
            }
        }

        logger.info("-------------------------------------");
        logger.info("Loading SNOMED-CT [RF2]      ");
        logger.info("-------------------------------------");
        logger.info("Concept file:      " + concept_file);
        logger.info("Relationship file: " + relationship_file);

        if (concept_file == null) {
            throw new SLIB_Ex_Critic("Please specify a file containing the concept specification, argument " + ARG_CONCEPT_FILE);
        }
        if (relationship_file == null) {
            throw new SLIB_Ex_Critic("Please specify a file containing the relationship specification, argument " + ARG_RELATIONSHIP_FILE);
        }

        if (prefix == null && g.getURI() != null) {
            prefix = g.getURI().getNamespace();
        }

        logger.info("Loading concepts");

        DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
        Map<String, ConceptSnomedCT> concepts = new HashMap<String, ConceptSnomedCT>();

        FileInputStream fstream;
        try {
            fstream = new FileInputStream(concept_file);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            String[] data;
            boolean header = true;

            while ((line = br.readLine()) != null) {

                if (header) {
                    header = false;
                    continue;
                }

                data = p_tab.split(line);

                Date date = formatter.parse(data[CONCEPT_DATE]);

                if (concepts.containsKey(data[CONCEPT_ID])) {
                    ConceptSnomedCT cExists = concepts.get(data[CONCEPT_ID]);

                    if (cExists.date.before(date)) { // the current specification is more recent than the other loaded, we alsways consider the last one
                        ConceptSnomedCT newConcept = new ConceptSnomedCT(data[CONCEPT_ID], date, data[CONCEPT_ACTIVE].trim().equals("1"));
                        concepts.put(data[CONCEPT_ID], newConcept);
                    }
                    // else do nothing the current concept is obsolete compared to the one we already strore
                } else {
                    ConceptSnomedCT concept = new ConceptSnomedCT(data[CONCEPT_ID], date, data[CONCEPT_ACTIVE].trim().equals("1"));
                    concepts.put(data[CONCEPT_ID], concept);
                }

            }
            in.close();

            long loaded = 0;

            for (ConceptSnomedCT concept : concepts.values()) {

                if (!LOAD_ONLY_ACTIVE_CONCEPTS || concept.active) {
                    URI cURI = repo.getURI(prefix,concept.id);
                    conceptMap.put(concept.id, cURI);
                    loaded++;
                }
            }

            logger.info("Number of activeconcepts loaded " + loaded + " on "+concepts.size()+" concepts");            
            logger.info("Relationship file: " + relationship_file);

            in = new DataInputStream(new FileInputStream(relationship_file));
            br = new BufferedReader(new InputStreamReader(in));

            Map<String, RelationshipSnomedCT> relationships = new HashMap<String, RelationshipSnomedCT>();


            header = true;
            long c = 0;


            logger.info("Loading relationships information... please wait");
            // Load the relationships information
            while ((line = br.readLine()) != null) {

                if (header) {
                    header = false;
                    continue;
                }
                c++;
                if (c % 100000 == 0) {
                    logger.debug("Processed " + c + "\t" + relationships.size() + " relationships information loaded");
                }
                data = p_tab.split(line);

                Date date = formatter.parse(data[RELATIONSHIP_DATE]);

                if (relationships.containsKey(data[RELATIONSHIP_ID])) {
                    RelationshipSnomedCT rExists = relationships.get(data[RELATIONSHIP_ID]);

                    if (!rExists.date.after(date)) { // the current specification is more recent than the other loaded, we only consider the last one
                        RelationshipSnomedCT newRelationship = new RelationshipSnomedCT(data[RELATIONSHIP_SOURCE_CONCEPT_ID], data[RELATIONSHIP_TARGET_CONCEPT_ID], data[RELATIONSHIP_TYPE_ID], date, data[RELATIONSHIP_ACTIVE].trim().equals("1"));
                        // we replace the older
                        relationships.put(data[RELATIONSHIP_ID], newRelationship);
                    }
                    // else do nothing the current relationship is obsolete regarding th one we already strore
                } else {
                    RelationshipSnomedCT newRelationship = new RelationshipSnomedCT(data[RELATIONSHIP_SOURCE_CONCEPT_ID], data[RELATIONSHIP_TARGET_CONCEPT_ID], data[RELATIONSHIP_TYPE_ID], date, data[RELATIONSHIP_ACTIVE].trim().equals("1"));

                    // we replace the older
                    relationships.put(data[RELATIONSHIP_ID], newRelationship);
                }
            }

            // Load the relationships and corresponding concepts who are not defined as inactive
            double relationship_count = 0;
            logger.info("Adding relationships to the graph... please wait");
            for (RelationshipSnomedCT r : relationships.values()) {

                if (!LOAD_ONLY_ACTIVE_RELATIONSHIPS || r.active) {
                    if (conceptMap.containsKey(r.source) && conceptMap.containsKey(r.target)) {

                        URI src = conceptMap.get(r.source);
                        URI tar = conceptMap.get(r.target);
                        URI pred;

                        if (idMapping.containsKey(r.relationshipID)) {
                            pred = idMapping.get(r.relationshipID);
                        } else {
                            pred = repo.getURI(prefix,r.relationshipID);
                        }
                        E e = new Edge(src, pred, tar);

                        g.addE(e);
                        relationship_count++;
                    }
                }
            }
            logger.info("Number of relationships loaded: " + relationship_count);
            logger.info("-------------------------------------");
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
    }

    /**
     * Representation of a relationship SNOMED-CT Use to store the latest
     * relationships defined in the SnomedCT file.
     */
    class RelationshipSnomedCT {

        String source;
        String target;
        Date date;
        boolean active;
        String relationshipID;

        public RelationshipSnomedCT(String src, String tgt, String rID, Date d, boolean isActive) {
            this.source = src;
            this.target = tgt;
            this.date = d;
            this.relationshipID = rID;
            this.active = isActive;
        }
    }

    class ConceptSnomedCT {

        String id;
        Date date;
        boolean active;

        public ConceptSnomedCT(String id, Date d, boolean isActive) {
            this.id = id;
            this.date = d;
            this.active = isActive;
        }
    }
}
