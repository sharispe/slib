/*
 * 
 * Copyright or © or Copr. Ecole des Mines d'Alès (2012) 
 * LGI2P research center
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 * 
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
import slib.sglib.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe
 */
public class Indexer_MESH_XML {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<String, MeshConcept> idToConcepts = new HashMap<String, MeshConcept>();
    Set<MeshConcept> concepts = new HashSet<MeshConcept>();
    URIFactory factory;
    String default_namespace;

    /**
     * Return parent ID i.e. giving C10.228.140.300.275.500 will return
     * C10.228.140.300.275
     *
     * @return the ID of the parent node
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

    /**
     *
     * @param factory
     * @param filepath
     * @param defaultNamespace
     * @return the index
     * @throws SLIB_Exception
     */
    public IndexHash buildIndex(URIFactory factory, String filepath, String defaultNamespace) throws SLIB_Exception {

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

                IndexElementBasic i = new IndexElementBasic(uriConcept, c.descriptorName);
                i.addDescriptions(c.descriptions);

                index.addValue(uriConcept, i);

            }

        } catch (Exception ex) { // sorry
            throw new SLIB_Ex_Critic(ex.getMessage());
        }

        logger.info("MESH loader - process performed");
        return index;
    }
}