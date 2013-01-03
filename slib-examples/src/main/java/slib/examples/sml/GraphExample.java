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
package slib.examples.sml;

import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.graph.elements.EdgeTyped;
import slib.sglib.model.impl.graph.elements.VertexTyped;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.sml.sm.core.utils.SM_Engine;
import slib.utils.ex.SLIB_Exception;

/**
 * Snippet of code showing how to interact with the graph, i.e. add vertices, edges
 * The example also shows how to retrieve all the ancestors and descendants of a particular vertex
 * 
 * More information at http://www.lgi2p.ema.fr/kid/tools/sml/
 * 
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class GraphExample {
    
    public static void main(String[] params) throws SLIB_Exception{
       
        // We use a datafactory to create some elements such as URIs
        DataFactory factory = DataFactoryMemory.getSingleton();
        
        URI uriGraph = factory.createURI("http://graph/");
        URI uriVa    = factory.createURI("http://graph/vA");
        URI uriVb    = factory.createURI("http://graph/vB");
        URI uriVc    = factory.createURI("http://graph/vC");
        URI uriVd    = factory.createURI("http://graph/vD");
        
        URI relA    = factory.createURI("http://graph/interact");
        
        // We create a graph which is load in memory
        G graph = new GraphMemory(uriGraph);
        
        System.out.println("- Empty Graph");
        System.out.println(graph.toString());
        
        // We create vertices and an edge
        V vA = new VertexTyped(graph, uriVa, VType.CLASS);
        V vB = new VertexTyped(graph, uriVb, VType.CLASS);
        V vC = new VertexTyped(graph, uriVc, VType.CLASS);
        V vD = new VertexTyped(graph, uriVd, VType.CLASS);
 
        E e = new EdgeTyped(vD, vA, RDFS.SUBCLASSOF);
        
        // You can therfore add a vertex or an edge
        graph.addV(vA);
        graph.addE(e);
        
        System.out.println("- Graph");
        System.out.println(graph.toString());

        // It's also possible to directly add edges without explicitly create them
        graph.addE(vB, vA, RDFS.SUBCLASSOF);
        graph.addE(vC, vB, RDFS.SUBCLASSOF);
        graph.addE(vB, vD, relA);
        
       
        System.out.println("- Graph, edges added");
        System.out.println(graph.toString());
       
        
        
        // Retrieve the edges associated to a vertex
        Set<E> edgeOutD  = graph.getE(vD, Direction.OUT);   // out edges 
        Set<E> edgeInD   = graph.getE(vD, Direction.IN);    // in edges
        Set<E> edgeBothD = graph.getE(vD, Direction.BOTH);  // in and out edges
        Set<E> edgeBothSUBCLASSOF_D = graph.getE(RDFS.SUBCLASSOF,vD, Direction.BOTH); // in and out subClassOf edges
        
        System.out.println("D out : "+edgeOutD);
        System.out.println("D in  : "+edgeInD);
        System.out.println("D both: "+edgeBothD);
        System.out.println("D both (SubClassOf): "+edgeBothSUBCLASSOF_D);
        
        // Retrieve all the ancestors/descendant of a vertex
        SM_Engine engine = new SM_Engine(graph);
        Set<V> ancestorsB = engine.getAncestorsInc(vB);
        Set<V> descendantsB = engine.getDescendantsInc(vB);
        
        System.out.println("Ancestors vB:   "+ancestorsB);
        System.out.println("Descendants vB: "+descendantsB);
        
        
        // Retrieve the all the vertices and edges associated to a graph
        
        Set<V> vertices = graph.getV();
        Set<E> edges = graph.getE();
        
        System.out.println("-Vertices");
        for(V v : vertices){
            System.out.println("\t"+v);
        }
        
        System.out.println("-Edge");
        for(E edge : edges){
            System.out.println("\t"+edge);
        }
        
        
        System.out.println("- Graph, edges and vertices removed");
        // Remove the vertices or the edges
        graph.removeV(vertices); // associated edges are also removed
        graph.removeE(edges);
        
        
        System.out.println(graph.toString());
    }
    
}
