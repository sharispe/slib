package slib.examples.sml.general;

import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Exception;

/**
 * Snippet of code showing how to interact with the graph, i.e. add vertices, edges
 * The example also shows how to retrieve all the ancestors and descendants of a particular vertex
 * 
 * More information at http://www.semantic-measures-library.org/
 * 
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphExample {
    
    public static void main(String[] params) throws SLIB_Exception{
       
        // We use a datafactory to create some elements such as URIs
        URIFactory factory = URIFactoryMemory.getSingleton();
        
        URI uriGraph = factory.createURI("http://graph/");
        URI vA    = factory.createURI("http://graph/vA");
        URI vB    = factory.createURI("http://graph/vB");
        URI vC    = factory.createURI("http://graph/vC");
        URI vD    = factory.createURI("http://graph/vD");
        
        URI relA    = factory.createURI("http://graph/interact");
        
        // We create a graph which is loaded in memory
        G graph = new GraphMemory(uriGraph);
        
        System.out.println("- Empty Graph");
        System.out.println(graph.toString());
        
        // We create vertices and an edge
 
        E e = new Edge(vD, RDFS.SUBCLASSOF, vA);
        
        // You can therfore add a vertex or an edge
        graph.addV(vA);
        graph.addE(e);
        
        System.out.println("- Graph");
        System.out.println(graph.toString());

        // It's also possible to directly add edges without explicitly create them
        graph.addE(vB, RDFS.SUBCLASSOF,vA);
        graph.addE(vC, RDFS.SUBCLASSOF,vB);
        graph.addE(vB, relA,vD);
        
       
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
        Set<URI> ancestorsB = engine.getAncestorsInc(vB);
        Set<URI> descendantsB = engine.getDescendantsInc(vB);
        
        System.out.println("Ancestors vB:   "+ancestorsB);
        System.out.println("Descendants vB: "+descendantsB);
        
        
        // Retrieve all the vertices and edges associated to a graph
        
        Set<URI> vertices = graph.getV();
        Set<E> edges = graph.getE();
        
        System.out.println("-Vertices");
        for(URI v : vertices){
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
