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
package slib.graph.test.algo.graph.extraction;

import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.graph.test.algo.graph.SLIB_UnitTestValues;
import slib.graph.test.algo.graph.TestUtils;
import slib.graph.algo.extraction.rvf.RVF_DAG;
import slib.graph.algo.extraction.rvf.RVF_TAX;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.utils.WalkConstraintGeneric;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class TestReachableVerticesFinder {

    public G g;
    public RVF_DAG rvf;
    SLIB_UnitTestValues testValues;

    /**
     *
     * @throws SLIB_Exception
     */
    public TestReachableVerticesFinder() throws SLIB_Exception {

        testValues = new SLIB_UnitTestValues();
        g = TestUtils.loadTestGraph(GFormat.NTRIPLES, SLIB_UnitTestValues.G_DAG_BASIC);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_descendant_1() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);
        rvf = new RVF_DAG(g, wc);

        Set<URI> desc = rvf.getRV(testValues.G_BASIC_THING);
        int sizeInter = SetUtils.intersection(desc, g.getV()).size();

        System.out.println(sizeInter + "/" + g.getV().size());

        assertTrue(sizeInter == g.getV().size() - 1);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_descendant_2() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);
        rvf = new RVF_DAG(g, wc);

        Set<URI> desc = rvf.getRV(testValues.G_BASIC_SPIDER);
        System.out.println(desc);

        assertTrue(desc.size() == 4);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_descendant_3() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);
        rvf = new RVF_DAG(g, wc);

        Set<URI> desc = rvf.getRV(testValues.G_BASIC_OBJECT);
        assertTrue(desc.size() == 6);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_descendant_4() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);
        rvf = new RVF_DAG(g, wc);

        Set<URI> desc = rvf.getRV(testValues.G_BASIC_SPIDERMAN);
        assertTrue(desc.isEmpty());
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_ancestors_1() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT);
        rvf = new RVF_DAG(g, wc);

        Set<URI> anc = rvf.getRV(testValues.G_BASIC_OBJECT);

        assertTrue(anc.size() == 1);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_ancestors_1b() throws SLIB_Ex_Critic {

        RVF_TAX rvf_loc = new RVF_TAX(g, Direction.OUT);
        Map<URI, Set<URI>> ancestorsMap = rvf_loc.getAllRVClass();

        Set<URI> anc = ancestorsMap.get(testValues.G_BASIC_OBJECT);
        System.out.println(anc.size() + "\t" + anc);

        assertTrue(anc.size() == 1);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_ancestors_2() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT);
        rvf = new RVF_DAG(g, wc);

        Set<URI> anc = rvf.getRV(testValues.G_BASIC_MEN);
        System.out.println(anc);
        assertTrue(anc.size() == 7);
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_ancestors_3() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT);
        rvf = new RVF_DAG(g, wc);

        Set<URI> anc = rvf.getRV(testValues.G_BASIC_THING);
        assertTrue(anc.isEmpty());
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_all_1() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.BOTH);
        rvf = new RVF_DAG(g, wc);

        Set<URI> all = rvf.getRV(testValues.G_BASIC_ANIMAL);

        assertTrue(all.size() == g.getV().size());
    }

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    @Test
    public void test_dag_all_2() throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.BOTH);
        rvf = new RVF_DAG(g, wc);

        Set<URI> all = rvf.getRV(testValues.G_BASIC_TABLE);

        assertTrue(all.size() == g.getV().size());
    }
}
