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
package slib.tools.smltoolkit.sm.cli.profile.mesh;

import slib.tools.smltoolkit.sm.cli.profile.go.*;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.tools.module.CmdHandler;
import slib.tools.smltoolkit.sm.cli.core.utils.SML_SM_module_XML_block_conf;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe
 */
public class SmProfileMeSHHandler extends CmdHandler {

    SML_SM_module_XML_block_conf smconf;
    static Logger logger = LoggerFactory.getLogger(SmProfileGOHandler.class);

    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    public SmProfileMeSHHandler(String[] args) throws SLIB_Exception {
        super(new SmToolkitMeSHCst(), new SmProfileMeSHCst());
    }

    @Override
    public void processArgs(String[] args) {
        
        

        smconf = new SML_SM_module_XML_block_conf();

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            if (args == null || args.length == 0 || line.hasOption("help")) {
                String mess = null;
                if(args == null || args.length == 0){
                    mess = "No arguments...";
                }
                ending(mess, true, false, true);
            } else {
                
                
                if (line.hasOption("mesh")) {
                    smconf.setOntologyPath(line.getOptionValue("mesh"));
                }
                if (line.hasOption("annots")) {
                    smconf.setAnnotsPath(line.getOptionValue("annots"));
                }

                smconf.setAnnotsFormat(SmProfileMeSHCst.ANNOTSFORMAT_DEFAULT);

                if (line.hasOption("queries")) {
                    smconf.setQueries(line.getOptionValue("queries"));
                }
                if (line.hasOption("output")) {
                    smconf.setOutput(line.getOptionValue("output"));
                }
                if (line.hasOption("mtype")) {
                    smconf.setMtype(line.getOptionValue("mtype"));
                } else {
                    smconf.setMtype(SmProfileMeSHCst.MTYPE_DEFAULT);
                }

                if (line.hasOption("notfound")) {
                    smconf.setNotFound(line.getOptionValue("notfound"));
                } else {
                    smconf.setNotFound(SmProfileMeSHCst.NOTFOUND_DEFAULT);
                }
                if (line.hasOption("noannots")) {
                    smconf.setNoAnnots(line.getOptionValue("noannots"));
                } else {
                    smconf.setNoAnnots(SmProfileMeSHCst.NOANNOTS_DEFAULT);
                }

                if (line.hasOption("pm")) {
                    smconf.setPmShortFlag(line.getOptionValue("pm"));
                }
                if (line.hasOption("gm")) {
                    smconf.setGmShortFlag(line.getOptionValue("gm"));
                }
                if (line.hasOption("ic")) {
                    smconf.setIcShortFlag(line.getOptionValue("ic"));
                }
                if (line.hasOption("threads")) {
                    smconf.setThreads(line.getOptionValue("threads"));
                } else {
                    smconf.setThreads(SmProfileMeSHCst.THREADS_DEFAULT);
                }

                if (line.hasOption("notrmesh")) {
                    smconf.setNoTR_KR("true");
                }
                if (line.hasOption("notrannots")) {
                    smconf.setNoTR_Annots("true");
                }
                if (line.hasOption("quiet")) {
                    smconf.setQuiet("true");
                }

            }
        } catch (ParseException exp) {
            ending(cst.appName + " Parsing failed.  Reason: " + exp.getMessage(), true);
        }
    }

    public SML_SM_module_XML_block_conf getSmconf() {
        return smconf;
    }

}
