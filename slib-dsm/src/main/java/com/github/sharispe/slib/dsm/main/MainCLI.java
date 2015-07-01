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

import com.github.sharispe.slib.dsm.core.engine.VocStatComputer;
import com.github.sharispe.slib.dsm.core.engine.VocStatComputerThreads;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIteratorConstraint;
import com.github.sharispe.slib.dsm.core.kb.EntityVectorRepresentationComputer;
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
import java.io.Console;

import java.io.IOException;
import java.util.Arrays;
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

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
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
     * @param arr
     * @return
     */
    public static String[] shift(String[] arr) {
        return Arrays.copyOfRange(arr, 1, arr.length);
    }

    public static void showdoc() throws IOException {

        log("Please use one of the following tools: ");
        log("- lem: lemmatize the files contained in a directory");
        log("- voc_index: analyse a corpus by extracting the vocabulary considering specified constraint and create an index - basic statistics are also computed");
        log("- voc_index_dictionary: analyse a corpus to create an index of the given vocabulary- basic statistics are also computed");
        log("- merge_voc_index: merge voc indexes computed using voc_index");
        log("- compute_stat_voc: compute basic statistics on the given vocabulary");
        log("- compute_pmi: compute 2-gram PMI");
        log("- reduce_index_nb_occ: reduce a vocabulary based on the number of occurrences of words");
        log("- reduce_index_using_voc: reduce a vocabulary index considering a given vocabulary");
        log("- build_model: build a Distributional Model (DM) of the terms considering a vocabulary and a directory of files");
        log("- normalize: normalize the vector representations contained into a model (locally in each vector using cross-multiplication)");
        log("- reduce_model: reduce a model to remove useless dimension");
        log("- show_vec output: the compressed representation of a vector");
        log("- check_null_vec: check the number of vectors which are null");
        log("- sim: compute the similarity between two terms or two documents considering a DM");
        log("- simdoctest: compute the similarity between two documents using an approach based on a term/term DM and a doc/term DM");
        log("- bestsim: compute the terms/docs which are the most similar to the given term/doc");
        log("- bestsimdoctest: compute the terms/docs which are the most similar to the given term/doc using the approach tested in simdoctest");
        log("- kb: for egc");
        log("- dist_mat: compute the distance matrix between all the entities of the model");
        log("---------------------------------------------------------------------");
        log("An example of use can be:\n"
                + "\tlem to lemmatize a set of documents\n"
                + "\tvoc_index to extract the vocabulary\n"
                + "\tbuild_model to build a distributional model\n"
                + "\treduce_model to reduce a distributional model, e.g. for considering only informative dimensions\n"
                + "\tshow_vec output the compressed representation of a vector\n"
                + "\tsim to compute the similarity between terms or entities\n"
                + "\tbestsim to distinguish the k entities which are the more similar to the given one\n");
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
                    case "compute_pmi":
                        CMD_COMPUTE_PMI(argv);
                        break;
                    case "reduce_index_nb_occ":
                        CMD_REDUCE_INDEX_NB_OCC(argv);
                        break;
                    case "reduce_index_using_voc":
                        CMD_REDUCE_INDEX_USING_VOC(argv);
                        break;
                    case "build_model":
                        CMD_BUILD_MODEL(argv);
                        break;
//                    case "normalize":
//                        CMD_NORMALIZE(argv);
//                        break;
                    case "reduce_model":
                        CMD_REDUCE_MODEL(argv);
                        break;
//                    case "check_null_vec":
//                        CMD_CHECK_NULL_VEC(argv);
//                        break;
                    case "sim":
                        CMD_SIM(argv);
                        break;
//                    case "simdoctest":
//                        CMD_SIM_DOC_ADVANCED(argv);
//                        break;
                    case "bestsim":
                        CMD_BEST_SIM(argv);
                        break;
//                    case "bestsimdoctest":
//                        CMD_BEST_SIM_DOC_ADVANCED(argv);
//                        break;
                    case "show_vec":
                        CMD_SHOW_VEC(argv);
                        break;
                    case "kb":
                        EntityVectorRepresentationComputer.main(argv);
                        break;
//                    case "dist_mat":
//                        CMD_DIST_MAT(argv);
//                        break;
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

//    private static void CMD_SIM_DOC_ADVANCED(String[] argv) throws SLIB_Ex_Critic, IOException {
//
//        if (argv.length < 2 || argv.length > 4) {
//            log("[1] directory which contains the term/term distributional model");
//            log("[2] directory which contains the doc/term distributional model");
//            log("[3] entity label A or -i if you want to use the interactive mode");
//            log("[4] entity label B (if -i is not used)");
//            System.exit(0);
//        }
//        String dm_term_dir = argv[0];
//        String dm_doc_dir = argv[1];
//        ModelConf modelConf_TERM = ModelConf.load(dm_term_dir);
//        ModelConf modelConf_DOC = ModelConf.load(dm_doc_dir);
//
//        Map<String, Integer> docIndex = XPUtils.loadMAP(modelConf_DOC.getEntityIndex());
//        ModelAccessor modelAccessor_doc = new ModelAccessorMemory_2D(modelConf_DOC);
//        ModelAccessor modelAccessor_term = new ModelAccessorMemory_2D(modelConf_TERM);
//
//        Map<String, Integer> termIndex = XPUtils.loadMAP(modelConf_TERM.getEntityIndex());
//
//        if (argv[2].toLowerCase().equals("-i")) { // interactive mode
//
//            while (true) {
//                log("---------------------------------------");
//                String docA_label = getInput("Please type a label for doc A -- type quit() to stop: ");
//                if (docA_label.equals("quit()")) {
//                    break;
//                }
//                String docB_label = getInput("Please type a label for entity B -- type quit() to stop: ");
//                if (!docIndex.containsKey(docA_label)) {
//
//                    log("Index does not contain label : " + docA_label);
//
//                } else if (!docIndex.containsKey(docB_label)) {
//
//                    log("Index does not contain label : " + docB_label);
//
//                } else {
//                    double[] docA = modelAccessor_doc.vectorRepresentationOf(docIndex.get(docA_label));
//                    double[] docB = modelAccessor_doc.vectorRepresentationOf(docIndex.get(docB_label));
//
//                    double sim = SlibDist_Wrapper.computeDocSimNewApproach(docA, docB, termIndex, modelAccessor_term);
//                    log("sim " + docA_label + "/" + docB_label + " = " + sim);
//                }
//            }
//        } else {
//
//            String entityA = argv[2];
//            String entityB = argv[3];
//
//            log("Comparing " + entityA + "/" + entityB);
//            if (!docIndex.containsKey(entityA)) {
//                throw new SLIB_Ex_Critic("Index " + modelConf_DOC.getEntityIndex() + " does not contain entity label : " + entityA);
//            }
//            if (!docIndex.containsKey(entityB)) {
//                throw new SLIB_Ex_Critic("Index " + modelConf_DOC.getEntityIndex() + " does not contain entity label : " + entityB);
//            }
//            double sim = SlibDist_Wrapper.computeEntitySimilarity(docIndex, modelAccessor_doc, entityA, entityB);
//            log("sim " + entityA + "/" + entityB + " = " + sim);
//        }
//    }
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
            IndexedVector indexedVector = modelAccessor.vectorRepresentationOf(query);
            log("uncompressed size=" + indexedVector.values.length);
            double[] compressArray = CompressionUtils.compressDoubleArray(indexedVector.values);
            Map<Integer, Double> compressedVecAsMap = CompressionUtils.compressedDoubleArrayToMap(compressArray);

            log("compressed size=" + compressedVecAsMap.size());

            boolean show_compressed = getInput("Do you want to see a compressed representation? (y/n): ").toLowerCase().equals("y");
            if (show_compressed) {
                log("compressed representation:");
                SortedSet<Integer> keys = new TreeSet(compressedVecAsMap.keySet());
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

//    private static void CMD_BEST_SIM_DOC_ADVANCED(String[] argv) throws SLIB_Ex_Critic, IOException {
//
//        if (!(argv.length == 4 || argv.length == 5)) {
//            log("[1] directory which contains the term/term distributional model");
//            log("[2] directory which contains the doc/term distributional model");
//            log("[3] doc label or type -i for interactive mode");
//            log("[4] number of results (integer)");
//            log("[5] output file (optional, not considered in interactive mode) ");
//            System.exit(0);
//        }
//
//        String dm_term_dir = argv[0];
//        String dm_doc_dir = argv[1];
//        String doc_label = argv[2];
//
//        int k = Integer.parseInt(argv[3]);
//
//        String outfile = argv.length == 5 ? argv[4] : null;
//
//        ModelConf model_doc_conf = ModelConf.load(dm_doc_dir);
//        ModelConf model_term_conf = ModelConf.load(dm_term_dir);
//        Map<String, Integer> docIndex = XPUtils.loadMAP(model_doc_conf.getEntityIndex());
//        Map<String, Integer> termIndex = XPUtils.loadMAP(model_term_conf.getEntityIndex());
//
//        ModelAccessor_2D modelDocAccessor = new ModelAccessorMemory_2D(model_doc_conf);
//        ModelAccessor_2D modelTermAccessor = new ModelAccessorMemory_2D(model_term_conf);
//
//        if (doc_label.equals("-i")) { // interactive mode
//
//            while (true) {
//                log("---------------------------------------");
//                doc_label = getInput("Please type a doc label -- type quit() to stop: ");
//                if (!docIndex.containsKey(doc_label)) {
//                    if (doc_label.equals("quit()")) {
//                        break;
//                    }
//                    log("Index does not contain label : " + doc_label);
//
//                } else {
//                    SlibDist_Wrapper.computeBestDocSimilarity_Advanced(doc_label, docIndex, modelDocAccessor, termIndex, modelTermAccessor, k, true);
//                }
//            }
//
//        } else {
//
//            log("Looking for best sim " + doc_label + " (k=" + k + ")");
//            if (!docIndex.containsKey(doc_label)) {
//                log("Index does not contain label : " + doc_label);
//            }
//            Map<String, Double> res = SlibDist_Wrapper.computeBestDocSimilarity_Advanced(doc_label, docIndex, modelDocAccessor, termIndex, modelTermAccessor, k, outfile == null);
//            if (outfile != null) {
//                MapUtils.toFile(res, outfile);
//                logger.info("result: " + outfile);
//            }
//        }
//
//    }
    private static void CMD_LEM(String[] argv) throws IOException {

        if (argv.length < 2) {
            log("[0] directory which contains the files to lemmatize");
            log("[1] directory where to put the lemmatized files");
            log("[2] skip=true to skip process if the lemmatized file already exists (optional)");
            System.exit(0);
        } else {
            boolean skip_existing = argv.length == 3 && argv[2].toLowerCase().equals("skip=true");
            SlibDist_Wrapper.lemmatize(argv[0], argv[1], skip_existing);
        }
    }

    public static void CMD_VOC_INDEX(String[] argv) throws SLIB_Ex_Critic, IOException, Exception {

        if (argv.length < 2 || argv.length > 7) {
            log("[0] directory which contains the files to consider");
            log("[1] output directory");
            log("[2] word size constraint (optional default=1)");
            log("[3] (1) Forces size constraint to be strict or (2) allows words of size between 1 token and size constraint tokens (optional default=1)");
            log("[4] nbThreads (optional default=1)");
            log("[5] file per thread (optional default=" + VocStatComputer.DEFAULT_CHUNK_FILE_SIZE + "): number of files allocated to a thread");
            log("[6] cache thread (optional default=" + VocStatComputerThreads.DEFAULT_CACHE_MAP_SIZE + "): size of the index stored into memory");
            System.exit(0);
        } else {

            String corpusDir = argv[0];
            String outputDir = argv[1];
            int maxWordSize = argv.length >= 3 ? Integer.parseInt(argv[2]) : 1;
            WordIteratorConstraint sizeConstraint = argv.length >= 4 ? Integer.parseInt(argv[3]) == 2 ? WordIteratorConstraint.ALLOW_SHORTER_WORDS : WordIteratorConstraint.FIXED_SIZE : WordIteratorConstraint.FIXED_SIZE;
            int nbThreads = argv.length >= 5 ? Integer.parseInt(argv[4]) : 1;
            int file_per_threads = argv.length >= 6 ? Integer.parseInt(argv[5]) : VocStatComputer.DEFAULT_CHUNK_FILE_SIZE;
            int cache_thread = argv.length == 7 ? Integer.parseInt(argv[6]) : VocStatComputerThreads.DEFAULT_CACHE_MAP_SIZE;

            logger.info("Computing vocabulary, max word size: " + maxWordSize);
            VocStatComputer.computeVocStats(corpusDir, outputDir, maxWordSize, sizeConstraint, nbThreads, file_per_threads, cache_thread);
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
            VocStatComputer.computeVocStats(corpusDir, outputDir, vocabularyFile, nbThreads, file_per_threads, cache_thread);
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
        Set<String> indexToMerge = new HashSet();
        for (int i = 2; i < argv.length; i++) {
            indexToMerge.add(argv[i]);
        }
        VocStatComputer.mergeIndexes(indexToMerge, delete, new_index);
    }

    private static void CMD_BUILD_MODEL(String[] argv) throws SLIB_Exception, IOException {

        String dist_error
                = "- term/term: build a distributional model for comparing terms\n"
                + "- term/doc: build a distributional model for comparing terms\n"
                + "- doc/term_2: build a distributional model for comparing document (e.g. Term vectors * TF-IDF vectors)";

        if (argv.length == 0) {
            log(dist_error);
        } else if (argv[0].equals("term/term")) {


            argv = shift(argv);

            if (argv.length != 4) {
                log("[0] directory which contains the files to consider");
                log("[1] vocabulary index (e.g. generated by voc_index) - this vacabulary will be loaded into memory");
                log("[2] output model directory");
                log("[3] nb Threads");
                System.exit(0);
            } else {
                String corpus_dir = argv[0];
                String voc_files = argv[1];
                String model_dir = argv[2];
                int nbThreads = Integer.parseInt(argv[3]);

                SlibDist_Wrapper.buildTerm2TermDM(corpus_dir, voc_files, model_dir, nbThreads);
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
        } else if (argv[0].equals("doc/term_2")) {

            throw new UnsupportedOperationException();

//            argv = shift(argv);
//            if (argv.length != 4) {
//                log("[1] term/term distributional model");
//                log("[2] doc/term distributional model");
//                log("[3] k threashold, i.e. number of values to consider, type null to avoid applying the threashold");
//                log("[4] output directory");
//                System.exit(0);
//            } else {
//                String term_dm = argv[0];
//                String doc_dm = argv[1];
//                Integer k_threashold = argv[2].toLowerCase().equals("null") ? null : Integer.parseInt(argv[2]);
//                String new_dm = argv[3];
//                SlibDist_Wrapper.build_DM_doc_refined(term_dm, doc_dm, k_threashold, new_dm);
//            }
        } else {
            log(dist_error);
        }
    }

    private static void CMD_REDUCE_MODEL(String[] argv) throws IOException, SLIB_Ex_Critic, Exception {

        log("Params: " + Arrays.toString(argv));

        if (argv.length != 3) {

            log("[1] distributional model to reduce. It must have term reference as dimensions");
            log("[2] new model directory");
            log("[3] number of dimension to consider (k)");
            System.exit(0);

        } else {

            String model_dir = argv[0];
            String new_model_dir = argv[1];
            Integer nbDimension = Integer.parseInt(argv[2]);

            SlibDist_Wrapper.reduceSizeVectorRepresentations(model_dir, new_model_dir, nbDimension);

        }
    }

//    private static void CMD_CHECK_NULL_VEC(String[] argv) throws SLIB_Ex_Critic {
//
//        String dist_error = "[1] term distributional model to reduce (generated by dist term/term)\n";
//
//        if (argv.length != 1) {
//            log(dist_error);
//        } else {
//
//            String model_dir = argv[0];
//
//            ModelConf mconf = ModelConf.load(model_dir);
//            Map<Integer, String> vocIndex = MapUtils.revert(XPUtils.loadMAP(mconf.getEntityIndex()));
//
//            logger.info("Load in-memory model accessor for " + mconf.name);
//            ModelAccessorMemory_2D modelAccessor = new ModelAccessorMemory_2D(mconf);
//
//            int nb_vectors = modelAccessor.getElementIds().size();
//            int emptyVectorsCount = 0;
//            for (Integer i : modelAccessor.getElementIds()) {
//
//                if (modelAccessor.getCompressedRepresentation(i).isEmpty()) {
//                    logger.info("- null vector: id: " + i + "\tlabel: " + vocIndex.get(i));
//                    emptyVectorsCount++;
//                }
//            }
//            logger.info("Empty vectors: " + emptyVectorsCount + "/" + nb_vectors);
//        }
//    }
    private static void CMD_COMPUTE_VOC_STAT(String[] argv) throws Exception {

        logger.info("Compute Statistics");
//
        if (argv.length != 2) {
            log("[0] directory which contains the index to consider");
            log("[1] number of results");
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

//    private static void CMD_NORMALIZE(String[] argv) throws SLIB_Ex_Critic, IOException {
//
//        if (argv.length != 2) {
//
//            log("[1] distributional model to reduce.");
//            log("[2] new model directory");
//            System.exit(0);
//
//        } else {
//
//            String model_dir = argv[0];
//            String new_model_dir = argv[1];
//
//            ModelConf mconf = ModelConf.load(model_dir);
//
//            logger.info("Initialize nnormalized model");
//            ModelConf mconfNorm = new ModelConf(ModelType.TWO_D_DOC_MODEL, new_model_dir, new_model_dir, mconf.entity_size, mconf.vec_size, mconf.getNBFiles(), new_model_dir);
//            ConfUtils.initModel(mconfNorm);
//
//            FileUtils.copyFile(new File(mconf.getDimensionIndex()), new File(mconfNorm.getDimensionIndex()));
//            FileUtils.copyFile(new File(mconf.getEntityIndex()), new File(mconfNorm.getEntityIndex()));
//
//            logger.info("Normalizing");
//
//            ModelAccessorMemory_2D modelAccessor = new ModelAccessorMemory_2D(mconf);
//
//            int size = modelAccessor.getElementIds().size();
//            int c = 0;
//
//            File f = new File(mconfNorm.getModelBinary());
//            byte[] sep = {0};
//
//            try (PrintWriter indeWriter = new PrintWriter(mconfNorm.getModelIndex(), "UTF-8")) {
//
//                indeWriter.println("ID_ENT\tSTART_POS\tLENGTH_DOUBLE_NON_NULL");
//                long current = 0;
//
//                try (FileOutputStream fo = new FileOutputStream(f)) {
//
//                    for (Integer id : modelAccessor.getElementIds()) {
//
//                        c++;
//                        logger.info("Normalizing " + c + "/" + size);
//
//                        Map<Integer, Double> m_compressed = modelAccessor.getCompressedRepresentation(id);
//                        // search max
//                        Collection<Double> set = m_compressed.values();
//                        Double max = null;
//
//                        for (Double d : set) {
//                            if (max == null || d > max) {
//                                max = d;
//                            }
//                        }
//
//                        double val;
//                        Map<Integer, Double> m_norm_compressed = new HashMap<>(m_compressed.size());
//                        for (Integer d : m_compressed.keySet()) {
//                            val = m_compressed.get(d) / max;
//                            m_norm_compressed.put(d, val);
//                        }
//
//                        // here we retrieve the number of pair we will have in the compressed vector
//                        // i.e. [(1,0.4),(30,0.6),(5,0.7)...] refer to the doc
//                        byte[] compressed_vector_byte;
//                        compressed_vector_byte = CompressionUtils.toByteArray(m_norm_compressed);
//                        fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
//                        fo.write(sep);
//
//                        indeWriter.println(id + "\t" + current + "\t" + m_norm_compressed.size());
//
//                        current += m_norm_compressed.size() * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
//                        current += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator
//                    }
//                }
//                logger.info("Normalized vesion of model " + mconf.name + " has been built at " + mconfNorm.path);
//                indeWriter.close();
//            }
//        }
//    }
//    private static void CMD_DIST_MAT(String[] argv) throws SLIB_Ex_Critic, FileNotFoundException {
//
//        if (argv.length != 2) {
//
//            log("[1] directory of the model to consider.");
//            log("[2] File in which the distance matrix has to be saved");
//            System.exit(0);
//        }
//
//        String dm_dir = argv[0];
//        String distmat_file = argv[1];
//
//        logger.info("Loading model");
//        ModelConf mConf = ModelConf.load(dm_dir);
//
//        logger.info("Computing distance matrix");
//        Map<Integer, String> entityIndex = XPUtils.loadIndexRevert(mConf.getEntityIndex());
//
//        ModelAccessor_2D modelAccessor = new ModelAccessorFullMemory_2D(mConf);
//        Map<Integer, Map<Integer, Double>> distmat = new HashMap();
//        double[] v_a, v_b;
//        List<Integer> orderedIds = new ArrayList(modelAccessor.getElementIds());
//        Collections.sort(orderedIds);
//
//        for (int i = 0; i < orderedIds.size(); i++) {
//
//            logger.info(i + "/" + orderedIds.size());
//
//            int a = orderedIds.get(i);
//
//            if (!distmat.containsKey(a)) {
//                distmat.put(a, new HashMap<Integer, Double>());
//            }
//
//            v_a = modelAccessor.vectorRepresentationOf(a);
//
//            for (int j = i; j < orderedIds.size(); j++) {
//
//                int b = orderedIds.get(j);
//
//                v_b = modelAccessor.vectorRepresentationOf(b);
//                double dist = 1 - CosineSimilarity.sim(v_a, v_b);
//
//                if (!distmat.containsKey(b)) {
//                    distmat.put(b, new HashMap<Integer, Double>());
//                }
//                distmat.get(a).put(b, dist);
//                distmat.get(b).put(a, dist);
//            }
//        }
//
//        logger.info("Ploting distance matrix");
//
//        PrintWriter w = new PrintWriter(distmat_file);
//        w.println(orderedIds.size());
//
//        for (Integer i : orderedIds) {
//            StringBuilder l = new StringBuilder();
//            l.append(i).append("_").append(entityIndex.get(i));
//            for (Integer j : orderedIds) {
//                l.append("\t").append(distmat.get(i).get(j));
//            }
//            w.println(l.toString());
//        }
//        w.close();
//    }
}
