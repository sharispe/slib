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

import com.github.sharispe.slib.dsm.utils.FileUtility;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class VocUsageUtils {

    final static Logger logger = LoggerFactory.getLogger(VocUsageUtils.class);
    final static Pattern blank_pattern = Pattern.compile("\\s+");

    private static void initVocUsage(Map<String, Integer> vocIndex, Map<Integer, Integer> vocUsage) {
        vocUsage.clear();
        for (Integer k : vocIndex.values()) {
            vocUsage.put(k, 0);
        }
    }

    public static Map<Integer, Integer> globalVocUsage(Map<File, Map<Integer, Integer>> fileVocUsage, Map<String, Integer> vocIndex) {

        logger.info("Loading global voc usage");

        Map<Integer, Integer> vocUsage = new HashMap();
        initVocUsage(vocIndex, vocUsage);
        Integer k_id;

        for (Map<Integer, Integer> fusage : fileVocUsage.values()) {

            for (Map.Entry<Integer, Integer> e : fusage.entrySet()) {

                k_id = e.getKey();

                // the map has been initialize to 0
                // if an error occurs its because the vocIndex and the statistics on voc usage per files are not coherent
                vocUsage.put(k_id, vocUsage.get(k_id) + e.getValue());

            }
        }
        return vocUsage;
    }

    /**
     *
     *
     * @param directory
     * @param admittedExtensions
     * @param vocIndex
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws slib.utils.ex.SLIB_Ex_Critic
     */
    public static Map<Integer, Integer> globalVocUsage(String directory, List<String> admittedExtensions, Map<String, Integer> vocIndex) throws FileNotFoundException, IOException, SLIB_Ex_Critic {

        List<File> files = FileUtility.listFilesFromFolder(directory, admittedExtensions);
        return VocUsageUtils.globalVocUsage(files, vocIndex);
    }

    public static Map<Integer, Integer> globalVocUsage(List<File> files, Map<String, Integer> vocIndex) throws FileNotFoundException, IOException, SLIB_Ex_Critic {

        Map<Integer, Integer> vocUsage = new HashMap();
        initVocUsage(vocIndex, vocUsage);

        int nbFileDone = 0;
        Integer id_word;

        for (File f : files) {
            nbFileDone++;
            if (nbFileDone % 1000 == 0) {
                logger.info("File: " + nbFileDone + "/" + files.size() + "\t" + f.getPath() + "\r");
            }
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {

                String line = br.readLine();

                while (line != null) {

                    for (String w : blank_pattern.split(line)) {

                        id_word = vocIndex.get(w);

                        if (id_word != null) { // the map has been initialize to 0
                            vocUsage.put(id_word, vocUsage.get(id_word) + 1);
                        }
                    }

                    line = br.readLine();
                }
            }
        }
        return vocUsage;
    }

    public static Map<File, Map<Integer, Integer>> localVocUsage(Collection<File> files, Map<String, Integer> vocIndex) throws SLIB_Ex_Critic {
        Map<File, Map<Integer, Integer>> docsAsMap = new HashMap();
        try {
            logger.info("Loading local voc usage");

            int c = 0;
            for (File f : files) {

                c++;
                if (c % 1000 == 0) {
                    logger.info("processing " + c + "/" + files.size() + "\r");
                }
                Map<Integer, Integer> docAsMapLoc = new HashMap();
                List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()), Charset.defaultCharset());

                Integer id_word;
                for (String s : lines) {

                    for (String w : blank_pattern.split(s)) {
                        id_word = vocIndex.get(w);
                        if (id_word != null) {
                            if (!docAsMapLoc.containsKey(id_word)) {
                                docAsMapLoc.put(id_word, 1);
                            } else {
                                docAsMapLoc.put(id_word, docAsMapLoc.get(id_word));
                            }
                        }
                    }
                }

                docsAsMap.put(f, docAsMapLoc);
            }
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        return docsAsMap;
    }

    public static Map<Integer, Long> computeNBFilesWithWord(Collection<File> files, Map<String, Integer> vocIndex) throws SLIB_Ex_Critic {

        Map<Integer, Long> nbFilesWithWord = new HashMap();
        Map<File, Map<Integer, Integer>> localVocUsage = localVocUsage(files, vocIndex);

        for (Map<Integer, Integer> e : localVocUsage.values()) {

            for (Integer idWord : e.keySet()) {
                if (!nbFilesWithWord.containsKey(idWord)) {
                    nbFilesWithWord.put(idWord, 1L);
                } else {
                    nbFilesWithWord.put(idWord, nbFilesWithWord.get(idWord) + 1);
                }
            }
        }
        return nbFilesWithWord;
    }

}
