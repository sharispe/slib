package test;

/*
 * 
 * Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 * LGI2P research center
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 * 
 */
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_Average;
import slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_BestMatchAverage;
import slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_BestMatchMax;
import slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_Lord_2003;
import slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_Max;
import slib.sml.sm.core.measures.others.groupwise.indirect.Sim_groupwise_Min;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.MatrixDouble;

/**
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class TestGroupwiseIndirectMeasures {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    URIFactory factory;
    SM_Engine engine;
    MatrixDouble<URI, URI> matrix;
    Set<URI> setA;
    Set<URI> setB;
    URI s1_A, s1_B, s1_C, s1_D, s2_A, s2_B, s2_C;

    public TestGroupwiseIndirectMeasures() {

        logger.info("Loading required Data");

        factory = URIFactoryMemory.getSingleton();
        try {

            loadDATA();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error " + ex.getMessage());
        }
    }

    /**
     *
     * s1_A s1_B s1_C s1_D
     *
     * s2_A 0 0.5 1 0.7
     *
     * s2_B 0.2 0.3 0.6 1
     *
     * s2_C 0.5 0.8 0.9 0.1
     *
     *
     */
    private void loadDATA() {

        s1_A = factory.getURI("http://graph/s1_A");
        s1_B = factory.getURI("http://graph/s1_B");
        s1_C = factory.getURI("http://graph/s1_C");
        s1_D = factory.getURI("http://graph/s1_D");

        s2_A = factory.getURI("http://graph/s2_A");
        s2_B = factory.getURI("http://graph/s2_B");
        s2_C = factory.getURI("http://graph/s2_C");

        setA = new HashSet<URI>();
        setB = new HashSet<URI>();

        setA.add(s1_A);
        setA.add(s1_B);
        setA.add(s1_C);
        setA.add(s1_D);

        setB.add(s2_A);
        setB.add(s2_B);
        setB.add(s2_C);

        matrix = new MatrixDouble<URI, URI>(setA, setB);
        matrix.setValue(s1_A, s2_A, 0.);
        matrix.setValue(s1_A, s2_B, 0.2);
        matrix.setValue(s1_A, s2_C, 0.5);

        matrix.setValue(s1_B, s2_A, 0.5);
        matrix.setValue(s1_B, s2_B, 0.3);
        matrix.setValue(s1_B, s2_C, 0.8);

        matrix.setValue(s1_C, s2_A, 1.);
        matrix.setValue(s1_C, s2_B, 0.6);
        matrix.setValue(s1_C, s2_C, 0.9);

        matrix.setValue(s1_D, s2_A, 0.7);
        matrix.setValue(s1_D, s2_B, 1.);
        matrix.setValue(s1_D, s2_C, 0.1);
    }

    @Test
    public void testMatrix() throws SLIB_Ex_Critic {


        assertTrue(matrix.getNbColumns() == 4);
        assertTrue(matrix.getNbRows() == 3);
        assertTrue(matrix.getNbColumns() == matrix.getColumnElements().size());
        assertTrue(matrix.getNbRows() == matrix.getRowElements().size());
        assertTrue(matrix.getMax() == 1.);
        assertTrue(matrix.getMin() == 0.);
        assertTrue(matrix.isSquare() == false);
        assertTrue(matrix.getSum() == 6.6);
        assertTrue(matrix.getAverage() == 6.6 / (matrix.getNbColumns() * matrix.getNbRows()));
        assertTrue(matrix.isInColumnIndex(s1_A));
        assertTrue(matrix.isInRowIndex(s1_A) == false);
        assertTrue(matrix.getColumn(s1_A).length == matrix.getNbRows());
        assertTrue(matrix.getRow(s2_A).length == matrix.getNbColumns());
        assertTrue(matrix.getMaxColumn(s1_A) == 0.5);
        assertTrue(matrix.getMaxColumn(s1_B) == 0.8);
        assertTrue(matrix.getMaxRow(s2_B) == 1.);
        assertTrue(matrix.getColumnElements().containsAll(setA));
        assertTrue(matrix.getRowElements().containsAll(setB));
    }

    @Test
    public void testGroupwiseDirect() throws SLIB_Ex_Critic {

        assertTrue(Sim_groupwise_Average.sim(matrix) == 6.6 / (matrix.getNbColumns() * matrix.getNbRows()));
        assertTrue(Sim_groupwise_Max.sim(matrix) == 1.);
        assertTrue(Sim_groupwise_Min.sim(matrix) == 0.);
        System.out.println("BMM " + Sim_groupwise_BestMatchMax.sim(matrix));
        System.out.println("BMA " + Sim_groupwise_BestMatchAverage.sim(matrix));
        assertEquals("Average", 0.55, Sim_groupwise_Average.sim(matrix), 0.0001);
        assertEquals("Lord 2003", 0.55, Sim_groupwise_Lord_2003.compare(matrix), 0.0001);
        assertEquals("BMA", 0.8958, Sim_groupwise_BestMatchAverage.sim(matrix), 0.0001);
        assertEquals("BMM", 0.9666, Sim_groupwise_BestMatchMax.sim(matrix), 0.0001);
    }
}
