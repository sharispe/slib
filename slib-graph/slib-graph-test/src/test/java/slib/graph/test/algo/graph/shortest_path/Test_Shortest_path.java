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
package slib.graph.test.algo.graph.shortest_path;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.graph.algo.shortest_path.Dijkstra;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.Direction;
import slib.graph.test.algo.graph.SLIB_UnitTestValues;
import slib.graph.test.algo.graph.TestUtils;
import slib.graph.utils.WalkConstraintGeneric;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Test_Shortest_path {

    G g;
    SLIB_UnitTestValues test = new SLIB_UnitTestValues();

    /**
     *
     * @throws SLIB_Exception
     */
    public Test_Shortest_path() throws SLIB_Exception {
        g = TestUtils.loadTestGraph(GFormat.NTRIPLES, SLIB_UnitTestValues.G_DAG_BASIC);
    }

    /**
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testSP_0() throws SLIB_Exception {

        Dijkstra d = new Dijkstra(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT));
        SLIB_UnitTestValues testValues = new SLIB_UnitTestValues();

        URI thing = testValues.G_BASIC_THING;
        URI spiderman = test.G_BASIC_SPIDERMAN;
        URI animal = test.G_BASIC_ANIMAL;
        Double sp = d.shortestPath(thing, thing);
        assertTrue(sp == 0.);

        System.out.println("Shortest Path Thing/Thing = 0, found " + sp);
        sp = d.shortestPath(thing, thing);
        assertTrue(sp == 0.);


        sp = d.shortestPath(spiderman, thing);
        System.out.println("Shortest Path Spiderman/Thing = 3, found " + sp);
        assertTrue(sp == 3);


        sp = d.shortestPath(thing, spiderman);
        System.out.println("Shortest Path Thing/Spiderman = null, found " + sp);
        assertTrue(sp == Dijkstra.NOT_COMPUTED.doubleValue());
        
        sp = d.shortestPath(spiderman, animal);
        System.out.println("Shortest Path Spiderman/Animal = 2, found " + sp);
        assertTrue(sp == 2);
        
        sp = d.shortestPath(animal, thing);
        System.out.println("Shortest Path Animal/Thing = 3, found " + sp);
        assertTrue(sp == 3);
    }
    
    
    @Test
    public void testSP_1() throws SLIB_Exception {

        Dijkstra d = new Dijkstra(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN));
        SLIB_UnitTestValues testValues = new SLIB_UnitTestValues();

        URI thing = testValues.G_BASIC_THING;
        URI spiderman = test.G_BASIC_SPIDERMAN;
        URI animal = test.G_BASIC_ANIMAL;
        Double sp = d.shortestPath(thing, thing);
        assertTrue(sp == 0.);

        System.out.println("Shortest Path Thing/Thing = 0, found " + sp);
        sp = d.shortestPath(thing, thing);
        assertTrue(sp == 0.);


        sp = d.shortestPath(spiderman, thing);
        System.out.println("Shortest Path Spiderman/Thing = null, found " + sp);
        assertTrue(sp == Dijkstra.NOT_COMPUTED.doubleValue());


        sp = d.shortestPath(thing, spiderman);
        System.out.println("Shortest Path Thing/Spiderman = 3, found " + sp);
        assertTrue(sp == 3);
        
        sp = d.shortestPath(spiderman, animal);
        System.out.println("Shortest Path Spiderman/Animal = null, found " + sp);
        assertTrue(sp == Dijkstra.NOT_COMPUTED.doubleValue());
        
        sp = d.shortestPath(animal, spiderman);
        System.out.println("Shortest Path Animal/Spiderman = 2, found " + sp);
        assertTrue(sp == 2);
    }
    
    @Test
    public void testSP_2() throws SLIB_Exception {

        Dijkstra d = new Dijkstra(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.BOTH));
        SLIB_UnitTestValues testValues = new SLIB_UnitTestValues();

        URI thing = testValues.G_BASIC_THING;
        URI spiderman = test.G_BASIC_SPIDERMAN;
        URI animal = test.G_BASIC_ANIMAL;
        Double sp = d.shortestPath(thing, thing);
        assertTrue(sp == 0.);

        System.out.println("Shortest Path Thing/Thing = 0, found " + sp);
        sp = d.shortestPath(thing, thing);
        assertTrue(sp == 0.);


        sp = d.shortestPath(spiderman, thing);
        System.out.println("Shortest Path Spiderman/Thing = 3, found " + sp);
        assertTrue(sp == 3);


        sp = d.shortestPath(thing, spiderman);
        System.out.println("Shortest Path Thing/Spiderman = 3, found " + sp);
        assertTrue(sp == 3);
        
        sp = d.shortestPath(spiderman, animal);
        System.out.println("Shortest Path Spiderman/Animal = 2, found " + sp);
        assertTrue(sp == 2);
        
        sp = d.shortestPath(animal, spiderman);
        System.out.println("Shortest Path Animal/Spiderman = 2, found " + sp);
        assertTrue(sp == 2);
    }
}
