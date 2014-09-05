/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.examples.sml.snomedct;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.openrdf.model.URI;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.loader.bio.snomedct.GraphLoaderSnomedCT_RF2;
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
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

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
        URI snomedctURI = factory.getURI("http://snomedct/");
        G g = new GraphMemory(snomedctURI);

        GDataConf conf = new GDataConf(GFormat.SNOMED_CT_RF2);
        conf.addParameter(GraphLoaderSnomedCT_RF2.ARG_CONCEPT_FILE, SNOMEDCT_CONCEPT);
        conf.addParameter(GraphLoaderSnomedCT_RF2.ARG_RELATIONSHIP_FILE, SNOMEDCT_RELATIONSHIPS);

        GraphLoaderGeneric.populate(conf, g);

        System.out.println(g.toString());

        // We compute the similarity between the concepts 
        // associated to Heart	and Myocardium, i.e. 80891009 and 74281007 respectively
        // We first build URIs correspondind to those concepts
        URI heartURI = factory.getURI(snomedctURI.stringValue() + "80891009"); // i.e http://snomedct/230690007
        URI myocardiumURI = factory.getURI(snomedctURI.stringValue() + "74281007");

        // First we configure an intrincic IC 
        ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
        // Then we configure the pairwise measure to use, we here choose to use Lin formula
        SMconf smConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);
        
//        UtilDebug.exit();

        // We define the engine used to compute the similarity
        SM_Engine engine = new SM_Engine(g);

        // We retrieve the vertices corresponding to the two concepts

        double sim = engine.compare(smConf, heartURI, myocardiumURI);
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

            sim = engine.compare(smConf, c1, c2);

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
