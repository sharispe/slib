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
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.algo.graph.extraction.rvf.AncestorEngine;
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
import slib.utils.impl.ResultStack;
import slib.utils.impl.Timer;
import slib.utils.impl.UtilDebug;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class MeSHExample_SKOS {

    /**
     * Remove the cycles from the MeSH Graph see
     * http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh for
     * more information
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

            System.out.println("\t" + e);
            if (e.getTarget().equals(ethicsV)) {

                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);

                // We also remove the inverse, i.e. target narrower source
                E eInverse = new Edge(e.getTarget(), e.getSource(), skosNarrower);
                meshGraph.removeE(eInverse);
            }
        }

        ValidatorDAG validatorDAG = new ValidatorDAG();
        boolean isDAG = validatorDAG.containsTaxonomicDag(meshGraph);

        System.out.println("MeSH Graph is a DAG: " + isDAG);

        // We remove the edges creating cycles
        // see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh

        URI hydroxybutyratesURI = factory.createURI("http://www.nlm.nih.gov/mesh/D006885#concept");
        URI hydroxybutyricAcidURI = factory.createURI("http://www.nlm.nih.gov/mesh/D020155#concept");
        V hydroxybutyratesV = meshGraph.getV(hydroxybutyratesURI);
        V hydroxybutyricAcidV = meshGraph.getV(hydroxybutyricAcidURI);

        // We retrieve the direct subsumers of the concept (D009014)
        Set<E> hydroxybutyricAcidEdges = meshGraph.getE(skosBroader, hydroxybutyricAcidV, Direction.OUT);
        for (E e : hydroxybutyricAcidEdges) {

            System.out.println("\t" + e);
            if (e.getTarget().equals(hydroxybutyratesV)) {
                System.out.println("\t*** Removing edge " + e);
                meshGraph.removeE(e);

                // We also remove the inverse, i.e. target narrower source
                E eInverse = new Edge(e.getTarget(), e.getSource(), skosNarrower);
                meshGraph.removeE(eInverse);
            }
        }

    }

    public static void main(String[] args) {

        try {

            Timer t = new Timer();
            t.start();

            DataFactory factory = DataFactoryMemory.getSingleton();
            URI meshURI = factory.createURI("http://www.nlm.nih.gov/mesh/");

            G meshGraph = new GraphMemory(meshURI);

            GDataConf dataMeshSKOS = new GDataConf(GFormat.RDF_XML, "/data/mesh/mesh2013.rdf");
            GraphLoaderGeneric.populate(dataMeshSKOS, meshGraph);

            System.out.println(meshGraph);

            URI skosConceptURI = factory.createURI("http://www.w3.org/2004/02/skos/core#Concept");
            URI skosBroader  = factory.createURI("http://www.w3.org/2004/02/skos/core#broader");
            URI skosNarrower = factory.createURI("http://www.w3.org/2004/02/skos/core#narrower");

            V skosConcept = meshGraph.getV(skosConceptURI);

            /**
             * We retrieve all the concepts and we associate to them the type
             * CLASS. This is a hack to compute semantic similarities between
             * instances considering them as hierarchically structured (will be
             * change in next versions).
             */
            Set<V> concepts = meshGraph.getV(skosConcept, RDF.TYPE, Direction.IN);

            for (V v : concepts) {
                v.setType(VType.CLASS);
            }

            
            
            // we retrieve the current roots, i.e. the tree roots
            ValidatorDAG valDAG = new ValidatorDAG();
            WalkConstraintTax wcBroader = new WalkConstraintTax(skosBroader, Direction.OUT);
            Set<V> roots = valDAG.getDAGRoots(meshGraph, wcBroader);
            
            // We create the global root Notice that the tree root cannot be created
            URI virtualRootURI = factory.createURI("http://www.nlm.nih.gov/mesh/virtualRoot#concept");
            V virtualRoot = new Vertex(virtualRootURI, VType.CLASS);
            meshGraph.addV(virtualRoot);
            meshGraph.addE(virtualRoot,skosConcept, RDF.TYPE);
            concepts.add(virtualRoot);

            // for each tree root we create a skos:broader relationship to the virtualRoot
            for (V v : roots) { 
                System.out.println("Add "+v+"\t"+skosBroader+"\t"+virtualRoot);
                meshGraph.addE(v, virtualRoot, skosBroader);
                meshGraph.addE(virtualRoot,v, skosNarrower);
            }


            /*
             * We remove the cycles of the graph in order to obtain 
             * a rooted directed acyclic graph (DAG) and therefore be able to 
             * use most of semantic similarity measures.
             * see http://semantic-measures-library.org/sml/index.php?q=doc&page=mesh
             */

            // We check the graph is a DAG: answer NO
            ValidatorDAG validatorDAG = new ValidatorDAG();
            boolean isDAG = validatorDAG.isDag(meshGraph, wcBroader);
            System.out.println("MeSH Graph is a DAG - SKOS broader OUT : " + isDAG);

//            isDAG = validatorDAG.isDag(meshGraph, skosBroader, Direction.IN);
//            System.out.println("MeSH Graph is a DAG - SKOS broader IN : " + isDAG);
//            
            // We remove the cycles
            MeSHExample_SKOS.removeMeshCycles(meshGraph);

            isDAG = validatorDAG.isDag(meshGraph, wcBroader);

            // We check the graph is a DAG: answer Yes
            System.out.println("MeSH Graph is a DAG: " + isDAG);
            
//            UtilDebug.exit();

            /* 
             * Now we can compute Semantic Similarities between pairs vertices
             */

            // we first configure a pairwise measure
            ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011_a);
            SMconf measureConf = new SMconf("Lin_icSanchez", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);


            // We define the semantic measure engine to use 
            SM_Engine engine = new SM_Engine(meshGraph);

            WalkConstraints wcToBroader = new WalkConstraintTax(skosBroader, Direction.OUT);
            WalkConstraints wcToNarrower = new WalkConstraintTax(skosBroader, Direction.IN);
            // we can also define wcToNarrower = new WalkConstraintTax(skosNarrower, Direction.OUT); 

            engine.getAncestorEngine().setWalkConstraint(wcToBroader);
            engine.getDescendantEngine().setWalkConstraint(wcToNarrower);

            // We compute semantic similarities between concepts
            // e.g. between Paranoid Disorders (D010259) and Schizophrenia, Paranoid (D012563)
            URI c1URI = factory.createURI("http://www.nlm.nih.gov/mesh/D010259#concept");
            URI c2URI = factory.createURI("http://www.nlm.nih.gov/mesh/D012563#concept");

            V c1 = meshGraph.getV(c1URI); // Paranoid Disorders
            V c2 = meshGraph.getV(c2URI); // Schizophrenia, Paranoid

            for (E e : meshGraph.getE(c1, wcToBroader)) {
                System.out.println(e);
            }

            System.out.println("Ancestors "+c1);
            Set<V> ancC1 = engine.getAncestorEngine().getAncestorsInc(c1);
            for (V a : ancC1) {
                System.out.println("\t" + a);
            }
            
            for(V v : concepts){
                Set<V> anc = meshGraph.getV(v, skosBroader, Direction.OUT);
                Set<V> ancWC = meshGraph.getV(v, wcToBroader);
                if(anc.isEmpty()){
                    System.out.println("******** "+v+"\t"+ancWC.size());
                }
            }
            
            System.out.println("Contraint");
            System.out.println(wcToBroader);
            
