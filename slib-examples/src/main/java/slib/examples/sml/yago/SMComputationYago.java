package slib.examples.sml.yago;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.sglib.model.repo.DataFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class SMComputationYago {
    
    public static void main(String[] args) throws SLIB_Exception{
        
        /*
         * The data loading is composed of three steps:
         *  - (1) we load the yago taxonomy from the turtle file
         *  - (2) we type the vertices in order to specify the engine which are the vertices associated to classes (concepts)
         *       This treatment is required in order to perform some algorithms.
         *  - (3) We root vertices which are not rooted by owl:Thing. Algorithms require the processed graph to be connected
         *  i.e. to compute the Most Informative Common Ancestors of two concepts.
         * 
         * Notice that due to the size of the taxonomy, extra memory must be allocated to the JVM e.g. -Xmx3000m
         */
        
        Timer t = new Timer();
        t.start();
        
        String yagoTaxonomyFile = "/data/yago/yagoTaxonomy.ttl";
        
        DataFactory factory = DataFactoryMemory.getSingleton();
        URI yagoURI = factory.createURI("http://yago-knowledge.org/resource/");
        G g = new GraphMemory(yagoURI);
        
        // This is the configuration of the data 
        GDataConf dataConf = new GDataConf(GFormat.TURTLE, yagoTaxonomyFile);
        
        // This is the configuration of the action we want to perform after data loading, i.e. to type the vertices
        GAction actionTypeConf   = new GAction(GActionType.TYPE_VERTICES);
        
        // We specify an action to root the vertices, typed as class without outgoing rdfs:subclassOf relationship 
        // Those vertices are linked to owl:Thing by an eddge x  rdfs:subClassOf owl:Thing 
        GAction actionRerootConf = new GAction(GActionType.REROOTING);
        
        // We now create the configuration we will specify to the generic loader
        GraphConf gConf = new GraphConf();
        gConf.addGDataConf(dataConf);
        gConf.addGAction(actionTypeConf);
        gConf.addGAction(actionRerootConf);
        
        GraphLoaderGeneric.load(gConf,g);
        
        System.out.println(g.toString());
        
        // The taxonomy is now a rDAG, i.e. rooted Directed Acyclic Graph.
        // Check by yourself
        Set<V> roots = new ValidatorDAG().getTaxonomicDAGRoots(g);
        System.out.println("Roots: "+roots);
        
        
        // We compute the similarity between two concepts 
        URI wikiRugbyFoorballerURI = factory.createURI(yagoURI.stringValue() + "wikicategory_Rugby_footballers"); 
        URI WordnetSoccerPlayerURI = factory.createURI(yagoURI.stringValue() + "wordnet_soccer_player_110618342");

        // First we configure an intrincic IC 
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_DEPTH_MAX_NONLINEAR);
        // Then we configure the pairwise measure to use, we here choose to use Lin formula
        SMconf smConf = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);

        // We define the engine used to compute the similarity
        SM_Engine engine = new SM_Engine(g);

        // We retrieve the vertices corresponding to the two concepts
        V strokeVertex = g.getV(wikiRugbyFoorballerURI);
        V myocardiumVertex = g.getV(WordnetSoccerPlayerURI);

        double sim = engine.computePairwiseSim(smConf, strokeVertex, myocardiumVertex);
        System.out.println("Similarity: " + sim);

        /* 
         * Notice that the first computation is expensive as the engine compute the IC and extra information 
         * which are cached by the engine
         * Let's perform 10000 random computations (we only print some results).
         * We retrieve the set of vertices as a list
         */
        int totalComparison = 10000;

        List<V> listVertices = new ArrayList<V>(g.getV());
        int nbConcepts = listVertices.size();
        int id1, id2;
        V c1, c2;
        String idC1, idC2;
        Random r = new Random();

        for (int i = 0; i < totalComparison; i++) {
            id1 = r.nextInt(nbConcepts);
            id2 = r.nextInt(nbConcepts);

            c1 = listVertices.get(id1);
            c2 = listVertices.get(id2);

            sim = engine.computePairwiseSim(smConf, c1, c2);

            if ((i + 1) % 1000 == 0) {
                idC1 = ((URI) c1.getValue()).getLocalName();
                idC2 = ((URI) c2.getValue()).getLocalName();

                System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
            }
        }
        t.stop();
        t.elapsedTime();
        
    }
    
}
