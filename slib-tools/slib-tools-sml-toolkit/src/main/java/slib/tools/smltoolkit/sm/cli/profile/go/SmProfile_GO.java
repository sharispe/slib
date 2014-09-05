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
package slib.tools.smltoolkit.sm.cli.profile.go;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.utils.GraphActionExecutor;
import slib.graph.io.util.GFormat;
import slib.tools.smltoolkit.SmlModuleCLI;
import slib.tools.smltoolkit.sm.cli.core.utils.SML_SM_module_XML_block_conf;
import slib.tools.smltoolkit.sm.cli.core.utils.XMLConfUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Util;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
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
        SmProfileGOHandler c = new SmProfileGOHandler();
        c.processArgs(args);
        try {

            SML_SM_module_XML_block_conf smconf = c.getSmconf();
            boolean performGroupwise = smconf.mtype.equals(SmProfileGOCst.MTYPE_GROUPWISE);

            logger.info("Parameters");
            logger.info("---------------------------------------------------------------");
            logger.info("mType        : " + smconf.mtype);
            logger.info("Ontology     : " + smconf.ontologyPath);
            logger.info("Onto format  : " + smconf.ontologyFormat);
            logger.info("Aspect       : " + smconf.aspect);
            logger.info("Annots       : " + smconf.annotsPath);
            logger.info("Annot Format : " + smconf.annotsFormat);

            if (performGroupwise) {
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
            logger.info("notrgo       : " + Util.stringToBoolean(smconf.notrkr));
            logger.info("nonotrannots : " + Util.stringToBoolean(smconf.notrannots));
            logger.info("---------------------------------------------------------------");



            if (smconf.ontologyPath == null) {
                throw new SLIB_Ex_Critic("Please precise the location of the ontology");
            }
            if (smconf.ontologyFormat == null) {
                smconf.setOntologyFormat(SmProfileGOCst.GOFORMAT_DEFAULT);
            } else if (!Arrays.asList(SmProfileGOCst.GOFORMAT_VALID).contains(smconf.ontologyFormat)) {
                throw new SLIB_Ex_Critic("Please precise a valid ontology format, current '" + smconf.ontologyFormat + "' valid= " + Arrays.toString(SmProfileGOCst.GOFORMAT_VALID));
            } else if (smconf.ontologyFormat.equals(SmProfileGOCst.GOFORMAT_OWL)) {
                smconf.setOntologyFormat(GFormat.RDF_XML.name());
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


            String GO_PREFIX_OWL = "http://purl.org/obo/owl/GO#GO_";



            String GRAPH_URI = "http://bio/";
            String GENE_ID_PREFIX = GRAPH_URI + "geneid/";
            smconf.setGraphURI(GRAPH_URI);
            smconf.setPrefixURIAttribut(GENE_ID_PREFIX);


            //Build XML File
            // Ontology TAG
            xmlconf = "<sglib>\n";

            xmlconf += "\t<opt  threads = \"" + smconf.threads + "\"  />\n\n";

            xmlconf += "\t<namespaces>\n\t\t<nm prefix=\"GO\" ref=\"" + GO_PREFIX_OWL + "\" />\n\t</namespaces>\n\n";
            xmlconf += "\t<graphs>    \n";
            xmlconf += "\t\t<graph uri=\"" + smconf.graphURI + "\"  >    \n";
            xmlconf += "\t\t\t<data>\n";
            xmlconf += "\t\t\t\t<file format=\"" + smconf.ontologyFormat + "\"   path=\"" + smconf.ontologyPath + "\"/>    \n";

            if (smconf.annotsPath != null) {

                if (smconf.annotsFormat == null || smconf.annotsFormat.equals("GAF2")) {
                    smconf.setAnnotsFormat("GAF2");
                    xmlconf += "\t\t\t\t<file format=\"" + smconf.annotsFormat + "\"   path=\"" + smconf.annotsPath + "\"/>    \n";
                } else if (smconf.annotsFormat.equals("TSV")) {
                    // no prefixObject because the string will contain a PREFIX, e.g. GO:XXXXXX
                    xmlconf += "\t\t\t\t<file format=\"TSV_ANNOT\"   path=\"" + smconf.annotsPath + "\" prefixSubject=\"" + GENE_ID_PREFIX + "\" header=\"false\"/>    \n";
                } else {
                    throw new SLIB_Ex_Critic("Unsupported file format " + smconf.annotsFormat);
                }

            }
            xmlconf += "\t\t\t</data>\n\n";

            String actions = "";

            String goAspectValue;
            String actionValue = "VERTICES_REDUCTION";

            String GO_LOCAL_NAME_PREFIX = "";
            /*
             * OBO Format = GO:0008150 -> namespace + 0008150
             * OWL Format = http://purl.org/obo/owl/GO#GO_0008150 -> namespace + OWL_PREFIX + 0008150
             */
//            if (smconf.ontologyFormat.equals(GFormat.RDF_XML)) {
//                GO_LOCAL_NAME_PREFIX = "GO_";
//            }
            if (smconf.aspect == null || smconf.aspect.equals("BP")) {
                goAspectValue = GO_PREFIX_OWL + GO_LOCAL_NAME_PREFIX + "0008150";

            } else if (smconf.aspect.equals("MF")) {

                goAspectValue = GO_PREFIX_OWL + GO_LOCAL_NAME_PREFIX + "0003674";

            } else if (smconf.aspect.equals("CC")) {

                goAspectValue = GO_PREFIX_OWL + GO_LOCAL_NAME_PREFIX + "0005575";
            } else if (smconf.aspect.equals("GLOBAL")) {

                goAspectValue = GraphActionExecutor.REROOT_UNIVERSAL_ROOT_FLAG;
                actionValue = "REROOTING";

            } else { // expect custom=<GO term id>
                String[] data = smconf.aspect.split("=");
                if (data.length != 2) {
                    throw new SLIB_Ex_Critic("Cannot process the value " + smconf.aspect + " as a valid aspect for the GO");
                }
                goAspectValue = data[1];
                goAspectValue = goAspectValue.trim();
            }
            actions += "\t\t\t\t<action type=\"" + actionValue + "\" root_uri=\"" + goAspectValue + "\" />\n";

            if (!Util.stringToBoolean(smconf.notrkr)) {
                actions += "\t\t\t\t<action type=\"TRANSITIVE_REDUCTION\" target=\"CLASSES\" />\n";
            }
            if (smconf.annotsPath != null && !Util.stringToBoolean(smconf.notrannots)) {
                actions += "\t\t\t\t<action type=\"TRANSITIVE_REDUCTION\" target=\"INSTANCES\" />\n";
            }

            if (!actions.isEmpty()) {
                xmlconf += "\t\t\t<actions>\n" + actions + "\t\t\t</actions>\n";
            }
            xmlconf += "\t\t</graph>    \n";
            xmlconf += "\t</graphs>\n\n";

            if (smconf.filter != null) {
                if (!smconf.annotsFormat.equals(GFormat.GAF2.toString())) {
                    throw new SLIB_Ex_Critic("Filtering can only be performed on annotation file of type " + GFormat.GAF2.toString());
                }
                xmlconf += "\t<filters>\n" + XMLConfUtils.buildSML_FilterGAF2_XML_block(smconf.filter) + "\t</filters>\n";
            }


            xmlconf += XMLConfUtils.buildSML_SM_module_XML_block_GO_PROFILE(smconf);

            xmlconf += "</sglib>\n";

            logger.info("XML configuration file generated");
            logger.info(xmlconf);
            logger.info("---------------------------------------------------------------");

        } catch (Exception e) {
            c.ending(e.getMessage(), true, false, true);
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
        }
    }
}
