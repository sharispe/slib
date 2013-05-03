/*

 Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

 This software is a computer program whose purpose is to 
 process semantic graphs.

 This software is governed by the CeCILL  license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 */
package slib.sml.sm.core.engine;

import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.algo.graph.accessor.GraphAccessor;
import slib.sglib.algo.graph.extraction.rvf.AncestorEngine;
import slib.sglib.algo.graph.extraction.rvf.DescendantEngine;
import slib.sglib.algo.graph.extraction.rvf.RVF_TAX;
import slib.sglib.algo.graph.metric.DepthAnalyserAG;
import slib.sglib.algo.graph.reduction.dag.GraphReduction_Transitive;
import slib.sglib.algo.graph.shortest_path.Dijkstra;
import slib.sglib.algo.graph.traversal.classical.DFS;
import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.algo.graph.validator.dag.ValidatorDAG;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraint;
import slib.sglib.model.graph.weight.GWS;
import slib.sglib.utils.WalkConstraintUtils;
import slib.sml.sm.core.measures.Sim_Groupwise_Direct;
import slib.sml.sm.core.measures.Sim_Groupwise_Indirect;
import slib.sml.sm.core.measures.Sim_Pairwise;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.utils.SimDagEdgeUtils;
import slib.sml.sm.core.metrics.ic.annot.ICcorpus;
import slib.sml.sm.core.metrics.ic.topo.ICtopo;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Corpus;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.metrics.ic.utils.IcUtils;
import slib.sml.sm.core.metrics.vector.VectorWeight_Chabalier_2007;
import slib.sml.sm.core.utils.LCAFinder;
import slib.sml.sm.core.utils.LCAFinderImpl;
import slib.sml.sm.core.utils.SMconf;
import slib.sml.sm.core.utils.SMutils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.MatrixDouble;
import slib.utils.impl.SetUtils;

/**
 * This class defines a Semantic Measures Engine giving access to numerous
 * methods commonly used to define graph-based semantic measures.
 * <br/>
 * The engine distinguished two types of vertices in the graph:
 * <ul>
 * <li>
 * Classes: vertices corresponding to classes as defined by
 * {@link GraphAccessor}. In short, classes are the vertices composing the
 * taxonomic graph included in the given graph. In most cases the graph only
 * contains vertices associated to classes.
 * </li>
 * <li>
 * Instances: vertices corresponding to instances as defined by
 * {@link GraphAccessor}. Those instances are the vertices which are typed by
 * classes.
 * </li>
 * </ul>
 * More information between classes and instances can be found in the web site
 * of the library.
 *
 * Notice that this documentation to refer to classes and instances even if the
 * underlying object referring to them in the graph are URIs. Please, consider
 * that we refer to the classes/instances identified by the URIs.
 *
 * Accesses to ancestors/parents and descendants are only constrained by the
 * partial ordering defined by the RDFS.SUBCLASSOF relationships. As an example
 * an ancestors is any class which is link by a path composed of RDFS.SUBCLASSOF
 * relationships.
 *
 * <br/><b>Important</b>:<br/>
 * Note that the graph associated to the engine is expected to be immutable even
 * if this condition will not be checked during the process. Indeed, the engine
 * expects the graph not to be modified and will not work on a copy of the given
 * graph. The engine stores some results to ensure performances, as a conclusion
 * coherency of results will be impacted if the graph is modified next to engine
 * construction.
 *
 * Some methods provided by the class expect the underlying taxonomic graph to
 * be transitively reduced. In other words if z is a sub class of y and y is a
 * sub class of x an edge z is a sub class of x is not expected. As an example
 * this is important to ensure coherency of parent retrieval. Such transitive
 * reduction can be performed though the {@link GraphReduction_Transitive} class
 * or even more easily using the {@link GraphActionExecutor} class.
 *
 * The engine stores commonly accessed results (e.g. ancestors of a class) which
 * can lead to high memory consumption dealing with large graphs.
 *
 * @author Harispe Sébastien
 */
public class SM_Engine {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    final G graph;
    AncestorEngine ancGetter;
    DescendantEngine descGetter;
    LCAFinder lcaFinder;
    Set<URI> classes;
    Set<URI> classesLeaves;
    Set<URI> instances;
    URI root = null;
    SMProxResultStorage cache;
    boolean cachePairwiseResults = false;
    /**
     * TODO Replace by {@link GWS}
     */
    Map<URI, Double> vectorWeights = null;
    Map<SMconf, Sim_Pairwise> pairwiseMeasures;
    Map<SMconf, Sim_Groupwise_Indirect> groupwiseAddOnMeasures;
    Map<SMconf, Sim_Groupwise_Direct> groupwiseStandaloneMeasures;

