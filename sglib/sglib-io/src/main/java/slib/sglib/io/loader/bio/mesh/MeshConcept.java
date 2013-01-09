/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.sglib.io.loader.bio.mesh;

import java.util.HashSet;
import java.util.Set;
import slib.sglib.model.impl.graph.elements.VertexTyped;

/**
 * Dummy representation of a Mesh Concept
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class MeshConcept{
    
    String descriptorUI;
    String descriptorName;
    Set<String> treeNumberList;
    
    /**
     *
     */
    public MeshConcept(){
        treeNumberList = new HashSet<String>();
    }

    /**
     *
     * @return
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
     * @return
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
     *
     * @param treeNumber
     */
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
