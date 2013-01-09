package slib.sglib.io.loader.bio.mesh;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class MeshXMLHandler extends DefaultHandler {

    GraphLoader_MESH_XML loader;
    /**
     *
     */
    public  MeshConcept    concept;
    boolean descriptorName = false;
    boolean descriptorUI   = false;
    boolean treeNumber     = false;
    
    /**
     *
     * @param loader
     */
    public MeshXMLHandler(GraphLoader_MESH_XML loader){
        this.loader = loader;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("DescriptorRecord")) {// start creation of a concept
            
            if(concept != null){
                loader.addConcept(concept);
            }
            concept = new MeshConcept();
        }

        if (qName.equalsIgnoreCase("DescriptorUI") && concept.descriptorUI == null) { 
            descriptorUI = true;
        }

        if (qName.equalsIgnoreCase("descriptorName") && concept.descriptorName == null) { // define descriptor name
            descriptorName = true;
        }

        if (qName.equalsIgnoreCase("TreeNumber")) {// define tree number
            treeNumber = true;
        }
    }

    @Override
    public void endElement(String uri, String localName,
            String qName) throws SAXException {

        if (descriptorUI) {
            descriptorUI = false;
        } else if (descriptorName) {
            descriptorName = false;
        } else if (treeNumber) {
            treeNumber = false;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

        if (descriptorUI) {
            concept.descriptorUI = new String(ch, start, length);
        }
        else if (descriptorName) {
            concept.descriptorName = new String(ch, start, length);
        }
        else if (treeNumber) {
            concept.addTreeNumber(new String(ch, start, length));
        }
    }
}
