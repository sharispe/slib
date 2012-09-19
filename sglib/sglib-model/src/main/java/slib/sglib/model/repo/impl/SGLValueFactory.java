package slib.sglib.model.repo.impl;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.model.impl.ContextStatementImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.impl.ValueFactoryBase;
import org.openrdf.model.util.URIUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.sail.memory.model.BooleanMemLiteral;
import org.openrdf.sail.memory.model.CalendarMemLiteral;
import org.openrdf.sail.memory.model.DecimalMemLiteral;
import org.openrdf.sail.memory.model.IntegerMemLiteral;
import org.openrdf.sail.memory.model.MemLiteral;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.openrdf.sail.memory.model.NumericMemLiteral;
import org.openrdf.sail.memory.model.WeakObjectRegistry;

/**
 * Experimental attempt to design {@link MemValueFactory}
 * 
 * @author Sebastien Harispe
 *
 */
public class SGLValueFactory extends ValueFactoryBase {

	/*------------*
	 * Attributes *
	 *------------*/

	/**
	 * Registry containing the set of MemURI objects as used by a MemoryStore.
	 * This registry enables the reuse of objects, minimizing the number of
	 * objects in main memory.
	 */
	private final WeakObjectRegistry<URI> uriRegistry = new WeakObjectRegistry<URI>();

	/**
	 * Registry containing the set of MemBNode objects as used by a MemoryStore.
	 * This registry enables the reuse of objects, minimizing the number of
	 * objects in main memory.
	 */
	private final WeakObjectRegistry<BNode> bnodeRegistry = new WeakObjectRegistry<BNode>();

	/**
	 * Registry containing the set of MemLiteral objects as used by a
	 * MemoryStore. This registry enables the reuse of objects, minimizing the
	 * number of objects in main memory.
	 */
	private final WeakObjectRegistry<Literal> literalRegistry = new WeakObjectRegistry<Literal>();

	/**
	 * Registry containing the set of namespce strings as used by MemURI objects
	 * in a MemoryStore. This registry enables the reuse of objects, minimizing
	 * the number of objects in main memory.
	 */
	private final WeakObjectRegistry<String> namespaceRegistry = new WeakObjectRegistry<String>();

	/*---------*
	 * Methods *
	 *---------*/

	public void clear() {
		uriRegistry.clear();
		bnodeRegistry.clear();
		literalRegistry.clear();
		namespaceRegistry.clear();
	}

