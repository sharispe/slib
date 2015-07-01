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

import com.github.sharispe.slib.dsm.utils.BinarytUtils;
import com.github.sharispe.slib.dsm.utils.FileUtility;
import com.github.sharispe.slib.dsm.utils.Lemmatizer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sml.sm.core.measures.vector.CosineSimilarity;
import com.github.sharispe.slib.dsm.core.engine.DMEngine;
import com.github.sharispe.slib.dsm.core.engine.Voc;
import com.github.sharispe.slib.dsm.core.engine.VocInfo;
import com.github.sharispe.slib.dsm.core.engine.VocStatComputer;
import com.github.sharispe.slib.dsm.core.model.access.ModelAccessor;
import com.github.sharispe.slib.dsm.core.model.access.twodmodels.ModelAccessorPersistance_2D;
import com.github.sharispe.slib.dsm.core.model.utils.compression.CompressionUtils;
import com.github.sharispe.slib.dsm.core.model.utils.IndexedVectorInfo;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConfUtils;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.GConstants;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelType;
import com.github.sharispe.slib.dsm.core.model.access.twodmodels.ModelAccessor_2D;
import com.github.sharispe.slib.dsm.core.model.utils.IndexedVector;
import com.github.sharispe.slib.dsm.utils.RQueue;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Iterator;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SlibDist_Wrapper {

    static Logger logger = LoggerFactory.getLogger(SlibDist_Wrapper.class);

    /**
     * Create a lemmatized version of the files contained in a directory.
     *
     * @param indir the directory in which are the files to lemmatize
     * @param outdir the directory in which to save the result files
     * @param skipExisting
     * @throws IOException
     */
    public static void lemmatize(String indir, String outdir, boolean skipExisting) throws IOException {

        List<File> files = FileUtility.listFilesFromFolder(indir, null);

        logger.info("Processing " + files.size() + " files (skip existing=" + skipExisting + ")");
        int c = 0;
        for (File f : files) {
            c++;
            if (c % 100 == 0) {
                logger.info(c + "/" + files.size());
            }
            String fname = outdir + "/" + f.getName() + ".lem";

            if (!skipExisting || !new File(fname).exists()) {
                Lemmatizer.lemmatize(f.getAbsolutePath(), fname);
            }
        }
        logger.info("done");
    }

    /**
     *
     * @param path_term_model
     * @param path_doc_model
     * @param k define a threashold to reduce the dimension to consider when
     * summing the vectors
     * @param new_doc_model_dir
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SLIB_Ex_Critic
     */
//    public static void build_DM_doc_refined(String path_term_model, String path_doc_model, Integer k, String new_doc_model_dir) throws IOException, FileNotFoundException, SLIB_Ex_Critic {
//
//        logger.info("Loading data");
//
//        ModelConf term_dm_conf = ModelConf.load(path_term_model);
//        ModelConf doc_dm_conf = ModelConf.load(path_doc_model);
//
//        ModelAccessorMemory_2D termModelAccessor = new ModelAccessorMemory_2D(term_dm_conf);
//        ModelAccessorMemory_2D docModelAccessor = new ModelAccessorMemory_2D(doc_dm_conf);
//
//        Map<String,Integer> vocIndex = XPUtils.loadMAP(term_dm_conf.getEntityIndex());
//
//        Map<String, Integer> termIndex = vocIndex.getIndex();
//        Map<String, Integer> docIndex = XPUtils.loadMAP(doc_dm_conf.getEntityIndex());
//        Map<Integer, String> termIndexDocModel = MapUtils.revert(XPUtils.loadMAP(doc_dm_conf.getDimensionIndex()));
//
//        int vec_size_new_model = term_dm_conf.vec_size;
//
//        logger.info("Updating vectors...");
//
//        ModelConf new_doc_dm_conf = new ModelConf(ModelType.TWO_D_DOC_MODEL, new_doc_model_dir, new_doc_model_dir, docIndex.size(), vec_size_new_model, doc_dm_conf.nb_files, GConstants.STORAGE_FORMAT_VERSION);
//
//        long start_binary_vec = 0;
//        byte[] sep_binary = {0};
//        File f_binary = new File(new_doc_dm_conf.getModelBinary());
//
//        ConfUtils.initModel(new_doc_dm_conf);
//
//        // Flush row Index and binary representation of vectors
//        try (PrintWriter index_writer = new PrintWriter(new_doc_dm_conf.getModelIndex(), "UTF-8")) {
//            try (FileOutputStream fo = new FileOutputStream(f_binary)) {
//
//                index_writer.println("ID_ENT\tSTART_POS\tLENGTH_DOUBLE_NON_NULL");
//
//                for (Map.Entry<String, Integer> e : MapUtils.sortByValue(docIndex).entrySet()) {
//
//                    String f = e.getKey();
//                    int id_file = e.getValue();
//
//                    logger.info(f + "\t" + id_file + "/" + docIndex.size() + "\r");
//
//                    double[] vec_document = docModelAccessor.vectorRepresentationOf(id_file);
//
//                    double[] sum = new double[vec_size_new_model];
//
//                    for (int i = 0; i < vec_document.length; i++) {
//
//                        if (vec_document[i] != 0) {
//
//                            // we retrieve the label of the dimension i
//                            String dim_label = termIndexDocModel.get(i);
//
//                            if (!termIndex.containsKey(dim_label)) {
//                                throw new SLIB_Ex_Critic("Error term/term model does not contains a representation for term labelled: " + dim_label);
//                            }
//
//                            // we retrieve the dimension id of this label in the term model
//                            int id_term = termIndex.get(dim_label);
//                            double[] vec_term = termModelAccessor.vectorRepresentationOf(id_term);
//
//                            // we apply a threashold if any as been specified
//                            if (k != null) {
//                                vec_term = XPUtils.applyKthreshold(vec_term, k);
//                            }
//                            sum = XPUtils.sumVectors(sum, XPUtils.multiplyVector(vec_document[i], vec_term));
//                        }
//                    }
//                    // Write the vector into the binary file
//
//                    double[] compressedVector = CompressionUtils.compressDoubleArray(sum);
//                    int nonNullValues = compressedVector.length / 2;
//                    // write index info 
//                    index_writer.println(id_file + "\t" + start_binary_vec + "\t" + nonNullValues);
//
//                    // write the binary representation
//                    byte[] compressed_vector_byte = BinarytUtils.toByteArray(compressedVector);
//                    fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
//                    fo.write(sep_binary);
//
//                    start_binary_vec += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
//                    start_binary_vec += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator
//
//                    id_file++;
//                }
//
//                XPUtils.flushMAP(docIndex, new_doc_dm_conf.getEntityIndex());
//                // dimensions are not meaningfull
//                logger.info("model save at: " + new_doc_dm_conf.path);
//
//            }
//        }
//
//    }
    /**
     * Compute a TF-IDF vector representation of documents considering a given
     * vocabulary. The result is a set of files representing the vector
     * representation of each file composing the given directory.
     *
     * @param files
     * @param vocIndex
     * @param model
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SLIB_Ex_Critic
     */
//    public static void build_model_doc_classic(List<File> files, Voc vocIndex, ModelConf model) throws FileNotFoundException, IOException, SLIB_Ex_Critic {
//
//        ConfUtils.initModel(model);
//
//        // Computes several statistics 
//        // words occurrences in the collection of documents
//        logger.info("Loading the vocabulary stats (voc size=" + vocIndex.size() + ")");
//
//        // words occurrences for each document and in the collection
//        Map<Integer, Integer> nbDocWithTheTermInCollection = vocIndex.getNbfilesWithWord();
//
//        // for each word the number of document which contains it
//        int numberOfDocsInCollection = files.size();
//
//        double[] idf = new double[vocIndex.size()];
//        for (Integer i : vocIndex.getIndex().values()) {
//            idf[i] = Tf_Idf.idfCalculator(numberOfDocsInCollection, nbDocWithTheTermInCollection.get(i));
//        }
//        
////        Set<Integer> vocIndexIDs = new HashSet(vocIndex.getIndex().values());
//
//        logger.info("Computing vector representation for each document (n=" + files.size() + ")");
//
//        long start_binary_vec = 0;
//        byte[] sep_binary = {0};
//        File f_binary = new File(model.getModelBinary());
//
//        Map<String, Integer> entityIndex = new HashMap();
//
//        int c = 0;
//        // Flush row Index and binary representation of vectors
//        try (PrintWriter index_writer = new PrintWriter(model.getModelIndex(), "UTF-8")) {
//            try (FileOutputStream fo = new FileOutputStream(f_binary)) {
//
//                int id_file = 0;
//                index_writer.println("ID_ENT\tSTART_POS\tLENGTH_DOUBLE_NON_NULL");
//                // we now create a vector representation of the files
//                Map<Integer, Integer> vocDocUsage;
//                Map<Integer, Double> compressedVector;
//
//                int numberOfTermsInDocument, numberOfTermOccurrenceInDocument, nonNullValues;
//                byte[] compressed_vector_byte;
//                double tfidf;
//                for (File f : files) {
//
//                    vocDocUsage = VocUsageUtils.getVocUsage(f, vocIndex);
//                    compressedVector = new HashMap(vocDocUsage.size());
//
//                    c++;
//
//                    // compute number of terms in document 
//                    numberOfTermsInDocument = 0;
//                    for (Integer v : vocDocUsage.values()) {
//                        numberOfTermsInDocument += v;
//                    }
//
//                    int idWord;
//                    for (Map.Entry<Integer, Integer> e : vocDocUsage.entrySet()) {
//
//                        idWord = e.getKey();
//                        numberOfTermOccurrenceInDocument = e.getValue(); // This will always be > O
//
////                        if (!vocIndexIDs.contains(idWord)) {
////                            throw new SLIB_Ex_Critic("Word '" + idWord + "' is not in the index (which contains " + vocIndex.size() + " elements)...");
////                        }
//                        tfidf = idf[idWord] * Tf_Idf.tfCalculator(numberOfTermOccurrenceInDocument, numberOfTermsInDocument);
//
//                        if (tfidf == 0) { // we don't want 0 values
//                            continue;
//                        }
//                        compressedVector.put(idWord, tfidf);
//                    }
//
//                    nonNullValues = compressedVector.size();
//
//                    if (c % 1000 == 0) {
//                        logger.info("building vector " + c + "/" + files.size() + "  " + f.getPath() + " size non null=" + nonNullValues + "/" + vocIndex.size());
//                    }
//
//                    // write the binary representation
//                    compressed_vector_byte = CompressionUtils.toByteArray(compressedVector);
//                    fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
//                    fo.write(sep_binary);
//
//                    // write index info 
//                    index_writer.println(id_file + "\t" + start_binary_vec + "\t" + nonNullValues);
//
//                    start_binary_vec += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
//                    start_binary_vec += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator
//
//                    entityIndex.put(f.getPath(), id_file);
//                    id_file++;
//                }
//            }
//        }
//
//        XPUtils.flushMAP(entityIndex, model.getEntityIndex());
//        XPUtils.flushMAP(vocIndex.getIndex(), model.getDimensionIndex());
//        logger.info("model save at: " + model.path);
//    }
    /**
     * Compute a TF-IDF vector representation of documents considering a given
     * vocabulary. The result is a set of files representing the vector
     * representation of each file composing the given directory.
     *
     * @param voc_index
     * @param model_dir
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SLIB_Ex_Critic
     */
    public static void build_model_term_doc_classic(String voc_index, String model_dir) throws FileNotFoundException, IOException, SLIB_Ex_Critic {

        VocInfo vocInfo = new VocInfo(voc_index + "/" + VocStatComputer.GENERAL_INFO);
        ModelConf model = new ModelConf(ModelType.TWO_D_TERM_DOC, "T x D model", model_dir, vocInfo.nbWords, vocInfo.nbFiles, vocInfo.nbFiles, "0.1");

        ModelConfUtils.initModel(model);

        // Compute the vector representation of each word
        logger.info("Computing vector representation for each document (n=" + vocInfo.nbWords + ")");

        long start_binary_vec = 0;
        byte[] sep_binary = {0};
        File f_binary = new File(model.getModelBinary());

        byte[] compressed_vector_byte;

        Map<String, Integer> chunk_index = Utils.loadMap(voc_index + "/" + VocStatComputer.CHUNK_INDEX);
        int chunk_done_count = 0;

        // We generate 
        // (i)  the index in which information regarding the location of term vector representation will be specified 
        // (ii) the binary index in which the binary representation of vectors will be saved
        try (PrintWriter index_writer = new PrintWriter(model.getModelIndex(), "UTF-8")) {
            try (FileOutputStream fo = new FileOutputStream(f_binary)) {

                int id_word = 0;
                index_writer.println("ID_WORD\tSTART_POS\tLENGTH_DOUBLE_NON_NULL\tWORD");

                // We iterate over the chunks to process each associated words
                for (Integer id_chunk : chunk_index.values()) {

                    chunk_done_count++;
                    String p = Utils.format2digits((double) chunk_done_count * 100.0 / chunk_index.size());
                    System.out.print("processing chunk " + chunk_done_count + "/" + chunk_index.size() + "\t"+p+"%    \r");

                    // We iterate over the words of the chunk
                    // and create their vector representation
                    try (BufferedReader br = new BufferedReader(new FileReader(voc_index + "/" + id_chunk))) {
                        String line;

                        while ((line = br.readLine()) != null) {
                            String[] word_data = Utils.tab_pattern.split(line);

                            String word = word_data[0];
                            String encodedFileOcc = word_data[4];

                            // Convert the string that encodes the number of occurrences per files into a Map
                            Map<Integer, Double> compressedVectorAsMap = decodeFileOccDistribution(encodedFileOcc);
                            int nonNullValues = compressedVectorAsMap.size();

                            // write the binary representation
                            compressed_vector_byte = CompressionUtils.toByteArray(compressedVectorAsMap);
                            fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
                            fo.write(sep_binary);

                            // write index info 
                            index_writer.println(id_word + "\t" + start_binary_vec + "\t" + nonNullValues + "\t" + word);

                            start_binary_vec += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
                            start_binary_vec += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator

                            id_word++;
                        }
                    }
                }
            }
        }
        logger.info("model save at: " + model.path);
    }

    public static double computeEntitySimilarity(ModelAccessor model, IndexedVectorInfo eAID, IndexedVectorInfo eBID) throws FileNotFoundException, IOException, SLIB_Ex_Critic {

        double[] eA = model.vectorRepresentationOf(eAID).values;
        double[] eB = model.vectorRepresentationOf(eBID).values;

        return CosineSimilarity.sim(eA, eB);
    }

    public static RQueue<String, Double> computeBestSimilarities(ModelAccessor_2D modelAccessor, IndexedVectorInfo queryVectorInfo, int k_limit) throws IOException, SLIB_Ex_Critic {

        int nbComparisons = modelAccessor.getConf().entity_size;
        logger.info("entity=" + queryVectorInfo.label + "\tk=" + k_limit + " on " + nbComparisons + " entities (vector size: " + modelAccessor.getConf().vec_size + ")");

        Timer t = new Timer();
        t.start();

        double[] q = modelAccessor.vectorRepresentationOf(queryVectorInfo).values;

        RQueue<String, Double> bestScores = new RQueue(k_limit);

        Iterator<IndexedVector> vectorIterator = modelAccessor.iterator();

        int i = 0;

        while (vectorIterator.hasNext()) {

            IndexedVector vinfo = vectorIterator.next();
            double[] v = vinfo.values;

            double sim = CosineSimilarity.sim(q, v);

            bestScores.add(vinfo.label, sim);

            i++;
            if (i % 1000 == 0) {
                String p = Utils.format2digits((double) i * 100.0 / nbComparisons);
                System.out.print(i + "/" + nbComparisons + "  " + p + "% \r");
            }
        }
        t.stop();
        t.elapsedTime();
        return bestScores;

    }

//
//        double[] entity_target_vec = modelAccessor.vectorRepresentationOf(entityIndex.get(entityTarget));
//        double[] entity_o;
//
//        Map<Integer, EntityInfo_2D_MODEL> indexInfo = modelAccessor.getIndexedElementInfo();
//        Map<Integer, Double> scores = new HashMap();
//
//        int c = 0;
//        int skipped = 0;
//        int id;
//        for (Map.Entry<String, Integer> e : entityIndex.entrySet()) {
//
//            id = e.getValue();
//            if (indexInfo.get(id).length_double_non_null == 0) {
//                logger.info("skip entity " + e.getKey() + "\tempty vector");
//                skipped++;
//                continue;
//            }
//
//            entity_o = modelAccessor.vectorRepresentationOf(e.getValue());
//
//            double sim = CosineSimilarity.sim(entity_target_vec, entity_o);
//            scores.put(id, sim);
//            c++;
//            if (entityIndex.size() > 20 && c % (entityIndex.size() / 20) == 0) {
//                logger.info("processsing: " + c + "/" + entityIndex.size() + "... ");
//            }
//        }
//        logger.info("number of skipped entities: " + skipped);
//
//        String[] entityIndex_rev = new String[entityIndex.size()];
//        for (Map.Entry<String, Integer> e : entityIndex.entrySet()) {
//            entityIndex_rev[e.getValue()] = e.getKey();
//        }
//
//        logger.info("sorting results...");
//        // print the sorted resultsI
//        Map<String, Double> score_results = new LinkedHashMap();
//
//        for (Map.Entry<Integer, Double> e : MapUtils.sortByValueDecreasing(scores).entrySet()) {
//
//            Integer row_id = e.getKey();
//            Double score = e.getValue();
//            if (outputResult) {
//                logger.info("\t" + entityIndex_rev[row_id] + "\t" + score);
//            }
//            score_results.put(entityIndex_rev[row_id], score);
//            k_limit--;
//            if (k_limit == 0) {
//                break;
//            }
//        }
//        t.elapsedTime();
//        return score_results;
//
//    }
//    public static Map<String, Double> computeBestDocSimilarity_Advanced(
//            String docLabel, Map<String, Integer> docIndex, ModelAccessor_2D model_doc_accessor,
//            Map<String, Integer> termIndex, ModelAccessor_2D model_term_accessor,
//            int k_limit, boolean outputResult) throws IOException, SLIB_Ex_Critic {
//
//        logger.info("entity=" + docLabel + "\tk=" + k_limit + " on " + docIndex.size() + " documents");
//
//        Timer t = new Timer();
//        t.start();
//
//        double[] entity_target_vec = model_doc_accessor.vectorRepresentationOf(docIndex.get(docLabel));
//        double[] entity_o;
//
//        Map<Integer, EntityInfo_2D_MODEL> docInfo = model_doc_accessor.getIndexedElementInfo();
//        Map<Integer, Double> scores = new HashMap();
//
//        int c = 0;
//        int skipped = 0;
//        int id;
//        for (Map.Entry<String, Integer> e : docIndex.entrySet()) {
//
//            id = e.getValue();
//            if (docInfo.get(id).length_double_non_null == 0) {
//                logger.info("skip entity " + e.getKey() + "\tempty vector");
//                skipped++;
//                continue;
//            }
//
//            entity_o = model_doc_accessor.vectorRepresentationOf(e.getValue());
//
//            double sim = computeDocSimNewApproach(entity_target_vec, entity_o, termIndex, model_term_accessor);
//            scores.put(id, sim);
//            c++;
//            if (docIndex.size() > 20 && c % (docIndex.size() / 20) == 0) {
//                logger.info("processsing: " + c + "/" + docIndex.size() + "... ");
//            }
//        }
//        logger.info("number of skipped entities: " + skipped);
//
//        String[] entityIndex_rev = new String[docIndex.size()];
//        for (Map.Entry<String, Integer> e : docIndex.entrySet()) {
//            entityIndex_rev[e.getValue()] = e.getKey();
//        }
//
//        logger.info("sorting results...");
//        // print the sorted resultsI
//        Map<String, Double> sorted_score_results = new HashMap();
//
//        for (Map.Entry<Integer, Double> e : MapUtils.sortByValueDecreasing(scores).entrySet()) {
//
//            Integer row_id = e.getKey();
//            Double score = e.getValue();
//            if (outputResult) {
//                logger.info("\t" + entityIndex_rev[row_id] + "\t" + score);
//            }
//            sorted_score_results.put(entityIndex_rev[row_id], score);
//            k_limit--;
//            if (k_limit == 0) {
//                break;
//            }
//        }
//        t.elapsedTime();
//        return sorted_score_results;
//
//    }
//    public static double computeDocSimNewApproach(double[] vec_entity_a, double[] vec_entity_b, Map<String, Integer> termDimensionIndex, ModelAccessor termModelAccessor) throws SLIB_Ex_Critic {
//
//        double dotProduct = 0;
//        double normA = 0;
//        double normB = 0;
//
//        // (redefined) norm A 
//        for (int i = 0; i < vec_entity_a.length; i++) {
//
//            double[] a_i_vec = termModelAccessor.vectorRepresentationOf(i);
//
//            for (int j = 0; j < vec_entity_b.length; j++) {
//                double[] a_j_vec = termModelAccessor.vectorRepresentationOf(j);
//                normA += vec_entity_a[i] * vec_entity_a[j] * CosineSimilarity.sim(a_i_vec, a_j_vec);
//            }
//        }
//
//        // (redefined) norm B
//        for (int i = 0; i < vec_entity_b.length; i++) {
//
//            double[] b_i_vec = termModelAccessor.vectorRepresentationOf(i);
//
//            for (int j = 0; j < vec_entity_b.length; j++) {
//
//                double[] b_j_vec = termModelAccessor.vectorRepresentationOf(j);
//                normB += vec_entity_b[i] * vec_entity_b[j] * CosineSimilarity.sim(b_i_vec, b_j_vec);
//            }
//        }
//
//        // (redefined) dot product 
//        for (int i = 0; i < vec_entity_a.length; i++) {
//
//            double[] a_i_vec = termModelAccessor.vectorRepresentationOf(i);
//
//            for (int j = 0; j < vec_entity_b.length; j++) {
//
//                double[] b_j_vec = termModelAccessor.vectorRepresentationOf(j);
//                dotProduct += vec_entity_a[i] * vec_entity_b[j] * CosineSimilarity.sim(a_i_vec, b_j_vec);
//            }
//        }
//
//        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
//    }
    public static void buildTerm2TermDM(String corpusDir, String voc_file, String model_dir, int nbThreads) throws SLIB_Exception, IOException {
        DMEngine.build_distributional_model_TERM_TO_TERM(corpusDir, voc_file, model_dir, nbThreads);
    }

//    static void reduceModelWithTermsAsDimensions(VocStatConf vstatconf, ModelConf mconf, ActionConf_ReduceTerm2Term conf) throws SLIB_Ex_Critic {
//
//        if (mconf.nb_files != vstatconf.nb_files) {
//            throw new SLIB_Ex_Critic("Error, voc stats and model conf do not specify the same number of files... Are you sure you are using the good stats/model?");
//        }
//        logger.info("Loading entity index...");
//        Map<String, Integer> rowIndex = XPUtils.loadMAP(mconf.getEntityIndex());
//        logger.info("Loading dimension index...");
//        Map<String, Integer> dimensionIndex = XPUtils.loadMAP(mconf.getDimensionIndex());
//        Map<Integer, String> dimensionIndex_revert = MapUtils.revert(dimensionIndex);
//
//        logger.info("Loading voc usage...");
//        Map<Integer, Integer> vocTermUsage = XPUtils.loadVocUsage(vstatconf.getVocUsageFile());
//        Map<String, Integer> vocStatIndex = XPUtils.loadMAP(vstatconf.getVocIndex());
//
//        logger.info("Computing coverage of labels associated to dimensions...");
//        Map<Integer, Double> dimensionLabelCoverage = new HashMap();
//        for (Map.Entry<String, Integer> e : dimensionIndex.entrySet()) {
//
//            String dim_label = e.getKey();
//            int dim_id = e.getValue();
//
//            if (!vocStatIndex.containsKey(dim_label)) {
//                throw new SLIB_Ex_Critic("Error cannot define the dimension id associated to the dimension '" + dim_label + "'. Are you sure that the statistics in use have been computed using the vocabulary used to compute the model?");
//            }
//
//            int dim_id_in_vocstat = vocStatIndex.get(dim_label);
//            double coverage = (double) vocTermUsage.get(dim_id_in_vocstat) / (double) vstatconf.nb_files;
//            dimensionLabelCoverage.put(dim_id, coverage);
//        }
//
//        logger.info("Loading original model");
//
//        ModelAccessorMemory_2D modelAccessor = new ModelAccessorMemory_2D(mconf);
//
//        Map<Integer, Double> dimensionLabelCoverage_sorted;
//
//        if (conf.USE_HIGH_COVERAGE) {
//            logger.info("Use dimensions with best coverage");
//            dimensionLabelCoverage_sorted = MapUtils.sortByValueDecreasing(dimensionLabelCoverage);
//        } else {
//            logger.info("Use dimensions with lower coverage");
//            dimensionLabelCoverage_sorted = MapUtils.sortByValue(dimensionLabelCoverage);
//        }
//
//        List<Integer> selected_dims = new ArrayList();
////        Map<String, Integer> dimension_index_reduced = new HashMap();
//
//        if (conf.USE_K_APPROACH) {
//
//            Long nbDimensions = conf.k;
//
//            if (nbDimensions < 1 || nbDimensions > dimensionIndex.size() - 1) {
//                throw new SLIB_Ex_Critic("Incorrect number of dimensions, accepted [1," + (dimensionIndex.size() - 1) + "], initial model has " + dimensionIndex.size() + " dimensions");
//            }
//
//            logger.info("Identifying more relevant dimensions, k=" + nbDimensions);
//
//            // select the best dimensions
//            long k = nbDimensions;
//
//            int c = 0;
//            int dim_id;
//            String dim_label;
//            double dim_val;
//
//            for (Map.Entry<Integer, Double> e : dimensionLabelCoverage_sorted.entrySet()) {
//
//                dim_id = e.getKey();
//                dim_val = e.getValue();
//                dim_label = dimensionIndex_revert.get(dim_id);
//
//                logger.info(c + "\t" + dim_label + " (id entity=" + dim_id + ", id dimension=" + dim_id + ")\t" + dim_val);
//                selected_dims.add(dim_id);
//
//                k--;
//                c++;
//                if (k == 0) {
//                    break;
//                }
//            }
//        } else { // AUTO SELECTION OF THE DIMENSIONS NUMBER
//
//            logger.info("Identifying reduced set of dimensions which generates no more null vectors");
//
//            logger.info("Building reverse model for computing <dimension,entities>");
//
//            // We built a matrix which will enable us a fast access of all the entity ids
//            // associated to a specified dimension, i.e., the value of the dimension of 
//            // the entity representation is not set to null
//            Map<Integer, Set<Integer>> reverse_matrix = new HashMap();
//
//            // We also save the ids of the entities which are already associated to empty vectors
//            Set<Integer> entities_with_empty_vectors = new HashSet();
//
//            int count = 1;
//            for (Integer id_entity : rowIndex.values()) {
//
//                Set<Integer> nonnullDimensions = modelAccessor.getCompressedRepresentation(id_entity).keySet();
//
//                if (nonnullDimensions.isEmpty()) { // empty vec
//                    entities_with_empty_vectors.add(id_entity);
//                } else {
//                    for (Integer dim_id : nonnullDimensions) {
//                        if (!reverse_matrix.containsKey(dim_id)) {
//                            reverse_matrix.put(dim_id, new HashSet());
//                        }
//                        reverse_matrix.get(dim_id).add(id_entity);
//                    }
//                }
//                if (count % 100 == 0) {
//                    logger.info("..." + count + "/" + rowIndex.size());
//                }
//                count++;
//            }
//            modelAccessor = null;
//
//            // These are the entities for which the reduction will lead to 
//            // empty vectors if we only consider the dimensions specified 
//            // into selected_dims (we remove entities which are already associated to empty vectors)
//            Set<Integer> uncapturedEntities = new HashSet(rowIndex.values());
//            uncapturedEntities.removeAll(entities_with_empty_vectors);
//            logger.info("Non null vector expected " + uncapturedEntities.size() + " (" + entities_with_empty_vectors.size() + " already associated to null vectors)");
//
//            logger.info("Selecting dimensions...");
//
//            count = 0;
//
//            for (Map.Entry<Integer, Double> e : dimensionLabelCoverage_sorted.entrySet()) {
//
//                count++;
//                Integer dim_id = e.getKey();
//
//                if (!reverse_matrix.containsKey(dim_id)) {
//                    String dim_label = dimensionIndex_revert.get(dim_id);
//                    logger.info("skip dimension " + dim_label + " (id=" + dim_id + ") null dimension, i.e. all entities have null for this dimension\t");
//                    //continue; we could also exclude these dimensions but it will create inconsitencies if they are not excluded when k is set manualliy (see above)
//                } else {
//                    // we remove all the entities which have a non null value for this dimension
//                    uncapturedEntities.removeAll(reverse_matrix.get(dim_id));
//                }
//                selected_dims.add(dim_id);
//
//                if (count % 1000 == 0) {
//                    logger.info("\t dim:" + count + "/" + dimensionLabelCoverage_sorted.size() + " - added=" + selected_dims.size() + "\tmissing entities: " + uncapturedEntities.size() + "...");
//                }
//
//                if (uncapturedEntities.isEmpty()) {
//                    break;
//                }
//            }
//            if (!uncapturedEntities.isEmpty()) {
//                throw new SLIB_Ex_Critic("An error occured during model reduction, please report the problem... missing entities: " + uncapturedEntities.size());
//            }
//            logger.info("Number of dimensions selected " + selected_dims.size() + "/" + dimensionLabelCoverage_sorted.size());
//        }
//
//        if (conf.FLUSH_MODEL) {
//            Collections.sort(selected_dims);
//
//            // create new indexes
//            Map<String, Integer> newDimensionIndex = new HashMap();
//            Map<Integer, Integer> new_dimension_ids = new HashMap();
//
//            for (int new_id = 0; new_id < selected_dims.size(); new_id++) {
//                int old_id = selected_dims.get(new_id);
//                new_dimension_ids.put(old_id, new_id);
//                newDimensionIndex.put(dimensionIndex_revert.get(old_id), new_id);
//            }
//
//            // reduce the matrix
//            logger.info("Reducing model");
//            SparseMatrix newMatrix = SparseMatrixGenerator.buildSparseMatrix(rowIndex.size(), selected_dims.size());
//
//            int nb_entity = rowIndex.size();
//            int nb_entity_done = 0;
//
//            for (Integer entity_id : rowIndex.values()) {
//
//                if (nb_entity_done % 100 == 0) {
//                    logger.info("\t" + nb_entity_done + "/" + nb_entity);
//                }
//
//                double[] vec = modelAccessor.vectorRepresentationOf(entity_id);
//                for (Integer dim_id_old : selected_dims) {
//                    double dim_val = vec[dim_id_old];
//                    int dim_id_new = new_dimension_ids.get(dim_id_old);
//                    newMatrix.set(entity_id, dim_id_new, dim_val); // note that i = newIds.get(i) 
//                }
//                nb_entity_done++;
//            }
//
//            logger.info("flushing new model");
//
//            ModelConf newModelConf = new ModelConf(ModelType.TWO_D_TERM_MODEL, mconf.name, conf.NEW_MODEL_DIR, mconf.entity_size, selected_dims.size(), mconf.nb_files, mconf.format_version);
//            ConfUtils.initModel(newModelConf);
//            XPUtils.flushMAP(rowIndex, newModelConf.getEntityIndex());
//            XPUtils.flushMAP(newDimensionIndex, newModelConf.getDimensionIndex());
//            ConfUtils.buildIndex(newModelConf, rowIndex, newMatrix);
//            ConfUtils.buildModelBinary(newModelConf, rowIndex, newMatrix);
//        } else {
//            logger.info("Do not flush new model");
//        }
//    }
    /**
     * Convert a String of the form
     * FILE_ID_1-NB_OCCURRENCE_FILE_1:FILE_ID_2-NB_OCCURRENCE_FILE_2 etc (ex:
     * 2-50:3-1:4-1) into a map. Keys refer to the file ids values refer to the
     * associated value.
     *
     * @param encodedFileOcc
     * @return
     */
    private static Map<Integer, Double> decodeFileOccDistribution(String encodedFileOcc) {

        Map<Integer, Double> encodedResult = new HashMap();

        String[] nbocc_files = Utils.colon_pattern.split(encodedFileOcc);
        for (String nbocc_file : nbocc_files) {
            String[] data = Utils.dash_pattern.split(nbocc_file);
            encodedResult.put(Integer.parseInt(data[0]), Double.parseDouble(data[1]));
        }
        return encodedResult;
    }

    static void reduceSizeVectorRepresentations(String model_dir, String new_model_dir, int nbDimension) throws Exception {

        ModelConf mConf = ModelConf.load(model_dir);

        if (nbDimension > mConf.vec_size) {
            throw new SLIB_Ex_Critic("Specified number higher than current dimension number " + mConf.vec_size);
        }

        // 1 - count the number of time a specific dimension is used in a vector representation
        int[] vec_using_dimension = new int[mConf.vec_size];

        ModelAccessor_2D maccessor = new ModelAccessorPersistance_2D(mConf);
        Iterator<IndexedVector> it = maccessor.iterator();
        int c = 0;

        logger.info("Detecting dimensions to select");
        while (it.hasNext()) {

            if (c % 1000 == 0) {
                String p = Utils.format2digits((double) c * 100.0 / (double) mConf.entity_size);
                System.out.print("processing... " + c + "/" + mConf.entity_size + "\t" + p + "% \r");
            }
            c++;
            double[] vec = it.next().values;

            for (int i = 0; i < vec.length; i++) {
                if (vec[i] != 0) {
                    vec_using_dimension[i]++;
                }
            }
        }

        // We select the best dimensions
        RQueue<Integer, Integer> bestDimensionsResult = new RQueue(nbDimension);
        for (int i = 0; i < vec_using_dimension.length; i++) {
            bestDimensionsResult.add(i, vec_using_dimension[i]);
        }
        List<Integer> bestDimensions = bestDimensionsResult.getLabels();

        // 2 reduce the model only considering the selected dimensions
        ModelConf new_model_conf = new ModelConf(ModelType.TWO_D_TERM_DOC, mConf.name + " reduced", new_model_dir, mConf.entity_size, nbDimension, mConf.nb_files, mConf.format_version);
        ModelConfUtils.initModel(new_model_conf);

        // We generate 
        // (i)  the index in which information regarding the location of term vector representation will be specified 
        // (ii) the binary index in which the binary representation of vectors will be saved
        long start_binary_vec = 0;
        byte[] sep_binary = {0};
        File f_binary = new File(new_model_conf.getModelBinary());

        int null_vectors = 0; // number of reduced vector representations that correspond to empty vectors
        int id_word = 0;

        logger.info("Reducing vector representations");

        byte[] compressed_vector_byte;
        try (PrintWriter index_writer = new PrintWriter(new_model_conf.getModelIndex(), "UTF-8")) {
            try (FileOutputStream fo = new FileOutputStream(f_binary)) {

                index_writer.println("ID_WORD\tSTART_POS\tLENGTH_DOUBLE_NON_NULL\tWORD");

                // We iterate over the words and create their reduced vector representation
                it = maccessor.iterator();
                while (it.hasNext()) {

                    if (id_word % 1000 == 0) {
                        String p = Utils.format2digits((double) id_word * 100.0 / (double) mConf.entity_size);
                        System.out.print("processing... " + id_word + "/" + mConf.entity_size + "\t" + p + "% \r");
                    }
                    IndexedVector indexedVec = it.next();

                    String word = indexedVec.label;
                    double[] vec = indexedVec.values;

                    // all values that are not associated to selected dimensions are set to 0
                    // the others are set to the original value
                    Map<Integer, Double> compressedReducedVectorAsMap = new HashMap();
                    int new_id = 0;
                    for (Integer i : bestDimensions) {
                        if (vec[i] != 0) {
                            compressedReducedVectorAsMap.put(new_id, vec[i]);
                        }
                        new_id++;
                    }

                    int nonNullValues = compressedReducedVectorAsMap.size();
                    if (nonNullValues == 0) {
                        null_vectors++;
                    }

                    // write the binary representation
                    compressed_vector_byte = CompressionUtils.toByteArray(compressedReducedVectorAsMap);
                    fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
                    fo.write(sep_binary);

                    // write index info 
                    index_writer.println(id_word + "\t" + start_binary_vec + "\t" + nonNullValues + "\t" + word);

                    start_binary_vec += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
                    start_binary_vec += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator

                    id_word++;
                }
            }
        }
        String p = Utils.format2digits((double) null_vectors * 100.0 / (double) mConf.entity_size);
        logger.info("number of empty vector representations : " + null_vectors + "/" + mConf.entity_size + "\t" + p + "%");
        logger.info("reduced model save at: " + new_model_conf.path);

    }

}
