package slib.sglib.model.graph.impl.memory;

import info.aduna.iteration.CloseableIteration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.helpers.SailBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.impl.EdgeTyped;
import slib.sglib.model.graph.elements.impl.VertexTyped;
import slib.sglib.model.repo.impl.DataFactoryMemory;


public class GSailConnection extends NotifyingSailConnectionBase implements InferencerConnection {

	G graph;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private boolean forceInference = true;
    private boolean addition;
    private boolean deletion;
	
	public GSailConnection(G sailBase) {
		super((SailBase) sailBase);
		this.graph = sailBase;

	}

	@Override
	protected void closeInternal() throws SailException {
		// do nothing ?
	}

	@Override
	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings,
			boolean includeInferred) throws SailException {
		
		throw new UnsupportedOperationException("Not supported yet.");
		

	}

	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
			throws SailException {
		throw new UnsupportedOperationException("Not supported yet.");

	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(
			Resource subj, URI pred, Value obj, boolean includeInferred,
			Resource... contexts) throws SailException {
		
		if(!includeInferred)
			throw new SailException("No Distinction between inferred and non inferred possible...");

		
//		System.out.println(" - Get Statement Internal q subj: "+subj+"  pred: "+pred+"  Value "+obj+" Inferred "+includeInferred);
		
		if (0 == contexts.length) {
			
			Collection<E> resultEdges = queryEdges(subj, pred, obj);
			CloseableIteration<Statement, SailException> iterations = new StableStatementIteration(resultEdges);
			return iterations;
			
		} else {
			throw new UnsupportedOperationException("Contexts are not supported...");
		}
	}

	private Collection<E> queryEdges(Resource subj, URI pred, Value obj) {
		
		List<E> valids = new ArrayList<E>();
		
		boolean valid;
		
		for(E e : graph.getE()){
			
			valid = true;
			
			if(subj != null && !((Resource) e.getSource().getValue()).equals(subj))
				valid = false;
			
			if(valid && pred != null && !(e.getURI()).equals(pred))
				valid = false;
			
			if(valid && obj != null && !(e.getTarget().getValue()).equals(obj))
				valid = false;
			
			
			if(valid)
				valids.add(e);
		}
		return valids;
	}

	@Override
	protected long sizeInternal(Resource... contexts) throws SailException {
		return graph.getNumberEdges();
	}

	@Override
	protected void startTransactionInternal() throws SailException {
        addition = false;
        deletion = false;
	}

	@Override
	protected void commitInternal() throws SailException {
		
		
		
		if(!forceInference)
			deletion = true;
		
		else if(!addition && !deletion) return;

		logger.debug("Commit Internal");
		DefaultSailChangedEvent e = new DefaultSailChangedEvent(graph);
		e.setStatementsAdded(addition);
		e.setStatementsRemoved(deletion);

		((NotifyingSailBase)graph).notifySailChanged(e);

	}

	@Override
	protected void rollbackInternal() throws SailException {
		throw new SailException("rollbackInternal are not supported ...");
	}

	private E createEdge(Resource subj, URI pred, Value obj, Resource... contexts){

		V vsub = graph.getV(  (Value) subj);
		V vobj = graph.getV(obj);

		if(vsub == null) vsub = graph.addV( new VertexTyped(graph, (Value) subj, null) );
		if(vobj == null) vobj = graph.addV( new VertexTyped(graph, obj, null) );

		E edge = new EdgeTyped(vsub, vobj, pred);

		return edge;
	}

	@Override
	protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts) throws SailException {


		graph.addE( createEdge(subj, pred, obj, contexts));

		if (hasConnectionListeners()) {
			Statement s = DataFactoryMemory.getSingleton().createStatement(subj, pred, obj, null);
			notifyStatementAdded(s);
		}

		addition = true;
	}

	@Override
	protected void removeStatementsInternal(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException {
		graph.removeE( createEdge(subj, pred, obj, contexts) );

		if (hasConnectionListeners()) {
			Statement s = DataFactoryMemory.getSingleton().createStatement(subj, pred, obj, null);
			notifyStatementRemoved(s);
		}
		deletion = true;
	}

	@Override
	protected void clearInternal(Resource... contexts) throws SailException {
		// do nothing ?
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
			throws SailException {
		// TODO Auto-generated method stub Sep 7, 2012
		throw new UnsupportedOperationException("Not supported yet.");

	}

	@Override
	protected String getNamespaceInternal(String prefix) throws SailException {
		// TODO Auto-generated method stub Sep 7, 2012
		throw new UnsupportedOperationException("Not supported yet.");

	}

	@Override
	protected void setNamespaceInternal(String prefix, String name)
			throws SailException {
		// TODO Auto-generated method stub Sep 7, 2012
		throw new UnsupportedOperationException("Not supported yet.");

	}

	@Override
	protected void removeNamespaceInternal(String prefix) throws SailException {
		// TODO Auto-generated method stub Sep 7, 2012
		throw new UnsupportedOperationException("Not supported yet.");

	}

	@Override
	protected void clearNamespacesInternal() throws SailException {
		// TODO Auto-generated method stub Sep 7, 2012
		throw new UnsupportedOperationException("Not supported yet.");

	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException {

//		System.out.println("Add "+subj+" pred "+pred+" Obj "+obj);
		graph.addE( createEdge(subj, pred, obj, contexts));
		return true;
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj,
			Resource... contexts) throws SailException {
		removeStatementsInternal(subj, pred, obj, contexts);
		return true;
	}

	public void clearInferred(Resource... contexts) throws SailException {
		throw new UnsupportedOperationException("Inferred statements are not distinguished from original statements...");
	}

	public void flushUpdates() throws SailException {
		// do nothing ?
	}
	
	
	
	/**
	 * Blueprints API Copy and Paste
	 */

	  private class StableStatementIteration implements CloseableIteration<Statement, SailException> {
	        private final Iterator<E> iter;
	        private boolean closed = false;

	        public StableStatementIteration(final Iterable<E> iterator) {
	            iter = iterator.iterator();
	        }

	        public void close() throws SailException {
	            if (!closed) {
	                closed = true;
	            }
	        }

	        public boolean hasNext() throws SailException {
	            // Note: this used to throw an IllegalStateException if the iteration had already been closed,
	            // but such is not the behavior of Aduna's LookAheadIteration, which simply does not provide any more
	            // elements if the iteration has already been closed.
	            // The CloseableIteration API says nothing about what to expect from a closed iteration,
	            // so the behavior of LookAheadIteration will be taken as normative.
	            return !closed && iter.hasNext();
	        }

	        public Statement next() throws SailException {
	            if (closed) {
	                throw new IllegalStateException("already closed");
	            }

	            E e = iter.next();

	            Statement s = DataFactoryMemory.getSingleton().createStatement(
	            		(Resource) e.getSource().getValue(), 
	            		(URI) e.getURI(),
	            		(Value) e.getTarget().getValue(), null);

	            return s;
	        }

	        public void remove() throws SailException {
	            throw new UnsupportedOperationException();
	        }
	    }
}
