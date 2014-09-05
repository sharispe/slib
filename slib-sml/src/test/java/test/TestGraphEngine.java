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
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.slibformat.GraphLoader_SLIB;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class TestGraphEngine {

    public static final String path = System.getProperty("user.dir") + "/src/test/resources/";
    public static final String expectedValueFile = path + "graph_test_expected_values.csv";
    public static final String graphFile = path + "graph_test.slib";
    public static final String uriGraphTest = "http://graph/";
    Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<URI, Set<URI>> ancestors = new HashMap<URI, Set<URI>>();
    Map<URI, Set<URI>> descendants = new HashMap<URI, Set<URI>>();
    Map<URI, Set<URI>> leaves = new HashMap<URI, Set<URI>>();
    Map<String, Map<URI, Double>> informationContents = new HashMap<String, Map<URI, Double>>();
    URIFactory factory;
    SM_Engine engine;
    G graph;
    String SANCHEZ_IC = "SANCHEZ_IC";

    public TestGraphEngine() {

        logger.info("Loading required Data");

        factory = URIFactoryMemory.getSingleton();
        try {


            GraphLoader_SLIB loader = new GraphLoader_SLIB();
            graph = new GraphMemory(factory.getURI(uriGraphTest));
            loader.populate(new GDataConf(GFormat.SLIB, graphFile), graph);
            engine = new SM_Engine(graph);

            loadExpectedValues();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error " + ex.getMessage());
        }
    }

    private void loadExpectedValues() throws Exception {


        informationContents.put(SANCHEZ_IC, new HashMap<URI, Double>());

        FileInputStream fstream = new FileInputStream(expectedValueFile);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        String l;
        boolean header = true;

        int COL_CONCEPT_NS = 0;
        int COL_ANCESTORS_COUNT = 1;
        int COL_ANCESTORS = 2;
        int COL_DESCENDANTS_COUNT = 3;
        int COL_DESCENDANTS = 4;
        int COL_LEAVES_COUNT = 5;
        int COL_LEAVES = 6;
        int COL_SANCHEZ_IC = 8;

        while ((l = br.readLine()) != null) {
            if (header) {
                header = false;
                continue;
            }
            l = l.trim();

            if (l.startsWith("#")) {
                continue;
            }

            String[] data = l.split("\t");
            String nsConcept = data[COL_CONCEPT_NS];

            URI cURI = factory.getURI(uriGraphTest + nsConcept);

            int ancCount = Integer.parseInt(data[COL_ANCESTORS_COUNT]);
            String[] ancNSStrings = data[COL_ANCESTORS].split(";");

            if (ancCount != ancNSStrings.length) {
                throw new SLIB_Ex_Critic("Incoherence found in ancestors/count ancestors of " + cURI + " expected " + ancCount + " found " + ancNSStrings.length + " " + Arrays.toString(ancNSStrings));
            }

            Set<URI> ancSet = new HashSet<URI>();
            for (String s : ancNSStrings) {
                ancSet.add(factory.getURI(uriGraphTest + s));
            }
            ancestors.put(cURI, ancSet);

            int descCount = Integer.parseInt(data[COL_DESCENDANTS_COUNT]);
            String[] descNSStrings = data[COL_DESCENDANTS].split(";");

            if (descCount != descNSStrings.length) {
                throw new SLIB_Ex_Critic("Incoherence found in descendants/count descendants of " + cURI);
            }

            Set<URI> descSet = new HashSet<URI>();
            for (String s : descNSStrings) {
                descSet.add(factory.getURI(uriGraphTest + s));
            }
            descendants.put(cURI, descSet);

            int leavesCount = Integer.parseInt(data[COL_LEAVES_COUNT]);
            String[] leavesNSStrings = data[COL_LEAVES].split(";");

            Set<URI> leavesSet = new HashSet<URI>();
            for (String s : leavesNSStrings) {
                leavesSet.add(factory.getURI(uriGraphTest + s));
            }
            leaves.put(cURI, leavesSet);

            if (leavesCount != leavesNSStrings.length) {
                throw new SLIB_Ex_Critic("Incoherence found in leaves/count leaves of " + cURI);
            }

            double icSanchez = Double.parseDouble(data[COL_SANCHEZ_IC]);
            informationContents.get(SANCHEZ_IC).put(cURI, icSanchez);
        }
        in.close();


    }

    @Test
    public void test_ancestors() throws SLIB_Ex_Critic {


        for (URI v : graph.getV()) {
            Set<URI> ancsExpected = ancestors.get(v);
            Set<URI> ancs = engine.getAncestorsInc(v);
            assertTrue(ancsExpected.size() == ancs.size());



            logger.info("Expected :" + ancsExpected);
            logger.info("Found    :" + ancs);
            ancsExpected.removeAll(ancs);
            logger.info("Size ANC " + ancsExpected.size() + " expected 0");
            assertTrue(ancsExpected.isEmpty());
        }
    }

    @Test
    public void test_descendants() throws SLIB_Ex_Critic {


        for (URI v : graph.getV()) {
            Set<URI> descsExpected = descendants.get(v);
            Set<URI> descs = engine.getDescendantsInc(v);
            assertTrue(descsExpected.size() == descs.size());



            logger.info("Expected :" + descsExpected);
            logger.info("Found    :" + descs);
            descsExpected.removeAll(descs);
            logger.info("Size DESC " + descsExpected.size() + " expected 0");
            assertTrue(descsExpected.isEmpty());
        }
    }

    @Test
    public void test_leaves() throws SLIB_Ex_Critic {

        Map<URI, Set<URI>> leavesFound = engine.getReachableLeaves();

        for (URI v : graph.getV()) {


            Set<URI> leavesURIsExpected = leaves.get(v);
            assertTrue(leavesURIsExpected.size() == leavesFound.get(v).size());


            Set<URI> leavesURIsFound = leavesFound.get(v);


            logger.info("Expected :" + leavesURIsExpected);
            logger.info("Found    :" + leavesURIsFound);
            leavesURIsExpected.removeAll(leavesURIsFound);
            logger.info("Size DESC " + leavesURIsExpected.size() + " expected 0");
            assertTrue(leavesURIsExpected.isEmpty());
        }
    }

    @Test
    public void test_leaves_count() throws SLIB_Ex_Critic {

        Map<URI, Integer> reachableLeaves = engine.getAllNbReachableLeaves();

        for (URI v : graph.getV()) {
            Set<URI> leavesURIsExpected = leaves.get(v);
            assertTrue(leavesURIsExpected.size() == reachableLeaves.get(v));
        }
    }

    @Test
    public void test_ancestor_count() throws SLIB_Ex_Critic {

        Map<URI, Integer> nbAncestors = engine.getAllNbAncestorsInc();

        for (URI v : graph.getV()) {
            Set<URI> ancestorsURIsExpected = ancestors.get(v);
            System.out.println(v);
            System.out.println("Exp "+ancestorsURIsExpected.size() + " == " + nbAncestors.get(v));
            assertTrue(ancestorsURIsExpected.size() == nbAncestors.get(v));
        }
    }

    @Test
    public void test_retrieveLeaves() throws SLIB_Ex_Critic {

        Set<URI> leavesGraph = engine.getTaxonomicLeaves();

        //logger.info("" + leavesGraph.size() + "\t" + leavesGraph);
        //TODO check equality
        assertTrue(leavesGraph.size() == 7);
    }

    /**
     * TODO Change log base in expected results ... modified from e to 2
     *
     * @throws SLIB_Ex_Critic
     */
    //@Test
    public void test_IC_Sanchez() throws SLIB_Ex_Critic {

        Map<URI, Double> icSanchez = engine.computeIC(new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011));

        for (Entry<URI, Double> e : informationContents.get(SANCHEZ_IC).entrySet()) {

            URI v = e.getKey();

            Double icExpected = e.getValue();
            Double icComputed = icSanchez.get(v);

            if (icComputed == null) {
                throw new SLIB_Ex_Critic("Cannot found IC value for concept " + v);
            }

            logger.info(v + "\tExpected: " + icExpected.doubleValue() + "\tComputed: " + icComputed.doubleValue());

            assertEquals("IC", icExpected.doubleValue(), icComputed.doubleValue(), 0.0000001);

        }
    }
}
