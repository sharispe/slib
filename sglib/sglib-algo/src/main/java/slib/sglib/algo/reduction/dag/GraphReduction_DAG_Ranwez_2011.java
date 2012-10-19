/*

Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

This software is a computer program whose purpose is to 
process semantic graphs.

This software is governed by the CeCILL  license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL license and that you accept its terms.

 */
 
 
package slib.sglib.algo.reduction.dag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.traversal.classical.DFS;
import slib.sglib.algo.validator.dag.ValidatorDAG;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.ex.SLIB_Ex_Warning;
import slib.utils.impl.SetUtils;

import com.tinkerpop.blueprints.Direction;
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.model.repo.DataFactory;

/**
 * Algorithm used to extract a subgraph from a DAG (Directed Acyclic Graph) <br/>
 * Implementation of Ranwez et al. 2011 algorithm. <br/>
 * For in deep details please refer to the original paper: <br/>
 * 
 * ﻿Ranwez V, Ranwez S, Janaqi S: 
 * Sub-Ontology Extraction Using Hyponym and Hypernym Closure on is-a Directed Acyclic Graphs. 
 * IEEE Transactions on Knowledge and Data Engineering 2011, 99:1-14.
 * 
 * @author Sebastien Harispe
 */
public class GraphReduction_DAG_Ranwez_2011 {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	G graph;
	G graph_reduction;

	DataFactory data;

	Set<URI> selectedURI;
	V rootVertex;


	Set<V> 			 verticesRed;
	List<V> 		 traversalOrder;

	Set<URI> 	edgesTypes;
	
	Set<URI> roots;

	private Set<URI> edgesTypesDirect;
        
        /**
         * Taxonomic reduction
         * @param graph
         * @param rootURI
         * @throws SLIB_Exception 
         */
        public GraphReduction_DAG_Ranwez_2011(
                        DataFactory factory,
			G graph,
			URI rootURI
			) throws SLIB_Exception {
            
            this(factory, graph,rootURI,SetUtils.buildSet(RDFS.SUBCLASSOF),SetUtils.buildSet(RDFS.SUBCLASSOF),true);
        }


	/**
	 * Method used to perform the subGraph extraction
	 * The graph reduction is performed considering: <br/>
	 * <ul>
	 * 		<li> a collection of URI corresponding to the vertices to consider for the reduction (Query) </li>
	 * 		<li> a collection of edge types (Collection). An inverse collection (Collection_Inverse) will be build
	 * 			based on edge types inverse of the Collection. Inverse are retrieved from graph's edge types inverse mapping  
	 * 			The transitive edge types of the collection_Inverse are used to perform the topological sort which define 
	 * 			the set of vertices evaluated during the reduction.
	 * 			The reduction is the based on the transive closure considering a top-down extension of the Query using 
	 * 			Collection_Inverse and a bottom-up query extension using Collection. 
	 * 			Edges corresponding to the closure are added.  
	 * 			All directed edges of an edge type of Collection/Colloection_Inverse between a couple of vertex present in the
	 * 			reduction are added to the graph. 
	 * 		</li>
	 * <ul/>
	 * considering a , edge type to consider,  
	 * 

	 * @param graph	  		the graph
	 * @param rootURI		the node to consider as root of the graph
	 * @param selectedURI	Collection of URI corresponding to vertices of interest
	 * @param edgesTypes	Collection of edge type corresponding to edge types of interest
	 * @param validateDAGproperty boolean if true DAG property of the graph induce by the given parameters is checked
	 * @throws SGL_Exception
	 */
	public GraphReduction_DAG_Ranwez_2011(
                        DataFactory factory,
			G graph,
			URI rootURI, 
			Set<URI> edgesTypes, 
			Set<URI> edgesTypesDirect,
			boolean validateDAGproperty

			) throws SLIB_Exception {

		data = factory;

		this.edgesTypes  		= edgesTypes;
		this.edgesTypesDirect   = edgesTypesDirect;


		this.graph 		 = graph;

                if(graph.containsVertex(rootURI)){
                    rootVertex = graph.getV(rootURI);
                }
                else{
                    rootVertex = graph.createVertex(rootURI);
                }
		

		logger.debug("Selected Etypes: "+edgesTypes);

		if(graph.containsVertex(rootVertex))
			throw new SLIB_Ex_Critic("Unable to find Vertex associated to Root URI "+rootVertex.getValue());

		if(validateDAGproperty){

			logger.debug("Checking DAG property");

			ValidatorDAG vdag = new ValidatorDAG();

			boolean isDag = vdag.isDag(graph,edgesTypes, Direction.IN);// (graph, rootURI , edgesTypeInverse);

			if(!isDag)
				throw new SLIB_Ex_Critic(
						"Treatment can only be performed on a DAG, traversal "+
						"respecting your parameters define a cyclic graph.");


			// TODO REDUCE the graph

			logger.debug("DAG : "+isDag);
			
			ValidatorDAG validator = new ValidatorDAG();

			boolean uniqueRoot = validator.isUniqueRootedTaxonomicDag(graph, rootVertex);

			if(!uniqueRoot){
				logger.info("Specified root is not a unique Root: "+rootVertex.getValue());
				logger.info("Roots : "+validator.getTaxonomicDAGRoots(graph));
			}
		}
	}

