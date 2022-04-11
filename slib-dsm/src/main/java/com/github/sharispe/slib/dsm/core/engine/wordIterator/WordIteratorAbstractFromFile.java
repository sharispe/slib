/*
 *  Copyright or � or Copr. Ecole des Mines d'Al�s (2012-2014) 
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
package com.github.sharispe.slib.dsm.core.engine.wordIterator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.LoggerFactory;

/**
 * @author S�bastien Harispe (sebastien.harispe@gmail.com)
 */
public abstract class WordIteratorAbstractFromFile implements WordIterator {

    final int word_size_constraint;
    BufferedReader br;
    String[] array;
    StringBuffer sbuffer;

    // internal variables used for algorithmic purpose
    int current_loc_start_array = 0;
    int current_word_size = 1;

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(WordIteratorAbstractFromFile.class);

    /**
     * Note that any WordIterator instance has to be closed.
     *
     * @param f the file in which to iterate
     * @param word_size_constraint the word size constraint
     * @throws IOException
     */
    public WordIteratorAbstractFromFile(File f, int word_size_constraint) throws IOException {

        this.word_size_constraint = word_size_constraint;
        br = new BufferedReader(new FileReader(f));
        array = loadNextNonEmptyLine(); 
    }

    /**
     * @return true if the underlying file contains another word. If the
     * iterator is empty the methods return false and the underlying file is
     * automatically closed.
     */
    @Override
    public boolean hasNext() {
        if (array == null) {
            try {
                close();
                br = null;
            } catch (IOException ex) {
                logger.error(WordIteratorAbstractFromFile.class.getName() + "" + ex.getMessage());
                ex.printStackTrace();
            }
            return false;
        }
        return true;
    }

    @Override
    public void close() throws IOException {
        if (br != null) {
            br.close();
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /**
     * Load the next non-empty line from the file considering the constraint.
     *
     * @return an array containing each tokens of the next non empty line. If
     * there is no more empty line the methods return null.
     * @throws IOException
     */
    abstract String[] loadNextNonEmptyLine() throws IOException;

}
