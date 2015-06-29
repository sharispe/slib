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

import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrix;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConfUtils;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import com.github.sharispe.slib.dsm.utils.XPUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.io.FileUtils;


import slib.utils.ex.SLIB_Exception;

/**
 *
 * Distributional Model Engine. Class used to build distributional models
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class DMEngine {

    public static void build_distributional_model_TERM_TO_TERM(Collection<File> files, Voc vocIndex, ModelConf model, int nbThreads) throws SLIB_Exception, IOException {

        CoOcurrenceEngine engine = new CoOcurrenceEngine(vocIndex);
        SparseMatrix wordCoocurences = engine.computeCoOcurrence(files, nbThreads);
        build_distributional_model_TERM_TO_TERM(vocIndex, wordCoocurences, model);
    }

    public static void build_distributional_model_TERM_TO_TERM(Voc vocIndex, SparseMatrix matrix, ModelConf model) throws SLIB_Exception, IOException {

        ModelConfUtils.initModel(model);
        ModelConfUtils.buildIndex(model, vocIndex.getIndex(), matrix);

        // We flush the index for entities and the dimensions
        XPUtils.flushMAP(vocIndex.getIndex(), model.getEntityIndex());
        FileUtils.copyFile(new File(model.getEntityIndex()), new File(model.getDimensionIndex()));
        
        
        ModelConfUtils.buildModelBinary(model, vocIndex.getIndex(), matrix);
    }
}
