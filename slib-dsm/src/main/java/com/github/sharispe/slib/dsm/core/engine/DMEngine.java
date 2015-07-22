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
package com.github.sharispe.slib.dsm.core.engine;

import com.github.sharispe.slib.dsm.core.model.utils.compression.CompressionUtils;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.GConstants;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConfUtils;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelType;
import com.github.sharispe.slib.dsm.utils.BinarytUtils;
import com.github.sharispe.slib.dsm.utils.Utils;
import static com.github.sharispe.slib.dsm.utils.Utils.logger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import slib.utils.ex.SLIB_Ex_Critic;

import slib.utils.ex.SLIB_Exception;

/**
 *
 * Distributional Model Engine. Class used to build distributional models
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class DMEngine {

    public static void build_distributional_model_TERM_TO_TERM(String corpusDir, String vocabularyFile, String model_dir, int window_token_size, int nbThreads, int nbFilesPerChunk, int max_size_matrix) throws SLIB_Exception, IOException, InterruptedException {

        Vocabulary vocabulary = new Vocabulary(vocabularyFile);
        VocabularyIndex vocabularyIndex = new VocabularyIndex(vocabulary);
        CoOcurrenceEngine engine = new CoOcurrenceEngine(vocabularyIndex);
        engine.computeCoOcurrence(corpusDir, model_dir, window_token_size, nbThreads, nbFilesPerChunk, max_size_matrix);
        ModelConf modelConf = new ModelConf(ModelType.TWO_D_TERM_DOC, "TERM x TERM model", model_dir, vocabulary.size(), vocabulary.size(), engine.getNbFilesProcessed(), "0.1");
        buildModel(modelConf, vocabularyIndex, model_dir + "/matrix");

    }

    /**
     *
     * @param model
     * @param index
     * @param matrix
     * @throws SLIB_Ex_Critic
     */
    private static void buildModel(ModelConf model, VocabularyIndex index, String matrix_file) throws SLIB_Ex_Critic, IOException {

        logger.info("Matrix " + matrix_file);
        logger.info("Writting model index to " + model.getModelIndex());
        logger.info("Writting the model binary into " + model.getModelBinary());

        ModelConfUtils.initModel(model);

        byte[] sep = {0};
        int warning = 0;

        try (PrintWriter indexWriter = new PrintWriter(model.getModelIndex(), "UTF-8")) {

            try (FileOutputStream fo = new FileOutputStream(new File(model.getModelBinary()))) {

                try (BufferedReader br = new BufferedReader(new FileReader(matrix_file))) {

                    // refer to the documentation of the format
                    indexWriter.println("ID_ENT\tSTART_POS\tLENGTH_DOUBLE_NON_NULL\tWORD");

                    int c = 0;
                    int c_total = index.getVocabulary().size();
                    long current_start_pos = 0;

                    String line;
                    String[] data, data2;
                    int word_id, word_id_c, nonNullValues;
                    double occ;

                    Map<Integer, Double> vectorAsMap = null;
                    byte[] compressed_vector_byte;

                    Set<Integer> id_vectors = new HashSet(index.wordIdToWord.keySet());

                    // create the vector representations that are specified into the matrix file
                    while ((line = br.readLine()) != null) {

                        c++;

                        // extract the vector representation and associated word id
                        data = Utils.colon_pattern.split(line.trim());
                        word_id = Integer.parseInt(data[0]);
                        id_vectors.remove(word_id);

                        vectorAsMap = new HashMap();
                        for (int i = 1; i < data.length; i++) {
                            data2 = Utils.dash_pattern.split(data[i]);// 30-456
                            word_id_c = Integer.parseInt(data2[0]);
                            occ = Double.parseDouble(data2[1]);
                            vectorAsMap.put(word_id_c, occ);
                        }

                        nonNullValues = vectorAsMap.size();

                        indexWriter.println(word_id + "\t" + current_start_pos + "\t" + nonNullValues + "\t" + index.getWord(word_id));
                        current_start_pos += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
                        current_start_pos += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator

                        // here we retrieve the number of pair we will have in the compressed vector
                        // i.e. [(1,0.4),(30,0.6),(5,0.7)...] refer to the doc
                        compressed_vector_byte = CompressionUtils.toByteArray(vectorAsMap);
                        fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
                        fo.write(sep);

                        if (c % 1000 == 0) {
                            double p = c * 100.0 / c_total;
                            System.out.print("processing " + c + "/" + c_total + "\t" + Utils.format2digits(p) + "%\t\r");
                        }
                    }
                    // process words that do not have a vector representation 
                    // into the matrix file

                    compressed_vector_byte = BinarytUtils.toByteArray(new double[0]);

                    for (Integer id : id_vectors) {
                        nonNullValues = 0;
                        indexWriter.println(id + "\t" + current_start_pos + "\t" + nonNullValues + "\t" + index.getWord(id));
                        current_start_pos += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
                        current_start_pos += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator

                        fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
                        fo.write(sep);

                        logger.warn("[Warning] building the model, the entity '" + index.getWord(id)
                                + "' is associated to an empty vector (only null values)... "
                                + "This can lead to incoherent results performing some treatments.");
                        warning++;

                        if (c % 1000 == 0) {
                            double p = c * 100.0 / c_total;
                            System.out.print("processing " + c + "/" + c_total + "\t" + Utils.format2digits(p) + "%\t\r");
                        }
                    }

                }
                indexWriter.close();
                fo.close();

                if (warning != 0) {
                    logger.info(warning + " warnings (null vectors)");
                }
                FileUtils.deleteQuietly(new File(matrix_file));
                logger.info("Writting model index to " + model.getModelIndex());
                logger.info("Model built at " + model.path);

            }
        }
    }

}

