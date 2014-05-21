/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.tools.smltoolkit.sm.cli.core.cmd;

import java.util.Arrays;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.tools.module.CmdHandler;
import slib.tools.smltoolkit.sm.cli.core.utils.SmToolkitCst;
import slib.tools.smltoolkit.sm.cli.profile.mesh.SmProfile_MeSH;
import slib.tools.smltoolkit.sm.cli.profile.go.SmProfile_GO;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SmCmdHandler extends CmdHandler {

    /**
     *
     */
    public String xmlConfFile;
    public String xmlConfAsString;
    public String profile;
    static Logger logger = LoggerFactory.getLogger(SmCmdHandler.class);

    /**
     *
     * @throws SLIB_Exception
     */
    public SmCmdHandler() throws SLIB_Exception {
        
        super(new SmToolkitCst(), new SmCmdHandlerCst());
    }

    @Override
    public void processArgs(String[] args) throws SLIB_Exception {

        CommandLineParser parser = new BasicParser();
        
        //We only want the first two element of the array
        // from -profile GO -go /data/go/gene_ontology_ext.obo
        // we want -profile G0 to be passed to the SM module which will decide which profile to use if any asked
        // The rest of the parameters must be passed to the profile module

        String[] argSMmodule;
        String[] argSMProfile = null;
        if(args.length < 2){
            argSMmodule = args;
        }
        else{
            argSMmodule = new String[2];
            argSMmodule[0] = args[0];
            argSMmodule[1] = args[1];
        }
        if(args.length -2 > 0){
            // other arguments for profiles 
            argSMProfile = new String[args.length -2];
            
            for(int i = 2; i < args.length; i++){
                argSMProfile[i-2] = args[i];
            }
        }
        
        logger.debug("Global args  "+Arrays.toString(args));
        logger.debug("Module SM    "+Arrays.toString(argSMmodule));
        logger.debug("Profile args "+Arrays.toString(argSMProfile));

        try {
            CommandLine line = parser.parse(options, argSMmodule);
            
            if (argSMmodule.length == 0 || line.hasOption("help")) {
                ending(null, true);
            } else {
                if (line.hasOption("xmlconf")) {
                    
                    xmlConfFile = line.getOptionValue("xmlconf");
                
                } else if (line.hasOption("profile")) {
                    
                    profile = line.getOptionValue("profile");
                    logger.info("Process profile: "+profile);
                    
                    if(profile.toUpperCase().equals("GO")){
                        SmProfile_GO goprofile = new SmProfile_GO();
                        // We generate the XML configuration file
                        goprofile.execute(argSMProfile);
                        xmlConfAsString = goprofile.xmlconf;
                    }
                    else if(profile.toUpperCase().equals("MESH")){
                        logger.info("Loading MeSH config loader");
                        SmProfile_MeSH meshProfile = new SmProfile_MeSH();
                        // We generate the XML configuration file
                        meshProfile.execute(argSMProfile);
                        xmlConfAsString = meshProfile.xmlconf;
                    }
                    else{
                        throw new SLIB_Exception("Unsupported profile, admitted "+Arrays.toString(SmCmdHandlerCst.admittedProfiles));
                    }
                    
                } else {
                    ending(SmCmdHandlerCst.errorMissingXMLconfOrProfile, true);
                }
                
            }
        } catch (ParseException exp) {
            ending(cst.appName + " Parsing failed.  Reason: " + exp.getMessage(), true);
        }
    }
}
