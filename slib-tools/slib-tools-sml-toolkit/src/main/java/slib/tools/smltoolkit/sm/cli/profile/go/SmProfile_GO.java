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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sml.sm.core.utils.SMConstants;
import slib.tools.smltoolkit.SmlModuleCLI;
import slib.tools.smltoolkit.sm.cli.utils.SML_SM_module_XML_block_conf;
import slib.tools.smltoolkit.sm.cli.utils.XMLConfUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe
 */
public class SmProfile_GO implements SmlModuleCLI {

    Logger logger = LoggerFactory.getLogger(SmProfile_GO.class);
    public String xmlconf;

    /**
     *
     * @param args
     * @throws SLIB_Exception
     */
    @Override
    public void execute(String[] args) throws SLIB_Exception {
        SmProfileGOHandler c = new SmProfileGOHandler(args);



        logger.info("Processing GO");
        logger.info("Ontology     : " + c.ontologyPath);
        logger.info("Annots       : " + c.annotsPath);
        logger.info("Annot Format : " + c.annotsFormat);
        logger.info("Queries      : " + c.queries);
        logger.info("Output       : " + c.outputFile);
        logger.info("mType        : " + c.mType);
        logger.info("Aspect       : " + c.aspect);
        logger.info("notfound     : " + c.notfound);
        logger.info("noannots     : " + c.noannots);
        logger.info("filter       : " + c.filter);
        logger.info("pm           : " + c.pm);
        logger.info("gm           : " + c.gm);
        logger.info("ic           : " + c.ic);
        logger.info("quiet        : " + c.quiet);
        logger.info("threads      : " + c.threads);
        logger.info("notrgo       : " + c.notrgo);
        logger.info("nonotrannots : " + c.notrannots);

        if (c.ontologyPath == null) {
            throw new SLIB_Ex_Critic("Please precise the location of the ontology");
        }
        if (c.queries == null) {
            throw new SLIB_Ex_Critic("Please precise the location of the queries");
        }
        if (c.outputFile == null) {
            throw new SLIB_Ex_Critic("Please precise the location of the output file");
        }

        try {
            if (Integer.parseInt(c.threads) < 1) { //NumberFormatException will be thrown if not valid
                throw new Exception();
            }
        } catch (Exception e) {
            throw new SLIB_Ex_Critic("Please correct the number of threads allocated");
        }

        String graphURI = "http://g/";
        //Build XML File
        // Ontology TAG
        xmlconf = "<sglib>\n";

        xmlconf += "\t<opt  threads = \"" + c.threads + "\"  />\n\n";

        xmlconf += "\t<namespaces>\n\t\t<nm prefix=\"GO\" ref=\"" + graphURI + "\" />\n\t</namespaces>\n\n";
        xmlconf += "\t<graphs>    \n";
        xmlconf += "\t\t<graph uri=\"" + graphURI + "\"  >    \n";
        xmlconf += "\t\t\t<data>\n";
        xmlconf += "\t\t\t\t<file format=\"OBO\"   path=\"" + c.ontologyPath + "\"/>    \n";
        if (c.annotsPath != null) {
            if (c.annotsFormat == null) {
                c.annotsFormat = "GAF_2";
            }
            xmlconf += "\t\t\t\t<file format=\"" + c.annotsFormat + "\"   path=\"" + c.annotsPath + "\"/>    \n";
        }
        xmlconf += "\t\t\t</data>\n\n";

        String actions = "";

        String goAspectValue;
        if (c.aspect == null || c.aspect.equals("BP")) {
            goAspectValue = graphURI + "0008150";
        } else if (c.aspect.equals("MF")) {
            goAspectValue = graphURI + "0003674";
        } else if (c.aspect.equals("CC")) {
            goAspectValue = graphURI + "0005575";
        } else if (c.aspect.equals("GLOBAL")) {
            goAspectValue = "__FICTIVE__";
        } else { // expect custom=<GO term id>
            String[] data = c.aspect.split("=");
            if (data.length != 2) {
                throw new SLIB_Ex_Critic("Cannot process the value " + c.aspect + " as a valid aspect for the GO");
            }
            goAspectValue = data[1];
            goAspectValue = goAspectValue.trim();
        }
        actions += "\t\t\t\t<action type=\"REROOTING\" root_uri=\"" + goAspectValue + "\" />\n";

        if (!c.notrgo) {
            actions += "\t\t\t\t<action type=\"TRANSITIVE_REDUCTION\" target=\"CLASSES\" />\n";
        }
        if (c.annotsPath != null && !c.notrannots) {
            actions += "\t\t\t\t<action type=\"TRANSITIVE_REDUCTION\" target=\"INSTANCES\" />\n";
        }

        if (!actions.isEmpty()) {
            xmlconf += "\t\t\t<actions>\n" + actions + "\t\t\t</actions>\n";
        }
        xmlconf += "\t\t</graph>    \n";
        xmlconf += "\t</graphs>\n\n";
        
        if(c.filter != null){
            if(!c.annotsFormat.equals("GAF_2")){
                throw new SLIB_Ex_Critic("Filtering can only be performed on annotation file of type GAF_2");
            }
            xmlconf += "\t<filters>\n"+XMLConfUtils.buildSML_FilterGAF2_XML_block(c.filter)+"\t</filters>\n";
        }

        SML_SM_module_XML_block_conf smconf = new SML_SM_module_XML_block_conf()
                .setGraphURI(graphURI)
                .setThreads(c.threads)
                .setIcShortFlag(c.ic)
                .setPmShortFlag(c.pm)
                .setGmShortFlag(c.gm)
                .setMtype(c.mType)
                .setQueries(c.queries)
                .setOutput(c.outputFile);

        xmlconf += XMLConfUtils.buildSML_SM_module_XML_block(smconf);

        xmlconf += "</sglib>\n";



        System.out.println(xmlconf);



    }
}
