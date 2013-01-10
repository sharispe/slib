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
package slib.examples.sml;

import org.openrdf.model.URI;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.impl.graph.elements.VertexTyped;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * Example of a Semantic measure computation using the Semantic Measures
 * Library. In this snippet we estimate the similarity of two concepts expressed
 * in the Gene Ontology. The Gene Ontology is expressed in OBO format. The
 * similarity is estimated using Lin's measure.
 *
 * More information at http://www.lgi2p.ema.fr/kid/tools/sml/
 *
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root
 * element, change value="INFO" to value="DEBUG"
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class SMComputationGO_pairwise {

    public static void main(String[] params) throws SLIB_Exception {

        // Configuration files, set the file path according to your configuration.
        // The Gene Ontology (OBO format)
        String goOBO = "/data/go/gene_ontology_ext.obo";

        DataFactory factory = DataFactoryMemory.getSingleton();
        URI graph_uri = factory.createURI("http://go/");

        // We define a prefix in order to set 
        factory.loadNamespacePrefix("GO", graph_uri.toString());


        G graph = new GraphMemory(graph_uri);

        GDataConf graphconf = new GDataConf(GFormat.OBO, goOBO);
        GraphLoaderGeneric.populate(graphconf, graph);

        // General information about the graph
        System.out.println(graph.toString());
        
        
        
        
        // The Gene Ontology is not rooted, i.e. Molecular Function, Biological Process, Cellular Component, the three sub-ontologies of 
        // the GO are not rooted. We create such a virtual root in order to be able to compare 
        // the concepts expressed in different sub-ontologies.
        
        // We create a vertex corresponding to the virtual root
        // and we add it to the graph
        URI uriVR = factory.createURI("http://go/virtualRoot");
        V virtualRoot = new VertexTyped(graph, uriVR, VType.CLASS);
        graph.addV(virtualRoot);
        
        // We root the graphs using the virtual root as root
        GAction rooting = new GAction(GActionType.REROOTING);
        rooting.addParameter("root_uri", uriVR.stringValue());
        GraphActionExecutor.applyAction(factory, rooting, graph);
        
        System.out.println(graph.toString());

        int nbVertices = graph.getV(VType.CLASS).size();

        System.out.println("Nb vertices : " + nbVertices);


        // We compute the similarity between http://go/0071869 and the collection of vertices
        V concept = graph.getV(factory.createURI("http://go/0071869"));

        ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_SANCHEZ_2011_a);

        // Then we define the Semantic measure configuration
        SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
        smConf.setICconf(icConf);

        SM_Engine engine = new SM_Engine(graph);

        double sim;
        for (V v : graph.getV(VType.CLASS)) {

            sim = engine.computePairwiseSim(smConf, concept, v);
            System.out.println(concept+"\t"+v+"\t"+sim);
        }






//        
//        // Retrieve the inclusive ancestors of a vertex
//        URI whale_uri = factory.createURI("http://graph/class/Whale");
//        V whale = graph.getV(whale_uri);
//        Set<V> whaleAncs = engine.getAncestorsInc(whale);
//        
//        System.out.println("Whale ancestors:");
//        for(V a : whaleAncs){
//            System.out.println("\t"+a);
//        }
//        
//        // Retrieve the inclusive descendants of a vertex
//        Set<V> whaleDescs = engine.getDescendantsInc(whale);
//        
//        System.out.println("Whale descendants:");
//        for(V a : whaleDescs){
//            System.out.println("\t"+a);
//        }
//        
//        /*
//         * Now the Semantic similarity computation
//         * We will use an Lin measure using the information content 
//         * definition proposed by Sanchez et al.
//         * 
//         */
//        
//        // First we define the information content (IC) we will use
//        ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_SANCHEZ_2011_a);
//        
//        // Then we define the Semantic measure configuration
//        SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
//        smConf.setICconf(icConf);
//        
//        // Finally, we compute the similarity between the concepts Horse and Whale
//        V horse = graph.getV(factory.createURI("http://graph/class/Horse"));
//        
//        double sim = engine.computePairwiseSim(smConf, whale, horse);
//        System.out.println("Sim Whale/Horse: "+sim);
//        System.out.println("Sim Horse/Horse: "+engine.computePairwiseSim(smConf, horse, horse));
    }
}
