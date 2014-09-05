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

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import slib.graph.algo.utils.GAction;
import slib.graph.algo.utils.GActionType;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.conf.GraphConf;
import slib.graph.io.loader.csv.CSV_Mapping;
import slib.graph.io.loader.csv.CSV_StatementTemplate;
import slib.graph.io.loader.csv.CSV_StatementTemplate_Constraint;
import slib.graph.io.loader.csv.StatementTemplateElement;
import slib.graph.io.loader.csv.StatementTemplate_Constraint_Type;
import slib.graph.io.loader.utils.filter.graph.Filter;
import slib.graph.io.loader.utils.filter.graph.repo.FilterRepository;
import slib.graph.io.util.GFormat;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;
import slib.utils.i.Parametrable;
import slib.utils.impl.Util;
import slib.utils.threads.ThreadManager;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class XMLConfLoaderGeneric {

    private String xmlFile;
    private Document document;
    private URIFactory factory;
    private LinkedList<GraphConf> graphConfs;
    private LinkedHashSet<Filter> filters;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    static final Map<String, URI> admittedPType = new HashMap<String, URI>();

    static {
        admittedPType.put("RDF.TYPE", RDF.TYPE);
        admittedPType.put("RDFS.SUBCLASSOF", RDFS.SUBCLASSOF);
    }

    /**
     *
     * @param xmlFile
     * @throws SLIB_Ex_Critic
     */
    public XMLConfLoaderGeneric(String xmlFile) throws SLIB_Ex_Critic {


        factory = URIFactoryMemory.getSingleton();

        this.xmlFile = xmlFile;
        graphConfs = new LinkedList<GraphConf>();
        filters = new LinkedHashSet<Filter>();

        load();
    }

    private void load() throws SLIB_Ex_Critic {
        logger.info("Loading XML conf from : " + xmlFile);


        try {

            DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            document = parser.parse(new File(xmlFile));


            NodeList opt = document.getElementsByTagName(XmlTags.OPT_TAG);

            //------------------------------
            //	 Load General Option 
            //------------------------------
            logger.debug("Loading options");

            if (opt.getLength() == 1 && opt.item(0) instanceof Element) {
                extractOptConf(GenericConfBuilder.build((Element) opt.item(0)));
            } else if (opt.getLength() > 1) {
                Util.error("Only one " + XmlTags.OPT_TAG + " tag allowed");
            }




            //------------------------------
            //	 Load Variables 
            //------------------------------
            logger.debug("Loading variables");
            NodeList variablesConfig = document.getElementsByTagName(XmlTags.VARIABLES_TAG);

            if (variablesConfig.getLength() == 1 && variablesConfig.item(0) instanceof Element) {
                loadVariablesConf((Element) variablesConfig.item(0));
            } else if (variablesConfig.getLength() > 0) {
                Util.error("Only one " + XmlTags.VARIABLES_TAG + " is admitted");
            }

            //------------------------------
            //	 Load Name space Option 
            //------------------------------

            NodeList namespaces = document.getElementsByTagName(XmlTags.NAMESPACES_TAG);

            if (namespaces.getLength() == 1 && namespaces.item(0) instanceof Element) {
                loadNamespaces((Element) namespaces.item(0));
            } else if (namespaces.getLength() > 1) {
                Util.error("Only one " + XmlTags.NAMESPACES_TAG + " tag allowed");
            }



            //------------------------------
            //	 Load Graph Information
            //------------------------------

            logger.debug("Loading graph configurations");

            NodeList graphsConfig = document.getElementsByTagName(XmlTags.GRAPHS_TAG);


            // Check number of graph specification + critical attributes
            if (graphsConfig.getLength() == 1 && graphsConfig.item(0) instanceof Element) {

                NodeList nListGConf = ((Element) graphsConfig.item(0)).getElementsByTagName(XmlTags.GRAPH_TAG);


                for (int i = 0; i < nListGConf.getLength(); i++) {
                    Element gConf = (Element) nListGConf.item(i);
                    loadGraphConf(gConf);
                }
            } else {
                Util.error(XMLConstUtils.ERROR_NB_GRAPHS_SPEC);
            }


            //------------------------------
            //	 Load Filters Specification
            //------------------------------

            logger.debug("Loading filters");
            NodeList filtersElement = document.getElementsByTagName(XmlTags.FILTERS_TAG);

            if (filtersElement.getLength() == 1 && filtersElement.item(0) instanceof Element) {
                loadFiltersConf((Element) filtersElement.item(0));
            } else if (filtersElement.getLength() > 0) {
                Util.error("Only one " + XmlTags.FILTERS_TAG + " is admitted");
            }


        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }


        logger.info("generic configuration loaded ");
        //		logger.info("- Graph conf loaded : "+graphConfs.size());

    }

    private void loadNamespaces(Element item) throws SLIB_Ex_Critic {

        NodeList list = item.getElementsByTagName(XmlTags.NAMESPACE_TAG);

        for (int i = 0; i < list.getLength(); i++) {

            Conf m = GenericConfBuilder.build((Element) list.item(i));

            String prefix = (String) m.getParam(XmlTags.NS_ATTR_PREFIX);
            String ref = (String) m.getParam(XmlTags.NS_ATTR_REF);

            if (prefix == null) {
                throw new SLIB_Ex_Critic("Invalid " + XmlTags.NAMESPACE_TAG + " tag, missing a " + XmlTags.NS_ATTR_PREFIX + " attribut");
            } else if (ref == null) {
                throw new SLIB_Ex_Critic("Invalid " + XmlTags.NAMESPACE_TAG + " tag, missing a " + XmlTags.NS_ATTR_REF + " attribut associated to variable " + prefix);
            }

            logger.info("add namespace prefix : " + prefix + " ref : " + ref);
            factory.loadNamespacePrefix(prefix, ref);
        }
    }

    private void loadGraphConf(Element item) throws SLIB_Ex_Critic {


        logger.debug("Loading graph conf");

        GraphConf gconf = new GraphConf();

        // URI

        String uris = item.getAttribute("uri");
        uris = GenericConfBuilder.applyGlobalPatterns(uris);

        logger.debug("uri: " + uris);

        URI uri = factory.getURI(uris);
        gconf.setUri(uri);


        // Load Data
        String[] graphDataFileDefAtt = {"format", "path"};

        NodeList nListGdata = item.getElementsByTagName(XmlTags.DATA_TAG);

        if (nListGdata.getLength() == 1 && nListGdata.item(0) instanceof Element) {

            NodeList nListGConf = ((Element) nListGdata.item(0)).getElementsByTagName(XmlTags.FILE_TAG);

            for (int i = 0; i < nListGConf.getLength(); i++) {
                Element dataConf = (Element) nListGConf.item(i);

                Conf conf = GenericConfBuilder.build(dataConf);

                logger.debug("> data conf");

                // Data Format

                String format = conf.getParamAsString("format");
                logger.debug("- format: " + format);
                GFormat gFormat = null;

                if (format == null) {
                    throw new SLIB_Ex_Critic("Please precise a data format for each data to import, valids " + Arrays.toString(GFormat.values()));
                } else {
                    try {
                        gFormat = GFormat.valueOf(format.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new SLIB_Ex_Critic("Unknow data format " + format + ", valids " + Arrays.toString(GFormat.values()));
                    }
                }

                // Data Location

                String path = conf.getParamAsString("path");
                logger.debug("- path: " + path);

                GDataConf gDataConf = new GDataConf(gFormat, path);

                // Add Extra Parameters
                loadExtraParameters(conf, graphDataFileDefAtt, gDataConf);

                // Additional processing
                gDataConf = gDataConfAdditional(gFormat, dataConf, gDataConf);

                gconf.addGDataConf(gDataConf);

                logger.debug("");
            }
        } else {
            Util.error(XMLConstUtils.ERROR_NB_DATA_SPEC);
        }

        // Load Actions
        String[] graphActionDefAtt = {"type"};

        NodeList nListGactions = document.getElementsByTagName(XmlTags.ACTIONS_TAG);

        if (nListGactions.getLength() == 1 && nListGactions.item(0) instanceof Element) {

            NodeList nListGConf = ((Element) nListGactions.item(0)).getElementsByTagName(XmlTags.ACTION_TAG);

            for (int i = 0; i < nListGConf.getLength(); i++) {



                Element xmlConf = (Element) nListGConf.item(i);
                Conf conf = GenericConfBuilder.build(xmlConf);

                logger.debug("> action conf");

                String type = conf.getParamAsString("type");
                logger.debug("- type: " + type);

                GActionType gType = null;

                if (type == null) {
                    throw new SLIB_Ex_Critic("Please precise a type for each action to perform, valids " + Arrays.toString(GActionType.values()));
                } else {
                    try {
                        gType = GActionType.valueOf(type.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new SLIB_Ex_Critic("Unknow action type " + type + ", valids " + Arrays.toString(GActionType.values()));
                    }
                }

                GAction gAction = new GAction(gType);

                // Add Extra Parameters
                loadExtraParameters(conf, graphActionDefAtt, gAction);

                gconf.addGAction(gAction);
            }
        } else if (nListGactions.getLength() > 1) {
            Util.error(XMLConstUtils.ERROR_NB_ACTIONS_SPEC);
        }

        graphConfs.add(gconf);

    }

    private GDataConf gDataConfAdditional(GFormat gFormat, Element dataConf, GDataConf gDataConf) throws SLIB_Ex_Critic {

        if (gFormat == GFormat.CSV) {


            final Map<String, StatementTemplateElement> admittedStmConstraintElement = new HashMap<String, StatementTemplateElement>();
            {
                admittedStmConstraintElement.put("subject", StatementTemplateElement.SUBJECT);
                admittedStmConstraintElement.put("object", StatementTemplateElement.OBJECT);
            }

            final Map<String, StatementTemplate_Constraint_Type> admittedStmConstraintType = new HashMap<String, StatementTemplate_Constraint_Type>();
            {
                admittedStmConstraintType.put("EXISTS", StatementTemplate_Constraint_Type.EXISTS);
            }


            HashMap<Integer, CSV_Mapping> mappings = new HashMap<Integer, CSV_Mapping>();
            HashMap<Integer, CSV_StatementTemplate> stmtemplates = new HashMap<Integer, CSV_StatementTemplate>();

            // mappings

            NodeList list = dataConf.getElementsByTagName(XmlTags.MAP_TAG);

            for (int i = 0; i < list.getLength(); i++) {

                Element xmlConf = (Element) list.item(i);
                Conf conf = GenericConfBuilder.build(xmlConf);

                Integer field = Util.stringToInteger((String) conf.getParam(XmlTags.MAP_ATT_FIELD));
                String type = (String) conf.getParam(XmlTags.MAP_ATT_TYPE);
                String prefix = (String) conf.getParam(XmlTags.MAP_ATT_PREFIX);

                if (field == null) {
                    throw new SLIB_Ex_Critic("Cannot state field number associated to mapping definition in CSV configuration");
                }

                CSV_Mapping m = new CSV_Mapping(field, prefix);
                mappings.put(field, m);
            }

            gDataConf.addParameter("mappings", mappings);


            // statement template
            list = dataConf.getElementsByTagName(XmlTags.STM_TAG);

            for (int i = 0; i < list.getLength(); i++) {

                Element xmlConf = (Element) list.item(i);
                Conf conf = GenericConfBuilder.build(xmlConf);

                Integer s_id = Util.stringToInteger((String) conf.getParam(XmlTags.STM_ATT_SUBJECT));
                Integer o_id = Util.stringToInteger((String) conf.getParam(XmlTags.STM_ATT_OBJECT));

                String p_string = (String) conf.getParam(XmlTags.STM_ATT_PREDICATE);

                if (s_id == null) {
                    throw new SLIB_Ex_Critic("Cannot state number associated to subject statement template in CSV configuration");
                }

                if (o_id == null) {
                    throw new SLIB_Ex_Critic("Cannot state number associated to object statement template in CSV configuration");
                }

                if (p_string == null) {
                    throw new SLIB_Ex_Critic("Cannot state number associated to predicate statement template in CSV configuration");
                }

                URI p = null;

                if (admittedPType.containsKey(p_string)) {
                    p = admittedPType.get(p_string);
                } else {
                    p = factory.getURI(p_string);
                }


                CSV_StatementTemplate m = new CSV_StatementTemplate(s_id, o_id, p);

                // statement constraint
                NodeList listinner = dataConf.getElementsByTagName(XmlTags.STM_CONSTRAINT_TAG);

                for (int j = 0; j < listinner.getLength(); j++) {

                    Conf confinner = GenericConfBuilder.build((Element) listinner.item(j));
                    String element = (String) confinner.getParam(XmlTags.STM_CONSTRAINT_ATT_ELEMENT);
                    String typeString = (String) confinner.getParam(XmlTags.STM_CONSTRAINT_ATT_TYPE);

                    StatementTemplateElement elem = admittedStmConstraintElement.get(element);
                    StatementTemplate_Constraint_Type type = admittedStmConstraintType.get(typeString);

                    if (elem == null) {
                        throw new SLIB_Ex_Critic("Cannot state element " + element + " associated to statement constraint definition in CSV configuration, admitted " + admittedStmConstraintElement.keySet());
                    }
                    if (type == null) {
                        throw new SLIB_Ex_Critic("Cannot state type " + typeString + " associated to statement constraint definition in CSV configuration, admitted " + admittedStmConstraintType.keySet());
                    }

                    CSV_StatementTemplate_Constraint constraint = new CSV_StatementTemplate_Constraint(elem, type);
                    m.addConstraint(constraint);
                }

                stmtemplates.put(s_id, m);
            }

            gDataConf.addParameter("statementTemplates", stmtemplates);
        }

        return gDataConf;
    }

    private void loadExtraParameters(Conf conf, String[] restrictions, Parametrable p) {


        Map<String, Object> map = conf.getParams();

        for (String attName : map.keySet()) {


            String attValue = conf.getParamAsString(attName);

            boolean toLoad = true;
            for (String s : restrictions) {
                if (attName.equals(s)) {
                    toLoad = false;
                    break;
                }
            }

            if (!toLoad) {
                continue;
            }

            logger.debug("- " + attName + ": " + attValue);
            p.addParameter(attName, attValue);
        }
    }

    private void loadVariablesConf(Element item) throws SLIB_Ex_Critic {

        NodeList list = item.getElementsByTagName(XmlTags.VARIABLE_TAG);

        GlobalConfPattern userConf = GlobalConfPattern.getInstance();

        for (int i = 0; i < list.getLength(); i++) {

            Conf m = GenericConfBuilder.build((Element) list.item(i));

            String key = (String) m.getParam(XmlTags.KEY_ATTR);
            String value = (String) m.getParam(XmlTags.VALUE_ATTR);

            if (key == null) {
                throw new SLIB_Ex_Critic("Invalid " + XmlTags.VARIABLE_TAG + " tag, missing a " + XmlTags.KEY_ATTR + " attribut");
            } else if (value == null) {
                throw new SLIB_Ex_Critic("Invalid " + XmlTags.VARIABLE_TAG + " tag, missing a " + XmlTags.VALUE_ATTR + " attribut associated to variable " + key);
            }

            logger.info("add variable key : {" + key + "} value : " + value);
            userConf.addVar(key, value);
        }
    }

    private void loadFiltersConf(Element item) throws SLIB_Ex_Critic {

        NodeList list = item.getElementsByTagName(XmlTags.FILTER_TAG);
        LinkedHashSet<Conf> gConfGenerics = GenericConfBuilder.build(list);
        filters = buildFilters(gConfGenerics);

        for (Filter f : filters) {
            FilterRepository.getInstance().addFilter(f);
        }

    }

    private LinkedHashSet<Filter> buildFilters(LinkedHashSet<Conf> gConfGenerics) throws SLIB_Ex_Critic {

        for (Conf c : gConfGenerics) {

            Filter f = FilterBuilderGeneric.buildFilter(c);

            // check duplicate filter id
            for (Filter ft : filters) {
                if (ft.getId().equals(f.getId())) {
                    throw new SLIB_Ex_Critic("Duplicate id '" + f.getId() + "' found in filter specification");
                }
            }
            filters.add(f);
        }

        return filters;
    }

    private void extractOptConf(Conf gc) throws SLIB_Ex_Critic {

        String nbThread_s = (String) gc.getParam(XmlTags.OPT_NB_THREADS_ATTR);
        if (nbThread_s != null) {
            try {
                int nbThreads = Integer.parseInt(nbThread_s);
                ThreadManager.getSingleton().setMaxThread(nbThreads);
            } catch (NumberFormatException e) {
                throw new SLIB_Ex_Critic("Error converting " + XmlTags.OPT_NB_THREADS_ATTR + " to integer value ");
            }
        }
    }

    /**
     *
     * @return the linked list of configurations.
     */
    public LinkedList<GraphConf> getGraphConfs() {
        return graphConfs;
    }
}
