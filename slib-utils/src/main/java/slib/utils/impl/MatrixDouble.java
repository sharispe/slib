/*

 Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

 This software is a computer program whose purpose is to 
 process semantic graphs.

 This software is governed by the CeCILL  license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 */
package slib.utils.impl;

import java.util.HashMap;
import java.util.Set;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Matrix of Double object (Non Sparse matrix)
 *
 * @param <C> Object to index Columns
 * @param <R> Object to index Rows
 *
 * @author Sebastien Harispe
 */
public class MatrixDouble<C, R> {

    HashMap<C, Integer> columnIndex;
    HashMap<R, Integer> rowIndex;
    Double[][] matrix;

    /**
     * Create a matrix filled with null values considering the given indexes
     *
     * @param columResources
     * @param rowResources
     */
    public MatrixDouble(Set<C> columResources, Set<R> rowResources) {
        init(columResources, rowResources, null);
    }

    /**
     * Create a matrix filled with null values considering the given indexes
     *
     * @param columResources
     * @param rowResources
     * @param initValue default value
     */
    public MatrixDouble(Set<C> columResources, Set<R> rowResources, Double initValue) {
        init(columResources, rowResources, initValue);
    }

    private void init(Set<C> columResources, Set<R> rowResources, Double initValue) {
        columnIndex = new HashMap<C, Integer>(columResources.size());
        rowIndex = new HashMap<R, Integer>(rowResources.size());

        matrix = new Double[columResources.size()][rowResources.size()];

        int id = 0;

        for (C rc : columResources) {
            columnIndex.put(rc, id);
            id++;
        }

        id = 0;
        for (R rc : rowResources) {
            rowIndex.put(rc, id);
            id++;
        }

        if (initValue != null) {
            for (int i = 0; i < matrix.length; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    matrix[i][j] = initValue;
                }
            }
        }
    }

    /**
     *
     * @param r
     * @return
     */
    public Double[] getColumn(C r) {
        if (isInColumnIndex(r)) {
            return matrix[columnIndex.get(r)];
        }
        return null;
    }

    /**
     *
     * @param r
     * @return
     */
    public Double[] getRow(R r) {

        if (!isInRowIndex(r)) {
            return null;
        }

        Double[] row = new Double[columnIndex.keySet().size()];
        int i = 0;
        for (C c : columnIndex.keySet()) {
            try {
                row[i] = getValue(c, r);
            } catch (SLIB_Ex_Critic e) {
            }
        }

        return row;
    }

    /**
     *
     * @param colResource
     * @param rowResource
     * @return
     */
    public Double getValueCheckIndex(C colResource, R rowResource) {
        if (isInColumnIndex(colResource) && isInRowIndex(rowResource)) {
            return matrix[columnIndex.get(colResource)][rowIndex.get(rowResource)];
        }
        return null;
    }

    /**
     *
     * @param colResource
     * @param rowResource
     * @return
     * @throws SLIB_Ex_Critic
     */
    public Double getValue(C colResource, R rowResource) throws SLIB_Ex_Critic {
        try {
            return matrix[columnIndex.get(colResource)][rowIndex.get(rowResource)];
        } catch (Exception e) {
            throw new SLIB_Ex_Critic("Undefined index contains col index " + colResource + " " + isInColumnIndex(colResource) + "\ncontains row index " + rowResource + " " + isInRowIndex(rowResource) + " in matrix " + e.getMessage());
        }
    }

    /**
     *
     * @param colResource
     * @param rowResource
     * @param value
     */
    public void setValue(C colResource, R rowResource, Double value) {
        matrix[columnIndex.get(colResource)][rowIndex.get(rowResource)] = value;
    }

    /**
     *
     * @param r
     * @return
     */
    public boolean isInRowIndex(R r) {

        return rowIndex.keySet().contains(r);
    }

    /**
     *
     * @param r
     * @return
     */
    public boolean isInColumnIndex(C r) {
        return columnIndex.keySet().contains(r);
    }

    /**
     *
     * @return
     */
    public int getNbColumns() {
        return columnIndex.size();
    }

    /**
     *
     * @return
     */
    public int getNbRows() {
        return rowIndex.size();
    }

    /**
     *
     * @return
     */
    public boolean isSquare() {
        return columnIndex.size() == rowIndex.size();
    }

    /**
     *
     * @return
     */
    public Double getMax() {
        Double max = null;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {

                if (matrix[i][j] != null && (max == null || matrix[i][j] > max)) {
                    max = matrix[i][j];
                }
            }
        }
        return max;
    }

    /**
     *
     * @param v
     * @return
     * @throws SLIB_Ex_Critic
     */
    public Double getMaxColumn(C v) throws SLIB_Ex_Critic {

        if (!isInColumnIndex(v)) {
            throw new SLIB_Ex_Critic("Unable to locate " + v + "in column index");
        }

        Double[] columnScore = getColumn(v);
        Double max = null;
        for (int i = 0; i < columnScore.length; i++) {
            if (columnScore[i] != null && (max == null || max < columnScore[i])) {
                max = columnScore[i];
            }
        }
        return max;
    }

    /**
     *
     * @param v
     * @return
     * @throws SLIB_Ex_Critic
     */
    public Double getMaxRow(R v) throws SLIB_Ex_Critic {

        if (!isInRowIndex(v)) {
            throw new SLIB_Ex_Critic("Unable to locate " + v + "in row index");
        }

        Double[] rowScore = getRow(v);
        Double max = null;
        for (int i = 0; i < rowScore.length; i++) {
            if (rowScore[i] != null && (max == null || max < rowScore[i])) {
                max = rowScore[i];
            }
        }
        return max;
    }

    /**
     * Return the minimal value stored in the matrix
     *
     * @return null if the matrix is empty
     */
    public Double getMin() {

        Double min = null;

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {

                if (matrix[i][j] != null && (min == null || matrix[i][j] < min)) {
                    min = matrix[i][j];
                }
            }
        }
        return min;
    }

    /**
     * Return the average of contained valued i.e. sum non null values / number
     * of non null
     *
     * @return null if the matrix is only composed of null value.
     */
    public Double getAverage() {
        Double sum = 0.;
        double count = 0.;
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                if (matrix[i][j] != null) {
                    sum += matrix[i][j];
                    count++;
                }
            }
        }
        if (count == 0) {
            return null;
        }
        return sum / count;

    }

    /**
     * @return the underlying matrix
     */
    public Double[][] getMatrix() {
        return matrix;
    }

    @Override
    public String toString() {

        String out = "";
        for (C v : columnIndex.keySet()) {
            out += "\t" + v.toString();
        }

        out += "\n";

        for (R vj : rowIndex.keySet()) {

            out += vj.toString();

            for (C v : columnIndex.keySet()) {
                try {
                    out += "\t" + getValue(v, vj);
                } catch (SLIB_Ex_Critic e) {
                    e.printStackTrace();// no problem
                }
            }
            out += "\n";
        }
        out += "\n";

        return out;
    }
}
