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
package slib.sml.sm.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.extraction.rvf.RVF_TAX;
import slib.sglib.algo.metric.DepthAnalyserAG;
import slib.sglib.algo.shortest_path.Dijkstra;
import slib.sglib.algo.traversal.classical.DFS;
import slib.sglib.algo.utils.RooterDAG;
import slib.sglib.algo.utils.WalkConstraintTax;
import slib.sglib.algo.validator.dag.ValidatorDAG;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.weight.GWS;
import slib.sglib.model.repo.impl.DataFactoryMemory;
import slib.sglib.model.voc.SLIBVOC;
import slib.sml.sm.core.measures.Sim_Groupwise_AddOn;
import slib.sml.sm.core.measures.Sim_Groupwise_Standalone;
import slib.sml.sm.core.measures.Sim_Pairwise;
import slib.sml.sm.core.measures.framework.core.engine.GraphRepresentation;
import slib.sml.sm.core.measures.framework.core.engine.RepresentationOperators;
import slib.sml.sm.core.measures.framework.core.measures.Sim_Framework;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.utils.SimDagEdgeUtils;
import slib.sml.sm.core.measures.graph.pairwise.dag.hybrid.utils.SimDagHybridUtils;
import slib.sml.sm.core.metrics.ic.annot.ICcorpus;
import slib.sml.sm.core.metrics.ic.topo.ICtopo;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Corpus;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.metrics.ic.utils.IcUtils;
import slib.sml.sm.core.metrics.vector.VectorWeight_Chabalier_2007;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.ex.SLIB_Ex_Warning;
import slib.utils.impl.MatrixDouble;
import slib.utils.impl.ResultStack;
import slib.utils.impl.SetUtils;

import com.tinkerpop.blueprints.Direction;
import java.lang.reflect.Constructor;
import slib.sglib.model.graph.elements.impl.VertexTyped;
import slib.sglib.model.repo.DataFactory;

/**
 * {@link SM_Engine} is used as an accessor to numerous commonly required
 * methods for SMs computation. Depending on the tuning of the engine some
 * caching system can be taken into account in order to boost some process.
 *
 * A {@link SM_Engine} instance is used by SMs in order to access the ancestors
 * or descendants of particular vertices.
 *
 * @author Sebastien Harispe
 *
 */
public class SM_Engine {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    DataFactory factory = DataFactoryMemory.getSingleton();
    final G graph;
    RVF_TAX ancGetter;
    RVF_TAX descGetter;
    Set<URI> allRelTypes;
    /**
     * Set of URI associated to the predicates admitted to perform bottom up
     * traversal on the underlying taxonomic graph of the considered graph e.g.
     * SUBCLASSOF (PART_OF)
     */
    Set<URI> goToSuperClassETypes;
    V root = null;
//	VirtualInstancesAccessor iAccessorCorpus;
    SMProxResultStorage cache;
    boolean cachePairwiseResults = false;
    /**
     * TODO Move to {@link GWS}
     */
    ResultStack<V, Double> vectorWeights = null;
    ValidatorDAG validator;
    Map<SMconf, Sim_Pairwise> pairwiseMeasures;
    Map<SMconf, Sim_Groupwise_AddOn> groupwiseAddOnMeasures;
    Map<SMconf, Sim_Groupwise_Standalone> groupwiseStandaloneMeasures;

    public SM_Engine(ValidatorDAG validatorDag, G g,
            Set<URI> setEtypes_a) throws SLIB_Ex_Critic {

        this.graph = g;
        this.validator = validatorDag;

        this.goToSuperClassETypes = setEtypes_a;

        // store all relationship
        allRelTypes = new HashSet<URI>();
        allRelTypes.addAll(goToSuperClassETypes);

        ancGetter = new RVF_TAX(g, Direction.OUT);
        descGetter = new RVF_TAX(g, Direction.IN);

        init();
    }

    private void init() throws SLIB_Ex_Critic {

        cache = new SMProxResultStorage();
        pairwiseMeasures = new ConcurrentHashMap<SMconf, Sim_Pairwise>();

        groupwiseAddOnMeasures = new ConcurrentHashMap<SMconf, Sim_Groupwise_AddOn>();
        groupwiseStandaloneMeasures = new ConcurrentHashMap<SMconf, Sim_Groupwise_Standalone>();
    }

