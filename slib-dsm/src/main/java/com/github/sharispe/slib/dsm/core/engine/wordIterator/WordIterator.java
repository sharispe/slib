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

import java.io.IOException;
import java.util.Iterator;

/**
 *
 * WordIterator can be used to iterate over words located in a text considering
 * specific constraints. Because of the way data is processed tokens separated
 * by multiple spaces will be implicitly converted to a sequence of tokens
 * separated by a single space, e.g. machine~~~learning (~ represents a space)
 * will be converted by "machine learning".
 *
 * token are considered to be sequences of characters that do not contains
 * spaces, e.g. the cat represents two tokens.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public interface WordIterator extends Iterator<String> {

    /**
     * Close the underlying file. Must always be called to properly close the
     * iterator that has not be fully traversed (cf. hasNext).
     *
     * @throws IOException
     */
    public void close() throws IOException;

    /**
     * @return the strategy considered to iterate over the words
     */
    public WordIteratorConstraint getConstraint();

    /**
     * @return the number of words that have been scanned so far (words may not
     * respect the constrain)
     */
    public long nbScannedWords();

    /**
     * @return the number of words that respect the constrain that have been
     * scanned so far
     */
    public long nbValidScannedWords();

}
