package slib.sglib.io.loader.bio.mesh;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class MeshXMLHandler extends DefaultHandler {

    GraphLoader_MESH_XML loader;
    public MeshConcept concept;
    boolean descriptorName = false;
    boolean descriptorUI = false;
    boolean treeNumber = false;
    final String DESCRIPTOR_RECORD = "DescriptorRecord";
    final String DESCRIPTOR_URI = "DescriptorUI";
    final String DESCRIPTOR_NAME = "DescriptorName";
    final String TREE_NUMBER = "TreeNumber";

    private StringBuffer currentTreeNumber;
    private StringBuffer currentDescriptorName;
    private StringBuffer currentDescriptorUI;

    /**
     * Create a XML handler for MeSH.
     *
     * @param loader the loader associated to the handler
     */
    public MeshXMLHandler(GraphLoader_MESH_XML loader) {
        this.loader = loader;

        currentTreeNumber = new StringBuffer();
        currentDescriptorName = new StringBuffer();
        currentDescriptorUI = new StringBuffer();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equals(DESCRIPTOR_RECORD)) {// start creation of a concept
            concept = new MeshConcept();
        }

        if (qName.equals(DESCRIPTOR_URI) && concept.descriptorUI == null) {
            descriptorUI = true;
        }

        if (qName.equals(DESCRIPTOR_NAME) && concept.descriptorName == null) { // define descriptor name
            descriptorName = true;
        }

        if (qName.equals(TREE_NUMBER)) {// define tree number
            treeNumber = true;
        }
    }

    @Override
    public void endElement(String uri, String localName,
            String qName) throws SAXException {

        if (descriptorUI) {
            descriptorUI = false;
            concept.descriptorUI = currentDescriptorUI.toString();
            currentDescriptorUI = new StringBuffer();
        } else if (descriptorName) {
            descriptorName = false;
            concept.descriptorName = currentDescriptorName.toString();
            currentDescriptorName = new StringBuffer();
        } else if (treeNumber) {
            treeNumber = false;
            concept.addTreeNumber(currentTreeNumber.toString());
            currentTreeNumber = new StringBuffer();
        }

        if (qName.equals(DESCRIPTOR_RECORD)) {
            loader.addConcept(concept);
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

        if (descriptorUI) {
            currentDescriptorUI.append(new String(ch, start, length));
        } else if (descriptorName) {
            currentDescriptorName.append(new String(ch, start, length));
        } else if (treeNumber) {
            currentTreeNumber.append(new String(ch, start, length));
        }
    }
}
