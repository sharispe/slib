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

import com.github.sharispe.slib.dsm.core.engine.CoOccurrenceEngineTheads.CooccEngineResult;
import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrix;
import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrixGenerator;
import com.github.sharispe.slib.dsm.utils.FileUtility;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class CoOcurrenceEngine {

    Logger logger = LoggerFactory.getLogger(CoOcurrenceEngine.class);

    public static final int WINDOW_TOKEN_SIZE = 30;

    private final VocabularyIndex vocabularyIndex;
    private long fileProcessed;

    public CoOcurrenceEngine(VocabularyIndex voc) {
        this.vocabularyIndex = voc;
    }

    public long getNumberFileProcessed() {
        return fileProcessed;
    }

    /**
     *
     * @param directory
     * @param admittedExtensions
     * @return
     * @throws IOException
     * @deprecated
     */
    public Map<String, Map<String, Double>> computeMatrixTermIDFDocFromDir(String directory, List<String> admittedExtensions) throws IOException {

        logger.info("Computing words co term doc matrix from directory: " + directory);
        List<File> files = FileUtility.listFilesFromFolder(directory, admittedExtensions);
        logger.info("Processing " + files.size() + " files");

        Map<String, Map<String, Long>> mat = new HashMap(files.size());
        int c = 0;

        for (File f : files) {

            c++;
            if (c % 1000 == 0) {
                logger.info("Processing " + c + "/" + files.size() + f.getPath());
            }

            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                String line = br.readLine();

                while (line != null) {

                    Map<String, Long> m = new HashMap<>();

                    for (String s : line.split("\\s")) {
                        if (!m.containsKey(s)) {
                            m.put(s, new Long(1));
                        } else {
                            m.put(s, m.get(s) + (long) 1);
                        }
                    }
                    mat.put(f.getPath(), m);
                    line = br.readLine();
                }
            }
        }

        logger.info("Computing TF-IDF");
        long nbDoc = mat.size();

        /* 
         * Compute the tf - idf
         *  1)  we compute the number of documents in which each entry occurs
         *  to compute the IDF idf(t) = log ( D/ Dt ) with D the number of documents 
         * and Dt the number of document in which the term t occurs.
         * 
         * 2) we compute the tf -idf for each term t in each document d, with: 
         *  tf_idf (t,d) = tf(t,d) x idf(t)
         *  considering tf(t,d) the number of occurence of t in the document d (raw frequency)
         */
        Map<String, Double> idf = new HashMap<>();

        // We compute idf - a) Dt
        for (String fname : mat.keySet()) {

            for (String t : mat.get(fname).keySet()) {

                if (!idf.containsKey(t)) {
                    idf.put(t, 1.);
                } else {
                    idf.put(t, idf.get(t) + 1.);
                }
            }
        }

        // b) we finish the idf computation
        for (Entry<String, Double> e : idf.entrySet()) {

            double idfc = Math.log(nbDoc / e.getValue());
            idf.put(e.getKey(), idfc);
        }

        // We compute the tf_idf
        Map<String, Map<String, Double>> mat_tfidf = new HashMap<>();

        for (String fname : mat.keySet()) {

            Map<String, Double> matd_tfidf = new HashMap<>();

            for (Entry<String, Long> e : mat.get(fname).entrySet()) {

//                logger.info(e.getKey()+"\t"+idf.containsKey(e.getKey()));
                double tf = e.getValue();
                double idfc = idf.get(e.getKey());

                matd_tfidf.put(e.getKey(), tf * idfc);
            }

            mat_tfidf.put(fname, matd_tfidf);
        }

        logger.info("-------------------------------------");

        for (String fname : mat_tfidf.keySet()) {
            logger.info("\n" + fname);
            for (Entry<String, Double> e : mat_tfidf.get(fname).entrySet()) {
                System.out.print(" (" + e.getKey() + ", " + e.getValue() + ")");
            }
        }
        return mat_tfidf;
    }

    /**
     * Compute the cooccurrence between the terms contained in the texts located
     * in a specified directory. The vocabulary and vocabulary usage is expected
     * to be loaded in the object prior to computation.
     *
     * @param corpusDir
     * @param nbThreads
     * @throws slib.utils.ex.SLIB_Ex_Critic
     */
    public void computeCoOcurrence(String corpusDir, String output_dir_path, int nbThreads, int max_size_matrix) throws SLIB_Exception, IOException {

        int vocsize = vocabularyIndex.getVocabulary().size();
        // The word cocccurence matrix will be access by numerous threads
        ExecutorService threadPool = Executors.newFixedThreadPool(nbThreads);
        CompletionService<CooccEngineResult> taskCompletionService = new ExecutorCompletionService(threadPool);

        List<Future<CooccEngineResult>> futures = new ArrayList();

        int count_files = Utils.countNbFiles(corpusDir);

        logger.info("Number of files: " + count_files);
        logger.info("Vocabulary contains: " + vocsize);

        File output_dir = new File(output_dir_path);
        output_dir.mkdirs();

        List<File> flist = new ArrayList();
        int chunk_size = count_files / nbThreads;
        if (chunk_size > 10000) {
            chunk_size = 10000;
        }

        logger.info("chunk size " + chunk_size);

        int count_chunk = 0;
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(corpusDir), TrueFileFilter.TRUE, TrueFileFilter.INSTANCE);

        while (fileIterator.hasNext()) {

            File f = fileIterator.next();

            flist.add(f);

            if (flist.size() == chunk_size) {
                Callable<CooccEngineResult> worker = new CoOccurrenceEngineTheads(count_chunk, flist, vocabularyIndex, output_dir + "/t_" + count_chunk, max_size_matrix);
                futures.add(taskCompletionService.submit(worker));
                flist = new ArrayList();
                count_chunk++;
            }

        }
        if (!flist.isEmpty()) {
            Callable<CooccEngineResult> worker = new CoOccurrenceEngineTheads(count_chunk, flist, vocabularyIndex, output_dir + "/t_" + count_chunk, max_size_matrix);
            futures.add(taskCompletionService.submit(worker));
            count_chunk++;
        }
        threadPool.shutdown();

        ResultProcessor resultProcessor = new ResultProcessor(futures.size(), taskCompletionService, count_files);
        Thread result_thread = new Thread(resultProcessor);
        result_thread.start();

        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            throw new SLIB_Ex_Critic("Error compute matrix coocurrence: " + e.getMessage());
        }

        fileProcessed += count_files;
        logger.info("Number of errors detect: " + resultProcessor.file_processing_errors + "/" + count_files);
        if (resultProcessor.critical_errors != 0) {
            throw new SLIB_Ex_Critic("An error occured processing the corpus... please consult the log");
        }

        // do merge tmp matrices
        logger.info("merging tmp matrices");

        try (PrintWriter matrixWriter = new PrintWriter(output_dir_path + "/matrix", "UTF-8")) {

            int nbWords = vocabularyIndex.vocabulary.size();
            int c = 0;

            for (Integer wordId : vocabularyIndex.wordIdToWord.keySet()) {
                
                c++;
                
                if(c % 1000 == 0){
                    double p = c * 100.0 / nbWords;
                    System.out.print("\t"+c+"/"+nbWords+"   "+Utils.format2digits(p)+"%\t\r");
                }

                Map<Integer, Double> vec_word = new HashMap();

                for (int i = 0; i < count_chunk; i++) {

                    Map<Integer, Double> vec_chunk = getCompressedVector(wordId, output_dir + "/t_" + i + "/matrix");
                    
                    if (vec_chunk != null) {

                        for (Map.Entry<Integer, Double> e : vec_chunk.entrySet()) {

                            int wid = e.getKey();

                            if (vec_word.containsKey(wid)) {
                                vec_word.put(wid, vec_word.get(wid) + vec_chunk.get(wid));
                            } else {
                                vec_word.put(wid, vec_chunk.get(wid));
                            }
                        }
                    }
                }
                matrixWriter.write(CoOccurrenceEngineTheads.convertVectorMapToString(wordId, vec_word));
            }
        }

        logger.info("done");
    }

    /**
     * return null if either the matrix file is empty or the matrix does not
     * contain the vector
     *
     * @param id
     * @param matrix_path
     * @return
     * @throws IOException
     */
    private final Map<Integer, Double> getCompressedVector(int id, String matrix_path) throws IOException {

        Map<Integer, Double> map = null;

        File matrix_file = new File(matrix_path);

        if (matrix_file.exists()) {

            // search corresponding vector
            try (BufferedReader br = new BufferedReader(new FileReader(matrix_file))) {

                String line;
                String[] data, data2;
                int word_id, word_id_c;
                double occ, old_occ;

                // format 
                // word_id:word_id_1-nb_coccurences_word_id_word_id_1::word_id_2-nb_coccurences_word_id_word_id_2:...
                // e.g. 156:30-456:45-2
                // means that the word with id 156 cooccurred 456 times with word 30 and 2 times with word 45
                while ((line = br.readLine()) != null) {

                    data = Utils.colon_pattern.split(line.trim());
                    word_id = Integer.parseInt(data[0]);

                    if (word_id == id) {

                        map = new HashMap();
                        for (int i = 1; i < data.length; i++) {
                            data2 = Utils.dash_pattern.split(data[i]);// 30-456
                            word_id_c = Integer.parseInt(data2[0]);
                            occ = Double.parseDouble(data2[1]);
                            map.put(word_id_c, occ);
                        }
                    }
                }
            }
        }
        return map;
    }

    class ResultProcessor implements Runnable {

        private final int nbServices;
        private final int nbFiles;
        private final CompletionService<CooccEngineResult> services;
        int file_processing_errors = 0;
        int critical_errors = 0;

        public ResultProcessor(int nbServices, CompletionService<CooccEngineResult> services, int nbFiles) {
            this.nbServices = nbServices;
            this.services = services;
            this.nbFiles = nbFiles;
        }

        @Override
        public void run() {

            int performed = 0;
            int file_processed = 0;

            CompletionService<CooccEngineResult> this_service = this.services;

            while (performed < nbServices) {

                try {
                    logger.info("Waiting for new results, performed " + performed + "/" + nbServices);
                    CooccEngineResult result = this_service.take().get();
                    file_processed += result.file_processed;
                    file_processing_errors += result.errors;
                    performed++;
                    logger.info("*** " + file_processed + "/" + nbFiles + ", errors=" + file_processing_errors + "\t(" + performed + "/" + nbServices + ")");
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    critical_errors++;
                }
            }
        }

    }
}
