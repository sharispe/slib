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
package slib.graph.test.algo.graph.validator.dag;

import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;

import slib.graph.test.algo.graph.SLIB_UnitTestValues;
import slib.graph.test.algo.graph.TestUtils;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.utils.WalkConstraintGeneric;
import slib.graph.model.impl.graph.elements.Edge;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class TestValidatorDAG {

    G g;
    URI rootURI;
    SLIB_UnitTestValues testValues;

    /**
     *
     * @throws SLIB_Exception
     */
    public TestValidatorDAG() throws SLIB_Exception {

        testValues = new SLIB_UnitTestValues();
        rootURI = testValues.G_BASIC_THING;

        g = TestUtils.loadTestGraph(GFormat.NTRIPLES, SLIB_UnitTestValues.G_DAG_BASIC);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_root() throws SLIB_Ex_Critic {

        System.out.println(g.toString());

        Set<URI> roots = new ValidatorDAG().getTaxonomicRoots(g);

        System.out.println("Roots: " + roots);
        assertTrue(roots.size() == 1);
        assertTrue(roots.iterator().next().equals(testValues.G_BASIC_THING));


        URI newRoot = URIFactoryMemory.getSingleton().getURI("http://newRoot");
        g.addE(testValues.G_BASIC_ANIMAL,RDFS.SUBCLASSOF,newRoot);

        roots = new ValidatorDAG().getTaxonomicRoots(g);
        System.out.println("Roots "+roots);
        assertTrue(roots.size() == 2);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_root_2() throws SLIB_Ex_Critic {

        System.out.println(g.toString());

        Set<URI> roots = new ValidatorDAG().getDAGRoots(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT));

        assertTrue(roots.size() == 1);
        assertTrue(roots.iterator().next().equals(testValues.G_BASIC_THING));
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_true_dag() throws SLIB_Ex_Critic {
        boolean isDag = new ValidatorDAG().isUniqueRootedDagRoot(g, rootURI, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN));
        assertTrue(isDag);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_true_tax_dag() throws SLIB_Ex_Critic {

        boolean isDag = new ValidatorDAG().containsTaxonomicDag(g);

        assertTrue(isDag == true);

        E e = new Edge(testValues.G_BASIC_THING, RDFS.SUBCLASSOF, testValues.G_BASIC_HUMAN);
        g.addE(e);

        isDag = new ValidatorDAG().containsTaxonomicDag(g);
        assertTrue(isDag == false);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_true_tax_dag_unique_root() throws SLIB_Ex_Critic {

        boolean isDag = new ValidatorDAG().containsTaxonomicDagWithUniqueRoot(g);

        assertTrue(isDag == true);

        // We create another root
        g.addE(testValues.G_BASIC_HUMAN,RDFS.SUBCLASSOF,URIFactoryMemory.getSingleton().getURI("http://newURI"));

        isDag = new ValidatorDAG().containsTaxonomicDagWithUniqueRoot(g);
        assertTrue(isDag == false);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_false_tax_dag_unique_root() throws SLIB_Ex_Critic {

        boolean isDag = new ValidatorDAG().containsTaxonomicDagWithUniqueRoot(g);

        assertTrue(isDag == true);

        URI newRootURI = URIFactoryMemory.getSingleton().getURI(SLIB_UnitTestValues.uriGraph + "new_Root");

        g.addV(newRootURI);

        g.addE(testValues.G_BASIC_FICTIV_ORGANISM, RDFS.SUBCLASSOF, newRootURI);
        g.addE(newRootURI, RDFS.SUBCLASSOF, testValues.G_BASIC_FICTIV_ORGANISM);

        isDag = new ValidatorDAG().isDag(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN));
        assertTrue(isDag == false);

        isDag = new ValidatorDAG().containsTaxonomicDag(g);
        assertTrue(isDag == false);

        isDag = new ValidatorDAG().containsTaxonomicDagWithUniqueRoot(g);
        assertTrue(isDag == false);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_false_tax_dag() throws SLIB_Ex_Critic {

        URI animalURI = testValues.G_BASIC_ANIMAL;
        URI menURI = testValues.G_BASIC_MEN;

        System.out.println(g.toString());

        // create a cycle
        g.addE(animalURI, RDFS.SUBCLASSOF, menURI);

        System.out.println(g.toString());

        boolean isDag = new ValidatorDAG().containsTaxonomicDag(g);

        assertTrue(isDag == false);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_false_dag() throws SLIB_Ex_Critic {

        URI animalURI = testValues.G_BASIC_ANIMAL;
        URI menURI = testValues.G_BASIC_MEN;

        System.out.println(g.toString());

        // add an is-a inverse relationship between men and animal to create a cycle
        g.addE(animalURI, RDFS.SUBCLASSOF, menURI);

        System.out.println(g.toString());

        assertTrue(new ValidatorDAG().isDag(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN)) == false);


        boolean isDag = new ValidatorDAG().isUniqueRootedDagRoot(g, rootURI, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT));

        assertTrue(isDag == false);
    }
}
