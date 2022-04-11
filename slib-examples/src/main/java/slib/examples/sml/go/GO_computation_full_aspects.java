/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.examples.sml.go;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.impl.Timer;

/**
 *
 * @author sharispe
 */
public class GO_computation_full_aspects {

    public static void main(String[] params) throws Exception {

        Timer t = new Timer();
        t.start();

        // Configuration files, set the file path according to your configuration.
        // The Gene Ontology (OBO format)
        String goOBO = "/data/go/go.obo";
        String basename_output_file = "/data/tmp/output_sim_";

        URIFactory factory = URIFactoryMemory.getSingleton();
        URI graph_uri = factory.getURI("http://go/");

        // We create a vertex corresponding to the BP concept
        final URI bpGOTerm = factory.getURI("http://go/0008150");
        final URI ccGOTerm = factory.getURI("http://go/0005575");
        final URI mfGOTerm = factory.getURI("http://go/0003674");

        @SuppressWarnings("serial")
		HashMap<String, URI> aspects = new HashMap<String, URI>() {
            {
                put("BP", bpGOTerm);
                put("CC", ccGOTerm);
                put("MF", mfGOTerm);
            }
        };

        factory.loadNamespacePrefix("GO", graph_uri.toString());

        for (Map.Entry<String, URI> e : aspects.entrySet()) {

            System.out.println("Processing " + e.getKey() + " aspects");

            String fname = basename_output_file + e.getKey() + ".tsv";

            URI GOTerm_root_aspect = e.getValue();

            G graph = new GraphMemory(graph_uri);

            GDataConf goConf = new GDataConf(GFormat.OBO, goOBO);

            GraphLoaderGeneric.populate(goConf, graph);

            // General information about the graph
            System.out.println(graph.toString());

            // The Gene Ontology is composed of several aspects, i.e. Molecular Function, Biological Process, Cellular Component
            // To focus on one of them we do the following, e.g. for Biological process (GO:008150)
            // We root the graph using the BP concept (all nodes that are not subsuming BP will be removed)
            GAction reduction = new GAction(GActionType.VERTICES_REDUCTION);
            reduction.addParameter("root_uri", GOTerm_root_aspect.stringValue());
            GraphActionExecutor.applyAction(factory, reduction, graph);

            System.out.println(graph.toString());

            int nbVertices = graph.getV().size();

            System.out.println("Nb vertices : " + nbVertices);

            SM_Engine engine = new SM_Engine(graph);
            
            long nb_comparison = graph.getV().size() * graph.getV().size();
            long comparison_done = 0;

            try (PrintWriter writer = new PrintWriter(fname, "UTF-8")) {
                
                
                ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_SANCHEZ_2011);
                
                // Then we define the Semantic measure configuration
                SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
                smConf.setICconf(icConf);
                
                for (URI u : graph.getV()) {
                    
                    for (URI v : graph.getV()) {
                        
                        double sim = engine.compare(smConf, u, v);
                        writer.println(u.getLocalName()+"\t"+v.getLocalName()+"\t"+sim);
                        comparison_done++;
                    }
                    
                    System.out.println(comparison_done+"/"+nb_comparison);
                }
            }

            System.out.println("consult : " + fname);
        }
        System.out.println("Process done");
        t.stop();
        t.elapsedTime();
    }
}
