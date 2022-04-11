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
package com.github.sharispe.slib.dsm.core.engine.wordIterator;

import com.github.sharispe.slib.dsm.core.corpus.Document;
import com.github.sharispe.slib.dsm.utils.Utils;
import java.io.IOException;
import org.slf4j.LoggerFactory;

/**
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public abstract class WordIteratorAbstract implements WordIterator {

    final int word_size_constraint;
    String[] array;
    String[] lines;
    int idCurrentLine = 0;
    StringBuffer sbuffer;

    // internal variables used for algorithmic purpose
    int current_loc_start_array = 0;
    int current_word_size = 1;

    final static org.slf4j.Logger logger = LoggerFactory.getLogger(WordIteratorAbstract.class);

    /**
     * Note that any WordIterator instance has to be closed.
     *
     * @param d the document to analyse
     * @param word_size_constraint the word size constraint
     * @throws IOException if an error occurs
     */
    public WordIteratorAbstract(Document d, int word_size_constraint) throws IOException {

        this.word_size_constraint = word_size_constraint;
        lines = d.getContent().split("\n");
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
            } catch (IOException ex) {
                logger.error(WordIteratorAbstract.class.getName() + "" + ex.getMessage());
                ex.printStackTrace();
            }
            return false;
        }
        return true;
    }

    @Override
    public void close() throws IOException {
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
     * @throws IOException if an error occurs
     */
    public final String[] loadNextNonEmptyLine() throws IOException {

        String[] nextLine = null;
        String l;
        while (idCurrentLine < lines.length && nextLine == null) {
            idCurrentLine++;
            l = lines[idCurrentLine - 1].trim();
            if (l.length() == 0) {
                continue;
            }
            nextLine = Utils.blank_pattern.split(l);
            if (nextLine.length == 0) {
                nextLine = null;
            }
        }
//        System.out.println("NextNonEmptyLine: " + Arrays.toString(nextLine));
//        if (nextLine != null) {
//            System.out.println(nextLine.length);
//        }
        return nextLine;
    }

}
