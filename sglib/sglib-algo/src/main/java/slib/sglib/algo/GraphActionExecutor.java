package slib.sglib.algo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.Sail;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.extraction.rvf.RVF_TAX;
import slib.sglib.algo.inf.TypeInferencer;
import slib.sglib.algo.reduction.dag.GraphReduction_DAG;
import slib.sglib.algo.reduction.dag.GraphReduction_Transitive;
import slib.sglib.algo.utils.GAction;
import slib.sglib.algo.utils.GActionType;
import slib.sglib.algo.utils.RooterDAG;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.SGLcst;
import slib.sglib.model.voc.SGLVOC;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.Util;

import slib.sglib.model.repo.DataFactory;


public class GraphActionExecutor {

	static Logger logger = LoggerFactory.getLogger(GraphActionExecutor.class);

	public static void applyAction(DataFactory factory, GAction action,G g) throws SLIB_Ex_Critic{

		GActionType actionType = action.type;

		if(actionType == GActionType.TRANSITIVE_REDUCTION)
			transitive_reduction(factory, action,g);

		else if(actionType == GActionType.REROOTING)
			rerooting(factory, action,g);

		else if(actionType == GActionType.TYPE_VERTICES)
			type_vertices(factory, action,g);

		else if(actionType == GActionType.RDFS_INFERENCE)
			rdfsInference(factory, action,g);


		else if(actionType == GActionType.VERTICES_REDUCTION)
			verticeReduction(factory, action,g);

		else
			throw new SLIB_Ex_Critic("Unknow action "+action.type);
	}

	private static void verticeReduction(DataFactory factory, GAction action, G g) throws SLIB_Ex_Critic {

		logger.debug("Starting "+GActionType.VERTICES_REDUCTION);

		String regex     = (String) action.getParameter("regex");
		String vocVal    = (String) action.getParameter("vocabulary");
		String file_uris = (String) action.getParameter("file_uris");


		Set<V> toRemove = new HashSet<V>();

		if(regex != null){

			logger.debug("Applying regex: "+regex);
			Pattern pattern;

			try{
				pattern = Pattern.compile(regex);
			}
			catch(PatternSyntaxException e){
				throw new SLIB_Ex_Critic("The specified regex '"+regex+"' is invalid: "+e.getMessage());
			}


			Matcher matcher;

			for(V v : g.getV()){
				matcher = pattern.matcher(v.getValue().stringValue());

				if(matcher.find()){
					toRemove.add(v);
					logger.debug("regex matches: "+v);
				}
			}

			logger.debug("Vertices to remove: "+toRemove.size()+"/"+g.getV().size());


			g.removeV(toRemove);

			logger.debug("ending "+GActionType.VERTICES_REDUCTION);
		}
		else if(vocVal != null){

			String[] vocs = vocVal.split(",");

			for(String voc : vocs){

				if(voc.trim().equals("RDF")){
					logger.info("Removing RDF vocabulary");
					removeVocURIs(factory, getRDFVocURIs(), g);
				}
				else if(voc.trim().equals("RDFS")){
					logger.info("Removing RDFS vocabulary");
					removeVocURIs(factory, getRDFSVocURIs(), g);
				}
				else if(voc.trim().equals("OWL")){
					logger.info("Removing OWL vocabulary");
					removeVocURIs(factory, getOWLVocURIs(), g);
				}
			}
		}
		else if(file_uris != null){

			String[] files = file_uris.split(",");

			for(String f : files){

				logger.info("Removing Uris specified in "+f);

				try {

					FileInputStream fstream = new FileInputStream(f.trim());
					DataInputStream in 		= new DataInputStream(fstream);
					BufferedReader br 		= new BufferedReader(new InputStreamReader(in));

					String line;

					while ((line = br.readLine()) != null)   {

						line = line.trim();
						V v = g.getV( factory.createURI(line) );
						if(v != null) g.removeV(v);
					}
					in.close();
				} catch (IOException e) {
					throw new SLIB_Ex_Critic(e.getMessage());
				}
			}
		}

	}




	private static void rdfsInference(DataFactory factory, GAction action, G g) throws SLIB_Ex_Critic{

		logger.debug("Apply inference engine");
		Sail sail = new ForwardChainingRDFSInferencer(g);
		Repository repo = new SailRepository(sail);

		repo = new SailRepository(new ForwardChainingRDFSInferencer(g));
		try {

			repo.initialize();
			RepositoryConnection con = repo.getConnection();
			con.setAutoCommit(false);

			for(E e : g.getE())
				con.add(factory.createStatement((Resource)e.getSource().getValue(), e.getURI(),e.getTarget().getValue()));

			con.commit();
			con.close();
			repo.shutDown();

		} catch (RepositoryException e) {
			throw new SLIB_Ex_Critic(e.getMessage());
		} 

	}

