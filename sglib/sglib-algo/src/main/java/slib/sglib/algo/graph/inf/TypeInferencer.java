package slib.sglib.algo.graph.inf;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.algo.graph.inf.utils.VRule;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;

/**
 *
 * @author Harispe Sébastien
 */
public class TypeInferencer {

    G g;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * @param g
     * @param clearExistingTypes
     * @return
     */
    public boolean inferTypes(G g, boolean clearExistingTypes) {

        logger.info("Type inference");

        this.g = g;

        long total = g.getV().size();
        long toResolve = 0;

        for (V v : g.getV()) {

            if (v.getType() != VType.UNDEFINED) {

                if (clearExistingTypes) {
                    v.setType(VType.UNDEFINED);
                    toResolve++;
                }
            } else {
                toResolve++;
            }
        }

        logger.info("Type to resolve " + toResolve);


        boolean stable = false;
        int it = 1;

        while (!stable) {

            logger.info("Iteration " + it + ", undefined types: " + toResolve + "/" + total);
            it++;

            long infCurrentIt = 0; // inference next it

            for (E e : g.getE()) {

                V s = e.getSource();
                V o = e.getTarget();

                if (s.getType() == VType.UNDEFINED && (inferenceTypeInner(true, e) == true)) {
                    infCurrentIt++;
                }

                if (o.getType() == VType.UNDEFINED && (inferenceTypeInner(false, e) == true)) {
                    infCurrentIt++;
                }

                if (toResolve - infCurrentIt == 0) {
                    stable = true;
                    break;
                }
            }

            toResolve -= infCurrentIt;
//            logger.debug("Inference made " + infCurrentIt);

            if (infCurrentIt == 0) {
                stable = true;
            }
        }
        logger.info("Final undefined: " + toResolve + "/" + total);

//        if (toResolve != 0) {
//            for (V v : g.getV()) {
//                if (v.getType() == VType.UNDEFINED) {
//                    logger.debug(v.getType() + "" + "\t\t" + v.getValue());
//                }
//
//            }
//        }

        return toResolve == 0;

    }

    private boolean inferenceTypeInner(boolean processSubject, E e) {

        URI p = e.getURI();
        VType type = null;

        if (processSubject) {

            V s = e.getSource();

            if (p.equals(RDF.TYPE)) {

                Value oURI = e.getTarget().getValue();

                if (!(oURI.equals(RDFS.CLASS) || oURI.equals(OWL.CLASS)) && !oURI.equals(RDFS.RESOURCE) && !oURI.equals(RDF.PROPERTY)) {
                    type = VType.INSTANCE;
                }
            } else if (p.equals(RDFS.SUBCLASSOF)) {
                type = VType.CLASS;
            } else if (p.equals(RDFS.DOMAIN) || p.equals(RDFS.RANGE)) {
                type = VType.PROPERTY;
            } else {
                V o = e.getTarget();
                if (o.getType() == VType.INSTANCE) {
                    type = VType.INSTANCE;
                }
            }

            if (type != null) {

//                logger.debug("SUBJECT " + s + "  " + type + "  >" + e);
                s.setType(type);
                return true;
            }
        } else { // process object

            V o = e.getTarget();


            if (p.equals(RDFS.SUBCLASSOF)) {
                type = VType.CLASS;
            } else if (p.equals(RDF.TYPE)) {
                type = VType.CLASS;
            } else if (p.equals(RDFS.DOMAIN) || p.equals(RDFS.RANGE)) {
                type = VType.CLASS;
            } else if (o.getValue() instanceof Literal) {
                type = VType.LITERAL;
            }

            if (type != null) {
//                logger.debug("OBJECT " + o + "  " + type + "  >" + e);
                o.setType(type);
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param g
     * @param rule
     */
    public void applyVrule(G g, VRule rule) {
        for (V v : g.getV()) {

            if (v.getType() == null) {
                v.setType(rule.apply(v));
            }
        }
    }
}
