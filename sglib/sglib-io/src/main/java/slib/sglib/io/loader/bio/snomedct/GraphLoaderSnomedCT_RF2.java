package slib.sglib.io.loader.bio.snomedct;

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
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoader;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.graph.elements.Vertex;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class GraphLoaderSnomedCT_RF2 implements GraphLoader {

    public static String ARG_CONCEPT_FILE = "concept_file";
    public static String ARG_RELATIONSHIP_FILE = "relationship_file";
    public static String ARG_PREFIX = "prefix";
    public static String ARG_LOAD_INACTIVE_CONCEPTS = "load_inactive_concepts";
    public static String ARG_LOAD_INACTIVE_RELATIONSHIPS = "load_inactive_relationships";
    public static String ID_SUBCLASSOF_SNOMED = "116680003";
    
    
    Map<String, V> conceptMap = new HashMap<String, V>();
    Logger logger = LoggerFactory.getLogger(this.getClass());
    Pattern p_tab = Pattern.compile("\\t");
    DataFactory repo = DataFactoryMemory.getSingleton();
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
    private int CONCEPT_ID = 0;
    private int CONCEPT_ACTIVE = 2;
    private int CONCEPT_DATE = 1;
    // Relationships file columns
    private int RELATIONSHIP_ID = 0;
    private int RELATIONSHIP_DATE = 1;
    private int RELATIONSHIP_ACTIVE = 2;
    private int RELATIONSHIP_SOURCE_CONCEPT_ID = 4;
    private int RELATIONSHIP_TARGET_CONCEPT_ID = 5;
    private int RELATIONSHIP_TYPE_ID = 7;
    private boolean LOAD_ONLY_ACTIVE_CONCEPTS = true;
    private boolean LOAD_ONLY_ACTIVE_RELATIONSHIPS = true;

    @Override
    public G load(GraphConf conf) throws SLIB_Exception {
        return GraphLoaderGeneric.load(conf);
    }

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

        logger.info("Loading SNOMED-CT [RF2]      ");
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
                    URI cURI = repo.createURI(prefix,concept.id);
                    V v = g.addV(new Vertex(cURI, VType.CLASS));
                    conceptMap.put(concept.id, v);
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
                    logger.debug("Processed " + c + "\t" + relationships.size() + " relationships loaded");
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
            logger.info("Loading relationships... please wait");
            for (RelationshipSnomedCT r : relationships.values()) {

                if (!LOAD_ONLY_ACTIVE_RELATIONSHIPS || r.active) {
                    if (conceptMap.containsKey(r.source) && conceptMap.containsKey(r.target)) {

                        V src = conceptMap.get(r.source);
                        V tar = conceptMap.get(r.target);
                        URI pred;

                        if (idMapping.containsKey(r.relationshipID)) {
                            pred = idMapping.get(r.relationshipID);
                        } else {
                            pred = repo.createURI(prefix,r.relationshipID);
                        }
                        E e = new Edge(src, tar, pred);

                        g.addE(e);
                        relationship_count++;
                    }
                }
            }
            logger.info("Number of relationships loaded: " + relationship_count);
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
