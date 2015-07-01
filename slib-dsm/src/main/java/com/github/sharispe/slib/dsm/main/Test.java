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
import com.github.sharispe.slib.dsm.core.engine.VocabularyIndex.Node;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.File;
import static java.lang.reflect.Array.set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
                + " Machine learning is a subfield of computer science that evolved from the study of pattern recognition and computational learning theory in artificial intelligence"
                + " Machine learning explores the construction and study of algorithms that can learn from and make predictions on data";
        int[] ids = CoOccurrenceEngineTheads.tokenArrayToIDArray(Utils.blank_pattern.split(text), index);
        System.out.println(Arrays.toString(ids));
        process(ids, index);

        UtilDebug.exit();

    }

    static void process(int[] text, VocabularyIndex index) {

        int window_size_right = 10;
        int window_size_left = 10;

        int start_focal_word = 0;
        int size_focal_word = 1;

        Node current_node = null;

        
        
        List<Node> sequenceToken = null;
        int start_previous = 0;

        Result result = getNextWord(text, start_focal_word, sequenceToken, index);
        
        while (result != null) {
            
            if (result.nodeHistory == null) {
                start_focal_word++;
                sequenceToken = null;
            }
            else{
                sequenceToken = result.nodeHistory;
                start_focal_word = result.start_loc;
            }
            result = getNextWord(text, start_focal_word, sequenceToken, index);
        }

    }
    
    private static class Result{
        List<Node> nodeHistory;
        int start_loc;
        
        public Result(List<Node> nodeHistory, int start_loc){
            this.nodeHistory = nodeHistory;
            this.start_loc = start_loc;
        }
    }

    private static Result getNextWord(int[] text, int start, List<Node> nodeHistory, VocabularyIndex index) {

        if (start >= text.length) {
            return null;
        }

        int id_start_token = text[start];

        System.out.println("start and id token: " + start + "\t" + id_start_token+"\thas history: "+(nodeHistory!=null));

        if (id_start_token == -1) { // means that the token is not indexed we iterate
            System.out.println("next token not indexed iterate");
            return getNextWord(text, start + 1, null, index);
        }

        Node next_node;

        if (nodeHistory == null) { // we are starting a new word 

            System.out.println("looking for new word");
            next_node = index.getTree_root().getChild(id_start_token);

            if (next_node == null) { // current position is not a node we iterate
                System.out.println("iterate");
                return getNextWord(text, start + 1, null, index);

            } else if (next_node.isWordEnd()) { // current position is a node corresponding to a word

                nodeHistory = new ArrayList();
                nodeHistory.add(next_node);
                String word = createWord(nodeHistory, index);
                System.out.println("found word: " + next_node + "\t" + word);

                return new Result(nodeHistory,start);

            } else { // current position is a node that does not correspond to a word

                nodeHistory = new ArrayList();
                nodeHistory.add(next_node);
                System.out.println("extending");
                return getNextWord(text, start, nodeHistory, index);
            }
        } else {  // we try to extend the current word 

            System.out.println("looking for word extension\thistory size:"+nodeHistory.size());
            int token_sequence_size = nodeHistory.size();
            if (start + token_sequence_size >= text.length) {
                return getNextWord(text, start + 1, null, index);
            }
            Node last_node = nodeHistory.get(token_sequence_size - 1);
            next_node = last_node.getChild(text[start + token_sequence_size]);

            if (next_node == null) { // word cannot be extended we iterate

                System.out.println("iterate");
                return getNextWord(text, start + 1, null, index);

            } else if (next_node.isWordEnd()) { // word extension is a node corresponding to a word

                nodeHistory.add(next_node);
                String word = createWord(nodeHistory, index);
                System.out.println("found word extension: " + next_node + "\t" + word);

                return new Result(nodeHistory,start);

            } else { // word extension is a node that does not correspond to a word
                nodeHistory.add(next_node);
                System.out.println("extending");
                return getNextWord(text, start, nodeHistory, index);
            }
        }
    }

    private static String createWord(List<Node> nodeHistory, VocabularyIndex index) {
        String word = "";
        for (int i = 0; i < nodeHistory.size(); i++) {
            if (i != 0) {
                word += " ";
            }
            word += index.getToken(nodeHistory.get(i).getId());
        }
        return word;
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