// TODO REMOVE
//    public static void buildModelBinary(ModelConf model, Map<String, Integer> index, SparseMatrix m) throws SLIB_Ex_Critic {
//
//        logger.info("Writting the model into " + model.getModelBinary());
//
//        byte[] sep = {0};
//
//        File f = new File(model.getModelBinary());
//        int warning = 0;
//
//        try (FileOutputStream fo = new FileOutputStream(f)) {
//            for (Map.Entry<String, Integer> e : MapUtils.sortByValue(index).entrySet()) {
//
//                //double[] cooc_term = m.getElementVector(e.getValue());
//                int id = e.getValue();
//                Map<Integer, Double> row = m.getDimensionValuesForElement(id);
//                int nonNullValues = m.getNbNonNullValuesInElementVector(id);
//
//                if (nonNullValues == 0) {
//                    logger.warn("[Warning] building the model, the entity '" + e.getKey()
//                            + "' is associated to an empty vector (only null values)... "
//                            + "This can lead to incoherent results performing some treatments.");
//                    warning++;
//                } else if (nonNullValues == -1) {
//                    logger.warn("[Warning] building the model, the entity '" + e.getKey()
//                            + "' is associated to an empty vector (only null values)... because it has not been processed in the given corpora... "
//                            + "This can lead to incoherent results performing some treatments.");
//                    nonNullValues = 0;
//                    warning++;
//                }
//
//                // here we retrieve the number of pair we will have in the compressed vector
//                // i.e. [(1,0.4),(30,0.6),(5,0.7)...] refer to the doc
//                byte[] compressed_vector_byte;
//
//                if (row == null) {
//                    compressed_vector_byte = BinarytUtils.toByteArray(new double[0]);
//                } else {
//                    compressed_vector_byte = CompressionUtils.toByteArray(row);
//                }
//                fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
//                fo.write(sep);
//            }
//
//            if (warning != 0) {
//                logger.info(warning + " warnings (null vectors)");
//            }
//            logger.info("Model built at " + model.path);
//
//        } catch (IOException e) {
//            throw new SLIB_Ex_Critic(e.getMessage());
//        }
//    }