    /**
     * Constructor of an engine associated to the given graph.
     *
     * Note that the engine expects the graph not to be modified and coherency
     * of results are only ensured in this case. Please refer to the general
     * documentation of the class for more information considering this specific
     * restriction.
     *
     * The engine creation is expensive, avoid useless calls to the constructor.
     * Indeed, some information such as classes and instances of the graph are
     * computed at engine creation which could lead to performance issues
     * dealing with large graph.
     *
     * @param graph the graph associated to the engine.
     * @throws SLIB_Ex_Critic
     */
    public SM_Engine(G g) throws SLIB_Ex_Critic {

        this.graph = g;

        logger.info("---------------------------------------------------------------");
        logger.info("Loading Semantic Measures Engine for graph " + graph.getURI());
        logger.info("---------------------------------------------------------------");
        logger.info(g.toString());

        ancGetter = new AncestorEngine(graph);
        descGetter = new DescendantEngine(graph);

        logger.info("Computing classes");
        classes = GraphAccessor.getClasses(graph);
        logger.info("Computing instances");
        instances = GraphAccessor.getInstances(graph);

        logger.info("Classes  : " + classes.size());
        logger.info("Instances: " + instances.size());

        cache = new SMProxResultStorage();
        pairwiseMeasures = new ConcurrentHashMap<SMconf, Sim_Pairwise>();

        groupwiseAddOnMeasures = new ConcurrentHashMap<SMconf, Sim_Groupwise_Indirect>();
        groupwiseStandaloneMeasures = new ConcurrentHashMap<SMconf, Sim_Groupwise_Direct>();

        lcaFinder = new LCAFinderImpl(this);

        logger.info("Inferences ");
        logger.info("---------------------------------------------------------------");

        logger.info("Inferring ancestors");
        computeAllclassesAncestors();
        logger.info("Inferring descendants");
        computeAllclassesDescendants();
        logger.info("Inferring Conceptual Leaves");
        computeLeaves();
        logger.info("Semantic measures Engine initialized");
        logger.info("---------------------------------------------------------------");
    }

    /**
     * Compute the inclusive ancestors for all classes.
     *
     * @throws SLIB_Ex_Critic
     */
    private void computeAllclassesAncestors() throws SLIB_Ex_Critic {
        cache.ancestorsInc = ancGetter.getAllAncestorsInc();
    }

    /**
     * Compute the inclusive descendants for all classes.
     *
     * @throws SLIB_Ex_Critic
     */
    private void computeAllclassesDescendants() throws SLIB_Ex_Critic {
        cache.descendantsInc = descGetter.getAllDescendantsInc();
    }

    /**
     * Compute the union of the inclusive ancestors of a set of classes.
     *
     * The given classes are included in the result (inclusive). This process
     * can be computationally expensive if the number of ancestors is important.
     * The result is not cached by the engine.
     *
     * @param setClasses the set of classes considered
     * @return the union of the inclusive ancestors of the given classes
     * @throws IllegalAccessException if the given set contains an URI which
     * cannot be associated to a class
     */
    public Set<URI> getAncestorsInc(Set<URI> setClasses) {

        throwErrorIfNotClass(setClasses);

        Set<URI> unionAnc = new HashSet<URI>();

        for (URI v : setClasses) {
            unionAnc.addAll(getAncestorsInc(v));
        }
        return unionAnc;
    }

    /**
     * Give access to a view of the inclusive ancestors of a class.
     *
     * The given class will therefore be include in the results. The result is
     * cached by the engine for fast access.
     *
     * @param v the considered class
     * @return the set of inclusive ancestors of the given class (v included)
     * @throws IllegalAccessException if the given URI cannot be associated to a
     * class
     */
    public Set<URI> getAncestorsInc(URI v) {

        throwErrorIfNotClass(v);
        return Collections.unmodifiableSet(cache.ancestorsInc.get(v));
    }

    /**
     * Give access to a view of the inclusive descendants of a class.
     *
     * The given class will therefore be include in the results. The result is
     * cached by the engine for fast access.
     *
     * @param v the considered class
     * @return the set of inclusive descendants of the given class (v included)
     * @throws IllegalAccessException if the given URI cannot be associated to a
     * class
     */
    public synchronized Set<URI> getDescendantsInc(URI v) {
        throwErrorIfNotClass(v);
        return Collections.unmodifiableSet(cache.descendantsInc.get(v));
    }

    /**
     * Get the parents of a class, that is to say its direct ancestors.
     *
     * <br/><b>Important</b>:<br/>
     *
     * The direct parent of a class are all classes x linked to the given class
     * c to a an edge x RDFS.SUBLASSOF c. The result is not cached by the
     * engine. To ensure result coherency the underlying requires to be
     * transitively reduced, refer to the class documentation for more
     * information.
     *
     * @param v the focus vertex
     * @return the set of parents of the given vertex
     * @throws IllegalAccessException if the given URI cannot be associated to a
     * class
     */
    public Set<URI> getParents(URI v) {

        throwErrorIfNotClass(v);

        Set<URI> parents = graph.getV(v, ancGetter.getWalkConstraint());
        return parents;
    }

