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

import com.github.sharispe.slib.dsm.core.corpus.Corpus;
import com.github.sharispe.slib.dsm.core.corpus.Document;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIteratorConstraint;
import com.github.sharispe.slib.dsm.utils.RQueue;
import com.github.sharispe.slib.dsm.utils.Utils;
import static com.github.sharispe.slib.dsm.utils.Utils.loadMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
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
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class VocStatComputer {

    private static long nb_files_processed = 0;
    private static long nb_files_to_analyse = 0;

    public final static int DEFAULT_CHUNK_FILE_SIZE = 50000;

    public static final String CORPUS_INDEX = "corpus_index.tsv";
    public static final String CHUNK_INDEX = "chunk_index.tsv";
    public static final String GENERAL_INFO = "info";

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
     * @param corpus the location of the corpus (set of files)
     * @param outputDir the result directory
     * @param wordSizeConstraint the maximal word size (in tokens)
     * @param nbThreads the number of threads allocated to the process
     * @param file_per_threads the number of files a thread must process by
     * iteration
     * @param cache_thread the number of values stored into memory in each
     * threads before being flushed into the disk. This parameters improves
     * computational performances by increasing memory consumption.
     * @param wordIteratorConstraint the word iterator constraints
     * @throws IOException if an IO related error occurs
     * @throws SLIB_Ex_Critic if an error occurs
     * @throws Exception if an error occurs
     */
    public static synchronized void computeVocStats(Corpus corpus, String outputDir, int wordSizeConstraint, WordIteratorConstraint wordIteratorConstraint, int nbThreads, int file_per_threads, int cache_thread) throws IOException, SLIB_Ex_Critic, Exception {
        computeVocStats_inner(corpus, outputDir, wordSizeConstraint, wordIteratorConstraint, null, nbThreads, file_per_threads, cache_thread);
    }

    public static synchronized void computeVocStats(Corpus corpus, String outputDir, String vocabularyFile, int nbThreads, int file_per_threads, int cache_thread) throws IOException, SLIB_Ex_Critic, Exception {
        computeVocStats_inner(corpus, outputDir, 0, null, vocabularyFile, nbThreads, file_per_threads, cache_thread);
    }

    private static synchronized void computeVocStats_inner(Corpus corpus, String outputDir, int wordSizeConstraint, WordIteratorConstraint wordIteratorConstraint, String vocabularyFile, int nbThreads, int file_per_threads, int cache_thread) throws IOException, SLIB_Ex_Critic, Exception {

        logger.info("Computing statistics for corpus: " + corpus);

        boolean use_vocabulary = vocabularyFile != null;
        Vocabulary vocabulary = null;

        if (use_vocabulary) {
            logger.info("vocabulary already defined: " + vocabularyFile);
            vocabulary = new Vocabulary(vocabularyFile);
        } else {
            logger.info("vocabulary will be extracted considering following constraint");
            logger.info("word size constraint (token): " + wordSizeConstraint);
            logger.info("word iterator constraint: " + wordIteratorConstraint);
        }

        logger.info("nb threads: " + nbThreads);
        logger.info("file per threads: " + file_per_threads);
        logger.info("cache per threads: " + cache_thread);

        nb_files_to_analyse = corpus.getSize();

        logger.info("vocabulary index output: " + outputDir);
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

        Iterable<Document> documents = corpus.getDocuments();
        List<Document> docSubset = new ArrayList();

        int c = 0;
        
        for (Document d : documents) {

            c++;
            
            if (docSubset.size() == chunk_size || (c ==  nb_files_to_analyse && !docSubset.isEmpty())) {

                poolWorker.awaitFreeResource();
                poolWorker.addTask();
                Callable<VocStatResult> worker;
                if (use_vocabulary) {
                    worker = new VocStatComputerThreads(poolWorker, count_chunk, docSubset, vocabulary, outputDir + "/tmp", cache_thread);
                } else {
                    worker = new VocStatComputerThreads(poolWorker, count_chunk, docSubset, wordSizeConstraint, wordIteratorConstraint, outputDir + "/tmp", cache_thread);
                }
                count_chunk++;
                poolWorker.getPool().submit(worker);
                docSubset = new ArrayList();
            } else {
                docSubset.add(d);
            }
        }

        poolWorker.shutdown();

        logger.info("number of files processed: " + nb_files_processed);

        // Build the Final index
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

    /**
     * Build an index considering an existing one by only considering words that
     * have a specified minimal number of occurrences
     *
     * @param indexToReduceLocation location of the index to reduce
     * @param reducedIndexLocation location of the reduced index
     * @param minNbOcc min number of occurrences
     * @throws SLIB_Ex_Critic if an error occurs
     * @throws IOException if an IO related error occurs
     */
    public static void reduceIndexUsingNbOcc(String indexToReduceLocation, String reducedIndexLocation, int minNbOcc) throws SLIB_Ex_Critic, IOException {

        logger.info("Reducing index " + indexToReduceLocation + "\t into " + reducedIndexLocation);
        logger.info("Only considering words with at least " + minNbOcc + " occurrences");

        // prepare the new index
        File reducedIndex = new File(reducedIndexLocation);
        reducedIndex.mkdir();

        Map<String, Integer> indexIDs = loadMap(indexToReduceLocation + "/" + CHUNK_INDEX);
        List<String> chunk_index_keys = new ArrayList(indexIDs.keySet());
        Collections.sort(chunk_index_keys, String.CASE_INSENSITIVE_ORDER);

        File reducedIndexFile = new File(reducedIndexLocation + "/" + CHUNK_INDEX);
        int nbWordsTested = 0;
        int nbWordsSelected = 0;

        try (FileWriter fwReducedIndexFile = new FileWriter(reducedIndexFile)) {

            int idFile = 0;
            int c = 0;

            for (String indexKey : chunk_index_keys) {

                long update_nb_word_chunk = 0;
                c++;

                if (c % 10 == 0) {
                    double p = c * 100.0 / chunk_index_keys.size();
                    System.out.print("processing: " + c + "/" + chunk_index_keys.size() + "\t" + Utils.format2digits(p) + "%" + "\r");
                }
                int indexID = indexIDs.get(indexKey);
                BufferedReader br = new BufferedReader(new FileReader(indexToReduceLocation + "/" + indexID));

                int nbWords = 0;

                File reducedChunkFile = new File(reducedIndexLocation + "/" + idFile);

                try (FileWriter fwReducedChunk = new FileWriter(reducedChunkFile)) {

                    String line = br.readLine();

                    String[] data;
                    while (line != null) {

                        nbWordsTested++;
                        data = Utils.tab_pattern.split(line);
                        int nbOccurrences = Integer.parseInt(data[2]);

                        if (nbOccurrences >= minNbOcc) {
                            update_nb_word_chunk++;
                            nbWordsSelected++;
                            nbWords++;
                            fwReducedChunk.write(data[0] + "\t" + Integer.parseInt(data[1]) + "\t" + Integer.parseInt(data[2]) + "\t" + Integer.parseInt(data[3]) + "\t" + data[4] + "\n");
                        }
                        line = br.readLine();
                    }
                }
                if (nbWords == 0) {
                    reducedChunkFile.delete();
                } else {
                    fwReducedIndexFile.write(indexKey + "\t" + idFile + "\t" + update_nb_word_chunk + "\n");
                    idFile++;
                }
            }
            double p = (double) nbWordsSelected * 100.0 / nbWordsTested;
            logger.info(nbWordsSelected + "/" + nbWordsTested + " words selected (" + Utils.format2digits(p) + "%)");

            // Copy other files 
            Files.copy(new File(indexToReduceLocation + "/" + CORPUS_INDEX).toPath(), new File(reducedIndexLocation + "/" + CORPUS_INDEX).toPath());

            VocStatInfo voc_info = new VocStatInfo(indexToReduceLocation + "/" + GENERAL_INFO);
            VocStatInfo voc_info_reduced = new VocStatInfo(nbWordsSelected, voc_info.nbScannedWords, voc_info.nbValidatedScannedWords, voc_info.nbFiles);
            voc_info_reduced.flush(reducedIndexLocation + "/" + GENERAL_INFO);
        }
    }

    /**
     * Build an index considering an existing one by only considering words
     * specified into a given vocabulary. The vocabulary is a file with one word
     * per line.
     *
     * @param indexToReduceLocation location of the index to reduce
     * @param reducedIndexLocation location of the reduced index
     * @param vocabularyLocation location of the vocabulary to consider
     * @throws SLIB_Ex_Critic if an error occurs
     * @throws IOException if an IO related error occurs
     */
    public static void reduceIndexUsingVoc(String indexToReduceLocation, String reducedIndexLocation, String vocabularyLocation) throws SLIB_Ex_Critic, IOException {

        logger.info("Reducing index " + indexToReduceLocation + "\t into " + reducedIndexLocation);
        logger.info("Only considering words defined into vocabulary " + vocabularyLocation);

        Set<String> vocabulary = Utils.loadWords(vocabularyLocation);

        // prepare the new index
        File reducedIndex = new File(reducedIndexLocation);
        reducedIndex.mkdir();

        Map<String, Integer> indexIDs = loadMap(indexToReduceLocation + "/" + CHUNK_INDEX);
        List<String> chunk_index_keys = new ArrayList(indexIDs.keySet());
        Collections.sort(chunk_index_keys, String.CASE_INSENSITIVE_ORDER);

        File reducedIndexFile = new File(reducedIndexLocation + "/" + CHUNK_INDEX);
        int nbWordsTested = 0;
        int nbWordsSelected = 0;

        try (FileWriter fwReducedIndexFile = new FileWriter(reducedIndexFile)) {

            int idFile = 0;
            int c = 0;

            // We process the chunks
            for (String indexKey : chunk_index_keys) {

                int update_nb_word_chunk = 0;
                c++;

                if (c % 10 == 0) {
                    double p = c * 100.0 / chunk_index_keys.size();
                    System.out.print("processing: " + c + "/" + chunk_index_keys.size() + "\t" + Utils.format2digits(p) + "%" + "\r");
                }
                int indexID = indexIDs.get(indexKey);
                BufferedReader br = new BufferedReader(new FileReader(indexToReduceLocation + "/" + indexID));

                int nbWords = 0;

                File reducedChunkFile = new File(reducedIndexLocation + "/" + idFile);

                try (FileWriter fwReducedChunk = new FileWriter(reducedChunkFile)) {

                    String line = br.readLine();

                    String[] data;
                    while (line != null) {

                        nbWordsTested++;
                        data = Utils.tab_pattern.split(line);
                        String label = data[0];

                        if (vocabulary.contains(label)) {
                            update_nb_word_chunk++;
                            nbWordsSelected++;
                            nbWords++;
                            fwReducedChunk.write(data[0] + "\t" + Integer.parseInt(data[1]) + "\t" + Integer.parseInt(data[2]) + "\t" + Integer.parseInt(data[3]) + "\t" + data[4] + "\n");
                        }
                        line = br.readLine();
                    }
                }
                if (nbWords == 0) {
                    reducedChunkFile.delete();
                } else {
                    fwReducedIndexFile.write(indexKey + "\t" + idFile + "\t" + update_nb_word_chunk + "\n");
                    idFile++;
                }
            }
            double p = (double) nbWordsSelected * 100.0 / nbWordsTested;
            logger.info(nbWordsSelected + "/" + nbWordsTested + " words selected (" + Utils.format2digits(p) + "%)");

            // Copy other files 
            Files.copy(new File(indexToReduceLocation + "/" + CORPUS_INDEX).toPath(), new File(reducedIndexLocation + "/" + CORPUS_INDEX).toPath());

            VocStatInfo voc_info = new VocStatInfo(indexToReduceLocation + "/" + GENERAL_INFO);
            VocStatInfo voc_info_reduced = new VocStatInfo(nbWordsSelected, voc_info.nbScannedWords, voc_info.nbValidatedScannedWords, voc_info.nbFiles);
            voc_info_reduced.flush(reducedIndexLocation + "/" + GENERAL_INFO);
        }
    }

    /**
     * Merge the given statistics computed from different corpus. Note that
     * statistics are assumed to be computed on different corpus that do not
     * contains the same files (if a word occurs 23 times into corpus A and 40
     * times into corpus B it will be considered to occur 63 times in the merged
     * corpus). To avoid high computational time complexity we consider that two
     * indexes do not contains the same file.
     *
     * @param indexes the set of paths of the index to merge
     * @param deleteMerged true to delete merged indexes
     * @param mergedIndexLocation the merged index location
     * @throws Exception if an error occurs
     */
    public static void mergeIndexes(Set<String> indexes, boolean deleteMerged, String mergedIndexLocation) throws Exception {

        LinkedHashSet<String> pathIndexesToMerge = new LinkedHashSet(indexes);
        logger.info("Merging " + pathIndexesToMerge.size() + " indexes");
        logger.info("Computing key indexes");

        File mergedIndex = new File(mergedIndexLocation);
        mergedIndex.mkdir();

        // retrieve all the key indexes
        Set<String> chunkKeys = new HashSet();

        int voc_size = 0;
        long totalScanWords = 0;
        long totalValidatedScanWords = 0;
        int totalFile = 0;

        // In each index file numbering starts from 0
        // a new id is therefore computed for each file of the merged index
        // to avoid high computational time complexity we consider that two 
        // indexes do not contains the same file
        Map<String, Integer> valuesToAddToCorpusFileIds = new HashMap();
        int nbFiles = 0;

        // we generate the new corpus index
        // and we retrive all the chunk keys
        try (FileWriter corpusIndexWriter = new FileWriter(mergedIndexLocation + "/" + CORPUS_INDEX)) {

            for (String idx : pathIndexesToMerge) {

                logger.info("Loading index: " + idx);
                Map<String, Integer> chunksToMerge = Utils.loadMap(idx + "/" + CHUNK_INDEX);
                chunkKeys.addAll(chunksToMerge.keySet());

                VocStatInfo idxInfo = new VocStatInfo(idx + "/" + GENERAL_INFO);
                totalFile += idxInfo.nbFiles;
                totalScanWords += idxInfo.nbScannedWords;
                totalValidatedScanWords += idxInfo.nbValidatedScannedWords;

                Map<Integer, String> fileIdsToMerge = Utils.loadMap_IntString(idx + "/" + CORPUS_INDEX);

                for (Map.Entry<Integer, String> entrySet : fileIdsToMerge.entrySet()) {
                    Integer key = entrySet.getKey();
                    String value = entrySet.getValue();
                    corpusIndexWriter.write((key + nbFiles) + "\t" + value + "\n");
                }
                valuesToAddToCorpusFileIds.put(idx, nbFiles);
                logger.info(idx + " - add " + nbFiles + " to corpus file ids");
                nbFiles += fileIdsToMerge.size();

            }
        }
        logger.info("Size merged index: " + chunkKeys.size());

        List<String> indexKeysSorted = new ArrayList(chunkKeys);
        Collections.sort(indexKeysSorted, String.CASE_INSENSITIVE_ORDER);

        // map used to store the information of the words for the merged indexes
        try (FileWriter fileIdx = new FileWriter(mergedIndexLocation + "/" + CHUNK_INDEX)) {
            // map used to store the information of the words for the merged indexes
            Map<String, WordInfo> mergedIdxKeyWordsInfo = new HashMap();

            logger.info("Building merged index");

            for (int i = 0; i < indexKeysSorted.size(); i++) {

                // clear info associated to another key
                mergedIdxKeyWordsInfo.clear();

                String key = indexKeysSorted.get(i);

                logger.info("processing " + (i + 1) + "/" + indexKeysSorted.size() + "\t" + key);

                // for each key we merge the all the indexes
                for (String idx : pathIndexesToMerge) {

                    Map<String, Integer> idxIndexKeys = loadMap(idx + "/" + CHUNK_INDEX);

                    // the file that may contain the statistics of the words starting with the current key
                    String idxKeyChunk = idx + "/" + idxIndexKeys.get(key);

                    if (new File(idxKeyChunk).exists()) {

                        Map<String, WordInfo> idxKeyWordsInfo = MapIndexer.loadMapWordInfo(new File(idxKeyChunk));

                        for (String m : idxKeyWordsInfo.keySet()) {

                            WordInfo winfo = idxKeyWordsInfo.get(m);

                            // compute the new string of document ids 
                            String newFileInfo = "";
                            String[] fileInfos = winfo.additionnalInfo.split(":"); // 5-1:6-3:7-5... file_id_1-nb_occ_file_1:file_id_2-nb_occ_file_2

                            for (int j = 0; j < fileInfos.length; j++) {
                                String[] infoFile = fileInfos[j].split("-");
                                int idFile = Integer.parseInt(infoFile[0]) + valuesToAddToCorpusFileIds.get(idx);
                                if (j != 0) {
                                    newFileInfo += ":";
                                }
                                newFileInfo += idFile + "-" + infoFile[1];

                            }

                            if (mergedIdxKeyWordsInfo.containsKey(m)) {
                                mergedIdxKeyWordsInfo.get(m).sumWordInfo(winfo);
                                // concat additional information
                                mergedIdxKeyWordsInfo.get(m).concatAdditionnalInfo(newFileInfo);
                            } else {
                                mergedIdxKeyWordsInfo.put(m, winfo);
                                // additional info may have been changed
                                mergedIdxKeyWordsInfo.get(m).additionnalInfo = newFileInfo;
                            }
                        }
                    }
                }

                fileIdx.write(key + "\t" + i + "\t" + mergedIdxKeyWordsInfo.size() + "\n");

                // Flush map and n-gram size
                // n-gram size_n-gram nb_occurrences
                try (FileWriter file = new FileWriter(mergedIndexLocation + "/" + i)) {

                    for (Map.Entry<String, WordInfo> e : mergedIdxKeyWordsInfo.entrySet()) {
                        file.write(e.getKey() + "\t" + Utils.blank_pattern.split(e.getKey()).length + "\t" + e.getValue().nbOccurrences + "\t" + e.getValue().nbFilesWithWord + "\t" + e.getValue().additionnalInfo + "\n");
                    }
                }
                voc_size += mergedIdxKeyWordsInfo.size();
            }
        }

        VocStatInfo mergedInfo = new VocStatInfo(voc_size, totalScanWords, totalValidatedScanWords, totalFile);
        mergedInfo.flush(mergedIndexLocation + "/" + GENERAL_INFO);

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

        Map<String, Integer> indexFiles = Utils.loadMap(dir + "/" + CHUNK_INDEX);

        logger.info("computing global statistics...");
        int c = 0;
        for (Integer i : indexFiles.values()) {
            c++;

            File chunkFile = new File(dir + "/" + i);
            logger.info("processing chunk " + c + "/" + indexFiles.size() + "\t" + chunkFile + "\r");
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

            RQueue<String, Double> bestProbabilities = new RQueue(nbResults);

            logger.info("Computing ngrams with best probabilities");

            Map<Integer, Integer> wordsWithNbOccurences = new HashMap();

            double total_words = 0;

            for (Integer i : indexFiles.values()) {

                File chunk = new File(dir + "/" + i);

                // load the statistics associated to the prefix
                List<NgramInfo> ngramInfo = loadListNgramInfo(chunk);

                for (NgramInfo info : ngramInfo) {

                    if (info.size == size_ngram) {
                        double p = info.nbOccurrences / (double) nb_occ;
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

    public static void computePMI(String dir) throws Exception {

        Map<Integer, Integer> sizeVoc_NGRAM = new HashMap();
        Map<Integer, Integer> nbOcc_NGRAM = new HashMap();

        Map<String, Integer> indexFiles = Utils.loadMap(dir + "/" + CHUNK_INDEX);

        logger.info("computing global statistics...");
        int c = 0;
        for (Integer i : indexFiles.values()) {
            c++;

            File chunkFile = new File(dir + "/" + i);
            System.out.print("processing chunk " + c + "/" + indexFiles.size() + "\r");
            List<NgramInfo> ngramInfo = loadListNgramInfo(chunkFile);

            for (NgramInfo info : ngramInfo) {

                if (!sizeVoc_NGRAM.containsKey(info.size)) {
                    sizeVoc_NGRAM.put(info.size, 0);
                    nbOcc_NGRAM.put(info.size, 0);
                }

                sizeVoc_NGRAM.put(info.size, sizeVoc_NGRAM.get(info.size) + 1);
                nbOcc_NGRAM.put(info.size, nbOcc_NGRAM.get(info.size) + info.nbOccurrences);
            }
        }
        // sort keys 
        List<Integer> sortedKeys = new ArrayList(sizeVoc_NGRAM.keySet());
        Collections.sort(sortedKeys);

        // computing statistics for simple token
        logger.info("computing probabilities of single tokens...");
        int nb_occ_n1 = nbOcc_NGRAM.get(1);

        Map<String, Double> probabilityToken = new HashMap();
        RQueue<String, Double> bestProbabilityToken = new RQueue(10000);

        for (Integer i : indexFiles.values()) {

            File chunk = new File(dir + "/" + i);

            // load the statistics associated to the prefix
            List<NgramInfo> ngramInfo = loadListNgramInfo(chunk);

            for (NgramInfo info : ngramInfo) {

                if (info.size == 1) {
                    double p = (double) info.nbOccurrences / (double) nb_occ_n1;
                    bestProbabilityToken.add(info.ngram, p);
                    probabilityToken.put(info.ngram, p);
                }
            }
        }

        logger.info(bestProbabilityToken.toString());

        logger.info("computing probabilities of 2-grams and PMI...");
        // compute pmi for each 2-gram size 

        int nb_occ_n2 = nbOcc_NGRAM.get(2);
        int nb_ngram_n2 = sizeVoc_NGRAM.get(2);

        RQueue<String, Double> higherPMI = new RQueue(100);
        RQueue<String, Double> lowestPMI = new RQueue(100, false);
        RQueue<String, Double> allPMI = new RQueue(nb_ngram_n2);

        logger.info("Computing ngrams with best PMI");

        for (Integer i : indexFiles.values()) {

            File chunk = new File(dir + "/" + i);

            // load the statistics associated to the prefix
            List<NgramInfo> ngramInfo = loadListNgramInfo(chunk);

            for (NgramInfo info : ngramInfo) {

                if (info.size == 2) {

                    String[] twoGram = Utils.blank_pattern.split(info.ngram);

                    String u = twoGram[0];
                    String v = twoGram[1];

                    double p_uv = (double) info.nbOccurrences / (double) nb_occ_n2;
                    double p_u = probabilityToken.get(u);
                    double p_v = probabilityToken.get(v);

                    double pmi = Math.log(p_uv / (p_u * p_v));

                    higherPMI.add(info.ngram, pmi);
                    lowestPMI.add(info.ngram, pmi);
                    allPMI.add(info.ngram, pmi);
                }
            }
        }
        logger.info("Best PMI");
        logger.info(higherPMI.toString());
        logger.info("Lowest PMI");
        logger.info(lowestPMI.toString());
        logger.info("All PMI");
        logger.info(allPMI.toString());
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

                if (data.length != 5) {
                    logger.info("skip: " + line);
                } else {
                    list.add(new NgramInfo(data[0], Integer.parseInt(data[1]), Integer.parseInt(data[2]), Integer.parseInt(data[3])));
                }
                line = br.readLine();
            }

        }
        return list;
    }

}
