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
import java.util.Map;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to access information which is stored into the model
 *
 * TODO Do not open a new file channel each time you load a vector.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ModelAccessorPersistance_2D extends ModelAccessor_2D {


    FileInputStream fis;
    FileChannel fchannel;

    public ModelAccessorPersistance_2D(ModelConf c) throws SLIB_Ex_Critic {
        super(c);
    }

    public ModelAccessorPersistance_2D(ModelConf c, Map<Integer, EntityInfo_2D_MODEL> index) throws SLIB_Ex_Critic {
        super(c,index);
    }

    private void open() throws IOException {
        if (fis == null) {
            logger.info("Creating channel for " + model.getModelBinary());
            fis = new FileInputStream(model.getModelBinary());
            fchannel = fis.getChannel();
        }
    }

    public void close() throws IOException {
        if (fis != null) {
            fis.close();
        }
    }

    @Override
    public double[] vectorRepresentationOf(int id_vector) throws SLIB_Ex_Critic {

        if (!index.containsKey(id_vector)) {
            throw new SLIB_Ex_Critic("Does not contain a vector representation for id=" + id_vector);
        }

        int nb_val_vector = index.get(id_vector).length_double_non_null;
        // we prepare the vector which will retrieve the compressed version of the vector
        double[] vector = new double[nb_val_vector * 2];

        try {

            open();

            // we retrieve the starting position of the vector in the file
            long start = index.get(id_vector).start_pos;
            // we compute the size of the array of bytes which will contain the double values in bytes
            long sizeBufferInByte = nb_val_vector * 2 * BinarytUtils.BYTE_PER_DOUBLE;

            // we load the channel
            // We build a ByteBuffer which will contain the corresponding vector in byte values
            ByteBuffer bbf = ByteBuffer.allocateDirect((int) sizeBufferInByte);
            fchannel.position(start); // we locate the channel to the begining
            fchannel.read(bbf); // we fill our buffer, i.e. we wite into our buffer
            bbf.position(0); // we set the buffer position to 0 to read it from the beginning of what we just retrieved
            // we convert the buffer into a double buffer for convenience
            DoubleBuffer dbf = bbf.asDoubleBuffer();
            dbf.get(vector); // we read the buffer to fill the vector 

        } catch (IOException e) {
            throw new SLIB_Ex_Critic("Error reading the index: " + e.getMessage());
        }
        double[] uncompressed_vector = CompressionUtils.uncompressDoubleArray(vector, model.vec_size);

        return uncompressed_vector;
    }
}
