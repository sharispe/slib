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
 
 
package slib.tools.sml;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import slib.tools.module.ModuleCmdHandlerCst;

public class SmlCliCst extends ModuleCmdHandlerCst{



	public static final String   appCmdName 	  	= "sml.jar ";
	
	public static final String   ModuleName_SM 	  	= "sm";
	public static final String   ModuleName_SMBB 	= "smbb";
	public static final String   ModuleName_SME 	= "sme";
	public static final String   ModuleName_SMUTILS = "smutils";
	public static final String   ModuleName_SML_DEPLOY = "sml_deploy";
	public static final String[] acceptedModules 	= {ModuleName_SM,ModuleName_SMBB,ModuleName_SME,ModuleName_SMUTILS,ModuleName_SML_DEPLOY};
	
	
	
	public static final String  moduleArg = "module";
	
	public static boolean debugMode  = false;
	
	
	
	public SmlCliCst() {
		super(appCmdName, debugMode, optionsOrder);
	}
	
	/*
	 * Error messages  
	 */
	
	public static final String errorModule = "[ERROR] Incorrect module, supported are "+Arrays.toString(SmlCliCst.acceptedModules);
			
	/*
	 * Setting Options 
	 */
	
	public static Option help 		= new Option( "help", "print this message" );
	public static Option version 	= new Option( "version", "print the version in use" );
	

	@SuppressWarnings("static-access")
	public static Option module		 = OptionBuilder.withArgName( "value")
	.hasArg()
	.withDescription( "Module name "+Arrays.toString(SmlCliCst.acceptedModules))
	.create( moduleArg );


	/*
	 * Use this data structure to define order of options in help message
	 */
	public final static HashMap<Option,Integer> optionsOrder = new HashMap<Option,Integer>();
	static
	{
		optionsOrder.put(module, optionsOrder.size());
		optionsOrder.put(version, optionsOrder.size());
		optionsOrder.put(help, optionsOrder.size());
		
	 }
}
