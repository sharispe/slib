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
package com.github.sharispe.slib.dsm.core.model.access.twodmodels;

import com.github.sharispe.slib.dsm.core.model.utils.compression.CompressionUtils;
import com.github.sharispe.slib.dsm.core.model.utils.entityinfo.EntityInfo_2D_MODEL;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import com.github.sharispe.slib.dsm.utils.BinarytUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to access information which is stored into the model This class
 * enable fast access of the vectors but stores them into a matrix
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ModelAccessorMemory_2D extends ModelAccessor_2D {

    Map<Integer, Map<Integer, Double>> mat;

    public ModelAccessorMemory_2D(ModelConf model) throws SLIB_Ex_Critic {
        super(model);
        loadModel();
    }

    public ModelAccessorMemory_2D(ModelConf model, Map<Integer, EntityInfo_2D_MODEL> index) throws SLIB_Ex_Critic {
        super(model, index);
        loadModel();
    }

    @Override
    public double[] vectorRepresentationOf(int id_vector) throws SLIB_Ex_Critic {
        if (!mat.containsKey(id_vector)) {
            throw new SLIB_Ex_Critic("Does not contain a vector representation for id=" + id_vector);
        }
        return CompressionUtils.toArray(mat.get(id_vector), model.vec_size);
    }


    private void loadModel() throws SLIB_Ex_Critic {

        logger.info("Loading model into memory (compressed)");

        int size_entity = model.entity_size;

        mat = new HashMap();

        try {

            FileInputStream fis = new FileInputStream(model.getModelBinary());
            FileChannel fchannel = fis.getChannel();

            int c = 1;
            for (EntityInfo_2D_MODEL einfo : index.values()) {

                if (c % 10000 == 0) {
                    logger.info("..." + c + "/" + size_entity);
                }
                c++;

                int size_compressed_vector = einfo.length_double_non_null * 2;
                // refer to persistant model accessor for details
                double[] vector_compressed = new double[size_compressed_vector];
                long start = einfo.start_pos;
                long sizeBufferInByte = size_compressed_vector * BinarytUtils.BYTE_PER_DOUBLE;
                ByteBuffer bbf = ByteBuffer.allocateDirect((int) sizeBufferInByte);
                fchannel.position(start);
                fchannel.read(bbf);
                bbf.position(0);
                DoubleBuffer dbf = bbf.asDoubleBuffer();
                dbf.get(vector_compressed);

                mat.put(einfo.id, CompressionUtils.compressedDoubleArrayToMap(vector_compressed));
            }
        } catch (IOException e) {
            throw new SLIB_Ex_Critic("Error reading the index: " + e.getMessage());
        }

        logger.info("model loaded");

    }

    /**
     * Free the memory allocated to the representation of the given entity. Note
     * that the underlying model (associated to this accessor) will not be
     * modified.
     *
     * @param id
     */
    public void clearRepresentationOf(int id) {
        mat.remove(id);
    }

    /**
     * @param id
     * @return the compressed representation of the given identifier
     */
    public Map<Integer, Double> getCompressedRepresentation(int id) {
        return mat.get(id);
    }
}
