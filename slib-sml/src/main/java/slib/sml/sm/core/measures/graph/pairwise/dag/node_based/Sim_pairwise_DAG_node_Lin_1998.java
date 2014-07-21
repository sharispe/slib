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
package slib.sml.sm.core.measures.graph.pairwise.dag.node_based;

import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Implementation of the measure proposed by Lin for comparing two concepts
 * defined in a taxonomy.
 *
 * <p>
 * SYMMETRIC = YES <br/>
 * VALUE : [0,1]
 * </p>
 *
 * <p>
 * <b>Reference</b>: Lin, Dekang. (1998). An Information-Theoretic Definition of
 * Similarity. In 15th International Conference of Machine Learning (pp.
 * 296–304). Madison,WI.
 * </p>
 *
 * <p>
 * Considering a function IC which is used to assess the information content of
 * a concept, with IC(u) the information content of the concept u, this measure
 * relies on a ratio between the IC of the Most Specific Common Ancestor of the
 * compared concepts and the sum of their IC. The MICA is therefore the common
 * ancestor of the compared concepts which maximizes the selected IC function.
 * If multiple MICAs are found only one will be considered. <br/> <br/>
 *
 * The measure is therefore defined by: <br/><br/>
 *
 * <code>
 * sim(u,v) = 2 x IC(MICA(u,v)) / (IC(u) + IC(v))
 * </code>
 * <br/><br/>
 * Note that originally, the formulation proposed by Lin considered <code>IC(u) =
 * log(p(u))</code> which is equivalent to considering the IC formulation
 * proposed by Resnik <code>IC(u) = -log(p(u))</code>. However, in this
 * implementation any IC formulation can be used - the IC values must decrease
 * from the leaves to the root(s) of the taxonomy.
 * </p>
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Sim_pairwise_DAG_node_Lin_1998 extends  Sim_DAG_node_abstract {

    private static boolean PREVENT_INCOHERENCES = true;
    final public static boolean IS_SYMMETRIC = true;

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        double ic_a = c.getIC(conf.getICconf(), a);
        double ic_b = c.getIC(conf.getICconf(), b);
        double ic_MICA = c.getIC_MICA(conf.getICconf(), a, b);

        return sim(ic_a, ic_b, ic_MICA);
    }

    /**
     * Compute the similarity considering the given information content values.
     *
     * @param ic_a the IC of the concept A
     * @param ic_b the IC of the concept B
     * @param ic_mica the IC of the Most Informative Common Ancestors of A and B
     * @return the semantic similarity
     *
     * @throws SLIB_Ex_Critic
     */
    public static double sim(double ic_a, double ic_b, double ic_mica) throws SLIB_Ex_Critic {

        double lin = 0.;

        double den = ic_a + ic_b;

        if (PREVENT_INCOHERENCES
                && ((ic_mica > ic_a && ic_mica - ic_a > 0.00001)
                || (ic_mica > ic_b && ic_mica - ic_b > 0.00001))) {
            throw new SLIB_Ex_Critic("Cannot compute Lin considering ic MICA > ic C1 or ic c2, ic MICA set to " + ic_mica + " ic c1 " + ic_a + " ic c2 " + ic_b);
        }

        if (den != 0) {
            lin = (2. * ic_mica) / den;
        }
        return lin;
    }

    /**
     * Setting this parameter you can decide if incoherences in the input
     * parameters must be detected (true) or not (false) - default true.
     *
     * This is mainly to check that the information content values of the
     * compared concepts are lower than the information content of their most
     * informative common ancestor - which can lead to similarity values greater
     * than 1.
     *
     * @param preventIncoherency true if incoherences must be detected
     */
    public void setPreventIncoherency(boolean preventIncoherency) {
        Sim_pairwise_DAG_node_Lin_1998.PREVENT_INCOHERENCES = preventIncoherency;
    }

    @Override
    public Boolean isSymmetric() {
        return IS_SYMMETRIC;
    }
}
