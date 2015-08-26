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
package com.github.sharispe.slib.dsm.core.model.utils.compression;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import com.github.sharispe.slib.dsm.utils.BinarytUtils;
import java.util.Arrays;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class CompressionUtils {

    /**
     * Compress the given double array (refer to the documentation for the
     * format of compressed array).
     *
     * @param arr
     * @return
     */
    public static double[] compressDoubleArray(double[] arr) {
        
        // count the number of non null values
        int nonnull = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                nonnull++;
            }
        }
        
        // we fill the vector
        double[] r = new double[nonnull*2];
        int c = 0;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] != 0) {
                r[c] = i;
                r[c+1] = arr[i];
                c+=2;
            }
        }
        return r;
    }

    /**
     * Uncompress the given compressed double array (refer to the documentation
     * for the format of compressed array).
     *
     * @param arr
     * @param size
     * @return
     * @throws slib.utils.ex.SLIB_Ex_Critic
     */
    public static double[] uncompressDoubleArray(double[] arr, int size) throws SLIB_Ex_Critic {
        

        double[] u = new double[size];

        for (int i = 0; i < arr.length; i += 2) {

            if (arr[i] < 0 || arr[i] >= size) {
                throw new SLIB_Ex_Critic("Critical error, the index is corrupted (according to the specified size:" + size + "): entry specified in cell "+i+" is not valid id=" + arr[i] + " val=" + arr[i + 1] + "\n" + Arrays.toString(arr));
            }
            u[(int) arr[i]] = arr[i + 1];
        }
        return u;
    }

    /**
     * Convert the given compressed double array into a map (refer to the
     * documentation for the format of compressed array). Each map entry
     * provides key=id, value=associated value such as array[id] = value.
     *
     * @param arr
     * @return
     */
    public static Map<Integer, Double> compressedDoubleArrayToMap(double[] arr) {

        Map<Integer, Double> map = new HashMap(arr.length / 2);

        for (int i = 0; i < arr.length; i += 2) {
            map.put((int) arr[i], arr[i + 1]);
        }
        return map;
    }

    /**
     * Convert the given map to an array of the given size Values which are not
     * specified are filled to zero. Each map entry is expected to provide
     * key=id, value=associated value such as array[id] = value.
     *
     * @param sparserow
     * @param sizeVector
     * @return
     * @throws slib.utils.ex.SLIB_Ex_Critic
     */
    public static double[] toArray(Map<Integer, Double> sparserow, int sizeVector) throws SLIB_Ex_Critic {

        double[] vec = new double[sizeVector];
        double k;
        for (Map.Entry<Integer, Double> e : sparserow.entrySet()) {

            k = e.getKey();
            if (k >= sizeVector || k < 0) {
                throw new SLIB_Ex_Critic("The index seems corrupted (according to the specified size:" + sizeVector + "): it contains an entry id=" + e.getKey() + " val=" + e.getValue() + "\nArray: " + sparserow);

            }
            vec[e.getKey()] = e.getValue();
        }
        return vec;
    }

    /**
     * Convert an array of double values into an array of byte
     *
     * @param doubleMap
     * @return the corresponding array of byte
     */
    public static byte[] toByteArray(Map<Integer, Double> doubleMap) {
        byte[] bytes = new byte[doubleMap.size() * 2 * BinarytUtils.BYTE_PER_DOUBLE];
        int i = 0;
        for (Map.Entry<Integer, Double> e : doubleMap.entrySet()) {
            ByteBuffer.wrap(bytes, i * BinarytUtils.BYTE_PER_DOUBLE, BinarytUtils.BYTE_PER_DOUBLE).putDouble(e.getKey());
            ByteBuffer.wrap(bytes, (i + 1) * BinarytUtils.BYTE_PER_DOUBLE, BinarytUtils.BYTE_PER_DOUBLE).putDouble(e.getValue());
            i += 2;
        }

        return bytes;
    }

    /**
     * Convert an array of double values into an array of byte
     *
     * @param array
     * @return the corresponding array of byte
     */
    public static byte[] toByteArray(double[] array) {
        
        byte[] bytes = new byte[array.length * BinarytUtils.BYTE_PER_DOUBLE];
        
        for (int i = 0; i < array.length; i ++) {
            ByteBuffer.wrap(bytes, i * BinarytUtils.BYTE_PER_DOUBLE, BinarytUtils.BYTE_PER_DOUBLE).putDouble(array[i]);
        }
        return bytes;
    }
    
    public static void main(String[] a) throws SLIB_Ex_Critic{
        
        Map<Integer,Double> m = new HashMap();
        m.put(0, 0.1);
        m.put(1, 3.0);
        System.out.println(Arrays.toString(toByteArray(m)));
        
        double[] t = {0,0.1,1,3.0};
        System.out.println(Arrays.toString(toByteArray(t)));
        
    }
}
