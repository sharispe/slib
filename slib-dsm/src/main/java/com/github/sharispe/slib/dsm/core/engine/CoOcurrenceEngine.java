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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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

    public static final int WINDOW_SIZE_LEFT = 30;
    public static final int WINDOW_SIZE_RIGHT = 30;

    private Voc vocIndex;
    Pattern blank_pattern = Pattern.compile("\\s+");

    public CoOcurrenceEngine(Voc voc_index) {
        this.vocIndex = voc_index;
    }

    
    /**
     *
     * @param directory
     * @param admittedExtensions
     * @return
     * @throws IOException
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
     * @param files
     * @param nbThreads
     * @return
     * @throws slib.utils.ex.SLIB_Ex_Critic
     */
    public SparseMatrix computeCoOcurrence(Collection<File> files, int nbThreads) throws SLIB_Exception {

        int vocsize = vocIndex.size();
        // The word cocccurence matrix will be access by numerous threads
        SparseMatrix wordCoocurences = SparseMatrixGenerator.buildConcurrentSparseMatrix(vocsize, vocsize);

        ExecutorService threadPool = Executors.newFixedThreadPool(nbThreads);
        CompletionService<CooccEngineResult> taskCompletionService = new ExecutorCompletionService(threadPool);

        List<Future<CooccEngineResult>> futures = new ArrayList();

        logger.info("Number of files: " + files.size());
        logger.info("Vocabulary contains: " + vocsize);

        logger.info("processing " + files.size() + " files");

        List<File> flist = new ArrayList();
        int chunk_size = files.size() / nbThreads;
        if (chunk_size > 10000) {
            chunk_size = 10000;
        }

        logger.info("chunk size " + chunk_size);

        int count_chunk = 0;

        for (File f : files) {

            flist.add(f);

            if (flist.size() == chunk_size) {
                Callable<CooccEngineResult> worker = new CoOccurrenceEngineTheads(count_chunk, flist, vocIndex, wordCoocurences);
                futures.add(taskCompletionService.submit(worker));
                flist = new ArrayList();
                count_chunk++;
            }

        }
        if (!flist.isEmpty()) {
            Callable<CooccEngineResult> worker = new CoOccurrenceEngineTheads(count_chunk, flist, vocIndex, wordCoocurences);
            futures.add(taskCompletionService.submit(worker));
            count_chunk++;
        }
        threadPool.shutdown();

        ResultProcessor resultProcessor = new ResultProcessor(futures.size(), taskCompletionService, files.size());
        Thread result_thread = new Thread(resultProcessor);
        result_thread.start();

        try {
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            throw new SLIB_Ex_Critic("Error compute matrix coocurrence: " + e.getMessage());
        }

        logger.info("Number of errors detect: " + resultProcessor.file_processing_errors + "/" + files.size());
        if (resultProcessor.critical_errors != 0) {
            throw new SLIB_Ex_Critic("An error occured processing the corpus... please consult the log");
        }

        logger.info("done");
        return wordCoocurences;
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
