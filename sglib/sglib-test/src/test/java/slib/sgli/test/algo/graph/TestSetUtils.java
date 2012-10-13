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
 
 
package slib.sgli.test.algo.graph;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.impl.VertexTyped;
import slib.sglib.model.repo.impl.DataRepository;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

public class TestSetUtils {


	Set<V> setA = new HashSet<V>();
	Set<V> setB = new HashSet<V>();

	DataRepository data = DataRepository.getSingleton();

	G g1;
	G g2;
	
	V a = new VertexTyped(null, data.createURI("http://#a"), null); 
	V b = new VertexTyped(null, data.createURI("http://#b"), null);
	V c = new VertexTyped(null, data.createURI("http://#c"), null); 
	V d = new VertexTyped(null, data.createURI("http://#d"), null); 
	V e = new VertexTyped(null, data.createURI("http://#e"), null); 
	V f = new VertexTyped(null, data.createURI("http://#f"), null); 

	int c1_intersection = 0;
	int c1_union		= 1;

	int c2_intersection = 0;
	int c2_union		= 6;

	public TestSetUtils() throws SLIB_Ex_Critic{

	}

	private void populate_c1(){
		setA = new HashSet<V>();
		setB = new HashSet<V>();
		setA.add(a);
	}

	private void populate_c2(){
		setA = new HashSet<V>();
		setB = new HashSet<V>();		
		setA.add(a);
		setA.add(b);
		setA.add(c);
		setA.add(d);

		setA.add(b);
		setA.add(c);
		setA.add(d);
		setA.add(e);
		setA.add(f);
	}


//	@Test
//	public void set_operation_on_large_sets() throws SLIB_Exception{
//
//		String path = System.getProperty("user.dir")+"/data/test/graph/obo/";
//		String go 	= path+"gene_ontology_ext.obo";
//
//		String filePath = go;
//
//		GraphConf conf  = new GraphConf(SLIB_UnitTestValues.uriGraph,GraphFileFormats.OBO, filePath);
//		GraphConf conf2 = new GraphConf(SLIB_UnitTestValues.uriGraph,GraphFileFormats.OBO, filePath);
//
//		g1 = GraphLoaderGeneric.load(conf);
//		g2 = GraphLoaderGeneric.load(conf2);
//
//		setA = g1.getVertices();
//		setB = g2.getVertices();
//
//
//		Random r = new Random();
//		int todel = r.nextInt(setA.size());
//
//		delSetAElem(todel);
//
//		assertTrue(SetUtils.union(setA, setB).size() == setB.size());
//
//
//		setA = g1.getVertices();
//		setB = g2.getVertices();
//
//
//		todel = r.nextInt(setA.size());
//		delSetAElem(todel);
//		assertTrue(SetUtils.intersection(setA, setB).size() == setA.size());
//
//		setA = g1.getVertices();
//		setB = g2.getVertices();
//
//
//		todel = r.nextInt(setA.size());
//		delSetAElem(todel);
//		assertTrue(SetUtils.intersection(setA, setB).size() == g1.getVertices().size());
//
//		setA = g1.getVertices();
//		setB = g2.getVertices();
//
//
//		todel = r.nextInt(setA.size());
//		delSetAElem(todel);
//		assertTrue(SetUtils.union(setA, setB).size() == g1.getVertices().size());
//
//
//	}
//
//	private void delSetAElem(int todel) {
//		Iterator<V> it = setA.iterator();
//		HashSet<V> newSet = new HashSet<V>();
//
//		while(it.hasNext()){
//			if(todel > 0)
//				newSet.add(it.next());
//			todel--;
//		}
//		setA = newSet;
//	}



	@Test
	public void c2_intersection(){
		populate_c2();
		assertTrue(SetUtils.intersection(setA, setB).size() == c2_intersection);
	}

	@Test
	public void c2_union(){
		populate_c2();
		assertTrue(SetUtils.union(setA, setB).size() == c2_union);
	}

	@Test
	public void c1_intersection(){
		populate_c1();
		assertTrue(SetUtils.intersection(setA, setB).size() == c1_intersection);
	}

	@Test
	public void c1_union(){
		populate_c1();
		assertTrue(SetUtils.union(setA, setB).size() == c1_union);
	}

	@Test
	public void emptySetsIntersection(){
		assertTrue(SetUtils.intersection(setA, setB).size() == 0);
	}

	@Test
	public void emptySetsUnion(){
		assertTrue(SetUtils.union(setA, setB).size() == 0);
	}


}
