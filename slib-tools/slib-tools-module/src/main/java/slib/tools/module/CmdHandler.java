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
package slib.tools.module;

import java.util.Comparator;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Abstract command handler
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 *
 */
public abstract class CmdHandler {

    public ModuleCst cst;
    public ToolCmdHandlerCst cstCmd;
    public Options options;
    public HelpFormatter helpFormatter;
    private String USAGE;
    private final String HEADER = "----------------------------------------------------------------------";
    private final String FOOTER = HEADER;
    static Logger logger = LoggerFactory.getLogger(CmdHandler.class);
    Comparator<Option> comparator;

    /**
     *
     * @param cst
     * @param cstCmd
     * @throws SLIB_Exception
     */
    public CmdHandler(ModuleCst cst, ToolCmdHandlerCst cstCmd) throws SLIB_Exception {
        this.cst = cst;
        this.cstCmd = cstCmd;

        USAGE = "java -jar " + this.cstCmd.getAppCmdName() + " [arguments]";

        this.comparator = new OptionComparator(cstCmd.getOptionOrder());

        logger.info(HEADER);
        logger.info("\t" + cst.getAppName() + " " + cst.getVersion());
        logger.info(HEADER);

        showDescription();
        showRef();
        if (cst.reference != null) {
            logger.info(HEADER);
        }

        options = new Options();

        for (Option o : cstCmd.getOptionOrder().keySet()) {
            options.addOption(o);
        }


        helpFormatter = new HelpFormatter();
        helpFormatter.setOptionComparator(comparator);

    }

    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    public abstract void processArgs(String[] args) throws SLIB_Exception;

    /**
     *
     * @throws SLIB_Exception
     */
    public void showCmdLineExamples() throws SLIB_Exception {
        throw new SLIB_Ex_Critic("No command line examples, sorry");
    }

    /**
     * Show the description of the module if any
     */
    public final void showDescription() {
        if (cst.getDescription() != null) {
            logger.info(cst.getDescription());
        }
    }

    /**
     *
     */
    public void showContact() {
        if (cst.getContact() != null) {
            logger.info("Contact: " + cst.getContact());
        }
    }

    /**
     *
     */
    public final void showRef() {
        if (cst.getReference() != null) {
            logger.info("\nPlease cite: \n" + cst.getReference());
        }
    }

    /**
     *
     * @param message
     * @param showHelp
     * @param showDesc
     * @param showContact
     */
    public void ending(String message, boolean showHelp, boolean showDesc, boolean showContact) {

        
        if (message != null) {
            logger.info(HEADER);
            logger.info(message);
        }

        

        if (showDesc) {
            showDescription();
        }
        if (showContact) {
            showContact();
        }
        if (showHelp) {
            helpFormatter.printHelp(USAGE, HEADER, options, FOOTER);
        }

        System.exit(0);

    }

    /**
     * Ends the program (without error).
     * @param message the message to show set to null if not message
     * @param showHelp
     */
    public void ending(String message, boolean showHelp) {

        ending(message, showHelp, false, true);
        
    }
}
