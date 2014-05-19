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

import java.util.HashSet;
import java.util.Set;

/**
 * Simple representation of a Mesh Descriptor
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class MeshDescriptor {

    String descriptorUI;
    String descriptorName;
    Set<String> treeNumberList;
    Set<String> descriptions; // Term as string

    /**
     *
     */
    public MeshDescriptor() {
        treeNumberList = new HashSet<String>();
        descriptions = new HashSet<String>();
    }

    /**
     *
     * @return the descriptor id
     */
    public String getDescriptorUI() {
        return descriptorUI;
    }

    /**
     *
     * @param descriptorUI
     */
    public void setDescriptorUI(String descriptorUI) {
        this.descriptorUI = descriptorUI;
    }

    /**
     *
     * @return the name
     */
    public String getDescriptorName() {
        return descriptorName;
    }

    /**
     *
     * @param descriptorName
     */
    public void setDescriptorName(String descriptorName) {
        this.descriptorName = descriptorName;
    }

    /**
     * @param treeNumber
     */
    public void addTreeNumber(String treeNumber) {
        treeNumberList.add(treeNumber);
    }

    @Override
    public String toString() {
        String out = descriptorUI + "\n";
        out += "\t" + descriptorName + "\n";
        out += "\t" + treeNumberList + "\n";
        return out;
    }
}
