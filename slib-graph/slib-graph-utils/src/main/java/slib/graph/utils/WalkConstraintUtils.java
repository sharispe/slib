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

import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class WalkConstraintUtils {

    /**
     * Build a {@link WalkConstraint} object corresponding to the inverse of the
     * current object.
     *
     * Definition of an inverse follows:
     * <ul>
     * <li>Accepted Predicate are unchanged</li>
     * <li>Direction associated to accepted predicate are the inverse of those
     * defined: IN -> OUT, OUT -> IN, BOTH -> BOTH (if boolean
     * includeBOTHpredicate is set to true)
     * </li>
     * </ul>
     *
     * @param includeBOTHpredicate defines if the predicated associated to a
     * direction {@link Direction#BOTH} must also be included
     * @return a {@link WalkConstraint} object corresponding to the inverse of
     * this one, see definition below
     */
    public static WalkConstraint getInverse(WalkConstraint wc, boolean includeBOTHpredicate) {

        Map<URI, Direction> oppositeAcceptedWalks = new HashMap<URI, Direction>();

        for (URI e : wc.getAcceptedWalks_DIR_IN()) {
            oppositeAcceptedWalks.put(e, Direction.OUT);
        }

        for (URI e : wc.getAcceptedWalks_DIR_OUT()) {
            oppositeAcceptedWalks.put(e, Direction.IN);
        }

        if (includeBOTHpredicate) {
            for (URI e : wc.getAcceptedWalks_DIR_BOTH()) {
                oppositeAcceptedWalks.put(e, Direction.BOTH);
            }
        }
        return new WalkConstraintGeneric(oppositeAcceptedWalks);
    }

    public static WalkConstraint copy(WalkConstraint walkConstraint) {

        WalkConstraintGeneric newwc = new WalkConstraintGeneric();
        newwc.addAcceptedTraversal(walkConstraint.getAcceptedWalks_DIR_IN(), Direction.IN);
        newwc.addAcceptedTraversal(walkConstraint.getAcceptedWalks_DIR_OUT(), Direction.OUT);
        newwc.addAcceptedTraversal(walkConstraint.getAcceptedWalks_DIR_BOTH(), Direction.BOTH);
        
        return newwc;

    }
}
