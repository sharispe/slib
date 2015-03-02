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
package com.github.sharispe.slib.dsm.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class BinarytUtils {

    public final static int BYTE_PER_DOUBLE = Double.SIZE / Byte.SIZE;

    /**
     * Convert an array of double values into an array of byte
     *
     * @param doubleArray the array to convert
     * @return the corresponding array of byte
     */
    public static byte[] toByteArray(double[] doubleArray) {
        byte[] bytes = new byte[doubleArray.length * BYTE_PER_DOUBLE];
        for (int i = 0; i < doubleArray.length; i++) {
            ByteBuffer.wrap(bytes, i * BYTE_PER_DOUBLE, BYTE_PER_DOUBLE).putDouble(doubleArray[i]);
        }
        return bytes;
    }

    

    /**
     * Convert an array of byte into an array of double
     *
     * @param byteArray the array to convert
     * @return the corresponding array of double
     */
    public static double[] toDoubleArray(byte[] byteArray) {
        double[] doubles = new double[byteArray.length / BYTE_PER_DOUBLE];
        for (int i = 0; i < doubles.length; i++) {
            doubles[i] = ByteBuffer.wrap(byteArray, i * BYTE_PER_DOUBLE, BYTE_PER_DOUBLE).getDouble();
        }
        return doubles;
    }

    /**
     * Load the content of a file into a byte array
     *
     * @param aFileName
     * @return the content of the file as a byte array
     * @throws IOException
     */
    static byte[] fileToByteArray(String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        return Files.readAllBytes(path);
    }

    /**
     * Write an array of byte into a file
     *
     * @param aBytes
     * @param aFileName
     * @throws IOException
     */
    static void writeFile(byte[] aBytes, String aFileName) throws IOException {
        Path path = Paths.get(aFileName);
        Files.write(path, aBytes); //creates, overwrites
    }
}
