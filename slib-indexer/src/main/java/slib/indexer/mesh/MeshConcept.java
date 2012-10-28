/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.indexer.mesh;

import java.util.HashSet;
import java.util.Set;

/**
 * Dummy representation of a Mesh Concept
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class MeshConcept{
    
    String descriptorUI;
    String descriptorName;
    Set<String> treeNumberList;
    Set<String> descriptions; // Term as string
    
    public MeshConcept(){
        treeNumberList = new HashSet<String>();
        descriptions = new HashSet<String>();
    }

    public String getDescriptorUI() {
        return descriptorUI;
    }

    public void setDescriptorUI(String descriptorUI) {
        this.descriptorUI = descriptorUI;
    }

    public String getDescriptorName() {
        return descriptorName;
    }

    public void setDescriptorName(String descriptorName) {
        this.descriptorName = descriptorName;
    }
    
    
    public void addTreeNumber(String treeNumber){
        treeNumberList.add(treeNumber);
    }
    
    @Override
    public String toString(){
        String out = descriptorUI +"\n";
        out += "\t"+descriptorName+"\n";
        out += "\t"+treeNumberList+"\n";
        return out;
    }
    
    
}
