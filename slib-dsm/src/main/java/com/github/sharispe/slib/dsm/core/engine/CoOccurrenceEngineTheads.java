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

import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrix;
import com.github.sharispe.slib.dsm.core.model.utils.SparseMatrixGenerator;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class CoOccurrenceEngineTheads implements Callable<CooccEngineResult> {

    int id;
    Collection<File> files;
    Logger logger = LoggerFactory.getLogger(CoOccurrenceEngineTheads.class);
    VocabularyIndex vocabularyIndex;
    SparseMatrix matrix;
    File matrix_dir;
    int fileErrors = 0;
    int nbFileDone = 0;
    int nbFileDoneLastIteration = 0;
    final int max_matrix_size;
    final int window_token_size;

    public CoOccurrenceEngineTheads(int id, Collection<File> files, VocabularyIndex vocabularyIndex, int window_token_size, String dir_path, int max_matrix_size) {
        this.id = id;
        this.files = files;
        this.vocabularyIndex = vocabularyIndex;
        this.window_token_size = window_token_size;
        this.max_matrix_size = max_matrix_size;

        matrix_dir = new File(dir_path);
        matrix_dir.mkdirs();
    }

    @Override
    public CooccEngineResult call() {

        try {

            matrix = SparseMatrixGenerator.buildSparseMatrix(vocabularyIndex.vocabulary.size(), vocabularyIndex.vocabulary.size());

            for (File f : files) {
                
                // TODO remove log
                //logger.info("(" + id + ") " + nbFileDone + "/" + files.size()+"  "+f.getPath());

                nbFileDone++;

                try {
                    loadWordCooccurrenceFromFile(f);
                } catch (SLIB_Ex_Critic ex) {
                    logger.error("Critical error " + ex.getMessage());
                    fileErrors++;
                }

                if (matrix.storedValues() > max_matrix_size) {
                    flushMatrix();
                    matrix.clear();
                }
            }
            flushMatrix();
            matrix.clear();

            CoOcurrenceEngine.incrementProcessedFiles(nbFileDoneLastIteration);
            nbFileDoneLastIteration = 0;
            double p = CoOcurrenceEngine.getNbFilesProcessed() * 100.0 / CoOcurrenceEngine.getNbFilesToAnalyse();
            String ps = Utils.format2digits(p);

            logger.info("(" + id + ") File: " + nbFileDone + "/" + files.size() + "\t\tcache: " + matrix.storedValues() + "/" + max_matrix_size + "\t corpus: " + CoOcurrenceEngine.getNbFilesProcessed() + "/" + CoOcurrenceEngine.getNbFilesToAnalyse() + "\t" + ps + "%  " + Utils.getCurrentDateAsString());

            return new CooccEngineResult(nbFileDone, fileErrors);

        } catch (Exception e) {
            e.printStackTrace();
            return new CooccEngineResult(nbFileDone, fileErrors);
        }
    }

    private void loadWordCooccurrenceFromFile(File file) throws SLIB_Ex_Critic {

        // TODO REMOVE THIS LOG
//        logger.info("(" + id + ") processing " + nbFileDone + "/" + files.size() + ": " + file.getPath());

        if (vocabularyIndex == null || vocabularyIndex.getVocabulary().size() < 2) {
            throw new SLIB_Ex_Critic("You must first load or specify a vocabulary of size larger than 2");
        }

        try {
            String s = FileUtils.readFileToString(file);
            
            if (nbFileDone % 100 == 0) {
                CoOcurrenceEngine.incrementProcessedFiles(nbFileDoneLastIteration);
                nbFileDoneLastIteration = 0;
                double p = CoOcurrenceEngine.getNbFilesProcessed() * 100.0 / CoOcurrenceEngine.getNbFilesToAnalyse();
                String ps = Utils.format2digits(p);
                logger.info("(" + id + ") File: " + nbFileDone + "/" + files.size() + "\t\tcache: " + matrix.storedValues() + "/" + max_matrix_size + "\t corpus: " + CoOcurrenceEngine.getNbFilesProcessed() + "/" + CoOcurrenceEngine.getNbFilesToAnalyse() + "\t" + ps + "%  " + Utils.getCurrentDateAsString());
            }
            
            CoocurenceComputer.compute(s,vocabularyIndex,window_token_size,matrix);

            nbFileDoneLastIteration++;
        } catch (IOException ex) {
            ex.printStackTrace();
            fileErrors++;
            new SLIB_Ex_Critic(ex.getMessage());
        }

        // TODO REMOVE THIS LOG
//        logger.info("(" + id + ") done " + nbFileDone + "/" + files.size() + ": " + file.getPath());

    }

 

    private void flushMatrix() {

        try {

            logger.info("Flushing matrix size: " + matrix.storedValues() + " (limit " + max_matrix_size + ") into " + matrix_dir);

            File matrix_file = new File(matrix_dir + "/matrix");
            File new_matrix_file = new File(matrix_file.getPath() + ".new");

            List<Integer> sorted_ids_matrix = new ArrayList(matrix.getElementIDs());
            Collections.sort(sorted_ids_matrix);

            Map<Integer, Double> new_matrix_current_word_compressed_vector;

            if (!matrix_file.exists()) { // first time the matrix is flushed

                try (PrintWriter matrixWriter = new PrintWriter(matrix_file, "UTF-8")) {

                    for (Integer word_id : sorted_ids_matrix) {
                        // get vector associated to this word
                        new_matrix_current_word_compressed_vector = matrix.getDimensionValuesForElement(word_id);
                        // flush the vector into the file
                        matrixWriter.write(convertVectorMapToString(word_id, new_matrix_current_word_compressed_vector));
                    }
                }

            } else { // add to existing matrix and update existing vectors if needed

                try (PrintWriter matrixWriter = new PrintWriter(new_matrix_file, "UTF-8")) {

                    int nb_words_new_matrix_processed = 0;

                    try (BufferedReader br = new BufferedReader(new FileReader(matrix_file))) {

                        String line;
                        String[] old_matrix_current_word_vector, data2;
                        Integer old_matrix_current_word_id, word_id_c;
                        double add_occ, old_occ;

                        // format 
                        // word_id:word_id_1-nb_coccurences_word_id_word_id_1::word_id_2-nb_coccurences_word_id_word_id_2:...
                        // e.g. 156:30-456:45-2
                        // means that the word with id 156 cooccurred 456 times with word 30 and 2 times with word 45
                        while ((line = br.readLine()) != null) {

                            old_matrix_current_word_vector = Utils.colon_pattern.split(line.trim());
                            old_matrix_current_word_id = Integer.parseInt(old_matrix_current_word_vector[0]);

                            // while the matrix contains vectors with a lower id
                            // than the id of the vector currently processed (i.e. strored into the file)
                            // we process it (i means that it has never been seen) - the aim is to keep a sorted matrix
                            while (nb_words_new_matrix_processed < sorted_ids_matrix.size() && sorted_ids_matrix.get(nb_words_new_matrix_processed) < old_matrix_current_word_id) {
                                Integer current_word_id_new_matrix = sorted_ids_matrix.get(nb_words_new_matrix_processed);
                                String v = convertVectorMapToString(current_word_id_new_matrix, matrix.getDimensionValuesForElement(current_word_id_new_matrix));

                                matrixWriter.write(v);
                                nb_words_new_matrix_processed++;

                            }
                            // all the vectors of the new matrix associated to an id lower than the one of the old matrix currently processed have been processed

                            // try to get the vector associated to this word in the matrix
                            new_matrix_current_word_compressed_vector = matrix.getDimensionValuesForElement(old_matrix_current_word_id);

                            if (new_matrix_current_word_compressed_vector != null) { // information about the vector is stored in the matrix - add new occurrences

                                for (int i = 1; i < old_matrix_current_word_vector.length; i++) {
                                    data2 = Utils.dash_pattern.split(old_matrix_current_word_vector[i]);// 30-456
                                    word_id_c = Integer.parseInt(data2[0]);
                                    add_occ = Double.parseDouble(data2[1]);
                                    old_occ = new_matrix_current_word_compressed_vector.containsKey(word_id_c) ? new_matrix_current_word_compressed_vector.get(word_id_c) : 0;
                                    new_matrix_current_word_compressed_vector.put(word_id_c, old_occ + add_occ);
                                }

                                // flush the vector into the file
                                matrixWriter.write(convertVectorMapToString(old_matrix_current_word_id, new_matrix_current_word_compressed_vector));
                            } else { // new vector
                                matrixWriter.write(line + "\n");
                            }
                            nb_words_new_matrix_processed++; // in both case a word of the new matrix is processed 
                        }
                    }
                    // process new vectors that have an id bigger than the one of the last vector stored in the matrix
                    for (; nb_words_new_matrix_processed < sorted_ids_matrix.size(); nb_words_new_matrix_processed++) {
                        int id = sorted_ids_matrix.get(nb_words_new_matrix_processed);

                        String v = convertVectorMapToString(id, matrix.getDimensionValuesForElement(id));
                        matrixWriter.write(v);
                    }
                }
                // replace old matrix by new
                Files.move(new_matrix_file.toPath(), matrix_file.toPath(), REPLACE_EXISTING);
            }
            logger.info("matrix flushed into " + matrix_dir);

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(CoOccurrenceEngineTheads.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // format // word_id:word_id_1-nb_coccurences_word_id_word_id_1::word_id_2-nb_coccurences_word_id_word_id_2:...
    // e.g. 156:30-456:45-2
    // means that the word with id 156 cooccurred 456 times with word 30 and 2 times with word 45
    // note that the map are sorted according to the word_ids for which cooccurrences are stored
    protected static String convertVectorMapToString(int vectorID, Map<Integer, Double> vector) {
        StringBuilder vectorAsString;
        vectorAsString = new StringBuilder();

        vectorAsString.append(vectorID);

        Map<Integer, Double> sortedMap = new TreeMap();
        sortedMap.putAll(vector);

        for (Map.Entry<Integer, Double> e : sortedMap.entrySet()) {
            vectorAsString.append(':');
            vectorAsString.append(e.getKey());
            vectorAsString.append('-');
            vectorAsString.append((int) (double) e.getValue());
        }
        vectorAsString.append('\n');

        return vectorAsString.toString();
    }
}
