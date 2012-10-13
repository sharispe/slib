package slib.tools.smltoolkit.sml_server_deploy.cli;

import slib.tools.module.ModuleCst;
import slib.tools.smltoolkit.SmlToolKitCliCst;
import slib.tools.smltoolkit.SmlToolKitCst;
import slib.utils.ex.SLIB_Ex_Critic;

public class SmlDeployToolKitCst extends ModuleCst {

	
	
        public static final String   properties_prefix   = "sml-toolkit-deploy";
	
	
	public SmlDeployToolKitCst() throws SLIB_Ex_Critic {
		super(SmlToolKitCst.properties_file_name,properties_prefix);
	}
}