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
 
 
package slib.tools.smltoolkit;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import slib.tools.module.ToolCmdHandlerCst;

public class SmlToolKitCliCst extends ToolCmdHandlerCst{



	public static final String   appCmdName 	= "sml-toolkit-<version>.jar ";
	
	public static final String   ToolName_SM 	= "sm";
	public static final String   ToolName_SMBB 	= "smbb";
	public static final String   ToolName_SME 	= "sme";
	public static final String   ToolName_SMUTILS = "smutils";
	public static final String   ToolName_SML_DEPLOY = "sml_deploy";
	public static final String[] acceptedTools 	= {ToolName_SM,ToolName_SMBB,ToolName_SME,ToolName_SMUTILS,ToolName_SML_DEPLOY};
	
	
	
	public static final String  toolArg = "t";
	
	public static boolean debugMode  = false;
	
	
	
	public SmlToolKitCliCst() {
		super(appCmdName, debugMode, optionsOrder);
	}
	
	/*
	 * Error messages  
	 */
	
	public static final String errorTool = "[ERROR] Incorrect tool, supported are "+Arrays.toString(SmlToolKitCliCst.acceptedTools);
			
	/*
	 * Setting Options 
	 */
	
	public static Option help 		= new Option( "help", "print this message" );
	public static Option version 	= new Option( "version", "print the version in use" );
	

	@SuppressWarnings("static-access")
	public static Option tool		 = OptionBuilder.withArgName( "value")
	.hasArg()
	.withDescription( "Tool name "+Arrays.toString(SmlToolKitCliCst.acceptedTools))
	.create( toolArg );


	/*
	 * Use this data structure to define order of options in help message
	 */
	public final static HashMap<Option,Integer> optionsOrder = new HashMap<Option,Integer>();
	static
	{
		optionsOrder.put(tool, optionsOrder.size());
		optionsOrder.put(version, optionsOrder.size());
		optionsOrder.put(help, optionsOrder.size());
		
	 }
}
