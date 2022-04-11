/*
 *  Copyright or � or Copr. Ecole des Mines d'Al�s (2012-2014) 
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
package com.github.sharispe.slib.dsm.core.model.access.twodmodels;

import com.github.sharispe.slib.dsm.core.model.utils.IndexedVectorInfo;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import static slib.graph.io.loader.GraphLoaderGeneric.logger;

/**
 *
 * @author S�bastien Harispe (sebastien.harispe@gmail.com)
 */
public class IndexedVectorInfoIterator implements Iterator<IndexedVectorInfo> {

    BufferedReader br;
    String line;

    public IndexedVectorInfoIterator(ModelConf model) throws IOException {

        br = new BufferedReader(new FileReader(model.getModelIndex()));
        line = br.readLine(); //skip header
        line = br.readLine();
    }

    @Override
    public boolean hasNext() {
        return line != null;
    }

    @Override
    public IndexedVectorInfo next() {
        try {
            if (line != null) {
                String[] word_data = Utils.tab_pattern.split(line);
                if (word_data.length != 4) {
                    line = br.readLine();
                    return next();
                }

                IndexedVectorInfo info = new IndexedVectorInfo(Integer.parseInt(word_data[0]), Long.parseLong(word_data[1]), Integer.parseInt(word_data[2]), word_data[3]);

                // try to close the file
                line = br.readLine();
                if (line == null) {
                    br.close();
                }
                return info;

            }
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        return null;
    }

}
