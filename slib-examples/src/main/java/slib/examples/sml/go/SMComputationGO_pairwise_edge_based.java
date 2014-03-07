package slib.examples.sml.go;

import org.openrdf.model.URI;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;

/**
 * Example of a Semantic measure computation using the Semantic Measures
 * Library. In this snippet we estimate the similarity of two concepts expressed
 * in the Gene Ontology. The Gene Ontology is expressed in OBO format. The
 * similarity is estimated using structural measures.
 *
 * More information at http://www.semantic-measures-library.org/
 *
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root
 * element, change value="INFO" to value="DEBUG"
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class SMComputationGO_pairwise_edge_based {

    public static void main(String[] params) throws SLIB_Exception {

        // Configuration files, set the file path according to your configuration.
        // The Gene Ontology (OBO format)
        String goOBO = "/data/go/gene_ontology_ext.obo";

        URIFactory factory = URIFactoryMemory.getSingleton();
        URI graph_uri = factory.createURI("http://go/");

        // We define a prefix in order to build valid uris from ids such as GO:XXXXX, 
        // considering the configuration specified below the URI associated to GO:XXXXX will be http://go/XXXXX
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
        URI virtualRoot = factory.createURI("http://go/virtualRoot");
        graph.addV(virtualRoot);
        
        // We root the graphs using the virtual root as root
        GAction rooting = new GAction(GActionType.REROOTING);
        rooting.addParameter("root_uri", virtualRoot.stringValue());
        GraphActionExecutor.applyAction(factory, rooting, graph);
        
        System.out.println(graph.toString());

        int nbVertices = graph.getV().size();

        System.out.println("Nb vertices : " + nbVertices);


        // We compute the similarity between http://go/0071869 and the collection of vertices
        URI concept = factory.createURI("http://go/0071869");


        // Then we define the Semantic measure configuration
        SMconf smConf = new SMconf("Rada", SMConstants.FLAG_SIM_PAIRWISE_DAG_EDGE_RADA_1989);
        
        SM_Engine engine = new SM_Engine(graph);

        double sim;
        for (URI v : graph.getV()) {

            sim = engine.computePairwiseSim(smConf, concept, v);
            System.out.println(concept+"\t"+v+"\t"+sim);
        }
    }
}
