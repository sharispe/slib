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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.extraction.rvf.RVF_TAX;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

import com.tinkerpop.blueprints.Direction;

/**
 * Class used to build or perform a reduction of a DAG.
 * 
 * @author Sebastien Harispe
 *
 */
public class GraphReduction_DAG {
	
	static Logger logger = LoggerFactory.getLogger(GraphReduction_DAG.class);


	/**
	 * Depracated Javadoc
	 * 
	 * Perform the reduction of a specified acyclic graph considering a given a walk configuration
	 * i.e. starting node and relationships to consider during the walk. 
	 * Note that the reduction is not performed on the graph given as base.
	 * However the process can also be performed in place i.e. on the input graph if the URI of
	 * the graphReduction is the same as the input graph URI. <br/><br/>
	 * 
	 * First the vertices reachable from the given source are computed.
	 * Then, edges from which the source and the target are are in the reachable vertices are added.
	 * A restriction on the edges added to the reduction can also be made using onlyAddGivenEtypes.
	 * In this case only edge types given as input (and their respective inverses) will be added. <br/><br/>
	 *  
	 * @param g the graph from which the corresponding reduction must be build
	 * @param rootURI The URI of the vertex from where the reduction is started (root)
	 * @param graphReductionURI The URI of the resulting graph. Can be equals to the URI of g.
	 * @param onlyAddGivenEtypes only edge types given as input (and their respective inverses) will be added.
	 * @return the graph which corresponds to the reduction
	 * 
	 * @throws SGL_Ex_Critic
	 */
	public static void reduction(G g,URI rootURI, final Set<URI> edgeTypes, Direction dir, boolean onlyAddGivenEtypes) throws SLIB_Ex_Critic{

		logger.info("Reduction");

		V root = g.getV(rootURI);

		if(root == null)
			throw new SLIB_Ex_Critic("Cannot resolve specified root "+rootURI);

		RVF_TAX rvf = new RVF_TAX(g,Direction.IN);

		// Get all classes reachable from the given root
		Set<V> vertices = rvf.getRVClass(root);
		
		logger.debug("RVF (classes): "+vertices.size());
		logger.debug("Extends reduction to linked vertices");
		
		Set<V> verticesExtended = new HashSet<V>();
		// Get all nodes linked to the classes selected which are not classes
		for(V v : vertices){
			
			for(E e: g.getE(v, Direction.IN)){
				if(e.getSource().getType() != VType.CLASS)
					verticesExtended.add(e.getSource());
			}
			
			for(E e: g.getE(v, Direction.OUT)){
				if(e.getTarget().getType() != VType.CLASS)
					verticesExtended.add(e.getTarget());
			}
			
		}
		
		vertices.addAll(verticesExtended);
		verticesExtended = null;
		
		logger.info("Reduction will contain "+vertices.size()+" vertices");
		logger.debug("performing reduction");

		ArrayList<V> verticesToRemove = new ArrayList<V>();

		for(V v : g.getV()){
			if(!vertices.contains(v))
				verticesToRemove.add(v);
		}
		
		for(V v : verticesToRemove)
			g.removeV(v);

		logger.info("End Taxonomic reduction");
	}



	/**
	 * Perform a taxonomic reduction considering a walk oriented by SUPERCLASSOF relationships.<br/>
	 * Please see {@link GraphReduction_DAG#reduction(G, URI, URI, Set, boolean)} for further information.
	 * 
	 * @param g the graph from which the corresponding reduction must be build
	 * @param rootURI The URI of the vertex from where the reduction is started (root)
	 * @param graphReductionURI The URI of the resulting graph. Can be equals to the URI of g.
	 * @param onlyAddTaxonomical if true only SUPERCLASSOF and SUBCLASSOF edge types will be added else all relationships are added.
	 * @return the graph which corresponds to the reduction
	 * @throws SGL_Ex_Critic
	 */
	public static void taxonomicReduction(G g,URI rootURI, boolean onlyAddTaxonomical) throws SLIB_Ex_Critic{

		reduction(g, rootURI, SetUtils.buildSet(RDFS.SUBCLASSOF),Direction.IN, onlyAddTaxonomical);
	}

}
