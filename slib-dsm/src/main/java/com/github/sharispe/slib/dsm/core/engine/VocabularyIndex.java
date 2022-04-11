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

import com.github.sharispe.slib.dsm.utils.Utils;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author SÃ©bastien Harispe (sebastien.harispe@gmail.com)
 */
public class VocabularyIndex {

    Vocabulary vocabulary;
    Map<String, Integer> wordToWordId;
    Map<Integer, String> wordIdToWord;

    Map<String, Integer> tokenIDIndex;
    Map<Integer, String> tokenIndex;
    TokenNode tree_root;

    Logger logger = LoggerFactory.getLogger(VocabularyIndex.class);

    public VocabularyIndex(Vocabulary voc) {
        this.vocabulary = voc;

        buildIndex();
    }

    public Vocabulary getVocabulary() {
        return vocabulary;
    }

    /**
     * @param token the token of interest
     * @return null if the token is not indexed
     */
    public Integer getTokenID(String token) {
        return tokenIDIndex.get(token);
    }

    /**
     * @param id the id of the token of interest
     * @return null if the token is not indexed
     */
    public String getToken(int id) {
        return tokenIndex.get(id);
    }

    /**
     * @param word the word of interest
     * @return null if the word is not indexed
     */
    public Integer getWordID(String word) {
        return wordToWordId.get(word);
    }

    /**
     * @param id the id of the word of interest
     * @return null if the word is not indexed
     */
    public String getWord(int id) {
        return wordIdToWord.get(id);
    }

    private void buildIndex() {

        logger.info("Building index");

        tokenIDIndex = new HashMap();
        tokenIndex = new HashMap();
        wordIdToWord = new HashMap();
        wordToWordId = new HashMap();

        tree_root = new TokenNode(-1, null);

        int wordID = 0;
        int tokenID = 0;
        int nodeNumber = 0;

        for (String w : vocabulary.getElements()) {

            wordToWordId.put(w, wordID);
            wordIdToWord.put(wordID, w);
            
            String[] tokens = Utils.blank_pattern.split(w);
            TokenNode current_token_node;
            int current_token_node_id;
            TokenNode parentNode = null;

            for (int i = 0; i < tokens.length; i++) {

                String token = tokens[i];

                // create an ID for the token if none exists
                if (!tokenIDIndex.containsKey(token)) {
                    tokenIDIndex.put(token, tokenID);
                    tokenIndex.put(tokenID, token);
                    current_token_node_id = tokenID;
                    tokenID++;
                } else {
                    current_token_node_id = tokenIDIndex.get(token);
                }

                // processing root node
                if (parentNode == null) {
                    // check if the root node does not exist
                    current_token_node = tree_root.getChild(current_token_node_id);
                    if (current_token_node == null) {
                        current_token_node = new TokenNode(current_token_node_id, tree_root);
                        tree_root.addChild(current_token_node);
                        nodeNumber++;
                    }
                } else { // processing inner tree node
                    TokenNode existingChild = parentNode.getChild(current_token_node_id);
                    if (existingChild == null) { // the node does not exist
                        current_token_node = new TokenNode(current_token_node_id, parentNode);
                        nodeNumber++;
                        parentNode.addChild(current_token_node);
                    } else {
                        current_token_node = existingChild;
                    }
                }
                parentNode = current_token_node;

                if (i == tokens.length - 1) { // word processed
                    current_token_node.isWordEnd(true);
                    current_token_node.setWordID(wordID);
                }
            }
            wordID++;
        }
        logger.info("Index completed, token: " + tokenIDIndex.size() + "\tnodes: " + nodeNumber+"\twords: "+wordIdToWord.size());
    }

    public TokenNode getTree_root() {
        return tree_root;
    }

    public Map<String, Integer> getWordToWordId() {
        return wordToWordId;
    }

    public Map<Integer, String> getWordIdToWord() {
        return wordIdToWord;
    }
    
    
    

    public class TokenNode {

        int id;
        boolean isWordEnd;
        int wordID;
        TokenNode parent;
        Map<Integer, TokenNode> children;

        public TokenNode(int id, TokenNode parent) {
            this.id = id;
            this.parent = parent;
        }

        public TokenNode getChild(Integer id) {
            if (children == null) {
                return null;
            } else {
                return children.get(id);
            }
        }

        public boolean hasChild() {
            if (children == null) {
                return false;
            } else {
                return !children.isEmpty();
            }
        }

        private void addChild(TokenNode childNode) {
            if (children == null) {
                children = new HashMap();
            }
            children.put(childNode.id, childNode);
        }

        public void isWordEnd(boolean b) {
            isWordEnd = b;
        }

        public boolean isWordEnd() {
            return isWordEnd;
        }

        public int getId() {
            return id;
        }

        public void setWordID(int wordID) {
            this.wordID = wordID;
        }

        public int getWordID() {
            return wordID;
        }

    }

}