//            UtilDebug.exit();

            ResultStack<V, Double> ics = engine.computeIC(icConf);
            System.out.println("IC root "+ics.get(virtualRoot));
            

            // We compute the similarity
            double sim = engine.computePairwiseSim(measureConf, c1, c2);
            System.out.println("Sim " + c1.getValue() + "\t" + c2.getValue() + "\t" + sim);

            
//            UtilDebug.exit();
            /* 
             * The computation of the first similarity is not very fast because   
             * the engine compute extra informations which are cached for next computations.
             * Lets compute 1000000 random pairwise similarities
             */
            int totalComparison = 1000000;
            List<V> conceptsList = new ArrayList<V>(meshGraph.getV(VType.CLASS));
            int id1, id2;
            String idC1, idC2;
            Random r = new Random();

            for (int i = 0; i < totalComparison; i++) {
                id1 = r.nextInt(concepts.size());
                id2 = r.nextInt(concepts.size());

                c1 = conceptsList.get(id1);
                c2 = conceptsList.get(id2);

                sim = engine.computePairwiseSim(measureConf, c1, c2);

                if ((i + 1) % 50000 == 0) {
                    idC1 = ((URI) c1.getValue()).getLocalName();
                    idC2 = ((URI) c2.getValue()).getLocalName();

                    System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
                }
            }
            t.stop();
            t.elapsedTime();




        } catch (SLIB_Exception ex) {
            Logger.getLogger(MeSHExample_SKOS.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
