package slib.examples.sml.general;

import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * Example of a Semantic measure computation using the Semantic Measures Library.
 * In this snippet we estimate the similarity of two concepts expressed in a semantic graph.
 * The semantic graph is expressed in Ntriples.
 * The similarity is estimated using Lin's measure.
 * 
 * More information at http://www.semantic-measures-library.org/
 * 
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root element, change value="INFO" to value="DEBUG"
 * 
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class SMComputation {
    
    public static void main(String[] params) throws SLIB_Exception{
        
        
        URIFactory factory = URIFactoryMemory.getSingleton();
        
        URI graph_uri = factory.createURI("http://graph/");
        
        G graph = new GraphMemory(graph_uri);
        
        String fpath = System.getProperty("user.dir")+"/src/main/resources/graph_test.nt";
        GDataConf graphconf = new GDataConf(GFormat.NTRIPLES, fpath);
        GraphLoaderGeneric.populate(graphconf, graph);
        
        // General information about the graph
        System.out.println(graph.toString());
        
        
        SM_Engine engine = new SM_Engine(graph);
        
        // Retrieve the inclusive ancestors of a vertex
        URI whale = factory.createURI("http://graph/class/Whale");
        Set<URI> whaleAncs = engine.getAncestorsInc(whale);
        
        System.out.println("Whale ancestors:");
        for(URI a : whaleAncs){
            System.out.println("\t"+a);
        }
        
        // Retrieve the inclusive descendants of a vertex
        Set<URI> whaleDescs = engine.getDescendantsInc(whale);
        
        System.out.println("Whale descendants:");
        for(URI a : whaleDescs){
            System.out.println("\t"+a);
        }
        
        /*
         * Now the Semantic similarity computation
         * We will use an Lin measure using the information content 
         * definition proposed by Sanchez et al.
         * 
         */
        
        // First we define the information content (IC) we will use
        ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_SANCHEZ_2011);
        
        // Then we define the Semantic measure configuration
        SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
        smConf.setICconf(icConf);
        
        // Finally, we compute the similarity between the concepts Horse and Whale
        URI horse = factory.createURI("http://graph/class/Horse");
        
        double sim = engine.computePairwiseSim(smConf, whale, horse);
        System.out.println("Sim Whale/Horse: "+sim);
        System.out.println("Sim Horse/Horse: "+engine.computePairwiseSim(smConf, horse, horse));
    }
}
