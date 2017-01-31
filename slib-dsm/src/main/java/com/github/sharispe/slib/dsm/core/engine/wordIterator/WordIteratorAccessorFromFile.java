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

import com.github.sharispe.slib.dsm.core.engine.Vocabulary;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class WordIteratorAccessorFromFile {

    /**
     * Provides access to the suitable word accessor considering the given
     * constraints.
     *
     * @param f the file from which the word iteration has to be done.
     * @param word_size_constraint the constraint related to the number of must
     * have (at most). The way this constraint is understood is function of the
     * word iterator constraint definition (see above).
     * @param c defines how the iteration is performed considering the given
     * size constraint. ALLOW_SHORTER_WORDS enables words that are shorter or
     * equal to the specified constraint. FIXED_SIZE forces the words to have a
     * number of token equal to the given constraint.
     * @return the appropriate instance of word iterator considering the given
     * constraints
     * @throws SLIB_Ex_Critic
     * @throws IOException
     */
    public static WordIterator getWordIterator(File f, int word_size_constraint, WordIteratorConstraint c) throws SLIB_Ex_Critic, IOException {
        switch (c) {
            case ALLOW_SHORTER_WORDS:
                return new WordIterator_Allow_ShorterFromFile(f, word_size_constraint);
            case FIXED_SIZE:
                return new WordIterator_FixedSizeFromFile(f, word_size_constraint);
            default:
                throw new SLIB_Ex_Critic(c + " is not a supported word constraint");
        }
    }

    public static WordIterator getWordIterator(File f, Vocabulary vocabulary) throws IOException {
        return new WordIterator_DictionaryFromFile(f, vocabulary);
    }

}