    public SM_Engine(G g) throws SLIB_Exception {
        this(new ValidatorDAG(), g, SetUtils.buildSet(RDFS.SUBCLASSOF));
    }

    /**
     * Compute the union of the inclusive ancestors of a set of classes
     *
     * @NOT_CACHED
     * @param setClasses the set of classes considered
     * @return the union of the inclusive ancestors of the given classes
     * @see SM_manager#getAncestors(V);
     */
    public Set<V> getAncestors(Set<V> setClasses) {

        Set<V> unionAnc = new HashSet<V>();

        for (V v : setClasses) {
            unionAnc = SetUtils.union(unionAnc, getAncestors(v));
        }
        return unionAnc;
    }

    /**
     * Return inclusive ancestors of a class as a set of {@link V} i.e v its
     * exclusive ancestors.
     *
     * @CACHED
     * @param v the considered class
     * @return the set of inclusive ancestors of the given class
     */
    public Set<V> getAncestors(V v) {

        if (cache.ancestors.get(v) == null) {
            Set<V> anc = ancGetter.getRV(v);
            cache.ancestors.put(v, anc);
        }
        return cache.ancestors.get(v);
    }

    public Set<V> getParents(V v) {

        Set<E> edges = graph.getE(goToSuperClassETypes, v, Direction.OUT);
        Set<V> parents = new HashSet<V>();

        for (E e : edges) {
            parents.add(e.getTarget());
        }

        return parents;
    }

    /**
     * Compute disjoint ancestors. TO OPTIMIZE
     *
     * @param c1
     * @param c2
     * @return a set containing disjoint ancestors as a set of vertices
     */
    public Set<V> getDisjointCommonAncestors(V c1, V c2) {

        Set<V> ancC1 = getAncestors(c1);
        Set<V> ancC2 = getAncestors(c2);

        Set<V> commonAncs = SetUtils.intersection(ancC1, ancC2);

        // search for disjoint ancestors

        Map<V, Set<V>> ancestorsMapping = new HashMap<V, Set<V>>();

        for (V v : commonAncs) {
            ancestorsMapping.put(v, getAncestors(v));
        }

        Set<V> disjointAncs = new HashSet<V>();

        for (Entry<V, Set<V>> entry : ancestorsMapping.entrySet()) {

            Set<V> anc = entry.getValue();
            boolean valid = true;

            for (Entry<V, Set<V>> entry2 : ancestorsMapping.entrySet()) {

                if (!(entry2.getKey().equals(entry.getKey())) && entry2.getValue().containsAll(anc)) {
                    //logger.debug(entry2.getKey()+" contains "+entry.getKey());
                    valid = false;
                    break;
                }
            }
            if (valid) {
                disjointAncs.add(entry.getKey());
            }
        }
        return disjointAncs;
    }

    /**
     * @CACHED
     *
     * @return @throws SGL_Ex_Critic
     */
    public ResultStack<V, Integer> getMaxDepths() throws SLIB_Exception {

        if (cache.maxDepths == null) {
            DepthAnalyserAG dephtAnalyser = new DepthAnalyserAG(factory, graph, new WalkConstraintTax(RDFS.SUBCLASSOF, Direction.IN));
            cache.maxDepths = dephtAnalyser.getVMaxDepths();
        }

        return cache.maxDepths;
    }

    /**
     * @CACHED
     *
     * @return @throws SGL_Ex_Critic
     */
    public ResultStack<V, Integer> getMinDepths() throws SLIB_Exception {

        if (cache.minDepths == null) {
            DepthAnalyserAG dephtAnalyser = new DepthAnalyserAG(factory, graph, new WalkConstraintTax(RDFS.SUBCLASSOF, Direction.IN));
            cache.minDepths = dephtAnalyser.getVMinDepths();
        }

        return cache.minDepths;
    }

    /**
     * @CACHED
     *
     * @param icLabel
     * @param v
     * @return
     * @throws SGL_Ex_Critic
     */
    public double getIC(ICconf icConf, V v) throws SLIB_Exception {

        if (cache.metrics_results.get(icConf) == null) {
            computeIC(icConf);
        }

        return cache.metrics_results.get(icConf).get(v);
    }

