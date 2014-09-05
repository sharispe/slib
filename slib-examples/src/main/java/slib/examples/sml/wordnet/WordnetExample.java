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
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Simple example which how to take advantage of the Semantic Measures Library
 * to compute the semantic similarity of two nouns defined in WordNet 3.1
 *
 * Documentation: http://www.semantic-measures-library.org
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class WordnetExample {

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
        GDataConf dataVerb = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data.verb");
        GDataConf dataAdj = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data.adj");
        GDataConf dataAdv = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data.adv");

        loader.populate(dataNoun, wordnet);
        loader.populate(dataVerb, wordnet);
        loader.populate(dataAdj, wordnet);
        loader.populate(dataAdv, wordnet);

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

        // uncomment if you want to show the index, i.e. nouns and associated URIs (identifiers)
        for (Map.Entry<String, Set<URI>> entry : indexWordnetNoun.getIndex().entrySet()) {
            System.out.println(entry.getKey() + "\t" + entry.getValue());
        }

        // We focus on three specific nouns in this example
        // - iced_coffee	[http://graph/wordnet/07936780]
        // - instant_coffee	[http://graph/wordnet/07936903]
        // - green_tea          [http://graph/wordnet/07951392]
        // We retrive their identifiers
        Set<URI> uris_iced_coffee = indexWordnetNoun.get("iced_coffee");
        Set<URI> uris_instant_coffee = indexWordnetNoun.get("instant_coffee");
        Set<URI> uris_green_tea = indexWordnetNoun.get("green_tea");

        // Note that multiple URIs (identifiers) can be associated to the same noun
        // In this example we only consider nouns associated to a single URI so we retrieve their URI
        URI uri_iced_coffee = uris_iced_coffee.iterator().next();
        URI uri_instant_coffee = uris_instant_coffee.iterator().next();
        URI uri_green_tea = uris_green_tea.iterator().next();

        // We configure a pairwise semantic similarity measure, 
        // i.e., a measure which will be used to assess the similarity 
        // of two nouns regarding their associated vertices in WordNet
        ICconf iconf = new IC_Conf_Topo(SMConstants.FLAG_ICI_SECO_2004);
        SMconf measureConf = new SMconf(SMConstants.FLAG_SIM_PAIRWISE_DAG_NODE_LIN_1998);
        measureConf.setICconf(iconf);

        // We define the engine used to compute the score of the configured measure
        // several preprocessing will be made prior to the first computation, e.g. to compute the Information Content (IC)
        // of the vertices. This may take some few secondes
        SM_Engine engine = new SM_Engine(wordnet);

        // we compute the semantic similarities
        double sim_iced_coffee_vs_instant_coffee = engine.compare(measureConf, uri_iced_coffee, uri_instant_coffee);
        double sim_iced_coffee_vs_green_tea = engine.compare(measureConf, uri_iced_coffee, uri_green_tea);

        // That's it
        System.out.println("sim(iced_coffee,instant_coffee) = " + sim_iced_coffee_vs_instant_coffee);
        System.out.println("sim(iced_coffee,green_tea)      = " + sim_iced_coffee_vs_green_tea);

        // Which prints
        // sim(iced_coffee,instant_coffee) = 0.7573022852697784
        // sim(iced_coffee,green_tea)      = 0.3833914674618656
    }
}
