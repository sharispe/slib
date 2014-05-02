package slib.examples.sml.snomedct;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.openrdf.model.URI;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.bio.snomedct.GraphLoaderSnomedCT_RF2;
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
import slib.utils.impl.Timer;
import slib.utils.impl.UtilDebug;

/**
 *
 * Example of a Semantic measure computation using the Semantic Measures
 * Library. In this snippet we estimate the similarity of two concepts expressed
 * in the SNOMED-CT semantic Graph.
 *
 * More information at http://www.semantic-measures-library.org/
 *
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root
 * element, change value="INFO" to value="DEBUG"
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SMComputationSnomedCT {

    public static void main(String[] params) throws SLIB_Exception {

        // Some variables defining the locations of the files from which the Snomed-CT will be loaded
        // The loader we use requires Snomed-CT expressed using RF2 format. 
        String DATA_DIR = "/data"; // this is the directory in which the downloaded snomed version has been extracted
        String SNOMEDCT_VERSION = "20120731";
        String SNOMEDCT_DIR = DATA_DIR + "/SnomedCT_Release_INT_" + SNOMEDCT_VERSION + "/RF2Release/Full/Terminology";
        String SNOMEDCT_CONCEPT = SNOMEDCT_DIR + "/sct2_Concept_Full_INT_" + SNOMEDCT_VERSION + ".txt";
        String SNOMEDCT_RELATIONSHIPS = SNOMEDCT_DIR + "/sct2_Relationship_Full_INT_" + SNOMEDCT_VERSION + ".txt";

        // We configure a timer
        Timer t = new Timer();
        t.start();

        // We create an in-memory graph in which we will load Snomed-CT.
        // Notice that Snomed-CT is quite large (e.g. version 20120731 contains 296433 concepts and872318 relationships ).
        // You will need to allocate extra memory to the JVM e.g add -Xmx3000m parameter to allocate 3Go.
        URIFactory factory = URIFactoryMemory.getSingleton();
        URI snomedctURI = factory.createURI("http://snomedct/");
        G g = new GraphMemory(snomedctURI);

        GDataConf conf = new GDataConf(GFormat.SNOMED_CT_RF2);
        conf.addParameter(GraphLoaderSnomedCT_RF2.ARG_CONCEPT_FILE, SNOMEDCT_CONCEPT);
        conf.addParameter(GraphLoaderSnomedCT_RF2.ARG_RELATIONSHIP_FILE, SNOMEDCT_RELATIONSHIPS);

        GraphLoaderGeneric.populate(conf, g);

        System.out.println(g.toString());

        // We compute the similarity between the concepts 
        // associated to Heart	and Myocardium, i.e. 80891009 and 74281007 respectively
        // We first build URIs correspondind to those concepts
        URI heartURI = factory.createURI(snomedctURI.stringValue() + "80891009"); // i.e http://snomedct/230690007
        URI myocardiumURI = factory.createURI(snomedctURI.stringValue() + "74281007");

        // First we configure an intrincic IC 
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
        // Then we configure the pairwise measure to use, we here choose to use Lin formula
        SMconf smConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);
        
//        UtilDebug.exit();

        // We define the engine used to compute the similarity
        SM_Engine engine = new SM_Engine(g);

        // We retrieve the vertices corresponding to the two concepts

        double sim = engine.computePairwiseSim(smConf, heartURI, myocardiumURI);
        System.out.println("Similarity Heart/Myocardium: " + sim);

        /* 
         * Notice that the first computation is expensive as the engine compute the IC and extra information 
         * which are cached by the engine
         * Let's perform 100000 random computations (we only print some results).
         * We retrieve the set of vertices as a list
         */
        int totalComparison = 100000;

        List<URI> listVertices = new ArrayList<URI>(g.getV());
        int nbConcepts = listVertices.size();
        int id1, id2;
        URI c1, c2;
        String idC1, idC2;
        Random r = new Random();

        for (int i = 0; i < totalComparison; i++) {
            id1 = r.nextInt(nbConcepts);
            id2 = r.nextInt(nbConcepts);

            c1 = listVertices.get(id1);
            c2 = listVertices.get(id2);

            sim = engine.computePairwiseSim(smConf, c1, c2);

            if ((i + 1) % 1000 == 0) {
                idC1 = c1.getLocalName();
                idC2 = c2.getLocalName();

                System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
            }
        }
        t.stop();
        t.elapsedTime();
    }
}
