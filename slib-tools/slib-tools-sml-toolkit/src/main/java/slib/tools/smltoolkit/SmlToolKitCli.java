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



import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.tools.module.CmdHandler;
import slib.tools.smltoolkit.sm.cli.SmCli;
import slib.tools.smltoolkit.smbb.cli.SmbbCli;
import slib.tools.smltoolkit.sme.cli.SmeCli;
import slib.tools.smltoolkit.sml_server_deploy.cli.SmlDeployCli;
import slib.tools.smltoolkit.smutils.cli.SmlUtilsCli;
import slib.utils.ex.SLIB_Exception;

/**
 * Semantic Measures Library Command Line Interface 
 * 
 * @author Sebastien Harispe
 *
 */
public class SmlToolKitCli extends CmdHandler{


	public String  tool	  = null;

	static Logger logger = LoggerFactory.getLogger(SmlToolKitCli.class);

	String[] argsTool;
	String[] argsGeneral;
	CommandLine argsGeneralCMD;

	public SmlToolKitCli(String[] args) throws SLIB_Exception {

		super(new SmlToolKitCst() , new SmlToolKitCliCst(), args);
		


	}


	public void processArgs(String[] args) throws SLIB_Exception {

		preProcessArgs(args);

		logger.info("Args General "+Arrays.toString(argsGeneral));
		logger.info("Args Tool    "+Arrays.toString(argsTool));

		CommandLineParser parser = new BasicParser();

		try {
			argsGeneralCMD = parser.parse( options, argsGeneral );

			if(argsGeneralCMD.hasOption("help")){
				ending(null,true);
			}
			else if(argsGeneralCMD.hasOption("version")){
				ending("version "+cst.version,false);
			}
			else{
				//-- tool name 
				if(argsGeneralCMD.hasOption(SmlToolKitCliCst.toolArg))
					tool = argsGeneralCMD.getOptionValue( SmlToolKitCliCst.toolArg );
				else
					ending(SmlToolKitCliCst.errorTool,true);
			}

		}
		catch( ParseException exp ) {
			ending( "Error : "+SmlToolKitCliCst.appCmdName+" Parsing failed.  Trace: " + exp.getMessage(),true );
		}
		launch();
	}


	private void launch() throws SLIB_Exception {


		SmlModuleCLI cli = null;

		if(!Arrays.asList(SmlToolKitCliCst.acceptedTools).contains(tool))
			ending(SmlToolKitCliCst.errorTool,true);

		else if(tool.equals(SmlToolKitCliCst.ToolName_SM))
			cli = new SmCli();

		else if(tool.equals(SmlToolKitCliCst.ToolName_SMBB))
			cli = new SmbbCli();

		else if(tool.equals(SmlToolKitCliCst.ToolName_SME))
			cli = new SmeCli();

		else if(tool.equals(SmlToolKitCliCst.ToolName_SMUTILS))
			cli = new SmlUtilsCli();

		else if(tool.equals(SmlToolKitCliCst.ToolName_SML_DEPLOY))
			cli = new SmlDeployCli();


		cli.execute(argsTool);
	}

	/**
	 * Method used to split arguments framework and module arguments
	 * Populate 
	 * 	argsGeneral  arguments prefixing and including -module value
	 *  argsModule   arguments following  -module value
	 * 
	 * @param in
	 */
	private void preProcessArgs(String[] in){

		ArrayList<String> argsModule_  = new ArrayList<String>();
		ArrayList<String> argsGeneral_ = new ArrayList<String>();

		boolean moduleArgs = false;
		boolean prefmoduleArgs = false;

		for (int i = 0; i < in.length; i++) {

			if(moduleArgs)
				argsModule_.add(in[i]);
			else{
				argsGeneral_.add(in[i]);

				if(in[i].equals("-"+SmlToolKitCliCst.toolArg))
					prefmoduleArgs = true;
				else if(prefmoduleArgs)
					moduleArgs = true;
			}
		}

		String []strArray = new String[argsModule_.size()];
		argsTool = argsModule_.toArray(strArray);

		String []strArrayb = new String[argsGeneral_.size()];
		argsGeneral = argsGeneral_.toArray(strArrayb);
	}

	@SuppressWarnings("unused")
	public static void main(String[] args) {

		try {
			SmlToolKitCli c = new SmlToolKitCli(args);

		} catch (Exception e) {
			logger.error("\n\n[Error] "+e.getMessage()+"\n");
			if(logger.isDebugEnabled())
				e.printStackTrace();

			System.exit(-1);
		}
	}
}
