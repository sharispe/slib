/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.sml.sm.core.measures.string;

/**
 * Based on Levenshtein distance 
 * i.e. normalized =  1 - norm(Levenshtein)
 *  not normalized =  length(longest string) - distance
 * 
 * @see LevenshteinDistance
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class LevenshteinSimilarity extends LevenshteinDistance{
    
    /**
     *
     * @param normalize
     */
    public LevenshteinSimilarity(boolean normalize){
        super(normalize);
    }
    
    /**
     *
     * @param insP
     * @param delP
     * @param mismatchP
     * @param normalize
     */
    public LevenshteinSimilarity(double insP, double delP, double mismatchP, boolean normalize){
        super(insP, delP, mismatchP, normalize);
    }
    
    /**
     *
     * @param a
     * @param b
     * @return the similarity
     */
    public double sim(String a, String b){
        
        if(this.norm) {
            return 1 - distance(a, b);
        }
        else { // 
            return (b.length() > a.length() ? b.length() : a.length()) - distance(a, b);
        }
        
    }
}
