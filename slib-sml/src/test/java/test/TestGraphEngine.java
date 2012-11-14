package test;

/*
 * 
 * Copyright or © or Copr. Ecole des Mines d'Alès (2012) 
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
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.slibformat.GraphLoader_SLIB;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.impl.memory.GraphMemory;
import slib.sglib.model.repo.DataFactory;
import slib.sglib.model.repo.impl.DataFactoryMemory;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.ResultStack;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
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
    Map<String, ResultStack<V, Double>> informationContents = new HashMap<String, ResultStack<V, Double>>();
    DataFactory factory;
    SM_Engine engine;
    G graph;
    String SANCHEZ_IC = "SANCHEZ_IC";

    public TestGraphEngine() {

        logger.info("Loading required Data");

        factory = DataFactoryMemory.getSingleton();
        try {
           

            GraphLoader_SLIB loader = new GraphLoader_SLIB();
            graph = new GraphMemory(factory.createURI(uriGraphTest));
            loader.populate(new GDataConf(GFormat.SLIB, graphFile), graph);
            engine = new SM_Engine(graph);
            
             loadExpectedValues();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error("Error " + ex.getMessage());
        }
    }

    private void loadExpectedValues() throws Exception {

        
        informationContents.put(SANCHEZ_IC, new ResultStack<V, Double>());

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

            URI cURI = factory.createURI(uriGraphTest + nsConcept);
            V v = graph.getV(cURI);

            int ancCount = Integer.parseInt(data[COL_ANCESTORS_COUNT]);
            String[] ancNSStrings = data[COL_ANCESTORS].split(";");

            if (ancCount != ancNSStrings.length) {
                throw new SLIB_Ex_Critic("Incoherence found in ancestors/count ancestors of " + cURI + " expected " + ancCount + " found " + ancNSStrings.length + " " + Arrays.toString(ancNSStrings));
            }

            Set<URI> ancSet = new HashSet<URI>();
            for (String s : ancNSStrings) {
                ancSet.add(factory.createURI(uriGraphTest + s));
            }
            ancestors.put(cURI, ancSet);

            int descCount = Integer.parseInt(data[COL_DESCENDANTS_COUNT]);
            String[] descNSStrings = data[COL_DESCENDANTS].split(";");

            if (descCount != descNSStrings.length) {
                throw new SLIB_Ex_Critic("Incoherence found in descendants/count descendants of " + cURI);
            }

            Set<URI> descSet = new HashSet<URI>();
            for (String s : descNSStrings) {
                descSet.add(factory.createURI(uriGraphTest + s));
            }
            descendants.put(cURI, descSet);

            int leavesCount = Integer.parseInt(data[COL_LEAVES_COUNT]);
            String[] leavesNSStrings = data[COL_LEAVES].split(";");

            Set<URI> leavesSet = new HashSet<URI>();
            for (String s : leavesNSStrings) {
                leavesSet.add(factory.createURI(uriGraphTest + s));
            }
            leaves.put(cURI, leavesSet);

            if (leavesCount != leavesNSStrings.length) {
                throw new SLIB_Ex_Critic("Incoherence found in leaves/count leaves of " + cURI);
            }

            double icSanchez = Double.parseDouble(data[COL_SANCHEZ_IC]);
            informationContents.get(SANCHEZ_IC).add(v, icSanchez);
        }
        in.close();


    }

    @Test
    public void test_ancestors() throws SLIB_Ex_Critic {


        for (V v : graph.getV()) {
            Set<URI> ancsExpected = ancestors.get((URI) v.getValue());
            Set<V> ancs = engine.getAncestorsInc(v);
            assertTrue(ancsExpected.size() == ancs.size());

            // Convert to URIs 
            Set<URI> ancsURIs = new HashSet<URI>();
            for (V a : ancs) {
                ancsURIs.add((URI) a.getValue());
            }

            logger.info("Expected :" + ancsExpected);
            logger.info("Found    :" + ancsURIs);
            ancsExpected.removeAll(ancsURIs);
            logger.info("Size ANC " + ancsExpected.size() + " expected 0");
            assertTrue(ancsExpected.isEmpty());
        }
    }

    @Test
    public void test_descendants() throws SLIB_Ex_Critic {


        for (V v : graph.getV()) {
            Set<URI> descsExpected = descendants.get((URI) v.getValue());
            Set<V> descs = engine.getDescendantsInc(v);
            assertTrue(descsExpected.size() == descs.size());

            // Convert to URIs 
            Set<URI> descsURIs = new HashSet<URI>();
            for (V a : descs) {
                descsURIs.add((URI) a.getValue());
            }

            logger.info("Expected :" + descsExpected);
            logger.info("Found    :" + descsURIs);
            descsExpected.removeAll(descsURIs);
            logger.info("Size DESC " + descsExpected.size() + " expected 0");
            assertTrue(descsExpected.isEmpty());
        }
    }

    @Test
    public void test_leaves() throws SLIB_Ex_Critic {

        Map<V, Set<V>> leavesFound = engine.getReachableLeaves();

        for (V v : graph.getV()) {


            Set<URI> leavesURIsExpected = leaves.get((URI) v.getValue());
            assertTrue(leavesURIsExpected.size() == leavesFound.get(v).size());

            // Convert to URIs 
            Set<URI> leavesURIsFound = new HashSet<URI>();
            for (V a : leavesFound.get(v)) {
                leavesURIsFound.add((URI) a.getValue());
            }

            logger.info("Expected :" + leavesURIsExpected);
            logger.info("Found    :" + leavesURIsFound);
            leavesURIsExpected.removeAll(leavesURIsFound);
            logger.info("Size DESC " + leavesURIsExpected.size() + " expected 0");
            assertTrue(leavesURIsExpected.isEmpty());
        }
    }

    @Test
    public void test_leaves_count() throws SLIB_Ex_Critic {

        ResultStack<V, Double> reachableLeaves = engine.getAllNbReachableLeaves();

        for (V v : graph.getV()) {
            Set<URI> leavesURIsExpected = leaves.get((URI) v.getValue());
            assertTrue(leavesURIsExpected.size() == reachableLeaves.get(v));
        }
    }

    @Test
    public void test_ancestor_count() throws SLIB_Ex_Critic {

        ResultStack<V, Double> nbAncestors = engine.getAllNbAncestors();

        for (V v : graph.getV()) {
            Set<URI> ancestorsURIsExpected = ancestors.get((URI) v.getValue());
            assertTrue(ancestorsURIsExpected.size() == nbAncestors.get(v));
        }
    }

    @Test
    public void test_retrieveLeaves() throws SLIB_Ex_Critic {

        Set<V> leavesGraph = engine.getLeaves();

        logger.info("" + leavesGraph.size() + "\t" + leavesGraph);
        //TODO check equality
        assertTrue(leavesGraph.size() == 7);
    }
    
    @Test
    public void test_IC_Sanchez() throws SLIB_Ex_Critic {

        ResultStack<V, Double> icSanchez = engine.computeIC(new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011_a));

        for (Entry<V,Double> e : informationContents.get(SANCHEZ_IC).entrySet()) {
            
            V v = e.getKey();
            
            Double icExpected = e.getValue();
            Double icComputed = icSanchez.get(v);
            
            if(icComputed == null){
                throw new SLIB_Ex_Critic("Cannot found IC value for concept "+v.getValue());
            }
            
            logger.info(v.getValue()+"\tExpected: "+icExpected.doubleValue()+"\tComputed: "+icComputed.doubleValue());
            
            assertEquals("IC", icExpected.doubleValue(), icComputed.doubleValue(), 0.0000001);
            
        }
    }
}
