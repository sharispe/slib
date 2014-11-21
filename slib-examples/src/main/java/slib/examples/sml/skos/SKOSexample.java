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
package slib.examples.sml.skos;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.SKOS;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SKOSexample {

    public static void main(String[] args) throws SLIB_Ex_Critic {

        URIFactory f = URIFactoryMemory.getSingleton();
        URI gURI = f.getURI("http://graph");
        URI root = f.getURI("http://test/root");
        URI a = f.getURI("http://test/a");
        URI a1 = f.getURI("http://test/a1");
        URI a2 = f.getURI("http://test/a2");
        URI b = f.getURI("http://test/b");

        G graph = new GraphMemory(gURI);
        graph.addE(a, SKOS.BROADER_TRANSITIVE, root);
        graph.addE(b, SKOS.BROADER_TRANSITIVE, root);
        graph.addE(a1, SKOS.BROADER_TRANSITIVE, a);
        graph.addE(a2, SKOS.BROADER_TRANSITIVE, a);

        for (E e : graph.getE()) {
            System.out.println(e);
        }
        // In this case we have to configure the engine such as SKOS.BROADER_TRANSITIVE
        // relationship is considered as structuring relationship
        SM_Engine e = new SM_Engine(graph, SetUtils.buildSet(SKOS.BROADER_TRANSITIVE), null, null);
        SMconf m = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_EDGE_WU_PALMER_1994);

        double sim_a1_a1 = e.compare(m, a1, a1);
        double sim_a1_a2 = e.compare(m, a1, a2);
        double sim_a1_b = e.compare(m, a1, b);

        System.out.println("sim_a1_a1: " + sim_a1_a1);
        System.out.println("sim_a1_a2: " + sim_a1_a2);
        System.out.println("sim_a1_b: " + sim_a1_b);
    }
}
