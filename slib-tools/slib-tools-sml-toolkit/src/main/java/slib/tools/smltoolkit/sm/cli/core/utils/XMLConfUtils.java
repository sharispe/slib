/*
 * 
 * Copyright or © or Copr. Ecole des Mines d'Alès (2012) 
 * LGI2P research center
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 * 
 */
package slib.tools.smltoolkit.sm.cli.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import slib.sml.sm.core.utils.SMConstants;
import slib.tools.module.XmlTags;
import slib.tools.smltoolkit.sm.cli.conf.xml.utils.Sm_XML_Cst;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sébastien Harispe
 */
public class XMLConfUtils {

    /**
     * this parameter can be used to filter the GO terms associated to a gene
     * product when the provided annotation file is in GAF2 format. - EC=
     * evidence_codes evidence codes separated by commas e.g. EC=IEA only IEA
     * annotations will be considered - Taxon=taxon_ids taxon ids separated by
     * commas e.g. Taxon=9696 to only consider annotations associated to Taxon
     * 9696 Example of value -filter EC=IEA,XXX:Taxon=9696
     *
     * @param filterConfAsString
     * @return
     * @throws SLIB_Ex_Critic
     */
    public static String buildSML_FilterGAF2_XML_block(String filterConfAsString) throws SLIB_Ex_Critic {

        if (filterConfAsString == null) {
            return "";
        }

        List<String> listParams = new ArrayList<String>(Arrays.asList(filterConfAsString.split(":")));
        String conf = "";
        String remove_ecAtt = null, taxonAtt = null;

        for (String s : listParams) {

            String[] data = s.split("=");
            if (data[0].equals("noEC")) {
                if (data.length == 2) {
                    remove_ecAtt = "remove_ec=\"" + data[1] + "\"";
                }
            } else if (data[0].equals("Taxon")) {
                if (data.length == 2) {
                    taxonAtt = "tax_ids=\"" + data[1] + "\"";
                }
            } else {
                throw new SLIB_Ex_Critic("Cannot process the filter parameters in " + s + " please consult the documentation");
            }
        }

        if (remove_ecAtt != null || taxonAtt != null) {
            conf += "\t\t<filter\n";
            conf += "\t\t\tid = \"filter_gaf_2\"\n";
            conf += "\t\t\ttype = \"GAF2\"\n";
            if (remove_ecAtt != null) {
                conf += "\t\t\t" + remove_ecAtt + "\n";
            }
            if (taxonAtt != null) {
                conf += "\t\t\t" + taxonAtt + "\n";
            }
            conf += "\t\t/>\n";
        }

        return conf;
    }

