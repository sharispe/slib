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
package slib.tools.smltoolkit.sm.cli.profile.go;

import java.util.Arrays;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.tools.module.CmdHandler;
import slib.tools.smltoolkit.sm.cli.utils.SmToolkitCst;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe
 */
public class SmProfileGOHandler extends CmdHandler {

    public String ontologyPath;
    public String annotsPath;
    public String annotsFormat;
    public String outputFile;
    public String queries;
    public String mType;
    public String aspect;
    public String notfound;
    public String noannots;
    public String filter;
    public String pm;
    public String gm;
    public String ic;
    public String threads;
    public boolean notrgo = false;
    public boolean notrannots = false;
    public boolean quiet = false;
    static Logger logger = LoggerFactory.getLogger(SmProfileGOHandler.class);

    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    public SmProfileGOHandler(String[] args) throws SLIB_Exception {
        super(new SmToolkitCst(), new SmProfileGOCst(), args);
    }

    @Override
    public void processArgs(String[] args) {

        CommandLineParser parser = new BasicParser();

        try {
            CommandLine line = parser.parse(options, args);

            logger.debug("GO profile args : " + Arrays.toString(args));
            if (line.hasOption("help")) {
                ending("", true);
            } else {
                if (line.hasOption("go")) {
                    ontologyPath = line.getOptionValue("go");
                }
                if (line.hasOption("annots")) {
                    annotsPath = line.getOptionValue("annots");
                } 
                if (line.hasOption("annotsformat")) {
                    annotsFormat = line.getOptionValue("annotsformat");
                }
                if (line.hasOption("queries")) {
                    queries = line.getOptionValue("queries");
                }
                if (line.hasOption("output")) {
                    outputFile = line.getOptionValue("output");
                }
                if (line.hasOption("mtype")) {
                    mType = line.getOptionValue("mtype");
                }
                if (line.hasOption("aspect")) {
                    aspect = line.getOptionValue("aspect");
                }
                if (line.hasOption("notfound")) {
                    notfound = line.getOptionValue("notfound");
                }
                if (line.hasOption("noannots")) {
                    noannots = line.getOptionValue("noannots");
                }
                if (line.hasOption("filter")) {
                    filter = line.getOptionValue("filter");
                }
                if (line.hasOption("pm")) {
                    pm = line.getOptionValue("pm");
                }
                if (line.hasOption("gm")) {
                    gm = line.getOptionValue("gm");
                }
                if (line.hasOption("ic")) {
                    ic = line.getOptionValue("ic");
                }
                if (line.hasOption("threads")) {
                    threads = line.getOptionValue("threads");
                }
                else{
                    threads = "1";
                }
                
                if (line.hasOption("notrgo")) {
                    notrgo = true;
                }
                if (line.hasOption("notrannots")) {
                    notrannots = true;
                }
                if (line.hasOption("quiet")) {
                    quiet = true;
                }

            }
        } catch (ParseException exp) {
            ending(cst.appName + " Parsing failed.  Reason: " + exp.getMessage(), true);
        }
    }

    public static void main(String[] args) throws SLIB_Exception {

        args = "-t sm -profile GO -go /data/go/gene_ontology_ext.obo ".split(" ");
        System.out.println(Arrays.toString(args));
        SmProfileGOHandler sm = new SmProfileGOHandler(args);


    }
}
