package slib.sglib.algo.graph.utils;

/**
 * Enumeration used to represent the various types of actions which can be applied to a graph.
 * 
 * @author Sébastien Harispe
 */
public enum GActionType {

    /**
     * Transitive Reduction.
     */
    TRANSITIVE_REDUCTION,
    
    /**
     * Rooting treatment.
     */
    REROOTING,
    
//    /**
//     * Action flag corresponding to the RDFS Inference.
//     */
//    RDFS_INFERENCE,
    
    /**
     * Reduction of vertices.
     */
    VERTICES_REDUCTION,
    
    /**
     * Predicate Substitute.
     */
    PREDICATE_SUBSTITUTE
}
