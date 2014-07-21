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
package slib.sml.sm.core.measures;

/**
 *
 * Generic interface of a measure which can be used to compare two objects.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public interface Measure {

    /**
     * Specifies the type of measures. As an example this approach can be used
     * to know if the measure is a similarity measure.
     *
     * @return the type of the measure
     */
    public MType getType();

    /**
     * Specified if the measure is symmetric. If the property is not proved
     * theoretically, if the value is not specified, or if the result depends on
     * the input value, the result is set to null.
     *
     * @return true if the measure is symmetric
     */
    public Boolean isSymmetric();

    /**
     * Specified if the measure is normalized. If the property is not proved
     * theoretically, if the value is not specified, or if the result depends on
     * the input value, the result is set to null.
     *
     * @return true if the measure is normalized
     */
    public Boolean isNormalized();
}
