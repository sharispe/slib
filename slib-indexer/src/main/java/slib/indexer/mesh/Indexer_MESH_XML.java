/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.indexer.mesh;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.indexer.IndexElementBasic;
import slib.indexer.IndexHash;
import slib.sglib.model.repo.DataFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

public class Indexer_MESH_XML {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<String, MeshConcept> idToConcepts = new HashMap<String, MeshConcept>();
    Set<MeshConcept> concepts = new HashSet<MeshConcept>();
    DataFactory factory;
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

    public IndexHash buildIndex(DataFactory factory, String filepath, String defaultNamespace) throws SLIB_Exception {

        this.factory = factory;

        this.default_namespace = defaultNamespace;

        IndexHash index = new IndexHash();
        try {
            logger.info(" Mesh XML Indexer");
            idToConcepts = new HashMap<String, MeshConcept>();
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser saxParser;

            saxParser = f.newSAXParser();
            saxParser.parse(filepath, new MeshXMLHandler(this));


            logger.info("Number of descriptor loaded " + concepts.size());
            logger.info("Generating relationships ");
            
            

            // create relationships 
            for (Entry<String, MeshConcept> e : idToConcepts.entrySet()) {

                MeshConcept c = e.getValue();

                String uriConceptAsString = default_namespace + c.getDescriptorUI();
                URI uriConcept = factory.createURI(uriConceptAsString);
                
                IndexElementBasic i = new IndexElementBasic();
                i.addDescriptions(c.descriptions);
                i.setPreferredDescription(c.descriptorName);
                i.addDescription(c.descriptorName);
                
                index.addValue(uriConcept, i);
                                
            }

        } catch (Exception ex) { // sorry
            throw new SLIB_Ex_Critic(ex.getMessage());
        }

        logger.info("MESH loader - process performed");
        return index;
    }
}