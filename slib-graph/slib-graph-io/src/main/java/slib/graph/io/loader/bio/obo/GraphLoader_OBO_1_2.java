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
package slib.graph.io.loader.bio.obo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.graph.io.conf.GDataConf;
import slib.graph.io.conf.GraphConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.io.loader.bio.obo.utils.OboRelationship;
import slib.graph.io.loader.bio.obo.utils.OboTerm;
import slib.graph.io.loader.bio.obo.utils.OboType;
import slib.graph.model.graph.G;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.ex.SLIB_Ex_Warning;
import slib.utils.impl.OBOconstants;

/**
 * <a href="http://www.geneontology.org/GO.format.obo-1_2.shtml">OBO
 * specification</a> <br/>
 *
 * Compatibility with other format-version than 1.2 is not supported. <br/>
 *
 * <b> The mapping consider </b> <br/> <ul> <li>[Term] as RDF/OWL class mapping
 * to build vertices </li> <li>[Typedef] as relationships definitions between
 * nodes </li> <li>"is_a" as the only predefined relationType </li>
 * <li>[instance] are not loaded </li> </ul> Meta informations: <br/> <ul> <li>
 * format-version : Only required argument of the specification expect 1.2 throw
 * non warning exception (<SGTK_Exception_Warning>) if 1.2 version is not
 * detected </li> <li> default-namespace: the graph URI </li> </ul>
 *
 * <b> [Term] </b>	<br/>
 *
 * Each Term are loaded as a specific vertex in the graph. <br/> A Term is then
 * mapped as a <Vertex> of <IVertexType> equals to <Constants> V_TYPE_CLASS.
 * <br/> Note that obsolete Terms/Typedefs are excluded during graph
 * construction (see below is_obsolete). <br/> <br/>
 *
 * Flag considered : <br/>
 *
 * <ul> <li> id: <URI> Loaded as the URI of the current [Term/Class/Vertex]
 * specification </li> <li> is a: <URI> Loaded as an <Edge> of <EdgeType>
 * <Constants> IS_A with currently defined Vertex as source and specified <URI>
 * as target Vertex </li> <li> relationships: <EdgeType> <URI> Loaded as an
 * <Edge> of specified <EdgeType> with source as current Vertex and target
 * specified by the precise <URI> The EdgeType have to be defined by a [TypeDef]
 * specification to be taken into account. </li>
 *
 * <li> is_obsolete: <boolean> Specifying the validity of the current Term
 * specification </li> <li> intersection_of/XREF...: These information are not
 * loaded. </li> </ul>
 *
 * <b>	[Typedef] as <EdgeType> </b> <br/>
 *
 * Every Typedef definition is loaded as an <EdgeType>. <br/> This definition is
 * used to specify the type authorized for <Edge> specification <br/>
 *
 * <ul> <li> id: <URI> the URI of the <EdgeType> </li>
 *
 * <li> is_transitive: <boolean> Specifying if the relationship have to be
 * considered as transitive </li>
 *
 * <li> inverse_of <URI> Used to defined the URI of the <EdgeType> to consider
 * as the inverse of the current specified EdgeType e.g if an edge type X is
 * defined as the inverse of Y, a relationship of type X defined between A and B
 * will lead to the creation of: <ul> <li> an edge of type X betwween v(A) ->
 * v(B) </li> <li> an edge of type Y betwween v(B) -> v(A) </li> </ul> </li>
 *
 * <li> is_symmetric <boolean> Used to defined an EdgeType as the inverse of
 * itself e.g if an edge type X is transitive, a relationship defined as an edge
 * of type X betwween v(A) -> v(B) is defined will imply the creation of an edge
 * of type X as v(B) -> v(A) </li>
 *
 * <li> is_obsolete: <boolean> Specifying the validity of the current Typedef
 * specification </li>
 *
 * <li> transitive_over/XREF...: These information are not loaded. </li> </ul>
 * TODO : load instances
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphLoader_OBO_1_2 implements GraphLoader {

    URIFactoryMemory data = URIFactoryMemory.getSingleton();
    GraphConf conf;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    boolean onTermSpec = false;
    boolean onTypeDef = false;
    G g;
    URI graphURI;
    String filepath;
    String defaultNamespace;
    final String format_parser = "1.2";
    String format_version = "undefined";
    boolean allow_all_gafVersion;
    HashMap<String, OboTerm> oboTerms;
    HashMap<String, OboType> oboTypes;
    HashMap<String, String> inverseRel;
    OboTerm oboTermCurrent = null;
    OboType oboTypeCurrent = null;
    Pattern colon = Pattern.compile(":");
    Pattern exclamation = Pattern.compile("!");
    Pattern spaces = Pattern.compile("\\s+");

    private void init(G g, String file, String defaultNamespace) {

        this.g = g;
        this.graphURI = g.getURI();

        this.filepath = file;

        this.defaultNamespace = defaultNamespace;

        format_version = "undefined";

        oboTerms = new HashMap<String, OboTerm>();
        oboTypes = new HashMap<String, OboType>();

        inverseRel = new HashMap<String, String>();

        oboTermCurrent = null;
        oboTypeCurrent = null;
    }

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        String defaultNamespaceVal = (String) conf.getParameter("default-namespace");

        if (defaultNamespaceVal == null) {
            defaultNamespace = g.getURI().getNamespace();
            logger.info("OBO loader set default-namespace " + defaultNamespace);
        } else {
            defaultNamespace = defaultNamespaceVal;
        }

        init(g, conf.getLoc(), defaultNamespace);

        logger.info("-------------------------------------");
        logger.info("Loading OBO specification from:" + filepath);
        logger.info("-------------------------------------");

        loadOboSpec();

        logger.info("OBO specification loaded.");
        logger.info("-------------------------------------");
    }

    private void loadOboSpec() throws SLIB_Exception {

        try {

            FileInputStream fstream = new FileInputStream(filepath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            boolean metadataLoaded = false;

            String line, flag, value;

            String subClassOfURI = RDFS.SUBCLASSOF.stringValue();

            String[] data;

            String gNamespace = g.getURI().getNamespace();

            while ((line = br.readLine()) != null) {

                flag = null;
                value = null;
                data = null;

                line = line.trim();

                if (!metadataLoaded) { // loading OBO meta data

                    if (line.equals(OBOconstants.TERM_FLAG) || line.equals(OBOconstants.TYPEDEF_FLAG)) {

                        metadataLoaded = true;

                        // check format-version 
                        if (!format_version.equals(format_parser) && !allow_all_gafVersion) {
                            throw new SLIB_Ex_Warning("Parser of format-version '" + format_parser + "' used to load OBO version '" + format_version + "'");
                        }

                        if (line.equals(OBOconstants.TERM_FLAG)) {
                            onTermSpec = true;
                        } else {
                            onTypeDef = true;
                        }
                    } else {
                        data = getDataColonSplit(line);

                        if (data != null) {

//							if(data[0].equals(OBOconstants.DEF_NAMESPACE_FLAG))
//								defaultNamespace = data[1];
//
//							else 
                            if (data[0].equals(OBOconstants.FORMAT_VERSION_FLAG)) {
                                format_version = data[1];
                            }
                        }
                    }
                } else {
                    if (onTermSpec) { // loading [Term]

                        checkLine(line);

                        if (onTermSpec) {

                            data = getDataColonSplit(line);

                            if (data == null || data.length != 2) {
                                continue;
                            }

                            flag = data[0];
                            value = data[1];

                            if (flag.equals(OBOconstants.TERM_ID_FLAG)) { // id

                                oboTermCurrent = new OboTerm();
                                oboTermCurrent.setURIstring(buildURI(value));
                            } else if (flag.equals(OBOconstants.ISA_FLAG)) { // is_a
                                oboTermCurrent.addRel(subClassOfURI, buildURI(value));
                            } // is_obsolete:
                            else if (flag.equals(OBOconstants.OBSOLETE_FLAG)) { // is_obsolete

                                if (value.equals("true")) {
                                    oboTermCurrent.setObsolete(true);
                                }
                            } else if (flag.equals(OBOconstants.RELATIONSHIP_FLAG)) { // relationship

                                String[] datasub = spaces.split(value);

                                String relType = buildURI(datasub[0].trim());
                                String targetURI = buildURI(datasub[1].trim());

                                oboTermCurrent.addRel(relType, targetURI);
                            }

                        }
                    } else if (onTypeDef) { // Loading [TypeDef]

                        checkLine(line);

                        if (onTypeDef) {

                            data = getDataColonSplit(line);

                            if (data == null || data.length != 2) {
                                continue;
                            }

                            flag = data[0];
                            value = data[1];

                            // id:
                            if (flag.equals(OBOconstants.TYPEDEF_ID_FLAG)) {
                                oboTypeCurrent = new OboType(buildURI(value));
                            } // is_transitive:
                            else if (flag.equals(OBOconstants.TYPEDEF_ISTRANSIVE_FLAG)) {

                                if (value.equals("true")) {
                                    oboTypeCurrent.setTransitivity(true);
                                }
                            } // inverse_of:
                            else if (flag.equals(OBOconstants.TYPEDEF_INVERSE_OF_FLAG)) {

                                String uri_opp = buildURI(value);

                                setOppositeRel(oboTypeCurrent.getURIstring(), uri_opp);
                                setOppositeRel(uri_opp, oboTypeCurrent.getURIstring());
                            } // is_symmetric:
                            else if (flag.equals(OBOconstants.TYPEDEF_SYMMETRIC_FLAG)) {

                                if (value.equals("true")) {
                                    oboTypeCurrent.setSymmetricity(true);
                                    setOppositeRel(oboTypeCurrent.getURIstring(), oboTypeCurrent.getURIstring());
                                }
                            } // is_obsolete:
                            else if (flag.equals(OBOconstants.OBSOLETE_FLAG)) {

                                if (value.equals("true")) {
                                    oboTypeCurrent.setObsolete(true);
                                }
                            }
                        }
                    }
                }
            }
            handleElement();

            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        loadGraph();

        logger.info("OBO Loading ok.");
    }

    private String buildURI(String value) throws SLIB_Ex_Critic {

        String info[] = getDataColonSplit(value);

        if (info != null && info.length == 2) {

            String ns = data.getNamespace(info[0]);
            if (ns == null) {
                throw new SLIB_Ex_Critic("No namespace associated to prefix " + info[0] + ". Cannot load " + value + ", please load required namespace prefix");
            }

            return ns + info[1];
        } else {
            return defaultNamespace + value;
        }
    }

    private void checkLine(String line) throws SLIB_Ex_Critic {

        if (line.equals(OBOconstants.TERM_FLAG)) {
            handleElement();
            onTermSpec = true;
            onTypeDef = false;
        } else if (line.equals(OBOconstants.TYPEDEF_FLAG)) {
            handleElement();
            onTermSpec = false;
            onTypeDef = true;
        }
    }

    private void handleElement() throws SLIB_Ex_Critic {

        if (onTermSpec) {
            handleTerm();
        } else if (onTypeDef) {
            handleTypeDef();
        }
    }

    private void setOppositeRel(String uri, String oppositeURI) throws SLIB_Ex_Critic {

        // Check if opposite have already been specified
        // and that is opposite is not the one we try to specify
        if (inverseRel.containsKey(uri)
                && !inverseRel.get(uri).equals(oppositeURI)) {

            String error = "\nError trying to set [Typedef] '" + uri + "' inverse as '" + oppositeURI + "'"
                    + " because '" + inverseRel.get(uri) + "'"
                    + " was already set as it inverse.\n"
                    + "Please correct [Typedef] '" + uri + "' & "
                    + "[Typedef] '" + oppositeURI + "' specification.";

            throw new SLIB_Ex_Critic(error);
        }
        inverseRel.put(uri, oppositeURI);
    }

    private void handleTerm() throws SLIB_Ex_Critic {

        if (onTermSpec) {

            if (oboTerms.containsKey(oboTermCurrent.getURIstring())) {
                throw new SLIB_Ex_Critic("Duplicate entry for [Term] " + oboTermCurrent.getURIstring());
            }

            oboTerms.put(oboTermCurrent.getURIstring(), oboTermCurrent);
            oboTermCurrent = new OboTerm();
        }
    }

    private void handleTypeDef() throws SLIB_Ex_Critic {

        if (onTypeDef) {
            if (oboTypes.containsKey(oboTypeCurrent.getURIstring())) {
                throw new SLIB_Ex_Critic("Duplicate entry for [Typedef] " + oboTypeCurrent.getURIstring());
            }

            oboTypes.put(oboTypeCurrent.getURIstring(), oboTypeCurrent);
            oboTermCurrent = new OboTerm();
        }
    }

    private String[] getDataColonSplit(String line) {

        if (line.isEmpty()) {
            return null;
        }

        String data[] = colon.split(exclamation.split(line, 2)[0], 2);
        data[0] = data[0].trim();

        if (data.length > 1) {
            data[1] = data[1].trim();
        }

        return data;
    }

    /**
     * Create the graph considering information loaded
     *
     * @throws SGL_Exception
     */
    private void loadGraph() throws SLIB_Exception {

        // - create vertices -----------------------------------------------
        int nbObsolete = 0;
        int nbObsoleteTypeDef = 0;

        for (Entry<String, OboTerm> e : oboTerms.entrySet()) {

            if (!e.getValue().isObsolete()) {

                URI termURI = data.getURI(e.getKey());
                g.addV(termURI);
            } else {
                nbObsolete++;
            }
        }

        Set<String> obsoletesETypes = new HashSet<String>();

        // create  Edge Type and inverse
        for (Entry<String, OboType> e : oboTypes.entrySet()) {

            String eTypeUriString;

            eTypeUriString = e.getKey();
            OboType type = e.getValue();

            if (type.isObsolete()) {
                nbObsoleteTypeDef++;
                obsoletesETypes.add(eTypeUriString);
                continue;
            }
        }

        // create  Edge Type and inverse only for non obsolete relationships
        for (Entry<String, OboTerm> entry : oboTerms.entrySet()) {

            OboTerm t = entry.getValue();

            if (!t.isObsolete()) {

                for (OboRelationship r : t.getRelationships()) {

                    String typeString = r.getTypeUriString();

                    if (!obsoletesETypes.contains(typeString)) {

                        URI srcURI = data.getURI(t.getURIstring());
                        URI targetURI = data.getURI(r.getTargetUriString());
                        URI type = data.getURI(typeString);

                        g.addV(targetURI); // we ensure the target exists
                        g.addE(srcURI, type, targetURI);
                    }
                }
            }
        }

        logger.info("Term specified : " + oboTerms.size());
        logger.info("skipping " + nbObsolete + " obsolete Terms");
        if (nbObsoleteTypeDef != 0) {
            logger.info("skipping " + nbObsoleteTypeDef + " obsolete Type Def");
        }
    }

    /**
     *
     * @return true if all GAF version are supported.
     */
    public boolean isAllow_all_gafVersion() {
        return allow_all_gafVersion;
    }

    /**
     *
     * @param allow_all_gafVersion
     */
    public void setAllow_all_gafVersion(boolean allow_all_gafVersion) {
        this.allow_all_gafVersion = allow_all_gafVersion;
    }
}
