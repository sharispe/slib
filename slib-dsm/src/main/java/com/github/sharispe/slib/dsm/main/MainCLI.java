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
package com.github.sharispe.slib.dsm.main;

import com.github.sharispe.slib.dsm.core.corpus.Corpus;
import com.github.sharispe.slib.dsm.core.corpus.CorpusFromFile;
import com.github.sharispe.slib.dsm.core.corpus.CorpusFromFileDir;
import com.github.sharispe.slib.dsm.core.engine.VocStatComputer;
import com.github.sharispe.slib.dsm.core.engine.VocStatComputerThreads;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIteratorConstraint;
import com.github.sharispe.slib.dsm.core.model.access.ModelAccessor;
import com.github.sharispe.slib.dsm.core.model.access.twodmodels.IndexedVectorInfoIterator;
import com.github.sharispe.slib.dsm.core.model.access.twodmodels.ModelAccessorPersistance_2D;
import com.github.sharispe.slib.dsm.core.model.access.twodmodels.ModelAccessor_2D;
import com.github.sharispe.slib.dsm.core.model.utils.IndexedVector;
import com.github.sharispe.slib.dsm.core.model.utils.ModelUtil;
import com.github.sharispe.slib.dsm.core.model.utils.IndexedVectorInfo;
import com.github.sharispe.slib.dsm.core.model.utils.compression.CompressionUtils;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import com.github.sharispe.slib.dsm.utils.RQueue;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.Console;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class MainCLI {

    static Logger logger = LoggerFactory.getLogger(MainCLI.class);
    private final static String VERSION = "0.1-beta";

    public static void log(String l) {
        logger.info(l);
    }

    /**
     * remove first element
     *
     * @param arr array
     * @return the modified array
     */
    public static String[] shift(String[] arr) {
        return Arrays.copyOfRange(arr, 1, arr.length);
    }

    public static void showdoc() throws IOException {

        log("---------------------------------------------------------------------");
        log("Please use one of the following tools: ");
        log("---------------------------------------------------------------------");
        log("---------------------------------------------------------------------");
        log("Tools used for computing the similarities using a model (word representation)");
        log("---------------------------------------------------------------------");
        log("- sim: compute the similarity between two elements considering a model");
        log("- bestsim: compute the elements which are the most similar to the given one");
        log("- get_sim: access to the similarity between two elements considering a result matrix");
        log("- get_bestsim: compute the terms/docs which are the most similar to the given elements considering a result matrix");
        log("---------------------------------------------------------------------");
        log("---------------------------------------------------------------------");
        log("Tools used for computing models (e.g. word representation)");
        log("---------------------------------------------------------------------");
        log("- lem: use StanfordCoreNLP library to lemmatize the files contained into a directory - only words tagged with NN,NNS, NNP and VB  penn tags are conserved.");
        log("- voc_index: analyse a corpus by extracting the vocabulary considering specified constraint and create an index - basic statistics are also computed");
        log("- voc_index_dictionary: analyse a corpus to create an index of the given vocabulary- basic statistics are also computed");
        log("- merge_voc_index: merge voc indexes computed using voc_index");
        log("- compute_stat_voc: compute basic statistics on the given vocabulary - for each n-gram size x this command computes (1) the n-grams with the maximal number of occurrences and (2) the distributions (a): number of occurrences / number of ngram of size x, (b) number of occurrences / number of words with a number of occurrences lower or equal than the number of occurrences (percentage is also printed). ");
        log("- reduce_index_nb_occ: reduce a vocabulary based on the number of occurrences of words");
        log("- reduce_index_using_voc: reduce a vocabulary index considering a given vocabulary");
        log("- compute_word_cocc: compute the coocurences between words of a specific vocabulary considering a specific window size");
        log("- build_model: build a Distributional Model (DM) of the terms considering a vocabulary and a directory of files");
        log("- reduce_model: reduce the number of dimensions of a model");
        log("- show_vec output: the compressed representation of a vector");
        log("- check_null_vec: check the number of vectors which are null");
        log("An example of use can be:\n"
                + "\tlem to lemmatize a set of documents\n"
                + "\tvoc_index to extract the vocabulary\n"
                + "\tbuild_model to build a distributional model\n"
                + "\treduce_model to reduce a distributional model, e.g. for considering only informative dimensions\n"
                + "\tshow_vec output the compressed representation of a vector\n"
                + "\tsim to compute the similarity between terms or entities\n"
                + "\tbestsim to distinguish the k entities which are the more similar to the given one\n");
        log("---------------------------------------------------------------------");
        log("Useful commands:\n");
        log("Extract all the words from an index: cat /tmp/voc_stats/[0-9]* | awk '{print $1}'\n\n");
        log("Experimental:\n");
        log("- compute_pmi: compute 2-gram PMI");
        log("- normalize: normalize the vector representations contained into a model (locally in each vector using cross-multiplication)");
        log("---------------------------------------------------------------------");
    }

    public static void main(String[] argv) {

        try {

            log("---------------------------------------------------------------------");
            log("SLIB-DSM " + VERSION);
            log("---------------------------------------------------------------------");
            log("args: " + Arrays.toString(argv));

            if (argv.length != 0) {

                String cmd = argv[0].toLowerCase();
                argv = shift(argv);

                switch (cmd) {
                    case "lem":
                        CMD_LEM(argv);
                        break;
                    case "voc_index":
                        CMD_VOC_INDEX(argv);
                        break;
                    case "voc_index_dictionary":
                        CMD_VOC_INDEX_DICTIONNARY(argv);
                        break;
                    case "merge_voc_index":
                        CMD_MERGE_VOC_INDEX(argv);
                        break;
                    case "compute_stat_voc":
                        CMD_COMPUTE_VOC_STAT(argv);
                        break;
                    case "reduce_index_nb_occ":
                        CMD_REDUCE_INDEX_NB_OCC(argv);
                        break;
                    case "reduce_index_using_voc":
                        CMD_REDUCE_INDEX_USING_VOC(argv);
                        break;
                    case "compute_word_cocc":
                        CMD_COMPUTE_WORD_COOCC(argv);
                        break;
                    case "build_model":
                        CMD_BUILD_MODEL(argv);
                        break;
                    case "reduce_model":
                        CMD_REDUCE_MODEL(argv);
                        break;
                    case "sim":
                        CMD_SIM(argv);
                        break;
                    case "get_sim":
                        CMD_GET_SIM(argv);
                        break;
                    case "bestsim":
                        CMD_BEST_SIM(argv);
                        break;
                    case "get_bestsim":
                        CMD_GET_BEST_SIM(argv);
                        break;

                    // UTILS
                    case "show_vec":
                        CMD_SHOW_VEC(argv);
                        break;

                    // EXPERIMENTAL
                    case "reduce_word_cocc_matrix":
                        CMD_REDUCE_WORD_COOCC_MATRIX(argv);
                        break;
                    case "compute_pmi":
                        CMD_COMPUTE_PMI(argv);
                        break;
                    default:
                        showdoc();
                        break;
                }
            } else {
                showdoc();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
    }

    private static void CMD_SIM(String[] argv) throws SLIB_Ex_Critic, IOException {

        if (argv.length < 2 || argv.length > 3) {
            log("[1] directory which contains the distributional model");
            log("[2] entity label A|entity label B or -i if you want to use the interactive mode");
            log("[3] entity label B (if -i is not used)");
            System.exit(0);
        }
        String dm_dir = argv[0];
        ModelConf modelConf = ModelConf.load(dm_dir);
        ModelAccessor modelAccessor = new ModelAccessorPersistance_2D(modelConf);

        if (argv[1].toLowerCase().equals("-i")) { // interactive mode

            while (true) {
                log("---------------------------------------");
                String entityA = getInput("Please type a label for entity A -- type quit() to stop: ");
                if (entityA.equals("quit()")) {
                    break;
                }
                String entityB = getInput("Please type a label for entity B -- type quit() to stop: ");

                IndexedVectorInfo entityA_vecInfo = ModelUtil.searchEntityVectorInfo(modelConf, entityA);
                IndexedVectorInfo entityB_vecInfo = ModelUtil.searchEntityVectorInfo(modelConf, entityB);

                log(entityA + ":" + entityA_vecInfo);
                log(entityB + ":" + entityB_vecInfo);

                if (entityA_vecInfo == null) {
                    log("Index does not contain label : " + entityA);
                } else if (entityB_vecInfo == null) {
                    log("Index does not contain label : " + entityB);
                } else {
                    double sim = SlibDist_Wrapper.computeEntitySimilarity(modelAccessor, entityA_vecInfo, entityB_vecInfo);
                    log("sim " + entityA + "/" + entityB + " = " + sim);
                }
            }
        } else {

            String entityA = argv[1];
            String entityB = argv[2];

            log("Comparing " + entityA + "/" + entityB);
            IndexedVectorInfo entityA_vecInfo = ModelUtil.searchEntityVectorInfo(modelConf, entityA);
            IndexedVectorInfo entityB_vecInfo = ModelUtil.searchEntityVectorInfo(modelConf, entityB);

            log(entityA + ":" + entityA_vecInfo);
            log(entityB + ":" + entityB_vecInfo);

            if (entityA_vecInfo == null) {
                log("Index does not contain label : " + entityA);
            } else if (entityB_vecInfo == null) {
                log("Index does not contain label : " + entityB);
            } else {
                double sim = SlibDist_Wrapper.computeEntitySimilarity(modelAccessor, entityA_vecInfo, entityB_vecInfo);
                log("sim " + entityA + "/" + entityB + " = " + sim);
            }
        }
    }

    private static void CMD_GET_SIM(String[] argv) throws SLIB_Ex_Critic, IOException {

        if (argv.length < 2 || argv.length > 3) {
            log("[1] directory which contains the similarity results");
            log("[2] entity label A|entity label B or -i if you want to use the interactive mode");
            log("[3] entity label B (if -i is not used)");
            System.exit(0);
        }
        String dm_dir = argv[0];
        ModelConf modelConf = ModelConf.load(dm_dir);
        Iterator<IndexedVectorInfo> itIndexedVectorInfo = new IndexedVectorInfoIterator(modelConf);
        Map<String, IndexedVectorInfo> index = new HashMap<String, IndexedVectorInfo>();
        logger.info("Loading index into memory");
        while (itIndexedVectorInfo.hasNext()) {
            IndexedVectorInfo i = itIndexedVectorInfo.next();
            index.put(i.label, i);
        }

        ModelAccessor modelAccessor = new ModelAccessorPersistance_2D(modelConf);

        if (argv[1].toLowerCase().equals("-i")) { // interactive mode

            while (true) {
                log("---------------------------------------");
                String entityA = getInput("Please type a label for entity A -- type quit() to stop: ");
                if (entityA.equals("quit()")) {
                    break;
                }
                String entityB = getInput("Please type a label for entity B -- type quit() to stop: ");

                IndexedVectorInfo entityA_vecInfo = index.get(entityA);
                IndexedVectorInfo entityB_vecInfo = index.get(entityB);

                log(entityA + ":" + entityA_vecInfo);
                log(entityB + ":" + entityB_vecInfo);

                if (entityA_vecInfo == null) {
                    log("Index does not contain label : " + entityA);
                } else if (entityB_vecInfo == null) {
                    log("Index does not contain label : " + entityB);
                } else {
                    double[] vec_sim = modelAccessor.vectorRepresentationOf(entityA_vecInfo).values;
                    double sim = vec_sim[entityB_vecInfo.id];
                    log("sim " + entityA + "/" + entityB + " = " + sim);
                }
            }
        } else {

            String entityA = argv[1];
            String entityB = argv[2];

            log("Comparing " + entityA + "/" + entityB);
            IndexedVectorInfo entityA_vecInfo = index.get(entityA);
            IndexedVectorInfo entityB_vecInfo = index.get(entityB);

            log(entityA + ":" + entityA_vecInfo);
            log(entityB + ":" + entityB_vecInfo);

            if (entityA_vecInfo == null) {
                log("Index does not contain label : " + entityA);
            } else if (entityB_vecInfo == null) {
                log("Index does not contain label : " + entityB);
            } else {
                double[] vec_sim = modelAccessor.vectorRepresentationOf(entityA_vecInfo).values;
                double sim = vec_sim[entityB_vecInfo.id];
                log("sim " + entityA + "/" + entityB + " = " + sim);
            }
        }
    }

    private static void CMD_SHOW_VEC(String[] argv) throws SLIB_Ex_Critic, IOException {

        if (argv.length != 2) {
            log("[1] directory which contains the distributional model");
            log("[2] label or id, e.g. using label=Paris or id=30");
            System.exit(0);
        }

        String dm_dir = argv[0];
        String[] data = argv[1].split("=");
        String flag = data[0];

        if (!flag.equals("label") && !flag.equals("id")) {
            throw new SLIB_Ex_Critic("Please specify term or id, e.g. using label=Paris or id=30");
        }

        // retrieve the value 
        // We rebuild it in case it was a label containing =
        String val = "";
        for (int i = 1; i < data.length; i++) {
            if (i != 1) {
                val += "=";
            }
            val += data[i];
        }

        int word_id = -1;
        if (flag.equals("id")) {
            word_id = Integer.parseInt(val);
        }

        ModelConf model = ModelConf.load(dm_dir);
        ModelAccessor_2D modelAccessor = new ModelAccessorPersistance_2D(ModelConf.load(dm_dir));
        Iterator<IndexedVectorInfo> it = new IndexedVectorInfoIterator(model);

        IndexedVectorInfo query = null;

        while (it.hasNext()) {

            IndexedVectorInfo info = it.next();

            if (word_id != -1) {
                if (info.id == word_id) {
                    query = info;
                    break;
                }
            } else if (val.equals(info.label)) {
                query = info;
                break;
            }
        }

        if (query == null) {
            log("Cannot locate associated vector");
        } else {
            logger.info("query: " + query.toString());
            logger.info("Loading vector result");
            IndexedVector indexedVector = modelAccessor.vectorRepresentationOf(query);
//            logger.info(Arrays.toString(indexedVector.values));
            log("uncompressed size=" + indexedVector.values.length);
            double[] compressArray = CompressionUtils.compressDoubleArray(indexedVector.values);
            Map<Integer, Double> compressedVecAsMap = CompressionUtils.compressedDoubleArrayToMap(compressArray);

            log("compressed size=" + compressedVecAsMap.size());

            boolean show_compressed = getInput("Do you want to see a compressed representation? (y/n): ").toLowerCase().equals("y");
            if (show_compressed) {
                log("compressed representation:");
                SortedSet<Integer> keys = new TreeSet<Integer>(compressedVecAsMap.keySet());
                for (Integer k : keys) {
                    log("(" + k + "," + compressedVecAsMap.get(k) + ")");
                }
            }
        }

    }

    static String getInput(String message) {
        Console c = System.console();
        if (c == null) {
            System.err.println("No console.");
            System.exit(1);
        }
        return c.readLine(message);
    }

    private static void CMD_BEST_SIM(String[] argv) throws SLIB_Ex_Critic, IOException {

        if (argv.length < 3) {
            log("[1] directory which contains the distributional model");
            log("[2] number of results (integer)");
            log("[3] entity label");
            System.exit(0);
        }

        String dm_dir = argv[0];
        int k = Integer.parseInt(argv[1]);

        // retrieve multi token words
        String entity_label = "";
        for (int i = 2; i < argv.length; i++) {
            entity_label += argv[i] + " ";
        }
        entity_label = entity_label.trim();

        ModelConf mConf = ModelConf.load(dm_dir);
        ModelAccessor_2D modelAccessor = new ModelAccessorPersistance_2D(mConf);

        IndexedVectorInfo queryVectorInfo = ModelUtil.searchEntityVectorInfo(mConf, entity_label);

        if (queryVectorInfo == null) {
            log("Cannot find vecto associated to " + entity_label);
            return;
        }

        RQueue<String, Double> bestSim = SlibDist_Wrapper.computeBestSimilarities(modelAccessor, queryVectorInfo, k);

        System.out.println(bestSim.toString());

    }

    private static void CMD_GET_BEST_SIM(String[] argv) throws SLIB_Ex_Critic, IOException {

        if (argv.length < 4) {
            log("[1] directory which contains the result matrix");
            log("[2] number of results (integer)");
            log("[3] consider 0 value as min value, e.g. model compression technique may use this trick to reduce the number of values stored into the model (e.g. pmi results) (1 = yes)");
            log("[4] entity label");
            System.exit(0);
        }

        String dm_dir = argv[0];
        int k = Integer.parseInt(argv[1]);
        boolean changeNilToMin = Integer.parseInt(argv[2]) == 1;

        logger.info("results: " + dm_dir);
        logger.info("k: " + k);
        logger.info("change 0 values: " + changeNilToMin);

        // retrieve multi token words
        String entity_label = "";
        for (int i = 3; i < argv.length; i++) {
            entity_label += argv[i] + " ";
        }
        entity_label = entity_label.trim();

        ModelConf modelConf = ModelConf.load(dm_dir);
        ModelAccessor_2D modelAccessor = new ModelAccessorPersistance_2D(modelConf);

        Iterator<IndexedVectorInfo> itIndexedVectorInfo = new IndexedVectorInfoIterator(modelConf);
        Map<String, IndexedVectorInfo> index = new HashMap<String, IndexedVectorInfo>();
        Map<Integer, String> index_id = new HashMap<Integer, String>();
        logger.info("Loading index into memory");
        while (itIndexedVectorInfo.hasNext()) {
            IndexedVectorInfo i = itIndexedVectorInfo.next();
            index.put(i.label, i);
            index_id.put(i.id, i.label);
        }

        IndexedVectorInfo queryVectorInfo = index.get(entity_label);

        if (queryVectorInfo == null) {
            log("Cannot find vecto associated to " + entity_label);
            return;
        }

        logger.info(queryVectorInfo.toString());

        Timer t = new Timer();
        t.start();

        double[] vector_result = modelAccessor.vectorRepresentationOf(queryVectorInfo).values;

        RQueue<String, Double> bestSim = new RQueue<String, Double>(k);

        for (int i = 0; i < vector_result.length; i++) {

            String label = index_id.get(i);
            if (changeNilToMin && vector_result[i] == 0) {
                vector_result[i] = -Double.MAX_VALUE;
            }
            bestSim.add(label, vector_result[i]);

            i++;
            if (i % 1000 == 0) {
                String p = Utils.format2digits((double) i * 100.0 / vector_result.length);
                System.out.print(i + "/" + vector_result.length + "  " + p + "% \r");
            }
        }
        t.stop();
        t.elapsedTime();

        System.out.println(bestSim.toString());

    }

    private static void CMD_LEM(String[] argv) throws IOException {

        if (argv.length < 3) {
            log("[0] directory which contains the files to lemmatize");
            log("[1] directory where to put the lemmatized files");
            log("[2] path to stanford POS model, e.g. stanford-corenlp-3.4-models/edu/stanford/nlp/models/pos-tagger/english-bidirectional/english-bidirectional-distsim.tagger - models are available at http://nlp.stanford.edu/software/tagger.shtml");
            log("[3] skip=true to skip process if the lemmatized file already exists (optional)");
            System.exit(0);
        } else {
            boolean skip_existing = argv.length == 4 && argv[3].toLowerCase().equals("skip=true");
            SlibDist_Wrapper.lemmatize(argv[0], argv[1], argv[2], skip_existing);
        }
    }

    public static void CMD_VOC_INDEX(String[] argv) throws SLIB_Ex_Critic, IOException, Exception {

        if (argv.length < 2 || argv.length > 7) {
            log("[0] directory which contains the documents to consider");
            log("[1] type of corpus (1) one directory, all files are documents, (2) one file a document per line, e.g. list of tweet");
            log("[2] output directory");
            log("[3] word size constraint (optional default=1)");
            log("[4] (1) Forces size constraint to be strict or (2) allows words of size between 1 token and size constraint tokens (optional default=1)");
            log("[5] nbThreads (optional default=1)");
            log("[6] file per thread (optional default=" + VocStatComputer.DEFAULT_CHUNK_FILE_SIZE + "): number of files allocated to a thread");
            log("[7] cache thread (optional default=" + VocStatComputerThreads.DEFAULT_CACHE_MAP_SIZE + "): size of the index stored into memory");
            System.exit(0);
        } else {

            String corpusPath = argv[0];
            int corpusType = Integer.parseInt(argv[1]);
            String outputDir = argv[2];
            int maxWordSize = argv.length >= 4 ? Integer.parseInt(argv[3]) : 1;
            WordIteratorConstraint sizeConstraint = argv.length >= 5 ? Integer.parseInt(argv[4]) == 2 ? WordIteratorConstraint.ALLOW_SHORTER_WORDS : WordIteratorConstraint.FIXED_SIZE : WordIteratorConstraint.FIXED_SIZE;
            int nbThreads = argv.length >= 6 ? Integer.parseInt(argv[5]) : 1;
            int file_per_threads = argv.length >= 7 ? Integer.parseInt(argv[6]) : VocStatComputer.DEFAULT_CHUNK_FILE_SIZE;
            int cache_thread = argv.length == 8 ? Integer.parseInt(argv[8]) : VocStatComputerThreads.DEFAULT_CACHE_MAP_SIZE;

            logger.info("Computing vocabulary, max word size: " + maxWordSize);

            Corpus corpus;

            if (corpusType == 1) {
                corpus = new CorpusFromFileDir(corpusPath);
            }
            else{
                corpus = new CorpusFromFile(corpusPath);
            }
            
            VocStatComputer.computeVocStats(corpus, outputDir, maxWordSize, sizeConstraint, nbThreads, file_per_threads, cache_thread);
            logger.info("Vocabulary computed at: " + outputDir);
        }
    }

    public static void CMD_VOC_INDEX_DICTIONNARY(String[] argv) throws SLIB_Ex_Critic, IOException, Exception {

        if (argv.length < 3 || argv.length > 6) {
            log("[0] directory which contains the files to consider");
            log("[1] output directory");
            log("[2] vocabulary file (one word per line)");
            log("[3] nbThreads (optional default=1)");
            log("[4] file per thread (optional default=" + VocStatComputer.DEFAULT_CHUNK_FILE_SIZE + "): number of files allocated to a thread");
            log("[5] cache thread (optional default=" + VocStatComputerThreads.DEFAULT_CACHE_MAP_SIZE + "): size of the index stored into memory");
            System.exit(0);
        } else {

            String corpusDir = argv[0];
            String outputDir = argv[1];
            String vocabularyFile = argv[2];
            int nbThreads = argv.length >= 4 ? Integer.parseInt(argv[3]) : 1;
            int file_per_threads = argv.length >= 5 ? Integer.parseInt(argv[4]) : VocStatComputer.DEFAULT_CHUNK_FILE_SIZE;
            int cache_thread = argv.length == 6 ? Integer.parseInt(argv[5]) : VocStatComputerThreads.DEFAULT_CACHE_MAP_SIZE;

            logger.info("indexing vocabulary: " + vocabularyFile);
            Corpus corpus = new CorpusFromFileDir(corpusDir);
            VocStatComputer.computeVocStats(corpus, outputDir, vocabularyFile, nbThreads, file_per_threads, cache_thread);
            logger.info("Vocabulary indexed at: " + outputDir);
        }
    }

    private static void CMD_MERGE_VOC_INDEX(String[] argv) throws Exception {
        if (argv.length < 4) {
            log("[0] directory of the merged index");
            log("[1] delete merged indexes (true/false)");
            log("[2] index 1");
            log("[3] index 2");
            log("[n] index n (optional)");
            System.exit(0);
        }

        String new_index = argv[0];
        boolean delete = argv[1].equalsIgnoreCase("true");
        Set<String> indexToMerge = new HashSet<String>();
        for (int i = 2; i < argv.length; i++) {
            indexToMerge.add(argv[i]);
        }
        VocStatComputer.mergeIndexes(indexToMerge, delete, new_index);
    }

    private static void CMD_BUILD_MODEL(String[] argv) throws SLIB_Exception, IOException, Exception {

        String dist_error
                = "- term/term: build a distributional model for comparing terms\n"
                + "- term/doc: build a distributional model for comparing terms\n";
        if (argv.length == 0) {
            log(dist_error);
        } else if (argv[0].equals("term/term")) {

            argv = shift(argv);

            dist_error = "- compute_pmi : compute pmi using a cooccurence matrix";

            if (argv.length > 0 && argv[0].equals("compute_pmi")) {

                argv = shift(argv);
                if (argv.length != 3) {
                    log("[0] directory which contains the model with cooccurences");
                    log("[1] vocabulary index");
                    log("[2] output model directory");
                    System.exit(0);
                } else {
                    String cooccurence_model = argv[0];
                    String voc_dir = argv[1];
                    String model_dir = argv[2];

                    SlibDist_Wrapper.buildTerm2Term_PMI_DM(cooccurence_model, voc_dir, model_dir);
                }
            } else {
                log(dist_error);
            }

        } else if (argv[0].equals("term/doc")) {

            argv = shift(argv);

            if (argv.length != 2) {
                log("[0] vocabulary index (e.g. generated by voc_index)");
                log("[1] output directory");
                System.exit(0);
            } else {
                String dir_voc = argv[0];
                String model_dir = argv[1];

//
//                ModelConf modelConf = new ModelConf(ModelType.TWO_D_DOC_MODEL, model_dir, model_dir, files.size(), vocIndex.size(), files.size(), GConstants.STORAGE_FORMAT_VERSION);
//
                SlibDist_Wrapper.build_model_term_doc_classic(dir_voc, model_dir);
            }
        } else {
            log(dist_error);
        }
    }

    private static void CMD_REDUCE_MODEL(String[] argv) throws IOException, SLIB_Ex_Critic, Exception {

        log("Params: " + Arrays.toString(argv));

        if (argv.length < 1) {
            log("Please precise the name of the reduction technique you want to use.");
            log("k_most_used: select the dimensions by selecting the k most frequently used dimensions. This approach can for instance be used to reduce a model based on cooccurrence values.");
            log("k_random_process: build new vector representations by randomly mixing existing dimensions into a vector of size k. This approach can also be used to reduce a model based on cooccurrence values.");
            log("--------------------------------------------------------------------------------------------------------");
            log("Note that widely used matrix techniques not available in this toolkit can also be applied (e.g. SVD).");
            log("The approaches proposed below are just quick & dirty solutions compared to traditional reduction techniques.");
            System.exit(0);
        } else {
            String reduction_technique = argv[0];

            log("reduction technique: " + reduction_technique);
            argv = shift(argv);

            switch (reduction_technique) {

                case "k_most_used":

                    if (argv.length != 3) {

                        log("[1] Model to reduce.");
                        log("[2] new model directory");
                        log("[3] number of dimension to consider (k)");
                        System.exit(0);

                    } else {

                        String model_dir = argv[0];
                        String new_model_dir = argv[1];
                        Integer nbDimension = Integer.parseInt(argv[2]);

                        SlibDist_Wrapper.reduceSizeVectorRepresentations_K_MostUsedDimensions(model_dir, new_model_dir, nbDimension);

                    }
                    break;

                case "k_random_process":

                    if (argv.length < 3) {

                        log("[1] Model to reduce.");
                        log("[2] new model directory");
                        log("[3] number of dimension to consider (k) - an additionnal dimension may be used");
                        log("[4] number of iteration to consider (optional default 10)");
                        log("[5] log the factors used to reduce the vectors true/false (optional default 10)");
                        System.exit(0);

                    } else {

                        String model_dir = argv[0];
                        String new_model_dir = argv[1];
                        Integer nbDimension = Integer.parseInt(argv[2]);
                        Integer nbIterations = argv.length >= 4 ? Integer.parseInt(argv[3]) : 10;
                        boolean logFactors = argv.length >= 5 ? argv[4].toLowerCase().equals("true") : false;

                        SlibDist_Wrapper.reduceSizeVectorRepresentations_K_Random_Process(model_dir, new_model_dir, nbDimension, nbIterations, logFactors);
                    }
                    break;

                default:
                    log("unknown reduction technique");
            }
        }

    }

    private static void CMD_COMPUTE_VOC_STAT(String[] argv) throws Exception {

        logger.info("Compute Statistics");
//
        if (argv.length != 2) {
            log("[0] directory which contains the index to consider");
            log("[1] the number of results with best number of occurrences to show per n-gram size");
            System.exit(0);
        } else {
            String voc_dir = argv[0];
            int nbResutls = Integer.parseInt(argv[1]);
            VocStatComputer.computeStat(voc_dir, nbResutls);
        }
    }

    private static void CMD_COMPUTE_PMI(String[] argv) throws Exception {

        logger.info("Compute PMI");
//
        if (argv.length != 1) {
            log("[0] directory which contains the index to consider");
            System.exit(0);
        } else {
            String voc_dir = argv[0];
            VocStatComputer.computePMI(voc_dir);
        }
    }

    private static void CMD_REDUCE_INDEX_NB_OCC(String[] argv) throws SLIB_Ex_Critic, IOException, SLIB_Exception {

        logger.info("Reduce a given index only considering words that occurs more than k times");

        if (argv.length != 3) {
            log("[0] indexed vocabulary to reduce (e.g. generated by voc_index)");
            log("[1] location of the reduced index");
            log("[2] minimal number of occurrences the terms must have to keep them");
            System.exit(0);
        } else {

            String index_to_reduce = argv[0];
            String reduced_index = argv[1];
            int k = Integer.parseInt(argv[2]);

            VocStatComputer.reduceIndexUsingNbOcc(index_to_reduce, reduced_index, k);
            logger.info("Index reduced at " + reduced_index);
        }

    }

    private static void CMD_REDUCE_INDEX_USING_VOC(String[] argv) throws SLIB_Ex_Critic, IOException, SLIB_Exception {

        logger.info("Reduce a given index only considering words that occurs more than k times");

        if (argv.length != 3) {
            log("[0] indexed vocabulary to reduce (e.g. generated by voc_index)");
            log("[1] location of the reduced index");
            log("[2] vocabulary to consider (one word per line)");
            System.exit(0);
        } else {

            String index_to_reduce = argv[0];
            String reduced_index = argv[1];
            String vocabulary = argv[2];

            VocStatComputer.reduceIndexUsingVoc(index_to_reduce, reduced_index, vocabulary);
            logger.info("Index reduced at " + reduced_index);
        }
    }

    private static void CMD_COMPUTE_WORD_COOCC(String[] argv) throws SLIB_Exception, IOException, InterruptedException {

        if (argv.length < 3 || argv.length > 7) {
            log("[0] directory which contains the files to consider");
            log("[1] vocabulary file (one word per line) - an index of this vocabulary will be loaded into memory");
            log("[2] output model directory");
            log("[3] size left/right windows (optional, default 30)");
            log("[4] nb Threads (optional, default 2)");
            log("[5] nb files per chunk (optional, default 10000)");
            log("[6] max matrice size per thread (optional, default 1000000)");
            System.exit(0);
        } else {
            String corpus_dir = argv[0];
            String voc_file = argv[1];
            String model_dir = argv[2];
            int window_size_token = argv.length >= 4 ? Integer.parseInt(argv[3]) : 30;
            int nbThreads = argv.length >= 5 ? Integer.parseInt(argv[4]) : 2;
            int nbFilesPerChunk = argv.length >= 6 ? Integer.parseInt(argv[5]) : 10000;
            int max_matrix_size = argv.length == 7 ? Integer.parseInt(argv[6]) : 1000000;

            SlibDist_Wrapper.buildTerm2TermDM(corpus_dir, voc_file, model_dir, window_size_token, nbThreads, nbFilesPerChunk, max_matrix_size);
        }
    }

    /**
     * This method generate a word cooccurence matrix considering a specific
     * vocabulary. It has been developed in order to extract a specific
     * submatrix of a given matrix.
     *
     * @param argv
     */
    private static void CMD_REDUCE_WORD_COOCC_MATRIX(String[] argv) {

        if (argv.length != 3) {
            log("[0] vocabulary file (one word per line) - an index of this vocabulary will be loaded into memory");
            log("[1] original cooccurence matrix to reduce");
            log("[2] output matrix directory directory");
            System.exit(0);
        } else {
            String voc_file = argv[0];
            String model_dir = argv[1];
            String output_dir = argv[2];
            //PJE  int window_size_token = argv.length >= 4 ? Integer.parseInt(argv[3]) : 30;
            //PJE int nbThreads = argv.length >= 5 ? Integer.parseInt(argv[4]) : 2;
            //PJE int nbFilesPerChunk = argv.length >= 6 ? Integer.parseInt(argv[5]) : 10000;
            //PJE  int max_matrix_size = argv.length == 7 ? Integer.parseInt(argv[6]) : 1000000;

            SlibDist_Wrapper.reduceWordOccMatrix(voc_file, model_dir, output_dir);
        }
    }
}
