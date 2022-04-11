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
import java.io.IOException;

/**
 * Implementation of the interface {@link WordIterator} considering a maximal
 * word size - shorter words are also returned. As an example considering the
 * following sentence "Twenty years from now you will be more disappointed by
 * the things that you didn’t do than by the ones you did do, so throw off the
 * bowlines, sail away from safe harbor, catch the trade winds in your sails.
 * Explore, Dream, Discover. –Mark Twain" and considering a maximum word size of
 * 3 the iterator will return (1) Twenty, (2) Twenty years, (3) Twenty years
 * from, (4) years, (5) years from, (6) years from now, ...
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class WordIterator_Allow_Shorter extends WordIteratorAbstract {

    long nbScannedWords;

    public WordIterator_Allow_Shorter(Document d, int word_size_constraint) throws IOException {
        super(d, word_size_constraint);
        current_word_size = 1;
    }

    @Override
    public String next() {

//        logger.info("loc_start: " + (current_loc_start_array + 1) + "/" + array.length);
//        logger.info("word_size: " + current_word_size);
//        logger.info("word_size cst: " + word_size_constraint);
        sbuffer = new StringBuffer();

        // We build the current word
        for (int i = current_loc_start_array; i < current_loc_start_array + current_word_size; i++) {

            if (i != current_loc_start_array) {
                sbuffer.append(' ');
            }
            sbuffer.append(array[i]);
        }

        String w = sbuffer.toString();

        // We prepare the setting for the next iteration
        // (1) we try to enlarge the word if possible. 
        // we reset the size to 1 and we define the starting point to be the next token if 
        // - (i) extending the word will violate the word size constraint, 
        // - (ii) there is no more space to build such a larger word (but a shorter one could be possible).
        current_word_size++;

        if (current_word_size > word_size_constraint || current_loc_start_array + current_word_size > array.length) {
            current_word_size = 1;
            current_loc_start_array++;

            // (2) we check that we have not already processed the last token
            // if this is the case we load the next line
            if (current_loc_start_array == array.length) {

                current_loc_start_array = 0;
                try {
                    array = loadNextNonEmptyLine();
                } catch (IOException ex) {
                    logger.error(WordIteratorAbstract.class.getName(), ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
        nbScannedWords++;
        return w;
    }

    @Override
    public WordIteratorConstraint getConstraint() {
        return WordIteratorConstraint.ALLOW_SHORTER_WORDS;
    }


    @Override
    public long nbScannedWords() {
        return nbScannedWords;
    }

    @Override
    public long nbValidScannedWords() {
        return nbScannedWords;
    }

}
