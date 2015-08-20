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
package com.github.sharispe.slib.dsm.core.model.access;

import com.github.sharispe.slib.dsm.core.model.utils.IndexedVectorInfo;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class ModelAccessorUtils {

    static Logger logger = LoggerFactory.getLogger(ModelAccessorUtils.class);

    public static Map<Integer, IndexedVectorInfo> loadIndex_2D_MODEL(ModelConf model) throws SLIB_Ex_Critic {

        Map<Integer, IndexedVectorInfo> index = new HashMap();
        String index_path = model.getModelIndex();

        logger.info("Loading index from " + index_path);

        try (BufferedReader br = new BufferedReader(new FileReader(index_path))) {
            // skip header
            br.readLine();
            String line = br.readLine();

            while (line != null) {

                String[] data = line.split("\t");

//                if (data.length == 3) {
                    int id = Integer.parseInt(data[0]);
                    long start_pos = Long.parseLong(data[1]);
                    int length_double_non_null = Integer.parseInt(data[2]);

                    index.put(id, new IndexedVectorInfo(id, start_pos, length_double_non_null,data[3]));
//                }

                line = br.readLine();
            }
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        logger.info("Index loaded, size=" + index.size());
        return index;
    }
}
