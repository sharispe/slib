/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * Normalized = distance(a,b) / (b.length() > a.length() ? b.length() : a.length());
 * i.e. distance divided by the length of the longer String
 * 
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
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
