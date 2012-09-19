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
 
 
package slib.tools.sml.smutils.cli;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.smutils.ResultsMerger;
import slib.sml.smutils.SQLiteUtils;
import slib.tools.sml.SmlModuleCLI;
import slib.tools.sml.smutils.cli.utils.SmUtilsCmdHandler;
import slib.tools.sml.smutils.cli.utils.SmUtilsCmdHandlerCst;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;

/**
 * TODO manage empty annotation
 * Specify in the documentation that empty annotation are excluded
 * 
 * 
 * 
 * @author harispe sébastien
 *
 */
public class SmlUtilsCli implements SmlModuleCLI{

	Logger logger = LoggerFactory.getLogger(SmlUtilsCli.class);
	
	private SmUtilsCmdHandler cfgLoader;
	
	public void execute(String[] argsModule) throws SGL_Exception {
		cfgLoader = new SmUtilsCmdHandler(argsModule);
		execute();
	}


	public void execute() throws SGL_Ex_Critic{
		
		if(cfgLoader.process.equals(SmUtilsCmdHandlerCst.process_resultMerger))
			process_resultMerger();
		
		else if(cfgLoader.process.equals(SmUtilsCmdHandlerCst.process_sqlLiteUtils))
			process_sqlLiteUtils();
		
		else
			cfgLoader.ending(SmUtilsCmdHandlerCst.errorMissingProcess,true);
	}
	
	private void process_sqlLiteUtils() throws SGL_Ex_Critic{
		
		SQLiteUtils utils = new SQLiteUtils();
		
		if(cfgLoader.BATCH_LIMIT != null)
			utils.setBATCH_LIMIT(cfgLoader.BATCH_LIMIT);
		
		if(cfgLoader.BATCH_LIMIT_MATRIX_LINE != null)
			utils.setBATCH_LIMIT_MATRIX_LINE(cfgLoader.BATCH_LIMIT_MATRIX_LINE);
			
		
		if(cfgLoader.action == null)
			cfgLoader.ending("Please specify an action, admitted "+Arrays.toString(SmUtilsCmdHandlerCst.acceptedActionSQLite),true);
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_create )){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			if(cfgLoader.table_A == null)
				cfgLoader.ending("Please specify a table A ",true);
			
			if(cfgLoader.file_A == null)
				cfgLoader.ending("Please specify a file A ",true);
			
			utils.createTableDB(cfgLoader.file_A, cfgLoader.db_A, cfgLoader.table_A);
			
		}
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_merge )){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			if(cfgLoader.db_B == null)
				cfgLoader.db_B = cfgLoader.db_A;
			
			if(cfgLoader.db_M == null)
				cfgLoader.db_M = cfgLoader.db_A;
			
			if(cfgLoader.table_A == null)
				cfgLoader.ending("Please specify a table A ",true);
			
			if(cfgLoader.table_B == null)
				cfgLoader.ending("Please specify a table B ",true);
			
			if(cfgLoader.table_M == null)
				cfgLoader.ending("Please specify a table M ",true);
			
			
			utils.mergeTables(cfgLoader.db_A, cfgLoader.table_A,cfgLoader.db_B, cfgLoader.table_B,cfgLoader.db_M, cfgLoader.table_M);
			
		}
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_flush )){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			if(cfgLoader.table_A == null)
				cfgLoader.ending("Please specify a table A ",true);
			
			if(cfgLoader.output == null)
				cfgLoader.ending("Please specify an output ",true);
			
			
			utils.flushTableInFile(cfgLoader.db_A, cfgLoader.table_A, cfgLoader.output);
		}
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_drop)){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			if(cfgLoader.table_A == null)
				cfgLoader.ending("Please specify a table A ",true);
			
			utils.dropTable(cfgLoader.db_A, cfgLoader.table_A);
		}
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_rename)){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			if(cfgLoader.table_A == null)
				cfgLoader.ending("Please specify a table A ",true);
			
			if(cfgLoader.table_B == null)
				cfgLoader.ending("Please specify a table B ",true);
			
			utils.renameTable(cfgLoader.db_A, cfgLoader.table_A, cfgLoader.table_B);
		}
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_info)){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			utils.getInfo(cfgLoader.db_A, cfgLoader.table_A);
		}
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_copy)){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			if(cfgLoader.db_B == null)
				cfgLoader.db_B = cfgLoader.db_A;
			
			if(cfgLoader.table_A == null)
				cfgLoader.ending("Please specify a table A ",true);
			
			if(cfgLoader.table_B == null)
				utils.copyTable(cfgLoader.db_A, cfgLoader.table_A, cfgLoader.db_B,cfgLoader.table_A);
			else
				utils.copyTable(cfgLoader.db_A, cfgLoader.table_A, cfgLoader.db_B,cfgLoader.table_B);
		}
		
		else if(cfgLoader.action.equals( SmUtilsCmdHandlerCst.sqlLiteUtils_dropColumns)){
			
			if(cfgLoader.db_A == null)
				cfgLoader.ending("Please specify a database A ",true);
			
			if(cfgLoader.table_A == null)
				cfgLoader.ending("Please specify a table A ",true);
			

			if(cfgLoader.colsToremove == null)
				cfgLoader.ending("Please specify columns to remove ",true);
			
			
			utils.dropColumns(cfgLoader.db_A, cfgLoader.table_A,cfgLoader.colsToremove);
			
		}
		
		else
			throw new SGL_Ex_Critic("Unsupported operation, see "+Arrays.toString(SmUtilsCmdHandlerCst.acceptedActionSQLite));
	}
	
	private void process_resultMerger() throws SGL_Ex_Critic{
		if(cfgLoader.file_A == null)
			cfgLoader.ending("Please specify a file A ",true);
		
		if(cfgLoader.file_B == null)
			cfgLoader.ending("Please specify a file B ",true);
		
		if(cfgLoader.output == null)
			cfgLoader.ending("Please specify an output file",true);
		
		
		ResultsMerger merger = new ResultsMerger();
		
		if(cfgLoader.tmp_dir == null){
			
			try {
				merger.process(cfgLoader.file_A,cfgLoader.file_B,cfgLoader.output);
			} 
			catch (OutOfMemoryError e) {
				throw new SGL_Ex_Critic(" Out Of MemoryError, processed file are too large to be processed in memory. Please allocate more memory, use tmp_dir option or use "+SmUtilsCmdHandlerCst.process_sqlLiteUtils+"(recommended)");
			}
		}
		else{
			try {
				merger.processLarge(cfgLoader.file_A,cfgLoader.file_B,cfgLoader.output,cfgLoader.tmp_dir,cfgLoader.split_size);
			} 
			catch (OutOfMemoryError e) {
				throw new SGL_Ex_Critic(" Out Of MemoryError, processed file are too large to be processed. Please allocate more memory, use a tiny split_size parameter or use "+SmUtilsCmdHandlerCst.process_sqlLiteUtils+"(recommended)");
			}
		}
	}



}