	/**
	 * Vocabulary associated to RDF
	 * @return the strings associated to the URIs of the RDF vocabulary
	 */
	private static String[] getRDFVocURIs() {

		return new String[]{
				"http://www.w3.org/1999/02/22-rdf-syntax-ns#first"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#type"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#rest"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#value"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#Property"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#object"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#List"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#subject"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#langString"
				,"http://www.w3.org/1999/02/22-rdf-syntax-ns#li"
		};

	}

	private static String[] getRDFSVocURIs() {
		return new String[]{
				"http://www.w3.org/2000/01/rdf-schema#subClassOf"
				,"http://www.w3.org/2000/01/rdf-schema#label"
				,"http://www.w3.org/2000/01/rdf-schema#Class"
				,"http://www.w3.org/2000/01/rdf-schema#member"
				,"http://www.w3.org/2000/01/rdf-schema#comment"
				,"http://www.w3.org/2000/01/rdf-schema#Literal"
				,"http://www.w3.org/2000/01/rdf-schema#seeAlso"
				,"http://www.w3.org/2000/01/rdf-schema#Resource"
				,"http://www.w3.org/2000/01/rdf-schema#Container"
				,"http://www.w3.org/2000/01/rdf-schema#isDefinedBy"
				,"http://www.w3.org/2000/01/rdf-schema#domain"
				,"http://www.w3.org/2000/01/rdf-schema#subPropertyOf"
				,"http://www.w3.org/2000/01/rdf-schema#Datatype"
				,"http://www.w3.org/2000/01/rdf-schema#range"
				,"http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty"
		};
	}

	private static String[] getOWLVocURIs() {
		return new String[]{
				"http://www.w3.org/2002/07/owl#AllDifferent"
				,"http://www.w3.org/2002/07/owl#allValuesFrom"
				,"http://www.w3.org/2002/07/owl#AnnotationProperty"
				,"http://www.w3.org/2002/07/owl#backwardCompatibleWith"
				,"http://www.w3.org/2002/07/owl#cardinality"
				,"http://www.w3.org/2002/07/owl#Class"
				,"http://www.w3.org/2002/07/owl#complementOf"
				,"http://www.w3.org/2002/07/owl#DatatypeProperty"
				,"http://www.w3.org/2002/07/owl#DeprecatedClass"
				,"http://www.w3.org/2002/07/owl#DeprecatedProperty"
				,"http://www.w3.org/2002/07/owl#differentFrom"
				,"http://www.w3.org/2002/07/owl#disjointWith"
				,"http://www.w3.org/2002/07/owl#distinctMembers"
				,"http://www.w3.org/2002/07/owl#equivalentClass"
				,"http://www.w3.org/2002/07/owl#equivalentProperty"
				,"http://www.w3.org/2002/07/owl#FunctionalProperty"
				,"http://www.w3.org/2002/07/owl#hasValue"
				,"http://www.w3.org/2002/07/owl#imports"
				,"http://www.w3.org/2002/07/owl#incompatibleWith"
				,"http://www.w3.org/2002/07/owl#Individual"
				,"http://www.w3.org/2002/07/owl#intersectionOf"
				,"http://www.w3.org/2002/07/owl#InverseFunctionalProperty"
				,"http://www.w3.org/2002/07/owl#inverseOf"
				,"http://www.w3.org/2002/07/owl#maxCardinality"
				,"http://www.w3.org/2002/07/owl#minCardinality"
				,"http://www.w3.org/2002/07/owl#ObjectProperty"
				,"http://www.w3.org/2002/07/owl#oneOf"
				,"http://www.w3.org/2002/07/owl#onProperty"
				,"http://www.w3.org/2002/07/owl#Ontology"
				,"http://www.w3.org/2002/07/owl#OntologyProperty"
				,"http://www.w3.org/2002/07/owl#priorVersion"
				,"http://www.w3.org/2002/07/owl#Restriction"
				,"http://www.w3.org/2002/07/owl#sameAs"
				,"http://www.w3.org/2002/07/owl#someValuesFrom"
				,"http://www.w3.org/2002/07/owl#SymmetricProperty"
				,"http://www.w3.org/2002/07/owl#TransitiveProperty"
				,"http://www.w3.org/2002/07/owl#unionOf"
				,"http://www.w3.org/2002/07/owl#versionInfo"
		};
	}



	/**
	 * Try to remove the vertices associated to the given URIs specified as strings.
	 * If a string is not a valid URI a {@link IllegalArgumentException} can be throw.  
	 * @param toRemove set of strings corresponding to the URIs to remove
	 * @param g the graph in which the treatment require to be performed.
	 */
	private static void removeVocURIs(DataFactory factory, String[] toRemove, G g) {

		for(String s : toRemove){
			V v = g.getV( factory.createURI(s) );
			if(v != null) g.removeV(v);
		}
	}

