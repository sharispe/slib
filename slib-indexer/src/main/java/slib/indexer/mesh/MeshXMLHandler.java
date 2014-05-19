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

import java.util.Set;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class MeshXMLHandler extends DefaultHandler {

    Set<MeshDescriptor> descriptors;
    public MeshDescriptor currentDescriptor;
    boolean descriptorName = false;
    boolean descriptorUI = false;
    boolean treeNumber = false;
    boolean termDesc = false;
    boolean termDescString = false;

    StringBuilder tmpDescString;
    StringBuilder tmpDescriptorUI;
    StringBuilder tmpDescriptorName;
    StringBuilder tmpTreeNumber;

    
    public MeshXMLHandler(Set<MeshDescriptor> concepts) {
        this.descriptors = concepts;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("DescriptorRecord")) {// start creation of a concept

            if (currentDescriptor != null) {
                descriptors.add(currentDescriptor);
            }
            currentDescriptor = new MeshDescriptor();
        }

        // define UI only the first UI specified is considered
        if (qName.equalsIgnoreCase("DescriptorUI") && currentDescriptor.descriptorUI == null) {
            descriptorUI = true;
            tmpDescriptorUI = new StringBuilder();
        }

        // Define the name of the concept as the first descriptor tag encountred
        if (qName.equalsIgnoreCase("descriptorName") && currentDescriptor.descriptorName == null) { // define descriptor name
            descriptorName = true;
            tmpDescriptorName = new StringBuilder();
        }

        // Define the name of the concept as the first descriptor tag encountred
        if (qName.equalsIgnoreCase("Term")) { // define term definition
            termDesc = true;
        } else if (termDesc && qName.equalsIgnoreCase("String")) {// define tree number
            termDescString = true;
            tmpDescString = new StringBuilder();
        }

        if (qName.equalsIgnoreCase("TreeNumber")) {// define tree number
            treeNumber = true;
            tmpTreeNumber = new StringBuilder();
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (descriptorUI) {
            descriptorUI = false;
            currentDescriptor.descriptorUI = tmpDescriptorUI.toString();
        } else if (descriptorName) {
            descriptorName = false;
            currentDescriptor.descriptorName = tmpDescriptorName.toString();
        } else if (treeNumber) {
            treeNumber = false;
            currentDescriptor.treeNumberList.add(tmpTreeNumber.toString());
        } else if (qName.equalsIgnoreCase("Term")) { // define term definition
            termDesc = false;
        } else if (termDesc && qName.equalsIgnoreCase("String")) {
            termDescString = false;
            currentDescriptor.descriptions.add(tmpDescString.toString());
            tmpDescString = null;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {

        if (descriptorUI) {
            tmpDescriptorUI.append(new String(ch, start, length));
        } else if (descriptorName) {
            tmpDescriptorName.append(new String(ch, start, length));
        } else if (treeNumber) {
            tmpTreeNumber.append(new String(ch, start, length));
        } else if (termDescString) {
            // Parser is calling characters method more than one time, because it can and allowed per spec...
            tmpDescString.append(new String(ch, start, length));
        }
    }
}
