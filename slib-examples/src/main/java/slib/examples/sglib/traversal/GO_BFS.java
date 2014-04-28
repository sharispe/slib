package slib.examples.sglib.traversal;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import slib.sglib.algo.graph.traversal.classical.BFS;
import slib.sglib.algo.graph.utils.GAction;
import slib.sglib.algo.graph.utils.GActionType;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraint;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sglib.utils.WalkConstraintGeneric;
import slib.utils.ex.SLIB_Exception;

/**
 * Basic example which shows how to perform a Breadth-First Search on the Gene
 * Ontology.
 *
 * @author Sébastien Harispe
 */
public class GO_BFS {

    public static void main(String[] params) throws SLIB_Exception {

        // - The Gene Ontology (OBO format) 
        // refer to http://www.geneontology.org
        String goOBO = "/data/go/gene_ontology_ext.obo";

        URIFactory factory = URIFactoryMemory.getSingleton();
        URI graph_uri = factory.createURI("http://go/");

        // We define a prefix in order to build valid uris from ids such as GO:XXXXX, 
        // considering the configuration specified below the URI associated 
        // to GO:XXXXX will be http://go/XXXXX
        factory.loadNamespacePrefix("GO", graph_uri.toString());

        // We configure the graph
        GraphConf graphConf = new GraphConf(graph_uri);
        graphConf.addGDataConf(new GDataConf(GFormat.OBO, goOBO));

        GAction rooting = new GAction(GActionType.REROOTING);
        rooting.addParameter("root_uri", OWL.THING.stringValue());

        graphConf.addGAction(rooting);

        G graph = GraphLoaderGeneric.load(graphConf);

        // General information about the graph
        System.out.println(graph.toString());

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);

        // The BFS will be performed from the root according to the walk constraint specified above
        // In this case, only rdfs:SubClassOf triplets (edges) will be considered and the traversal 
        // will always be made from the target to the source of the SubClassOf relationship
        BFS bfs = new BFS(graph, OWL.THING, wc); // you can do the same with the DFS class
        
        while(bfs.hasNext()){
            URI n = bfs.next();
            System.out.println("-"+n);
        }
        System.out.println("BFS done");
    }
}
