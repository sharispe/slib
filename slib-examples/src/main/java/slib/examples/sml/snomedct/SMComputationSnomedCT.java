package slib.examples.sml.snomedct;

import org.openrdf.model.URI;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
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
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * Example of a Semantic measure computation using the Semantic Measures
 * Library. In this snippet we estimate the similarity of two concepts expressed
 * in the SNOMED-CT semantic Graph.
 * 
 * More information at http://www.lgi2p.ema.fr/kid/tools/sml/
 *
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root
 * element, change value="INFO" to value="DEBUG"
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class SMComputationSnomedCT {

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
    }
}
