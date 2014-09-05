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
package slib.indexer.mesh;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import slib.indexer.URIDescriptionBasic;
import slib.indexer.IndexHash;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Class used to build an index for the descriptors specified in the MeSH XML.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Indexer_MESH_XML {

    static Logger logger = LoggerFactory.getLogger(Indexer_MESH_XML.class);
    static Set<MeshDescriptor> descriptors = new HashSet<MeshDescriptor>();
    static URIFactory factory;
    static String default_namespace;

    /**
     * Build an index for the descriptors specified in the MeSH XML. Each
     * descriptors will be associated to its preferred name and descriptions.
     *
     * @param factory the URI factory which will be used to generate the URIs
     * @param filepath the path to the XML file
     * @param defaultNamespace the default namespace used to generate the URIs
     * @return the index
     * @throws SLIB_Exception
     */
    public static IndexHash buildIndex(URIFactory factory, String filepath, String defaultNamespace) throws SLIB_Exception {

        Indexer_MESH_XML.factory = factory;

        Indexer_MESH_XML.default_namespace = defaultNamespace;

        IndexHash index = new IndexHash();
        try {
            logger.info(" Mesh XML Indexer");
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser saxParser;

            saxParser = f.newSAXParser();
            saxParser.parse(filepath, new MeshXMLHandler(descriptors));

            logger.info("Number of descriptor loaded " + descriptors.size());
            logger.info("Generating relationships ");
            
            // create relationships 
            for (MeshDescriptor c : descriptors) {

                String uriConceptAsString = default_namespace + c.getDescriptorUI();
                URI uriConcept = factory.getURI(uriConceptAsString);

                URIDescriptionBasic i = new URIDescriptionBasic(uriConcept, c.descriptorName);
                i.addDescriptions(c.descriptions);

                index.addDescription(uriConcept, i);
            }

        } catch (IOException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        } catch (ParserConfigurationException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        } catch (SAXException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
        logger.info("MESH loader - process performed");
        return index;
    }
}
