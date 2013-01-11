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
package slib.sml.sm.core.measures.framework.impl.set;

import java.util.HashSet;
import java.util.Set;
import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.measures.framework.core.engine.GraphRepresentation;
import slib.sml.sm.core.measures.framework.core.engine.IGraphRepresentation;
import slib.sml.sm.core.measures.framework.core.engine.RepresentationOperators;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.OperatorConf;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;
import slib.utils.impl.SetUtils;

/**
 * {@link OperatorsSet_IC} object defines operators used to compare
 * {@link GraphRepresentationAsSet} representation evaluating the
 * informativeness of terms. This class revisits {@link OperatorsSet} evaluating
 * commonalities, differences, informativeness etc as the sum of the
 * informativeness of the classes composing set intersection, symmetric
 * difference etc. The function used to defined class informativeness can be an
 * IC (information Content) or another metric defined in the given configuration
 * i.e. {@link OperatorConf} object specified during object creation. This class
 * also defines rules to avoid NaN values if one the
 * {@link GraphRepresentationAsSet} processed only contains
 *
 * @author Sebastien Harispe
 *
 */
public class OperatorsSet_IC extends RepresentationOperators {

    /**
     * Create an {@link OperatorsSet_IC} object. see
     * {@link #supportRepresentations(GraphRepresentation...)} to check the
     * {@link GraphRepresentation} supported by the operators. The given
     * configuration must contains an associated metric defining the method to
     * use to compute {@link V} informativeness see {@link OperatorConf#ic} and
     * {@link ICconf}
     *
     * @param conf the configuration of the operator
     * @throws SLIB_Exception
     */
    public OperatorsSet_IC(OperatorConf conf) throws SLIB_Exception {
        super(conf);

        if (this.conf.ic == null) {
            throw new SLIB_Exception("Please associate an IC configuration to operator " + conf.id + "  " + conf.flag);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The {@link OperatorsSet_IC} implementation evaluates commonalities as sum
     * of the informativeness of the classes contained in the set intersection.
     *
     * @throws SLIB_Exception
     */
    public double commonalities(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception {

        Set<V> a = ((GraphRepresentationAsSet) rep_a).anc;
        Set<V> b = ((GraphRepresentationAsSet) rep_b).anc;

        ResultStack<V, Double> ics = manager.getIC_results(this.conf.ic);

        double m = 0;
        Set<V> inter = SetUtils.intersection(a, b);
        for (V v : inter) {
            m += ics.get(v);
        }

        return m;
    }

    /**
     * {@inheritDoc}
     *
     * The {@link OperatorsSet_IC} implementation evaluates subtraction as sum
     * of the informativeness of the classes contained in the rep_a -
     * intersection(rep_a,rep_b).
     *
     * @throws SLIB_Exception
     */
    @Override
    public double subtraction(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception {

        Set<V> a = new HashSet<V>(((GraphRepresentationAsSet) rep_a).anc);
        Set<V> b = ((GraphRepresentationAsSet) rep_b).anc;

        a.removeAll(SetUtils.intersection(a, b));

        ResultStack<V, Double> ics = manager.getIC_results(this.conf.ic);

        double m = 0;
        for (V v : a) {
            m += ics.get(v);
        }
        return m;
    }

    /**
     * {@inheritDoc}
     *
     * The {@link OperatorsSet_IC} implementation evaluates difference as sum of
     * the informativeness of the classes contained in the union(rep_a,rep_b) \
     * intersection(rep_a,rep_b).
     *
     * @throws SLIB_Exception
     */
    public double diff(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception {

        Set<V> notShared = new HashSet<V>(((GraphRepresentationAsSet) rep_a).anc);
        Set<V> b = new HashSet<V>(((GraphRepresentationAsSet) rep_b).anc);
        notShared.addAll(b);
        notShared.removeAll(SetUtils.intersection(notShared, b));

        ResultStack<V, Double> ics = manager.getIC_results(this.conf.ic);

        double m = 0;
        for (V v : notShared) {
            m += ics.get(v);
        }

        return m;
    }

    /**
     * {@inheritDoc}
     *
     * The {@link OperatorsSet_IC} implementation only supports graph
     * representation as {@link GraphRepresentationAsSet}
     */
    @Override
    public boolean supportRepresentations(GraphRepresentation... reps) {
        for (IGraphRepresentation r : reps) {

            if (!(r instanceof GraphRepresentationAsSet)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * The {@link OperatorsSet_IC} implementation defines the informativeness as
     * the sum of informativeness of the set of vertices
     *
     * @param rep
     * @throws SLIB_Exception
     */
    @Override
    public double informativeness(GraphRepresentation rep, SM_Engine manager) throws SLIB_Exception {

        Set<V> a = ((GraphRepresentationAsSet) rep).anc;

        double m = 0;

        ResultStack<V, Double> ics = manager.getIC_results(this.conf.ic);

        for (V v : a) {
            m += ics.get(v);
        }
        return m;
    }

    /**
     * {@inheritDoc}
     *
     * Return true if the {@link GraphRepresentation} are supported see
     * {@link #supportRepresentations(GraphRepresentation...)} and if compared
     * representation not only contains the root of the graph
     *
     * @throws SLIB_Exception
     */
    @Override
    public boolean validateRules(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception {

        if (supportRepresentations(rep_a, rep_b)) {

            Set<V> a = ((GraphRepresentationAsSet) rep_a).anc;
            Set<V> b = ((GraphRepresentationAsSet) rep_a).anc;

            V root = manager.getRoot();

            if (a.size() == 1 && a.iterator().next().equals(root)) {
                return false;
            }

            if (b.size() == 1 && b.iterator().next().equals(root)) {
                return false;
            }

            return true;
        }
        return false;

    }

    /**
     * {@inheritDoc}
     *
     * {@link OperatorsSet_IC} implements commonalities operator.
     */
    @Override
    public boolean asOperatorCommonalities() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * {@link OperatorsSet_IC} implements difference operator.
     */
    @Override
    public boolean asOperatorDifference() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * {@link OperatorsSet_IC} implements informativeness operator.
     */
    @Override
    public boolean asOperatorGRinformativness() {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * {@link OperatorsSet_IC} implements subtraction operator.
     */
    @Override
    public boolean asOperatorSubstraction() {
        return true;
    }
}
