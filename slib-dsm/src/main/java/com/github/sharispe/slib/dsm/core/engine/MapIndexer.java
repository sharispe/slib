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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class MapIndexer {

    String directory;
    String label;
    Map<String, Integer> fileIds;
    int nextFilesIds = 0;
    final static Logger logger = LoggerFactory.getLogger(MapIndexer.class);

    public MapIndexer(String filepath, String label) {

        this.label = label;
        this.directory = filepath;
        fileIds = new HashMap();
        logger.info("(" + this.label + ") map indexer: " + this.directory);
        new File(filepath).mkdirs();
    }

    public int computeVocabularySize() throws Exception {

        int vocSize = 0;
        for (int i = 0; i < nextFilesIds; i++) {
            Map<String, WordInfo> m = loadMapWordInfo(new File(directory + "/" + i));
            vocSize += m.size();
        }
        return vocSize;
    }

    public void addToIndex(Map<String, WordInfo> map) throws Exception {

        logger.info("(" + label + ") add map size " + map.size() + " to index");
        logger.info("(" + label + ") sort map keys");

        List<String> sortedWords = new ArrayList(map.keySet());
        Collections.sort(sortedWords, String.CASE_INSENSITIVE_ORDER);

        logger.info("(" + label + ") process chunks");

        char a = 0;
        char b = 0;

        char atmp = 1;
        char btmp = 1;

        Map<String, WordInfo> map_chunk = new HashMap();

        for (String word : sortedWords) {

//            logger.info("'"+word+"'"+"\t"+map_chunk.size());
            atmp = Character.toLowerCase(word.charAt(0));
            btmp = word.length() > 1 ? Character.toLowerCase(word.charAt(1)) : 'a';

            if (a != 0 && (atmp != a || btmp != b)) {
                flush(map_chunk, a, b);
            }
            map_chunk.put(word, map.get(word));

            a = atmp;
            b = btmp;
        }
        flush(map_chunk, a, b);

        Utils.flushMapKV(fileIds, "\t", this.directory + "/" + VocStatComputer.FILE_INDEX);

        logger.info("(" + label + ") add map size " + map.size() + " to index [done]");

    }

    public void flush(Map<String, WordInfo> mapToAdd, char a, char b) throws Exception {

        if (mapToAdd.isEmpty()) {
            return;
        }

        File index_file;
        String key = a + "" + b;

        if (!fileIds.containsKey(key)) {
            index_file = new File(directory + "/" + nextFilesIds);
            fileIds.put(key, nextFilesIds);
            nextFilesIds++;
        } else {
            index_file = new File(directory + "/" + fileIds.get(key));
        }

        Map<String, WordInfo> map = loadMapWordInfo(index_file);
        String k;
        for (Map.Entry<String, WordInfo> e : mapToAdd.entrySet()) {
            k = e.getKey();
            if (map.containsKey(k)) {
                map.get(k).sumWordInfo(e.getValue());
            } else {
                map.put(k, e.getValue());
            }
        }

//        logger.info("("+id+") "+a + "" + b + "\tsize: " + map.size() + "\t"+index_file);
        flushMapWordInfo(index_file, map);
        mapToAdd.clear();
    }

    public static Map<String, WordInfo> loadMapWordInfo(File wordInfoFile) throws Exception {

        Map<String, WordInfo> map = new HashMap();
        if (!wordInfoFile.exists()) {
            return map;
        }

        BufferedReader br = new BufferedReader(new FileReader(wordInfoFile));

        try {
            String line = br.readLine();

            String[] data;
            while (line != null) {
                data = Utils.tab_pattern.split(line);

                map.put(data[0], new WordInfo(Integer.parseInt(data[1]), Integer.parseInt(data[2]),Integer.parseInt(data[3])));
                line = br.readLine();
            }

        } finally {
            br.close();
        }
        return map;
    }

    public static void flushMapWordInfo(File index_file, Map<String, WordInfo> map) throws Exception {
        try (FileWriter file = new FileWriter(index_file)) {
            for (Map.Entry<String, WordInfo> e : map.entrySet()) {
                WordInfo info = e.getValue();
                file.write(e.getKey() + "\t" + info.ngramsize + "\t" + info.nbOccurrences + "\t" + info.nbFilesWithWord + "\n");
            }
        }
    }

}
