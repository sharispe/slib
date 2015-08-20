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

import com.github.sharispe.slib.dsm.core.model.access.ModelAccessorUtils;
import com.github.sharispe.slib.dsm.core.model.utils.IndexedVector;
import com.github.sharispe.slib.dsm.core.model.utils.compression.CompressionUtils;
import com.github.sharispe.slib.dsm.core.model.utils.IndexedVectorInfo;
import com.github.sharispe.slib.dsm.core.model.utils.modelconf.ModelConf;
import com.github.sharispe.slib.dsm.utils.BinarytUtils;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.Map;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to access information which is stored into the model This class
 * enable fast access of the vectors but stores them into a matrix
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class ModelAccessorFullMemory_2D extends ModelAccessor_2D {

    IndexedVector[] vectors;

    public ModelAccessorFullMemory_2D(ModelConf model) throws SLIB_Ex_Critic {
        super(model);
        loadModel();
    }

    @Override
    public Iterator<IndexedVector> iterator() {
        return new ModelIterator();
    }

    public class ModelIterator implements Iterator<IndexedVector> {

        int c = 0;

        @Override
        public boolean hasNext() {
            return c < vectors.length;
        }

        @Override
        public IndexedVector next() {
            IndexedVector v = vectors[c];
            c++;
            return v;
        }

    }

    @Override
    public IndexedVector vectorRepresentationOf(IndexedVectorInfo vectorInfo) throws SLIB_Ex_Critic {
        return vectors[vectorInfo.id];
    }

    private void loadModel() throws SLIB_Ex_Critic {

        logger.info("Loading model into memory");

        Map<Integer, IndexedVectorInfo> index = ModelAccessorUtils.loadIndex_2D_MODEL(model);

        vectors = new IndexedVector[model.vec_size];

        try {

            FileInputStream fis = new FileInputStream(model.getModelBinary());
            FileChannel fchannel = fis.getChannel();

            int c = 1;
            for (IndexedVectorInfo einfo : index.values()) {

                logger.info("..." + c + "/" + index.size());
                c++;
                int size_vector_compressed = einfo.length_double_non_null * 2;
                // refer to persistant model accessor for details
                double[] vector_compressed = new double[size_vector_compressed];
                long start = einfo.start_pos;
                long sizeBufferInByte = size_vector_compressed * BinarytUtils.BYTE_PER_DOUBLE;
                ByteBuffer bbf = ByteBuffer.allocateDirect((int) sizeBufferInByte);
                fchannel.position(start);
                fchannel.read(bbf);
                bbf.position(0);
                DoubleBuffer dbf = bbf.asDoubleBuffer();
                dbf.get(vector_compressed);
                vectors[einfo.id] = new IndexedVector(einfo.id, einfo.label, CompressionUtils.uncompressDoubleArray(vector_compressed, model.vec_size));
            }
        } catch (IOException e) {
            throw new SLIB_Ex_Critic("Error reading the index: " + e.getMessage());
        }

        logger.info("model loaded");

    }
}
