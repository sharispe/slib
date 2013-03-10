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
package slib.sglib.test.algo.graph.metric;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.algo.graph.metric.DepthAnalyserAG;
import slib.sglib.algo.graph.utils.WalkConstraintTax;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.sglib.test.algo.graph.SLIB_UnitTestValues;
import slib.sglib.test.algo.graph.TestUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;

/**
 *
 * @author seb
 */
public class TestDepthAnalyser {

    G g;
    DepthAnalyserAG depthAnalyser = null;
    DataFactoryMemory df = DataFactoryMemory.getSingleton();
    int min_depth_spiderman = 3;
    int min_depth_real_organism = 2;
    int min_depth_plant = 3;
    int min_depth_women = 7;
    int min_depth_thing = 0;
    int max_depth_spiderman = 8;
    int max_depth_real_organism = 2;
    int max_depth_plant = 3;
    int max_depth_women = 7;
    int max_depth_thing = 0;
    SLIB_UnitTestValues testvalues;
    WalkConstraintTax wc = new WalkConstraintTax(RDFS.SUBCLASSOF, Direction.IN);

    /**
     *
     * @throws SLIB_Exception
     */
    public TestDepthAnalyser() {
        try {
            testvalues = new SLIB_UnitTestValues();
            g = TestUtils.loadTestGraph(GFormat.SGL, SLIB_UnitTestValues.G_DAG_BASIC);
            depthAnalyser = new DepthAnalyserAG(df, g, wc);

        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(TestDepthAnalyser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @throws SLIB_Exception
     */
    @Test
    public void test_max_depth() throws SLIB_Exception {

        ResultStack<V, Integer> maxDepths = depthAnalyser.getVMaxDepths();

        assertTrue(max_depth_spiderman == maxDepths.get(g.getV(testvalues.G_BASIC_SPIDERMAN)));
        assertTrue(max_depth_real_organism == maxDepths.get(g.getV(testvalues.G_BASIC_REAL_ORGANISM)));
        assertTrue(max_depth_thing == maxDepths.get(g.getV(testvalues.G_BASIC_THING)));
        assertTrue(max_depth_plant == maxDepths.get(g.getV(testvalues.G_BASIC_PLANT)));
        assertTrue(max_depth_women == maxDepths.get(g.getV(testvalues.G_BASIC_WOMEN)));
    }

    /**
     *
     * @throws SLIB_Exception
     */
    @Test
    public void test_min_depth() throws SLIB_Exception {

        ResultStack<V, Integer> minDepths = depthAnalyser.getVMinDepths();



        assertTrue(min_depth_spiderman == minDepths.get(g.getV(testvalues.G_BASIC_SPIDERMAN)));
        assertTrue(min_depth_real_organism == minDepths.get(g.getV(testvalues.G_BASIC_REAL_ORGANISM)));
        assertTrue(min_depth_thing == minDepths.get(g.getV(testvalues.G_BASIC_THING)));
        assertTrue(min_depth_plant == minDepths.get(g.getV(testvalues.G_BASIC_PLANT)));
        assertTrue(min_depth_women == minDepths.get(g.getV(testvalues.G_BASIC_WOMEN)));
    }
}
