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

/**
 *
 * General constants All constant are kept here for easy access
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GConstants {

    // ----------------------------------------------------------
    // General constants
    // ----------------------------------------------------------
    public static final String SEP = System.getProperty("file.separator");

    // ----------------------------------------------------------
    // Statistics 
    // ----------------------------------------------------------
    public static final String STAT_PROPERTIES_FILE = "stats.properties";
    public static final String STAT_VOC_FILE = "vocabulary.tsv";
    public static final String STAT_USAGE_FILE = "usage.tsv";

    public static final String STAT_PARAM_NB_FILES = "nb_files";
    public static final String STAT_PARAM_ORIGINAL_VOC_FILE = "original_voc_file";
    public static final String STAT_PARAM_ANALYZED_DIR = "original_stat_dir";

    // ----------------------------------------------------------
    // Model 
    // ----------------------------------------------------------
    public static final String STORAGE_FORMAT_VERSION = "0.0";
    public static final int STORAGE_FORMAT_SEPARATOR_SIZE = 1;// byte
    public static final byte STORAGE_FORMAT_SEPARATOR_VALUE = 0;

    public static final String MODEL_PROPERTIES_FILE = "model.properties";
    public static final String MODEL_INDEX_FILE = "model_index_table.tsv";
    public static final String MODEL_BINARY_FILE = "model.bat";
    public static final String MODEL_ENTITY_INDEX_FILE = "entity_index.tsv";
    public static final String MODEL_DIMENSION_INDEX_FILE = "dimension_index.tsv";

    public static final String MODEL_PARAM_TYPE = "type";
    public static final String MODEL_PARAM_NAME = "name";
    public static final String MODEL_PARAM_ENTITY_SIZE = "entities_size";
    public static final String MODEL_PARAM_VEC_SIZE = "vec_size";
    public static final String MODEL_PARAM_VERSION = "version";
    public static final String MODEL_PARAM_NB_FILES = "nb_files";

}
