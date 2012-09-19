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
 
 
package slib.sgli.test.algo.graph;


import org.openrdf.model.URI;

import slib.sglib.model.repo.impl.DataRepository;
import slib.utils.ex.SGL_Ex_Critic;


public class SGL_UnitTestValues {

	public static final String path 		= System.getProperty("user.dir")+"/src/test/resources/graph/";
	
	public static final String G_DAG_BASIC  = path + "sgl/g.sgl";
	public static final String G_GO 	    = path + "obo/gene_ontology_ext.obo";

	public static final URI uriGraph = DataRepository.getSingleton().createURI("http://g/");
	
	public URI G_BASIC_THING;
	public URI G_BASIC_ANIMAL;
	public URI G_BASIC_MEN;
	public URI G_BASIC_HUMAN;
	public URI G_BASIC_SPIDER;  
	public URI G_BASIC_SPIDERMAN; 
	public URI G_BASIC_OBJECT;  
	public URI G_BASIC_MAMMAL;  
	public URI G_BASIC_TOOLS;   

	public URI G_BASIC_ORGANISM;
	public URI G_BASIC_FICTIV_ORGANISM;
	public URI G_BASIC_SWISS_KNIFE;
	public URI G_BASIC_KNIFE;
	public URI G_BASIC_TABLE;
	public URI G_BASIC_REAL_ORGANISM;
	public URI G_BASIC_PLANT;
	public URI G_BASIC_WOMEN;
	public URI G_BASIC_TOMATO;
	
	public URI G_BASIC_STRANGE_ANIMAL;
	public URI G_BASIC_STRANGE_ANIMAL_2;
	
	public int G_DAG_BASIC_MAX_DEPTH 	= 8;
	
	DataRepository data = DataRepository.getSingleton();
	
	public SGL_UnitTestValues() throws SGL_Ex_Critic{
		
		G_BASIC_THING  	 		= data.createURI(uriGraph+"thing");
		G_BASIC_ANIMAL  		= data.createURI(uriGraph+"animal");
		G_BASIC_MEN	   			= data.createURI(uriGraph+"men");
		G_BASIC_HUMAN	   		= data.createURI(uriGraph+"human");
		G_BASIC_SPIDER  		= data.createURI(uriGraph+"spider");
		G_BASIC_SPIDERMAN  		= data.createURI(uriGraph+"spiderman");
		G_BASIC_OBJECT  		= data.createURI(uriGraph+"object");
		G_BASIC_MAMMAL  		= data.createURI(uriGraph+"mammal");
		G_BASIC_TOOLS   		= data.createURI(uriGraph+"tools");
		G_BASIC_TOMATO   		= data.createURI(uriGraph+"tomato");

		G_BASIC_ORGANISM 		= data.createURI(uriGraph+"organism");
		G_BASIC_FICTIV_ORGANISM = data.createURI(uriGraph+"fictiv_organism");
		G_BASIC_SWISS_KNIFE 	= data.createURI(uriGraph+"swiss_knife");
		G_BASIC_KNIFE 			= data.createURI(uriGraph+"knife");
		G_BASIC_TABLE 			= data.createURI(uriGraph+"table");
		
		G_BASIC_REAL_ORGANISM 	= data.createURI(uriGraph+"real_organism");
		G_BASIC_PLANT 			= data.createURI(uriGraph+"plant");
		G_BASIC_WOMEN 			= data.createURI(uriGraph+"women");
		
		G_BASIC_STRANGE_ANIMAL 		= data.createURI(uriGraph+"strange_animal");
		G_BASIC_STRANGE_ANIMAL_2 		= data.createURI(uriGraph+"strange_animal_2");
		
	}




	/*
	 * IC considered ICI_RESNIK_UNPROPAGATED_1995
	 */

	public static final double ic_test_mouse			= 0.6884571838304332;
	public static final double ic_test_carrot			= 1.0;
	public static final double ic_test_tomato			= 1.0;
	public static final double ic_test_fictiv_organism	= 0.8034383677671774;
	public static final double ic_test_spider			= 0.8034383677671774;
	public static final double ic_test_mushroom			= 1.0;
	public static final double ic_test_fish				= 0.6068767355343547;
	public static final double ic_test_domestic_mouse	= 1.0;
	public static final double ic_test_vegetable		= 0.6884571838304332;
	public static final double ic_test_wild_mouse		= 1.0;
	public static final double ic_test_women			= 1.0;
	public static final double ic_test_object			= 0.4481817342635088;
	public static final double ic_test_plant			= 0.5435980237416919;
	public static final double ic_test_child			= 1.0;
	public static final double ic_test_table			= 1.0;
	public static final double ic_test_knife			= 0.8034383677671774;
	public static final double ic_test_idea				= 1.0;
	public static final double ic_test_mammal			= 0.3470363915088692;
	public static final double ic_test_men		= 0.8034383677671774;
	public static final double ic_test_animal	= 0.21375347106870957;
	public static final double ic_test_thing	= 0.0;
	public static final double ic_test_hummer	= 1.0;
	public static final double ic_test_complex_tools	= 0.8034383677671774;
	public static final double ic_test_tools	= 0.5435980237416919;
	public static final double ic_test_human	= 0.4918955515976106;
	public static final double ic_test_adult	= 0.6068767355343547;
	public static final double ic_test_shark	= 1.0;
	public static final double ic_test_organism	= 0.08719604748338367;
	public static final double ic_test_clownfish	= 1.0;
	public static final double ic_test_swiss_knife	= 1.0;
	public static final double ic_test_real_organism	= 0.11084127841113375;
	public static final double ic_test_spiderman	= 1.0;
	public static final double ic_test_lionfish	= 1.0;
	public static final double ic_test_cactus	= 1.0;





}
