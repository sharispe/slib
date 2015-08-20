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

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to store the configuration of a model
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class ModelConf {

    static Logger logger = LoggerFactory.getLogger(ModelConf.class);

    final ModelType type;
    public final String name;
    public final String path;
    public final int entity_size;
    public final int vec_size;
    public final String format_version;
    public final long nb_files;

    public ModelConf(ModelType type, String name, String path, int entity_size, int vec_size, long nb_files, String format_version) {
        this.type = type;
        this.name = name;
        this.path = path;
        this.entity_size = entity_size;
        this.vec_size = vec_size;
        this.nb_files = nb_files;
        this.format_version = format_version;
    }

    public long getNBFiles() {
        return nb_files;
    }

    public String getModelIndex() {
        return path + GConstants.SEP + GConstants.MODEL_INDEX_FILE;
    }

    public String getModelProperties() {
        return path + GConstants.SEP + GConstants.MODEL_PROPERTIES_FILE;
    }

    public String getModelBinary() {
        return path + GConstants.SEP + GConstants.MODEL_BINARY_FILE;
    }

    public String getEntityIndex() {
        return path + GConstants.SEP + GConstants.MODEL_ENTITY_INDEX_FILE;
    }

    public String getDimensionIndex() {
        return path + GConstants.SEP + GConstants.MODEL_DIMENSION_INDEX_FILE;
    }

    /**
     * Load a model configuration from a model repository
     *
     * @param dir
     * @return
     * @throws slib.utils.ex.SLIB_Ex_Critic
     */
    public static ModelConf load(String dir) throws SLIB_Ex_Critic {

        String model_properties = dir + GConstants.SEP + GConstants.MODEL_PROPERTIES_FILE;
        logger.info("Loading model configuration from: " + model_properties);

        Properties prop = new Properties();

        try {
            prop.load(new FileReader(model_properties));
            ModelType type = ModelType.valueOf(prop.getProperty(GConstants.MODEL_PARAM_TYPE));
            String name = prop.getProperty(GConstants.MODEL_PARAM_NAME);
            int entities_size = Integer.parseInt(prop.getProperty(GConstants.MODEL_PARAM_ENTITY_SIZE));
            int vec_size = Integer.parseInt(prop.getProperty(GConstants.MODEL_PARAM_VEC_SIZE));
            String version = prop.getProperty(GConstants.MODEL_PARAM_VERSION);
            long nb_files = Long.parseLong(prop.getProperty(GConstants.MODEL_PARAM_NB_FILES));

            return new ModelConf(type, name, dir, entities_size, vec_size, nb_files, version);

        } catch (IOException | NumberFormatException ex) {
            throw new SLIB_Ex_Critic("Error reading property file: " + ex.getMessage());
        }

    }
}
