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
package slib.utils.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class used to represent a Matrix which can be used to store values associated
 * to pairs of elements.
 *
 * @param <C> Object to index Columns
 * @param <R> Object to index Rows
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class MatrixDouble<C, R> {

    private Map<C, Integer> columnIndex;
    private Map<R, Integer> rowIndex;
    private Double[][] matrix;
    private int columns_number;
    private int rows_number;

    /**
     * Create a matrix filled with null values considering the given indexes.
     *
     * @param columResources the columns
     * @param rowResources the rows
     */
    public MatrixDouble(Set<C> columResources, Set<R> rowResources) {
        init(columResources, rowResources);
    }

    /**
     * Create a matrix filled with null values considering the given indexes
     *
     * @param columResources
     * @param rowResources
     * @param initValue default value
     */
    public MatrixDouble(Set<C> columResources, Set<R> rowResources, Double initValue) {
        init(columResources, rowResources);

        if (initValue != null) {
            for (Double[] mat : matrix) {
                for (int j = 0; j < mat.length; j++) {
                    mat[j] = initValue;
                }
            }
        }
    }

    private void init(Set<C> columResources, Set<R> rowResources) {
        columns_number = columResources.size();
        rows_number = rowResources.size();

        columnIndex = new HashMap<C, Integer>(columns_number);
        rowIndex = new HashMap<R, Integer>(rows_number);
        matrix = new Double[columns_number][rows_number];

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
    }

    /**
     * Return the column associated to the given element
     *
     * @param r the element
     * @return a tab corresponding to the values associated to the element
     */
    public Double[] getColumn(C r) {
        if (isInColumnIndex(r)) {
            return matrix[columnIndex.get(r)];
        }
        return null;
    }

    /**
     * Return the row associated to the given element
     *
     * @param r
     * @return the row associated to the given element
     */
    public Double[] getRow(R r) {

        if (!isInRowIndex(r)) {
            return null;
        }

        Double[] row = new Double[columns_number];
        int row_resource_id = rowIndex.get(r);

        for (int j = 0; j < columns_number; j++) {
            row[j] = matrix[j][row_resource_id];
        }
        return row;
    }

    /**
     *
     * @param colResource
     * @param rowResource
     * @return the value associated to the column and row resources. null if no
     * value exists.
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
     * @return the value associated to the column and row resources.
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
     * @return true if the given resource is in the row index.
     */
    public boolean isInRowIndex(R r) {

        return rowIndex.keySet().contains(r);
    }

    /**
     *
     * @param r
     * @return true if the given resource is in the column index.
     */
    public boolean isInColumnIndex(C r) {
        return columnIndex.keySet().contains(r);
    }

    /**
     *
     * @return the number of column.
     */
    public int getNbColumns() {
        return columns_number;
    }

    /**
     *
     * @return the number of row.
     */
    public int getNbRows() {
        return rows_number;
    }

    /**
     *
     * @return true if the matrix is square
     */
    public boolean isSquare() {
        return rows_number == columns_number;
    }

    /**
     * @return the maximal value stored in the matrix
     */
    public Double getMax() {
        Double max = null;

        for (Double[] mat : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                if (mat[j] != null && (max == null || mat[j] > max)) {
                    max = mat[j];
                }
            }
        }
        return max;
    }

    /**
     * @param v 
     * @return the maximal value stored in the column of the given resource
     * @throws IllegalArgumentException if the given value cannot be associated
     * to a column
     */
   
    public Double getMaxColumn(C v) {

        if (!isInColumnIndex(v)) {
            throw new IllegalArgumentException("Unable to locate " + v + "in the column index");
        }

        Double[] columnScore = getColumn(v);
        Double max = null;
        for (int i = 0; i < rows_number; i++) {
            if (columnScore[i] != null && (max == null || max < columnScore[i])) {
                max = columnScore[i];
            }
        }
        return max;
    }

    /**
     * @param v
     * @return the maximal value stored in the row of the given resource
     * @throws IllegalArgumentException if the given value cannot be associated
     * to a row
     */
    public Double getMaxRow(R v) {

        if (!isInRowIndex(v)) {
            throw new IllegalArgumentException("Unable to locate " + v + "in the row index");
        }

        Double[] rowScore = getRow(v);
        Double max = null;
        for (int i = 0; i < columns_number; i++) {
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

        for (Double[] row : matrix) {
            for (Double v : row) {
                if (v != null && (min == null || v < min)) {
                    min = v;
                }
            }
        }
        return min;
    }

    /**
     * Return the average of contained valued. Null values are excluded.
     *
     * @return null if the matrix is only composed of null value.
     */
    public Double getAverage() {
        Double sum = 0.;
        double count = 0.;
        for (Double[] row : matrix) {
            for (Double v : row) {
                if (v != null) {
                    sum += v;
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
                    throw new RuntimeException("Ooops an error occur in Matrix class");// no problem
                }
            }
            out += "\n";
        }
        out += "\n";

        return out;
    }

    public Set<C> getColumnElements() {
        return columnIndex.keySet();
    }

    public Set<R> getRowElements() {
        return rowIndex.keySet();
    }

    /**
     * Compute the sum of the values contained in the matrix. null cells are
     * skipped. If the matrix only contains null values, sum is equal to null.
     *
     * @return the sum of the values contained in the matrix.
     */
    public Double getSum() {
        double sum = 0;
        boolean touched = false;
        for (Double[] row : matrix) {
            for (Double v : row) {
                if (v != null) {
                    sum += v;
                    touched = true;
                }
            }
        }
        if (touched) {
            return sum;
        } else {
            return null;
        }
    }
}