	private static void type_vertices(DataFactory factory,GAction action, G g) throws SLIB_Ex_Critic {

		logger.debug("Start Typing vertices");

		TypeInferencer inf = new TypeInferencer();
		boolean complete = inf.inferTypes(g, false);

		String fails = (String) action.getParameter("stopfail");

		if(fails != null){
			if(Util.stringToBoolean(fails) && !complete)
				throw new SLIB_Ex_Critic("Type inferencer fails to resolve all types...");
		}

		logger.debug("End Typing vertices");
	}

	private static void rerooting(DataFactory factory, GAction action, G g) throws SLIB_Ex_Critic {


		logger.info("Rerooting");

		// Re-rooting
		String rootURIs = (String) action.getParameter("root_uri");

		logger.info("Fetching root node, uri: "+rootURIs);


		if(rootURIs != null && !rootURIs.isEmpty()){

			if(rootURIs.equals(SGLcst.FICTIVE_ROOT)){
                                URI rootURI = factory.createURI(rootURIs);
				RooterDAG.rootUnderlyingTaxonomicDAG(g, rootURI);
                        }
			else{
				URI rootURI = factory.createURI(rootURIs);

				if(g.getV(rootURI) == null)
					throw new SLIB_Ex_Critic("Cannot resolve specified root:"+rootURI);
				else{
					logger.info("Reduce graph considering root "+rootURI);
					GraphReduction_DAG.taxonomicReduction(g, rootURI, false);
				}
			}
		}
		else{
			throw new SLIB_Ex_Critic("Please specify a 'root_uri' associated to the action rerooting");
		}

	}

	private static void transitive_reduction(DataFactory factory, GAction action, G g) throws SLIB_Ex_Critic {

		String target = (String) action.getParameter("target");

		logger.debug("Transitive Reduction");
		logger.debug("Target: "+target);


		String[] admittedTarget = {"CLASSES","INSTANCES"};

		if(!Arrays.asList(admittedTarget).contains(target)){
			throw new SLIB_Ex_Critic("Unknow target "+target+", admitted "+Arrays.asList(admittedTarget));
		}
		else if(target.equals("CLASSES"))
			GraphReduction_Transitive.process(g);

		else if(target.equals("INSTANCES"))
			transitive_reductionInstance(action, g);


	}

	private static void transitive_reductionInstance(GAction action, G g) throws SLIB_Ex_Critic {

		// --------------- TO_SPLIT

		int invalidInstanceNb = 0;
		int annotNbBase 	= 0;
		int annotDeleted    = 0;

		logger.info("Cleaning RDF.TYPE of "+g.getURI());
		System.out.println(g);

		RVF_TAX rvf  = new RVF_TAX(g, Direction.IN);

		// Retrieve descendants for all vertices
		Map<V, Set<V>> descs =  rvf.getAllRVClass();

		Set<V> entities = g.getV(VType.INSTANCE);

		for (V instance : entities){

			HashSet<E> redundants = new HashSet<E>();
			Set<E> eToclasses = g.getE(RDF.TYPE,instance,Direction.OUT);

			annotNbBase += eToclasses.size();

			for (E e : eToclasses) {

				if(!redundants.contains(e)){

					for (E e2 : eToclasses) {
						// TODO optimize Transitive reduction or for(i ... for(j=i+1
						if(e != e2 && 
								!redundants.contains(e2) && 
								descs.get(e.getTarget()).contains(e2.getTarget()))
							redundants.add(e2);
					}
				}
			}

			if(redundants.size() != 0){
				g.removeE(redundants);
				invalidInstanceNb++;
				annotDeleted += redundants.size();
			}
		}

		double invalidInstanceP = 0;
		if(entities.size() > 0)
			invalidInstanceP = invalidInstanceNb*100/entities.size();

		double annotDelP = 0;
		if(annotNbBase > 0)
			annotDelP = annotDeleted*100/annotNbBase;

		logger.info("Number of instance containing abnormal annotation: "+invalidInstanceNb+"/"+entities.size()+"  i.e. ("+invalidInstanceP+"%)");
		logger.info("Number of annotations: "+annotNbBase+", deleted: "+annotDeleted+" ("+(annotDelP)+"%), current annotation number "+(annotNbBase-annotDeleted));


	}

	public static void applyActions(DataFactory factory, Collection<GAction> actions,G g) throws SLIB_Ex_Critic{

		for(GAction action : actions)
			applyAction(factory,action, g);
	}

}
