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
package slib.graph.test.algo.graph.reduction.dag;

import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import slib.graph.algo.accessor.GraphAccessor;
import slib.graph.algo.extraction.rvf.AncestorEngine;
import slib.graph.algo.extraction.rvf.DescendantEngine;
import slib.graph.algo.reduction.dag.GraphReduction_Transitive;
import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.algo.utils.RooterDAG;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.conf.GraphConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.graph.test.algo.graph.SLIB_UnitTestValues;
import slib.graph.test.algo.graph.TestUtils;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Test_GraphReduction_Transitive {

    G g;
    URIFactory f = URIFactoryMemory.getSingleton();
    SLIB_UnitTestValues test = new SLIB_UnitTestValues();

    /**
     *
     * @throws SLIB_Exception
     */
    public Test_GraphReduction_Transitive() throws SLIB_Exception {
        g = TestUtils.loadTestGraph(GFormat.NTRIPLES, SLIB_UnitTestValues.G_DAG_BASIC);
    }

    /**
     *
     * @throws SLIB_Exception
     */
    @Test
    public void transitiveReductionToyGraph() throws SLIB_Exception {

        System.out.println("Checking Transitive Reduction");
        Set<E> removedEdges;

        removedEdges = GraphReduction_Transitive.process(g);

        assertTrue(removedEdges.isEmpty());

        g.addE(test.G_BASIC_SPIDERMAN, RDFS.SUBCLASSOF, test.G_BASIC_ORGANISM);

        removedEdges = GraphReduction_Transitive.process(g);

        System.out.println("Removed Edges " + removedEdges);
        assertTrue(removedEdges.size() == 1);

        E er = null;
        for (E e : removedEdges) {
            if (e.getURI().equals(RDFS.SUBCLASSOF)) {
                er = e;
            }
        }

        assertTrue(er.getSource().equals(test.G_BASIC_SPIDERMAN) && er.getTarget().equals(test.G_BASIC_ORGANISM));


        g.addE(test.G_BASIC_ORGANISM, RDFS.SUBCLASSOF, test.G_BASIC_THING);

        removedEdges = GraphReduction_Transitive.process(g);
        System.out.println(removedEdges);
        assertTrue(removedEdges.isEmpty());// duplicate edge note allowed
    }
    
    
    @Test
    public void transitiveReduction_Classes_2() throws SLIB_Exception {

        System.out.println("Checking Transitive Reduction of "+RDFS.SUBCLASSOF);
        Set<E> removedEdges;

        removedEdges = GraphReduction_Transitive.process(g);

        assertTrue(removedEdges.isEmpty());

        g.addE(test.G_BASIC_HUMAN, RDFS.SUBCLASSOF, test.G_BASIC_THING);

        removedEdges = GraphReduction_Transitive.process(g);

        System.out.println("Removed Edges " + removedEdges);
        assertTrue(removedEdges.size() == 1);

        E er =removedEdges.iterator().next();

        assertTrue(er.getSource().equals(test.G_BASIC_HUMAN) && er.getTarget().equals(test.G_BASIC_THING));


        g.addE(test.G_BASIC_HUMAN, RDFS.SUBCLASSOF, test.G_BASIC_THING);
        
        GAction actionTR = new GAction(GActionType.TRANSITIVE_REDUCTION);
        actionTR.addParameter("target", "CLASSES");
        
        int nbEdges = g.getNumberEdges();
        assertTrue(g.containsEdge(test.G_BASIC_HUMAN, RDFS.SUBCLASSOF, test.G_BASIC_THING));
        
        GraphActionExecutor.applyAction(actionTR, g);
        
        assertTrue(!g.containsEdge(test.G_BASIC_HUMAN, RDFS.SUBCLASSOF, test.G_BASIC_THING));
        assertTrue(g.getNumberEdges() == nbEdges-1);        
    }
    

    @Test
    public void transitiveReduction_Instances() throws SLIB_Exception {

        System.out.println("Checking Transitive Reduction of "+RDF.TYPE);
        Set<E> removedEdges;

        removedEdges = GraphReduction_Transitive.process(g);

        assertTrue(removedEdges.isEmpty());

        URI instanceHuman = f.getURI("http://test/darwin");
        
        
        g.addE(instanceHuman, RDF.TYPE, test.G_BASIC_HUMAN);
        
        GAction actionTR = new GAction(GActionType.TRANSITIVE_REDUCTION);
        actionTR.addParameter("target", "INSTANCES");
        
        // 1 - without redundancy
        int nbEdges = g.getNumberEdges();
        assertTrue(g.containsEdge(instanceHuman, RDF.TYPE, test.G_BASIC_HUMAN));
        
        GraphActionExecutor.applyAction(actionTR, g);
        
        assertTrue(g.containsEdge(instanceHuman, RDF.TYPE, test.G_BASIC_HUMAN));
        assertTrue(g.getNumberEdges() == nbEdges);        
        
        
        // 2 - with a redundancy
        g.addE(instanceHuman, RDF.TYPE, test.G_BASIC_THING);
        nbEdges = g.getNumberEdges();
        
        assertTrue(g.containsEdge(instanceHuman, RDF.TYPE, test.G_BASIC_THING));
        GraphActionExecutor.applyAction(actionTR, g);
        assertTrue(!g.containsEdge(instanceHuman, RDF.TYPE, test.G_BASIC_THING));
        assertTrue(g.getNumberEdges() == nbEdges-1);      
    }
    
    /**
     *
     * @throws SLIB_Exception
     */
    @Test
    public void transitiveReductionGO() throws SLIB_Exception {

        String gofilePath = SLIB_UnitTestValues.G_GO;

        URIFactoryMemory.getSingleton().loadNamespacePrefix("GO", "http://GO#");
        

        GraphConf gconf = new GraphConf();
        gconf.setUri(SLIB_UnitTestValues.uriGraph);
        GDataConf conf = new GDataConf(GFormat.OBO, gofilePath);
        gconf.addGDataConf(conf);

        
        g = GraphLoaderGeneric.load(gconf);

        System.out.println(g.toString());

        URI root_uri = OWL.THING;

        RooterDAG.rootUnderlyingTaxonomicDAG(g, root_uri);

        System.out.println(g.toString());

        long nbEdgesOrigin = g.getE().size();

        Set<E> removedEdges = GraphReduction_Transitive.process(g);

        long nbEdgesTR = g.getE().size();

        /*
         * We check coherence between original number of edges and removed
         */
        System.out.println("Number of edges before TR: " + nbEdgesOrigin);
        System.out.println("Number of edges after TR:  " + nbEdgesTR);
        System.out.println("Removed Edges:  " + removedEdges.size());

        assertTrue(nbEdgesOrigin == nbEdgesTR + removedEdges.size());

        // Get all ancestors 

        AncestorEngine rvf = new AncestorEngine(g);
        Map<URI, Set<URI>> ancestorsMap = rvf.getAllRVClass();

        // we check the root do not contains ancestors
        
        assertTrue(ancestorsMap.get(root_uri).isEmpty());
        
        DescendantEngine rvd = new DescendantEngine(g);
        Set<URI> classes = GraphAccessor.getClasses(g);
        assertTrue(rvd.getDescendantsExc(root_uri).size() == classes.size()-1);

        /*
         * We check all remove edges can be inferred
         */

        boolean valid = true;

        for (E e : removedEdges) {

            if (e.getURI().equals(RDFS.SUBCLASSOF)) {

                URI v = e.getSource();
                URI inferableAncestor = e.getTarget();
                if (!ancestorsMap.get(v).contains(inferableAncestor)) {
                    valid = false;
                    System.out.println(e + " was removed but cannot be infered");
                    System.out.println("Inferable " + ancestorsMap.get(v));
                    break;
                }
            }
        }
        assertTrue(valid);
    }
}
