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
package slib.examples.sml.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import slib.sglib.algo.graph.utils.WalkConstraintTax;
import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.sglib.model.impl.graph.elements.Edge;
import slib.sglib.model.impl.graph.elements.Vertex;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class MeSHExample_SKOS {

    

    public static void main(String[] args) {

        try {

            // Start a timer
            Timer t = new Timer();
            t.start();

            // The factory is used to build URIs. The factory caches the URIs to avoid useless object creation
            DataFactory factory = DataFactoryMemory.getSingleton();

            // We create the URI of the graph
            URI meshURI = factory.createURI("http://www.nlm.nih.gov/mesh/");

            // We create an in-memory graph
            G meshGraph = new GraphMemory(meshURI);

            // We configure a data set expressed in RDF XML
            GDataConf dataMeshSKOS = new GDataConf(GFormat.RDF_XML, "/data/mesh/mesh2013.rdf");

            // We populate the graph with the data set
            GraphLoaderGeneric.populate(dataMeshSKOS, meshGraph);

            System.out.println(meshGraph);

            // We create some URIs we will use during the process
            URI skosConceptURI = factory.createURI("http://www.w3.org/2004/02/skos/core#Concept");
            URI skosBroader = factory.createURI("http://www.w3.org/2004/02/skos/core#broader");
            URI skosNarrower = factory.createURI("http://www.w3.org/2004/02/skos/core#narrower");

            // We retrieve the graph vertex associated to the URI http://www.w3.org/2004/02/skos/core#Concept
            V skosConcept = meshGraph.getV(skosConceptURI);

            // We retrieve all the MeSH concepts, 
            // i.e. the vertices x for which a triplet x RDF.TYPE http://www.w3.org/2004/02/skos/core#Concept is found
            Set<V> concepts = meshGraph.getV(skosConcept, RDF.TYPE, Direction.IN);

            /*
             * We associate a type to the concepts
             * The type is VType.CLASS, this is required by the Semantic Measures engine
             * which requieres a clear distinction between classes and instances (e.g. for information content computation)
             */
            for (V v : concepts) {
                v.setType(VType.CLASS);
            }

            // we retrieve the current roots of the graph, at this stage the roots are the roots of the trees
            ValidatorDAG valDAG = new ValidatorDAG();
            // WalConstraint is an object enabling to define contraints to reach the neighbors of a vertex
            WalkConstraintTax wcBroader = new WalkConstraintTax(skosBroader, Direction.OUT);
            Set<V> roots = valDAG.getDAGRoots(meshGraph, wcBroader);

            /*
             * We create a global root which will subsume the roots of the trees. 
             * Notice that it would be preferable to: 
             * - first: create a unique tree root to stress that the roots of each tree are initially part of the same hierachy.
             * - Second: create a virtual root subsuming the unique root of each tree.
             * 
             * This two step process is performed by the XML Loader but cannot be performed as the SKOS format do not specify the tree nodes .
             */

            // We create the virtual root as http://www.nlm.nih.gov/mesh/virtualRoot#concept

            URI virtualRootURI = factory.createURI("http://www.nlm.nih.gov/mesh/virtualRoot#concept");
            V virtualRoot = new Vertex(virtualRootURI, VType.CLASS);
            // We add it to the graph
            meshGraph.addV(virtualRoot);
            meshGraph.addE(virtualRoot, skosConcept, RDF.TYPE);
//            concepts.add(virtualRoot);

            // for each tree root we create a skos:broader relationship to the virtualRoot
            for (V v : roots) {
                System.out.println("Add " + v + "\t" + skosBroader + "\t" + virtualRoot);
                meshGraph.addE(v, virtualRoot, skosBroader);
                meshGraph.addE(virtualRoot, v, skosNarrower);
            }

            /*
             * At this stage the Mesh Graph is unified, i.e. all tree roots are subsumed
             * by the virtual root. We now remove the cycles in order to obtain a rooted Directed Acyclic Graph (rDAG).
             * Most semantic similarity measures requires rDAG to be computed.
             * The Semantic Measures Engine will throw an error if the graph contains cycles and you use a measure requiring such graph constraints
             *
             * We therefore remove the relationships which create cycles.
             * see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh for more information
             */

            // We check the graph is a DAG: answer NO
            ValidatorDAG validatorDAG = new ValidatorDAG();
            boolean isDAG = validatorDAG.isDag(meshGraph, wcBroader);
            System.out.println("MeSH Graph is a DAG - SKOS broader OUT : " + isDAG);

            // We remove the cycles
            MeSHExample_SKOS.removeMeshCycles(meshGraph);

            // Is the graph acyclic?
            isDAG = validatorDAG.isDag(meshGraph, wcBroader);

            // Yes it is
            System.out.println("MeSH Graph is a DAG: " + isDAG);

            /* 
             * Now we can start the configuration of the engine to
             * compute Semantic Similarities between pairs of vertices
             *
             * We first define the configuration of the pairwise measure.
             * We use Lin formula driven by Sanchez et al. intrinsic IC.
             */
            ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011_a);
            SMconf measureConf = new SMconf("Lin_icSanchez", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);


            // We define the semantic measure engine to use 
            SM_Engine engine = new SM_Engine(meshGraph);

            // We define the constraints to retrieve the parents and the child of a vertex
            WalkConstraints wcToBroader = new WalkConstraintTax(skosBroader, Direction.OUT);
            WalkConstraints wcToNarrower = new WalkConstraintTax(skosBroader, Direction.IN);
            // we can also define wcToNarrower = new WalkConstraintTax(skosNarrower, Direction.OUT); 

            /* We now redefine the underlying configuration of the engine.
             * The engine is originally defined to process taxonomies structured by RDFS.SUBCLASSOF relationships.
             * Here we tune the engine to consider skos:broader as a substitute to RDFS.SUBCLASSOF
             */
            engine.getAncestorEngine().setWalkConstraint(wcToBroader);
            engine.getDescendantEngine().setWalkConstraint(wcToNarrower);

            // We finally compute semantic similarities between two concepts (groups of concepts can also be compared)
            // e.g. between Paranoid Disorders (D010259) and Schizophrenia, Paranoid (D012563)
            URI c1URI = factory.createURI("http://www.nlm.nih.gov/mesh/D010259#concept");
            URI c2URI = factory.createURI("http://www.nlm.nih.gov/mesh/D012563#concept");

            V c1 = meshGraph.getV(c1URI); // Paranoid Disorders
            V c2 = meshGraph.getV(c2URI); // Schizophrenia, Paranoid
            
            double sim = engine.computePairwiseSim(measureConf, c1, c2);
            System.out.println("Sim " + c1.getValue() + "\t" + c2.getValue() + "\t" + sim);

//            UtilDebug.exit();
            
            /* 
             * The computation of the first similarity is not very fast because   
             * the engine compute extra informations which are cached to boost next computations.
             * Lets now compute 1000000 random pairwise similarities.
             */
            
            int totalComparison = 1000000;
            // We create a list containing all concepts, i.e. the elements associated to the type CLASS
            List<V> conceptsList = new ArrayList<V>(meshGraph.getV(VType.CLASS));
            int id1, id2;
            Random r = new Random();

            for (int i = 0; i < totalComparison; i++) {
                id1 = r.nextInt(concepts.size());
                id2 = r.nextInt(concepts.size());

                c1 = conceptsList.get(id1);
                c2 = conceptsList.get(id2);

                sim = engine.computePairwiseSim(measureConf, c1, c2);

                // We only print 20 results but all are computed (see above)
                if ((i + 1) % 50000 == 0) {
                    System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + c1.getValue() + "/" + c2.getValue() + ": " + sim);
                }
            }
            // We print the time elapsed
            t.stop();
            t.elapsedTime();


        } catch (SLIB_Exception ex) {
            Logger.getLogger(MeSHExample_SKOS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Static method used to remove the cycles from the MeSH Graph 
     * see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh for more information
     *
     * @param meshGraph the graph associated to the MeSH
     *
     * @throws SLIB_Ex_Critic
     */
    public static void removeMeshCycles(G meshGraph) throws SLIB_Ex_Critic {
        
        DataFactory factory = DataFactoryMemory.getSingleton();

        URI skosBroader = factory.createURI("http://www.w3.org/2004/02/skos/core#broader");
        URI skosNarrower = factory.createURI("http://www.w3.org/2004/02/skos/core#narrower");

        // We remove the edges creating cycles
        URI ethicsURI = factory.createURI("http://www.nlm.nih.gov/mesh/D004989#concept");
        URI moralsURI = factory.createURI("http://www.nlm.nih.gov/mesh/D009014#concept");
        V ethicsV = meshGraph.getV(ethicsURI);
        V moralsV = meshGraph.getV(moralsURI);

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> moralsEdges = meshGraph.getE(skosBroader, moralsV, Direction.OUT);
        for (E e : moralsEdges) {

            if (e.getTarget().equals(ethicsV)) {

                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);

                // We also remove the inverse, i.e. target narrower source
                E eInverse = new Edge(e.getTarget(), e.getSource(), skosNarrower);
                meshGraph.removeE(eInverse);
            }
        }

        // The second one

        URI hydroxybutyratesURI = factory.createURI("http://www.nlm.nih.gov/mesh/D006885#concept");
        URI hydroxybutyricAcidURI = factory.createURI("http://www.nlm.nih.gov/mesh/D020155#concept");
        V hydroxybutyratesV = meshGraph.getV(hydroxybutyratesURI);
        V hydroxybutyricAcidV = meshGraph.getV(hydroxybutyricAcidURI);

        // We retrieve the direct subsumers of the concept (D020155)
        Set<E> hydroxybutyricAcidEdges = meshGraph.getE(skosBroader, hydroxybutyricAcidV, Direction.OUT);
        for (E e : hydroxybutyricAcidEdges) {

            if (e.getTarget().equals(hydroxybutyratesV)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);

                // We also remove the inverse, i.e. target narrower source
                E eInverse = new Edge(e.getTarget(), e.getSource(), skosNarrower);
                meshGraph.removeE(eInverse);
            }
        }

    }
}
