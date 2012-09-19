package slib.sglib.io.loader.rdf;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.impl.EdgeTyped;
import slib.sglib.model.graph.elements.impl.VertexTyped;

public class SglRdfHandler implements RDFHandler {
	
	G g;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	int count = 0;
	
	public SglRdfHandler(G g){
		this.g = g;
	}

	public void startRDF() throws RDFHandlerException {
		
		logger.debug("Starting Processing");
		count = 0;
	}

	public void endRDF() throws RDFHandlerException {
		
		logger.debug("Ending Processing "+count+" statements loaded ");
		logger.debug("vertices: "+g.getV().size());
		logger.debug("edges   : "+g.getE().size());
	}

	public void handleNamespace(String prefix, String uri) throws RDFHandlerException {
		
	}

	public void handleStatement(Statement st) throws RDFHandlerException {
 
			V subject = g.getV(st.getSubject());
			V object  = g.getV(st.getObject());
			
			if(subject == null)
				subject = new VertexTyped(g, st.getSubject(), null);
			
			if(object == null)
				object  = new VertexTyped(g, st.getObject(), null);
			
			E e = new EdgeTyped(subject, object, st.getPredicate());
			
			count++;
			
			g.addE(e);
	}

	public void handleComment(String comment) throws RDFHandlerException {
	}

}
