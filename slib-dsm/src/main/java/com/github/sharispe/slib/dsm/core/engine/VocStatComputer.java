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

import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIteratorConstraint;
import com.github.sharispe.slib.dsm.utils.KBestQueue;
import com.github.sharispe.slib.dsm.utils.Utils;
import static com.github.sharispe.slib.dsm.utils.Utils.loadMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.threads.PoolWorker;
import slib.utils.threads.ThreadManager;

/**
 * This class is used to compute statistics about a vocabulary considering a
 * specific corpora. The vocabulary to consider may be computed (specifying
 * constraints on the word token size) or given as input. Considering this
 * vocabulary the class provides utility method to compute the number of files
 * into which the word occurs as well as the number of occurrences. Global
 * corpus statistics are also stored, e.g. number of files, size of the
 * vocabulary... The statistics associated to a vocabulary are therefore always
 * associated to those corpora informations.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class VocStatComputer {

    private static int nb_files_processed = 0;
    private static int nb_files_to_analyse = 0;

    public final static int DEFAULT_CHUNK_FILE_SIZE = 50000;

    public static final String FILE_INDEX = "file_index.tsv";
    public static final String FILE_VOC_INFO = "info";

    private static final Object lock_nb_files_processed = new Object();

    public static void incrementProcessedFiles(int i) {
        synchronized (lock_nb_files_processed) {
            nb_files_processed += i;
        }
    }

    final static Logger logger = LoggerFactory.getLogger(VocStatComputer.class);

    /**
     * Compute word statistics considering a given corpus and specific
     * constraint on words. Considering a specified maximal token size n, the
     * method computes statistics for each words occurring into the corpus that
     * have a token size lower or equal than n (a token is a sequence of
     * characters that are different to the space character, e.g. "machine
     * learning" is a word composed of two tokens "machine" and "learning").
     * During the process each file located in the directory will be processed
     * (recursive process). Statistics will be stored into the given directory.
     * Refer to the class documentation for more information about the format
     * used for storing the statistics.
     *
     * @param corpusDir the location of the corpus (set of files)
     * @param outputDir the result directory
     * @param wordSizeConstraint the maximal word size (in tokens)
     * @param nbThreads the number of threads allocated to the process
     * @param file_per_threads the number of files a thread must process by
     * iteration
     * @param cache_thread the number of values stored into memory in each
     * threads before being flushed into the disk. This parameters improves
     * computational performances by increasing memory consumption.
     * @return the vocabulary and associated statistics.
     * @throws IOException
     * @throws SLIB_Ex_Critic
     * @throws Exception
     */
    public static synchronized Voc computeVocStats(String corpusDir, String outputDir, int wordSizeConstraint, int nbThreads, int file_per_threads, int cache_thread, WordIteratorConstraint wordIteratorConstraint) throws IOException, SLIB_Ex_Critic, Exception {

        logger.info("Computing statistics for directory: " + corpusDir);
        logger.info("word size constraint (token): " + wordSizeConstraint);
        logger.info("word iterator constraint: " + wordIteratorConstraint);
        logger.info("nb threads: " + nbThreads);
        logger.info("file per threads: " + file_per_threads);
        logger.info("cache per threads: " + cache_thread);

        nb_files_to_analyse = countNbFiles(corpusDir);

        logger.info("vocabulary output: " + outputDir);
        logger.info("Number of files: " + nb_files_to_analyse);

        nb_files_processed = 0;

        int chunk_size = file_per_threads;
        if (chunk_size < 1) {
            chunk_size = DEFAULT_CHUNK_FILE_SIZE;
        }

        logger.info("chunk size " + chunk_size);

        int count_chunk = 0;

        ThreadManager threadManager = ThreadManager.getSingleton();
        threadManager.setMaxThread(nbThreads);

        PoolWorker poolWorker = threadManager.getMaxLoadPoolWorker();

        Iterator<File> fileIterator = FileUtils.iterateFiles(new File(corpusDir), TrueFileFilter.TRUE, TrueFileFilter.INSTANCE);

        while (fileIterator.hasNext()) {

            poolWorker.awaitFreeResource();

            List<File> flist = new ArrayList();
            while (fileIterator.hasNext() && flist.size() != chunk_size) {
                File n = fileIterator.next();
//                logger.info("queuing: " + n.toString());
                flist.add(n);
            }
            poolWorker.addTask();
            Callable<VocStatResult> worker = new VocStatComputerThreads(poolWorker, count_chunk, flist, wordSizeConstraint, outputDir + "/tmp", cache_thread, wordIteratorConstraint);
            count_chunk++;
            poolWorker.getPool().submit(worker);
        }

        poolWorker.shutdown();

        logger.info("number of files processed: " + nb_files_processed);

        if (count_chunk == 1) { // we only move the content
            File dstDir = new File(outputDir);
            for (File srcFile : new File(outputDir + "/tmp/t_0").listFiles()) {
                if (srcFile.isDirectory()) {
                    FileUtils.copyDirectoryToDirectory(srcFile, dstDir);
                } else {
                    FileUtils.copyFileToDirectory(srcFile, dstDir);
                }
            }
        } else { // we merge the chunks
            logger.info("Finalizing, merging " + count_chunk + " subindexes");
            Set<String> indexToMerge = new HashSet();

            for (int i = 0; i < count_chunk; i++) {
                indexToMerge.add(outputDir + "/tmp/t_" + i);
            }
            mergeIndexes(indexToMerge, true, outputDir);
        }

        // remove tmp location
        logger.info("Removing tmp directory");
        FileUtils.deleteDirectory(new File(outputDir + "/tmp/"));
        logger.info("Index computed.");
        return null;
    }

    /**
     * @return the number of files already processed.
     */
    public static int getNbFilesProcessed() {
        return nb_files_processed;
    }

    /**
     * @return the total number of files to analyse.
     */
    public static int getNbFilesToAnalyse() {
        return nb_files_to_analyse;
    }

    /**
     *
     * @param indexToReduceLocation
     * @param reducedIndexLocation
     * @param minNbOcc
     * @throws SLIB_Ex_Critic
     * @throws IOException
     */
    @Deprecated
    public static void reduceIndex(String indexToReduceLocation, String reducedIndexLocation, int minNbOcc) throws SLIB_Ex_Critic, IOException {

        logger.info("Reducing index " + indexToReduceLocation + "\t into " + reducedIndexLocation + " only considering words with at least " + minNbOcc + " occurrences");
        Map<String, Integer> indexIDs = loadMap(indexToReduceLocation + "/" + FILE_INDEX);

        File reducedIndex = new File(reducedIndexLocation);
        reducedIndex.mkdir();

        List<String> indexes = new ArrayList(indexIDs.keySet());
        Collections.sort(indexes, String.CASE_INSENSITIVE_ORDER);

        File reducedIndexFile = new File(reducedIndexLocation + "/" + FILE_INDEX);
        int nbWordsTested = 0;
        int nbWordsSelected = 0;

        try (FileWriter fwReducedIndexFile = new FileWriter(reducedIndexFile)) {

            int idFile = 0;
            int c = 0;

            for (String indexKey : indexes) {

                c++;

                logger.info(c + "/" + indexes.size());

                int indexID = indexIDs.get(indexKey);
                BufferedReader br = new BufferedReader(new FileReader(indexToReduceLocation + "/" + indexID));

                File reducedIndexChunk = new File(reducedIndexLocation + "/" + idFile);
                FileWriter brChunk = new FileWriter(reducedIndexChunk);

                int nbWords = 0;

                try {
                    String line = br.readLine();

                    String[] data;
                    while (line != null) {

                        nbWordsTested++;
                        data = Utils.tab_pattern.split(line);
                        NgramInfo nInfo = new NgramInfo(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3]));

                        if (nInfo.nbOccurrences >= minNbOcc) {
                            nbWordsSelected++;
                            nbWords++;
                            brChunk.write(nInfo.ngram + "\t" + nInfo.size + "\t" + nInfo.nbOccurrences + "\t" + nInfo.nbFilesWithNgram + "\n");
                        }
                        line = br.readLine();
                    }

                } finally {
                    br.close();
                    brChunk.close();
                }

                if (nbWords == 0) {
                    reducedIndexChunk.delete();
                } else {
                    fwReducedIndexFile.write(indexKey + "\t" + idFile);
                    idFile++;
                }

            }
        }
        double p = (double) nbWordsSelected * 100.0 / nbWordsTested;
        logger.info(nbWordsSelected + "/" + nbWordsTested + " words selected (" + p + "%)");

    }

    /**
     * Merge the given statistics computed from different corpus. Note that
     * statistics are assumed to be computed on different corpus that do not
     * contains the same files (if a word occurs 23 times into corpus A and 40
     * times into corpus B it will be considered to occur 63 times in the merged
     * corpus).
     *
     * @param pathIndexesToMerge the set of paths of the index to merge
     * @param deleteMerged true to delete merged indexes
     * @param mergedIndexLocation the merged index location
     * @throws SLIB_Ex_Critic
     * @throws IOException
     * @throws Exception
     */
    public static void mergeIndexes(Set<String> pathIndexesToMerge, boolean deleteMerged, String mergedIndexLocation) throws Exception {

        logger.info("Merging " + pathIndexesToMerge.size() + " indexes");
        logger.info("Computing key indexes");

        File mergedIndex = new File(mergedIndexLocation);
        mergedIndex.mkdir();

        // retrieve all the key indexes
        Set<String> indexKeys = new HashSet();

        int totalWordNumbers = 0;
        int totalFile = 0;

        for (String idx : pathIndexesToMerge) {
            logger.info("Loading index: " + idx);
            Map<String, Integer> indexToMerge = Utils.loadMap(idx + "/" + FILE_INDEX);
            indexKeys.addAll(indexToMerge.keySet());

            VocInfo idxInfo = new VocInfo(idx + "/" + FILE_VOC_INFO);
            totalFile += idxInfo.nbFiles;
        }
        logger.info("Size merged index: " + indexKeys.size());

        List<String> indexKeysSorted = new ArrayList(indexKeys);
        Collections.sort(indexKeysSorted, String.CASE_INSENSITIVE_ORDER);

        // map used to store the information of the words for the merged indexes
        try (FileWriter fileIdx = new FileWriter(mergedIndexLocation + "/" + FILE_INDEX)) {
            // map used to store the information of the words for the merged indexes
            Map<String, WordInfo> mergedIdxKeyWordsInfo = new HashMap();

            logger.info("Building merged index");

            for (int i = 0; i < indexKeysSorted.size(); i++) {

                // clear info associated to another key
                mergedIdxKeyWordsInfo.clear();

                String key = indexKeysSorted.get(i);
                fileIdx.write(key + "\t" + i + "\n");

                logger.info("processing " + (i + 1) + "/" + indexKeysSorted.size() + "\t" + key);

                // for each key we merge the all the indexes
                for (String idx : pathIndexesToMerge) {

                    Map<String, Integer> idxIndexKeys = loadMap(idx + "/" + FILE_INDEX);

                    // the file that may contain the statistics of the words starting with the current key
                    String idxKeyChunk = idx + "/" + idxIndexKeys.get(key);

                    if (new File(idxKeyChunk).exists()) {

                        Map<String, WordInfo> idxKeyWordsInfo = MapIndexer.loadMapWordInfo(new File(idxKeyChunk));

                        for (String m : idxKeyWordsInfo.keySet()) {

                            if (mergedIdxKeyWordsInfo.containsKey(m)) {
                                mergedIdxKeyWordsInfo.get(m).sumWordInfo(idxKeyWordsInfo.get(m));
                            } else {
                                mergedIdxKeyWordsInfo.put(m, idxKeyWordsInfo.get(m));
                            }
                        }
                    }
                }

                // Flush map and n-gram size
                // n-gram size_n-gram nb_occurrences
                try (FileWriter file = new FileWriter(mergedIndexLocation + "/" + i)) {

                    for (Map.Entry<String, WordInfo> e : mergedIdxKeyWordsInfo.entrySet()) {
                        file.write(e.getKey() + "\t" + Utils.blank_pattern.split(e.getKey()).length + "\t" + e.getValue().nbOccurrences + "\t" + +e.getValue().nbFilesWithWord + "\n");
                    }
                }
                totalWordNumbers += mergedIdxKeyWordsInfo.size();
            }
        }

        VocInfo mergedInfo = new VocInfo(totalWordNumbers, totalFile);
        mergedInfo.flush(mergedIndexLocation + "/" + FILE_VOC_INFO);

        if (deleteMerged) {
            for (String idx : pathIndexesToMerge) {
                logger.info("Removing index: " + idx);
                FileUtils.deleteDirectory(new File(idx));
            }
        }
    }

    public static void computeStat(String dir, int nbResults) throws SLIB_Ex_Critic, Exception {

        Map<Integer, Integer> sizeVoc_NGRAM = new HashMap();
        Map<Integer, Integer> nbOcc_NGRAM = new HashMap();
        Map<Integer, Integer> nb_unique_NGRAM = new HashMap();

        Map<String, Integer> indexFiles = Utils.loadMap(dir + "/" + FILE_INDEX);

        logger.info("computing global statistics...");
        int c = 0;
        for (Integer i : indexFiles.values()) {
            c++;

            File chunkFile = new File(dir + "/" + i);
            logger.info("\tprocessing chunk " + c + "/" + indexFiles.size() + "\t" + chunkFile);
            List<NgramInfo> ngramInfo = loadListNgramInfo(chunkFile);

            for (NgramInfo info : ngramInfo) {

                if (!sizeVoc_NGRAM.containsKey(info.size)) {
                    sizeVoc_NGRAM.put(info.size, 0);
                    nbOcc_NGRAM.put(info.size, 0);
                    nb_unique_NGRAM.put(info.size, 0);
                }

                sizeVoc_NGRAM.put(info.size, sizeVoc_NGRAM.get(info.size) + 1);
                nbOcc_NGRAM.put(info.size, nbOcc_NGRAM.get(info.size) + info.nbOccurrences);

                if (info.nbOccurrences == 1) {
                    nb_unique_NGRAM.put(info.size, nb_unique_NGRAM.get(info.size) + 1);
                }
            }
        }
        // sort keys 
        List<Integer> sortedKeys = new ArrayList(sizeVoc_NGRAM.keySet());
        Collections.sort(sortedKeys);

        // compute statistics for each n-gram size 
        for (Integer size_ngram : sortedKeys) {

            int nb_ngram = sizeVoc_NGRAM.get(size_ngram);
            int nb_occ = nbOcc_NGRAM.get(size_ngram);
            int nb_unique = nb_unique_NGRAM.get(size_ngram);
            int nb_not_unique = nb_ngram - nb_unique;
            int p_unique = nb_unique * 100 / nb_ngram;
            int p_not_unique = nb_not_unique * 100 / nb_ngram;

            logger.info("------------------------------------");
            logger.info("size n-gram (" + size_ngram + "): " + nb_ngram);
            logger.info("nbOcc: " + nb_occ);
            logger.info("nb  unique: " + nb_unique + " (" + p_unique + "%)");
            logger.info("nb !unique: " + nb_not_unique + " (" + p_not_unique + "%)");
            
            KBestQueue<String, Double> bestProbabilities = new KBestQueue(nbResults);

            logger.info("Computing ngrams with best probabilities");

            Map<Integer, Integer> wordsWithNbOccurences = new HashMap();

            double total_words = 0;

            for (Integer i : indexFiles.values()) {

                File chunk = new File(dir + "/" + i);

                // load the statistics associated to the prefix
                List<NgramInfo> ngramInfo = loadListNgramInfo(chunk);

                for (NgramInfo info : ngramInfo) {

                    if (info.size == size_ngram) {
                        double p = info.nbOccurrences / (double) nb_ngram;
                        bestProbabilities.add(info.ngram, p);
                        total_words++;

                        if (!wordsWithNbOccurences.containsKey(info.nbOccurrences)) {
                            wordsWithNbOccurences.put(info.nbOccurrences, 1);
                        } else {
                            wordsWithNbOccurences.put(info.nbOccurrences, wordsWithNbOccurences.get(info.nbOccurrences) + 1);
                        }
                    }
                }
            }
            logger.info("Best probabilities ngram size: " + size_ngram);
            logger.info(bestProbabilities.toString());

            List<Integer> sortedKeysNbOcc = new ArrayList(wordsWithNbOccurences.keySet());
            Collections.sort(sortedKeysNbOcc);

            logger.info("Distribution");

            logger.info("nbOcc\tnbWords");
            double current_sum = 0;
            for (int i = sortedKeysNbOcc.size() - 1; i >= 0; i--) {

                int j = sortedKeysNbOcc.get(i);

                current_sum += wordsWithNbOccurences.get(j);
                double p = current_sum * 100.0 / total_words;
                logger.info(j + "\t" + wordsWithNbOccurences.get(j) + "\t" + current_sum + "\t" + p + "%\t");
            }
        }
    }

    private static List<NgramInfo> loadListNgramInfo(File ngramInfoFile) throws Exception {

        List<NgramInfo> list = new ArrayList();
        if (!ngramInfoFile.exists()) {
            return list;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(ngramInfoFile))) {
            String line = br.readLine();

            String[] data;
            while (line != null) {
                data = Utils.tab_pattern.split(line);

                if (data.length != 4) {
                    logger.info("skip: " + line);
                } else {
                    list.add(new NgramInfo(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])));
                }
                line = br.readLine();
            }

        }
        return list;
    }

    public static int countNbFiles(String corpusDir) throws IOException {
        return countNbFiles(FileSystems.getDefault().getPath(corpusDir));
    }

    /**
     * Counts the number of files located into a given directory.
     *
     * @param corpusDir location of the corpus
     * @return the number of files the corpus contains
     * @throws IOException
     */
    private static int countNbFiles(Path corpusDir) throws IOException {

        int count = 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(corpusDir)) {
            for (Path p : ds) {
                count++;
                if (Files.isDirectory(p)) {
                    count += countNbFiles(p);
                }
            }
        }
        return count;
    }

}
