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
 
 
package slib.tools.ontofocus.cli.utils;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import slib.sglib.io.util.GFormat;
import slib.tools.module.ToolCmdHandlerCst;

public class OntoFocusCmdHandlerCst extends ToolCmdHandlerCst{


	public static final String   appCmdName 	  	= "OntoFocus.jar ";
	public static final GFormat   format_default  	= GFormat.OBO;
	public static final GFormat[] acceptedFormats 	= {GFormat.OBO, GFormat.OWL};
	
	public static boolean debugMode  = false;
	public static boolean addRelsVal = false;
	public static String  rootURI	 = null;
	
	public static String incR_Separator = ","; // separator used to defined multiple relationships to consider during the process
	
	/*
	 * Error messages  
	 */
	
	public static final String errorOntology = "[ERROR] Please specify an ontology, supported format are "+Arrays.toString(OntoFocusCmdHandlerCst.acceptedFormats);
	public static final String errorOutput   = "[ERROR] Please specify an output file";
	public static final String errorFocus    = "[ERROR] Please specify a file containing focus term/concept uri/id (one per line)";
			
	/*
	 * Setting Options 
	 */
	
	public static Option help 			= new Option( "help", "print this message" );
	public static Option addR 			= new Option( "addR", "add all directed relationships, only considering types explicitly defined by the ontology, to the final reduction\n"+
													  "(optional, default:"+addRelsVal+")" );
	
	@SuppressWarnings("static-access")
	public static Option ontology  = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "ontology file (required)" )
	.create( "onto" );
	
	@SuppressWarnings("static-access")
	public static Option root  = OptionBuilder.withArgName( "id/URI" )
	.hasArg()
	.withDescription( 	"id/uri of the concept/term to consider as the root of the graph\n"+
						"(optional, default: the entity with no outgoing IS-A/SubClassOf relationship,"+
						" if multiple choices generate an error)" )
	.create( "root" );
	
	@SuppressWarnings("static-access")
	public static Option incR  = OptionBuilder.withArgName( "id/URI" )
	.hasArg()
	.withDescription( "id/uri of other relationships to consider during topological sort and graph extension. "+
					  "Is-a (obo) or rdfs:SubClassOf (owl) always considered."+
					  " Useful to include other transitive relationship e.g part-of (obo) relationships."+
					  " Multiple relationship id/URI can be specified using '"+incR_Separator+"' separator \n"+
					  "(optional, default: Is-a (obo) or rdfs:SubClassOf (owl))" )
	.create( "incR" );


	@SuppressWarnings("static-access")
	public static Option out		 = OptionBuilder.withArgName( "file")
	.hasArg()
	.withDescription( "output file (optional, default: stdout)" )
	.create( "out" );


	@SuppressWarnings("static-access")
	public static Option entities	 = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "file containing id/uri of the concepts/terms to focus on, one per line (required)" )
	.create( "focus" );
	
	
	@SuppressWarnings("static-access")
	public static Option format 	 = OptionBuilder.withArgName( "value" )
	.hasArg()
	.withDescription( "format (optional, default:"+format_default+", values: "+Arrays.toString(acceptedFormats)+")" )
	.create( "format" );
	
	/*
	 * Use this data structure to define order of options in help message
	 */
	public final static HashMap<Option,Integer> optionsOrder = new HashMap<Option,Integer>();
	static
	{
		optionsOrder.put(ontology, optionsOrder.size());
		optionsOrder.put(entities, optionsOrder.size());
		optionsOrder.put(format, optionsOrder.size());
		optionsOrder.put(out, optionsOrder.size());
		optionsOrder.put(addR, optionsOrder.size());
		optionsOrder.put(root, optionsOrder.size());
		optionsOrder.put(incR, optionsOrder.size());
		
		optionsOrder.put(help, optionsOrder.size());
		
	 }


	public OntoFocusCmdHandlerCst() {
		
		super(appCmdName, debugMode, optionsOrder);
	}
}
