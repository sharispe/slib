package slib.indexer.mesh;

import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class MeshXMLHandler extends DefaultHandler {

    Indexer_MESH_XML loader;
    public MeshConcept concept;
    boolean descriptorName = false;
    boolean descriptorUI = false;
    boolean treeNumber = false;
    boolean termDesc = false;
    boolean termDescString = false;
    
    StringBuilder tmpString;

    public MeshXMLHandler(Indexer_MESH_XML loader) {
        this.loader = loader;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("DescriptorRecord")) {// start creation of a concept

            if (concept != null) {
                loader.addConcept(concept);
            }
            concept = new MeshConcept();
        }

        // define UI only the first UI specified is considered
        if (qName.equalsIgnoreCase("DescriptorUI") && concept.descriptorUI == null) {
            descriptorUI = true;
        }

        // Define the name of the concept as the first descriptor tag encountred
        if (qName.equalsIgnoreCase("descriptorName") && concept.descriptorName == null) { // define descriptor name
            descriptorName = true;
        }

        // Define the name of the concept as the first descriptor tag encountred
        if (qName.equalsIgnoreCase("Term")) { // define term definition
            termDesc = true;
        }
        else if (termDesc && qName.equalsIgnoreCase("String")) {// define tree number
            termDescString = true;
            tmpString = new StringBuilder();
        }

        if (qName.equalsIgnoreCase("TreeNumber")) {// define tree number
            treeNumber = true;
        }
        
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (descriptorUI) {
            descriptorUI = false;
        } else if (descriptorName) {
            descriptorName = false;
        } else if (treeNumber) {
            treeNumber = false;
        } else if (qName.equalsIgnoreCase("Term")) { // define term definition
            termDesc = false;
        } else if (termDesc && qName.equalsIgnoreCase("String")) {
            termDescString = false;
            concept.descriptions.add(tmpString.toString());
            tmpString = null;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

        if (descriptorUI) {
            concept.descriptorUI = new String(ch, start, length);
        } else if (descriptorName) {
            concept.descriptorName = new String(ch, start, length);
        } else if (treeNumber) {
            concept.addTreeNumber(new String(ch, start, length));
        } else if (termDescString) {
            // Parser is calling characters method more than one time, because it can and allowed per spec...
            tmpString.append(new String(ch, start, length));
        }
    }
}
