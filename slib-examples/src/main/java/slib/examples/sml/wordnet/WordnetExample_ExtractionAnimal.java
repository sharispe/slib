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
package slib.examples.sml.wordnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.indexer.wordnet.IndexerWordNetBasic;
import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.wordnet.GraphLoader_Wordnet;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.UtilDebug;

/**
 * Simple example which how to take advantage of the Semantic Measures Library
 * to compute the semantic similarity of two nouns defined in WordNet 3.1
 *
 * Documentation: http://www.semantic-measures-library.org
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class WordnetExample_ExtractionAnimal {

    public static void main(String[] args) throws SLIB_Ex_Critic, SLIB_Exception {

        // Location of WordNet Data
        String dataloc = "/data/WordNet/WordNet-3.1/dict/";

        // We create the graph
        URIFactory factory = URIFactoryMemory.getSingleton();
        URI guri = factory.getURI("http://graph/wordnet/");
        G wordnet = new GraphMemory(guri);

        // We load the data into the graph
        GraphLoader_Wordnet loader = new GraphLoader_Wordnet();

        GDataConf dataNoun = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data.noun");

        loader.populate(dataNoun, wordnet);

        // We root the graph which has been loaded (this is optional but may be required to compare synset which do not share common ancestors).
        GAction addRoot = new GAction(GActionType.REROOTING);
        GraphActionExecutor.applyAction(addRoot, wordnet);

        // This is optional. It just shows which are the synsets which are not subsumed
        ValidatorDAG validatorDAG = new ValidatorDAG();
        Set<URI> roots = validatorDAG.getTaxonomicRoots(wordnet);
        System.out.println("Roots: " + roots);

        // We create an index to map the nouns to the vertices of the graph
        // We only build an index for the nouns in this example
        String data_noun = dataloc + "index.noun";

        IndexerWordNetBasic indexWordnetNoun = new IndexerWordNetBasic(factory, wordnet, data_noun);
        Map<URI, Set<String>> indexURI2String = new HashMap();

        for (Map.Entry<String, Set<URI>> entry : indexWordnetNoun.getIndex().entrySet()) {

            String localNames = "";
            for (URI u : entry.getValue()) {

                if (!indexURI2String.containsKey(u)) {
                    Set<String> labels = new HashSet();
                    labels.add(entry.getKey().replace("_", " "));
                    indexURI2String.put(u, labels);

                } else {
                    indexURI2String.get(u).add(entry.getKey().replace("_", " "));
                }

                if (!localNames.isEmpty()) {
                    localNames += ", ";
                }
                localNames += u.getLocalName();
                
                System.out.println(u + "\t" + entry.getKey().replace("_", " "));
            }
            //System.out.println(entry.getKey().replace("_", " ") + "\t" + localNames);

        }

        UtilDebug.exit();

        System.out.println("URI TO LABELS INDEX");
        for (Map.Entry<URI, Set<String>> entry : indexURI2String.entrySet()) {

            String labels = "";

            for (String l : entry.getValue()) {

                if (!labels.isEmpty()) {
                    labels += ", ";
                }
                labels += l;
            }

//            System.out.println(entry.getKey().getLocalName() + "\t" + labels);
        }

        Set<URI> uris_animal = indexWordnetNoun.get("animal");
        System.out.println("Animal " + uris_animal);

//        // In this example we only consider nouns associated to a single URI so we retrieve their URI
        URI uri_animal = uris_animal.iterator().next();

//        URI uri_instant_coffee = uris_instant_coffee.iterator().next();
//        URI uri_green_tea = uris_green_tea.iterator().next();
//
//        // We configure a pairwise semantic similarity measure, 
//        // i.e., a measure which will be used to assess the similarity 
//        // of two nouns regarding their associated vertices in WordNet
//        ICconf iconf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
//        SMconf measureConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
//        measureConf.setICconf(iconf);
//
//        // We define the engine used to compute the score of the configured measure
//        // several preprocessing will be made prior to the first computation, e.g. to compute the Information Content (IC)
//        // of the vertices. This may take some few secondes
        SM_Engine engine = new SM_Engine(wordnet);

        System.out.println("Graph ======================");
        Set<URI> classes = engine.getClasses();

        for (URI u : wordnet.getV()) {

            if (!classes.contains(u)) {
                continue;
            }

            String ancestorsLocalNames = "";
            Set<URI> ancs = engine.getAncestorsInc(u);
            for (URI a : ancs) {
                if (!ancestorsLocalNames.isEmpty()) {
                    ancestorsLocalNames += ", ";
                }
                ancestorsLocalNames += a.getLocalName();
            }
            System.out.println(u.getLocalName() + "\t" + ancestorsLocalNames);
        }

        System.out.println("=============");

        UtilDebug.exit();

        Set<URI> animalsUris = engine.getDescendantsInc(uri_animal);
        for (URI u : animalsUris) {
            System.out.println(u + "\t" + indexURI2String.get(u));
        }
    }
}
