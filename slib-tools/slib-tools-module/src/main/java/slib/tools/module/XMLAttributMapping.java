package slib.tools.module;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import slib.sglib.algo.utils.GActionType;
import slib.sglib.io.util.GFormat;

public class XMLAttributMapping {
	
   
    
    public static final Map<String, GFormat> GDataFormatMapping;
    static {
        Map<String, GFormat> aMap = new HashMap<String, GFormat>();
        
        aMap.put("obo"  	, GFormat.OBO );
        aMap.put("rdf_xml"  , GFormat.RDF_XML );
        aMap.put("ntriples" , GFormat.NTRIPLES );
        aMap.put("gaf_2"	, GFormat.GAF2);
        aMap.put("csv"		, GFormat.CSV);
        
        GDataFormatMapping = Collections.unmodifiableMap(aMap);
    }
    
    public static final Map<String, GActionType> GActionTypeMapping;
    static {
        Map<String, GActionType> aMap = new HashMap<String, GActionType>();
        
        aMap.put("rerooting", GActionType.REROOTING );
        aMap.put("transitive_reduction", GActionType.TRANSITIVE_REDUCTION );
        aMap.put("type_vertices", GActionType.TYPE_VERTICES );
        aMap.put("rdfs_inference", GActionType.RDFS_INFERENCE );
        aMap.put("remove_RDFS_Extra_Vertices", GActionType.REMOVE_RDFS_EXTRA_VERTICES );
        aMap.put("vertices_reduction", GActionType.VERTICES_REDUCTION );
        
        
        GActionTypeMapping = Collections.unmodifiableMap(aMap);
    }

}
