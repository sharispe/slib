/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.tools.ontofocus;

import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.tools.ontofocus.core.OntoFocus;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe
 */
public class TestOntoFocusReduction {

    G graph;
    URIFactory factory = URIFactoryMemory.getSingleton();

    public TestOntoFocusReduction() {
    }

    /**
     * Basic test (only the bottom up is evaluated)
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction1() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, true);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A22"));


        assertTrue(g.getV().size() == 3);
        assertTrue(g.getE().size() == 2);
    }

    /**
     * Basic test (only the bottom up is evaluated)
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction1b() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, true);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A12"));


        assertTrue(g.getV().size() == 3);
        assertTrue(g.getE().size() == 2);
    }

    /**
     * Basic test (bottom up/top down is evaluated
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction2() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, true);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A21"));


        assertTrue(g.getV().size() == 4);
        assertTrue(g.getE().size() == 4);
    }
    

    /**
     * Test with inclusion of a non taxonomic relationship involving two nodes
     * selected by the reduction process
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction2b() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, true);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A21"));


        assertTrue(g.getV().size() == 4);
        assertTrue(g.getE().size() == 5);
    }

    /**
     * Test with inclusion of a non taxonomic relationship involving two nodes
     * selected by the reduction process
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction3() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, true);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A21", "A221", "A222"));


        assertTrue(g.getV().size() == 9);
        assertTrue(g.getE().size() == 11);
    }

    @Test
    public void testReduction3b() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, false);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A21", "A221", "A222"));


        assertTrue(g.getV().size() == 9);
        assertTrue(g.getE().size() == 12);
    }

    /**
     * Test with inclusion of a non taxonomic relationship involving two nodes
     * selected by the reduction process + transitive reduction
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction3c() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, true);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A21", "A221", "A222"));


        assertTrue(g.getV().size() == 9);
        assertTrue(g.getE().size() == 11);
    }

    /**
     * Test multiple roots
     * @throws SLIB_Exception 
     */
    @Test
    public void testReduction4() throws SLIB_Exception {

        graph = buildGraph2();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd, null, true);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().createURI("http://reduction"), buildSetOfURIs(graph, "A11", "A21", "A221", "A222"));


        assertTrue(g.getV().size() == 9);
        assertTrue(g.getE().size() == 11);
    }

    public static G buildGraph() {
        URIFactory factory = URIFactoryMemory.getSingleton();
        G g = new GraphMemory(factory.createURI("http://graph"));

        addE(g, "A1", RDFS.SUBCLASSOF, "ROOT");
        addE(g, "A2", RDFS.SUBCLASSOF, "ROOT");
        addE(g, "A11", RDFS.SUBCLASSOF, "A1");
        addE(g, "A12", RDFS.SUBCLASSOF, "A1");
        addE(g, "A21", RDFS.SUBCLASSOF, "A2");
        addE(g, "A22", RDFS.SUBCLASSOF, "A2");
        addE(g, "A221", RDFS.SUBCLASSOF, "A22");
        addE(g, "A222", RDFS.SUBCLASSOF, "A22");
        addE(g, "A3", RDFS.SUBCLASSOF, "A221");
        addE(g, "A3", RDFS.SUBCLASSOF, "A222");
        addE(g, "A3", RDFS.SUBCLASSOF, "ROOT");
        addE(g, "A4", RDFS.SUBCLASSOF, "A11");
        addE(g, "A4", RDFS.SUBCLASSOF, "A21");
        addE(g, "A5", RDFS.SUBCLASSOF, "A4");
        addE(g, "A11", OWL.SAMEAS, "A21"); // makes no sense, just for the test
        return g;
    }

    public static G buildGraph2() {

        G g = buildGraph();

        addE(g, "A1", RDFS.SUBCLASSOF, "ROOT2");
        return g;
    }

    private static void addE(G g, String s, URI p, String o) {
        g.addE(
                URIFactoryMemory.getSingleton().createURI(g.getURI() + "/" + s),
                p,
                URIFactoryMemory.getSingleton().createURI(g.getURI() + "/" + o));
    }

    private Set<URI> buildSetOfURIs(G graph, String... uriAsStrings) {
        Set<URI> uris = new HashSet<URI>();

        for (String s : uriAsStrings) {
            uris.add(factory.createURI(graph.getURI() + "/" + s));
        }
        return uris;

    }
}
