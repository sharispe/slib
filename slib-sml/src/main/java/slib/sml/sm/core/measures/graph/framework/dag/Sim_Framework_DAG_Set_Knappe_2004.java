/*

 Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

 This software is a computer program whose purpose is to 
 process semantic graphs.

 This software is governed by the CeCILL  license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 */
package slib.sml.sm.core.measures.graph.framework.dag;

import java.util.Set;
import org.openrdf.model.URI;

import slib.sml.sm.core.utils.SMconf;
import slib.utils.impl.SetUtils;

/**
 * Knappe R, Bulskov H, Andreasen T: Perspectives on ontology-based
 * querying. International Journal of Intelligent Systems 2004, 22:739-761.
 *
 * @author Sebastien Harispe
 */
public class Sim_Framework_DAG_Set_Knappe_2004 extends Sim_Framework_DAG_Set_abstract {

    private double k = 0.5;

    public Sim_Framework_DAG_Set_Knappe_2004() {
    }

    
    public Sim_Framework_DAG_Set_Knappe_2004(double k) {
        if(k < 0 || k > 1){
            throw new IllegalArgumentException("k parameter must be in [0;1]");
        }
        this.k = k;
    }

    @Override
    public double sim(Set<URI> ancA, Set<URI> ancB, SMconf conf) {

        Set<URI> interSecAncestors = SetUtils.intersection(ancA, ancB);

        double nbAncest_a = ancA.size();
        double nbAncest_b = ancB.size();

        double knappe = (double) k * (interSecAncestors.size() / nbAncest_a) + (1 - k) * (interSecAncestors.size() / nbAncest_b);

        return knappe;
    }

    /**
     *
     * @return
     */
    public double getK() {
        return k;
    }

    /**
     *
     * @param k
     */
    public void setK(double k) {
        this.k = k;
    }
    
    @Override
    public boolean isSymmetric() {
        return k == 0.5;
    }
}
