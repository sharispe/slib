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

import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIterator;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIteratorAccessor;
import com.github.sharispe.slib.dsm.core.engine.wordIterator.WordIteratorConstraint;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.utils.threads.PoolWorker;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class VocStatComputerThreads implements Callable<VocStatResult> {

    PoolWorker poolWorker;
    List<File> files;
    int max_size_word;
    int id;
    MapIndexer indexer;
    String rootPath;
    WordIteratorConstraint wordIteratorConstraint;
    final Vocabulary vocabulary;

    public final static int DEFAULT_CACHE_MAP_SIZE = 1000000;
    final int cache_map_size;

    final static Logger logger = LoggerFactory.getLogger(VocStatComputerThreads.class);

    VocStatComputerThreads(PoolWorker poolWorker, int id, List<File> flist, int max_size_word, WordIteratorConstraint wordIteratorConstraint, String dir_root_index) {
        this(poolWorker, id, flist, max_size_word, wordIteratorConstraint, dir_root_index, DEFAULT_CACHE_MAP_SIZE);
    }

    VocStatComputerThreads(PoolWorker poolWorker, int id, List<File> flist, int max_size_word, WordIteratorConstraint wordIteratorConstraint, String dir_root_index, int cache_thread) {

        this.poolWorker = poolWorker;
        this.id = id;
        this.files = flist;
        this.max_size_word = max_size_word;
        this.rootPath = dir_root_index + "/t_" + id;
        this.indexer = new MapIndexer(rootPath, id + "");
        this.cache_map_size = cache_thread;
        this.wordIteratorConstraint = wordIteratorConstraint;
        this.vocabulary = null;
    }

    VocStatComputerThreads(PoolWorker poolWorker, int id, List<File> flist, final Vocabulary vocabulary, String dir_root_index, int cache_thread) {

        this.poolWorker = poolWorker;
        this.id = id;
        this.files = flist;
        this.vocabulary = vocabulary;
        this.rootPath = dir_root_index + "/t_" + id;
        this.indexer = new MapIndexer(rootPath, id + "");
        this.cache_map_size = cache_thread;
    }

    @Override
    public VocStatResult call() throws Exception {

        try {

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            logger.info("(" + id + ") computation initiated " + dateFormat.format(new Date()));
            logger.info("(" + id + ") files: " + files.size());

            long wordScanned = 0;
            long validateWordScanned = 0;

            int nbFileDone = 0, nbFileDoneLastIteration = 0;
            WordIterator wordIT;
            Map<String, Integer> fileVoc;
            String w;

            Map<String, WordInfo> wordOccurences = new HashMap();

            File corpus_index = new File(rootPath + "/" + VocStatComputer.CORPUS_INDEX);

            try (FileWriter corpusIndexWriter = new FileWriter(corpus_index)) {

                for (File f : files) {

//                    logger.info("processing: "+f);
                    nbFileDoneLastIteration++;

                    int file_id = nbFileDone;
                    corpusIndexWriter.write(file_id + "\t" + f.getPath() + "\n");

                    if (nbFileDone % 1000 == 0) {
                        VocStatComputer.incrementProcessedFiles(nbFileDoneLastIteration);
                        nbFileDoneLastIteration = 0;
                        double p = VocStatComputer.getNbFilesProcessed() * 100.0 / VocStatComputer.getNbFilesToAnalyse();
                        String ps = Utils.format2digits(p);
                        logger.info("(" + id + ") File: " + nbFileDone + "/" + files.size() + "\t\tcache: " + wordOccurences.size() + "/" + cache_map_size + "\t corpus: " + VocStatComputer.getNbFilesProcessed() + "/" + VocStatComputer.getNbFilesToAnalyse() + "\t" + ps + "%");
                    }

                    if (vocabulary != null) {
                        wordIT = WordIteratorAccessor.getWordIterator(f, vocabulary);
                    } else {
                        wordIT = WordIteratorAccessor.getWordIterator(f, max_size_word, wordIteratorConstraint);
                    }

                    fileVoc = new HashMap();

                    // retrieve all the words and associated number of occurrences
                    while (wordIT.hasNext()) {

                        w = wordIT.next();

                        if (!wordOccurences.containsKey(w)) {
                            int size = Utils.blank_pattern.split(w).length;
                            wordOccurences.put(w, new WordInfo(size));
                        }
                        if (!fileVoc.containsKey(w)) {
                            fileVoc.put(w, 1);
                        } else {
                            fileVoc.put(w, fileVoc.get(w) + 1);
                        }
                        wordOccurences.get(w).addOccurrence();
                    }

                    wordScanned += wordIT.nbScannedWords();
                    validateWordScanned += wordIT.nbValidScannedWords();

                    // add a file occurrence to each word
                    for (String word : fileVoc.keySet()) {
                        WordInfo winfo = wordOccurences.get(word);
                        winfo.addFileWithWord();
                        winfo.concatAdditionnalInfo(file_id + "-" + fileVoc.get(word));
                    }

                    if (wordOccurences.size() >= cache_map_size) {
                        indexer.addToIndex(wordOccurences);
                        logger.info("(" + id + ") * File: " + nbFileDone + "/" + files.size() + "\t" + " on " + VocStatComputer.getNbFilesProcessed() + "/" + VocStatComputer.getNbFilesToAnalyse() + "\t" + dateFormat.format(new Date()));
                        wordOccurences.clear();
                    }
                    nbFileDone++;
                }
            }
            indexer.addToIndex(wordOccurences);
            VocStatComputer.incrementProcessedFiles(nbFileDoneLastIteration);
            wordOccurences.clear();

            // compute vocabulary size
            VocStatInfo info = new VocStatInfo(indexer.computeVocabularySize(), wordScanned, validateWordScanned, nbFileDone);
            info.flush(this.rootPath + "/" + VocStatComputer.GENERAL_INFO);

            double p = VocStatComputer.getNbFilesProcessed() * 100.0 / VocStatComputer.getNbFilesToAnalyse();
            String ps = Utils.format2digits(p);
            logger.info("(" + id + ") File: " + nbFileDone + "/" + files.size() + "\t\tcache: " + wordOccurences.size() + "/" + cache_map_size + "\t corpus: " + VocStatComputer.getNbFilesProcessed() + "/" + VocStatComputer.getNbFilesToAnalyse() + "\t" + ps + "%");
        } catch (Exception e) {
            logger.error("An error occured in thread: " + id);
            e.printStackTrace();
            throw e;
        } finally {
            poolWorker.taskComplete();
        }
        return new VocStatResult();
    }

}
