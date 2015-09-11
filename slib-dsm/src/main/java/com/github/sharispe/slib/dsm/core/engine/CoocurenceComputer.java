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
package com.github.sharispe.slib.dsm.core.engine;

import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrix;
import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrixGenerator;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Sébastien Harispe {@literal (sebastien.harispe@gmail.com)}
 */
class CoocurenceComputer {

    static void compute(String text, VocabularyIndex vocabularyIndex, int window_token_size, SparseMatrix result_matrix) {

        String[] stab = Utils.blank_pattern.split(text);

        int[] encodedText = encodeTokenArray(stab, vocabularyIndex);

        List<Word> leftWords = new ArrayList();

        Word word = getNextWord(encodedText, 0, null, vocabularyIndex);

        while (word != null) {
            
            

            int idCurrentWord = word.wordID;
            int startWindowLeft = word.start_loc - window_token_size;

            leftWords.add(word);

//            System.out.println("word: " + vocabularyIndex.getWord(word.wordID) + " id: " + idCurrentWord + " start: " + word.start_loc + " end: " + word.end_loc);
//            System.out.println("\tstart window left: " + startWindowLeft);
//            System.out.println("\tsize window left: " + leftWords.size());
//            System.out.println("\t\t +: " + vocabularyIndex.getWord(idCurrentWord) + " / " + vocabularyIndex.getWord(idCurrentWord));
            
            result_matrix.add(idCurrentWord, idCurrentWord, 1);
            
            // remove words that are not in the window
            Iterator<Word> i = leftWords.iterator();
            while (i.hasNext()) {

                Word windowWord = i.next();

                if (windowWord.start_loc < startWindowLeft) {
//                    System.out.println("\t\t[removing]: " + vocabularyIndex.getWord(windowWord.wordID));
                    i.remove();
                } else {
                    /* add cooccurence between current word and left window words
                    * and cooccurence between left window words and current word as well
                    * if we the sentence 'artificial intelligence x x x' and the words 'artificial' and 'artificial intelligence' in the index
                    * we do not want to consider that intelligence cooccurs with 'artificial intelligence'
                    */
                    if (windowWord.end_loc < word.end_loc && idCurrentWord != windowWord.wordID) {
//                        System.out.println("\t\t +: " + vocabularyIndex.getWord(idCurrentWord) + " / " + vocabularyIndex.getWord(windowWord.wordID));
//                        System.out.println("\t\t +: " + vocabularyIndex.getWord(windowWord.wordID) + " / " + vocabularyIndex.getWord(idCurrentWord));
                        result_matrix.add(idCurrentWord, windowWord.wordID, 1);
                        result_matrix.add(windowWord.wordID, idCurrentWord, 1);
                    }
                }
            }
            word = getNextWord(encodedText, word.start_loc, word.tokens, vocabularyIndex);
        }
    }

    /**
     * Return the next word, as a sequence of node, which correspond to the next
     * indexed word considering the given context, that is: text sequence, start
     * location, previous word (as a list of Node).
     *
     * @param encodeText
     * @param start
     * @param tokenNodeHistory
     * @param index
     * @return
     */
    public static Word getNextWord(int[] encodeText, int start, List<VocabularyIndex.TokenNode> tokenNodeHistory, VocabularyIndex index) {

        Word r = null;

        while (start < encodeText.length && r == null) {

//            if (r == null) {
//                System.out.println("\tstill looking");
//            }
//            System.out.println("\tlooking at : " + start + "/"+text.length+" token: "+index.getToken(text[start])+"\ttoken history: " + showTokenNodeHistory(tokenNodeHistory, index));
            r = getNextWordInner(encodeText, start, tokenNodeHistory, index);

            tokenNodeHistory = null;
            start++;
        }

//        System.out.println("\tfound: "+r);
//        if(r != null) System.out.println("\tfound " + index.getWord(r.wordID));
        return r;
    }

