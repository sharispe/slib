package slib.sglib.algo.graph.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;

/**
 *
 * Class used to facilitate the use of WalkConstraint over graph, only considering the vertices associated to {@link VType#CLASS}.
 *
 * @author Harispe Sébastien
 */
public class WalkConstraintTax implements WalkConstraints {

    Map<URI, Direction> acceptedWalksIN = new HashMap<URI, Direction>();
    Map<URI, Direction> acceptedWalksOUT = new HashMap<URI, Direction>();
    Set<URI> validPredicates = new HashSet<URI>();

    /**
     * Build an instance of walk constraint considering the walk rule.
     * @param walkRules a map containing the predicate to consider and their respective direction
     */
    public WalkConstraintTax(Map<URI, Direction> walkRules) {
        
        for (Entry<URI, Direction> e : walkRules.entrySet()) {

            Direction dir = e.getValue();

            if (dir == Direction.IN || dir == Direction.BOTH) {
                acceptedWalksIN.put(e.getKey(), dir);
            }

            if (dir == Direction.OUT || dir == Direction.BOTH) {
                acceptedWalksOUT.put(e.getKey(), dir);
            }
        }

        validPredicates.addAll(acceptedWalksIN.keySet());
        validPredicates.addAll(acceptedWalksOUT.keySet());
    }

    /**
     * Build an instance of walk constraint considering the given predicate URI and direction.
     * @param acceptedPredicate the predicate URI to consider
     * @param dir the direction associated
     */
    public WalkConstraintTax(URI acceptedPredicate, Direction dir) {
        if (dir == Direction.IN || dir == Direction.BOTH) {
            acceptedWalksIN.put(acceptedPredicate, dir);
        }

        if (dir == Direction.OUT || dir == Direction.BOTH) {
            acceptedWalksOUT.put(acceptedPredicate, dir);
        }

        validPredicates.add(acceptedPredicate);
    }

    @Override
    public boolean respectConstaints(E e, Direction dir) {
        boolean valid = false;

        if (dir == Direction.IN || dir == Direction.BOTH) {
            valid = acceptedWalksIN.keySet().contains(e.getURI());
        }

        if (dir == Direction.OUT || (!valid && dir == Direction.BOTH)) {
            valid = acceptedWalksOUT.keySet().contains(e.getURI());
        }

        return valid;
    }

    @Override
    public Set<URI> getAcceptedPredicates() {
        return validPredicates;
    }

    @Override
    public Map<URI, Direction> getAcceptedTraversals() {

        Map<URI, Direction> acceptedWalks = new HashMap<URI, Direction>();

        acceptedWalks.putAll(acceptedWalksIN);
        acceptedWalks.putAll(acceptedWalksOUT);

        return acceptedWalks;
    }

    @Override
    public Set<URI> getAcceptedWalks_DIR_IN() {
        return acceptedWalksIN.keySet();
    }

    @Override
    public Set<URI> getAcceptedWalks_DIR_OUT() {
        return acceptedWalksOUT.keySet();
    }

    @Override
    public Set<URI> getAcceptedWalks_DIR_BOTH() {

        Set<URI> acceptedWalks = new HashSet<URI>();
        acceptedWalks.addAll(acceptedWalksIN.keySet());
        acceptedWalks.addAll(acceptedWalksOUT.keySet());

        return acceptedWalks;
    }

    @Override
    public WalkConstraints getInverse(boolean includeBOTHpredicates) {

        Map<URI, Direction> oppositeAcceptedWalks = new HashMap<URI, Direction>();

        for (Entry<URI, Direction> e : acceptedWalksIN.entrySet()) {
            oppositeAcceptedWalks.put(e.getKey(), Direction.OUT);
        }

        for (Entry<URI, Direction> e : acceptedWalksOUT.entrySet()) {
            oppositeAcceptedWalks.put(e.getKey(), Direction.IN);
        }

        return new WalkConstraintTax(oppositeAcceptedWalks);
    }

    @Override
    public Direction getAssociatedDirection(URI uri) {
        Direction dir = acceptedWalksIN.get(uri);

        if (dir == null && acceptedWalksOUT.containsKey(uri)) {
            dir = Direction.OUT;
        } else if (dir != null && acceptedWalksOUT.containsKey(uri)) {
            dir = Direction.BOTH;
        }
        return dir;
    }

    

    @Override
    public void addAcceptedTraversal(URI pred, Direction dir) {
        // TODO Auto-generated method stub Aug 29, 2012
        throw new UnsupportedOperationException("Not supported yet.");

    }
    
    @Override
    public String toString() {

        String out = "Walconstraint\n"
                + "\tAcceptedWalkIN: \n";

        for (Entry<URI, Direction> e : acceptedWalksIN.entrySet()) {
            out += "\t\t" + e.getKey() + "\t" + e.getValue() + "\n";
        }
        out += "\tAcceptedWalkOUT:\n";
        for (Entry<URI, Direction> e : acceptedWalksOUT.entrySet()) {
            out += "\t\t" + e.getKey() + "\t" + e.getValue() + "\n";
        }

        return out;
    }

    @Override
    public boolean respectConstaints(URI v) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