    /**
     * @CACHED
     *
     * @return @throws SGL_Ex_Critic
     */
    public int getMaxDepth() throws SLIB_Exception {

        if (cache.maxDepth == null) {

            ResultStack<V, Integer> maxDepths = getMaxDepths();

            int maxDepth = 0;

            // Compute max depth
            for (V v : maxDepths.keySet()) {
                if (maxDepths.get(v) > maxDepth) {
                    maxDepth = maxDepths.get(v);
                }
            }
            cache.maxDepth = maxDepth;
        }
        return cache.maxDepth.intValue();
    }

    /**
     * @CACHED ! Be careful modification of RelTypes requires cache clearing
     * @param a
     * @param b
     * @return
     * @throws SGL_Ex_Critic
     */
    public double getShortestPath(V a, V b) throws SLIB_Ex_Critic {

        if (cache.shortestPath.get(a) == null) {
            Dijkstra dijkstra = new Dijkstra(graph, allRelTypes);
            ConcurrentHashMap<V, Double> minDists_cA = dijkstra.shortestPath(a);
            cache.shortestPath.put(a, minDists_cA);
        }
        return cache.shortestPath.get(a).get(b);
    }

    /**
     * @NOT_CACHED
     * @param a
     * @param b
     * @return
     * @throws SGL_Ex_Critic
     */
    public V getMSA(V a, V b) throws SLIB_Ex_Critic {

        Dijkstra dijkstra = new Dijkstra(graph, allRelTypes);

        V msa_pk = SimDagEdgeUtils.getMSA_pekar_staab(getRoot(), getAllShortestPath(a), getAllShortestPath(b), getAncestors(a), getAncestors(b), dijkstra);

        return msa_pk;
    }

    /**
     * @throws SGL_Ex_Critic
     */
    public synchronized V getRoot() throws SLIB_Ex_Critic {
        if (root == null) {
            URI rooturi = RooterDAG.rootUnderlyingTaxonomicDAG(graph, SLIBVOC.UNIVERSAL_ROOT);
            root = (V) graph.getV(rooturi);
        }
        return root;
    }

    /**
     * @CACHED @param a
     * @return
     * @throws SGL_Ex_Critic
     */
    public Map<V, Double> getAllShortestPath(V a) throws SLIB_Ex_Critic {

        if (cache.shortestPath.get(a) == null) {
            Dijkstra dijkstra = new Dijkstra(graph, allRelTypes);
            ConcurrentHashMap<V, Double> minDists_cA = dijkstra.shortestPath(a);
            cache.shortestPath.put(a, minDists_cA);
        }

        return cache.shortestPath.get(a);
    }

    /**
     * @NOT_CACHED
     * @param a
     * @param b
     * @return
     */
    public Set<V> getHypoAncEx(V a, V b) {

        Set<V> anc_a = getAncestors(a);
        Set<V> anc_b = getAncestors(b);

        Set<V> unionAncestors = SetUtils.union(anc_a, anc_b);
        Set<V> interAncestors = SetUtils.union(anc_a, anc_b);

        Set<V> ancsEx = unionAncestors;
        unionAncestors.removeAll(interAncestors);

        Set<V> hypoAncsEx = new HashSet<V>();

        for (V v : ancsEx) {
            Set<V> descCurAnc = descGetter.getRV(v);
            hypoAncsEx = SetUtils.union(hypoAncsEx, descCurAnc);
        }
        return hypoAncsEx;
    }

    /**
     * @CACHED Return a set of vertices corresponding to the inclusive
     * descendants of a term t i.e t + descendants of t
     * @param v
     * @return
     */
    public Set<V> getDescendants(V v) {
        if (cache.descendants.get(v) == null) {
            Set<V> rv = descGetter.getRV(v);
            cache.descendants.put(v, rv);
        }
        return cache.descendants.get(v);
    }

    //	/**
    //	 * @NOT_CACHED
    //	 * @return
    //	 */
    //	public HashMap< V,Collection<V> > getAllHypo(){
    //		return rvfDesc.getAllVertices();
    //	}
    /**
     * @NOT_CACHED
     * @param v
     * @return
     */
    public Map<V, Double> computeSemanticContribution(V v) {
        SimDagHybridUtils SimDagHybridUtil = new SimDagHybridUtils();
        Map<V, Double> sContrib_A = SimDagHybridUtil.computeSemanticContribution_Wang_2007(v, getAncestors(v), graph, goToSuperClassETypes);

        return sContrib_A;
    }

