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
 
 
package slib.tools.sml.sme.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.sme.discriminative_power.DiscriminativePowerComputer;
import slib.tools.sml.SmlModuleCLI;
import slib.tools.sml.sme.cli.utils.SmeCmdHandler;
import slib.tools.sml.sme.cli.utils.SmeCmdHandlerCst;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;

/**
 * TODO manage empty annotation
 * Specify in the documentation that empty annotation are excluded
 * 
 * 
 * @author seb
 *
 */
public class SmeCli implements SmlModuleCLI{

	Logger logger = LoggerFactory.getLogger(SmeCli.class);
	
	private SmeCmdHandler cfgLoader;


	public void execute(String[] argsModule) throws SGL_Exception {
		cfgLoader = new SmeCmdHandler(argsModule);
		execute();
	}

	public void execute() throws SGL_Ex_Critic{
		
		if(cfgLoader.process.equals(SmeCmdHandlerCst.processDP)){
			
			if(cfgLoader.clan_file == null)
				cfgLoader.ending("Please specify a Clan file",true);
			
			if(cfgLoader.sm_file == null)
				cfgLoader.ending("Please specify a file containing semantic measure results",true);
			
			if(cfgLoader.output == null)
				cfgLoader.ending("Please specify an output file",true);
			
			DiscriminativePowerComputer c = new DiscriminativePowerComputer();
			c.compute(cfgLoader.clan_file, cfgLoader.sm_file , cfgLoader.maxValue ,cfgLoader.output);
		}
		else
			cfgLoader.ending(SmeCmdHandlerCst.errorMissingProcess,true);
	}



}
