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
package slib.sml.sm.core.measures.graph.framework.dag;

import java.util.Set;
import org.openrdf.model.URI;

import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 * Bader G, Hogue C: An automated method for finding molecular complexes in
 * large protein interaction networks. BMC bioinformatics 2003, 4:2.
 *
 * Cited by ﻿Lin C, Cho Y-rae, Hwang W-chang, Pei P, Zhang A: Clustering methods
 * in protein-protein interaction network. in Knowledge Discovery in
 * Bioinformatics: Techniques, Methods and Application 2006.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_Framework_DAG_Set_Bader_2003 extends Sim_Framework_DAG_Set_abstract {

    @Override
    public double compare(Set<URI> setA, Set<URI> setB, SMconf conf) throws SLIB_Exception {

        Set<URI> interSecAncestors = SetUtils.intersection(setA, setB);

        int nbAncest_a = setA.size();
        int nbAncest_b = setB.size();

        double mb = (double) Math.pow(interSecAncestors.size(), 2) / (nbAncest_a * nbAncest_b);

        return mb;
    }


}
