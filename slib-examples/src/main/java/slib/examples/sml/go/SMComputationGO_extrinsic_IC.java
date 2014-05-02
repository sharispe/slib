package slib.examples.sml.go;

import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import slib.sglib.algo.graph.accessor.GraphAccessor;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Corpus;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * Example which shows how to use the Semantic Measures Library to compute 
 * the Information Content (IC) of Gene Ontology terms using an Extrinsic IC 
 * (e.g. using Resnik IC). 
 * 
 * In this snippet we compute the IC of the GO terms defined in the 
 * Gene Ontology using Resnik IC formulation
 *
 * More information at http://www.semantic-measures-library.org/
 *
 * 
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SMComputationGO_extrinsic_IC {

    public static void main(String[] params) throws SLIB_Exception {

        // The input files.
        // - The Gene Ontology (OBO format) 
        // - Annotations (GAF2)
        String goOBO = "/data/go/gene_ontology_ext.obo";
        String annot = "/data/go/gene_association.goa_human";

        
        URIFactory factory = URIFactoryMemory.getSingleton();
        URI graph_uri = factory.createURI("http://go/");

        // We define a prefix in order to build valid uris from ids such as GO:XXXXX, 
        // considering the configuration specified below the URI associated 
        // to GO:XXXXX will be http://go/XXXXX
        factory.loadNamespacePrefix("GO", graph_uri.toString());

        // We configure the graph
        GraphConf graphConf = new GraphConf(graph_uri);
        graphConf.addGDataConf(new GDataConf(GFormat.OBO, goOBO));
        graphConf.addGDataConf(new GDataConf(GFormat.GAF2, annot));

        GAction rooting = new GAction(GActionType.REROOTING);
        rooting.addParameter("root_uri", OWL.THING.stringValue());

        graphConf.addGAction(rooting);

        G graph = GraphLoaderGeneric.load(graphConf);

        // General information about the graph
        System.out.println(graph.toString());

        // We retrieve only the classes, i.e. GO terms
        // Note that the graph also contains the genes
        Set<URI> goTerms = GraphAccessor.getClasses(graph);
        System.out.println("GO terms : " + goTerms.size());

        // We configure the IC
        ICconf icConfRes = new IC_Conf_Corpus(SMConstants.FLAG_IC_ANNOT_RESNIK_1995);
        
        SM_Engine engine = new SM_Engine(graph);

        for (URI goTerm : goTerms) {

            System.out.println(goTerm + "\t" + engine.getIC(icConfRes,goTerm));
        }
    }
}
