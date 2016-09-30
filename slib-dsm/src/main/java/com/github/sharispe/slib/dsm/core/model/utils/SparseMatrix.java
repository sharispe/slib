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
package com.github.sharispe.slib.dsm.core.model.utils;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Simple sparse matrix representation used to store a distributional model into
 * memory
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class SparseMatrix {

    static Logger logger = LoggerFactory.getLogger(SparseMatrix.class);
    MapGenerator<Integer, Double> mgenerator;
    int nbElements;
    int nbDimensions;
    Map<Integer, Map<Integer, Double>> m;
    int storedValue;

    /**
     *
     * @return the set of identifiers referring to rows with at least one
     * non-null value
     */
    public Set<Integer> getElementIDs() {
        return m.keySet();
    }

    /**
     *
     * @param id the id of the dimension
     * @return the vector associated to a row as map key value for each
     * dimension not associated to a null value.
     */
    public Map<Integer, Double> getDimensionValuesForElement(int id) {
        return m.containsKey(id) ? m.get(id) : null;
    }

    protected SparseMatrix(int nbElements, int nbDimensions, MapGenerator<Integer, Double> mgenerator) {
        this.nbElements = nbElements;
        this.nbDimensions = nbDimensions;
        this.mgenerator = mgenerator;
        m = mgenerator.newMapOfMap();
        logger.info("Create matrix: row:" + nbElements + "\tcol:" + nbDimensions);
    }

    public void set(int elementId, int dimensionID, double value) {
        if (value == 0) {
            return;
        }
        if (!m.containsKey(elementId)) {
            m.put(elementId, mgenerator.newMap());
            storedValue++;
        } else if (!m.get(elementId).containsKey(dimensionID)) {
            storedValue++;
        }
        m.get(elementId).put(dimensionID, value);
    }

    /**
     * Add the given value to the current matrix cell
     *
     * @param elementId element id
     * @param dimensionID dimension id
     * @param toadd the value to add
     */
    public void add(int elementId, int dimensionID, double toadd) {
        if (toadd == 0) { // we don't want to store 0 values
            return;
        }
        double cval = 0;
        if (!m.containsKey(elementId)) {
            m.put(elementId, mgenerator.newMap());
            storedValue++;
        } else if (!m.get(elementId).containsKey(dimensionID)) {
            storedValue++;
        } else {
            cval = m.get(elementId).get(dimensionID);
        }
        m.get(elementId).put(dimensionID, cval + toadd);
    }

    public double get(int elementId, int dimensionID) {
        if (!m.containsKey(elementId)) {
            return 0;
        } else if (!m.get(elementId).containsKey(dimensionID)) {
            return 0;
        }
        return m.get(elementId).get(dimensionID);
    }

    /**
     * row of the matrix
     *
     * @param elementId the id of the element for which we xant the vector (id of the row)
     * @return the vector associated to that row
     */
    public double[] getElementVector(int elementId) {
        double[] v = new double[nbDimensions];

        for (int i = 0; i < nbDimensions; i++) {
            v[i] = get(elementId, i);
        }
        return v;
    }

    /**
     * also return 0 if the element does not exist
     *
     * @param elementID the element id (row id)
     * @return the number of non null values
     */
    public int getNbNonNullValuesInElementVector(int elementID) {

        if (!m.containsKey(elementID)) {
            return 0;
        }
        // For this to be correct the matrix must never store 0 values
        return m.get(elementID).size();
    }

    /**
     * column of the matrix
     *
     * @param dimId dimension id (column id)
     * @return the column vector associated to that dimension
     */
    public double[] getDimensionVector(int dimId) {

        double[] v = new double[nbElements];

        for (int i = 0; i < nbElements; i++) {
            v[i] = get(i, dimId);
        }
        return v;
    }

    public void add(SparseMatrix s) {

        logger.info("Start adding " + s.toString());
        Map<Integer, Double> cmap;
        Integer kk;
        double cval;

        for (Integer k : s.getElementIDs()) {
            cmap = m.get(k);
            if (cmap == null) {
                cmap = mgenerator.newMap();
                m.put(k, cmap);
            }

            for (Entry<Integer, Double> e : s.getDimensionValuesForElement(k).entrySet()) {

                kk = e.getKey();

                cval = cmap.containsKey(kk) ? cmap.get(kk) : 0;
                cval += e.getValue();

                if (cval != 0) {
                    cmap.put(kk, cval);
                }
            }
        }
        logger.info("Finish adding " + s.toString());
    }

    @Override
    public String toString() {
        return "matrix elements: " + nbElements + "\tdimensions: " + nbDimensions + "\t" + hashCode();
    }

    public static void main(String[] args) {
        SparseMatrix m = SparseMatrixGenerator.buildSparseMatrix(5, 10);
        m.set(0, 0, 10);
        m.add(0, 0, 1);
        System.out.println(m.get(0, 0));
        System.out.println(m.get(4, 9));
    }

    /**
     * @return the number of values that have been stored
     */
    public int storedValues() {
        return storedValue;
    }

    /**
     * Empty the matrix
     */
    public void clear() {
        m.clear();
        storedValue=0;
    }

}
