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
package slib.graph.utils;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.utils.impl.SetUtils;

/**
 *
 * Class used to facilitate the use of WalkConstraint over graph.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class WalkConstraintGeneric implements WalkConstraint {

    Set<URI> acceptedWalksIN = new HashSet<URI>();
    Set<URI> acceptedWalksOUT = new HashSet<URI>();

    public WalkConstraintGeneric() {
    }

    /**
     * Build an instance of walk constraint considering the walk rule.
     *
     * @param walkRules a map containing the predicate to consider and their
     * respective direction
     */
    public WalkConstraintGeneric(Map<URI, Direction> walkRules) {

        for (Entry<URI, Direction> e : walkRules.entrySet()) {

            Direction dir = e.getValue();

            if (dir == Direction.IN || dir == Direction.BOTH) {
                acceptedWalksIN.add(e.getKey());
            }

            if (dir == Direction.OUT || dir == Direction.BOTH) {
                acceptedWalksOUT.add(e.getKey());
            }
        }
    }

    /**
     * Build an instance of walk constraint considering the given predicate URI
     * and direction.
     *
     * @param acceptedPredicate the predicate URI to consider
     * @param dir the direction associated
     */
    public WalkConstraintGeneric(URI acceptedPredicate, Direction dir) {
        if (dir == Direction.IN || dir == Direction.BOTH) {
            acceptedWalksIN.add(acceptedPredicate);
        }

        if (dir == Direction.OUT || dir == Direction.BOTH) {
            acceptedWalksOUT.add(acceptedPredicate);
        }
    }

    @Override
    public boolean respectConstaints(E e, Direction dir) {
        boolean valid = false;

        if (dir == Direction.IN || dir == Direction.BOTH) {
            valid = acceptedWalksIN.contains(e.getURI());
        }

        if (dir == Direction.OUT || (!valid && dir == Direction.BOTH)) {
            valid = acceptedWalksOUT.contains(e.getURI());
        }

        return valid;
    }

    @Override
    public Set<URI> getAcceptedPredicates() {
        Set<URI> union = new HashSet<URI>();
        union.addAll(acceptedWalksIN);
        union.addAll(acceptedWalksOUT);
        return union;
    }

//    @Override
//    public Map<URI, Direction> getAcceptedTraversals() {
//
//        Map<URI, Direction> acceptedWalks = new HashMap<URI, Direction>();
//
//        acceptedWalks.putAll(acceptedWalksIN);
//        acceptedWalks.putAll(acceptedWalksOUT);
//
//        return acceptedWalks;
//    }
    @Override
    public Set<URI> getAcceptedWalks_DIR_IN() {
        return Collections.unmodifiableSet(acceptedWalksIN);
    }

    @Override
    public Set<URI> getAcceptedWalks_DIR_OUT() {
        return Collections.unmodifiableSet(acceptedWalksOUT);
    }

    @Override
    public Set<URI> getAcceptedWalks_DIR_BOTH() {

        Set<URI> acceptedWalks = SetUtils.intersection(acceptedWalksIN, acceptedWalksOUT);
        return acceptedWalks;
    }

    @Override
    public Direction getAssociatedDirection(URI uri) {
        Direction dir = null;

        if (acceptedWalksIN.contains(uri)) {
            dir = Direction.IN;
        }
        if (acceptedWalksOUT.contains(uri)) {
            if (dir != null) {
                dir = Direction.BOTH;
            } else {
                dir = Direction.OUT;
            }
        }
        return dir;
    }

    @Override
    public void addAcceptedTraversal(URI pred, Direction dir) {
        if (dir == Direction.OUT || dir == Direction.BOTH) {
            acceptedWalksOUT.add(pred);
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {
            acceptedWalksIN.add(pred);
        }
    }

    @Override
    public void addAcceptedTraversal(Set<URI> pred, Direction dir) {
        if (dir == Direction.OUT || dir == Direction.BOTH) {
            acceptedWalksOUT.addAll(pred);
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {
            acceptedWalksIN.addAll(pred);
        }
    }

    @Override
    public void addWalkconstraints(WalkConstraint wc) {
        acceptedWalksIN.addAll(wc.getAcceptedWalks_DIR_IN());
        acceptedWalksOUT.addAll(wc.getAcceptedWalks_DIR_OUT());
    }

    @Override
    public boolean acceptOutWalks() {
        return !acceptedWalksOUT.isEmpty();
    }

    @Override
    public boolean acceptInWalks() {
        return !acceptedWalksIN.isEmpty();
    }

    @Override
    public String toString() {

        String out = "Walconstraint\n"
                + "\tAcceptedWalkIN: \n";

        for (URI e : acceptedWalksIN) {
            out += "\t\t" + e + "\n";
        }
        out += "\tAcceptedWalkOUT:\n";
        for (URI e : acceptedWalksOUT) {
            out += "\t\t" + e + "\n";
        }

        return out;
    }
}
