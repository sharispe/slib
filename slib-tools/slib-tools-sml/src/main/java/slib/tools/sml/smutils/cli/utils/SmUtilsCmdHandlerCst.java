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
 
 
package slib.tools.sml.smutils.cli.utils;

import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;

import slib.sml.smutils.ResultsMerger;
import slib.sml.smutils.SQLiteUtils;
import slib.tools.module.ModuleCmdHandlerCst;
import slib.tools.sml.SmlCliCst;

public class SmUtilsCmdHandlerCst extends ModuleCmdHandlerCst{

	

	public static final String   moduleName 	  	= SmlCliCst.ModuleName_SMUTILS;
	public static final String   appCmdName 	  	= SmlCliCst.appCmdName+" -module "+moduleName;
	public static boolean 		 debugMode  = false;
	
	
	
	public static final String   process_resultMerger = "result_merger";
	public static final String   process_sqlLiteUtils = "SQLiteUtils";
	
	public static final String  sqlLiteUtils_create = "create";
	public static final String  sqlLiteUtils_drop   = "drop";
	public static final String  sqlLiteUtils_dropColumns = "dropColumns";
	public static final String  sqlLiteUtils_merge  = "merge";
	public static final String  sqlLiteUtils_flush  = "flush";
	public static final String  sqlLiteUtils_rename = "rename";
	public static final String  sqlLiteUtils_info   = "info";
	public static final String  sqlLiteUtils_copy   = "copy";
	
	
	
	
	public static String cmd_examples = 
			"Command line examples \n" +
			
			process_sqlLiteUtils+"\n" +
			
			"Here we consider that sml_cmd is equivalent to java -jar sml.jar -process "+process_sqlLiteUtils+"\n\n" +

					"\n- Create a database table from a result file \n" +
					"\t sml_cmd -action "+sqlLiteUtils_create+" -file_A result_file -db_A db_name -table_A table_name" +
					
					"\n- Flush (Export) a database table into a result file \n" +
					"\t sml_cmd -action "+sqlLiteUtils_flush+" -db_A db_name -table_A table_name -file_A output_result_file " +


					"\n\n- Get information about a database \n" +
					"\t sml_cmd -action "+sqlLiteUtils_info+" -db_A db_name "+
					
					"\n\n- Remove columns from a table (see get info to know column names)\n" +
					"\t sml_cmd -action "+sqlLiteUtils_dropColumns+" -db_A db_name -table_A table_name -colsToremove col1,col2"+
					
					"\n\n- Remove table from a database (see get info to know table names)\n" +
					"\t sml_cmd -action "+sqlLiteUtils_drop+" -db_A db_name -table_A table_name"+
					
					"\n\n- Copy a table\n" +
					"-- into the same database \n"+
					"\t sml_cmd -action "+sqlLiteUtils_copy+" -db_A db_name -table_A table_name -table_B copy_table_name"+
					"\n-- into another database \n"+
					"\t sml_cmd -action "+sqlLiteUtils_copy+" -db_A db_name -table_A table_name -db_B db_target_name -table_B copy_table_name"+
					
					"\n\n- Merge two tables\n" +
					"-- of the same database into the original database \n"+
					"\t sml_cmd -action "+sqlLiteUtils_merge+" -db_A db_name -table_A table_name -table_B tableB_name -table_M tableM_name"+
					
					"\n-- of the same database into another database \n"+
					"\t sml_cmd -action "+sqlLiteUtils_merge+" -db_A db_name -table_A table_name -table_B tableB_name -db_M merge_db_name -table_M tableM_name"+

					"\n-- of the different database\n"+
					"\t sml_cmd -action "+sqlLiteUtils_merge+" -db_A db_name -table_A table_name -db_B db_nameB -table_B tableB_name -table_M tableM_name"

					;
	
	
	
	
	public static final String[] acceptedProcesses 	= {process_resultMerger,process_sqlLiteUtils};
	public static final String[] acceptedActionSQLite  = {sqlLiteUtils_copy,sqlLiteUtils_create,sqlLiteUtils_drop,sqlLiteUtils_dropColumns,sqlLiteUtils_merge,sqlLiteUtils_flush,sqlLiteUtils_rename,sqlLiteUtils_info};
	/*
	 * Error messages  
	 */
	
	public static final String errorMissingProcess = "[ERROR] Please specify a process to perform, available "+Arrays.toString(acceptedProcesses);
	
	/*
	 * Setting Options 
	 */
	
	public static Option help 			= new Option( "help", "print this message" );
	public static Option examples 		= new Option( "ex", "print command line examples" );
	
	@SuppressWarnings("static-access")
	public static Option process  = OptionBuilder.withArgName( "process" )
	.hasArg()
	.withDescription( "process to perform (required) "+Arrays.toString(acceptedProcesses) )
	.create( "process" );
	
	
	
	/*
	 * sqliteUtils
	 */

	@SuppressWarnings("static-access")
	public static Option action  = OptionBuilder.withArgName( "action" )
	.hasArg()
	.withDescription( "action to perform (required for "+process_sqlLiteUtils+") "+Arrays.toString(acceptedActionSQLite) )
	.create( "action" );
	
