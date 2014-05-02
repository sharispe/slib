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
package slib.tools.smltoolkit.sm.cli.conf.xml.loader;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Corpus;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.OperatorConf;
import slib.sml.sm.core.utils.SMConstants;
import slib.sml.sm.core.utils.SMconf;
import slib.tools.module.GenericConfBuilder;
import slib.tools.module.XML_ModuleConfLoader;
import slib.tools.module.XmlTags;
import slib.tools.smltoolkit.sm.cli.conf.xml.utils.Sm_XML_Cst;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.i.Conf;
import slib.utils.impl.Util;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Sm_XMLConfLoader extends XML_ModuleConfLoader {

    Logger logger = LoggerFactory.getLogger(Sm_XMLConfLoader.class);
    /**
     *
     */
    public LinkedHashSet<SMconf> gConfPairwise;
    /**
     *
     */
    public LinkedHashSet<SMconf> gConfGroupwise;
    /**
     *
     */
    public LinkedHashSet<ICconf> gConfICs;
    /**
     *
     */
    public LinkedHashSet<Conf> gConfQueries;
    /**
     *
     */
    public LinkedHashSet<OperatorConf> gConfOperators;
    Integer nbThreads;
    Integer benchSize;
    Boolean cachePairwiseResults;
    boolean quiet = false;
    /**
     *
     */
    public String graphURI;

    //	private boolean optConfDefined = false;
    /**
     *
     * @param confFile
     * @throws SLIB_Exception
     */
    public Sm_XMLConfLoader(String confFile) throws SLIB_Exception {

        super(confFile);

        gConfPairwise = new LinkedHashSet<SMconf>();
        gConfGroupwise = new LinkedHashSet<SMconf>();
        gConfQueries = new LinkedHashSet<Conf>();
        gConfICs = new LinkedHashSet<ICconf>();
        gConfOperators = new LinkedHashSet<OperatorConf>();

        logger.info("Loading " + Sm_XML_Cst.SML_SM + " Configuration : " + confFile);

        try {

            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = parser.parse(new File(confFile));

            // check if the XML configuration refers to another XML file
            NodeList smNode = document.getElementsByTagName(Sm_XML_Cst.SML_TAG);

            if (smNode.getLength() == 1 && smNode.item(0) instanceof Element) {

                Conf gconf = GenericConfBuilder.build(smNode).iterator().next();

                String module = (String) gconf.getParam(Sm_XML_Cst.SML_MODULE);

                if (module == null || !module.equals(Sm_XML_Cst.SML_SM)) {
                    Util.error("Please specify a attribut module='sm' to tag " + Sm_XML_Cst.SML_TAG);
                }

                graphURI = (String) gconf.getParam(XmlTags.GRAPH_ATT);

                if (graphURI == null) {
                    Util.error("Please specify a attribut graph='graph_uri' to tag " + Sm_XML_Cst.SML_TAG + " specifying which graph must be considered ");
                }

                String includeFile = (String) gconf.getParam(Sm_XML_Cst.SML_SM_include);
                if (includeFile != null) {
                    logger.info("including " + includeFile);

                    DocumentBuilder p = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    Document dinc = p.parse(new File(includeFile));
                    loadData(dinc);
                }
            } else {
                if (smNode.getLength() == 0) {
                    Util.error("A " + Sm_XML_Cst.SML_TAG + " tag must be specified");
                } else {
                    Util.error("A unique " + Sm_XML_Cst.SML_TAG + " tag must be specified");
                }
            }

            loadData(document);
            checkData();


            logger.info("Configuration loaded... ");

        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                e.printStackTrace();
            }
            throw new SLIB_Exception(e.getMessage());
        }
    }

    private void checkData() throws SLIB_Ex_Critic {
        checkIcs();
        checkPairwiseMeasures();
        checkGroupwiseMeasures();
        checkQueries();
    }

    private void loadData(Document document) throws SLIB_Ex_Critic {

        NodeList opt = document.getElementsByTagName(Sm_XML_Cst.OPT_MODULE_TAG);

        // opt
        if (opt.getLength() == 1 && opt.item(0) instanceof Element) {
            extractOptConf(GenericConfBuilder.build((Element) opt.item(0)));
        } else if (opt.getLength() > 1) {
            Util.error("Only one " + Sm_XML_Cst.OPT_MODULE_TAG + " tag allowed");
        }


        NodeList ics = document.getElementsByTagName(Sm_XML_Cst.ICS_TAG);

        for (int i = 0; i < ics.getLength(); i++) {
            if (ics.item(i) instanceof Element) {
                loadICs((Element) ics.item(i));
            }
        }


        NodeList measure = document.getElementsByTagName(Sm_XML_Cst.MEASURES_TAG);

        for (int i = 0; i < measure.getLength(); i++) {
            if (measure.item(i) instanceof Element) {
                processMeasureSpec((Element) measure.item(i));
            }
        }

        NodeList queries = document.getElementsByTagName(Sm_XML_Cst.QUERIES_TAG);
        for (int i = 0; i < queries.getLength(); i++) {
            if (queries.item(i) instanceof Element) {
                loadQueries((Element) queries.item(i));
            }
        }
    }

    private void checkQueries() throws SLIB_Ex_Critic {

        for (Conf conf : gConfQueries) {

            String type = (String) conf.getParam(XmlTags.TYPE_ATTR);
            String output = (String) conf.getParam(XmlTags.OUTPUT_ATTR);

            if (output == null) {
                Util.error("Please specify an output file for each query");
            }

            if (!(type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC)
                    || type.equals(Sm_XML_Cst.QUERIES_TYPE_OTOO)
                    || type.equals(Sm_XML_Cst.QUERIES_TYPE_CTOC_FULL))) {
                Util.error("Please precise a valid type to all queries, error due to type=" + type);
            }
        }
    }

    private void checkIcs() throws SLIB_Ex_Critic {

        for (ICconf m : gConfICs) {

            String flag = m.getFlag();
            String id = m.getId();
            String label = m.getLabel();

            if (SMConstants.IC_FLAGS.contains(flag)) {

                // Check for duplicate label && id
                for (ICconf mm : gConfICs) {

                    if (mm != m && (label.equals(mm.getLabel())
                            || id.equals(mm.getId()))) {

                        if (id.equals(mm.getId())) {
                            Util.error("Duplicate IC id:" + m.getId());
                        } else {
                            Util.error("Duplicate IC label:" + label);
                        }
                    }
                }
            } else {
                if (flag == null) {
                    Util.error("A flag must be specified to each IC tag specified");
                } else {
                    Util.error("Unknown IC flag:" + flag);
                }
            }
        }
        logger.info(gConfICs.size() + " IC configurations loaded ");
    }

    private void loadQueries(Element item) throws SLIB_Ex_Critic {
        Conf querySet = GenericConfBuilder.build(item);
        gConfQueries.add(querySet);
    }

    private void loadICs(Element item) throws SLIB_Ex_Critic {
        NodeList list = item.getElementsByTagName(Sm_XML_Cst.IC_ATTR);
        LinkedHashSet<Conf> gConfICsGenerics = GenericConfBuilder.build(list);
        gConfICs.addAll(buildICconf(gConfICsGenerics));
    }

    private void checkPairwiseMeasures() throws SLIB_Ex_Critic {

        for (SMconf m : gConfPairwise) {

            String id = m.getId();
            String flag = m.getFlag();
            String label = m.getLabel();

            if (SMConstants.PAIRWISE_MEASURE_FLAGS.contains(flag)) {

                // --- Check ID

                if (id == null) {
                    Util.error("Please specify an id tag for all pairwise measure flag:" + flag);
                }

                // Check for duplicate label
                for (SMconf mm : gConfPairwise) {
                    if (mm != m && id.equals(mm.getId())) {
                        Util.error("Duplicate id value for pairwise measure:" + id);
                    }
                }

                // --- Check Label

                if (label == null) {
                    m.setLabel(id);
                }


                // Check for duplicate label
                for (SMconf mm : gConfPairwise) {
                    if (mm != m && m.getLabel().equals(mm.getLabel())) {
                        Util.error("Duplicate label:" + m.getLabel());
                    }
                }


                if (SMConstants.MEASURE_FLAGS_IC_DEPENDENCY.contains(flag)) {


                    ICconf ic = m.getICconf();

                    if (ic == null || ic.getId() == null) {
                        Util.error("Please specify an IC to node based measure: " + m.getId());
                    }

                    // Search corresponding ic specification
                    boolean valid = false;

                    for (ICconf gc : gConfICs) {
                        if (gc.getId().equals(ic.getId())) {
                            valid = true;
                            break;
                        }
                    }

                    if (!valid) {
                        Util.error("Cannot resolve IC '" + ic + "' specified for pairwise measure '" + label + "'");
                    }
                }
            } else {
                if (flag == null) {
                    Util.error("A pairwise measure have no specified flag ");
                } else {
                    Util.error("Unknown pairwise measure flag:" + flag);
                }
            }
        }
        logger.info(gConfPairwise.size() + " pairwise measure configurations loaded ");
    }

    private void checkGroupwiseMeasures() throws SLIB_Ex_Critic {

        for (SMconf m : gConfGroupwise) {

            String id = m.getId();
            String flag = m.getFlag();
            String label = m.getLabel();
            String pairwise_measure = m.getParamAsString(Sm_XML_Cst.PAIRWISE_MEASURE_ATTR);

            if (SMConstants.GROUPWISE_MEASURE_FLAGS.contains(flag)) {

                if (id == null) {
                    Util.error("Please specify an id tag for all groupwise measures");
                }

                // Check Pairwise measure dependency

                if (SMConstants.SIM_GROUPWISE_ADD_ON.containsKey(flag)) {
                    if (pairwise_measure == null) {
                        Util.error("Please specify a pairwise measure associated to groupwise measures id=" + id);
                    }

                    // Check pairwise measure exists
                    boolean valid = false;
                    for (SMconf mm : gConfPairwise) {
                        if (mm.getId().equals(pairwise_measure)) {
                            valid = true;
                            break;
                        }
                    }
                    if (!valid) {
                        throw new SLIB_Ex_Critic("Cannot refer to unloaded pairwise measure '" + pairwise_measure + "' in groupwise "
                                + "measure definition id=" + id);
                    }

                }

                // check IC dependency

                if (SMConstants.MEASURE_FLAGS_IC_DEPENDENCY.contains(flag)) {


                    ICconf ic = m.getICconf();

                    if (ic == null || ic.getId() == null) {
                        Util.error("Please specify an IC to measure: " + m.getId());
                    }

                    // Search corresponding IC specification
                    boolean valid = false;

                    for (ICconf gc : gConfICs) {
                        if (gc.getId().equals(ic.getId())) {
                            valid = true;
                            break;
                        }
                    }

                    if (!valid) {
                        Util.error("Cannot resolve IC '" + ic + "' specified for graoupwise measure '" + label + "'");
                    }
                }

                // Check for duplicate label
                for (SMconf mm : gConfGroupwise) {
                    if (mm != m && id.equals(mm.getId())) {
                        Util.error("Duplicate id value for groupwise measure:" + id);
                    }
                }


                if (label == null) {
                    m.setLabel(id);
                }

                // Check for duplicate label
                for (SMconf mm : gConfGroupwise) {
                    if (mm != m && m.getLabel().equals(mm.getLabel())) {
                        Util.error("Duplicate label:" + m.getLabel());
                    }
                }

            } else {
                if (flag == null) {
                    Util.error("A groupwise measure have no specified flag ");
                } else {
                    Util.error("Unknown groupwise measure flag:" + flag);
                }
            }
        }
        logger.info(gConfGroupwise.size() + " groupwise measure configurations  loaded ");
    }

    private void processMeasureSpec(Element item) throws SLIB_Ex_Critic {

        String type = getAttValue((Element) item, XmlTags.TYPE_ATTR);

        if (type != null) {

            NodeList list = item.getElementsByTagName(Sm_XML_Cst.MEASURE_TAG);
            LinkedHashSet<Conf> gConf = GenericConfBuilder.build(list);

            if (type.equals(Sm_XML_Cst.TYPE_VALUE_PAIRWISE)) {
                gConfPairwise.addAll(buildPairwiseConf(gConf));
            } else if (type.equals(Sm_XML_Cst.TYPE_VALUE_GROUPWISE)) {
                gConfGroupwise.addAll(buildGroupwiseConf(gConf));
            } else {
                Util.error("Unsupported type of measures specified (" + type + ")");
            }
        } else {
            Util.error("Please precise the type of measure associated to 'measures' tag");
        }
    }

    private LinkedHashSet<SMconf> buildPairwiseConf(LinkedHashSet<Conf> gCong) throws SLIB_Ex_Critic {

        LinkedHashSet<SMconf> sspPairwiseConf = new LinkedHashSet<SMconf>();

        for (Conf c : gCong) {

            String id = (String) c.getParam(XmlTags.ID_ATTR);
            String label = (String) c.getParam(XmlTags.LABEL_ATTR);

            if (label == null) {
                label = id;
            }


            String icID = (String) c.getParam(Sm_XML_Cst.IC_ATTR);

            logger.debug("Loading measure " + label);

            if (icID != null && icID.equals(Sm_XML_Cst.IC_ATTR_VALUE_FULL_LIST)) {

                if (gConfICs.size() == 0) {
                    throw new SLIB_Ex_Critic(" Pairwise measure " + label + " requires IC(s) to be defined, none found");
                }

                for (ICconf ic_conf : gConfICs) {


                    String avoided = null;

                    if (ic_conf.getParams() != null) {
                        avoided = (String) ic_conf.getParams().get(XmlTags.EXCLUDE_AUTO_MEASURE);
                    }

                    if (Util.stringToBoolean(avoided)) {
                        continue;
                    }

                    String id_tmp = id + "_" + ic_conf.getId();
                    String label_tmp = label;

                    if (label != null) {
                        label_tmp = label + "_" + ic_conf.getId();
                    }

                    c.addParam(XmlTags.ID_ATTR, id_tmp);
                    c.addParam(XmlTags.LABEL_ATTR, label_tmp);

                    c.addParam(Sm_XML_Cst.IC_ATTR, ic_conf.getId());
                    sspPairwiseConf.add(buildPairwiseConf(c));
                }
            } else {
                sspPairwiseConf.add(buildPairwiseConf(c));
            }
        }
        return sspPairwiseConf;
    }

    private SMconf buildPairwiseConf(Conf c) throws SLIB_Ex_Critic {

        // Attributes (processed below), not considered as extraParameters

        String[] defaultAttributs = {
            XmlTags.ID_ATTR,
            XmlTags.LABEL_ATTR,
            Sm_XML_Cst.FLAG_ATTR,
            Sm_XML_Cst.IC_ATTR,
            Sm_XML_Cst.IC_PROB
        };


        String id = (String) c.getParam(XmlTags.ID_ATTR);
        String label = (String) c.getParam(XmlTags.LABEL_ATTR);
        String flag = (String) c.getParam(Sm_XML_Cst.FLAG_ATTR);
        String icID = (String) c.getParam(Sm_XML_Cst.IC_ATTR);


        ICconf icConf = null;

        if (icID != null) { // search ic

            icConf = getIC(icID);

            if (icConf == null) {
                throw new SLIB_Ex_Critic("Cannot locate IC " + icID + " define for pairwise measure " + id);
            }
        }

        SMconf pc = new SMconf(id, flag, label, icConf);

        // load IC used to compute prob MICA
        if (SMConstants.MEASURE_REQUIRE_EXTRA_IC.contains(flag)) {

            String ic_prob_id = (String) c.getParam(Sm_XML_Cst.IC_PROB);
            ICconf ic_prob = getIC(ic_prob_id);

            if (ic_prob == null) {
                throw new SLIB_Ex_Critic("Cannot locate IC used to compute MICA probability for " + id + ", please define an attribute " + Sm_XML_Cst.IC_PROB + " refering to an IC id ");
            }

            pc.addParam(Sm_XML_Cst.IC_PROB, ic_prob);
        }

        pc = addExtraAttributs(defaultAttributs, c, pc);

        return pc;
    }

    /**
     * Load extra parameters. Add to the given object extending {@link Conf}
     * object all the parameters found in the given {@link Conf} object,
     * excluding those having a name defined in the array of String
     *
     * @param defaultAttributs the name of the attributes to not consider as
     * extra parameters
     * @param c the {@link Conf} object containing all the key-value
     * @param pc the {@link SMconf} object to populate
     * @return pc populated.
     */
    private <C extends Conf> C addExtraAttributs(String[] defaultAttributs, Conf c, C pc) {

        List<String> def = Arrays.asList(defaultAttributs);

        for (Entry<String, Object> e : c.getParams().entrySet()) {
            if (!def.contains(e.getKey())) {
                pc.addParam(e.getKey(), e.getValue());
            }
        }
        return pc;
    }

    private ICconf getIC(String icID) {

        for (ICconf ic : gConfICs) {
            if (ic.getId().equals(icID)) {
                return ic;
            }
        }
        return null;
    }

    private LinkedHashSet<SMconf> buildGroupwiseConf(LinkedHashSet<Conf> gCong) throws SLIB_Ex_Critic {

        LinkedHashSet<SMconf> sspGoupwiseConf = new LinkedHashSet<SMconf>();

        for (Conf c : gCong) {

            String id = (String) c.getParam(XmlTags.ID_ATTR);
            String label = (String) c.getParam(XmlTags.LABEL_ATTR);
            String flag = (String) c.getParam(Sm_XML_Cst.FLAG_ATTR);
            String pairwise_measure = (String) c.getParam(Sm_XML_Cst.PAIRWISE_MEASURE_ATTR);
            String ic_id = (String) c.getParam(Sm_XML_Cst.IC_TAG);

            if (SMConstants.GROUPWISE_MEASURE_FLAGS.contains(flag)
                    && pairwise_measure != null
                    && pairwise_measure.equals(Sm_XML_Cst.PAIRWISE_MEASURE_ATTR_VALUE_FULL_LIST)) {

                // Generate mixing strategy configuration for all pairwise measures loaded


                for (SMconf pmConf : gConfPairwise) {

                    String id_tmp = id + "_" + pmConf.getId();
                    String label_tmp = null;

                    if (label != null) {
                        label_tmp = label + "_" + pmConf.getId();
                    }

                    c.addParam(XmlTags.ID_ATTR, id_tmp);
                    c.addParam(XmlTags.LABEL_ATTR, label_tmp);
                    c.addParam(Sm_XML_Cst.PAIRWISE_MEASURE_ATTR, pmConf.getId());



                    if(ic_id != null && ic_id.equals(Sm_XML_Cst.IC_ATTR_VALUE_FULL_LIST)) {


                        for (ICconf ic_conf : gConfICs) {

                            String id_tmp_2 = id_tmp + "_" + ic_conf.getId();
                            String label_tmp_2 = null;

                            if (label_tmp != null) {
                                label_tmp_2 = label_tmp + "_" + ic_conf.getId();
                            }

                            c.addParam(XmlTags.ID_ATTR, id_tmp_2);
                            c.addParam(XmlTags.LABEL_ATTR, label_tmp_2);

                            c.addParam(Sm_XML_Cst.IC_ATTR, ic_conf.getId());
                            sspGoupwiseConf.add(buildGroupwiseConf(c));
                        }
                    } else {
                        sspGoupwiseConf.add(buildGroupwiseConf(c));
                    }
                }

            } else {

                if (ic_id != null && ic_id.equals(Sm_XML_Cst.IC_ATTR_VALUE_FULL_LIST)) {

                    for (ICconf ic_conf : gConfICs) {

                        String id_tmp = id + "_" + ic_conf.getId();
                        String label_tmp = label;

                        if (label != null) {
                            label_tmp = label + "_" + ic_conf.getId();
                        }

                        c.addParam(XmlTags.ID_ATTR, id_tmp);
                        c.addParam(XmlTags.LABEL_ATTR, label_tmp);

                        c.addParam(Sm_XML_Cst.IC_ATTR, ic_conf.getId());
                        sspGoupwiseConf.add(buildGroupwiseConf(c));
                    }
                } else {
                    sspGoupwiseConf.add(buildGroupwiseConf(c));
                }
            }

        }
        return sspGoupwiseConf;
    }

    private SMconf buildGroupwiseConf(Conf c) throws SLIB_Ex_Critic {

        // Attributes (processed below), not considered as extraParameters


        String[] defaultAttributs = {
            XmlTags.ID_ATTR,
            XmlTags.LABEL_ATTR,
            Sm_XML_Cst.FLAG_ATTR,
            Sm_XML_Cst.IC_ATTR,
            Sm_XML_Cst.PAIRWISE_MEASURE_ATTR,};

        String id = (String) c.getParam(XmlTags.ID_ATTR);
        String label = (String) c.getParam(XmlTags.LABEL_ATTR);
        String flag = (String) c.getParam(Sm_XML_Cst.FLAG_ATTR);
        String ic_id = (String) c.getParam(Sm_XML_Cst.IC_TAG);
        String pairwise_measure = (String) c.getParam(Sm_XML_Cst.PAIRWISE_MEASURE_ATTR);

        ICconf icConf = null;

        if (ic_id != null) { // search ic

            for (ICconf ic : gConfICs) {
                if (ic.getId().equals(ic_id)) {
                    icConf = ic;
                    break;
                }
            }
            if (icConf == null) {
                throw new SLIB_Ex_Critic("Cannot locate IC " + ic_id + " define for groupwise measure " + id);
            }

        }

        SMconf pc = new SMconf(id, flag, label, icConf);
        pc.addParam(Sm_XML_Cst.PAIRWISE_MEASURE_ATTR,pairwise_measure);

        pc = addExtraAttributs(defaultAttributs, c, pc);

        return pc;
    }

    private void extractOptConf(Conf gc) throws SLIB_Ex_Critic {

        String benchSize_s = (String) gc.getParam(Sm_XML_Cst.OPT_BENCH_SIZE_ATTR);
        String cache_pairwise_s = (String) gc.getParam(Sm_XML_Cst.OPT_CACHE_PAIRWISE_ATTR);
        String quiet_s = (String) gc.getParam(Sm_XML_Cst.OPT_QUIET_ATTR);


        if (benchSize_s != null) {
            try {
                benchSize = Integer.parseInt(benchSize_s);
            } catch (NumberFormatException e) {
                throw new SLIB_Ex_Critic("Error converting " + Sm_XML_Cst.OPT_BENCH_SIZE_ATTR + " to an integer value ");
            }
        }



        if (cache_pairwise_s != null) {

            if (Util.stringToBoolean(cache_pairwise_s)) {
                cachePairwiseResults = true;
            } else {
                cachePairwiseResults = false;
            }
        }

        if (quiet_s != null) {
            if (Util.stringToBoolean(quiet_s)) {
                quiet = true;
            } else {
                quiet = false;
            }
        }
    }

    private LinkedHashSet<ICconf> buildICconf(LinkedHashSet<Conf> gCong) throws SLIB_Ex_Critic {


        LinkedHashSet<ICconf> icConfSet = new LinkedHashSet<ICconf>();

        // Attributes (processed below), not considered as extraParameters

        String[] defaultAttributs = {
            XmlTags.ID_ATTR,
            XmlTags.LABEL_ATTR,
            Sm_XML_Cst.FLAG_ATTR,
            XmlTags.KB_ATTR
        };

        for (Conf c : gCong) {

            String id = (String) c.getParam(XmlTags.ID_ATTR);
            String label = (String) c.getParam(XmlTags.LABEL_ATTR);
            String flag = (String) c.getParam(Sm_XML_Cst.FLAG_ATTR);

            if (id == null) {
                id = flag;
            }
            if (label == null) {
                label = id;
            }

            ICconf ic = null;

            if (SMConstants.SIM_PAIRWISE_DAG_NODE_IC_INTRINSIC.containsKey(flag)) {
                ic = new IC_Conf_Topo(id, label, flag);
            } else if (SMConstants.SIM_PAIRWISE_DAG_NODE_IC_ANNOT.containsKey(flag)) {

                ic = new IC_Conf_Corpus(id, label, flag);
            } else {
                throw new SLIB_Ex_Critic("Cannot resolve IC flag: " + flag);
            }

            ic = addExtraAttributs(defaultAttributs, c, ic);


            for (ICconf m : icConfSet) {
                if (m.getId().equals(ic.getId())) {
                    throw new SLIB_Ex_Critic("Duplicate id for IC " + ic.getId());
                }
            }
            icConfSet.add(ic);
        }
        return icConfSet;
    }

    /**
     *
     * @return the allocated size of the bench
     */
    public Integer getBenchSize() {
        return benchSize;
    }

    public Boolean getCachePairwiseResults() {
        return cachePairwiseResults;
    }

    public boolean isQuiet() {
        return quiet;
    }
}
