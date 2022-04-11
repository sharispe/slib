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

import com.github.sharispe.slib.dsm.core.model.utils.modelconf.GConstants;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class VocStatConf {

    static Logger logger = LoggerFactory.getLogger(VocStatConf.class);

    public final String dir_analyzed;
    public final String original_voc_file;
    public final int nb_files;
    public final String path;

    public VocStatConf(String path, String dir_analyzed, String original_voc_file, int nb_files) {
        this.path = path;
        this.dir_analyzed = dir_analyzed;
        this.original_voc_file = original_voc_file;
        this.nb_files = nb_files;
    }

    public long getNBFiles() {
        return nb_files;
    }

    public String getVocIndex() {
        return path + GConstants.SEP + GConstants.STAT_VOC_FILE;
    }

    public String getVocProperties() {
        return path + GConstants.SEP + GConstants.STAT_PROPERTIES_FILE;
    }

    public String getVocUsageFile() {
        return path + GConstants.SEP + GConstants.STAT_USAGE_FILE;
    }

    /**
     * Load a vocabulary stat configuration from a model repository
     *
     * @param dir the directory of the statistics
     * @return the statistics
     * @throws slib.utils.ex.SLIB_Ex_Critic if an error occurs
     */
    public static VocStatConf load(String dir) throws SLIB_Ex_Critic {

        String model_properties = dir + GConstants.SEP + GConstants.STAT_PROPERTIES_FILE;
        logger.info("Loading voc stat configuration from: " + model_properties);

        Properties prop = new Properties();

        try {
            prop.load(new FileReader(model_properties));
            String dir_analyzed = prop.getProperty(GConstants.STAT_PARAM_ANALYZED_DIR);
            String original_voc_file = prop.getProperty(GConstants.STAT_PARAM_ORIGINAL_VOC_FILE);
            int nb_files = Integer.parseInt(prop.getProperty(GConstants.MODEL_PARAM_NB_FILES));

            return new VocStatConf(dir, dir_analyzed, original_voc_file, nb_files);

        } catch (IOException | NumberFormatException ex) {
            throw new SLIB_Ex_Critic("Error reading property file: " + ex.getMessage());
        }
    }

}
