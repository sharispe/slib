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

import com.github.sharispe.slib.dsm.core.engine.Voc;
import com.github.sharispe.slib.dsm.core.engine.WordInfo;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.LoggerFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class XPUtils {

    static org.slf4j.Logger logger = LoggerFactory.getLogger(XPUtils.class);

    public static double[] sumVectors(double[] a, double[] b) throws SLIB_Ex_Critic {

        if (a.length != b.length) {
            throw new SLIB_Ex_Critic("vectors do not have the same length...");
        }

        double[] r = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            r[i] = a[i] + b[i];
        }
        return r;
    }

    public static double[] multiplyVector(double val, double[] a) throws SLIB_Ex_Critic {

        double[] r = new double[a.length];

        for (int i = 0; i < a.length; i++) {
            r[i] = val * a[i];
        }
        return r;
    }

//    public static void flushIndex(Voc index, String filename) throws SLIB_Ex_Critic {
//
//        logger.info("Flushing index into " + filename + " (n=" + index.size() + ")");
//        try (PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
//            
//            writer.println(index.getMaxWordSize());
//            
//            Map<String,Integer> indexInternal = index.getIndex();
//            
//            for (String k : indexInternal.keySet()) {
//
//                writer.println(indexInternal.get(k) + "\t" + index.getSize(k) + "\t" + k);
//            }
//        } catch (FileNotFoundException | UnsupportedEncodingException e) {
//            throw new SLIB_Ex_Critic(e.getMessage());
//        }
//
//    }
    
    /**
     * use Utils class function instead
     * @param <K>
     * @param <V>
     * @param index
     * @param filename
     * @throws SLIB_Ex_Critic
     * @deprecated
     */
    @Deprecated
    public static <K,V extends Comparable> void flushMAP(Map<K,V> index, String filename) throws SLIB_Ex_Critic {

        logger.info("Flushing index into " + filename + " (n=" + index.size() + ")");
        try (PrintWriter writer = new PrintWriter(filename, "UTF-8")) {
            
            
            for (Object k : MapUtils.sortByValue(index).keySet()) {

                writer.println(index.get(((K) k)).toString() + "\t" + k);
            }
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }

    }
    
    public static Map<Integer,String> loadIndexRevert(String filename) throws SLIB_Ex_Critic {

        Map<Integer,String> vocIndex = new HashMap();
        Voc i = new Voc(filename);
        for(Entry<String, Integer> e  : i.getIndex().entrySet()){
            vocIndex.put(e.getValue(), e.getKey());
        }
        return vocIndex;
    }

//    public static VocIndex loadIndex(String filename) throws SLIB_Ex_Critic {
//
//        
//        
//        int maxWordSize = 1;
//        Map<Integer, WordInfo> vocIndexMap = new HashMap();
//        try {
//            String line;
//            String[] data;
//            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
//                
//                line = br.readLine();
//                // try to retrieve wordsize
//                try{
//                line = line.trim();
//                maxWordSize = Integer.parseInt(line);
//                }
//                catch(Exception e ){
//                    throw new SLIB_Ex_Critic("Error cannot retrieve the max word size of the index");
//                }
//                line = br.readLine();
//                
//                
//                while (line != null) {
//                    data = line.split("\t");
//                    if (data.length == 3) {
//                        vocIndexMap.put(Integer.parseInt(data[0]), new WordInfo(data[2], Integer.parseInt(data[1])));
//                    }
//                    line = br.readLine();
//                }
//            }
//
//        } catch (IOException ex) {
//            throw new SLIB_Ex_Critic(ex.getMessage());
//        }
//        logger.info("Index " + filename + " loaded (n=" + vocIndexMap.size() + ")");
//        return new VocIndex(vocIndexMap,maxWordSize);
//    }
    
    /**
     * Use utils
     * @param filename
     * @return
     * @throws SLIB_Ex_Critic
     * @deprecated
     */
    @Deprecated
    public static Map<String, Integer> loadMAP(String filename) throws SLIB_Ex_Critic {

        Map<String, Integer> map = new HashMap();
        try {
            String line;
            String[] data;
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                
                line = br.readLine();
                
                while (line != null) {
                    data = line.split("\t");
                    if (data.length == 2) {
                        map.put(data[1], Integer.parseInt(data[0]));
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

    public static Map<Integer, Integer> loadVocUsage(String filename) throws SLIB_Ex_Critic {

        Map<Integer, Integer> vocUsage = new HashMap();
        try {
            String line;
            String[] data;
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                br.readLine(); //header
                line = br.readLine();

                while (line != null) {
                    data = line.split("\t");
                    if (data.length == 2) {
                        vocUsage.put(Integer.parseInt(data[0]), Integer.parseInt(data[1]));
                    }
                    line = br.readLine();
                }
            } //header

        } catch (IOException ex) {
            throw new SLIB_Ex_Critic(ex.getMessage());
        }
        logger.info("Voc usage " + filename + " loaded (n=" + vocUsage.size() + ")");
        return vocUsage;
    }

    /**
     * Distinguish the k dimensions associated to the k higher values in the
     * vector and replace the values for the dimensions which are not in this k
     * by 0. As an example given the vector [0,3,6,2,8] and setting k=2 the
     * results will be [0,0,6,0,8]
     *
     * @param vec
     * @param k
     * @return
     */
    public static double[] applyKthreshold(double[] vec, int k) {
        Map<Integer, Double> vecAsMap = new HashMap<>();
        for (int i = 0; i < vec.length; i++) {
            vecAsMap.put(i, vec[i]);
        }
        double[] vecn = new double[vec.length]; // values are initialized to 0 by default
        for (Map.Entry<Integer, Double> e : MapUtils.sortByValue(vecAsMap).entrySet()) {
            vecn[e.getKey()] = e.getValue();
            k--;
            if (k == 0) {
                break;
            }
        }
        return vecn;
    }

}