    /**
     * @NOT_CACHED
     * @param v
     * @return
     */
    public double computeSV_Wang_2007(V v) {
        SimDagHybridUtils SimDagHybridUtil = new SimDagHybridUtils();
        Map<V, Double> sContrib_A = SimDagHybridUtil.computeSemanticContribution_Wang_2007(v, getAncestors(v), graph, goToSuperClassETypes);
        double sv_A = SimDagHybridUtil.computeSV_Wang_2007(sContrib_A);
        return sv_A;
    }

    /**
     * @CACHED @param a
     * @param b
     * @return
     * @throws SGL_Exception_Warning
     * @throws SGL_Ex_Critic
     */
    public double getIC_MICA(ICconf icConf, V a, V b) throws SLIB_Exception {

        if (cache.metrics_results.get(icConf) == null) {
            computeIC(icConf);
        }


        double ic_mica = IcUtils.searchMax_IC_MICA(getAncestors(a), getAncestors(b), getIC_results(icConf));
        return ic_mica;
    }

    public double getP_MICA(ICconf conf, V a, V b) throws SLIB_Exception {

        double prob_mica = IcUtils.searchMin_pOc_MICA(getAncestors(a), getAncestors(b), getIC_results(conf));
        return prob_mica;
    }

    public ResultStack<V, Long> getAllNbDescendants() throws SLIB_Ex_Critic {
        Map<V, Set<V>> allDescendants = descGetter.getAllRVClass();
        ResultStack<V, Long> allNbDescendants = new ResultStack<V, Long>();

        for (V c : graph.getVClass()) {
            int nbDesc = allDescendants.get(c).size();
            allNbDescendants.add(c, (long) nbDesc);
        }
        return allNbDescendants;
    }

    public ResultStack<V, Double> getIC_results(ICconf icConf) throws SLIB_Ex_Critic {


        if (cache.metrics_results.get(icConf) == null) {
            cache.metrics_results.put(icConf, computeIC(icConf));
        }


        return cache.metrics_results.get(icConf);
    }

