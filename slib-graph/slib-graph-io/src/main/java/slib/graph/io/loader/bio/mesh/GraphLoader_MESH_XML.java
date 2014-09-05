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
package slib.graph.io.loader.bio.mesh;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.graph.elements.Edge;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * MeSH Loader. This class permits to load the MeSH trees into a Directed
 * Acyclic Graph http://www.nlm.nih.gov/mesh/trees.html
 *
 * The loader was designed for the 2013 XML version of the MeSH, coherency with
 * prior or older version hasn't been tested.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphLoader_MESH_XML implements GraphLoader {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<String, MeshConcept> idToConcepts = new HashMap<String, MeshConcept>();
    Set<MeshConcept> concepts = new HashSet<MeshConcept>();
    G graph;
    URIFactory factory = URIFactoryMemory.getSingleton();
    int conceptIgnored = 0;
    /**
     *
     */
    public static final String ARG_PREFIX = "prefix";
    String default_namespace;

    /**
     * Return parent ID i.e. giving C10.228.140.300.275.500 will return
     * C10.228.140.300.275
     *
     * @return a String associated to the parent ID.
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

        if (!concept.treeNumberList.isEmpty()) {

            for (String s : concept.treeNumberList) {
                idToConcepts.put(s, concept);
            }
            concepts.add(concept);
        } else {
            logger.info("Warning: no tree number associated to " + concept.descriptorUI + " concept ignored...");
            conceptIgnored++;
        }
    }

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        this.graph = g;

        default_namespace = (String) conf.getParameter(ARG_PREFIX);

        if (default_namespace == null) {
            default_namespace = graph.getURI().getNamespace();
        }
        try {
            logger.info("-------------------------------------");
            logger.info("Loading Mesh XML");
            logger.info("-------------------------------------");

            idToConcepts = new HashMap<String, MeshConcept>();

            SAXParserFactory parserfactory = SAXParserFactory.newInstance();
            SAXParser saxParser;

            saxParser = parserfactory.newSAXParser();
            saxParser.parse(conf.getLoc(), new MeshXMLHandler(this));

            logger.info("Number of descriptor loaded " + concepts.size() + " (ignored " + conceptIgnored + ")");
            logger.info("Loading relationships ");

            // Create universal root if required
            URI universalRoot = OWL.THING;

            if (!graph.containsVertex(universalRoot)) {
                graph.addV(universalRoot);
            }

            // create relationships and roots of each tree
            for (Entry<String, MeshConcept> e : idToConcepts.entrySet()) {

                MeshConcept c = e.getValue();

                URI vConcept = getOrCreateVertex(c.descriptorUI);

                for (String treeNumber : c.treeNumberList) {

                    String parentId = getParentId(treeNumber);

                    if (parentId != null) {

                        MeshConcept parent = idToConcepts.get(parentId);

                        if (parent == null) {
                            throw new SLIB_Ex_Critic("Cannot locate parent identified by TreeNumber " + treeNumber + "\nError occured processing\n" + c);
                        } else {

                            //System.out.println("\t" + parentId + "\t" + parent.descriptorUI);
                            URI vParent = getOrCreateVertex(parent.descriptorUI);
                            E edge = new Edge(vConcept, RDFS.SUBCLASSOF, vParent);

                            g.addE(edge);
                        }
                    } else {
                        /* Those vertices are the inner roots of each trees, 
                         * i.e. Psychiatry and Psychology [F] tree has for inner roots:
                         *  - Behavior and Behavior Mechanisms [F01] 
                         *  - Psychological Phenomena and Processes [F02] 
                         *  - Mental Disorders [F03] 
                         *  - Behavioral Disciplines and Activities [F04] 
                         * A vertex has already been created for each inner root (e.g. F01, F02, F03, F04) 
                         * , we therefore create a vertex for the tree root (e.g. F).
                         * Finally all the tree roots are rooted by a global root which do not 
                         * correspond to a concept specified into the mesh.
                         * 
                         * More information about MeSH trees at http://www.nlm.nih.gov/mesh/trees.html
                         */

                        // we link the tree inner root to the root tree
                        char localNameTreeRoot = treeNumber.charAt(0); // id of the tree root
                        URI rootTree = getOrCreateVertex(localNameTreeRoot + ""); // e.g. F
                        E treeInnerRootToTreeRoot = new Edge(vConcept, RDFS.SUBCLASSOF, rootTree);
                        g.addE(treeInnerRootToTreeRoot);
//                        logger.debug("Creating Edge : " + treeInnerRootToTreeRoot);

                        // we link the tree root to the universal root
                        E treeRootToUniversalRoot = new Edge(rootTree, RDFS.SUBCLASSOF, universalRoot);
                        g.addE(treeRootToUniversalRoot);
//                        logger.debug("Creating Edge : " + treeRootToUniversalRoot);
                    }
                }
            }

        } catch (IOException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        } catch (ParserConfigurationException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        } catch (SAXException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        } catch (SLIB_Ex_Critic ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }

        logger.info("MESH loader - process performed");
        logger.info("-------------------------------------");
    }

    private URI getOrCreateVertex(String descriptorUI) {

        String uriConceptAsString = default_namespace + descriptorUI;

        URI uriConcept = factory.getURI(uriConceptAsString);

        if (!graph.containsVertex(uriConcept)) {
            graph.addV(uriConcept);
        }
        return uriConcept;
    }

    public static void main(String[] args) throws Exception {

        URIFactoryMemory factory = URIFactoryMemory.getSingleton();
        G graph = new GraphMemory(factory.getURI("http://mesh"));
        GraphLoader_MESH_XML loader = new GraphLoader_MESH_XML();
        loader.populate(new GDataConf(GFormat.MESH_XML, "/data/mesh/desc2013.xml"), graph);
        URI dna_barcoding = factory.getURI("http://D058893");
        System.out.println(graph);

        System.out.println(graph.containsVertex(dna_barcoding));

    }
}