	@SuppressWarnings("static-access")
	public static Option db_A  = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "database A (required for "+process_sqlLiteUtils+")" )
	.create( "db_A" );
	
	@SuppressWarnings("static-access")
	public static Option db_B  = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "database B (optional for "+process_sqlLiteUtils+" action "+sqlLiteUtils_merge+")" )
	.create( "db_B" );
	
	@SuppressWarnings("static-access")
	public static Option db_M  = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "database M merge (optional for "+process_sqlLiteUtils+" action "+sqlLiteUtils_merge+")" )
	.create( "db_M" );
	
	@SuppressWarnings("static-access")
	public static Option table_A  = OptionBuilder.withArgName( "table name" )
	.hasArg()
	.withDescription( "table A (required for "+process_sqlLiteUtils+")" )
	.create( "table_A" );
	
	@SuppressWarnings("static-access")
	public static Option table_B  = OptionBuilder.withArgName( "table name " )
	.hasArg()
	.withDescription( "table B (required for "+process_sqlLiteUtils+" action "+sqlLiteUtils_merge+")")
	.create( "table_B" );
	
	@SuppressWarnings("static-access")
	public static Option table_M  = OptionBuilder.withArgName( "table name" )
	.hasArg()
	.withDescription( "table M merge (required for "+process_sqlLiteUtils+" action "+sqlLiteUtils_merge+")" )
	.create( "table_M" );
	
	@SuppressWarnings("static-access")
	public static Option colsToremove  = OptionBuilder.withArgName( "column names" )
	.hasArg()
	.withDescription( "columns to remove separated by comma (required for "+process_sqlLiteUtils+" action "+sqlLiteUtils_dropColumns+")" )
	.create( "colsToremove" );
	
	
	@SuppressWarnings("static-access")
	public static Option BATCH_LIMIT  = OptionBuilder.withArgName( "integer" )
	.hasArg()
	.withDescription( "BATCH_LIMIT defines the number of queries to be loaded " +
			"in memory before database execution. Allowing an high can boost performance allocating " +
			"more memory to the processed performed (optional for "+process_sqlLiteUtils+" " +
			"actions "+sqlLiteUtils_copy+","+sqlLiteUtils_create+","+sqlLiteUtils_merge+","+sqlLiteUtils_dropColumns+")" +
			" default "+SQLiteUtils.BATCH_LIMIT )
	.create( "BATCH_LIMIT" );
	
	@SuppressWarnings("static-access")
	public static Option BATCH_LIMIT_MATRIX_LINE  
	= OptionBuilder.withArgName( "integer" )
	.hasArg()
	.withDescription( "BATCH_LIMIT_MATRIX_LINE defines the number of database rows to be loaded " +
			"in memory before flushing. Allowing an high can boost performance allocating " +
			"more memory to the processed performed (optional for "+process_sqlLiteUtils+
			" action "+sqlLiteUtils_flush+") default "+SQLiteUtils.BATCH_LIMIT_MATRIX_LINE )
	.create( "BATCH_LIMIT_MATRIX_LINE" );
	
	
	/*
	 * result Merger
	 */

	@SuppressWarnings("static-access")
	public static Option file_A  = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "semantic measures result file A (required for "+process_resultMerger+" and "+process_sqlLiteUtils+" actions "+sqlLiteUtils_create+", "+sqlLiteUtils_flush+")" )
	.create( "file_A" );
	
	@SuppressWarnings("static-access")
	public static Option file_B  = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "semantic measures result file B (required for "+process_resultMerger+")" )
	.create( "file_B" );
	
	@SuppressWarnings("static-access")
	public static Option output  = OptionBuilder.withArgName( "file" )
	.hasArg()
	.withDescription( "output file (required for "+process_resultMerger+")" )
	.create( "output" );
	
	@SuppressWarnings("static-access")
	public static Option tmp_dir  = OptionBuilder.withArgName( "directory" )
	.hasArg()
	.withDescription( "temporary directory. Specify an existing directory path if you want to process large files. Input files will be splitted in numerous tiny files in the specified directory ("+process_resultMerger+" optional)" )
	.create( "tmp_dir" );
	
	@SuppressWarnings("static-access")
	public static Option split_size  = OptionBuilder.withArgName( "integer" )
	.hasArg()
	.withDescription( "Integer value. Define Number of line contained by splitted file ("+process_resultMerger+" optional, default "+ResultsMerger.splitSize_default+" )" )
	.create( "split_size" );
	
	
	

	
	/*
	 * Use this data structure to define order of options in help message
	 */
	public final static HashMap<Option,Integer> optionsOrder = new HashMap<Option,Integer>();
	static
	{
		
		optionsOrder.put(examples, optionsOrder.size());
		
		optionsOrder.put(process, optionsOrder.size());
		optionsOrder.put(file_A, optionsOrder.size());
		optionsOrder.put(file_B, optionsOrder.size());
		optionsOrder.put(output, optionsOrder.size());
		optionsOrder.put(tmp_dir, optionsOrder.size());
		optionsOrder.put(split_size, optionsOrder.size());
		
		optionsOrder.put(action, optionsOrder.size());
		optionsOrder.put(db_A, optionsOrder.size());
		optionsOrder.put(db_B, optionsOrder.size());
		optionsOrder.put(db_M, optionsOrder.size());
		optionsOrder.put(table_A, optionsOrder.size());
		optionsOrder.put(table_B, optionsOrder.size());
		optionsOrder.put(table_M, optionsOrder.size());
		optionsOrder.put(colsToremove, optionsOrder.size());
		optionsOrder.put(BATCH_LIMIT, optionsOrder.size());
		optionsOrder.put(BATCH_LIMIT_MATRIX_LINE, optionsOrder.size());
		
		optionsOrder.put(help, optionsOrder.size());
	 }

	
	
	public SmUtilsCmdHandlerCst() {
		super(appCmdName, debugMode, optionsOrder);
	}
}