	public void exec(Set<URI> selectedURI, G g_reduction) throws SLIB_Ex_Critic, SLIB_Ex_Warning {

                this.graph_reduction = g_reduction;
		this.selectedURI = selectedURI;
		
		verticesRed = new HashSet<V>();
		
		
		logger.debug("Query composed of  "+selectedURI.size()+" elements");
		logger.debug("Edges types   "+edgesTypes.size()+" elements");

		checkQueryValidity();
		computeTraversalRestriction();

		// Selection of the vertices part of the reduction

		for (URI type: edgesTypes) {
			
			Set<URI> types = SetUtils.buildSet(type);
			
//			Top Down (considering subClassOf as originally admitted eType)
			verticesRed.addAll( reduce(traversalOrder, types, Direction.OUT) );
			logger.debug("Reduction Vertices : "+verticesRed.size());
		}

		Collections.reverse(traversalOrder);
		
		for (URI type: edgesTypes) {
			
			Set<URI> types = SetUtils.buildSet(type);
			
//			Top Down (considering subClassOf as originally admitted eType)
			verticesRed.addAll( reduce(traversalOrder, types, Direction.IN) );
			logger.debug("Reduction Vertices : "+verticesRed.size());
		}
		
		// Bottom Up
		double vReductionP = 100-(verticesRed.size()*100/graph.getV().size()); //percentage of vertices reduction

		logger.debug("Reduction Vertices : "+verticesRed.size()+ " ( ~" +vReductionP+"% of "+graph.getURI()+")");
		logger.debug("Reduction : "+verticesRed);

		
		graph_reduction.addV(verticesRed);

		Collections.reverse(traversalOrder);
		for(URI e : edgesTypes){
//			if(e.isTransitive())		
				addEdges(traversalOrder, e);
		}


		// set direct edges
		if(edgesTypesDirect == null)
			edgesTypesDirect = data.getPredicateFactory().getURIs();

		logger.debug("Adding direct edges considering "+edgesTypesDirect.size()+" eType(s)");
		logger.debug(""+edgesTypesDirect);

		// Add Direct edges 
		for (V v : verticesRed) {

			Collection<E> outEdges = graph.getE(v,Direction.OUT);

			for(E e : outEdges){

				if(edgesTypesDirect.contains(e.getURI()) && 
						verticesRed.contains(e.getTarget()) &&
						!graph_reduction.containsEdge(v, e.getTarget(),Direction.OUT,e.getURI())){

					graph_reduction.addE(e.getSource(),e.getTarget(),e.getURI());
				}
			}
		}


		double eReductionP = 100-(graph_reduction.getE().size()*100/graph.getE().size()); // same for edges
		logger.debug("Reduction Edges 	 : "+verticesRed.size()+ " ( ~" +(eReductionP)+"% of "+graph.getURI()+")");
		logger.info("Reduction performed");
	}


	private void computeTraversalRestriction() {
		DFS dfs = new DFS(graph, rootVertex, edgesTypes, Direction.IN);
		traversalOrder = dfs.getTraversalOrder(); // rootID as last element
	}

