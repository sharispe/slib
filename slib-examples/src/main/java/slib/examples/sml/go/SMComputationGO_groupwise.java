package slib.examples.sml.go;

import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.algo.graph.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.algo.graph.extraction.rvf.instances.impl.InstanceAccessor_RDF_TYPE;
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
 * Library. In this snippet we estimate the similarity of two genes annotated by concepts (terms) 
 * defined in the Gene Ontology.
 * The Gene Ontology is expressed in OBO format. 
 * The similarity is estimated using an indirect groupwise measure based on: Lin's pairwise measure, Best Match Average aggregation strategy.
 * 
 * More information at http://www.lgi2p.ema.fr/kid/tools/sml/
 *
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root
 * element, change value="INFO" to value="DEBUG"
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class SMComputationGO_groupwise {

    public static void main(String[] params) throws SLIB_Exception {

        // Configuration files, set the file path according to your configuration.
        // The Gene Ontology (OBO format)
        String goOBO = "/data/go/gene_ontology_ext.obo";
        String annot = "/data/go/gene_association.goa_human";


        DataFactory factory = DataFactoryMemory.getSingleton();
        URI graph_uri = factory.createURI("http://go/");

        // We define a prefix in order to set 
        factory.loadNamespacePrefix("GO", graph_uri.toString());


        G graph = new GraphMemory(graph_uri);

        GDataConf goConf    = new GDataConf(GFormat.OBO, goOBO);
        GDataConf annotConf = new GDataConf(GFormat.GAF2, annot);
        
        GraphLoaderGeneric.populate(goConf, graph);
        GraphLoaderGeneric.populate(annotConf, graph);
        

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
        SMconf smConfPairwise = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
        smConfPairwise.setICconf(icConf);

        SMconf smConfGroupwise = new SMconf("BMA_LIN", SMConstants.FLAG_SIM_GROUPWISE_BMA);
        smConfGroupwise.setPairwise_measure_id(smConfPairwise.id);
        
        SM_Engine engine = new SM_Engine(graph);     
        
        V i = graph.getV(factory.createURI("http://go/I3L2H2"));
        
        // An object used to retrieve the annotation of an instance according 
        // to a particular semantic projection 
        InstancesAccessor iAccessor = new InstanceAccessor_RDF_TYPE(graph);
        
        Set<V> annotations_i = iAccessor.getDirectClass(i);
        System.out.println("http://go/I3L2H2 is annotated by "+annotations_i.size()+" concepts");   
        
        
        double sim;
        
        for (V v : graph.getV(VType.INSTANCE)) {

              Set<V> annotations_v = iAccessor.getDirectClass(v);
              
              sim = engine.computeGroupwiseAddOnSim(smConfGroupwise, smConfPairwise, annotations_i, annotations_v);
              System.out.println(i+"\t"+v+"\t"+sim);
        }
    }
}