    /**
     * Build a String representation of the Semantic Measures XML tag
     * considering the given configuration.
     *
     * @param c the configuration used to build the XML tag
     * @return a String representation of the Semantic Measures XML tag
     * corresponding to the given configuration
     * @throws SLIB_Ex_Critic
     */
    public static String buildSML_SM_module_XML_block(SML_SM_module_XML_block_conf c) throws SLIB_Ex_Critic {


        String optModuleXML = "";
        String icXML = "";
        String pmXML = "";
        String gmXML = "";
        String queryXML = "";

        String icflag = null;
        String pmflag = null;
        String gmflag = null;

        optModuleXML += "\t\t<opt_module threads=\"" + c.threads + "\" ";

        if (c.quiet != null) {
            optModuleXML += "\tquiet = \"" + c.quiet + "\"\n";
        }
        optModuleXML += "/>\n\n";

        //Create the XML part corresponding to the semantic measures

        if (c.icShortFlag != null) {
            if (SMConstants.IC_SHORT_FLAG.containsKey(c.icShortFlag)) {

                icflag = SMConstants.IC_SHORT_FLAG.get(c.icShortFlag);

                icXML += "\n\t\t<ics>\n"
                        + "\t\t\t<ic id   = \"" + c.icShortFlag + "\" flag = \"" + icflag + "\"  />\n";

            } else {
                throw new SLIB_Ex_Critic("The flag of the information content you selected '" + c.icShortFlag + "' cannot be associated to a measure, supported are " + SMConstants.IC_SHORT_FLAG.keySet());
            }
        }

        if (c.pmShortFlag != null) {

            if (SMConstants.SIM_PAIRWISE_SHORT_FLAG.containsKey(c.pmShortFlag)) {

                pmflag = SMConstants.SIM_PAIRWISE_SHORT_FLAG.get(c.pmShortFlag);
                String icAtt = "";
                if (icflag != null) {
                    icAtt = "ic = \"" + c.icShortFlag + "\" ";
                }

                if (c.pmShortFlag.equals(SMConstants.SHORT_FLAG_PM_SCHLICKER)) {
                    icXML += "\t\t\t<ic id   = \"ic_prob_propagatted\" flag = \"" + SMConstants.FLAG_ICI_PROB_OCCURENCE_PROPAGATED + "\"  />\n";
                    icAtt += " ic_prob = \"ic_prob_propagatted\"";
                }


                pmXML += "\t\t<measures type = \"pairwise\">\n"
                        + "\t\t\t<measure   id   = \"" + c.pmShortFlag + "\" flag = \"" + pmflag + "\"  " + icAtt + " />\n"
                        + "\t\t</measures>\n\n";
            } else {
                throw new SLIB_Ex_Critic("The flag of the pairwise semantic measure you selected '" + c.pmShortFlag + "' cannot be associated to a measure, supported are " + SMConstants.SIM_PAIRWISE_SHORT_FLAG.keySet());
            }
        }
        if (c.gmShortFlag != null) {
            if (SMConstants.SIM_GROUPWISE_SHORT_FLAG.containsKey(c.gmShortFlag)) {


                gmflag = SMConstants.SIM_GROUPWISE_SHORT_FLAG.get(c.gmShortFlag);

                String pmAtt = "";
                if (pmflag != null) {
                    pmAtt = "pairwise_measure = \"" + c.pmShortFlag + "\"";
                }

                String icAtt = "";
                if (icflag != null && pmflag == null) {
                    icAtt = "ic = \"" + c.icShortFlag + "\"";
                }

                gmXML += "\t\t<measures type = \"groupwise\">\n"
                        + "\t\t\t<measure   id   = \"" + c.gmShortFlag + "\" flag = \"" + gmflag + "\"  " + pmAtt + " " + icAtt + " />\n"
                        + "\t\t</measures>\n\n";
            } else {
                throw new SLIB_Ex_Critic("The flag of the groupwise semantic measure you selected '" + c.gmShortFlag + "' cannot be associated to a measure, supported are " + SMConstants.SIM_GROUPWISE_SHORT_FLAG.keySet());
            }
        }


        if (!icXML.isEmpty()) {
            icXML += "\t\t</ics>\n\n";
        }

        // Query



        String mType = "cTOc"; // pairwise measures

        if (c.mtype != null && c.mtype.equals("g")) {
            mType = "oTOo"; // groupwise measures

            if (c.gmShortFlag == null) {
                throw new SLIB_Ex_Critic("Please precise a groupwise measure -gm (also using -pm if you want to use an indirect groupwise measure)");
            }
        } else if (c.pmShortFlag == null) {
            throw new SLIB_Ex_Critic("Please precise a pairwise measure -pm");
        }

        queryXML += "\t\t<queries id= \"query\" \n"
                + "\t\t\t" + XmlTags.TYPE_ATTR + "    = \"" + mType + "\" \n"
                + "\t\t\t" + XmlTags.FILE_ATTR + "    = \"" + c.queries + "\" \n"
                + "\t\t\t" + XmlTags.OUTPUT_ATTR + "  = \"" + c.output + "\" \n";


        if (c.noAnnots != null) {
            queryXML += "\t\t\t" + Sm_XML_Cst.OPT_NO_ANNOTS_ATTR + " = \"" + c.noAnnots + "\"\n";

        }
        if (c.notFound != null) {
            queryXML += "\t\t\t" + Sm_XML_Cst.OPT_NOT_FOUND_ATTR + " = \"" + c.notFound + "\"\n";

        }

        // We just prefix the URIs by the URI associated to the graph and
        // we just output the local name in the result file
        if (c.mtype != null && c.mtype.equals("g")) {
            queryXML += "\t\t\t" + XmlTags.URI_PREFIX_ATTR + " = \"" + c.graphURI + "\"\n";
            queryXML += "\t\t\t" + Sm_XML_Cst.OUTPUT_BASENAME_ATT + " = \"false\" \n";
        } else {
            // Pairwise context: users will specify GO:xxxx 
            // we use loaded prefixes to build the URIs and
            // to output the results using GO:xxxx
            queryXML += "\t\t\t" + Sm_XML_Cst.USE_URI_PREFIX_ATTR + " = \"true\"\n";
            queryXML += "\t\t\t" + Sm_XML_Cst.OUTPUT_BASENAME_ATT + " = \"true\" \n";
            queryXML += "\t\t\t" + Sm_XML_Cst.USE_URI_PREFIX_OUTPUT_ATTR + " = \"true\"\n";
        }
        queryXML += "\t\t/>\n";


        String xmlconf = "\t<sml module=\"sm\" graph=\"" + c.graphURI + "\" >\n\n";
        xmlconf += optModuleXML;
        xmlconf += icXML;
        xmlconf += pmXML;
        xmlconf += gmXML;
        xmlconf += queryXML;

        xmlconf += "\t</sml>\n";
        return xmlconf;
    }
}
