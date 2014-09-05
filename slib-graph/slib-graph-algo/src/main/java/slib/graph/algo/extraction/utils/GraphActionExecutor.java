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
package slib.graph.algo.extraction.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.accessor.GraphAccessor;
import slib.graph.algo.extraction.rvf.DescendantEngine;
import slib.graph.algo.extraction.rvf.RVF_TAX;
import slib.graph.algo.reduction.dag.GraphReduction_Transitive;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.impl.graph.elements.Edge;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Class managing the execution of {@link GAction} over a graph.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphActionExecutor {

    static Logger logger = LoggerFactory.getLogger(GraphActionExecutor.class);

    public final static String REROOT_UNIVERSAL_ROOT_FLAG = "__FICTIVE__";

    /**
     * Apply the given action to the graph.
     *
     * @param action the action to perform
     * @param g the graph on which the action must be performed
     * @throws SLIB_Ex_Critic
     */
    public static void applyAction(GAction action, G g) throws SLIB_Ex_Critic {
        applyAction(URIFactoryMemory.getSingleton(), action, g);
    }

    /**
     * Apply an action to the graph.
     *
     * @param factory the factory to consider if element requires to be
     * generated (e.g. {@link URI})
     * @param action the action to perform
     * @param g the graph on which the action must be performed
     * @throws SLIB_Ex_Critic
     */
    public static void applyAction(URIFactory factory, GAction action, G g) throws SLIB_Ex_Critic {

        GActionType actionType = action.type;

        if (actionType == GActionType.TRANSITIVE_REDUCTION) {
            transitive_reduction(action, g);
        } else if (actionType == GActionType.REROOTING) {
            rerooting(factory, action, g);
        } else if (actionType == GActionType.VERTICES_REDUCTION) {
            verticeReduction(factory, action, g);
        } else if (actionType == GActionType.PREDICATE_SUBSTITUTE) {
            predicateSubstitution(factory, action, g);
        } else {
            throw new SLIB_Ex_Critic("Unknow action " + action.type);
        }
    }

    /**
     * Reduction of the set of vertices composing the graph.
     *
     * -------------------------------------------------------------------
     * IMPORTANT: If modified, this documentation must also be modified in the
     * class GActionType.
     * -------------------------------------------------------------------
     *
     * Accepted parameters are:
     *
     * <ul>
     *
     * <li> regex: specify a REGEX in Java syntax which will be used to test if
     * the value associated to a vertex makes it eligible to be removed. If the
     * value match the REGEX, the vertex will be removed </li>
     *
     * <li> vocabulary: Remove all the vertices associated to the vocabularies
     * specified. Accepted vocabularies flag are RDF, RDFS, OWL. Several
     * vocabularies can be specified using comma separator. </li>
     *
     * <li> file_uris: specify a list of files containing URIs corresponding to
     * the vertices to remove. Multiple files can be specified using comma
     * separator. </li>
     *
     * </ul>
     *
     *
     * @param factory the factory to consider if element requires to be
     * generated (e.g. {@link URI})
     * @param action the action to perform
     * @param g the graph on which the action must be performed
     * @throws SLIB_Ex_Critic
     */
    private static void verticeReduction(URIFactory factory, GAction action, G g) throws SLIB_Ex_Critic {

        logger.info("-------------------------------------");
        logger.info(" Vertices Reduction");
        logger.info("-------------------------------------");
        logger.info("Starting " + GActionType.VERTICES_REDUCTION);

        String regex = (String) action.getParameter("regex");
        String vocVal = (String) action.getParameter("vocabulary");
        String file_uris = (String) action.getParameter("file_uris");
        String rootURIs = (String) action.getParameter("root_uri");

        Set<URI> classes = GraphAccessor.getClasses(g);
        Set<URI> instances = GraphAccessor.getInstances(g);

        logger.info("Classes  : " + classes.size());
        logger.info("instances: " + instances.size());
        logger.info("vertices : " + g.getV().size());

        Set<URI> toRemove = new HashSet<URI>();

        if (rootURIs != null) {

            /*
             * Reduce the Graph considering all classes subsumed by the given root vertex
             * Instances annotated by those classes are also conserved into the graph, others are removed.
             */
            logger.info("Applying reduction of the part of the graph " + g.getURI() + " which is not contained in the graph induced by " + rootURIs + " (only the classes subsumed by the given root are considered)");

            try {
                URI rootURI = factory.getURI(rootURIs);

                if (!g.containsVertex(rootURI)) {
                    throw new SLIB_Ex_Critic("Error cannot state vertex associated to URI " + rootURI + " in graph " + g.getURI());
                }

                DescendantEngine descEngine = new DescendantEngine(g);
                Set<URI> descsInclusive = descEngine.getDescendantsInc(rootURI);

                logger.info(descsInclusive.size() + " subclasses of " + rootURI + " detected");

                int classesNb = classes.size();

                Set<URI> classesToRemove = classes;
                classesToRemove.removeAll(descsInclusive);

                logger.info("Removing " + classesToRemove.size() + "/" + classesNb + " classes of the graph");

                g.removeV(classesToRemove);

                // We then remove the entities which are not 
                // linked to the graph current underlying taxonomic graph
                Set<URI> instancesToRemove = new HashSet<URI>();

                for (URI v : instances) {

                    // No links to taxonomic graph anymore 
                    // we check the URI as is not considered as both instance and class
                    if (!descsInclusive.contains(v) && g.getV(v, RDF.TYPE, Direction.OUT).isEmpty()) {
                        instancesToRemove.add(v);
                    }
                }

                logger.info("Removing " + instancesToRemove.size() + " instances of the graph");
                g.removeV(instancesToRemove);

            } catch (IllegalArgumentException e) {
                throw new SLIB_Ex_Critic("Error value specified for parameter root_uri, i.e. " + rootURIs + " cannot be converted into an URI");
            }
        } else if (regex != null) {

            logger.info("Applying regex: " + regex);
            Pattern pattern;

            try {
                pattern = Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                throw new SLIB_Ex_Critic("The specified regex '" + regex + "' is invalid: " + e.getMessage());
            }

            Matcher matcher;

            for (URI v : g.getV()) {
                matcher = pattern.matcher(v.stringValue());

                if (matcher.find()) {
                    toRemove.add(v);
                    logger.debug("regex matches: " + v);
                }
            }

            logger.info("Vertices to remove: " + toRemove.size() + "/" + g.getV().size());

            g.removeV(toRemove);

            logger.debug("ending " + GActionType.VERTICES_REDUCTION);
        } else if (vocVal != null) {

            String[] vocs = vocVal.split(",");

            for (String voc : vocs) {

                if (voc.trim().equals("RDF")) {
                    logger.info("Removing RDF vocabulary");
                    removeVocURIs(factory, getRDFVocURIs(), g);
                } else if (voc.trim().equals("RDFS")) {
                    logger.info("Removing RDFS vocabulary");
                    removeVocURIs(factory, getRDFSVocURIs(), g);
                } else if (voc.trim().equals("OWL")) {
                    logger.info("Removing OWL vocabulary");
                    removeVocURIs(factory, getOWLVocURIs(), g);
                }
            }
        } else if (file_uris != null) {

            String[] files = file_uris.split(",");

            for (String f : files) {

                logger.info("Removing Uris specified in " + f);

                try {

                    FileInputStream fstream = new FileInputStream(f.trim());
                    DataInputStream in = new DataInputStream(fstream);
                    BufferedReader br = new BufferedReader(new InputStreamReader(in));

                    String line;

                    while ((line = br.readLine()) != null) {

                        line = line.trim();

                        g.removeV(factory.getURI(line));
                    }
                    in.close();
                } catch (IOException e) {
                    throw new SLIB_Ex_Critic(e.getMessage());
                }
            }
        }

        logger.info("vertices reduction performed");
        logger.info("-------------------------------------");
    }

    /**
     * Vocabulary associated to RDF
     *
     * @return the strings associated to the URIs of the RDF vocabulary
     */
    private static String[] getRDFVocURIs() {

        return new String[]{
            "http://www.w3.org/1999/02/22-rdf-syntax-ns#first", "http://www.w3.org/1999/02/22-rdf-syntax-ns#nil", "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate", "http://www.w3.org/1999/02/22-rdf-syntax-ns#Alt", "http://www.w3.org/1999/02/22-rdf-syntax-ns#Seq", "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://www.w3.org/1999/02/22-rdf-syntax-ns#rest", "http://www.w3.org/1999/02/22-rdf-syntax-ns#value", "http://www.w3.org/1999/02/22-rdf-syntax-ns#Bag", "http://www.w3.org/1999/02/22-rdf-syntax-ns#Property", "http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", "http://www.w3.org/1999/02/22-rdf-syntax-ns#object", "http://www.w3.org/1999/02/22-rdf-syntax-ns#List", "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement", "http://www.w3.org/1999/02/22-rdf-syntax-ns#subject", "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString", "http://www.w3.org/1999/02/22-rdf-syntax-ns#li"
        };

    }

    private static String[] getRDFSVocURIs() {
        return new String[]{
            "http://www.w3.org/2000/01/rdf-schema#subClassOf", "http://www.w3.org/2000/01/rdf-schema#label", "http://www.w3.org/2000/01/rdf-schema#Class", "http://www.w3.org/2000/01/rdf-schema#member", "http://www.w3.org/2000/01/rdf-schema#comment", "http://www.w3.org/2000/01/rdf-schema#Literal", "http://www.w3.org/2000/01/rdf-schema#seeAlso", "http://www.w3.org/2000/01/rdf-schema#Resource", "http://www.w3.org/2000/01/rdf-schema#Container", "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://www.w3.org/2000/01/rdf-schema#domain", "http://www.w3.org/2000/01/rdf-schema#subPropertyOf", "http://www.w3.org/2000/01/rdf-schema#Datatype", "http://www.w3.org/2000/01/rdf-schema#range", "http://www.w3.org/2000/01/rdf-schema#ContainerMembershipProperty"
        };
    }

    private static String[] getOWLVocURIs() {
        return new String[]{
            "http://www.w3.org/2002/07/owl#AllDifferent", "http://www.w3.org/2002/07/owl#allValuesFrom", "http://www.w3.org/2002/07/owl#AnnotationProperty", "http://www.w3.org/2002/07/owl#backwardCompatibleWith", "http://www.w3.org/2002/07/owl#cardinality", "http://www.w3.org/2002/07/owl#Class", "http://www.w3.org/2002/07/owl#complementOf", "http://www.w3.org/2002/07/owl#DatatypeProperty", "http://www.w3.org/2002/07/owl#DeprecatedClass", "http://www.w3.org/2002/07/owl#DeprecatedProperty", "http://www.w3.org/2002/07/owl#differentFrom", "http://www.w3.org/2002/07/owl#disjointWith", "http://www.w3.org/2002/07/owl#distinctMembers", "http://www.w3.org/2002/07/owl#equivalentClass", "http://www.w3.org/2002/07/owl#equivalentProperty", "http://www.w3.org/2002/07/owl#FunctionalProperty", "http://www.w3.org/2002/07/owl#hasValue", "http://www.w3.org/2002/07/owl#imports", "http://www.w3.org/2002/07/owl#incompatibleWith", "http://www.w3.org/2002/07/owl#Individual", "http://www.w3.org/2002/07/owl#intersectionOf", "http://www.w3.org/2002/07/owl#InverseFunctionalProperty", "http://www.w3.org/2002/07/owl#inverseOf", "http://www.w3.org/2002/07/owl#maxCardinality", "http://www.w3.org/2002/07/owl#minCardinality", "http://www.w3.org/2002/07/owl#ObjectProperty", "http://www.w3.org/2002/07/owl#oneOf", "http://www.w3.org/2002/07/owl#onProperty", "http://www.w3.org/2002/07/owl#Ontology", "http://www.w3.org/2002/07/owl#OntologyProperty", "http://www.w3.org/2002/07/owl#priorVersion", "http://www.w3.org/2002/07/owl#Restriction", "http://www.w3.org/2002/07/owl#sameAs", "http://www.w3.org/2002/07/owl#someValuesFrom", "http://www.w3.org/2002/07/owl#SymmetricProperty", "http://www.w3.org/2002/07/owl#TransitiveProperty", "http://www.w3.org/2002/07/owl#unionOf", "http://www.w3.org/2002/07/owl#versionInfo"
        };
    }

    /**
     * Try to remove the vertices associated to the given URIs specified as
     * strings. If a string is not a valid URI a
     * {@link IllegalArgumentException} can be throw.
     *
     * @param toRemove set of strings corresponding to the URIs to remove
     * @param g the graph in which the treatment require to be performed.
     */
    private static void removeVocURIs(URIFactory factory, String[] toRemove, G g) {

        for (String s : toRemove) {
            g.removeV(factory.getURI(s));
        }
    }

    /**
     * Root the graph according to the rdfs:subClassOf relationship.
     *
     * -------------------------------------------------------------------
     * IMPORTANT: If modified, this documentation must also be modified in the
     * class GActionType.
     * -------------------------------------------------------------------
     *
     * For each URI x which is involved in a statement in which the predicate
     * rdfs:subClassOf is used, if no statement x rdfs:subClassOf y exists, x is
     * considered to refer to a root. In this treatment, for each root x a
     * statement x rdfs:subClassOf new_root is created. The value of new_root
     * can be defined automatically or manually see below.
     *
     * The root URI can be specified using the parameter "root_uri":
     * <ul>
     * <li>the value must refer to the URI to consider for the root. It can be
     * an URI which is not already used in the graph.</li>
     * <li>"__FICTIVE__" as value will be substituted by OWL.THING, i.e. refers
     * to the OWL vocabulary in the Sesame API</li>
     * </ul>
     *
     *
     *
     * @param factory
     * @param action
     * @param g
     * @throws SLIB_Ex_Critic
     */
    private static void rerooting(URIFactory factory, GAction action, G g) throws SLIB_Ex_Critic {

        logger.info("-------------------------------------");
        logger.info("Rerooting");
        logger.info("-------------------------------------");

        // Re-rooting
        String rootURIstring = (String) action.getParameter("root_uri");

        logger.info("Fetching root node, uri: " + rootURIstring);

        URI rootURI;

        if (rootURIstring == null || rootURIstring.equals(REROOT_UNIVERSAL_ROOT_FLAG)) {
            rootURI = OWL.THING;
            g.addV(rootURI);
            logger.info("No root node explicitly specified using 'root_uri' parameter. Set root : " + rootURI);
        } else {
            rootURI = factory.getURI(rootURIstring);
            if (!g.containsVertex(rootURI)) {
                logger.info("Create class " + rootURI);
                g.addV(rootURI);
            }
        }

        RooterDAG.rootUnderlyingTaxonomicDAG(g, rootURI);

        logger.info("Rerooting performed");
        logger.info("-------------------------------------");
    }

    /**
     * Perform a transitive reduction of the relationships RDFS.SUBCLASSOF
     * and/or RDF.TYPE - this treatment removes the relationships which can be
     * removed according to the transitivity of the predicate rdfs:subClassOf.
     *
     * -------------------------------------------------------------------
     * IMPORTANT: If modified, this documentation must also be modified in the
     * class GActionType.
     * -------------------------------------------------------------------
     *
     * You can specify the type of relationships on which the treatment must be
     * performed using the parameter "target" with value:
     * <ul>
     * <li>CLASSES or rdfs:subClassOf or RDFS.SUBCLASSOF (upper or lower case)
     * to remove relationships rdfs:subClassOf which can be inferred </li>
     * <li>INSTANCES or rdf:type or RDF.TYPE (upper or lower case) to remove
     * relationships rdf:type which can be inferred </li>
     * <li>you can use both using a comma separator setting
     * "CLASSES,INSTANCES"</li>
     * </ul>
     *
     * @param action
     * @param g
     * @throws SLIB_Ex_Critic
     */
    private static void transitive_reduction(GAction action, G g) throws SLIB_Ex_Critic {

        String[] targets;

        logger.info("-------------------------------------");
        logger.info("Transitive Reduction");
        logger.info("-------------------------------------");

        if (!action.existsParam("target")) {
            targets = new String[1];
            targets[0] = "RDFS:SUBCLASSOF";
        }
        else{
            targets = ((String) action.getParameter("target")).split(",");
        }

        logger.info("Targets: " + Arrays.toString(targets));

        String[] admittedTarget_CLASSES = {"CLASSES", "RDFS:SUBCLASSOF", "RDFS.SUBCLASSOF"};
        String[] admittedTarget_INSTANCES = {"INSTANCES", "RDF:TYPE", "RDF.TYPE"};

        for (String target : targets) {
            if (Arrays.asList(admittedTarget_CLASSES).contains(target.trim().toUpperCase())) {
                GraphReduction_Transitive.process(g);
            } else if (Arrays.asList(admittedTarget_INSTANCES).contains(target.trim().toUpperCase())) {
                transitive_reductionInstance(action, g);
            } else {
                throw new SLIB_Ex_Critic("Unknow target '" + target + "', please precise a valid 'target' parameter', accepted values (in lower or upper case) " + Arrays.asList(admittedTarget_CLASSES) + " or " + Arrays.asList(admittedTarget_INSTANCES));
            }
        }

        logger.info(
                "Transitive reduction performed");
        logger.info(
                "-------------------------------------");

    }

    private static void transitive_reductionInstance(GAction action, G g) throws SLIB_Ex_Critic {

        int invalidInstanceNb = 0;
        int annotNbBase = 0;
        int annotDeleted = 0;

        Set<URI> instances = GraphAccessor.getInstances(g);

        logger.info("Cleaning " + RDF.TYPE + " triplets of " + g.getURI());
        System.out.println(g);

        RVF_TAX rvf = new RVF_TAX(g, Direction.IN);

        // Retrieve descendants for all vertices
        Map<URI, Set<URI>> descs = rvf.getAllRVClass();

        for (URI instance : instances) {

            Set<URI> redundants = new HashSet<URI>();
            Set<URI> classes = g.getV(instance, RDF.TYPE, Direction.OUT);

            annotNbBase += classes.size();

            Iterator<URI> it = classes.iterator();
            while (it.hasNext()) {

                URI c = it.next();
                Set<URI> descC = descs.get(c);

                for (URI c2 : classes) {

                    if (c != c2 && descC.contains(c2)) {
                        redundants.add(c);
                        it.remove();
                        break;
                    }
                }
            }

            if (!redundants.isEmpty()) {

                for (URI r : redundants) {
                    g.removeE(new Edge(instance, RDF.TYPE, r));
                }
                invalidInstanceNb++;
                annotDeleted += redundants.size();
            }
        }

        double invalidInstanceP = 0;
        if (instances.size() > 0) {
            invalidInstanceP = invalidInstanceNb * 100 / instances.size();
        }

        double annotDelP = 0;
        if (annotNbBase > 0) {
            annotDelP = annotDeleted * 100 / annotNbBase;
        }

        logger.info("Number of instance containing abnormal annotation: " + invalidInstanceNb + "/" + instances.size() + "  i.e. (" + invalidInstanceP + "%)");
        logger.info("Number of annotations: " + annotNbBase + ", deleted: " + annotDeleted + " (" + (annotDelP) + "%), current annotation number " + (annotNbBase - annotDeleted));

    }

    /**
     *
     * @param factory
     * @param actions
     * @param g
     * @throws SLIB_Ex_Critic
     */
    public static void applyActions(URIFactory factory, List<GAction> actions, G g) throws SLIB_Ex_Critic {

        logger.info("-------------------------------------");
        logger.info(" Applying actions");
        logger.info("-------------------------------------");

        for (GAction action : actions) {
            applyAction(factory, action, g);
        }
        logger.info("Actions performed");
        logger.info("-------------------------------------");
    }

    /**
     * Can be used to substitute the predicate of all the triplets with a
     * specific predicate.
     *
     * -------------------------------------------------------------------
     * IMPORTANT: If modified, this documentation must also be modified in the
     * class GActionType.
     * -------------------------------------------------------------------
     *
     * parameters expected:
     * <ul>
     * <li>old_uri: the URI predicate to replace</li>
     * <li>new_uri: the new URI predicate</li>
     * </ul>
     * You can use RDFS.SUBCLASSOF to refer to
     * http://www.w3.org/2000/01/rdf-schema#subClassOf
     *
     * @param factory
     * @param action
     * @param g
     * @throws SLIB_Ex_Critic
     */
    private static void predicateSubstitution(URIFactory factory, GAction action, G g) throws SLIB_Ex_Critic {

        logger.info("-------------------------------------");
        logger.info(" Predicate Substitution");
        logger.info("-------------------------------------");
        logger.info("Starting " + GActionType.PREDICATE_SUBSTITUTE);

        String old_URI = (String) action.getParameter("old_uri");
        String new_URI = (String) action.getParameter("new_uri");

        if (old_URI == null || new_URI == null) {
            throw new SLIB_Ex_Critic("Error - please specify a parameter old_uri and new_uri");
        }

        if (old_URI.toUpperCase().equals("RDFS.SUBCLASSOF")) {
            old_URI = RDFS.SUBCLASSOF.toString();
        }
        if (new_URI.toUpperCase().equals("RDFS.SUBCLASSOF")) {
            new_URI = RDFS.SUBCLASSOF.toString();
        }

        URI oldURI = factory.getURI(old_URI);
        URI newURI = factory.getURI(new_URI);

        Set<E> oldRel = g.getE(oldURI);
        g.removeE(oldRel);

        for (E e : oldRel) {
            g.addE(e.getSource(), newURI, e.getTarget());
        }

        logger.info(oldRel.size() + " relations modified");
    }
}
