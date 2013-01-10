package slib.sglib.algo.graph.utils;

/**
 * Enumeration used to represent the various types of actions which can be applied to a graph.
 * 
 * @author Harispe Sébastien
 */
public enum GActionType {

    /**
     * Classical Transitive Reduction,
     * We only consider taxonomic reduction.
     */
    TRANSITIVE_REDUCTION,
    
    /**
     * Action flag corresponding to a re rooting treatment.
     */
    REROOTING,
    
    /**
     * Action flag referring to the process of vertices typing.
     */
    TYPE_VERTICES,
    
    /**
     * Action flag corresponding to the RDFS Inference.
     */
    RDFS_INFERENCE,
    
    /**
     * Action flag corresponding to a reduction of vertices.
     */
    VERTICES_REDUCTION
}
