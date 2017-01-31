package slib.examples.graph.general;

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
import java.io.PrintWriter;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.conf.GraphConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;

public class BuildGraph {

    public static void main(String[] params) throws Exception {

        String graphFile = "/media/data/data/yago/data_non_utf8_formatted/yago_Tax_Types_for_slib.nt";

        URIFactory factory = URIFactoryMemory.getSingleton();
        URI graph_uri = factory.getURI("http://graph/");
        
        // We configure the graph
        GraphConf graphConf = new GraphConf(graph_uri);
        graphConf.addGDataConf(new GDataConf(GFormat.NTRIPLES, graphFile));

        GAction rooting = new GAction(GActionType.REROOTING);
        rooting.addParameter("root_uri", OWL.THING.stringValue());

        GAction tr = new GAction(GActionType.TRANSITIVE_REDUCTION);
        
        graphConf.addGAction(rooting);
        graphConf.addGAction(tr);
        
        G graph = GraphLoaderGeneric.load(graphConf);

        System.out.println(graph);
        
        ValidatorDAG validator = new ValidatorDAG();
        boolean isDag = validator.containsTaxonomicDagWithUniqueRoot(graph);
        System.out.println("DAG: "+isDag);
        
        PrintWriter writer = new PrintWriter("//media/data/data/yago/data_non_utf8_formatted/yago_Tax_Types_tr_rooted.nt");
        for(E e : graph.getE()){
            writer.println(e.getSource()+"\t"+e.getTarget());
        }
        writer.close();
    }
}
