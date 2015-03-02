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
package com.github.sharispe.slib.dsm.main;

import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrix;
import com.github.sharispe.slib.dsm.utils.MapUtils;
import edu.stanford.nlp.util.ArrayUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SlidingWindowTest {

    final static Pattern blank_pattern = Pattern.compile("\\s+");
    SparseMatrix localWordCoocurences;
    String s = "In this respect, the user's attention is drawn to the risks associated\n"
            + "with loading,  using,  modifying and/or developing or reproducing the\n"
            + "software by the user in light of its specific status of free software,\n"
            + "that may mean  that it is complicated to manipulate,  and  that  also\n"
            + "therefore means  that it is reserved for developers  and  experienced\n"
            + "professionals having in-depth computer knowledge. Users are therefore\n"
            + "encouraged to load and test the software's suitability as regards their\n"
            + "requirements in conditions enabling the security of their systems and/or \n"
            + "data to be ensured and,  more generally, to use and operate it in the \n"
            + "same conditions as regards security.";
    Map<String, Integer> vocabulary = createVoc(s);

    public static void main(String[] args) {

        new SlidingWindowTest().process();

    }

    private void showWindow(int i, int focalWordID, int pointer_right, List<Integer> window, Map<Integer, String> vocabulary) {

        String window_string = "[";
        for (int j = 0; j < window.size(); j++) {
            if (j != 0) {
                window_string += ",";
            }
            if (j == focalWordID) {
                window_string += "**";
            }
            window_string += vocabulary.get(window.get(j)) + " (" + window.get(j) + ")";
            if (j == focalWordID) {
                window_string += "**";
            }
        }

        window_string += "]";

        System.out.println(i + " [" + focalWordID + "] - " + pointer_right + " (" + window.size() + ")\t" + window_string + " ");
    }

    private void process() {
        // there is no step since we want all words to be processed

        Map<Integer, String> reverseVoc = MapUtils.revert(vocabulary);
        int window_size_left = 5;
        int window_size_right = 5;
        int window_size_total = window_size_left + 1 + window_size_right;
        int[] text = textToArrayIDs(vocabulary, s);

        List<Integer> window = new ArrayList();
        for (int i = 0; i < 1 + window_size_right; i++) {
            window.add(text[i]);
        }
        int pointer_right = 1 + window_size_right;

        int focalWordID = 0;
        boolean shrink_left, expend_right;

        int count = 0;
        for (int i = 0; i < text.length; i++) {

            count++;
            shrink_left = false;
            expend_right = false;

            // TODO ->  [[process window]]
            showWindow(count, focalWordID, pointer_right, window, reverseVoc);
            processWindowForward(focalWordID, window);

            if (window.size() >= window_size_total) { // shrink left
                shrink_left = true;
            }

            if (pointer_right < text.length) { // expend right
                expend_right = true;
            } else {
                if (window.size() > window_size_left + 1) {
                    shrink_left = true;
                }
            }

            if (shrink_left) {
                window.remove(0);
            }
            if (expend_right) {
                window.add(text[pointer_right]);

            }

            if (!(shrink_left && expend_right)) {
                if (focalWordID < window_size_left) {
                    focalWordID++;// expending right
                } else if (focalWordID > window_size_left) {
                    focalWordID--;
                }
            }
            pointer_right++;

        }

    }

    private static Map<String, Integer> createVoc(String s) {
        Map<String, Integer> vocabulary = new HashMap();
        int c = 0;
        for (String ss : blank_pattern.split(s)) {
            vocabulary.put(ss, c);
            c++;
        }
        return vocabulary;
    }

    private void processWindowForward(int focalWordID, List<Integer> window) {

        for (int i = focalWordID + 1; i < window.size(); i++) {
            localWordCoocurences.add(window.get(focalWordID), window.get(i), 1);
            localWordCoocurences.add(window.get(i), window.get(focalWordID), 1);
        }
    }

    private static int[] textToArrayIDs(Map<String, Integer> vocabulary, String s) {
        ArrayList<Integer> list = new ArrayList();
        for (String ss : blank_pattern.split(s)) {
            list.add(vocabulary.get(ss));
        }
        return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
    }

}
