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


package slib.sgli.test.algo.graph.reduction.dag;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import slib.sgli.test.algo.graph.SGL_UnitTestValues;
import slib.sgli.test.algo.graph.TestUtils;
import slib.sglib.algo.extraction.rvf.RVF_TAX;
import slib.sglib.algo.reduction.dag.GraphReduction_Transitive;
import slib.sglib.algo.utils.RooterDAG;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.bio.obo.GraphLoader_OBO_1_2;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.repo.impl.DataRepository;
import slib.sglib.model.voc.SGLVOC;
import slib.utils.ex.SGL_Exception;

import com.tinkerpop.blueprints.Direction;

public class Test_GraphReduction_Transitive {
	
	G g;
	SGL_UnitTestValues test = new SGL_UnitTestValues();

	public Test_GraphReduction_Transitive() throws SGL_Exception{
		g = TestUtils.loadTestGraph(GFormat.SGL,SGL_UnitTestValues.G_DAG_BASIC);
	}

	@Test
	public void transitiveReductionToyGraph() throws SGL_Exception{

		System.out.println("Checking Transitive Reduction");
		Set<E> removedEdges;

		removedEdges = GraphReduction_Transitive.process(g);
		
		assertTrue(removedEdges.size() == 0);

		V a = g.getV(test.G_BASIC_SPIDERMAN);
		V b = g.getV(test.G_BASIC_ORGANISM);
		
		g.addE(a, b, RDFS.SUBCLASSOF);

		removedEdges = GraphReduction_Transitive.process(g);
		
		System.out.println("Removed Edges "+removedEdges);
		assertTrue(removedEdges.size() == 1);

		E er = null;
		for(E e : removedEdges){
			if(e.getURI().equals(RDFS.SUBCLASSOF)) 
				er = e;
		}

		assertTrue(er.getSource().equals(a) && er.getTarget().equals(b));


		g.addE(g.getV(test.G_BASIC_ORGANISM), g.getV(test.G_BASIC_THING), RDFS.SUBCLASSOF);

		removedEdges = GraphReduction_Transitive.process(g);
		System.out.println(removedEdges);
		assertTrue(removedEdges.size() == 0);// duplicate edge note allowed
	}


	@Test
	public void transitiveReductionGO() throws SGL_Exception{

		String gofilePath 	= SGL_UnitTestValues.G_GO;

		DataRepository.getSingleton().loadNamespacePrefix("GO", "http://GO#");
		GraphLoader_OBO_1_2 loader = new GraphLoader_OBO_1_2();
		
		GraphConf gconf = new GraphConf();
		gconf.setUri(SGL_UnitTestValues.uriGraph);
		GDataConf conf = new GDataConf(GFormat.OBO, gofilePath);
		gconf.addGDataConf(new GDataConf(GFormat.OBO, gofilePath));
		
		g = loader.load(gconf);

		System.out.println(g.toString());
		
		URI root_uri = DataRepository.getSingleton().createURI(SGLVOC.SGL_NS, "FICTIVE_ROOT");
		
		RooterDAG.rootUnderlyingTaxonomicDAG(g, root_uri);
		
		System.out.println(g.toString());
		
		long nbEdgesOrigin = g.getE().size();
		
		Set<E> removedEdges = GraphReduction_Transitive.process(g);
		
		long nbEdgesTR = g.getE().size();
		
		/*
		 * We check coherence between original number of edges and removed
		 */
		System.out.println("Number of edges before TR: "+nbEdgesTR);
		System.out.println("Number of edges after TR:  "+nbEdgesTR);
		
		assertTrue(nbEdgesOrigin == nbEdgesTR+removedEdges.size());
				
		// Get all ancestors 
		
		RVF_TAX rvf = new RVF_TAX( g, Direction.OUT);
		Map<V,Set<V>> ancestorsMap = rvf.getAllRVClass();
		
		// we check the root do not contains ancestors
		V root = g.getV(root_uri);
		assertTrue(ancestorsMap.get(root).size() == 1 && ancestorsMap.get(root).iterator().next().equals(root));
		
		/*
		 * We check all remove edges can be inferred
		 */
		
		boolean valid = true;
		
		for(E e : removedEdges){
			
			if(e.getURI().equals(RDFS.SUBCLASSOF)){
				
				V v = (V) e.getSource();
				V inferableAncestor = e.getTarget();
				if(! ancestorsMap.get(v).contains(inferableAncestor)){
					valid = false;
					System.out.println(e+" was removed but cannot be infered");
					System.out.println("Inferable "+ancestorsMap.get(v));
					break;
				}
			}
		}
		assertTrue(valid);
	}
}
