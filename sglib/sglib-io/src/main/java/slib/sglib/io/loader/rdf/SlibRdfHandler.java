package slib.sglib.io.loader.rdf;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.impl.graph.elements.EdgeTyped;
import slib.sglib.model.impl.graph.elements.VertexTyped;

/**
 *
 * @author seb
 */
public class SlibRdfHandler implements RDFHandler {

    G g;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    int count = 0;

    /**
     *
     * @param g
     */
    public SlibRdfHandler(G g) {
        this.g = g;
    }

    @Override
    public void startRDF() throws RDFHandlerException {

        logger.info("Starting Processing");
        count = 0;
    }

    @Override
    public void endRDF() throws RDFHandlerException {

        logger.info("Ending Processing " + count + " statements loaded ");
        logger.info("vertices: " + g.getV().size());
        logger.info("edges   : " + g.getE().size());
    }

    @Override
    public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
    }

    @Override
    public void handleStatement(Statement st) throws RDFHandlerException {
        
        V subject = g.getV(st.getSubject());

        if (subject == null) {
            subject = new VertexTyped(g, st.getSubject(), null);
            g.addV(subject);
        }
        
        V object  = g.getV(st.getObject());

        if (object == null) {
            object = new VertexTyped(g, st.getObject(), null);
        }

        E e = new EdgeTyped(subject, object, st.getPredicate());

        count++;
        
        if(count % 100000 == 0){
            logger.info(count+" statements already loaded");
            logger.info("Number of vertices: "+g.getV().size());
            logger.info("Number of edges   : "+g.getE().size());
        }

        g.addE(e);
    }

    @Override
    public void handleComment(String comment) throws RDFHandlerException {
    }
}