    /**
     * Give access to a view of the maximal depth of all classes. The result is
     * stored by the engine.
     *
     * @return a resultStack containing the maximal depths for all classes
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> getMaxDepths() throws SLIB_Ex_Critic {

        if (cache.maxDepths == null) {
            DepthAnalyserAG dephtAnalyser = new DepthAnalyserAG(graph, descGetter.getWalkConstraint());
            cache.maxDepths = dephtAnalyser.getVMaxDepths();
        }

        return Collections.unmodifiableMap(cache.maxDepths);
    }

    /**
     * Give access to a view of the minimal depth of all classes. The result is
     * stored by the engine.
     *
     * @return a resultStack containing the maximal depths for all classes
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> getMinDepths() throws SLIB_Ex_Critic {

        if (cache.minDepths == null) {
            DepthAnalyserAG dephtAnalyser = new DepthAnalyserAG(graph, descGetter.getWalkConstraint());
            cache.minDepths = dephtAnalyser.getVMinDepths();
        }
        return Collections.unmodifiableMap(cache.minDepths);
    }

    /**
     * Get the Information Content of a class. The information content to
     * considered is defined by the given configuration.
     *
     * @param icConf The configuration of the information content
     * @param v the class
     * @return the information content of the specified class according to the
     * specified configuration.
     * @throws IllegalAccessException if the given URI cannot be associated to a
     * class
     * @throws SLIB_Exception
     *
     */
    public double getIC(ICconf icConf, URI v) throws SLIB_Exception {

        throwErrorIfNotClass(v);

        if (icConf == null) {
            throw new SLIB_Ex_Critic("Specified IC cannot be null");
        } else if (!classes.contains(v)) {
        }

        if (cache.metrics_results.get(icConf) == null) {
            computeIC(icConf);
        }

        return cache.metrics_results.get(icConf).get(v);
    }

    /**
     * Access to the maximal depth of a class in the underlying taxonomic graph.
     *
     * @return the maximal depth of the graph.
     * @throws SLIB_Exception
     */
    public int getMaxDepth() throws SLIB_Exception {

        if (cache.maxDepth == null) {
            cache.maxDepth = Collections.max(getMaxDepths().values());
        }
        return cache.maxDepth.intValue();
    }

    /**
     * Get the root of the taxonomic graph contained in the graph associated to
     * the engine. An exception will be thrown if the taxonomic graph contains
     * multiple roots.
     *
     * @return the class corresponding to the root.
     */
    public synchronized URI getRoot() throws SLIB_Ex_Critic {
        if (root == null) {
            URI rooturi = new ValidatorDAG().getRootedTaxonomicDAGRoot(graph);
            root = rooturi;
        }
        return root;
    }

    /**
     * Get the information content of the most informative common ancestor
     * (MICA) of two classes. The MICA is the class with the maximal IC found
     * among the sets of ancestors of the two given classes.
     *
     * @param icConf the configuration of the information content
     * @param a the first class
     * @param b the second class
     * @return the IC of the most informative common ancestor of the two
     * classes.
     * @throws SLIB_Exception if no common ancestor is found between the two
     * classes
     * @throws IllegalAccessException if the given URIs cannot be associated to
     * a class
     */
    public double getIC_MICA(ICconf icConf, URI a, URI b) throws SLIB_Exception {

        throwErrorIfNotClass(a);
        throwErrorIfNotClass(b);

        if (cache.metrics_results.get(icConf) == null) {
            computeIC(icConf);
        }
        return IcUtils.searchMax_IC_MICA(a, b, getAncestorsInc(a), getAncestorsInc(b), getIC_results(icConf));
    }

    /**
     * Get the most informative common ancestor (MICA) of two classes. The MICA
     * is the class with the maximal IC found among the sets of ancestors of the
     * two given classes.
     *
     * @param icConf the configuration of the information content
     * @param a the first class
     * @param b the second class
     * @return the most informative common ancestor of the two classes.
     * @throws SLIB_Exception if no common ancestor is found between the two
     * classes
     * @throws IllegalAccessException if the given URIs cannot be associated to
     * a class
     */
    public URI getMICA(ICconf icConf, URI a, URI b) throws SLIB_Exception {

        throwErrorIfNotClass(a);
        throwErrorIfNotClass(b);

        if (cache.metrics_results.get(icConf) == null) {
            computeIC(icConf);
        }
        return IcUtils.searchMICA(a, b, getAncestorsInc(a), getAncestorsInc(b), getIC_results(icConf));
    }

