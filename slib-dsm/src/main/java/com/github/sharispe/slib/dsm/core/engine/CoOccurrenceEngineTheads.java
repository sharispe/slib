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
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class CoOccurrenceEngineTheads implements Callable<CooccEngineResult> {

    int id;
    Collection<File> files;
    SparseMatrix globalCoocurences;
    Logger logger = LoggerFactory.getLogger(CoOccurrenceEngineTheads.class);
    Voc vocIndex;
    int fileErrors = 0;
    int nbFileDone = 0;

    int window_size_right = CoOcurrenceEngine.WINDOW_SIZE_RIGHT;
    int window_size_left = CoOcurrenceEngine.WINDOW_SIZE_LEFT;

    int window_size_total = CoOcurrenceEngine.WINDOW_SIZE_LEFT + 1 + CoOcurrenceEngine.WINDOW_SIZE_RIGHT;

    public class CooccEngineResult {

        int file_processed;
        int errors;

        public CooccEngineResult(int file_processed, int errors) {
            this.file_processed = file_processed;
            this.errors = errors;
        }

        public int getFile_processed() {
            return file_processed;
        }

        public int getNbErrors() {
            return errors;
        }

    }

    public CoOccurrenceEngineTheads(int id, Collection<File> files, Voc vocIndex, SparseMatrix globalCoocurences) {
        this.id = id;
        this.files = files;
        this.globalCoocurences = globalCoocurences;
        this.vocIndex = vocIndex;
    }

    @Override
    public CooccEngineResult call() {

        for (File f : files) {
            nbFileDone++;

            try {
                loadWordCooccurrenceFromFile(f);
            } catch (SLIB_Ex_Critic ex) {
                logger.error("Critical error " + ex.getMessage());
                fileErrors++;
            }

        }
        return new CooccEngineResult(nbFileDone, fileErrors);
    }

    private void loadWordCooccurrenceFromFile(File file) throws SLIB_Ex_Critic {

        if (vocIndex == null || vocIndex.size() < 2) {
            throw new SLIB_Ex_Critic("You must first load or specify a vocabulary");
        }

        try {
            String s = FileUtils.readFileToString(file);
            String[] stab = Utils.blank_pattern.split(s);
            int[] text = textToArrayIDs(stab);

            if (nbFileDone % 100 == 0) {
                logger.info("(thread=" + id + ") File: " + nbFileDone + "/" + files.size() + "\t" + file.getPath() + "\t word ex:" + (stab.length - text.length) + "/" + stab.length);
            }
            
            List<Integer> window = new ArrayList();
            int wsize = 1 + window_size_right < text.length ? 1 + window_size_right : text.length;
            for (int i = 0; i < wsize; i++) {
                window.add(text[i]);
            }
            int pointer_right = 1 + window_size_right;

            int focalWordID = 0;
            boolean shrink_left, expend_right;

            for (int i = 0; i < text.length; i++) {

                shrink_left = false;
                expend_right = false;

                processWindowForward(focalWordID, window);

                if (window.size() >= window_size_total) { // shrink left
                    shrink_left = true;
                }

                if (pointer_right < text.length) { // expend right
                    expend_right = true;
                } else {
                    if (window.size() > window_size_left + 1) {
                        shrink_left = true;
                    }
                }

                if (shrink_left) {
                    window.remove(0);
                }
                if (expend_right) {
                    window.add(text[pointer_right]);

                }

                if (!(shrink_left && expend_right)) {
                    if (focalWordID < window_size_left) {
                        focalWordID++;// expending right
                    } else if (focalWordID > window_size_left) {
                        focalWordID--;
                    }
                }
                pointer_right++;
            }

        } catch (IOException ex) {
            new SLIB_Ex_Critic(ex.getMessage());
        }
    }

    /**
     * No need to process backward
     *
     * @param focalWordID
     * @param window
     */
    private void processWindowForward(int focalWordID, List<Integer> window) {

        for (int i = focalWordID + 1; i < window.size(); i++) {
            globalCoocurences.add(window.get(focalWordID), window.get(i), 1);
            globalCoocurences.add(window.get(i), window.get(focalWordID), 1);
        }
    }

    /**
     * Convert a String to an array of IDs considering the given index. If a
     * word is not defined into the index, it will not be considered in the
     * process (but no specific marker will specify that the element has been
     * omitted).
     *
     * @param vocabulary
     * @param s
     * @return
     */
    private int[] textToArrayIDs(String[] s) {
        ArrayList<Integer> list = new ArrayList();
        for (String ss : s) {
            Integer id = vocIndex.getID(ss);
            if (id == null) {
//                logger.info("skip='"+ss+"'");
                continue;
            }
            list.add(vocIndex.getID(ss));
        }
        return ArrayUtils.toPrimitive(list.toArray(new Integer[list.size()]));
    }
    
    

}
