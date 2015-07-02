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
import static com.github.sharispe.slib.dsm.utils.Utils.logger;
import java.io.IOException;
import java.util.Set;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Vocabulary {

    private final Set<String> elements;
    private int size;
    private int max_token_lenght;

    public Vocabulary(Set<String> vocabulary) {
        this.elements = vocabulary;
        this.size = vocabulary.size();
        prepare();
    }

    public Vocabulary(String vocabularyFile) throws IOException {
        logger.info("loading vocabulary from: " + vocabularyFile);
        this.elements = Utils.loadWords(vocabularyFile);
        size = elements.size();
        prepare();
    }

    public boolean contains(String s) {
        return elements.contains(s);
    }

    public Set<String> getElements() {
        return elements;
    }

    public int size() {
        return size;
    }

    public int getMax_token_lenght() {
        return max_token_lenght;
    }

    private void prepare() {
        logger.info("preparing vocabulary");

        for (String s : elements) {
            int nbTokens = Utils.blank_pattern.split(s).length;
            if (nbTokens > max_token_lenght) {
                max_token_lenght = nbTokens;
            }
        }
        logger.info("vocabulary size: " + elements.size());
        logger.info("max token length: " + max_token_lenght);
    }

}
