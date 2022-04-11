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
import com.github.sharispe.slib.dsm.utils.StanfordLemmatizer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sml.sm.core.measures.vector.CosineSimilarity;
import com.github.sharispe.slib.dsm.core.engine.DMEngine;
import com.github.sharispe.slib.dsm.core.engine.MapIndexer;
import com.github.sharispe.slib.dsm.core.engine.VocStatInfo;
import com.github.sharispe.slib.dsm.core.engine.VocStatComputer;
import com.github.sharispe.slib.dsm.core.engine.WordInfo;
import com.github.sharispe.slib.dsm.core.model.access.ModelAccessor;
import com.github.sharispe.slib.dsm.core.model.access.twodmodels.IndexedVectorInfoIterator;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Timer;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class SlibDist_Wrapper {

    static Logger logger = LoggerFactory.getLogger(SlibDist_Wrapper.class);

    /**
     * Create a lemmatized version of the files contained in a directory.
     *
     * @param indir the directory in which are the files to lemmatize
     * @param outdir the directory in which to save the result files
     * @param pathPOSmodel the path to the POS model to consider
     * @param skipExisting if true existing lemmatized files will be excluded 
     * @throws IOException if an IO error occurs
     */
    public static void lemmatize(String indir, String outdir, String pathPOSmodel, boolean skipExisting) throws IOException {

        logger.info("Lemmatize: '" + indir + "'");
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
                StanfordLemmatizer.lemmatize(f.getAbsolutePath(), fname, pathPOSmodel);
            }
        }
        logger.info("done");
    }

    /**
     * Compute a TF-IDF vector representation of documents considering a given
     * vocabulary. The result is a set of files representing the vector
     * representation of each file composing the given directory.
     *
     * @param voc_index the location of the index
     * @param model_dir the location of the model
     * @throws FileNotFoundException if a file is not found
     * @throws IOException if an IO error occurs
     * @throws SLIB_Ex_Critic for other errors
     */
    public static void build_model_term_doc_classic(String voc_index, String model_dir) throws FileNotFoundException, IOException, SLIB_Ex_Critic {

        VocStatInfo vocInfo = new VocStatInfo(voc_index + "/" + VocStatComputer.GENERAL_INFO);
        ModelConf model = new ModelConf(ModelType.TWO_D_TERM_DOC, "T x D model", model_dir, vocInfo.vocSize, vocInfo.nbFiles, vocInfo.nbFiles, "0.1");

        ModelConfUtils.initModel(model);

        // Compute the vector representation of each word
        logger.info("Computing vector representation for each document (n=" + vocInfo.vocSize + ")");

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
                    System.out.print("processing chunk " + chunk_done_count + "/" + chunk_index.size() + "\t" + p + "%    \r");

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

        RQueue<String, Double> bestScores = new RQueue<String, Double>(k_limit);

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

    public static void buildTerm2TermDM(String corpusDir, String vocFile, String model_dir, int window_token_size, int nbThreads, int nbFilesPerChunk, int max_matrix_size) throws SLIB_Exception, IOException, InterruptedException {

        DMEngine.build_distributional_model_TERM_TO_TERM(corpusDir, vocFile, model_dir, window_token_size, nbThreads, nbFilesPerChunk, max_matrix_size);
    }

    static void buildTerm2Term_PMI_DM(String cooccurence_model_dir, String voc_stat_dir, String pmi_model_dir) throws SLIB_Ex_Critic, Exception {

        // loading the model that contains the cooccurences 
        ModelConf cocc_model_conf = ModelConf.load(cooccurence_model_dir);

        // prepare new model 
        ModelConf new_model_conf = new ModelConf(ModelType.TWO_D_TERM_DOC, "PMI model", pmi_model_dir, cocc_model_conf.entity_size, cocc_model_conf.vec_size, cocc_model_conf.nb_files, cocc_model_conf.format_version);
        ModelConfUtils.initModel(new_model_conf);

        // load basic stats dir
        // we generate the new corpus index
        // and we retrive all the chunk keys
        logger.info("Loading voc stats into memory");
        String voc_stat_chunk_index = voc_stat_dir + "/" + VocStatComputer.CHUNK_INDEX;
        String voc_stat_info = voc_stat_dir + "/" + VocStatComputer.GENERAL_INFO;
        logger.info("Loading index: " + voc_stat_chunk_index);
        Map<String, Integer> chunkStats = Utils.loadMap(voc_stat_chunk_index);

        VocStatInfo idxInfo = new VocStatInfo(voc_stat_info);
        long nb_word_corpus = idxInfo.nbScannedWords;

        Map<String, WordInfo> wordStat = new HashMap<String, WordInfo>();

        int c = 0;

        for (Integer chunkID : chunkStats.values()) {
            c++;
            System.out.print("loading chunk " + c + "/" + chunkStats.size() + "   \r");
            wordStat.putAll(MapIndexer.loadMapWordInfo(new File(voc_stat_dir + "/" + chunkID)));
        }
        logger.info("voc stat loaded: size=" + wordStat.size());

        ModelAccessor_2D modelAccessor = new ModelAccessorPersistance_2D(cocc_model_conf);

        long nb_word_vectors = cocc_model_conf.entity_size;

        Iterator<IndexedVector> idxVectorIterator = modelAccessor.iterator();

        // Load the labels associated to the dimension
        Iterator<IndexedVectorInfo> it = new IndexedVectorInfoIterator(cocc_model_conf);
        Map<Integer, String> idToLabel = new HashMap<Integer, String>();

        while (it.hasNext()) {
            IndexedVectorInfo info = it.next();
            idToLabel.put(info.id, info.label);
        }

        logger.info("id correspondances loaded: " + idToLabel.size());

        // 
        long start_binary_vec = 0;
        byte[] sep_binary = {0};
        File f_binary = new File(new_model_conf.getModelBinary());
        byte[] compressed_vector_byte;

        long id_a;
        long occ_a, occ_b, occ_ab, den;
        String word_b;

        long warning = 0, nb_values = 0;
        c = 0;

        try (PrintWriter index_writer = new PrintWriter(new_model_conf.getModelIndex(), "UTF-8")) {
            try (FileOutputStream fo = new FileOutputStream(f_binary)) {

                index_writer.println("ID_WORD\tSTART_POS\tLENGTH_DOUBLE_NON_NULL\tWORD");

                while (idxVectorIterator.hasNext()) {

                    c++;

                    if (c % 100 == 0) {
                        double p = c * 100.0 / nb_word_vectors;
                        System.out.print("processing vector " + c + "/" + nb_word_vectors + "\t" + Utils.format2digits(p) + "%\r");
                    }

                    IndexedVector v = idxVectorIterator.next();

                    id_a = v.id;
                    String word_a = v.label;
                    double[] cooccurences_a = v.values;

                    if (!wordStat.containsKey(word_a)) {
                        occ_a = 0;
                    } else {
                        occ_a = wordStat.get(word_a).nbOccurrences;
                    }

                    double[] pmi = new double[cooccurences_a.length];
                    int nonNullValues = 0;

                    for (int id_b = 0; id_b < cooccurences_a.length; id_b++) {

                        word_b = idToLabel.get(id_b);
                        if (!wordStat.containsKey(word_b)) {
                            occ_b = 0;
                        } else {
                            occ_b = wordStat.get(word_b).nbOccurrences;
                        }
                        occ_ab = (long) cooccurences_a[id_b];

                        den = (occ_a * occ_b);

                        if (den != 0 && occ_ab != 0) {
                            pmi[id_b] = Math.log((occ_ab * nb_word_corpus) / den);
                            if (pmi[id_b] == 0) {
                                pmi[id_b] = 0.01; // since 0 value is the minimal value
                            } else {
                                pmi[id_b] = (double) Math.round(pmi[id_b] * 100) / 100; // round to 2 decimal
                            }
                        } else {
                            pmi[id_b] = 0;
                        }

                        if (pmi[id_b] != 0) {
                            nonNullValues++;
                        }

                        if (occ_a == 0 || occ_b == 0) {
                            warning++;
                        }
                        nb_values++;
                    }

                    double[] compressArray = CompressionUtils.compressDoubleArray(pmi);
                    compressed_vector_byte = CompressionUtils.toByteArray(compressArray);

                    // write the binary representation
                    fo.write(compressed_vector_byte);
                    fo.write(sep_binary);

                    // write index info 
                    index_writer.println(id_a + "\t" + start_binary_vec + "\t" + nonNullValues + "\t" + word_a);

                    start_binary_vec += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
                    start_binary_vec += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator
                }
            }
        }
        if (warning != 0) {
            logger.info("[Warning] " + warning + "/" + nb_values + " pmi values have bee set to 0 due to missing frequency observation on word a or b");
        }
        logger.info("reduced model save at: " + new_model_conf.path);
    }

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

        Map<Integer, Double> encodedResult = new HashMap<Integer, Double>();

        String[] nbocc_files = Utils.colon_pattern.split(encodedFileOcc);
        for (String nbocc_file : nbocc_files) {
            String[] data = Utils.dash_pattern.split(nbocc_file);
            encodedResult.put(Integer.parseInt(data[0]), Double.parseDouble(data[1]));
        }
        return encodedResult;
    }

    static void reduceSizeVectorRepresentations_K_MostUsedDimensions(String model_dir, String new_model_dir, int nbDimension) throws Exception {

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

        // We select the best dimensions, i.e. the dimensions that are the most often used
        RQueue<Integer, Integer> bestDimensionsResult = new RQueue<Integer, Integer>(nbDimension);
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
                    Map<Integer, Double> compressedReducedVectorAsMap = new HashMap<Integer, Double>();
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

    static void reduceSizeVectorRepresentations_K_Random_Process(String model_dir, String new_model_dir, int nbDimensions, int nbIterations, boolean log_factors) throws Exception {

        logger.info("model to reduce     : " + model_dir);
        logger.info("new model           : " + new_model_dir);
        logger.info("new dimension number: " + nbDimensions);
        logger.info("nb Iterations       : " + nbIterations);
        logger.info("log used factors    : " + log_factors);

        ModelConf mConf = ModelConf.load(model_dir);

        if (nbDimensions > mConf.vec_size) {
            throw new SLIB_Ex_Critic("Specified number higher than current dimension number " + mConf.vec_size);
        }

        // 1 - generates the factors that will be used to reduce the matrix
        int group_size = mConf.vec_size / nbDimensions; // each new dimensions will summarize group_size old dimensions (a least dimension can be added).
        Integer[][][] all_params = new Integer[nbIterations][nbDimensions][group_size]; // stores the original dimensions that will be used to compute the new ones for each iteration.        
        @SuppressWarnings("unchecked")
		List<Integer>[] unused_dimensions = (List<Integer>[]) new ArrayList[nbDimensions];
        

        System.out.println("Building reduction factor matrices: n=" + mConf.vec_size + "\tk=" + nbDimensions + "\titerations=" + nbIterations + "\tgroup size: " + group_size);
        Random randomGenerator = new Random();

        for (int it = 0; it < nbIterations; it++) {

            logger.info("Generating factor for iteration " + (it + 1) + " \r");

            unused_dimensions[it] = new ArrayList<Integer>(mConf.vec_size);

            for (int i = 0; i < mConf.vec_size; i++) {
                unused_dimensions[it].add(i);
            }

            Integer[][] params_iteration = all_params[it];

            for (Integer[] param_group : params_iteration) {

                for (int i = 0; i < param_group.length; i++) {

                    int randomInt = randomGenerator.nextInt(unused_dimensions[it].size());
                    int dim_id = unused_dimensions[it].get(randomInt);
                    unused_dimensions[it].remove(randomInt);
                    param_group[i] = dim_id;
                }
            }

            if (log_factors) {

                logger.info("Generating log for factors");

                StringBuilder matrix_string = new StringBuilder();
                for (Integer[] param_group : params_iteration) {
                    for (int i = 0; i < param_group.length; i++) {
                        matrix_string.append("\t").append(param_group[i]);
                    }
                    matrix_string.append("\n");
                }
                logger.info(matrix_string.toString());

                logger.info("last dimension (if required): " + unused_dimensions[it]);
            }
        }

        int final_vec_size = nbDimensions;
        boolean addExtraDimension = false;
        if (!unused_dimensions[0].isEmpty()) { // only the parameters generated for one dimension have to be tested
            final_vec_size++;
            addExtraDimension = true;
        }

        logger.info("vector size is set to: " + final_vec_size);

        // 2 reduce the model only considering the selected dimensions
        ModelConf new_model_conf = new ModelConf(ModelType.TWO_D_TERM_DOC, mConf.name + " reduced", new_model_dir, mConf.entity_size, final_vec_size, mConf.nb_files, mConf.format_version);
        ModelConfUtils.initModel(new_model_conf);

        // We generate 
        // (i)  the index in which information regarding the location of term vector representation will be specified 
        // (ii) the binary index in which the binary representation of vectors will be saved
        long start_binary_vec = 0;
        byte[] sep_binary = {0};
        File f_binary = new File(new_model_conf.getModelBinary());

        int id_word = 0;

        logger.info("Reducing vector representations");

        ModelAccessor_2D maccessor = new ModelAccessorPersistance_2D(mConf);

        byte[] compressed_vector_byte;
        try (PrintWriter index_writer = new PrintWriter(new_model_conf.getModelIndex(), "UTF-8")) {
            try (FileOutputStream fo = new FileOutputStream(f_binary)) {

                index_writer.println("ID_WORD\tSTART_POS\tLENGTH_DOUBLE_NON_NULL\tWORD");

                // We iterate over the existing vector representations and we reduce them
                Iterator<IndexedVector> it = maccessor.iterator();

                while (it.hasNext()) {

                    if (id_word % 1000 == 0) {
                        String p = Utils.format2digits((double) id_word * 100.0 / (double) mConf.entity_size);
                        System.out.print("processing... " + id_word + "/" + mConf.entity_size + "\t" + p + "% \r");
                    }
                    IndexedVector indexedVec = it.next();

                    String word = indexedVec.label;
                    double[] old_vec = indexedVec.values;

                    double[] new_vec = new double[final_vec_size];

                    // Conceptually specking the following process consists of 
                    // (i)  generating a new reduced vector considering the set of parameters generated for this specific iteration
                    // (ii) summing this vector to the vectors already generated for the previous iterations
                    for (int i = 0; i < nbIterations; i++) {

                        for (int j = 0; j < all_params[i].length; j++) {
                            for (int k = 0; k < all_params[i][j].length; k++) {
                                new_vec[j] += old_vec[all_params[i][j][k]];
                            }
                        }
                        if (addExtraDimension) { // we process the extra dimension if any
                            for (int j = 0; j < unused_dimensions[i].size(); j++) {
                                new_vec[new_vec.length - 1] += old_vec[unused_dimensions[i].get(j)];
                            }
                        }
                    }

                    // We compute the number of non null values 
                    int nonNullValues = 0;
                    for (int i = 0; i < new_vec.length; i++) {
                        if (new_vec[i] != 0) {
                            nonNullValues++;
                        }
                    }

                    // write the binary representation
                    double[] compressed_double_array = CompressionUtils.compressDoubleArray(new_vec);
                    compressed_vector_byte = CompressionUtils.toByteArray(compressed_double_array);
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
        logger.info("reduced model save at: " + new_model_conf.path);

    }

    static void reduceWordOccMatrix(String voc_file, String model_dir, String output_dir) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
