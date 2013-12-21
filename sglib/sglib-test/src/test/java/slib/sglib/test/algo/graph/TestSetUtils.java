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
package slib.sglib.test.algo.graph;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;

import slib.sglib.model.graph.G;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 *
 * @author seb
 */
public class TestSetUtils {

    Set<URI> setA = new HashSet<URI>();
    Set<URI> setB = new HashSet<URI>();
    URIFactoryMemory data = URIFactoryMemory.getSingleton();
    G g1;
    G g2;
    URI a = data.createURI("http://#a");
    URI b = data.createURI("http://#b");
    URI c = data.createURI("http://#c");
    URI d = data.createURI("http://#d");
    URI e = data.createURI("http://#e");
    URI f = data.createURI("http://#f");
    int c1_intersection = 0;
    int c1_union = 1;
    int c2_intersection = 0;
    int c2_union = 6;

    /**
     *
     * @throws SLIB_Ex_Critic
     */
    public TestSetUtils() throws SLIB_Ex_Critic {
    }

    private void populate_c1() {
        setA = new HashSet<URI>();
        setB = new HashSet<URI>();
        setA.add(a);
    }

    private void populate_c2() {
        setA = new HashSet<URI>();
        setB = new HashSet<URI>();
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

    /**
     *
     */
    @Test
    public void c2_intersection() {
        populate_c2();
        assertTrue(SetUtils.intersection(setA, setB).size() == c2_intersection);
    }

    /**
     *
     */
    @Test
    public void c2_union() {
        populate_c2();
        assertTrue(SetUtils.union(setA, setB).size() == c2_union);
    }

    /**
     *
     */
    @Test
    public void c1_intersection() {
        populate_c1();
        assertTrue(SetUtils.intersection(setA, setB).size() == c1_intersection);
    }

    /**
     *
     */
    @Test
    public void c1_union() {
        populate_c1();
        assertTrue(SetUtils.union(setA, setB).size() == c1_union);
    }

    /**
     *
     */
    @Test
    public void emptySetsIntersection() {
        assertTrue(SetUtils.intersection(setA, setB).size() == 0);
    }

    /**
     *
     */
    @Test
    public void emptySetsUnion() {
        assertTrue(SetUtils.union(setA, setB).size() == 0);
    }
}
