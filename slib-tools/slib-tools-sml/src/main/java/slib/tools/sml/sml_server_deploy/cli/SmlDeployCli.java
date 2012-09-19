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


package slib.tools.sml.sml_server_deploy.cli;

import java.util.Arrays;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.sml_server_deploy.core.CmdExecutor;
import slib.sml.sml_server_deploy.core.SmlDeployXMLLoader;
import slib.tools.sml.SmlModuleCLI;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;


public class SmlDeployCli implements SmlModuleCLI{

	Logger logger = LoggerFactory.getLogger(SmlDeployCli.class);




	public void execute(String[] args) throws SGL_Exception {
		SmlDeployCmdHandler  c = new SmlDeployCmdHandler(args);

		SmlDeployXMLLoader loader = new SmlDeployXMLLoader();

		try {

			CmdExecutor cmdExecutor = loader.loadConf(c.xmlConfFile);

			if(c.benchmark_restrictions != null)
				cmdExecutor.setBenchmarkRestrictions(new LinkedList<String>(Arrays.asList(c.benchmark_restrictions)));

			if(c.profile_restrictions != null)
				cmdExecutor.setProfileRestrictions(new LinkedList<String>(Arrays.asList(c.profile_restrictions)));

			cmdExecutor.runAllBenchmarks();
		} catch (Exception e) {
			throw new SGL_Ex_Critic(e);
		}
	}

}
