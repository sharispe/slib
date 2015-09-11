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
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class CoOcurrenceEngine {

    Logger logger = LoggerFactory.getLogger(CoOcurrenceEngine.class);

    private final VocabularyIndex vocabularyIndex;

    private static long nb_files_processed = 0;
    private static long nb_files_to_analyse = 0;

    private static final Object lock_nb_files_processed = new Object();

    public CoOcurrenceEngine(VocabularyIndex voc) {
        this.vocabularyIndex = voc;
    }

    /**
     * @return the number of files already processed.
     */
    public static long getNbFilesProcessed() {
        return nb_files_processed;
    }

    /**
     * @return the total number of files to analyse.
     */
    public static long getNbFilesToAnalyse() {
        return nb_files_to_analyse;
    }

    public static void incrementProcessedFiles(int i) {
        synchronized (lock_nb_files_processed) {
            nb_files_processed += i;
        }
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
     * @param output_dir_path
     * @param window_token_size
     * @param nbThreadsLimit
     * @param nbFilesPerChunk
     * @param max_size_matrix
     * @throws slib.utils.ex.SLIB_Ex_Critic
     * @throws java.io.IOException
     */
public void computeCoOcurrence(String corpusDir, String output_dir_path, int window_token_size, int nbThreadsLimit, int nbFilesPerChunk, int max_size_matrix) throws SLIB_Exception, IOException, InterruptedException {

        logger.info("Computing cooccurences");
        logger.info("corpus dir: " + corpusDir);
        logger.info("model dir: " + output_dir_path);
        logger.info("windows size left/right: " + window_token_size);
        logger.info("thread limit: " + nbThreadsLimit);
        logger.info("Number of files per chunk: " + nbFilesPerChunk);
        logger.info("Matrix size per thread: " + max_size_matrix);

        int vocsize = vocabularyIndex.getVocabulary().size();
        logger.info("Vocabulary size: " + vocsize);
        // The word cocccurence matrix will be access by numerous threads
        ExecutorService threadPool = Executors.newFixedThreadPool(nbThreadsLimit);

        nb_files_to_analyse += Utils.countNbFiles(corpusDir);

        logger.info("Number of files: " + nb_files_to_analyse);

        File output_dir = new File(output_dir_path);
        output_dir.mkdirs();

        List<File> flist = new ArrayList();

        int count_chunk = 0;
        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(corpusDir), TrueFileFilter.TRUE, TrueFileFilter.INSTANCE);

        CompletionService<CooccEngineResult> taskCompletionService = new ExecutorCompletionService(threadPool);
        ResultProcessor resultProcessor = new ResultProcessor(taskCompletionService);
        resultProcessor.setActive(true);
        new Thread(resultProcessor).start();

        while (fileIterator.hasNext()) {

            while(resultProcessor.nbServicesRunning == nbThreadsLimit){
                logger.info(resultProcessor.nbServicesRunning+"/"+nbThreadsLimit+" threads running (master waiting for new resources)");
                Thread.sleep(20000);
            }
            
            File f = fileIterator.next();
            flist.add(f);

            if (flist.size() == nbFilesPerChunk || !fileIterator.hasNext()) {
                
                logger.info("Create thread to process chunk: "+count_chunk);
                Callable<CooccEngineResult> worker = new CoOccurrenceEngineTheads(count_chunk, flist, vocabularyIndex, window_token_size, output_dir + "/t_" + count_chunk, max_size_matrix);
                resultProcessor.add(worker);
                
                flist = new ArrayList();
                count_chunk++;
            }

        }
        threadPool.shutdown();

        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            resultProcessor.setActive(false);
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            throw new SLIB_Ex_Critic("Error computing matrix coocurrence: " + e.getMessage());
        }

        logger.info("Number of errors detected: " + resultProcessor.file_processing_errors + "/" + nb_files_to_analyse);
        logger.info("Number of critical errors: " + resultProcessor.critical_errors);
        if (resultProcessor.critical_errors != 0) {
            logger.error("An error occured processing the corpus... please consult the log");
            logger.info("We will try to complete the process...");
        }

        // -----------------------------------------------------------------
        // MERGE tmp matrices
        // -----------------------------------------------------------------
        logger.info("merging " + count_chunk + " tmp matrices");

        // We merge matrix per group of limited size  until no more 
        // matrices have to be merged
        int nb_new_matrices = 0;
        int limit_file_open = 100; // must be > 1 !

        int next_new_matrix_id = count_chunk; // the id of the next matrix
        int id_matrix = 0;

        int nbWords = vocabularyIndex.vocabulary.size();
        List<Integer> sorted_ids = new ArrayList(vocabularyIndex.wordIdToWord.keySet());
        Collections.sort(sorted_ids);

        BufferedReader[] readers;
        CompressedVector[] nextReaderVectors;

        do {
            while (count_chunk != 0) {

                File merged_matrix_dir = new File(output_dir_path + "/t_" + next_new_matrix_id);
                logger.info("next merged matrix: " + merged_matrix_dir);
                merged_matrix_dir.mkdirs();

                // We process a group of matrices an generate associated merged matrix
                try (PrintWriter matrixWriter = new PrintWriter(merged_matrix_dir + "/matrix", "UTF-8")) {

                    // load the buffers required to merge the group of matrices
                    // we also load the first vector of each matrix
                    readers = new BufferedReader[limit_file_open];
                    nextReaderVectors = new CompressedVector[limit_file_open];

                    int min_next_vec_id = Integer.MAX_VALUE;

                    int matrix_group_size = 0; // number of matrices in the group

                    for (int i = 0; i < limit_file_open && count_chunk > 0; i++) {

                        count_chunk--;
                        readers[i] = new BufferedReader(new FileReader(output_dir + "/t_" + id_matrix + "/matrix"));
//                        logger.info("\tadd "+output_dir + "/t_" + id_matrix + "/matrix\t to current group");
                        nextReaderVectors[i] = loadNextCompressedVector(readers[i]);

                        if (nextReaderVectors[i].vector_id < min_next_vec_id) {
                            min_next_vec_id = nextReaderVectors[i].vector_id;
                        }
                        id_matrix++;
                        matrix_group_size++;
                    }
                    logger.info("group size: " + matrix_group_size);

                    // we merge the matrices using the readers
                    // that have been loaded
                    int word_count = 0;
                    for (Integer wordId : sorted_ids) {

                        word_count++;

                        Map<Integer, Double> cv_merged = new HashMap();

                        if (wordId == min_next_vec_id) {

                            min_next_vec_id = Integer.MAX_VALUE;

                            for (int i = 0; i < matrix_group_size; i++) {

                                CompressedVector cv_chunk = nextReaderVectors[i];

                                if (cv_chunk != null && cv_chunk.vector_id == wordId) { // the chunk (matrix) contains information about this vector

                                    // we merge the vectors
                                    for (Map.Entry<Integer, Double> e : cv_chunk.vector.entrySet()) {

                                        int wid = e.getKey();

                                        if (cv_merged.containsKey(wid)) {
                                            cv_merged.put(wid, cv_merged.get(wid) + e.getValue());
                                        } else {
                                            cv_merged.put(wid, e.getValue());
                                        }
                                    }
                                    // vector merged - we update the next vector
                                    nextReaderVectors[i] = loadNextCompressedVector(readers[i]);
                                }
                                if (nextReaderVectors[i] != null && nextReaderVectors[i].vector_id < min_next_vec_id) {
                                    min_next_vec_id = nextReaderVectors[i].vector_id;
                                }
                            }
                        }
                        // else it will be an empty vector and it what we want

                        // we flush the current vector
                        matrixWriter.write(CoOccurrenceEngineTheads.convertVectorMapToString(wordId, cv_merged));

                        // info processing
                        if (word_count % 1000 == 0) {
                            double p = word_count * 100.0 / nbWords;
                            System.out.print("\t" + word_count + "/" + nbWords + "   " + Utils.format2digits(p) + "%\t nb matrices left " + (count_chunk + nb_new_matrices) + "\r");
                        }
                    }
                    System.out.print("\t" + word_count + "/" + nbWords + "   100%\t \r");

                    // close the readers
                    for (int i = 0; i < matrix_group_size; i++) {
                        readers[i].close();
                    }

                    // delete associated files
//                     TODO REMOVE COMMENTS
                    for (int i = id_matrix - matrix_group_size; i < id_matrix; i++) {
                        //ls System.out.println(" delete matrix at matrix " + output_dir + "/t_" + i);
                        //FileUtils.deleteDirectory(new File(output_dir + "/t_" + i));
                    }

                    nb_new_matrices++;
                    System.out.println(" matrices have been merged at matrix " + next_new_matrix_id + " (number of matrices created this iteration: " + nb_new_matrices + ")");
                    System.out.println("-----------------------------");
                    next_new_matrix_id += 1;
                }
            }

            System.out.println("======================================");
            System.out.println(nb_new_matrices + " new matrix generated");
            System.out.println("======================================");

            count_chunk = nb_new_matrices;
            nb_new_matrices = 0;

        } while (count_chunk != 1);

        // Finally we move the last matrix 
        FileUtils.moveFile(new File(output_dir + "/t_" + (next_new_matrix_id - 1) + "/matrix"), new File(output_dir + "/matrix"));
        new File(output_dir + "/t_" + (next_new_matrix_id - 1)).delete();

        logger.info("\nmerging done");
    }

    private CompressedVector loadNextCompressedVector(BufferedReader reader) throws IOException, SLIB_Ex_Critic {

        CompressedVector v = null;

        String line = reader.readLine();

        if (line != null) {

            String[] data = Utils.colon_pattern.split(line.trim());

            int word_id = Integer.parseInt(data[0]);

            String[] data2;
            int word_id_c;
            double occ;

            Map<Integer, Double> map = new HashMap();
            for (int i = 1; i < data.length; i++) {
                data2 = Utils.dash_pattern.split(data[i]);// 30-456

                if (data2.length != 2) {
                    throw new SLIB_Ex_Critic("Cannot extract compressed vector from the following line, expected vec_id:dim_id_1-value_1:dim_id_2:value-2...\nline: " + line + "\terror parsing " + data[i]);
                }
                word_id_c = Integer.parseInt(data2[0]);
                occ = Double.parseDouble(data2[1]);
                map.put(word_id_c, occ);
            }
            v = new CompressedVector(word_id, map);
        }
        return v;
    }

    private class CompressedVector {

        int vector_id;
        Map<Integer, Double> vector;

        public CompressedVector(int vector_id, Map<Integer, Double> vec) {
            this.vector_id = vector_id;
            this.vector = vec;
        }

    }

    /**
     * return null if either the matrix file is empty or the matrix does not
     * contain the vector
     *
     * @param id
     * @param matrix_path
     * @return
     * @throws IOException
     * @deprecated
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

        private int nbServicesRunning;
        private final CompletionService<CooccEngineResult> taskCompletionService;
        int file_processing_errors = 0;
        int critical_errors = 0;
        int nbServicesFinished = 0;

        List<Future<CooccEngineResult>> futures = new ArrayList();
        private boolean isActive;

        public ResultProcessor(CompletionService<CooccEngineResult> taskCompletionService) {
            this.taskCompletionService = taskCompletionService;
        }

        @Override
        public void run() {

            int file_processed = 0;

            while (isActive) {

                while (nbServicesRunning > 0) {

                    try {
                        logger.info(nbServicesRunning + " processes running" + "\t" + nbServicesFinished + " done");
                        CooccEngineResult result = taskCompletionService.take().get();
                        file_processed += result.file_processed;
                        file_processing_errors += result.errors;
                        nbServicesFinished++;
                        nbServicesRunning--;
                        logger.info("*** " + file_processed + "/" + CoOcurrenceEngine.nb_files_processed + ", errors=" + file_processing_errors + "\t(processes running: " + nbServicesRunning + ")");

                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("**** CRITICAL ERROR DETECTED " + e.getMessage());
                        e.printStackTrace();
                        critical_errors++;
                    }
                }
                try {
                    logger.info("WAITING COMPLETION OF RUNNING PROCESSES ("+nbServicesRunning+") TO PROCESS NEW RESULTS");
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage());
                }
            }
        }

        private void add(Callable<CooccEngineResult> worker) {
            nbServicesRunning++;
            futures.add(taskCompletionService.submit(worker));
            
        }

        private void setActive(boolean b) {
            isActive = b;
        }

    }
}