    /**
     * Compute the number of inclusive descendants for all classes
     *
     * @return a map containing the number of inclusive descendants
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> getAllNbDescendantsInc() throws SLIB_Ex_Critic {

        Map<URI, Integer> allNbDescendants = new HashMap<URI, Integer>();
        for (URI c : classes) {
            allNbDescendants.put(c, getAllDescendantsInc().get(c).size());
        }
        return allNbDescendants;
    }

    /**
     *
     * Access to a view of the inclusive descendants for all classes.
     *
     * @return the inclusive descendants for all classes
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Set<URI>> getAllDescendantsInc() throws SLIB_Ex_Critic {
        return Collections.unmodifiableMap(cache.descendantsInc);
    }

    /**
     * Access to the inclusive ancestors for all classes.
     *
     * @return the inclusive ancestors for all classes
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Set<URI>> getAllAncestorsInc() throws SLIB_Ex_Critic {
        return Collections.unmodifiableMap(cache.ancestorsInc);
    }

    /**
     * Access to a view of the information content of all classes.
     *
     * @param icConf the information content considered.
     * @return the information content of all classes
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Double> getIC_results(ICconf icConf) throws SLIB_Ex_Critic {


        if (!cache.metrics_results.containsKey(icConf)) {
            cache.metrics_results.put(icConf, computeIC(icConf));
        }
        return Collections.unmodifiableMap(cache.metrics_results.get(icConf));
    }

    /**
     * Compute the information content for all classes. Results are stored for
     * fast access.
     *
     * @param icConf the configuration to consider
     * @return the IC for all classes
     * @throws SLIB_Ex_Critic
     */
    public synchronized Map<URI, Double> computeIC(ICconf icConf) throws SLIB_Ex_Critic {

        if (icConf == null) {
            throw new SLIB_Ex_Critic("IC configuration cannot be set to null... " + icConf);
        } else if (cache.metrics_results.get(icConf) != null) {
            return Collections.unmodifiableMap(cache.metrics_results.get(icConf));
        }


        logger.info("---------------------------------------------------------------");
        logger.info("computing IC " + icConf.getId());
        logger.info("---------------------------------------------------------------");
        
        Class<?> cl;
        Map<URI, Double> results;

        try {

            String icClassName = icConf.getClassName();
            logger.info("Class name " + icClassName);

            if (icConf instanceof IC_Conf_Corpus) {

                IC_Conf_Corpus icConfCorpus = (IC_Conf_Corpus) icConf;

                cl = Class.forName(icClassName);
                Constructor<?> co = cl.getConstructor();
                ICcorpus o = (ICcorpus) co.newInstance();

                results = o.compute(icConfCorpus, this);
            } else {

                IC_Conf_Topo icConfTopo = (IC_Conf_Topo) icConf;

                cl = Class.forName(icClassName);
                Constructor<?> co = cl.getConstructor();
                ICtopo o = (ICtopo) co.newInstance();

                results = o.compute(icConfTopo, this);
            }

            cache.metrics_results.put(icConf, results);

            logger.info("Checking null or infinite in the ICs computed");

            for (Entry<URI, Double> e : results.entrySet()) {
                if (Double.isNaN(e.getValue()) || Double.isInfinite(e.getValue())) {
                    throw new SLIB_Ex_Critic("Incoherency found in IC " + icConf.getClassName() + "\nIC of vertex " + e.getKey() + " is set to " + e.getValue());
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        logger.info("ic " + icConf.getLabel() + " computed");
        logger.info("---------------------------------------------------------------");
        return Collections.unmodifiableMap(cache.metrics_results.get(icConf));
    }

    /**
     * Compute for each class x the classes which are leaves which are subsumed
     * by x. Inclusive i.e. a leaf will contain itself in it set of reachable
     * leaves. The result is cached for fast access.
     *
     *
     * @return the subsumed leaves for each classes
     */
    public synchronized Map<URI, Set<URI>> getReachableLeaves() {

        if (cache.reachableLeaves.isEmpty()) {
            cache.reachableLeaves = descGetter.getTerminalVertices();
        }
        return Collections.unmodifiableMap(cache.reachableLeaves);
    }
    
    /**
     * Compute for each class x the classes which are leaves which are subsumed
     * by x. Inclusive i.e. a leaf will contain itself in it set of reachable
     * leaves. The result is cached for fast access.
     *
     *
     * @return the subsumed leaves for each classes
     */
    public synchronized Set<URI> getReachableLeaves(URI uri) {
        return Collections.unmodifiableSet(getReachableLeaves().get(uri));
    }

    /**
     * Access to a view of the set of leaves of the underlying taxonomic graph.
     *
     * @return the set of classes which are leaves
     */
    public Set<URI> getTaxonomicLeaves() {
        return Collections.unmodifiableSet(classesLeaves);
    }

    /**
     * Compute for each class x the number classes which are leaves which are
     * subsumed by x. Inclusive i.e. a leaf will contain itself in it set of
     * reachable leaves. The result is cached for fast access.
     *
     *
     * @return the number subsumed leaves for each classes
     */
    public synchronized Map<URI, Integer> getAllNbReachableLeaves() {

        logger.info("Computing Nb Reachable Leaves : start");

        if (cache.allNbReachableLeaves == null) {

            Map<URI, Set<URI>> allReachableLeaves = getReachableLeaves();
            cache.allNbReachableLeaves = new HashMap<URI, Integer>();

            for (URI c : classes) {
                cache.allNbReachableLeaves.put(c, allReachableLeaves.get(c).size());
            }
        }


        logger.info("Computing Nb Reachable Leaves : end");

        return Collections.unmodifiableMap(cache.allNbReachableLeaves);
    }

    /**
     * Inclusive process
     *
     * @return
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> getAllNbAncestorsInc() throws SLIB_Ex_Critic {

        Map<URI, Set<URI>> allAncestors = cache.ancestorsInc;
        Map<URI, Integer> allNbancestors = new HashMap<URI, Integer>();

        for (URI c : classes) {
            allNbancestors.put(c, allAncestors.get(c).size());
        }
        return allNbancestors;
    }

    /**
     * Compute the pairwise semantic measures score considering the two vertices
     * and the semantic measure configuration.
     *
     * @param pairwiseConf the pairwise semantic measure configuration
     * @param a the first vertex/class/concept
     * @param b the second vertex/class/concept
     * @return the pairwise semantic measure score
     *
     * @throws SLIB_Ex_Critic
     *
     * @throws IllegalAccessException if the given URIs cannot be associated to
     * classes defined in the graph
     */
    public double computePairwiseSim(SMconf pairwiseConf, URI a, URI b) throws SLIB_Ex_Critic {

        throwErrorIfNotClass(a);
        throwErrorIfNotClass(b);

        double sim = -Double.MAX_VALUE;

        try {

            if (cachePairwiseResults
                    && cache.pairwise_results.containsKey(pairwiseConf)
                    && cache.pairwise_results.get(pairwiseConf).containsKey(a)
                    && cache.pairwise_results.get(pairwiseConf).get(a).containsKey(b)) {

                sim = cache.pairwise_results.get(pairwiseConf).get(a).get(b);
            } else {




                Sim_Pairwise pMeasure;

                synchronized (pairwiseMeasures) {

                    if (pairwiseMeasures.containsKey(pairwiseConf)) {
                        pMeasure = pairwiseMeasures.get(pairwiseConf);
                    } else {

                        Class<?> cl;
                        cl = Class.forName(pairwiseConf.className);
                        Constructor<?> co = cl.getConstructor();


                        pMeasure = (Sim_Pairwise) co.newInstance();
                        pairwiseMeasures.put(pairwiseConf, pMeasure);
                    }
                }
                sim = pMeasure.sim(a, b, this, pairwiseConf);



                if (Double.isNaN(sim) || Double.isInfinite(sim)) {
                    SMutils.throwArithmeticCriticalException(pairwiseConf, a, b, sim);
                }

                // Caching 

                if (cachePairwiseResults) {

                    if (cache.pairwise_results.get(pairwiseConf) == null) {

                        ConcurrentHashMap<URI, Map<URI, Double>> pairwise_result = new ConcurrentHashMap<URI, Map<URI, Double>>();

                        cache.pairwise_results.put(pairwiseConf, pairwise_result);
                    }

                    if (cache.pairwise_results.get(pairwiseConf).get(a) == null) {
                        cache.pairwise_results.get(pairwiseConf).put(a, new HashMap<URI, Double>());
                    }

                    cache.pairwise_results.get(pairwiseConf).get(a).put(b, sim);
                }
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new SLIB_Ex_Critic(e);
        }
        return sim;
    }

    /**
     * Compute the direct group wise semantic measure score considering the two
     * set of vertices and the semantic measure configuration.
     *
     * @param confGroupwise the direct groupwise semantic measure configuration
     * @param setA the first set of vertices/classes/concepts
     * @param setB the first set of vertices/classes/concepts
     * @return the group wise semantic measure score
     *
     * @throws SLIB_Ex_Critic
     *
     * @throws IllegalAccessException if the given URIs cannot be associated to
     * classes defined in the graph
     */
    public double computeGroupwiseStandaloneSim(
            SMconf confGroupwise,
            Set<URI> setA,
            Set<URI> setB) throws SLIB_Ex_Critic {

        throwErrorIfNotClass(setA);
        throwErrorIfNotClass(setB);

        double sim = -Double.MAX_VALUE;


        try {

            Sim_Groupwise_Direct gMeasure;

            synchronized (groupwiseStandaloneMeasures) {

                if (groupwiseStandaloneMeasures.containsKey(confGroupwise)) {
                    gMeasure = groupwiseStandaloneMeasures.get(confGroupwise);
                } else {
                    Class<?> cl;
                    String groupwiseClassName = confGroupwise.className;
                    cl = Class.forName(groupwiseClassName);
                    Constructor<?> co = cl.getConstructor();

                    gMeasure = (Sim_Groupwise_Direct) co.newInstance();
                    groupwiseStandaloneMeasures.put(confGroupwise, gMeasure);
                }
            }
            sim = gMeasure.sim(setA, setB, this, confGroupwise);

        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        return sim;
    }

    /**
     * Compute the indirect group wise semantic measure score considering the
     * two set of vertices and the semantic measure configuration.
     *
     * @param confGroupwise the pairwise semantic measure configuration
     * @param confPairwise the indirect aggregation strategy configuration
     * @param setA the first set of vertices/classes/concepts
     * @param setB the first set of vertices/classes/concepts
     * @return the group wise semantic measure score
     *
     * @throws SLIB_Ex_Critic
     *
     * @throws IllegalAccessException if the given URIs cannot be associated to
     * classes defined in the graph
     */
    public double computeGroupwiseAddOnSim(
            SMconf confGroupwise,
            SMconf confPairwise,
            Set<URI> setA,
            Set<URI> setB) throws SLIB_Ex_Critic {

        throwErrorIfNotClass(setA);
        throwErrorIfNotClass(setB);

        double sim = -Double.MAX_VALUE;

        try {
            Sim_Groupwise_Indirect gMeasure;

            synchronized (groupwiseAddOnMeasures) {

                if (groupwiseAddOnMeasures.containsKey(confGroupwise)) {
                    gMeasure = groupwiseAddOnMeasures.get(confGroupwise);
                } else {
                    Class<?> cl;
                    String groupwiseClassName = confGroupwise.className;
                    cl = Class.forName(groupwiseClassName);
                    Constructor<?> co = cl.getConstructor();

                    gMeasure = (Sim_Groupwise_Indirect) co.newInstance();
                    groupwiseAddOnMeasures.put(confGroupwise, gMeasure);
                }
            }


            sim = gMeasure.sim(setA, setB, this, confGroupwise, confPairwise);

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new SLIB_Ex_Critic(e);
        }

        return sim;
    }

    /**
     *
     * @return @throws SLIB_Ex_Critic
     *
     */
    public Map<URI, Integer> getnbPathLeadingToAllVertex() throws SLIB_Ex_Critic {

        if (cache.nbPathLeadingToAllVertices == null) {
            cache.nbPathLeadingToAllVertices = descGetter.computeNbPathLeadingToAllVertices();
        }

        return Collections.unmodifiableMap(cache.nbPathLeadingToAllVertices);
    }

    /**
     *
     * @return
     */
    public Map<URI, Integer> getNbInstancesInferredPropFromCorpus() {

        Map<URI, Set<URI>> linkedEntities = new HashMap<URI, Set<URI>>();

        for (URI i : instances) {
            Set<URI> annots = graph.getV(i, RDF.TYPE, Direction.OUT);

            if (annots != null) {
                for (URI c : annots) {
                    if (linkedEntities.get(c) == null) {
                        linkedEntities.put(c, new HashSet<URI>());
                    }
                    linkedEntities.get(c).add(i);
                }
            }

        }
        // Get Topological ordering trough DFS
        // - get roots
        Set<URI> roots = new ValidatorDAG().getDAGRoots(graph, ancGetter.getWalkConstraint());

        DFS dfs = new DFS(graph, roots, WalkConstraintUtils.getInverse(ancGetter.getWalkConstraint(), (false)));
        List<URI> topoOrdering = dfs.getTraversalOrder();


        Map<URI, Integer> rStack = new HashMap<URI, Integer>();

        // initialize data structure
        for (int i = 0; i < topoOrdering.size(); i++) {
            if (linkedEntities.get(topoOrdering.get(i)) == null) {
                linkedEntities.put(topoOrdering.get(i), new HashSet<URI>());
            }
        }

        for (int i = 0; i < topoOrdering.size(); i++) {

            URI currentV = topoOrdering.get(i);
            Set<URI> entities = linkedEntities.get(currentV);

            // propagate Linked Entities in a bottom up fashion according the topological order
            for (E e : graph.getE(currentV, ancGetter.getWalkConstraint())) {
                if (!entities.isEmpty()) {
                    linkedEntities.get(e.getTarget()).addAll(entities);
                }
            }

            rStack.put(currentV, entities.size());
        }
        cache.nbOccurrencePropagatted = rStack;

        return Collections.unmodifiableMap(cache.nbOccurrencePropagatted);
    }

    /**
     * Topological propagation considering one occurrence per term
     *
     * @return
     * @throws SLIB_Exception
     */
    public Map<URI, Integer> getNbOccurrenceProp() throws SLIB_Exception {

        if (cache.nbOccurrencePropagatted == null) {

            RVF_TAX RVF = new RVF_TAX(graph, Direction.IN);
            Map<URI, Integer> nbOccurrences = new HashMap<URI, Integer>();

            for (URI o : classes) {
                nbOccurrences.put(o, 1);
            }

            Map<URI, Integer> nbOccurrencesPropagated = RVF.propagateNbOccurences(nbOccurrences);
            cache.nbOccurrencePropagatted = nbOccurrencesPropagated;
        }

        return Collections.unmodifiableMap(cache.nbOccurrencePropagatted);
    }

    /**
     * Compute the matrix of similarity for two sets of vertex/concepts/classes.
     * In other words, the matrix will contain all the semantic scores which can
     * be computed for every pair of concepts which can be build from the two
     * sets.
     *
     * @param setA the first set of vertices/classes/concepts
     * @param setB the second set of vertices/classes/concepts
     * @param pairwiseConf the pairwise semantic measure configuration which
     * must be used to compute the score of a pair of vertex
     *
     * @return the matrix filled with the scores.
     * @throws SLIB_Ex_Critic
     * @throws IllegalAccessException if the given URIs cannot be associated to
     * a class
     */
    public MatrixDouble<URI, URI> getMatrixScore(
            Set<URI> setA,
            Set<URI> setB,
            SMconf pairwiseConf) throws SLIB_Ex_Critic {

        throwErrorIfNotClass(setA);
        throwErrorIfNotClass(setB);

        MatrixDouble<URI, URI> m = new MatrixDouble<URI, URI>(setA, setB);

        for (URI a : setA) {
            for (URI b : setB) {
                m.setValue(a, b, computePairwiseSim(pairwiseConf, a, b));
            }
        }
        return m;
    }

    /**
     * Check if the engine is configured to store the results of the pairwise
     * semantic measure computation.
     *
     * @return true if the engine store the results.
     */
    public boolean isCachePairwiseResults() {
        return cachePairwiseResults;
    }

    /**
     * Set the configuration of the engine regarding pairwise semantic measure
     * score caching.
     *
     * <br/><b>Important</b>:<br/>
     *
     * Storing the results can be very useful in specific cases. However,
     * storing the results can also lead to high memory consumption and
     * therefore slow the process or crash the process.
     *
     * @param cachePairwiseResults set to true if the engine must stores the
     * results.
     */
    public void setCachePairwiseResults(boolean cachePairwiseResults) {
        logger.info("Pairwise results caching set to " + cachePairwiseResults);
        this.cachePairwiseResults = cachePairwiseResults;
    }

    /**
     *
     * @param set
     * @param groupwiseconf
     * @return
     */
    public Map<URI, Double> getVector(Set<URI> set, SMconf groupwiseconf) {

        if (vectorWeights == null) {
            vectorWeights = VectorWeight_Chabalier_2007.compute(this);
        }

        Map<URI, Double> vector = new HashMap<URI, Double>();

        Set<URI> setAncestors;
        setAncestors = set; // unpropragatted
        //		setAncestors = getAncestors(set);

        for (Entry<URI, Double> e : vectorWeights.entrySet()) {

            URI v = e.getKey();

            if (setAncestors.contains(v)) {
                vector.put(v, e.getValue());
            } else {
                vector.put(v, 0.);
            }

        }
        return vector;
    }

    /**
     * Access to the graph associated to the engine.
     *
     * @return the graph associated to the engine
     */
    public G getGraph() {
        return graph;
    }

    /**
     * Set the ICS stored for the given IC configuration to the specified set of
     * values.
     *
     * @param icConf
     * @param ics
     */
    public void setICSvalues(ICconf icConf, Map<URI, Double> ics) {
        cache.metrics_results.put(icConf, ics);
    }

    public Set<URI> getLCAs(URI a, URI b) throws SLIB_Exception {
        return lcaFinder.getLCAs(a, b);
    }

    /**
     * TODO store the weighting scheme in a Map<String,GWS> Provide a way to
     * load edge weight from file or to compute them using specific methods
     *
     * @param param the key corresponding to the id of the weighting scheme to
     * retrieve
     * @return
     */
    public GWS getWeightingScheme(String param) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public AncestorEngine getAncestorEngine() {
        return ancGetter;
    }

    public DescendantEngine getDescendantEngine() {
        return descGetter;
    }

    private void computeLeaves() {
        classesLeaves = new HashSet<URI>();

        WalkConstraint wc = descGetter.getWalkConstraint();
        for (URI v : classes) {
            if (graph.getV(v, wc).isEmpty()) {
                classesLeaves.add(v);
            }
        }
    }

    /**
     * Access to the set of URI of the graph considered as classes.
     *
     * @return the set of classes
     */
    public Set<URI> getClasses() {
        return classes;
    }

    /**
     * Access to the set of URI of the graph considered as instances.
     *
     * @return the set of instances
     */
    public Set<URI> getInstances() {
        return instances;
    }
    
    private void throwErrorIfNotClass(URI c) {
        if (!classes.contains(c)) {
            throw new IllegalArgumentException("The given URI " + c + " cannot be associated to a class");
        }
    }

    private void throwErrorIfNotClass(Set<URI> c) {
        if (!classes.containsAll(c)) {
            // Search a example of URI which cannnot be associated to a class
            String ex = null;
            for(URI ce  : c){
                if(!c.contains(ce)){
                    ex = ce.toString();
                    break;
                }
            }
            throw new IllegalArgumentException("The given set of URIs " + c + " cannot be associated to a class, e.g. "+ex);
        }
    }

    /**
     * CACHED ! Be careful modification of RelTypes requires cache clearing
     *
     * @param a
     * @param b
     * @return
     * @throws SLIB_Ex_Critic
     */
    public double getShortestPath(URI a, URI b, GWS weightingScheme) throws SLIB_Ex_Critic {

        if (cache.shortestPath.get(a) == null) {

            WalkConstraint wc = WalkConstraintUtils.copy(ancGetter.getWalkConstraint());
            wc.addWalkconstraints(descGetter.getWalkConstraint());

            Dijkstra dijkstra = new Dijkstra(graph, wc, weightingScheme);
            ConcurrentHashMap<URI, Double> minDists_cA = dijkstra.shortestPath(a);
            cache.shortestPath.put(a, minDists_cA);
        }
        return cache.shortestPath.get(a).get(b);
    }

    /**
     * NOT_CACHED
     *
     * @param a
     * @param b
     * @return
     * @throws SLIB_Ex_Critic
     * @throws IllegalAccessException if the given URI cannot be associated to a
     * class
     */
    public URI getMSA(URI a, URI b, GWS weightingScheme) throws SLIB_Ex_Critic {

        Dijkstra dijkstra = new Dijkstra(graph, ancGetter.getWalkConstraint(), weightingScheme);

        URI msa_pk = SimDagEdgeUtils.getMSA_pekar_staab(getRoot(), getAllShortestPath(a, weightingScheme), getAllShortestPath(b, weightingScheme), getAncestorsInc(a), getAncestorsInc(b), dijkstra);

        return msa_pk;
    }

    /**
     * CACHED
     *
     * @param a
     * @return a map containing the weight of the shortest path linking a the
     * given vertex.
     *
     * @throws SLIB_Ex_Critic
     */
    public synchronized Map<URI, Double> getAllShortestPath(URI a, GWS weightingScheme) throws SLIB_Ex_Critic {

        if (cache.shortestPath.get(a) == null) {

            WalkConstraint wc = WalkConstraintUtils.copy(ancGetter.getWalkConstraint());
            wc.addWalkconstraints(descGetter.getWalkConstraint());

            Dijkstra dijkstra = new Dijkstra(graph, wc, weightingScheme);
            ConcurrentHashMap<URI, Double> minDists_cA = dijkstra.shortestPath(a);
            cache.shortestPath.put(a, minDists_cA);
        }

        return cache.shortestPath.get(a);
    }

    /**
     *
     * @param conf
     * @param a
     * @param b
     * @return
     * @throws SLIB_Exception
     * @throws IllegalAccessException if the given URI cannot be associated to a
     * class
     */
    public double getP_MICA(ICconf conf, URI a, URI b) throws SLIB_Exception {

        double prob_mica = IcUtils.searchMin_pOc_MICA(getAncestorsInc(a), getAncestorsInc(b), getIC_results(conf));
        return prob_mica;
    }

    /**
     * NOT_CACHED
     *
     * @param a
     * @param b
     * @return
     * @throws IllegalAccessException if the given URI cannot be associated to a
     * class
     */
    public Set<URI> getHypoAncEx(URI a, URI b) {

        Set<URI> anc_a = getAncestorsInc(a);
        Set<URI> anc_b = getAncestorsInc(b);

        Set<URI> unionAncestors = SetUtils.union(anc_a, anc_b);
        Set<URI> interAncestors = SetUtils.union(anc_a, anc_b);

        Set<URI> ancsEx = unionAncestors;
        unionAncestors.removeAll(interAncestors);

        Set<URI> hypoAncsEx = new HashSet<URI>();

        for (URI v : ancsEx) {
            Set<URI> descCurAnc = descGetter.getRV(v);
            hypoAncsEx = SetUtils.union(hypoAncsEx, descCurAnc);
        }
        return hypoAncsEx;
    }
}
