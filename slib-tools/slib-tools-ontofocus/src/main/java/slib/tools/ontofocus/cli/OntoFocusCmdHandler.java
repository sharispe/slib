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
 
 
package slib.tools.ontofocus.cli;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.io.util.GFormat;
import slib.tools.module.CmdHandler;
import slib.tools.module.XMLAttributMapping;
import slib.tools.ontofocus.cli.utils.OntoFocusCmdHandlerCst;
import slib.tools.ontofocus.core.OntoFocus;
import slib.tools.ontofocus.core.utils.OntoFocusConf;
import slib.tools.ontofocus.core.utils.OntoFocusCst;
import slib.utils.ex.SLIB_Exception;


/**
 *
 * @author seb
 */
public class OntoFocusCmdHandler extends CmdHandler {


	/**
     *
     */
    public String  ontoFile  = null;
	/**
     *
     */
    public String  out		  = null;
	/**
     *
     */
    public String  queryFile = null;
	/**
     *
     */
    public String  rootURI	  = null;
	/**
     *
     */
    public String  incR	  = null;
	/**
     *
     */
    public boolean addR	  = false;
	
	/**
     *
     */
    public GFormat  format 	  = OntoFocusCmdHandlerCst.format_default;


	static Logger logger = LoggerFactory.getLogger(OntoFocusCmdHandler.class);


	/**
     *
     * @param args
     * @throws SLIB_Exception
     */
    public OntoFocusCmdHandler(String[] args) throws SLIB_Exception {
		
		super(new OntoFocusCst(),new OntoFocusCmdHandlerCst(),args);

		processArgs(args);
	}
	
	
	public void processArgs(String[] args) {
		CommandLineParser parser = new BasicParser();

		try {
			CommandLine line = parser.parse( options, args );

			if(line.hasOption("help")){
				ending(null,true);
			}
			else{

				if(line.hasOption("addR"))
					addR = true;


				//-- Ontology file
				if(line.hasOption("onto"))
					ontoFile = line.getOptionValue( "onto" );
				else
					ending(OntoFocusCmdHandlerCst.errorOntology,true);

				//-- Output file
				if(line.hasOption("out"))
					out = line.getOptionValue( "out" );

				//-- focus file
				if(line.hasOption("focus"))
					queryFile = line.getOptionValue( "focus" );
				else
					ending(OntoFocusCmdHandlerCst.errorFocus,true);

				//-- Root URI
				if(line.hasOption("root"))
					rootURI = line.getOptionValue( "root" );

				//-- incR
				if(line.hasOption("incR"))
					incR = line.getOptionValue( "incR" );
				
				//-- format
				if(line.hasOption("format")){
					String formatAsString = line.getOptionValue( "format" );
					format = XMLAttributMapping.GDataFormatMapping.get(formatAsString);
				}
			}

		}
		catch( ParseException exp ) {
			ending( OntoFocusCmdHandlerCst.appCmdName+" Parsing failed.  Reason: " + exp.getMessage(),true );
		}
	}

	private OntoFocusConf getLoadedConf() {
		return new OntoFocusConf(ontoFile,format,rootURI,incR,addR,out,queryFile);
	}

	/**
     *
     * @param args
     */
    public static void main(String[] args) {

		try {
			OntoFocusCmdHandler c = new OntoFocusCmdHandler(args);
			
			OntoFocus p = new OntoFocus();
			p.excecute(c.getLoadedConf());

		} catch (Exception e) {
			logger.info("Ooops: "+e.getMessage());
			e.printStackTrace();
			logger.info("see log file.");
		}
	}
}
