/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.examples.sml.wordnet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.wordnet.GraphLoader_Wordnet_Full;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.indexer.wordnet.IndexerWordNetBasic;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author sharispe
 */
public class WordNetExample_FullLoader {

    public static void main(String[] args) throws SLIB_Ex_Critic, SLIB_Exception {

        String dataloc = "/data/WordNet/WordNet-3.1/dict/";

        String[] functions = {"noun", "verb", "adj", "adv"};

        URIFactory factory = URIFactoryMemory.getSingleton();
        URI guri = factory.getURI("http://graph/wordnet/");
        G wordnet = new GraphMemory(guri);

        // We load WordNet as well as an index
        GraphLoader_Wordnet_Full wordnetLoader = new GraphLoader_Wordnet_Full();
        IndexerWordNetBasic indexWordnetWordUris = new IndexerWordNetBasic(wordnet);

        for (String function : functions) {
            GDataConf data = new GDataConf(GFormat.WORDNET_DATA, dataloc + "data." + function);
            wordnetLoader.populate(data, wordnet);
            indexWordnetWordUris.populateIndex(factory, dataloc + "index." + function);
        }

        // reverse index to be able to retrieve the labels associated to an URI
        Map<URI, Set<String>> invertedIndex = new HashMap();
        for (Map.Entry<String, Set<URI>> e : indexWordnetWordUris.getIndex().entrySet()) {
            String label = e.getKey();
            Set<URI> uris = e.getValue();

            for (URI u : uris) {
                if(!invertedIndex.containsKey(u)){
                    invertedIndex.put(u,new HashSet<String>());
                }
                invertedIndex.get(u).add(label);
            }
        }

        // print the graph
        //        for (E e : wordnet.getE()) {
        //            System.out.println(e);
        //        }
        /**
         * SML Engine expects a directed acyclic graph structured by
         * RDFS.SUBCLASSOF relationships in order to compute similarities
         * between graph node. We can therefore build a graph respecting those
         * constraints by analyzing the loaded graph. However note that
         * replacing Hyponym / Hypernym / instance Hyponym / instance Hypernym
         * by corresponding RDFS.SUBCLASSOF relationships corresponds to using
         * classical GraphLoader_Wordnet loader Basically the mapping is done by
         * considering: (i) x hyponym y -> x RDFS.SUBCLASSOF (ii)y x instance
         * hypernym y -> x RDFS.SUBCLASSOF y (iii) x hyponym y -> y
         * REDFS.SUBCLASSOF x (iv) x instance hyponym y -> y REDFS.SUBCLASSOF x
         */
        // We can also design custom similarity function
        // or just process the graph
        // Here an example to retrieve similar adjectives
        // as defined at https://wordnet.princeton.edu/man/wninput.5WN.html
        URI similarTo = GraphLoader_Wordnet_Full.pointerSymbolsToURIs.get("&");
        // or URI similarTo = factory.getURI("http://SML/wordNet/SimilarTo");

        for (E e : wordnet.getE(similarTo)) {
            
            URI source = e.getSource();
            URI target = e.getTarget();
            
            System.out.println(e);
            
            // Retrieve first label associated to each URI
            // Note that sense are not loaded
            // cf Wordnet distrib /dict/index.sense
            String firstLabelSource = invertedIndex.containsKey(source) ? invertedIndex.get(source).iterator().next() : "NO_LABEL (SENSE)";
            String firstLabelTarget = invertedIndex.containsKey(target) ? invertedIndex.get(target).iterator().next() : "NO_LABEL (SENSE)";
            
            
            System.out.println(firstLabelSource+"\tsimilarTo\t"+firstLabelTarget);
        }

        // The mapping between symbols and URIs considered by SML is specified in
        // GraphLoader_WordNet_Full class (correspondance copy pasted below):
        // !    Antonym     http://SML/wordNet/Antonym  Noun, Verb, Adjective, Adverb
        // @    Hypernym    http://SML/wordNet/Hypernym Noun, Verb
        // @i    Instance Hypernym http://SML/wordNet/InstanceHypernym Noun
        // ~    Hyponym     http://SML/wordNet/Hyponym Noun, Verb
        // ~i    Instance Hyponym http://SML/wordNet/InstanceHyponym Noun
        // #m    Member holonym http://SML/wordNet/MemberHolonym Noun
        // #s    Substance holonym http://SML/wordNet/SubstanceHolonym Noun
        // #p    Part holonym http://SML/wordNet/PartHolonym Noun
        // %m    Member meronym http://SML/wordNet/MemberMeronym Noun
        // %s    Substance meronym http://SML/wordNet/SubstanceMeronym Noun
        // %p    Part meronym http://SML/wordNet/PartMeronym Noun
        // =    Attribute http://SML/wordNet/Attribut Noun, Adjective
        // +    Derivationally related form  http://SML/wordNet/DerivationallyRelatedForm Noun, Verb       
        // ;c    Domain of synset - TOPIC http://SML/wordNet/DomainOfTopic Noun, Verb, Adjective, Adverb
        // -c    Member of this domain - TOPIC http://SML/wordNet/MemberOfTopic Noun
        // ;r    Domain of synset - REGION  http://SML/wordNet/DomainOfRegion Noun, Verb, Adjective, Adverb
        // -r    Member of this domain - REGION http://SML/wordNet/MemberOfRegion Noun
        // ;u    Domain of synset - USAGE http://SML/wordNet/DomainOfUsage Noun, Adjective, Adverb
        // -u    Member of this domain - USAGE http://SML/wordNet/MemberOfUsage Noun, Verb
        // &    Similar to http://SML/wordNet/SimilarTo Adjective
        // *    Entailment  http://SML/wordNet/Entailment Verb
        // >    Cause   http://SML/wordNet/Cause Verb
        // ^    Also see   http://SML/wordNet/AlsoSee Verb, Adjective
        // $    Verb group   http://SML/wordNet/VerbGroup Verb
        // <    Cause   http://SML/wordNet/ParticipleOfVerb Adjective
        // \    Cause   http://SML/wordNet/DerivedPertains Adjective, Adverbe
    }
}
