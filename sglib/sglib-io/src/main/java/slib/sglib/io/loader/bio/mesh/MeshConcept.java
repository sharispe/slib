/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.sglib.io.loader.bio.mesh;

import java.util.HashSet;
import java.util.Set;
import slib.sglib.model.graph.elements.impl.VertexTyped;

/**
 * Dummy representation of a Mesh Concept
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class MeshConcept{
    
    String descriptorUI;
    String descriptorName;
    Set<String> treeNumberList;
    
    public MeshConcept(){
        treeNumberList = new HashSet<String>();
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