	/**
	 * Returns a previously created MemValue that is equal to the supplied value,
	 * or <tt>null</tt> if the supplied value is a new value or is equal to
	 * <tt>null</tt>.
	 * 
	 * @param value
	 *        The MemValue equivalent of the supplied value, or <tt>null</tt>.
	 * @return A previously created MemValue that is equal to <tt>value</tt>, or
	 *         <tt>null</tt> if no such value exists or if <tt>value</tt> is
	 *         equal to <tt>null</tt>.
	 */
	public Value getValue(Value value) {
		if (value instanceof Resource) {
			return getResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return getLiteral((Literal)value);
		}
		else if (value == null) {
			return null;
		}
		else {
			throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public Resource getResource(Resource resource) {
		if (resource instanceof URI) {
			return getURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return getBNode((BNode)resource);
		}
		else if (resource == null) {
			return null;
		}
		else {
			throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
		}
	}

	/**
	 * See getMemValue() for description.
	 */
	public synchronized URI getURI(URI uri) {
		return uriRegistry.get(uri);
	}

	/**
	 * See getMemValue() for description.
	 */
	public synchronized BNode getBNode(BNode bnode) {
		return bnodeRegistry.get(bnode);
	}

	/**
	 * See getMemValue() for description.
	 */
	public synchronized Literal getLiteral(Literal literal) {
			return literalRegistry.get(literal);
	}

	/**
	 * Gets all URIs that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<URI> getMemURIs() {
		return Collections.unmodifiableSet(uriRegistry);
	}

	/**
	 * Gets all bnodes that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemBNode objects.
	 */
	public Set<BNode> getBNodes() {
		return Collections.unmodifiableSet(bnodeRegistry);
	}

	/**
	 * Gets all literals that are managed by this value factory.
	 * <p>
	 * <b>Warning:</b> This method is not synchronized. To iterate over the
	 * returned set in a thread-safe way, this method should only be called while
	 * synchronizing on this object.
	 * 
	 * @return An unmodifiable Set of MemURI objects.
	 */
	public Set<Literal> getLiterals() {
		return Collections.unmodifiableSet(literalRegistry);
	}

	/**
	 * Gets or creates a MemValue for the supplied Value. If the factory already
	 * contains a MemValue object that is equivalent to the supplied value then
	 * this equivalent value will be returned. Otherwise a new MemValue will be
	 * created, stored for future calls and then returned.
	 * 
	 * @param value
	 *        A Resource or Literal.
	 * @return The existing or created MemValue.
	 */
	public Value getOrCreateValue(Value value) {
		if (value instanceof Resource) {
			return getOrCreateResource((Resource)value);
		}
		else if (value instanceof Literal) {
			return getOrCreateLiteral((Literal)value);
		}
		else {
			throw new IllegalArgumentException("value is not a Resource or Literal: " + value);
		}
	}

	/**
	 * See {@link #getOrCreateMemValue(Value)} for description.
	 */
	public Resource getOrCreateResource(Resource resource) {
		if (resource instanceof URI) {
			return getOrCreateURI((URI)resource);
		}
		else if (resource instanceof BNode) {
			return getOrCreateBNode((BNode)resource);
		}
		else {
			throw new IllegalArgumentException("resource is not a URI or BNode: " + resource);
		}
	}

	/**
	 * See {@link #getOrCreateMemValue(Value)} for description.
	 */
	public synchronized URI getOrCreateURI(URI uri) {
		URI urim = getURI(uri);

		if (urim == null) {
			// Namespace strings are relatively large objects and are shared
			// between uris
			String namespace = uri.getNamespace();
			String sharedNamespace = namespaceRegistry.get(namespace);

			if (sharedNamespace == null) {
				// New namespace, add it to the registry
				namespaceRegistry.add(namespace);
			}
			else {
				// Use the shared namespace
				namespace = sharedNamespace;
			}

			// Create a MemURI and add it to the registry
			urim = new URIImpl(namespace+uri.getLocalName());
			boolean wasNew = uriRegistry.add(urim);
			assert wasNew : "Created a duplicate MemURI for URI " + uri;
		}

		return urim;
	}

	/**
	 * See {@link #getOrCreateMemValue(Value)} for description.
	 */
	public synchronized BNode getOrCreateBNode(BNode bnode) {
		BNode memBNode = getBNode(bnode);

		if (memBNode == null) {
			memBNode = new BNodeImpl(bnode.getID());
			boolean wasNew = bnodeRegistry.add(memBNode);
			assert wasNew : "Created a duplicate MemBNode for bnode " + bnode;
		}

		return memBNode;
	}

	/**
	 * See {@link #getOrCreateMemValue(Value)} for description.
	 */
	public synchronized Literal getOrCreateLiteral(Literal literal) {
		Literal memLiteral = getLiteral(literal);

		if (memLiteral == null) {
			String label = literal.getLabel();
			URI datatype = literal.getDatatype();
			
			if (datatype != null) {
				try {
					if (XMLDatatypeUtil.isIntegerDatatype(datatype)) {
						memLiteral = new IntegerMemLiteral(this, label, literal.integerValue(), datatype);
					}
					else if (datatype.equals(XMLSchema.DECIMAL)) {
						memLiteral = new DecimalMemLiteral(this, label, literal.decimalValue(), datatype);
					}
					else if (datatype.equals(XMLSchema.FLOAT)) {
						memLiteral = new NumericMemLiteral(this, label, literal.floatValue(), datatype);
					}
					else if (datatype.equals(XMLSchema.DOUBLE)) {
						memLiteral = new NumericMemLiteral(this, label, literal.doubleValue(), datatype);
					}
					else if (datatype.equals(XMLSchema.BOOLEAN)) {
						memLiteral = new BooleanMemLiteral(this, label, literal.booleanValue());
					}
					else if (datatype.equals(XMLSchema.DATETIME)) {
						memLiteral = new CalendarMemLiteral(this, label, datatype, literal.calendarValue());
					}
					else {
						memLiteral = new LiteralImpl(label, datatype);
					}
				}
				catch (IllegalArgumentException e) {
					// Unable to parse literal label to primitive type
					memLiteral = new LiteralImpl(label, datatype);
				}
			}
			else if (literal.getLanguage() != null) {
				memLiteral = new LiteralImpl(label, literal.getLanguage());
			}
			else {
				memLiteral = new LiteralImpl(label);
			}

			boolean wasNew = literalRegistry.add(memLiteral);
			assert wasNew : "Created a duplicate MemLiteral for literal " + literal;
		}

		return memLiteral;
	}

	public synchronized URI createURI(String uri) {
		URI tempURI = new URIImpl(uri);
		return getOrCreateURI(tempURI);
	}

	public synchronized URI createURI(String namespace, String localName) {
		URI tempURI = null;

		// Reuse supplied namespace and local name strings if possible
		if (URIUtil.isCorrectURISplit(namespace, localName)) {
			if (namespace.indexOf(':') == -1) {
				throw new IllegalArgumentException("Not a valid (absolute) URI: " + namespace + localName);
			}

			tempURI = new URIImpl(namespace + localName);
		}
		else {
			tempURI = new URIImpl(namespace + localName);
		}

		return getOrCreateURI(tempURI);
	}

	public synchronized BNode createBNode(String nodeID) {
		BNode tempBNode = new BNodeImpl(nodeID);
		return getOrCreateBNode(tempBNode);
	}

	public synchronized Literal createLiteral(String value) {
		Literal tempLiteral = new LiteralImpl(value);
		return getOrCreateLiteral(tempLiteral);
	}

	public synchronized Literal createLiteral(String value, String language) {
		Literal tempLiteral = new LiteralImpl(value, language);
		return getOrCreateLiteral(tempLiteral);
	}

	public synchronized Literal createLiteral(String value, URI datatype) {
		Literal tempLiteral = new LiteralImpl(value, datatype);
		return getOrCreateLiteral(tempLiteral);
	}

	@Override
	public synchronized Literal createLiteral(boolean value) {
		Literal newLiteral = new BooleanLiteralImpl(value);
		return getSharedLiteral(newLiteral);
	}

	@Override
	protected synchronized Literal createIntegerLiteral(Number n, URI datatype) {
		MemLiteral newLiteral = new IntegerMemLiteral(this, BigInteger.valueOf(n.longValue()), datatype);
		return getSharedLiteral(newLiteral);
	}

	@Override
	protected synchronized Literal createFPLiteral(Number n, URI datatype) {
		MemLiteral newLiteral = new NumericMemLiteral(this, n, datatype);
		return getSharedLiteral(newLiteral);
	}

	@Override
	public synchronized Literal createLiteral(XMLGregorianCalendar calendar) {
		MemLiteral newLiteral = new CalendarMemLiteral(this, calendar);
		return getSharedLiteral(newLiteral);
	}

	private Literal getSharedLiteral(Literal newLiteral) {
		Literal sharedLiteral = literalRegistry.get(newLiteral);

		if (sharedLiteral == null) {
			boolean wasNew = literalRegistry.add(newLiteral);
			assert wasNew : "Created a duplicate MemLiteral for literal " + newLiteral;
			sharedLiteral = newLiteral;
		}

		return sharedLiteral;
	}

	public Statement createStatement(Resource subject, URI predicate, Value object) {
		return new StatementImpl(subject, predicate, object);
	}

	public Statement createStatement(Resource subject, URI predicate, Value object, Resource context) {
		if (context == null) {
			return new StatementImpl(subject, predicate, object);
		}
		else {
			return new ContextStatementImpl(subject, predicate, object, context);
		}
	}

}
