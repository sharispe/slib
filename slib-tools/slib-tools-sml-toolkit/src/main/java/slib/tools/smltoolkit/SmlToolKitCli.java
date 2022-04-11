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
import slib.tools.smltoolkit.sm.cli.core.SmCli;
import slib.utils.ex.SLIB_Exception;

/**
 * Semantic Measures Library Toolkit Command Line Interface
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 *
 */
public class SmlToolKitCli extends CmdHandler {

    public String tool = null;
    static Logger logger = LoggerFactory.getLogger(SmlToolKitCli.class);
    String[] argsTool;
    String[] argsGeneral;
    CommandLine argsGeneralCMD;

    /**
     * @throws SLIB_Exception
     */
    public SmlToolKitCli() throws SLIB_Exception {

        super(new SmlToolKitCst(), new SmlToolKitCliCst());
    }

    @Override
    public void processArgs(String[] args) throws SLIB_Exception {

        preProcessArgs(args);

        logger.info("Args " + Arrays.toString(argsGeneral));
        logger.info("Args Tool    " + Arrays.toString(argsTool));

        CommandLineParser parser = new BasicParser();

        try {
            argsGeneralCMD = parser.parse(options, argsGeneral);

            if (argsGeneral.length == 0 || argsGeneralCMD.hasOption("help")) {
                ending(null, true);
            } else if (argsGeneralCMD.hasOption("version")) {
                ending("version " + cst.version + " snapshot " + cst.versionSnapshot, false);
            } else {
                //-- tool name 
                if (argsGeneralCMD.hasOption(SmlToolKitCliCst.toolArg)) {
                    tool = argsGeneralCMD.getOptionValue(SmlToolKitCliCst.toolArg);
                } else {
                    ending(SmlToolKitCliCst.errorTool, true);
                }
            }

        } catch (ParseException exp) {
            ending("Error : " + SmlToolKitCliCst._appCmdName + " Parsing failed.  Trace: " + exp.getMessage(), true);
        }
        launch();
    }

    private void launch() throws SLIB_Exception {


        SmlModuleCLI cli = null;

        if (tool == null) {
            ending("", true);
        } else if (!Arrays.asList(SmlToolKitCliCst.acceptedTools).contains(tool)) {
            ending(SmlToolKitCliCst.errorTool, true);
        } else if (tool.equals(SmlToolKitCliCst.ToolName_SM)) {
            logger.info("Loading SM Tool");
            cli = new SmCli();
        }
//        else if (tool.equals(SmlToolKitCliCst.ToolName_SMBB)) {
//            cli = new SmbbCli();
//        } else if (tool.equals(SmlToolKitCliCst.ToolName_SME)) {
//            cli = new SmeCli();
//        } else if (tool.equals(SmlToolKitCliCst.ToolName_SMUTILS)) {
//            cli = new SmlUtilsCli();
//        } else if (tool.equals(SmlToolKitCliCst.ToolName_SML_DEPLOY)) {
//            cli = new SmlDeployCli();
//        }
        if (cli != null) {
            cli.execute(argsTool);
        }
    }

    /**
     * Method used to split arguments which are general from those which are
     * specific for a tool.
     *
     * @param args the array containing the all set of parameters.
     */
    private void preProcessArgs(String[] args) {

        ArrayList<String> argsModule_ = new ArrayList<String>();
        ArrayList<String> argsGeneral_ = new ArrayList<String>();

        boolean moduleArgs = false;
        boolean prefmoduleArgs = false;
        for (String arg : args) {
            if (moduleArgs) {
                argsModule_.add(arg);
            } else {
                argsGeneral_.add(arg);
                if (arg.equals("-" + SmlToolKitCliCst.toolArg)) {
                    prefmoduleArgs = true;
                } else if (prefmoduleArgs) {
                    moduleArgs = true;
                }
            }
        }

        String[] strArray = new String[argsModule_.size()];
        argsTool = argsModule_.toArray(strArray);

        String[] strArrayb = new String[argsGeneral_.size()];
        argsGeneral = argsGeneral_.toArray(strArrayb);
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

//        //DEBUG
//        String a = "-t sm -profile MESH "
//                + "-mesh /data/mesh/2014/desc2014.xml "
////                + "-queries /home/seb/Desktop/tmp/mesh/q.tmp "
////                + "-output /tmp/testMesH.tmp "
//////                + "-pm lin "
//////                + "-ic zhou "
////                + "-mtype g  "
////                + "-queries /home/seb/Desktop/tmp/mesh/q2.tmp "
////                + "-annots /home/seb/Desktop/tmp/mesh/annot.tmp "
////                + "-gm to ";
//
////                + " -t sm -xmlconf /data/tmp/testsml.xml ";
//                //                + " -t sm -xmlconf /tmp/sml-xmlconf.xml ";
//                //                +"-t sm "
//                //                + " -profile GO "
//                //                + " -quiet "
//                //                + " -mtype p "
//                //                + " -ic resnik "
//                //                + " -pm lin "
//                ////                + " -gm gic "
//                //                + " -go /data/go/eval/go_20130302-termdb.owl  "
//                //                + " -goformat OWL  "
//                //                + " -queries /data/go/eval/benchmark_1000_0.tsv"
//                //                + " -annots /data/go/eval/dump_orgHsegGO_sml.tsv"
//                //                + " -annotsFormat TSV "
//                //                + " -output /tmp/testsmltoolkitgo.tsv"
//                ////                + " -filter noEC=IEA:Taxon=9696,5454"
//                //                + " -noannots set=0"
//                //                + " -notfound stop"
//                ;
//
//        args = a.split("\\s+");

        try {
            SmlToolKitCli c = new SmlToolKitCli();
            c.processArgs(args);

        } catch (SLIB_Exception e) {
            logger.error("\n\n[Error] " + e.getMessage() + "\n");
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }

            System.exit(-1);
        }
    }
}
