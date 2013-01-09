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
 
 
package slib.tools.smltoolkit.smutils.cli.utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.tools.module.CmdHandler;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;


/**
 *
 * @author seb
 */
public class SmUtilsCmdHandler extends CmdHandler {




	// result_merger
	/**
     *
     */
    public String  process;
	/**
     *
     */
    public String  file_A;
	/**
     *
     */
    public String  file_B;
	/**
     *
     */
    public String  output;
	/**
     *
     */
    public String  tmp_dir;
	/**
     *
     */
    public Integer split_size;

	// sqliteUtils

	/**
     *
     */
    public String  action;
	/**
     *
     */
    public String  db_A;
	/**
     *
     */
    public String  db_B;
	/**
     *
     */
    public String  db_M;
	/**
     *
     */
    public String  table_A;
	/**
     *
     */
    public String  table_B;
	/**
     *
     */
    public String  table_M;
	/**
     *
     */
    public Integer BATCH_LIMIT;
	/**
     *
     */
    public Integer BATCH_LIMIT_MATRIX_LINE;
	/**
     *
     */
    public Set<String> colsToremove;


	static Logger logger = LoggerFactory.getLogger(SmUtilsCmdHandler.class);


	/**
     *
     * @param args
     * @throws SLIB_Exception
     */
    public SmUtilsCmdHandler(String[] args) throws SLIB_Exception {
		super(new SmUtilsToolKitCst(), new SmUtilsCmdHandlerCst(), args);
	}


	public void processArgs(String[] args) throws SLIB_Exception {

		CommandLineParser parser = new BasicParser();

		try {
			CommandLine cmd = parser.parse( options, args );
			
			if(cmd.hasOption("help")){
				ending(null,true);
			}
			else if(cmd.hasOption("ex")){
				showCmdLineExamples();
			}
			else{

				if(cmd.hasOption("process")){
					this.process = cmd.getOptionValue( "process" );
				}
				else
					ending(SmUtilsCmdHandlerCst.errorMissingProcess,true);

				if(cmd.hasOption("action"))
					this.action = cmd.getOptionValue( "action" );

				if(cmd.hasOption("db_A"))
					this.db_A = cmd.getOptionValue( "db_A" );

				if(cmd.hasOption("db_B"))
					this.db_B = cmd.getOptionValue( "db_B" );

				if(cmd.hasOption("db_M"))
					this.db_M = cmd.getOptionValue( "db_M" );

				if(cmd.hasOption("table_A"))
					this.table_A = cmd.getOptionValue( "table_A" );

				if(cmd.hasOption("table_B"))
					this.table_B = cmd.getOptionValue( "table_B" );

				if(cmd.hasOption("table_M"))
					this.table_M = cmd.getOptionValue( "table_M" );
				
				if(cmd.hasOption("colsToremove")){
					String colsToremoveString = cmd.getOptionValue( "colsToremove" );
					this.colsToremove = new HashSet<String>(Arrays.asList(colsToremoveString.split(",")));
				}
				if(cmd.hasOption("BATCH_LIMIT")){
					try{
						this.BATCH_LIMIT = Integer.parseInt(cmd.getOptionValue( "BATCH_LIMIT" ));
					}
					catch(NumberFormatException e){
						throw new SLIB_Ex_Critic("Cannot convert BATCH_LIMIT to integer");
					}
				}

				if(cmd.hasOption("BATCH_LIMIT_MATRIX_LINE")){
					try{
						this.BATCH_LIMIT_MATRIX_LINE = Integer.parseInt(cmd.getOptionValue( "BATCH_LIMIT_MATRIX_LINE" ));
					}
					catch(NumberFormatException e){
						throw new SLIB_Ex_Critic("Cannot convert BATCH_LIMIT_MATRIX_LINE to integer");
					}
				}

				if(cmd.hasOption("file_A"))
					this.file_A = cmd.getOptionValue( "file_A" );

				if(cmd.hasOption("file_B"))
					this.file_B = cmd.getOptionValue( "file_B" );

				if(cmd.hasOption("output"))
					this.output = cmd.getOptionValue( "output" );

				if(cmd.hasOption("tmp_dir"))
					this.tmp_dir = cmd.getOptionValue( "tmp_dir" );

				if(cmd.hasOption("split_size")){
					String maxValString = cmd.getOptionValue( "split_size" );
					try{
						split_size = Integer.parseInt(maxValString);
					}
					catch(NumberFormatException e){
						ending("Please specify an integer value for field split_size (current: "+maxValString+")", true);
					}
				}
			}

		}
		catch( ParseException exp ) {
			ending( cst.appName+" Parsing failed.  Reason: " + exp.getMessage(),true );
		}
	}
	
	@Override
	public void showCmdLineExamples() throws SLIB_Exception {
		System.out.println(SmUtilsCmdHandlerCst.cmd_examples);
		ending(null,false,false,false);
	}
	
	

	/**
     *
     * @param args
     * @throws SLIB_Exception
     */
    @SuppressWarnings("unused")
	public static void main(String[] args) throws SLIB_Exception {
		SmUtilsCmdHandler handler = new SmUtilsCmdHandler(null);
	}




}
