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

import java.io.File;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
@Deprecated
public class Voc {

    private DB db;
    private ConcurrentMap<String, String> meta;

    private ConcurrentMap<String, Integer> index;
    private ConcurrentMap<Integer, Integer> word_size;
    private ConcurrentMap<Integer, Integer> nbfilesWithWord;
    private ConcurrentMap<Integer, Integer> nbWordOccurences;

    final static Pattern blank_pattern = Pattern.compile("\\s+");

    private int last_id = 0;
    private int max_size = 0;

    public Voc(String directory) {

        File dir = new File(directory);
        dir.mkdirs();

        db = DBMaker.newFileDB(new File(directory + "/voc_info"))
                .transactionDisable()
                .closeOnJvmShutdown()
                .make();

        index = db.getHashMap("voc_index");
        meta = db.getHashMap("meta");
        word_size = db.getHashMap("voc_token_size");
        nbfilesWithWord = db.getHashMap("voc_nb_files");
        nbWordOccurences = db.getHashMap("voc_nb_occ");

        initMetaInfo();
    }

    public ConcurrentMap<Integer, Integer> getNbfilesWithWord() {
        return nbfilesWithWord;
    }

    public ConcurrentMap<Integer, Integer> getNbWordOccurences() {
        return nbWordOccurences;
    }

    public void commit() {
        db.commit();
    }

    public int size() {
        return index.size();
    }

    public Integer addWordToIndex(String word) {
        return addWordToIndex(word, blank_pattern.split(word).length);
    }

    public Integer addWordToIndex(String word, int token_size) {

        Integer id = index.get(word);

        if (id == null) {
            id = last_id;
            last_id++;
            if (max_size < token_size) {
                max_size = token_size;
            }
            index.put(word, id);
            word_size.put(id, token_size);
        }
        return id;
    }

    public void addWordOcc(String word, int nbOcc) {

        Integer id = index.get(word);

        if (id == null) {
            id = addWordToIndex(word);
        }
        if (!nbWordOccurences.containsKey(id)) {
            nbWordOccurences.put(id, nbOcc);
        } else {
            nbWordOccurences.put(id, nbWordOccurences.get(id) + nbOcc);
        }
    }

    public void addFileOcc(String word, int nbOcc) {

        Integer id = index.get(word);

        if (id == null) {
            id = addWordToIndex(word);
        }
        if (!nbfilesWithWord.containsKey(id)) {
            nbfilesWithWord.put(id, nbOcc);
        } else {
            nbfilesWithWord.put(id, nbfilesWithWord.get(id) + nbOcc);
        }
    }

    private void initMetaInfo() {
        last_id = 0;

        if (meta.containsKey("LAST_ID")) {
            last_id = Integer.parseInt(meta.get("LAST_ID"));
        }
        if (meta.containsKey("MAX_SIZE")) {
            max_size = Integer.parseInt(meta.get("MAX_SIZE"));
        }
    }

    public void close() {

        meta.put("LAST_ID", last_id + "");
        meta.put("MAX_SIZE", max_size + "");
        db.commit();
        db.close();

    }

    public Integer getID(String ss) {
        return index.get(ss);
    }

    public Integer getSize(String ss) {
        return word_size.get(index.get(ss));
    }

    public ConcurrentMap<String, Integer> getIndex() {
        return index;
    }

}
