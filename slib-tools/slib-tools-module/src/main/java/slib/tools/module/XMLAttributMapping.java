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
        
        aMap.put("REROOTING", GActionType.REROOTING );
        aMap.put("TRANSITIVE_REDUCTION", GActionType.TRANSITIVE_REDUCTION );
        aMap.put("TYPE_VERTICES", GActionType.TYPE_VERTICES );
        aMap.put("RDFS_INFERENCE", GActionType.RDFS_INFERENCE );
        aMap.put("VERTICES_REDUCTION", GActionType.VERTICES_REDUCTION );
        
        
        GActionTypeMapping = Collections.unmodifiableMap(aMap);
    }

}
