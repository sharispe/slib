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

import com.github.sharispe.slib.dsm.utils.Utils;
import java.util.HashMap;
import java.util.Map;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class VocStatInfo {

    private final static String NB_FILES_FLAG = "NB_FILES";
    private final static String NB_WORDS_FLAG = "SIZE_VOC";
    private final static String NB_SCANNED_WORDS = "NB_SCANNED_WORDS";
    private final static String NB_VALIDATED_SCANNED_WORDS = "NB_VALIDATED_SCANNED_WORDS";

    public final int vocSize;
    public final int nbFiles;
    public final long nbScannedWords;
    public final long nbValidatedScannedWords;

    public VocStatInfo(int vocSize, long nbScannedWords, long nbValidatedScannedWords, int nbFiles) {
        this.vocSize = vocSize;
        this.nbFiles = nbFiles;
        this.nbScannedWords = nbScannedWords;
        this.nbValidatedScannedWords = nbValidatedScannedWords;
    }

    /**
     * Stores the VocInfo into the given file.
     *
     * @param filepath the file path
     * @throws slib.utils.ex.SLIB_Ex_Critic if an error occurs
     */
    public void flush(String filepath) throws SLIB_Ex_Critic {

        Map<String, String> vocInfo = new HashMap<String, String>();
        vocInfo.put(NB_FILES_FLAG, nbFiles + "");
        vocInfo.put(NB_WORDS_FLAG, vocSize + "");
        vocInfo.put(NB_SCANNED_WORDS, nbScannedWords + "");
        vocInfo.put(NB_VALIDATED_SCANNED_WORDS, nbValidatedScannedWords + "");

        Utils.flushMapKV(vocInfo, "=", filepath);
    }

    /**
     * Build a VocInfo object considering the specified file.
     *
     * @param filepath of the following form NB_FILES=X NB_WORDS=Y
     * @throws slib.utils.ex.SLIB_Ex_Critic if an error occurs
     */
    public VocStatInfo(String filepath) throws SLIB_Ex_Critic {

        Map<String, Long> map = Utils.loadMapStringLong(filepath, "=");

        if (!map.containsKey(NB_WORDS_FLAG) || !map.containsKey(NB_FILES_FLAG) || !map.containsKey(NB_SCANNED_WORDS) || !map.containsKey(NB_VALIDATED_SCANNED_WORDS)) {
            throw new SLIB_Ex_Critic("Cannot load voc statistics from: " + filepath + ", please consult the documentation");
        }

        vocSize = (int) (long) map.get(NB_WORDS_FLAG);
        nbFiles = (int) (long) map.get(NB_FILES_FLAG);
        nbScannedWords = map.get(NB_SCANNED_WORDS);
        nbValidatedScannedWords = map.get(NB_VALIDATED_SCANNED_WORDS);

    }
}
