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
 
 
package slib.sglib.test.algo.graph.validator.dag;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import slib.sglib.test.algo.graph.SLIB_UnitTestValues;
import slib.sglib.test.algo.graph.TestUtils;
import slib.sglib.algo.validator.dag.ValidatorDAG;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.graph.elements.EdgeTyped;
import slib.sglib.model.impl.graph.elements.VertexTyped;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author seb
 */
public class TestValidatorDAG {

	G g;
	URI rootURI;
	SLIB_UnitTestValues testValues;

	/**
     *
     * @throws SLIB_Exception
     */
    public TestValidatorDAG() throws SLIB_Exception{

		testValues = new SLIB_UnitTestValues();
		rootURI = testValues.G_BASIC_THING;

		g = TestUtils.loadTestGraph(GFormat.SGL,SLIB_UnitTestValues.G_DAG_BASIC);
	}
	
	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_dag_root() throws SLIB_Ex_Critic{

		System.out.println(g.toString());
		
		Set<V> roots = new ValidatorDAG().getTaxonomicDAGRoots(g);
		
		System.out.println("Roots: "+roots);
		assertTrue(roots.size() == 1);
		assertTrue(((URI) roots.iterator().next().getValue()).equals(testValues.G_BASIC_THING));
		
		
		V newRoot = new VertexTyped(g, g.getDataFactory().createURI("http://newURI"), VType.CLASS);
		g.addV(newRoot);
		
		roots = new ValidatorDAG().getTaxonomicDAGRoots(g);
		assertTrue(roots.size() == 2);
	}
	
	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_dag_root_2() throws SLIB_Ex_Critic{

		System.out.println(g.toString());
		
		Set<V> roots = new ValidatorDAG().getDAGRoots(g,RDFS.SUBCLASSOF,Direction.OUT);
		
		assertTrue(roots.size() == 1);
		assertTrue(((URI) roots.iterator().next().getValue()).equals(testValues.G_BASIC_THING));
	}

	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_true_dag() throws SLIB_Ex_Critic{
                V root = new VertexTyped(g, rootURI, VType.CLASS);
		boolean isDag = new ValidatorDAG().isUniqueRootedDagRoot(g, root, RDFS.SUBCLASSOF,Direction.IN);
		assertTrue(isDag);
	}
	
	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_true_tax_dag() throws SLIB_Ex_Critic{

		boolean isDag = new ValidatorDAG().containsTaxonomicalDag(g);
		
		assertTrue(isDag == true);
		
		V root = g.getV(testValues.G_BASIC_THING);
		V human = g.getV(testValues.G_BASIC_HUMAN);
		
		//create cycle
		
		E e = new EdgeTyped(root, human, RDFS.SUBCLASSOF);
		g.addE(e);
		
		isDag = new ValidatorDAG().containsTaxonomicalDag(g);
		assertTrue(isDag == false);
	}
	
	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_true_tax_dag_unique_root() throws SLIB_Ex_Critic{

		boolean isDag = new ValidatorDAG().containsRootedTaxonomicDag(g);
		
		assertTrue(isDag == true);
		
		V newRoot = new VertexTyped(g, g.getDataFactory().createURI("http://newURI"), VType.CLASS);
		g.addV(newRoot);
		
		isDag = new ValidatorDAG().containsRootedTaxonomicDag(g);
		assertTrue(isDag == false);
	}
	
	
	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_false_tax_dag_unique_root() throws SLIB_Ex_Critic{

		boolean isDag = new ValidatorDAG().containsRootedTaxonomicDag(g);
		
		assertTrue(isDag == true);
		
		URI newRootURI = DataFactoryMemory.getSingleton().createURI(SLIB_UnitTestValues.uriGraph+"new_Root");
		
		V newRoot = g.addV(new VertexTyped(g,newRootURI, VType.CLASS));
		
		g.addE(g.getV(testValues.G_BASIC_FICTIV_ORGANISM),newRoot, RDFS.SUBCLASSOF);
		g.addE(newRoot, g.getV(testValues.G_BASIC_FICTIV_ORGANISM), RDFS.SUBCLASSOF);
		
		isDag = new ValidatorDAG().isDag(g, RDFS.SUBCLASSOF,Direction.IN);
		assertTrue(isDag == false);
		
		isDag = new ValidatorDAG().containsTaxonomicDag(g);
		assertTrue(isDag == false);
		
		isDag = new ValidatorDAG().containsRootedTaxonomicDag(g);
		assertTrue(isDag == false);
	}
	
	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_false_tax_dag() throws SLIB_Ex_Critic{

		URI animalURI 	= testValues.G_BASIC_ANIMAL;
		URI menURI 		= testValues.G_BASIC_MEN;
		
		System.out.println(g.toString());

		// create a cycle
		g.addE(g.getV(animalURI), g.getV(menURI), RDFS.SUBCLASSOF);
		
		System.out.println(g.toString());
		
		boolean isDag = new ValidatorDAG().containsTaxonomicalDag(g);
		
		assertTrue(isDag == false);
	}

	/**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
	public void test_false_dag() throws SLIB_Ex_Critic{

		URI animalURI 	= testValues.G_BASIC_ANIMAL;
		URI menURI 		= testValues.G_BASIC_MEN;
		
		System.out.println(g.toString());

		// add an is-a inverse relationship between men and animal to create a cycle
		g.addE(g.getV(animalURI), g.getV(menURI), RDFS.SUBCLASSOF);
		
		System.out.println(g.toString());
		
		assertTrue(new ValidatorDAG().isDag(g, RDFS.SUBCLASSOF,Direction.IN) == false);
		
                V root = new VertexTyped(g, rootURI, VType.CLASS);
		boolean isDag = new ValidatorDAG().isUniqueRootedDagRoot(g, root, RDFS.SUBCLASSOF);
		
		assertTrue(isDag == false);
	}

}
