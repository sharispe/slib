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

import com.github.sharispe.slib.dsm.core.engine.VocStatConf;
import com.github.sharispe.slib.dsm.utils.FileUtility;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * Utility class used to build a distributional model
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class ModelConfUtils {

    static Logger logger = LoggerFactory.getLogger(ModelConfUtils.class);

    /**
     * Initialize a model according to the given configuration.
     *
     * @param model the model configuration
     * @throws SLIB_Ex_Critic if an error occurs
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
     * @param conf the configuration
     * @throws SLIB_Ex_Critic if an error occurs
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
     * @param conf the configuration
     * @throws SLIB_Ex_Critic if an error occurs
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


}
