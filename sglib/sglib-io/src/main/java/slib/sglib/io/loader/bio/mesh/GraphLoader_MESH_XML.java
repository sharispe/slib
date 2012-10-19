/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.sglib.io.loader.bio.mesh;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.IGraphLoader;
import slib.sglib.io.util.GFormat;
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

public class GraphLoader_MESH_XML implements IGraphLoader {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<String, MeshConcept> idToConcepts = new HashMap<String, MeshConcept>();
    Set<MeshConcept> concepts = new HashSet<MeshConcept>();
    G graph;
    DataFactoryMemory dataRepo = DataFactoryMemory.getSingleton();
    
    
    public static final String ARG_PREFIX  = "prefix";
    
    
    String default_namespace;

    /**
     * Return parent ID i.e. giving C10.228.140.300.275.500 will return
     * C10.228.140.300.275
     *
     * @return
     */
    private String getParentId(String id) {

        String[] data = id.split("\\.");
        String idParent = null;

        for (int i = data.length - 2; i >= 0; i--) {
            if (idParent == null) {
                idParent = data[i];
            } else {
                idParent = data[i] + "." + idParent;
            }
        }
        return idParent;
    }

    void addConcept(MeshConcept concept) {
        for (String s : concept.treeNumberList) {
            idToConcepts.put(s, concept);
        }
        concepts.add(concept);
    }

    @Override
    public G load(GraphConf conf) throws SLIB_Exception {
        return GraphLoaderGeneric.load(conf);
    }

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        this.graph = g;

        
        default_namespace = (String) conf.getParameter(ARG_PREFIX);
        
        if(default_namespace == null){
            default_namespace = graph.getURI().getNamespace();
        }


        try {
            logger.info("Loading Mesh XML");
            idToConcepts = new HashMap<String, MeshConcept>();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser;

            saxParser = factory.newSAXParser();
            saxParser.parse(conf.getLoc(), new MeshXMLHandler(this));


            System.out.println("Number of descriptor loaded " + concepts.size());
            System.out.println("Generating relationships ");

            // create relationships 
            for (Entry<String, MeshConcept> e : idToConcepts.entrySet()) {

                MeshConcept c = e.getValue();

                // @TODO add possibility to set URI prefix
                //System.out.println(c.descriptorUI);
                V vConcept = getOrCreateVertex(c.descriptorUI);


                for (String idParent : c.treeNumberList) {

                    String parentId = getParentId(idParent);

                    if (parentId != null) { // roots


                        MeshConcept parent = idToConcepts.get(parentId);


                        if (parent == null) {
                            throw new SLIB_Ex_Critic("Cannot locate parent identified by TreeNumber " + idParent);
                        } else {
                            
                            //System.out.println("\t" + parentId + "\t" + parent.descriptorUI);
                            V vParent = getOrCreateVertex(parent.descriptorUI);
                            E edge = new EdgeTyped(vConcept, vParent, RDFS.SUBCLASSOF);
                            
                            g.addE(edge);
                        }
                    }
                }
            }

        } catch (Exception ex) { // sorry
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
        
        logger.info("MESH loader - process performed");
    }

    private V getOrCreateVertex(String descriptorUI) {

        String uriConceptAsString = default_namespace + descriptorUI;

        URI uriConcept = dataRepo.createURI(uriConceptAsString);
        V vConcept = graph.getV(uriConcept);

        if (vConcept == null) {
            vConcept = graph.addV(new VertexTyped(graph, uriConcept, VType.CLASS));
        }
        return vConcept;
    }
        
}