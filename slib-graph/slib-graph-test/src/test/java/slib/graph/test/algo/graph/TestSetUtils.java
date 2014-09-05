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
package slib.graph.test.algo.graph;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.openrdf.model.URI;

import slib.graph.model.graph.G;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class TestSetUtils {

    Set<URI> setA = new HashSet<URI>();
    Set<URI> setB = new HashSet<URI>();
    URIFactoryMemory data = URIFactoryMemory.getSingleton();
    G g1;
    G g2;
    URI a = data.getURI("http://#a");
    URI b = data.getURI("http://#b");
    URI c = data.getURI("http://#c");
    URI d = data.getURI("http://#d");
    URI e = data.getURI("http://#e");
    URI f = data.getURI("http://#f");
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