	private void addEdges(List<V> traversalOrder, URI edgeType) {

		logger.debug("Adding Edges of transitive type : "+edgeType);
		logger.debug("verticesRed : "+verticesRed);
		logger.debug("Starting from  : "+traversalOrder.get(0));

		HashMap<V, Collection<V>> vrra = new HashMap<V, Collection<V>>();
		
		

		for (int i = 0; i < traversalOrder.size(); i++) {

			V u = traversalOrder.get(i);
			
//			logger.debug("Processing  : "+u);
			

			if(!vrra.containsKey(u)) // leaf or root init
				vrra.put( u, new HashSet<V>());
			
//			logger.debug("Set : "+vrra.get(u));
			
			if(verticesRed.contains(u)){

//				logger.debug(""+u.getURI().getFragment());
//				logger.debug("nb "+vrra.get(u).size());
				
				for(V r : vrra.get(u)){
					

					V source = graph_reduction.getV( (URI) u.getValue());
					V target = graph_reduction.getV( (URI) r.getValue());

					graph_reduction.addE(target, source,edgeType);
					
//					logger.debug("\tAdding edge... "+target.getURI().getFragment()+" "+edgeType.getURI().getFragment()+" "+source.getURI().getFragment());
				}
				vrra.put(u, new HashSet<V>());
				vrra.get(u).add(u);
			}
			
			Collection<E> edges = graph.getE(edgeType, u, Direction.OUT);

			for (E e : edges) {

				V f = e.getTarget();

				if(!vrra.containsKey(f))
					vrra.put( f, new HashSet<V>());

				vrra.put(f, SetUtils.union(vrra.get(u), vrra.get(f)));
				
				if( verticesRed.contains(u))
					vrra.get(f).add(u);
			}

		}
	}

	/**
	 * Retrieve the vertices selected during a reduction
	 * Used to perform top-down and bottom up expansion of the query vertices
	 * @param order  an ArrayList of IVertex vertices order to consider during the expansion first element treated first
	 * @param edgeTypes a Collection of edge type used for the expansion
	 * @return	a collection of vertices corresponding to the expansion of the query
	 */
	private Set<V> reduce(List<V> order, Set<URI> edgeTypes,Direction dir){

		logger.debug("'Transitive Closure' considering EdgeTypes : "+edgeTypes);
		logger.debug("Propogation started from : "+order.get(0));
		
		HashMap<V, HashSet<V>> sd = new HashMap<V,HashSet<V>>(traversalOrder.size()); 
		HashMap<V, Integer> maxSd = new HashMap<V, Integer>(); 


		Set<V> verticesReduction = new HashSet<V>();

		for (V v:order){
			sd.put(  v, new HashSet<V>());
			maxSd.put(v, 0);
		}

		for ( V v :order) {
			
//			System.out.println("---> "+v);

			if(selectedURI.contains( (URI) v.getValue())){
				sd.get(v).add(v);
//				System.out.println("---> Adding (QUERY) "+v);
			}
			if(sd.get(v).size() > maxSd.get(v)){
				
				
				verticesReduction.add(v);
				sd.get(v).add(v);
//				System.out.println("---> Adding "+v+"  max "+maxSd.get(v)+"  "+sd.get(v).size());
			}

			for (E e : graph.getE(edgeTypes, v,dir)) {

				V t = (V) e.getTarget();

				// check that the vertex is contained in the traversal 
				// restriction defined by the root selected 
				
//				System.out.println("\t"+e.getSource().getURI().getFragment()+"\t"+e.getType().getURI().getFragment()+"\t"+t);
				
				if(sd.containsKey(t)){
					
					HashSet<V> union = new HashSet<V>( SetUtils.union(sd.get(t),sd.get(v)) );
//					System.out.println("Union "+union);
					sd.put(t, union);
					maxSd.put(t, Math.max(sd.get(v).size(), maxSd.get(t)));
				}
			}



		}
		return verticesReduction;
	}

	private void checkQueryValidity() throws SLIB_Ex_Critic, SLIB_Ex_Warning {


		if(selectedURI == null || selectedURI.size() < 2) {
                throw new SLIB_Ex_Warning("Warning: Query skipped, a minimim of two URI have to be specified to build a query");
            }

		for (URI uri : selectedURI) {

			if(!graph.containsVertex(uri)) {
                        throw new SLIB_Ex_Warning("No vertex associated to URI: "+uri);
                    }
		}
	}

}
