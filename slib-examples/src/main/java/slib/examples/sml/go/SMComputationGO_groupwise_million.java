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
package slib.examples.sml.go;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.algo.extraction.rvf.instances.InstancesAccessor;
import slib.graph.algo.extraction.rvf.instances.impl.InstanceAccessor_RDF_TYPE;
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
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * Example of a Semantic measure computation using the Semantic Measures
 * Library. In this snippet we estimate the similarity of two genes annotated by
 * concepts (terms) defined in the Gene Ontology. The Gene Ontology is expressed
 * in OBO format. The similarity is estimated using an indirect groupwise
 * measure based on: Lin's pairwise measure, Best Match Average aggregation
 * strategy.
 *
 * More information at http://www.semantic-measures-library.org/
 *
 * Note that you can set the LOG level in specified in log4j.xml, e.g. in root
 * element, change value="INFO" to value="DEBUG"
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SMComputationGO_groupwise_million {

    public static void main(String[] params) throws SLIB_Exception {

        Timer t = new Timer();
        t.start();

        // Configuration files, set the file path according to your configuration.
        // The Gene Ontology (OBO format)
        String goOBO = "/data/go/gene_ontology_ext.obo";
        String annot = "/data/go/gene_association.goa_human";


        URIFactory factory = URIFactoryMemory.getSingleton();
        URI graph_uri = factory.getURI("http://go/");

        // We define a prefix in order to build valid uris from ids such as GO:XXXXX, 
        // considering the configuration specified below the URI associated to GO:XXXXX will be http://go/XXXXX
        factory.loadNamespacePrefix("GO", graph_uri.toString());


        G graph = new GraphMemory(graph_uri);

        GDataConf goConf = new GDataConf(GFormat.OBO, goOBO);
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
        URI virtualRoot = factory.getURI("http://go/virtualRoot");
        graph.addV(virtualRoot);

        // We root the graphs using the virtual root as root
        GAction rooting = new GAction(GActionType.REROOTING);
        rooting.addParameter("root_uri", virtualRoot.stringValue());
        GraphActionExecutor.applyAction(factory, rooting, graph);

        System.out.println(graph.toString());

        int nbVertices = graph.getV().size();

        System.out.println("Nb vertices : " + nbVertices);




        ICconf icConf = new IC_Conf_Topo("Sanchez", SMConstants.FLAG_ICI_SANCHEZ_2011);

        // Then we define the Semantic measure configuration
        SMconf smConfPairwise = new SMconf("Lin", SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
        smConfPairwise.setICconf(icConf);

        SMconf smConfGroupwise = new SMconf("BMA_LIN", SMConstants.FLAG_SIM_GROUPWISE_BMA);

        SM_Engine engine = new SM_Engine(graph);



        // An object used to retrieve the annotation of an instance according 
        // to a particular semantic projection 
        InstancesAccessor iAccessor = new InstanceAccessor_RDF_TYPE(graph);

        List<URI> instances = new ArrayList<URI>(engine.getInstances());

        double sim;
        int count = 1000000;
        int size = instances.size();
        Random r = new Random();
        URI iA, iB;
        Set<URI> annots_A, annots_B;

        System.out.println("Start computing");

        for (int i = 1; i <= count; i++) {

            iA = instances.get(r.nextInt(size));
            iB = instances.get(r.nextInt(size));

            annots_A = iAccessor.getDirectClass(iA);
            annots_B = iAccessor.getDirectClass(iB);

            sim = engine.compare(smConfGroupwise, smConfPairwise, annots_A, annots_B);
            if (i % 100000 == 0) {
                System.out.println(i + "/" + count + "\t" + iA + "\t" + iB + "\t" + sim);
            }
        }
        System.out.println(count + " gene products semantic simlarity computed");
        t.stop();
        t.elapsedTime();
    }
}
