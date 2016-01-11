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
package com.github.sharispe.slib.dsm.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class Utils {

    public static DecimalFormat numberFormatTwoDigit = new DecimalFormat("#.00");

    // Pattern used to extract tokens.
    public final static Pattern blank_pattern = Pattern.compile("\\s+");
    // Pattern used to load indexes.
    public final static Pattern tab_pattern = Pattern.compile("\t");
    // Pattern used to key-value separated by a dash.
    public final static Pattern dash_pattern = Pattern.compile("-");
    // Pattern used values separated by a colon.
    public final static Pattern colon_pattern = Pattern.compile(":");

    public static String format2digits(double n) {
        return numberFormatTwoDigit.format(n);
    }

    public static org.slf4j.Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Flush the given map into the file by considering a key value entry per
     * line and a given key/value separator.
     *
     * @param <K>
     * @param <V>
     * @param index the index to store into the file
     * @param kvSep the key-value separator
     * @param filename the index file
     * @throws SLIB_Ex_Critic
     */
    public static <K, V> void flushMapKV(Map<K, V> index, String kvSep, String filename) throws SLIB_Ex_Critic {

        logger.info("Flushing index into " + filename + " (n=" + index.size() + ", separator=" + kvSep + ")");
        try (PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
            for (Object k : index.keySet()) {
                writer.println(k + kvSep + index.get(k));
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Flush the given map into the file by considering a key value entry per
     * line and a given key/value separator.
     *
     * @param <K>
     * @param <V>
     * @param index the index to store into the file
     * @param kvSep the key-value separator
     * @param filename the index file
     * @throws SLIB_Ex_Critic
     */
    public static <K, V> void flushMapVK(Map<K, V> index, String kvSep, String filename) throws SLIB_Ex_Critic {

        logger.info("Flushing index into " + filename + " (n=" + index.size() + ", separator=" + kvSep + ")");
        try (PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
            for (Object k : index.keySet()) {
                writer.println(index.get(((K) k)).toString() + kvSep + k);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * Load into memory a key-value String,Integer map that is defined into a
     * file.
     *
     * @param filename the file that contains the index - one key-value entry
     * per line with key and value delimited by a value that can be parsed by
     * the given pattern. If the entry contains more than two values only the
     * first will be used as key and the second as value.
     * @param p
     * @return an in-memory Map.
     * @throws SLIB_Ex_Critic
     */
    public static Map<String, Integer> loadMap(String filename, Pattern p) throws SLIB_Ex_Critic {

        Map<String, Integer> map = new HashMap();
        try {
            String line;
            String[] data;
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

                line = br.readLine();

                while (line != null) {
                    data = p.split(line);
                    if (data.length >= 2) {
                        map.put(data[0], Integer.parseInt(data[1]));
                    }
                    line = br.readLine();
                }
            }

        } catch (IOException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
//        logger.info("map " + filename + " loaded (n=" + map.size() + ")");
        return map;
    }

    /**
     * Load into memory a key-value String,Long map that is defined into a file.
     *
     * @param filename the file that contains the index - one key-value entry
     * per line with key and value delimited by a value that can be parsed by
     * the given pattern. If the entry contains more than two values only the
     * first will be used as key and the second as value.
     * @param p
     * @return an in-memory Map.
     * @throws SLIB_Ex_Critic
     */
    public static Map<String, Long> loadMapStringLong(String filename, Pattern p) throws SLIB_Ex_Critic {

        Map<String, Long> map = new HashMap();
        try {
            String line;
            String[] data;
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

                line = br.readLine();

                while (line != null) {
                    data = p.split(line);
                    if (data.length >= 2) {
                        map.put(data[0], Long.parseLong(data[1]));
                    }
                    line = br.readLine();
                }
            }

        } catch (IOException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
        logger.info("map " + filename + " loaded (n=" + map.size() + ")");
        return map;
    }

    public static Map<Integer, String> loadMap_IntString(String filename) throws SLIB_Ex_Critic {

        return loadMap_IntString(filename, tab_pattern);
    }

    public static Map<Integer, String> loadMap_IntString(String filename, Pattern p) throws SLIB_Ex_Critic {

        Map<Integer, String> map = new HashMap();
        try {
            String line;
            String[] data;
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {

                line = br.readLine();

                while (line != null) {
                    data = p.split(line);
                    if (data.length >= 2) {
                        map.put(Integer.parseInt(data[0]), data[1]);
                    }
                    line = br.readLine();
                }
            }

        } catch (IOException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
        logger.info("map " + filename + " loaded (n=" + map.size() + ")");
        return map;
    }

    /**
     * Load into memory a key-value String,Integer map that is defined into a
     * file considering a tabular separator between keys and values.
     *
     * @param filename
     * @return an in-memory Map.
     * @throws SLIB_Ex_Critic
     */
    public static Map<String, Integer> loadMap(String filename) throws SLIB_Ex_Critic {
        return loadMap(filename, tab_pattern);
    }

    public static Map<String, Integer> loadMap(String filename, String pattern) throws SLIB_Ex_Critic {
        return loadMap(filename, Pattern.compile(pattern));
    }

    public static Map<String, Long> loadMapStringLong(String filename, String pattern) throws SLIB_Ex_Critic {
        return loadMapStringLong(filename, Pattern.compile(pattern));
    }

//    public static Map<String, Integer> loadMAP(String filename, String separator) throws SLIB_Ex_Critic {
//
//        Pattern split_pattern = Pattern.compile(separator);
//        
//        Map<String, Integer> map = new HashMap();
//        try {
//            String line;
//            String[] data;
//            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
//
//                line = br.readLine();
//
//                while (line != null) {
//                    data = split_pattern.split(line);
//                    if (data.length == 2) {
//                        map.put(data[1], Integer.parseInt(data[0]));
//                    }
//                    line = br.readLine();
//                }
//            }
//
//        } catch (IOException ex) {
//            throw new SLIB_Ex_Critic(ex.getMessage());
//        }
//        logger.info("map " + filename + " loaded (n=" + map.size() + ")");
//        return map;
//    }
    public static Set<String> loadWords(String f) throws IOException {

        Set<String> words = new HashSet();

        // Load the vocabulary
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                words.add(line.trim());
            }
        }

        return words;
    }

    public static long countNbFiles(String corpusDir) throws IOException {
        return countNbFiles(FileSystems.getDefault().getPath(corpusDir));
    }

    /**
     * Counts the number of files located into a given directory.
     *
     * @param corpusDir location of the corpus
     * @return the number of files the corpus contains
     * @throws IOException
     */
    private static long countNbFiles(Path corpusDir) throws IOException {

        long count = 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(corpusDir)) {
            for (Path p : ds) {

                if (Files.isDirectory(p)) {
                    count += countNbFiles(p);
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    public static String getCurrentDateAsString() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    // copy paste http://stackoverflow.com/questions/453018/number-of-lines-in-a-file-in-java
    public static long countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];
            int count = 0;
            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            return (count == 0 && !empty) ? 1 : count;
        } finally {
            is.close();
        }
    }
}
