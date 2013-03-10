package slib.examples.sml.general;

import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.graph.elements.Vertex;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Exception;

/**
 * Snippet of code showing how to interact with the graph, i.e. add vertices, edges
 * The example also shows how to retrieve all the ancestors and descendants of a particular vertex
 * 
 * More information at http://www.semantic-measures-library.org/
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
        
        // We create a graph which is loaded in memory
        G graph = new GraphMemory(uriGraph);
        
        System.out.println("- Empty Graph");
        System.out.println(graph.toString());
        
        // We create vertices and an edge
        V vA = new Vertex(uriVa, VType.CLASS);
        V vB = new Vertex(uriVb, VType.CLASS);
        V vC = new Vertex(uriVc, VType.CLASS);
        V vD = new Vertex(uriVd, VType.CLASS);
 
        E e = new Edge(vD, vA, RDFS.SUBCLASSOF);
        
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
        
        
        // Retrieve all the vertices and edges associated to a graph
        
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
        // Remove the vertices and the edges
        graph.removeV(vertices); // associated edges are also removed
        graph.removeE(edges);
        
        
        System.out.println(graph.toString());
    }
    
}
