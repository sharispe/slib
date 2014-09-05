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
package slib.examples.sml.general;

import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.impl.graph.elements.Edge;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
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
        
        URI uriGraph = factory.getURI("http://graph/");
        URI vA    = factory.getURI("http://graph/vA");
        URI vB    = factory.getURI("http://graph/vB");
        URI vC    = factory.getURI("http://graph/vC");
        URI vD    = factory.getURI("http://graph/vD");
        
        URI relA    = factory.getURI("http://graph/interact");
        
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
