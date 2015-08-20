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
 * Based on Levenshtein distance 
 * i.e. normalized =  1 - norm(Levenshtein)
 *  not normalized =  length(longest string) - distance
 * 
 * @see LevenshteinDistance
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
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
