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
package com.github.sharispe.slib.dsm.core.model.utils.modelconf;

import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrix;
import com.github.sharispe.slib.dsm.core.model.utils.compression.CompressionUtils;
import com.github.sharispe.slib.dsm.main.VocStatConf;
import com.github.sharispe.slib.dsm.utils.BinarytUtils;
import com.github.sharispe.slib.dsm.utils.FileUtility;
import com.github.sharispe.slib.dsm.utils.MapUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * Utility class used to build a distributional model
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ConfUtils {

    static Logger logger = LoggerFactory.getLogger(ConfUtils.class);

    /**
     * Initialize a model according to the given configuration.
     *
     * @param model
     * @throws SLIB_Ex_Critic
     */
    public static void initModel(ModelConf model) throws SLIB_Ex_Critic {

        try {
            logger.info("Init model " + model.name);
            FileUtility.createDir(model.path);
            flushProperties(model);

        } catch (SLIB_Exception e) {
            throw new SLIB_Ex_Critic("Error creating model " + e.getMessage());
        }
    }

    public static void flushProperties(ModelConf model) throws SLIB_Ex_Critic {

        logger.info("Writting configuration into: " + model.getModelProperties());

        try (PrintWriter writer = new PrintWriter(model.getModelProperties(), "UTF-8")) {
            writer.println(GConstants.MODEL_PARAM_TYPE + "=" + model.type);
            writer.println(GConstants.MODEL_PARAM_NAME + "=" + model.name);
            writer.println(GConstants.MODEL_PARAM_ENTITY_SIZE + "=" + model.entity_size);
            writer.println(GConstants.MODEL_PARAM_VEC_SIZE + "=" + model.vec_size);
            writer.println(GConstants.MODEL_PARAM_VERSION + "=" + model.format_version);
            writer.println(GConstants.MODEL_PARAM_NB_FILES + "=" + model.nb_files);
        } catch (Exception e) {
            throw new SLIB_Ex_Critic("An error occured writing the configuration file: " + e.getMessage());
        }
    }

    /**
     * refactor to merge with method above Initialize a model according to the
     * given configuration.
     *
     * @param conf
     * @throws SLIB_Ex_Critic
     */
    public static void initVocUsage(VocStatConf conf) throws SLIB_Ex_Critic {

        try {
            logger.info("Init voc usage directory " + conf.path);
            FileUtility.createDir(conf.path);
            flushVocStatConfProperties(conf);

            String stat_voc_copy = conf.path + GConstants.SEP + GConstants.STAT_VOC_FILE;

            logger.info("Copy vocabulary");
            Files.copy(Paths.get(conf.original_voc_file), Paths.get(stat_voc_copy), StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException | SLIB_Exception e) {
            throw new SLIB_Ex_Critic("Error creating model " + e.getMessage());
        }
    }

    /**
     * refactor to merge with method above
     *
     * @param conf
     * @throws SLIB_Ex_Critic
     */
    public static void flushVocStatConfProperties(VocStatConf conf) throws SLIB_Ex_Critic {

        logger.info("Writting properties into: " + conf.getVocProperties());

        try (PrintWriter writer = new PrintWriter(conf.getVocProperties(), "UTF-8")) {
            writer.println(GConstants.STAT_PARAM_ORIGINAL_VOC_FILE + "=" + conf.original_voc_file);
            writer.println(GConstants.STAT_PARAM_ANALYZED_DIR + "=" + conf.dir_analyzed);
            writer.println(GConstants.STAT_PARAM_NB_FILES + "=" + conf.nb_files);
        } catch (Exception e) {
            throw new SLIB_Ex_Critic("An error occured writing the property file: " + e.getMessage());
        }
    }

    /**
     *
     * @param model
     * @param vocIndex
     * @param matrix
     * @throws SLIB_Ex_Critic
     */
    public static void buildIndex(ModelConf model, Map<String, Integer> vocIndex, SparseMatrix matrix) throws SLIB_Ex_Critic {

        logger.info("Printing model index to " + model.getModelIndex());

        try (PrintWriter writer = new PrintWriter(model.getModelIndex(), "UTF-8")) {
            // refer to the documentation of the format
            writer.println("ID_ENT\tSTART_POS\tLENGTH_DOUBLE_NON_NULL");

            long current = 0;
            for (Map.Entry<String, Integer> e : MapUtils.sortByValue(vocIndex).entrySet()) {

                int id = e.getValue();
                int nonNullValues = matrix.getNbNonNullValuesInElementVector(id);

                if (nonNullValues == -1) { // if the corresponding word as not been found in the corpus
                    nonNullValues = 0;
                }

                writer.println(id + "\t" + current + "\t" + nonNullValues);
//                logger.info(e.getKey()+"\t"+id + "\t" + current + "\t" + nonNullValues + "\t" + nbFilesWithCurrentWord);
//                logger.info(Arrays.toString(matrix.getElementVector(id)));
//                logger.info("nonNullValues: "+nonNullValues);
                current += nonNullValues * 2.0 * BinarytUtils.BYTE_PER_DOUBLE;
                current += GConstants.STORAGE_FORMAT_SEPARATOR_SIZE; // separator
            }
            writer.close();
        } catch (Exception e) {
            throw new SLIB_Ex_Critic("An error occured writing the index file: " + e.getMessage());
        }

    }

    public static void buildModelBinary(ModelConf model, Map<String, Integer> vocIndex, SparseMatrix m) throws SLIB_Ex_Critic {

        logger.info("Writting the model into " + model.getModelBinary());

        byte[] sep = {0};

        File f = new File(model.getModelBinary());
        int warning = 0;

        try (FileOutputStream fo = new FileOutputStream(f)) {
            for (Map.Entry<String, Integer> e : MapUtils.sortByValue(vocIndex).entrySet()) {

                //double[] cooc_term = m.getElementVector(e.getValue());
                int id = e.getValue();
                Map<Integer, Double> row = m.getDimensionValuesForElement(id);
                int nonNullValues = m.getNbNonNullValuesInElementVector(id);

                if (nonNullValues == 0) {
                    logger.warn("[Warning] building the model, the entity '" + e.getKey()
                            + "' is associated to an empty vector (only null values)... "
                            + "This can lead to incoherent results performing some treatments.");
                    warning++;
                } else if (nonNullValues == -1) {
                    logger.warn("[Warning] building the model, the entity '" + e.getKey()
                            + "' is associated to an empty vector (only null values)... because it has not been processed in the given corpora... "
                            + "This can lead to incoherent results performing some treatments.");
                    nonNullValues = 0;
                    warning++;
                }

                // here we retrieve the number of pair we will have in the compressed vector
                // i.e. [(1,0.4),(30,0.6),(5,0.7)...] refer to the doc
                byte[] compressed_vector_byte;

                if (row == null) {
                    compressed_vector_byte = BinarytUtils.toByteArray(new double[0]);
                }
                else{
                    compressed_vector_byte = CompressionUtils.toByteArray(row);
                }
                fo.write(compressed_vector_byte, 0, compressed_vector_byte.length);
                fo.write(sep);
            }

            if (warning != 0) {
                logger.info(warning + " warnings (null vectors)");
            }
            logger.info("Model built at " + model.path);

        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

}
