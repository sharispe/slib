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
package slib.examples.sml.general;

import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
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
 * Example of a Semantic measure computation using the Semantic Measures Library.
 * In this snippet we estimate the similarity of two concepts expressed in a semantic graph.
 * The semantic graph is expressed in Ntriples.
 * The similarity is estimated using Lin's measure.
 * 
 * More information at http://www.lgi2p.ema.fr/kid/tools/sml/
 * 
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root element, change value="INFO" to value="DEBUG"
 * 
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class SMComputation {
    
    public static void main(String[] params) throws SLIB_Exception{
        
        
        DataFactory factory = DataFactoryMemory.getSingleton();
        
        URI graph_uri = factory.createURI("http://graph/");
        
        G graph = new GraphMemory(graph_uri);
        
        String fpath = System.getProperty("user.dir")+"/src/main/resources/graph_test.nt";
        GDataConf graphconf = new GDataConf(GFormat.NTRIPLES, fpath);
        GraphLoaderGeneric.populate(graphconf, graph);
        
        // General information about the graph
        System.out.println(graph.toString());
        
        /*
        * The graph doesn't contain classes.
        * We therefore explicitly specify that all vertices composing the graph must be considered as classes.
        * This is required to retrieve the ancestors/descendants of a vertex and to use the engine used to perform semantic measures computation.
        * Note that some loaders automatically type vertices, which is not the case of the Ntriple loader.  
        * Reasoners coupled with rules can also be used to perform this treatment if you deal
        * with a semantic graph containing literal, instances...
        * Let's keep it simple.
        */
        for(V v : graph.getV()){
            v.setType(VType.CLASS);
            System.out.println("\t"+v.getValue()+"\t"+v.getType());
        }
        System.out.println(graph.toString());
        
        
        
        SM_Engine engine = new SM_Engine(graph);
        
        // Retrieve the inclusive ancestors of a vertex
        URI whale_uri = factory.createURI("http://graph/class/Whale");
        V whale = graph.getV(whale_uri);
        Set<V> whaleAncs = engine.getAncestorsInc(whale);
        
        System.out.println("Whale ancestors:");
        for(V a : whaleAncs){
            System.out.println("\t"+a);
        }
        
        // Retrieve the inclusive descendants of a vertex
        Set<V> whaleDescs = engine.getDescendantsInc(whale);
        
        System.out.println("Whale descendants:");
        for(V a : whaleDescs){
            System.out.println("\t"+a);
        }
        
        /*
         * Now the Semantic similarity computation
         * We will use an Lin measure using the information content 
         * definition proposed by Sanchez et al.
         * 
         */
        
        // First we define the information content (IC) we will use
        ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_SANCHEZ_2011_a);
        
        // Then we define the Semantic measure configuration
        SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
        smConf.setICconf(icConf);
        
        // Finally, we compute the similarity between the concepts Horse and Whale
        V horse = graph.getV(factory.createURI("http://graph/class/Horse"));
        
        double sim = engine.computePairwiseSim(smConf, whale, horse);
        System.out.println("Sim Whale/Horse: "+sim);
        System.out.println("Sim Horse/Horse: "+engine.computePairwiseSim(smConf, horse, horse));
    }
}