    public synchronized ResultStack<V, Double> computeIC(ICconf icConf) throws SLIB_Ex_Critic {

        if (icConf == null) {
            throw new SLIB_Ex_Critic("Unknown IC " + icConf);
        }

        logger.info("computing IC " + icConf.getId());

        if (cache.metrics_results.get(icConf) != null) {
            return cache.metrics_results.get(icConf);
        }

        Class<?> cl;
        ResultStack<V, Double> results = new ResultStack<V, Double>();

        try {



            String icClassName = icConf.getClassName();
            logger.info("Computing " + icClassName);

            if (icConf instanceof IC_Conf_Corpus) {

                cl = Class.forName(icClassName);
                Constructor<?> co = cl.getConstructor();
                ICcorpus o = (ICcorpus) co.newInstance();

                results = o.compute(this);
            } else {

                IC_Conf_Topo icConfTopo = (IC_Conf_Topo) icConf;

                cl = Class.forName(icClassName);
                Constructor<?> co = cl.getConstructor();
                ICtopo o = (ICtopo) co.newInstance();

                results = o.compute(icConfTopo, this);
            }

            cache.metrics_results.put(icConf, results);

            logger.debug(results.toString());

            return cache.metrics_results.get(icConf);

        } catch (Exception e) {
            e.printStackTrace();
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    public ResultStack<V, Double> getAllNbReachableLeaves() {

        logger.debug("Computing Nb Reachable Leaves : start");

        HashMap<V, Set<V>> allReachableLeaves = descGetter.getTerminalVertices();
        ResultStack<V, Double> allNbReachableLeaves = new ResultStack<V, Double>();

        for (V c : graph.getVClass()) {

            int nbDesc = allReachableLeaves.get(c).size();
            allNbReachableLeaves.add(c, (double) nbDesc);
        }

        logger.debug("Computing Nb Reachable Leaves : end");

        return allNbReachableLeaves;
    }

    public ResultStack<V, Double> getAllNbAncestors() throws SLIB_Ex_Critic {

        Map<V, Set<V>> allAncestors = ancGetter.getAllRVClass();
        ResultStack<V, Double> allNbancestors = new ResultStack<V, Double>();

        for (V c : graph.getVClass()) {
            int nbDesc = allAncestors.get(c).size();
            allNbancestors.add(c, (double) nbDesc);
        }
        return allNbancestors;
    }

    public int getNbVertices() {
        return graph.getV().size();
    }

    /**
     * @NOT_CACHED by default
     */
    public double computePairwiseSim(SMconf pairwiseConf, V a, V b) throws SLIB_Ex_Critic {

        double sim = -Double.MAX_VALUE;

        try {

            if (cachePairwiseResults
                    && cache.pairwise_results.containsKey(pairwiseConf)
                    && cache.pairwise_results.get(pairwiseConf).containsKey(a)
                    && cache.pairwise_results.get(pairwiseConf).get(a).containsKey(b)) {

                sim = cache.pairwise_results.get(pairwiseConf).get(a).get(b);
            } else {


                if (SMConstants.SIM_FRAMEWORK.containsKey(pairwiseConf.flag)) {

                    sim = computeSimFramework(pairwiseConf, a, b);

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

                }

                if (Double.isNaN(sim) || Double.isInfinite(sim)) {
                    SMutils.throwArithmeticCriticalException(pairwiseConf, a, b, sim);
                }

                // Caching 

                if (cachePairwiseResults) {

                    if (cache.pairwise_results.get(pairwiseConf) == null) {

                        ConcurrentHashMap<V, ResultStack<V, Double>> pairwise_result = new ConcurrentHashMap<V, ResultStack<V, Double>>();

                        cache.pairwise_results.put(pairwiseConf, pairwise_result);
                    }

                    if (cache.pairwise_results.get(pairwiseConf).get(a) == null) {
                        cache.pairwise_results.get(pairwiseConf).put(a, new ResultStack<V, Double>());
                    }

                    cache.pairwise_results.get(pairwiseConf).get(a).add(b, sim);
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

    private double computeSimFramework(SMconf measure_conf, V a, V b) throws Exception {

        String measureClass = measure_conf.className;
        String representation = SMConstants.representation.get(measure_conf.representation);
        String operator = SMConstants.operators.get(measure_conf.operator.flag);

        //		System.out.println("Framework measure");
        //		
        //		
        //		System.out.println(": Measure "+measureClass);
        //		System.out.println(": Representation "+representation);
        //		System.out.println(": Operator "+operator);

        Class<?> cl;

        // Measure
        cl = Class.forName(measureClass);
        Constructor<?> co_measure = cl.getConstructor();

        // Representation
        cl = Class.forName(representation);
        Constructor<?> co_representation = cl.getConstructor(Resource.class, this.getClass());

        // Operator
        cl = Class.forName(operator);
        Constructor<?> co_operator = cl.getConstructor(OperatorConf.class);


        Sim_Framework measure = (Sim_Framework) co_measure.newInstance();
        GraphRepresentation a_rep = (GraphRepresentation) co_representation.newInstance(a, this);
        GraphRepresentation b_rep = (GraphRepresentation) co_representation.newInstance(b, this);
        RepresentationOperators op = (RepresentationOperators) co_operator.newInstance(measure_conf.operator);

        double sim = measure.sim(a_rep, b_rep, this, op, measure_conf);

        return sim;
    }

    /**
     * @NOT_CACHED
     */
    public double computeGroupwiseStandaloneSim(
            SMconf confGroupwise,
            Set<V> setA,
            Set<V> setB) throws SLIB_Ex_Critic {

        double sim = -Double.MAX_VALUE;


        try {

            Sim_Groupwise_Standalone gMeasure;

            synchronized (groupwiseStandaloneMeasures) {

                if (groupwiseStandaloneMeasures.containsKey(confGroupwise)) {
                    gMeasure = groupwiseStandaloneMeasures.get(confGroupwise);
                } else {
                    Class<?> cl;
                    String groupwiseClassName = confGroupwise.className;
                    cl = Class.forName(groupwiseClassName);
                    Constructor<?> co = cl.getConstructor();

                    gMeasure = (Sim_Groupwise_Standalone) co.newInstance();
                    groupwiseStandaloneMeasures.put(confGroupwise, gMeasure);
                }
            }
            sim = gMeasure.sim(setA, setB, this, confGroupwise);

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new SLIB_Ex_Critic(e);
        }
        return sim;
    }

    /**
     * @NOT_CACHED TODO add measure caching
     */
    public double computeGroupwiseAddOnSim(
            SMconf confGroupwise,
            SMconf confPairwise,
            Set<V> setA,
            Set<V> setB) throws SLIB_Ex_Critic {

        double sim = -Double.MAX_VALUE;

        try {
            Sim_Groupwise_AddOn gMeasure;

            synchronized (groupwiseAddOnMeasures) {

                if (groupwiseAddOnMeasures.containsKey(confGroupwise)) {
                    gMeasure = groupwiseAddOnMeasures.get(confGroupwise);
                } else {
                    Class<?> cl;
                    String groupwiseClassName = confGroupwise.className;
                    cl = Class.forName(groupwiseClassName);
                    Constructor<?> co = cl.getConstructor();

                    gMeasure = (Sim_Groupwise_AddOn) co.newInstance();
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

    public ResultStack<V, Long> getnbPathLeadingToAllVertex() throws SLIB_Exception {

        if (cache.nbPathLeadingToAllVertices == null) {
            cache.nbPathLeadingToAllVertices = (ResultStack<V, Long>) descGetter.computeNbPathLeadingToAllVertices();
        }

        return cache.nbPathLeadingToAllVertices;
    }

//	public VirtualInstancesAccessor getInstanceAccessor(){
//		return iAccessorCorpus;
//	}
    public ResultStack<V, Long> getNbInstancesInferredPropFromCorpus() {

        HashMap<V, Set<V>> linkedEntities = new HashMap<V, Set<V>>();
        Set<V> instances = graph.getV(VType.INSTANCE);



        for (V o : instances) {
            Set<V> annots = graph.getV(o, RDF.TYPE, Direction.OUT);

            if (annots == null) {
                annots = new HashSet<V>();
            }

            for (V v : annots) {
                Set<V> instancesV = linkedEntities.get(v);

                if (instancesV == null) {
                    instancesV = new HashSet<V>();
                }

                instancesV.add(o);

                linkedEntities.put(v, instancesV);
            }

        }
        // Get Topological ordering trough DFS
        // - get roots
        Set<V> roots = new ValidatorDAG().getDAGRoots(graph, goToSuperClassETypes, Direction.OUT);

        DFS dfs = new DFS(graph, roots, goToSuperClassETypes, Direction.IN);
        List<V> topoOrdering = dfs.getTraversalOrder();


        ResultStack<V, Long> rStack = new ResultStack<V, Long>();

        // initialize data structure
        for (int i = 0; i < topoOrdering.size(); i++) {
            if (linkedEntities.get(topoOrdering.get(i)) == null) {
                linkedEntities.put(topoOrdering.get(i), new HashSet<V>());
            }
        }

        for (int i = 0; i < topoOrdering.size(); i++) {

            V currentV = topoOrdering.get(i);
            Set<V> entities = linkedEntities.get(currentV);

            // propagate Linked Entities in a bottom up fashion according the topological order
            for (E e : graph.getE(goToSuperClassETypes, currentV, Direction.OUT)) {
                if (!entities.isEmpty()) {
                    linkedEntities.get(e.getTarget()).addAll(entities);
                }
            }

            rStack.add(currentV, (long) entities.size());
        }
        cache.nbOccurrencePropagatted = rStack;

        return cache.nbOccurrencePropagatted;
    }

//	//TODO cache
//	public  ResultStack<V,Long> getNbInstancesInferredFromCorpus(){
//		return new ResultStack<V, Long>(iAccessorCorpus.getInferredInstancesNumberMapping());
//	}
//	public  ResultStack<V,Long> getNbOccurrencePropFromCorpus(VirtualInstancesAccessor iAccessor) throws SGL_Exception_Critical {
//
//
//		HashMap<V,Set<V>> linkedEntities = new HashMap<V,Set<V>>();
//
//		Set<V> instances = g.getV(VTypeGeneric.INSTANCE);
//		
//		for(V o : instances)
//			linkedEntities.put(o, g.getV(o,RDF.TYPE,Direction.OUT));
//
//		// Get Topological ordering trough DFS
//		// - get roots
//		Set<V> roots = new ValidatorDAG().getDAGRoots(g, goToSuperClassETypes, Direction.OUT);
//
//		DFS dfs = new DFS(g, roots, goToSuperClassETypes, Direction.IN);
//		List<V> topoOrdering = dfs.getTraversalOrder();
//
//
//		ResultStack<V,Long> rStack = new ResultStack<V,Long>();
//
//		for (int i = 0; i< topoOrdering.size(); i++) {
//
//			V currentV = topoOrdering.get(i);
//			Set<V> entities = linkedEntities.get(currentV);
//
//			// propagate Linked Entities in a bottom up fashion according the topological order
//			for(E e : g.getE(goToSuperClassETypes, currentV,Direction.OUT)){
//				if(!entities.isEmpty())
//					linkedEntities.get(e.getTarget()).addAll(entities);
//			}
//
//			rStack.add(currentV, (long) entities.size());
//		}
//		cache.nbOccurrencePropagatted = rStack;
//
//	return cache.nbOccurrencePropagatted;
//}
    /**
     * Topological propagation considering one occurrence per term
     *
     * @return
     * @throws SGL_Ex_Critic
     */
    public ResultStack<V, Long> getNbOccurrenceProp() throws SLIB_Exception {

        if (cache.nbOccurrencePropagatted == null) {

            RVF_TAX RVF = new RVF_TAX(graph, Direction.IN);
            ResultStack<V, Long> nbOccurrences = new ResultStack<V, Long>();

            for (V o : graph.getVClass()) {
                nbOccurrences.add(o, (long) 1);
            }

            ResultStack<V, Long> nbOccurrencesPropagated = (ResultStack<V, Long>) RVF.propagateNbOccurences(nbOccurrences);
            cache.nbOccurrencePropagatted = nbOccurrencesPropagated;
        }

        return cache.nbOccurrencePropagatted;
    }

    /**
     * Manage Symmetry
     *
     * @param setA
     * @param setB
     * @return
     * @throws SGL_Ex_Critic
     */
    public MatrixDouble<V, V> getMatrixScore(
            Set<V> setA,
            Set<V> setB,
            SMconf pairwiseConf) throws SLIB_Ex_Critic {


        MatrixDouble<V, V> m = new MatrixDouble<V, V>(setA, setB);

        for (V a : setA) {

            for (V b : setB) {
                double sim = computePairwiseSim(pairwiseConf, a, b);
                m.setValue(a, b, sim);
            }
        }
        return m;
    }

    public boolean isComputed(String icID) {
        if (cache.metrics_results.get(icID) == null) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isCachePairwiseResults() {
        return cachePairwiseResults;
    }

    public void setCachePairwiseResults(boolean cachePairwiseResults) {
        logger.info("Pairwise results caching set to " + cachePairwiseResults);
        this.cachePairwiseResults = cachePairwiseResults;
    }

    public ResultStack<V, Double> getSvalues(V a) {

        throw new UnsupportedOperationException();

    }

    public ResultStack<V, Double> getVector(Set<V> set, SMconf groupwiseconf) {

        if (vectorWeights == null) {
            vectorWeights = VectorWeight_Chabalier_2007.compute(graph);
        }

        ResultStack<V, Double> vector = new ResultStack<V, Double>();

        Set<V> setAncestors;
        setAncestors = set; // unpropragatted
        //		setAncestors = getAncestors(set);

        for (Entry<V, Double> e : vectorWeights.getValues().entrySet()) {

            V v = e.getKey();

            if (setAncestors.contains(v)) {
                vector.add(v, e.getValue());
            } else {
                vector.add(v, 0.);
            }

        }
        return vector;
    }

//	public void applyPostLoadingFilters(GraphConf gConf) throws SGL_Exception {
//
//
//			Set<FilterGraph> filters = FilterRepository.getInstance().getFilters();
//
//			for(FilterGraph f : filters){
//
//				if(f instanceof FilterGraph_Metrics){
//
//					FilterGraph_Metrics fm = (FilterGraph_Metrics)f ;
//
//					ResultStack<V,Double> metric_resutls = null;
//					// retrieve associated metric results
//					for(ICconf confIC : cache.metrics_results.keySet()){
//
//						if(confIC.getId().equals( fm.getMetric()) ){
//							metric_resutls = cache.metrics_results.get(confIC);
//						}
//					}
//					if(metric_resutls == null)
//						throw new SGL_Ex_Critic("Cannot find result for metric "+fm.getMetric()+" use in filter "+fm.getId());
//
//					GraphCleaner.removeAnnotationsFromMetric(g,metric_resutls,fm.getValue(),fm.isRemoveEmpty());
//				}
//		}
//	}
    public GraphRepresentation getRepresentation(V a, SMconf conf) {
        throw new UnsupportedOperationException();
    }

    public G getGraph() {
        return graph;
    }

    public Set<URI> getGoToSuperClassETypes() {
        return goToSuperClassETypes;
    }
}
