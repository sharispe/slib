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
import slib.utils.impl.SetUtils;

/**
 * Korbel JO, Snel B, Huynen M a, Bork P: SHOT: a web server for the
 * construction of genome phylogenies. Trends in genetics : TIG 2002, 18:158-62.
 *
 * Cited by ﻿1. Lin C, Cho Y-rae, Hwang W-chang, Pei P, Zhang A: Clustering
 * methods in protein-protein interaction network.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_Framework_DAG_Set_Korbel_2002 extends Sim_Framework_DAG_Set_abstract {

    @Override
    public double compare(Set<URI> ancA, Set<URI> ancB, SMconf conf) {

        Set<URI> interSecAncestors = SetUtils.intersection(ancA, ancB);

        double nbAncest_a = ancA.size();
        double nbAncest_b = ancB.size();

        double pow_a = Math.pow(ancA.size(), 2);
        double pow_b = Math.pow(ancB.size(), 2);

        double mb = ((Math.sqrt(pow_a + pow_b) / (Math.sqrt(2) * nbAncest_a * nbAncest_b)) * interSecAncestors.size());

        return mb;
    }

    @Override
    public Boolean isSymmetric() {
        return true;
    }
}
