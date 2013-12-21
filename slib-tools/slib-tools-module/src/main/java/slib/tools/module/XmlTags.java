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
 
 
package slib.tools.module;


/**
 *
 * @author Harispe Sébastien
 */
public class XmlTags {
	
	/**
     *
     */
    public static final String VARIABLES_TAG 	   = "variables";
	/**
     *
     */
    public static final String VARIABLE_TAG 	   = "var";
	
	/**
     *
     */
    public static final String NAMESPACES_TAG 	   = "namespaces";
	/**
     *
     */
    public static final String NAMESPACE_TAG 	   = "nm";
	
	/**
     *
     */
    public static final String NS_ATTR_PREFIX 	   = "prefix";
	/**
     *
     */
    public static final String NS_ATTR_REF 	   	   = "ref";
	
	/**
     *
     */
    public static final String KEY_ATTR 	   = "key";
	/**
     *
     */
    public static final String VALUE_ATTR 	   = "value";
	
	/**
     *
     */
    public static final String GRAPHS_TAG 		   = "graphs";
	/**
     *
     */
    public static final String GRAPH_TAG 		   = "graph";
	/**
     *
     */
    public static final String GRAPH_ATT 		   = "graph";
	/**
     *
     */
    public static final String GRAPH_RELATIONSHIP  = "relationship";
	
	/**
     *
     */
    public static final String KBS_TAG = "kbs";
	
	/**
     *
     */
    public static final String KB_TAG 		   = "kb";
	/**
     *
     */
    public static final String KB_ATTR 	       = "kb";
	/**
     *
     */
    public static final String KB_FILE_ATTR    = "kb_file";
	/**
     *
     */
    public static final String KB_FORMAT_ATTR  = "kb_format";
	/**
     *
     */
    public static final String KB_FILTERS_ATTR = "filters";
	
	/**
     *
     */
    public static final String TYPE_ATTR   = "type";
	/**
     *
     */
    public static final String OUTPUT_ATTR = "output";
	/**
     *
     */
    public static final String FILE_ATTR   = "file";
	
	/**
     *
     */
    public static final String FORMAT_ATTR = "format";
	/**
     *
     */
    public static final String URI_ATTR    = "uri";
	/**
     *
     */
    public static final String ID_ATTR     = "id";
	/**
     *
     */
    public static final String LABEL_ATTR  = "label";
	/**
     *
     */
    public static final String GRAPH_ATTR  = "graph";
	
	/**
     *
     */
    public static final String ROOT_ATTR  = "root";
	/**
     *
     */
    public static final String DAG_ATTR   = "rooted_dag";
	/**
     *
     */
    public static final String TRANSITIVE_REDUCTION_ATTR  = "transitive_reduction";
	
	/**
     *
     */
    public static final String FILTERS_TAG = "filters";
	/**
     *
     */
    public static final String FILTER_TAG  = "filter";
	/**
     *
     */
    public static final String CLEAN_TAG  = "clean";
	
	/**
     *
     */
    public static final String OPT_TAG 				= "opt";
	/**
     *
     */
    public static final String OPT_NB_THREADS_ATTR  = "threads";
	
	/**
     *
     */
    public static final String EXCLUDE_AUTO_MEASURE     = "exclude_auto_measure";
	
	/**
     *
     */
    public static final String DATA_TAG = "data";
	/**
     *
     */
    public static final String FILE_TAG = "file";
	
	/**
     *
     */
    public static final String ACTIONS_TAG = "actions";
	/**
     *
     */
    public static final String ACTION_TAG = "action";
	/**
     *
     */
    public static final String URI_PREFIX_ATTR = "uri_prefix";
    
    
    

	/**
     *
     */
    public static final String MAP_TAG = "map";
	
	/**
     *
     */
    public static final String MAP_ATT_FIELD  = "field";
	/**
     *
     */
    public static final String MAP_ATT_TYPE   = "type";
	/**
     *
     */
    public static final String MAP_ATT_PREFIX = "prefix";
	
	/**
     *
     */
    public static final String STM_TAG = "stm";
	
	/**
     *
     */
    public static final String STM_ATT_SUBJECT     = "subject";
	/**
     *
     */
    public static final String STM_ATT_PREDICATE   = "predicate";
	/**
     *
     */
    public static final String STM_ATT_OBJECT 	   = "object";
	
	/**
     *
     */
    public static final String STM_CONSTRAINT_TAG = "constraint";
	/**
     *
     */
    public static final String STM_CONSTRAINT_ATT_ELEMENT = "element";
	/**
     *
     */
    public static final String STM_CONSTRAINT_ATT_TYPE 	  = "type";
}
