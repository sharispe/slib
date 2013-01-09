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
 
 
package slib.sml.smbb.core;

import slib.tools.module.ModuleCst;


/**
 *
 * @author seb
 */
public class SmbbCst {
	

        /**
     *
     */
    public static String name = "smbb";
        
	/**
     *
     */
    public final static String type = "type";
	
	/**
     *
     */
    public final static String kb_id = "kb";
	/**
     *
     */
    public static String graph_uri = "graph";
	
	/**
     *
     */
    public final static String type_GO_PPI   = "GO.interact";
	/**
     *
     */
    public final static String type_GO_PFAM  = "GO.pFam";
	/**
     *
     */
    public final static String type_GO_EC    = "GO.ec";
	/**
     *
     */
    public final static String type_GO_KEGG  = "GO.kegg";
	
	/**
     *
     */
    public static String[] admittedTypes = {type_GO_KEGG,type_GO_PPI,type_GO_PFAM,type_GO_EC};
	
	// type_GO_PPI
	/**
     *
     */
    public static String knownRel 		 = "knownRel";
	/**
     *
     */
    public static String knownRelBase 	 = "knownRelBase";
	/**
     *
     */
    public static String positiveRel 	 = "positiveRel";
	/**
     *
     */
    public static String negativeRel 	 = "negativeRel";
	/**
     *
     */
    public static String setSize 		 = "set_size";
	/**
     *
     */
    public static String min_annot_size  = "min_annot";
	/**
     *
     */
    public static String taxon  = "taxon_id";
	/**
     *
     */
    public static String kegg_index 	  = "kegg_index";
	/**
     *
     */
    public static String pathway_clusters = "pathway_clusters";
	/**
     *
     */
    public static String index_out		  = "index_out";
	
	// type_GO_PFAM
	/**
     *
     */
    public static String pFamKb_index     = "pFamKB_index";
	/**
     *
     */
    public static String pFamClans        = "pFamClans";
	/**
     *
     */
    public static String out_pair_file    = "out_pair_file";
	/**
     *
     */
    public static String out_clan_prot    = "out_clan_prot";
	
	// type_GO_EC
	/**
     *
     */
    public static String ec2GO_mapping  = "ec2go_mapping";
	/**
     *
     */
    public static String random_number  = "random_number";
	/**
     *
     */
    public static String out_genes_ec   = "out_genes_ec";
	/**
     *
     */
    public static String out_genes_pair = "out_genes_pair";
	/**
     *
     */
    public static String out_genes_pair_ecsim   = "out_genes_pair_ecsim";
    

	
	
}