    /**
     * Return the next token considering the given parameters. The method return
     * null if no word can be created considering the given configuration.
     *
     * @param encodedText
     * @param start
     * @param tokenNodeHistory
     * @param index
     * @return
     */
    private static Word getNextWordInner(int[] encodedText, int start, List<VocabularyIndex.TokenNode> tokenNodeHistory, VocabularyIndex index) {

        if (start >= encodedText.length) {
            return null;
        }

        int id_start_token = encodedText[start];

        VocabularyIndex.TokenNode next_token;

        if (tokenNodeHistory == null || tokenNodeHistory.isEmpty()) { // we are starting a new word 

            if (id_start_token == -1) { // the current token is not indexed in all cases we iterate trying to start a new word
                return null;
            }

            // We are starting a new word the first token is therefore attached to the root
            next_token = index.getTree_root().getChild(id_start_token);

            // we test if the indexed token starts or is a word
            if (next_token == null) { // does not a start word and is not word by itself we iterate trying to start a new word
                return null;

            } else if (next_token.isWordEnd()) { // indexed token is a word

                tokenNodeHistory = new ArrayList();
                tokenNodeHistory.add(next_token); // we store the token into the history
                return new Word(next_token.getWordID(), tokenNodeHistory, start); // and we return the result

            } else { // indexed token is a not word but starts a word we therefore try to extend it

                tokenNodeHistory = new ArrayList();
                tokenNodeHistory.add(next_token);
                return getNextWordInner(encodedText, start, tokenNodeHistory, index);
            }
        } else {  // history is not null - we try to extend the current word 

            int token_sequence_size = tokenNodeHistory.size();
            if (start + token_sequence_size >= encodedText.length) { // word cannot be extended we iterate trying to start a new word
                return null;
            }
            // we try to extend the current word
            VocabularyIndex.TokenNode last_node = tokenNodeHistory.get(token_sequence_size - 1); // last token of the current word
            next_token = last_node.getChild(encodedText[start + token_sequence_size]);

            if (next_token == null) { // word cannot be extended we iterate

                return null;

            } else if (next_token.isWordEnd()) { // adding the next token to the current created a word

                tokenNodeHistory.add(next_token);
                return new Word(next_token.getWordID(), tokenNodeHistory, start);

            } else { // adding the next token extend a potential word but does not create a word yet
                tokenNodeHistory.add(next_token);
                return getNextWordInner(encodedText, start, tokenNodeHistory, index);
            }
        }
    }

    /**
     * Convert an array of String to an array of IDs considering the given
     * index. If a word is not defined into the index the value -1 is set.
     *
     * @param vocabularyIndex
     * @param tokenArray
     * @return
     */
    public static int[] encodeTokenArray(String[] tokenArray, VocabularyIndex vocabularyIndex) {

        int[] IDArray = new int[tokenArray.length];

        for (int i = 0; i < tokenArray.length; i++) {
            Integer k = vocabularyIndex.getTokenID(tokenArray[i]);
            if (k == null) {
                k = -1;
            }
            IDArray[i] = k;
        }
        return IDArray;
    }

    private static String showTokenNodeHistory(List<VocabularyIndex.TokenNode> tokenNodeHistory, VocabularyIndex index) {
        if (tokenNodeHistory == null) {
            return "null";
        } else {
            String s = "";
            for (VocabularyIndex.TokenNode t : tokenNodeHistory) {
                s += " " + index.getToken(t.id);
            }
            return s;
        }
    }

    public static void main(String[] argv) {

        String text = "Artificial intelligence (AI) is the intelligence exhibited by machines or software. "
                + "It is also the name of the academic field of study which studies how to create computers and "
                + "computer software that are capable of intelligent behavior. "
                + "Major AI researchers and textbooks define this field as the study and design of intelligent agents,[1] "
                + "in which an intelligent agent is a system that perceives its environment and takes actions that maximize "
                + "its chances of success.[2] John McCarthy, who coined the term in 1955,[3] defines it as "
                + "the science and engineering of making intelligent machines"; // [wikipedia]

        text = text.toLowerCase();
        Set<String> words = new HashSet(Arrays.asList("intelligence", "software", "artificial intelligence", "environment", "system that perceives its environment","maximize"));
        Vocabulary voc = new Vocabulary(words);
        VocabularyIndex index = new VocabularyIndex(voc);
        SparseMatrix matrix = SparseMatrixGenerator.buildSparseMatrix(voc.size(), voc.size());
        CoocurenceComputer.compute(text, index, 1000, matrix);

        System.out.println(text);

        for (String w : words) {

            int id_w = index.getWordID(w);

            for (String w2 : words) {

                int id_w2 = index.getWordID(w2);

                System.out.println(w + "/" + w2 + "\t" + matrix.get(id_w, id_w2));

            }
        }

    }

}
