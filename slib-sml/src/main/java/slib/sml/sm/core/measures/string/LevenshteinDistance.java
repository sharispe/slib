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
package slib.sml.sm.core.measures.string;

/**
 * Basic implementation of Levenshtein Distance
 * http://en.wikipedia.org/wiki/Levenshtein_distance
 * 
 * Levenshtein VI (1966). 
 * "Binary codes capable of correcting deletions, insertions, and reversals". 
 * Soviet Physics Doklady 10: 707–10.
 * 
 * Normalized = distance(a,b) / (b.length() greater than a.length() ? b.length() : a.length());
 * i.e. distance divided by the length of the longer String
 * 
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class LevenshteinDistance {

    double[][] mat;
    double ins = 1;
    double del = 1;
    double mismatch = 1;
    boolean norm = false;
    String a;
    String b;
    
    /**
     *
     * @param normalize
     */
    public LevenshteinDistance(boolean normalize){
        this.norm = normalize;
    }
    
    /**
     *
     * @param insP
     * @param delP
     * @param mismatchP
     * @param normalize
     */
    public LevenshteinDistance(double insP, double delP, double mismatchP, boolean normalize){
        this.ins = insP;
        this.del = delP;
        this.mismatch = mismatchP;
        this.norm = normalize;
    }

    /**
     *
     * @param a
     * @param b
     * @return the distance
     */
    public double distance(String a, String b) {

        this.a = a;
        this.b = b;

        double dist;

        if (a.length() == 0) {
            dist = b.length();
        } else if (b.length() == 0) {
            dist = a.length();
        } else {
            dist = computeDist(a, b);
        }

        if(norm) {
            dist = dist / (b.length() > a.length() ? b.length() : a.length());
        }
        return dist;
    }

    private double computeDist(String a, String b) {


        mat = new double[b.length()][a.length()];
        mat[0][0] = a.charAt(0) == b.charAt(0) ? 0 : mismatch;

        // init matrix
        for (int i = 1; i < a.length(); i++) {
            mat[0][i] = mat[0][i - 1] + ins;
        }
        for (int i = 1; i < b.length(); i++) {
            mat[i][0] = mat[i - 1][0] + ins;
        }


        for (int i = 1; i < a.length(); i++) {
            for (int j = 1; j < b.length(); j++) {

                mat[j][i] = getMin(i, j);

            }
        }

        return mat[b.length() - 1][a.length() - 1];
    }

    private double getMin(int i, int j) {

        double insert = mat[j - 1][i] + ins;
        double delet = mat[j][i - 1] + del;



        double mism = mat[j - 1][i - 1] + (b.charAt(j) == a.charAt(i) ? 0 : mismatch);

        double score;
        if (insert <= delet) {
            score = insert < mism ? insert : mism;
        } else {
            score = delet < mism ? delet : mism;
        }
        return score;
    }
}
