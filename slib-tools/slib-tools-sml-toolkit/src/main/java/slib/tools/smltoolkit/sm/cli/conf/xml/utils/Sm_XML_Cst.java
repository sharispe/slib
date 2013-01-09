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
 
 
package slib.tools.smltoolkit.sm.cli.conf.xml.utils;

/**
 *
 * @author seb
 */
public class Sm_XML_Cst {
	
	// Tags

	/**
     *
     */
    public static final String SML_TAG			= "sml";
	
	/**
     *
     */
    public static final String SML_SM			= "sm";
	
	/**
     *
     */
    public static final String SML_SM_include	= "include";

	/**
     *
     */
    public static final String MEASURES_TAG  	= "measures";

	/**
     *
     */
    public static final String ICS_TAG 		 	= "ics";
	
	/**
     *
     */
    public static final String OPERATORS_TAG 	= "operators";
	/**
     *
     */
    public static final String OPERATOR_TAG  	= "operator";
	
	

	/**
     *
     */
    public static final String QUERIES_TAG 		 = "queries";

	/**
     *
     */
    public static final String QUERIES_TYPE_CTOC = "cTOc";

	/**
     *
     */
    public static final String QUERIES_TYPE_OTOO = "oTOo";
	/**
     *
     */
    public static final String QUERIES_TYPE_OTOO_FULL = "oTOo_full";

	/**
     *
     */
    public static final String QUERY_TAG 	= "query";

	/**
     *
     */
    public static final String MEASURE_TAG  = "measure";

	/**
     *
     */
    public static final String IC_TAG 		= "ic";
	/**
     *
     */
    public static final String IC_ATTR 		= "ic";
	
	/**
	 * Value to specify to compute measures using all loaded IC
	 */
	public static final String IC_ATTR_VALUE_FULL_LIST = "[FULL_LIST_IC]";
	
	/**
     *
     */
    public static final String PAIRWISE_MEASURE_ATTR 		= "pairwise_measure";
	
	/**
	 * Value to specify to compute measures using all loaded IC
	 */
	public static final String PAIRWISE_MEASURE_ATTR_VALUE_FULL_LIST = "[FULL_LIST_PM]";
	
	
	/**
     *
     */
    public static final String IC_PROB 		= "ic_prob";
	
	/**
     *
     */
    public static final String FLAG_ATTR   			 = "flag";
	/**
     *
     */
    public static final String REPRESENTATION_ATTR   = "rep";
	/**
     *
     */
    public static final String OPERATOR_ID   		 = "operator";
	/**
     *
     */
    public static final String OPERATOR_FLAG_ATTR    = "operator_flag";
	/**
     *
     */
    public static final String FORCE_EXEC_ATTR 		 = "force_exec";

	/**
     *
     */
    public static final String TYPE_VALUE_PAIRWISE  = "pairwise";
	/**
     *
     */
    public static final String TYPE_VALUE_GROUPWISE = "groupwise";

	
	//public static final String CONF_XSD = "modules/ssp_eval/conf/conf.xsd";


	
	/**
     *
     */
    public static final String E1_ATTR = "e1";
	/**
     *
     */
    public static final String E2_ATTR = "e2";
	
	/**
     *
     */
    public static final String C1_ATTR = "c1";
	/**
     *
     */
    public static final String C2_ATTR = "c2";
	
	/**
     *
     */
    public static final String OPT_MODULE_TAG = "opt_module";
	
	/**
     *
     */
    public static final String OPT_BENCH_SIZE_ATTR 			= "bench_size";
	/**
     *
     */
    public static final String OPT_CACHE_PAIRWISE_ATTR 		= "cache_pairwise";
	/**
     *
     */
    public static final String OPT_SKIP_EMPTY_ANNOTS_ATTR   = "skipEmptyAnnots";
	/**
     *
     */
    public static final String OPT_EMPTY_ANNOTS_SCORE_ATTR  = "emptyAnnotsScore";
	
	// Errors
	/**
     *
     */
    final public static String ERROR_NB_GRAPH_SPEC = "Please specify a graph to load (only one graph is accepted)";

	/**
     *
     */
    public static final String SML_MODULE = "module";





}
