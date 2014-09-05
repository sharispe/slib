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
package slib.examples.sml.mesh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openrdf.model.URI;
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
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class MeSHExample_XML {

    public static void main(String[] args) {

        try {

            Timer t = new Timer();
            t.start();

            URIFactory factory = URIFactoryMemory.getSingleton();
            URI meshURI = factory.getURI("http://www.nlm.nih.gov/mesh/");

            G meshGraph = new GraphMemory(meshURI);

            GDataConf dataMeshXML = new GDataConf(GFormat.MESH_XML, "/data/mesh/2014/desc2014.xml"); // the DTD must be located in the same directory
            GraphLoaderGeneric.populate(dataMeshXML, meshGraph);

            System.out.println(meshGraph);

            /* 
             * Now we compute Semantic Similarities between pairs vertices
             */
            // we first configure a pairwise measure
            ICconf icConf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SANCHEZ_2011);
            SMconf measureConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998, icConf);

            // We define the semantic measure engine to use 
            SM_Engine engine = new SM_Engine(meshGraph);

            // We compute semantic similarities between concepts
            // e.g. between Paranoid Disorders (D010259) and Schizophrenia, Paranoid (D012563)
            URI c1 = factory.getURI("http://www.nlm.nih.gov/mesh/D010259"); // Paranoid Disorders
            URI c2 = factory.getURI("http://www.nlm.nih.gov/mesh/D012563"); // Schizophrenia, Paranoid

            // We compute the similarity
            double sim = engine.compare(measureConf, c1, c2);
            System.out.println("Sim " + c1 + "\t" + c2 + "\t" + sim);

            System.out.println(meshGraph.toString());

            /* 
             * The computation of the first similarity is not very fast because   
             * the engine compute extra informations which are cached for next computations.
             * Lets compute 10 000 000 random pairwise similarities
             */
            int totalComparison = 10000000;
            List<URI> concepts = new ArrayList<URI>(meshGraph.getV());
            int id1, id2;
            String idC1, idC2;
            Random r = new Random();

            for (int i = 0; i < totalComparison; i++) {
                id1 = r.nextInt(concepts.size());
                id2 = r.nextInt(concepts.size());

                c1 = concepts.get(id1);
                c2 = concepts.get(id2);

                sim = engine.compare(measureConf, c1, c2);

                if ((i + 1) % 50000 == 0) {
                    idC1 = c1.getLocalName();
                    idC2 = c2.getLocalName();

                    System.out.println("Sim " + (i + 1) + "/" + totalComparison + "\t" + idC1 + "/" + idC2 + ": " + sim);
                }
            }
            t.stop();
            t.elapsedTime();
        } catch (SLIB_Exception ex) {
            Logger.getLogger(MeSHExample_XML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
