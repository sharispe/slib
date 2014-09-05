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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.io.plotter.GraphPlotter_Graphviz;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.tools.ontofocus.core.OntoFocus;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class TestOntoFocusReduction {

    G graph;
    URIFactory factory = URIFactoryMemory.getSingleton();
    Logger logger = LoggerFactory.getLogger(TestOntoFocusReduction.class);

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
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A22");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());

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
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A22");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 3);
        assertTrue(g.getE().size() == 2);
    }

    /**
     * Basic test, bottom-up and top-down are evaluated
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction2() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A21");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 4);
        assertTrue(g.getE().size() == 4);
    }
    
    /**
     * Basic test (bottom-up and top-down are evaluated) + force a specific node to appear
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction2a() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A21");
        Set<URI> urisToInclude = buildSetOfURIs(graph, "A222", "A1");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd,urisToInclude);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info(" TEST 2A");
        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 7);
        assertTrue(g.getE().size() == 7);
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
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A21");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


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
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A21", "A221", "A222");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 9);
        assertTrue(g.getE().size() == 11);
    }

    @Test
    public void testReduction3b() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        boolean applyTR = false;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A21", "A221", "A222");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

         logger.info("# TEST 3b");
        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


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
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A21", "A221", "A222");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 9);
        assertTrue(g.getE().size() == 11);
    }

    /**
     * Test reduction which must return the whole graph All nodes are in the
     * query, all types of relationships are included and no transitive
     * reduction
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction3d() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        boolean applyTR = false;
        Set<URI> query = buildSetOfURIs(graph, "A1", "ROOT", "A2", "A11", "A12", "A21", "A2", "A22", "A221", "A222", "A3", "A4", "A5");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);


        String gviz = GraphPlotter_Graphviz.plot(factory,g, query, true, false, null);

        System.out.println(gviz);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 12);
        assertTrue(g.getE().size() == 15);
    }

    /**
     * Test reduction which must return the whole graph All nodes are in the
     * query, only specific types of relationships are included after the
     * reduction and no transitive reduction applied
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction3e() throws SLIB_Exception {

        graph = buildGraph();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        boolean applyTR = false;
        Set<URI> query = buildSetOfURIs(graph, "A1", "ROOT", "A2", "A11", "A12", "A21", "A2", "A22", "A221", "A222", "A3", "A4", "A5");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        String gviz = GraphPlotter_Graphviz.plot(factory,g, query, true, false, null);

        System.out.println(gviz);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 12);
        assertTrue(g.getE().size() == 15);
    }

    /**
     * Test reduction in which we force some nodes to appear
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction3f() throws SLIB_Exception {
        assertTrue(true == true);
    }

    /**
     * Test multiple roots
     *
     * @throws SLIB_Exception
     */
    @Test
    public void testReduction4() throws SLIB_Exception {

        graph = buildGraph2();
        Set<URI> taxonomicPredicates = new HashSet<URI>(), predicatesToAdd = new HashSet<URI>();
        taxonomicPredicates.add(RDFS.SUBCLASSOF);
        predicatesToAdd.add(OWL.SAMEAS);
        predicatesToAdd.add(RDFS.SUBCLASSOF);
        boolean applyTR = true;
        Set<URI> query = buildSetOfURIs(graph, "A11", "A21", "A221", "A222");
        OntoFocus ontofocus = new OntoFocus(factory, graph, taxonomicPredicates, predicatesToAdd);
        G g = ontofocus.performReduction(URIFactoryMemory.getSingleton().getURI("http://reduction"), query, applyTR);

        logger.info("#V=" + g.getV().size());
        logger.info("#E=" + g.getE().size());


        assertTrue(g.getV().size() == 9);
        assertTrue(g.getE().size() == 11);
    }

    public static G buildGraph() {
        URIFactory factory = URIFactoryMemory.getSingleton();
        G g = new GraphMemory(factory.getURI("http://graph"));

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
                URIFactoryMemory.getSingleton().getURI(g.getURI() + "/" + s),
                p,
                URIFactoryMemory.getSingleton().getURI(g.getURI() + "/" + o));
    }

    private Set<URI> buildSetOfURIs(G graph, String... uriAsStrings) {
        Set<URI> uris = new HashSet<URI>();

        for (String s : uriAsStrings) {
            uris.add(factory.getURI(graph.getURI() + "/" + s));
        }
        return uris;

    }
}
