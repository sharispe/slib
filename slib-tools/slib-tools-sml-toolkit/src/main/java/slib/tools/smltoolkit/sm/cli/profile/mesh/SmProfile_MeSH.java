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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.tools.smltoolkit.SmlModuleCLI;
import slib.tools.smltoolkit.sm.cli.core.utils.SML_SM_module_XML_block_conf;
import slib.tools.smltoolkit.sm.cli.core.utils.XMLConfUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Util;

/**
 *
 * @author Sébastien Harispe
 */
public class SmProfile_MeSH implements SmlModuleCLI {

    Logger logger = LoggerFactory.getLogger(SmProfile_MeSH.class);
    public String xmlconf;

    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    @Override
    public void execute(String[] args) throws SLIB_Exception {

        logger.info("Init command-line parser...");
        SmProfileMeSHHandler c = new SmProfileMeSHHandler(args);
        c.processArgs(args);
        
        try {

            logger.info("Parsing command-line...");
            SML_SM_module_XML_block_conf smconf = c.getSmconf();
            

            logger.info("Parameters");
            logger.info("---------------------------------------------------------------");
            logger.info("mType        : " + smconf.mtype);
            logger.info("Ontology     : " + smconf.ontologyPath);
            
            boolean performGroupwise = smconf.mtype.equals(SmProfileMeSHCst.MTYPE_GROUPWISE);

            if (performGroupwise) {
                logger.info("Annots       : " + smconf.annotsPath);
                logger.info("Annot Format : " + smconf.annotsFormat);
                logger.info("notfound     : " + smconf.notFound);
                logger.info("noannots     : " + smconf.noAnnots);
                logger.info("filter       : " + smconf.filter);
            }
            logger.info("Queries      : " + smconf.queries);
            logger.info("Output       : " + smconf.output);
            logger.info("pm           : " + smconf.pmShortFlag);
            logger.info("ic           : " + smconf.icShortFlag);

            if (performGroupwise) {
                logger.info("gm           : " + smconf.gmShortFlag);
            }

            logger.info("quiet        : " + Util.stringToBoolean(smconf.quiet));
            logger.info("threads      : " + smconf.threads);
            logger.info("notrkr       : " + Util.stringToBoolean(smconf.notrkr));
            logger.info("nonotrannots : " + Util.stringToBoolean(smconf.notrannots));
            logger.info("---------------------------------------------------------------");

            if (smconf.ontologyPath == null) {
                throw new SLIB_Ex_Critic("Please precise the location of the ontology");
            }
            if (smconf.queries == null) {
                throw new SLIB_Ex_Critic("Please precise the location of the queries");
            }
            if (smconf.output == null) {
                throw new SLIB_Ex_Critic("Please precise the location of the output file");
            }

            try {
                if (Integer.parseInt(smconf.threads) < 1) { //NumberFormatException will be thrown if not valid
                    throw new Exception();
                }
            } catch (Exception e) {
                throw new SLIB_Ex_Critic("Please correct the number of threads allocated");
            }

            smconf.setGraphURI("http://g/");

            //Build XML File
            // Ontology TAG
            xmlconf = "<sglib>\n";

            xmlconf += "\t<opt  threads = \"" + smconf.threads + "\"  />\n\n";

            xmlconf += "\t<graphs>    \n";
            xmlconf += "\t\t<graph uri=\"" + smconf.graphURI + "\"  >    \n";
            xmlconf += "\t\t\t<data>\n";
            xmlconf += "\t\t\t\t<file format=\"MESH_XML\"   path=\"" + smconf.ontologyPath + "\"/>    \n";

            if (smconf.annotsPath != null) {

                if (smconf.annotsFormat.equals("TSV")) {
                    
                    xmlconf += "\t\t\t\t<file format=\"TSV_ANNOT\"   path=\"" + smconf.annotsPath + "\" prefixSubject=\"" + smconf.graphURI + "\"  prefixObject=\"" + smconf.graphURI + "\" header=\"false\"/>    \n";
                } else {
                    throw new SLIB_Ex_Critic("Unsupported file format " + smconf.annotsFormat);
                }

            }
            xmlconf += "\t\t\t</data>\n\n";

            
            xmlconf += "\t\t</graph>    \n";
            xmlconf += "\t</graphs>\n\n";

            smconf.setPrefixURIAttribut(smconf.graphURI);
            xmlconf += XMLConfUtils.buildSML_SM_module_XML_block_MESH_PROFILE(smconf);

            xmlconf += "</sglib>\n";

            logger.info("XML configuration file generated");
            logger.info(xmlconf);
            logger.info("---------------------------------------------------------------");

        } catch (SLIB_Ex_Critic e) {
            c.ending("Error processing configuration: "+e.getMessage(), true, false, true);
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }
}
