/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.sglib.io.loader.bio.snomedct;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.GraphLoader;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.impl.EdgeTyped;
import slib.sglib.model.graph.elements.impl.VertexTyped;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.impl.memory.GraphMemory;
import slib.sglib.model.repo.impl.DataFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class GraphLoaderSnomedCT_RF2 implements GraphLoader {

    public static String ARG_CONCEPT_FILE       = "concept_file";
    public static String ARG_RELATIONSHIP_FILE  = "relationship_file";
    public static String ARG_PREFIX  = "prefix";
    
    
    HashMap<String, V> conceptMap = new HashMap<String, V>();
    Logger logger = LoggerFactory.getLogger(this.getClass());
    Pattern p_tab = Pattern.compile("\\t");
    DataFactoryMemory repo = DataFactoryMemory.getSingleton();
    public static Map<String, URI> idMapping = new HashMap<String, URI>() {
        {
            put("116680003", RDFS.SUBCLASSOF);
        }
    ;
    };
    private int CONCEPT_ID = 0;
    private int CONCEPT_ACTIVE = 2;
    private int RELATIONSHIP_ACTIVE = 2;
    private int RELATIONSHIP_SOURCE_CONCEPT_ID = 4;
    private int RELATIONSHIP_TARGET_CONCEPT_ID = 5;
    private int RELATIONSHIP_TYPE_ID = 7;

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

        String concept_file      = (String) conf.getParameter(ARG_CONCEPT_FILE);
        String relationship_file = (String) conf.getParameter(ARG_RELATIONSHIP_FILE);
        String prefix = (String) conf.getParameter(ARG_PREFIX);

        logger.info("Concept file:      " + concept_file);
        logger.info("Relationship file: " + relationship_file);

        if (concept_file == null) {
            throw new SLIB_Ex_Critic("Please specify a file containing the concept specification, argument " + ARG_CONCEPT_FILE);
        }
        if (relationship_file == null) {
            throw new SLIB_Ex_Critic("Please specify a file containing the relationship specification, argument " + ARG_RELATIONSHIP_FILE);
        }
        
        if(prefix == null){
            prefix = g.getURI().getNamespace();
        }

        logger.info("Loading concepts");

        FileInputStream fstream;
        try {
            fstream = new FileInputStream(concept_file);

            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            String[] split;

            while ((line = br.readLine()) != null) {

                split = p_tab.split(line);

                boolean active = split[CONCEPT_ACTIVE].trim().equals("1");

                if (active) {

                    URI cURI = repo.createURI(prefix + split[CONCEPT_ID]);
                    V v = g.addV(new VertexTyped(g, cURI, VType.CLASS));

                    //SnomedCT_concept c = new SnomedCT_concept(split[0],v);
                    conceptMap.put(split[CONCEPT_ID], v);
                }
            }
            in.close();

            logger.info("Number of concepts loaded " + conceptMap.size());
            logger.info("\nRelationship file: " + relationship_file);

            fstream = new FileInputStream(relationship_file);

            in = new DataInputStream(fstream);
            br = new BufferedReader(new InputStreamReader(in));

            double relationship_count = 0;

            while ((line = br.readLine()) != null) {

                split = p_tab.split(line);


                boolean active = split[RELATIONSHIP_ACTIVE].trim().equals("1");

                if (active) {
                    String source_id = split[RELATIONSHIP_SOURCE_CONCEPT_ID];
                    String target_id = split[RELATIONSHIP_TARGET_CONCEPT_ID];

                    if (conceptMap.containsKey(source_id) && conceptMap.containsKey(target_id)) {

                        V src = conceptMap.get(source_id);
                        V tar = conceptMap.get(target_id);

                        URI pred;

                        if (idMapping.containsKey(split[RELATIONSHIP_TYPE_ID])) {
                            pred = idMapping.get(split[RELATIONSHIP_TYPE_ID]);
                        } else {
                            pred = repo.createURI(prefix + split[RELATIONSHIP_TYPE_ID]);
                        }
                        E e = new EdgeTyped(src, tar, pred);

                        g.addE(e);
                        relationship_count++;
                    }
                }
            }
            logger.info("Number of relationships loaded: " + relationship_count);
        } catch (Exception ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
    }

    public class SnomedCT_concept {

        String id;
        V vertex;

        public SnomedCT_concept(String id, V v) {
            this.id = id;
            this.vertex = v;
        }

        public V getVertex() {
            return vertex;
        }
    }

    public static void main(String[] a) throws SLIB_Exception {

        G g = new GraphMemory(DataFactoryMemory.getSingleton().createURI("http://graph/snomed-ct/"));
        GraphLoaderSnomedCT_RF2 loader = new GraphLoaderSnomedCT_RF2();
        loader.populate(null, g);

    }
}
