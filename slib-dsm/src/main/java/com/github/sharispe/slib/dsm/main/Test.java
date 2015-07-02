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

import com.github.sharispe.slib.dsm.core.engine.CoOccurrenceEngineTheads;
import com.github.sharispe.slib.dsm.core.engine.Vocabulary;
import com.github.sharispe.slib.dsm.core.engine.VocabularyIndex;
import com.github.sharispe.slib.dsm.core.engine.VocabularyIndex.TokenNode;
import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrix;
import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrixGenerator;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import slib.utils.impl.UtilDebug;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Test {

    public static void main(String[] args) throws Exception {

        File f = new File("/home/seb/data/OANC/data/written_2/non-fiction/OUP/Castro/chL.txt");
        String vocPath = "/tmp/voc";//"/data/englishNouns.txt";

        String t = "/tmp/test";
        String terror = "/data/english-corpus/OANC/data/spoken/telephone/switchboard/27/sw2789-ms98-a-trans.txt";
        String tvoc = "/tmp/testvoc";

        Set<String> set = new HashSet();
        set.add("machine");
        set.add("machine learning");
        set.add("Machine learning");
        set.add("artificial");
        set.add("artificial intelligence");

        Vocabulary voc = new Vocabulary("/data/englishNouns.txt");
//        Vocabulary voc = new Vocabulary(set);
        VocabularyIndex index = new VocabularyIndex(voc);

        String text = "This is a test about machine learning  and computer science and other techhniques great "
                + " machine learning is a subfield of computer science that evolved from the study of pattern recognition and computational learning theory in artificial intelligence";
        int[] ids = CoOccurrenceEngineTheads.tokenArrayToIDArray(Utils.blank_pattern.split(text), index);
        System.out.println(Arrays.toString(ids));
        process(ids, index);

        UtilDebug.exit();

    }

    static void process(int[] text, VocabularyIndex index) {

        SparseMatrix matrix = SparseMatrixGenerator.buildSparseMatrix(index.getVocabulary().size(), index.getVocabulary().size());
        int window_token_size = 10;


        List<Word> leftWords = new ArrayList();

        Word word = getNextWord(text, 0, null, index);

        while (word != null) {

            int idWord = word.wordID;
            int startWindowLeft = word.start_loc - window_token_size;

//            System.out.println("----------------------");
//            System.out.println(index.getWord(word.wordID) + " (" + word.start_loc + ")");
            // remove words that are not in the window
            Iterator<Word> i = leftWords.iterator();
            while (i.hasNext()) {
                Word w = i.next();
                if (w.start_loc < startWindowLeft) {
                    i.remove();
                } else {
                    // add cooccurence between current word and left window words
                    matrix.add(idWord, w.wordID, 1);
//                    System.out.println(index.getWord(idWord) + "\t" + index.getWord(w.wordID));

                    // and cooccurence between left window words and current word as well
                    if (w.start_loc + window_token_size >= word.end_loc) {
                        matrix.add(w.wordID, idWord, 1);
//                        System.out.println(index.getWord(w.wordID) + "\t" + index.getWord(idWord));
                    }
                }
            }
//            for (Word w : leftWords) {
//                System.out.print(index.getWord(w.wordID) + " (" + w.start_loc + ")  : ");
//            }
//            System.out.println("");

            leftWords.add(word);

            word = getNextWord(text, word.start_loc, word.tokens, index);
        }

        int word1 = index.getWordID("machine learning");
        int word2 = index.getWordID("computer science");

        double nbOcc = matrix.get(word1, word2);
        System.out.println("cooccurence " + nbOcc);
    }

    private static class Word {

        List<TokenNode> tokens;
        int start_loc;
        int end_loc;
        int wordID;

        public Word(int wordID, List<TokenNode> tokens, int start_loc) {
            this.wordID = wordID;
            this.tokens = tokens;
            this.start_loc = start_loc;
            this.end_loc = start_loc+tokens.size()-1;
        }
    }

    /**
     * Return the next word, as a sequence of node, which correspond to the next
     * indexed word considering the given context, that is: text sequence, start
     * location, previous word (as a list of Node).
     *
     * @param text
     * @param start
     * @param tokenNodeHistory
     * @param index
     * @return
     */
    private static Word getNextWord(int[] text, int start, List<TokenNode> tokenNodeHistory, VocabularyIndex index) {

        if (start >= text.length) {
            return null;
        }

        int id_start_token = text[start];

//        System.out.println("start: " + start + "\ttoken: " + id_start_token + "\thas history: " + (nodeHistory != null));
        TokenNode next_node;

        if (tokenNodeHistory == null || tokenNodeHistory.isEmpty()) { // we are starting a new word and the current token is indexed

            if (id_start_token == -1) { // the current token is not indexed in all cases we iterate trying to start a new word
//                System.out.println("current token not indexed -> iterate trying to start a new word");
                return getNextWord(text, start + 1, null, index);
            }

//            System.out.print("current token indexed: ");
            next_node = index.getTree_root().getChild(id_start_token);

            // we test if the indexed token starts or is a word
            if (next_node == null) { // does not a start word and is not word we iterate trying to start a new word
//                System.out.println(" but does not a start word and is not word -> iterate trying to start a new word");
                return getNextWord(text, start + 1, null, index);

            } else if (next_node.isWordEnd()) { // indexed token is a word

                tokenNodeHistory = new ArrayList();
                tokenNodeHistory.add(next_node); // we store the token into the history
//                System.out.println("found word: " + next_node + "\t" + inde(nodeHistory, index));
                return new Word(next_node.getWordID(), tokenNodeHistory, start); // and we return the result

            } else { // indexed token is a not word but starts a word

                tokenNodeHistory = new ArrayList();
                tokenNodeHistory.add(next_node);
//                System.out.println("starts a  word -> extending");
                return getNextWord(text, start, tokenNodeHistory, index);
            }
        } else {  // history is not null - we try to extend the current word 

//            System.out.print("looking for word extension\thistory size:" + nodeHistory.size() + ": ");
            int token_sequence_size = tokenNodeHistory.size();
            if (start + token_sequence_size >= text.length) { // word cannot be extended we iterate trying to start a new word
//                System.out.println(" out of space, abort extension, iterate trying to start a new word");
                return getNextWord(text, start + 1, null, index);
            }
            // we try to extend the current word
            TokenNode last_node = tokenNodeHistory.get(token_sequence_size - 1); // last token of the current word
            next_node = last_node.getChild(text[start + token_sequence_size]);

            if (next_node == null) { // word cannot be extended we iterate

//                System.out.println("next token does not extend the current word, iterate trying to start a new word");
                return getNextWord(text, start + 1, null, index);

            } else if (next_node.isWordEnd()) { // adding the next token to the current created a word

                tokenNodeHistory.add(next_node);
//                System.out.println("found word extension: " + next_node + "\t" + createWord(nodeHistory, index));
                return new Word(next_node.getWordID(), tokenNodeHistory, start);

            } else { // adding the next token extend a potential word but does not create a word yet
                tokenNodeHistory.add(next_node);
//                System.out.println("extending existing word");
                return getNextWord(text, start, tokenNodeHistory, index);
            }
        }
    }

//    private void process(String input) throws Exception {
//
//        String[] elements = input.split("\\s");
//
//        Node current = null;
//
//        for (String e : elements) {
//
//            if (e.trim().isEmpty()) {
//                continue;
//            }
//
//            if (e.equals("+") || e.equals("-")) {
//                // do something
//                System.out.println("Operation: " + e);
//                Node n = new Node(e);
//                n.setOperation(e);
//                n.setLeftPart(current);
//                current = n;
//            } else if (e.equals("(")) {
//
//            } else {
//                System.out.println("Create Node: " + e);
//                Node n = new Node(e);
//
//                if (current == null) {
//                    current = n;
//                } else if (current.getOperation() == null) {
//                    throw new Exception("Maformed Expression");
//                } else {
//                    current.setRightPart(n);
//                }
//
//            }
//        }
//        System.out.println(current);
//
//    }
}

//    private class Node {
//
//    String label;
//    String operation;
//    Node leftPart, rightPart;
//
//    public Node(String label) {
//        this.label = label;
//    }
//
//    public String getOperation() {
//        return operation;
//    }
//
//    public void setOperation(String operation) {
//        this.operation = operation;
//    }
//
//    public Node getLeftPart() {
//        return leftPart;
//    }
//
//    public void setLeftPart(Node leftPart) {
//        this.leftPart = leftPart;
//    }
//
//    public Node getRightPart() {
//        return rightPart;
//    }
//
//    public void setRightPart(Node rightPart) {
//        this.rightPart = rightPart;
//    }
//
//    @Override
//    public String toString() {
//
//        if (this.operation != null) {
//            return "(" + this.leftPart.toString() + this.operation + this.rightPart + ")";
//        } else {
//            return this.label;
//        }
//
//    }

