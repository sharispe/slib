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
package slib.graph.io.loader.annot;

import au.com.bytecode.opencsv.CSVReader;
import java.io.FileReader;
import java.util.regex.Pattern;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.impl.graph.elements.Edge;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Util;

/**
 * Class used to load annotation file:  <code>
 * XXXX1[TAB]c1;c2;c3
 * XXXX2[TAB]c2;c3
 * ...
 * </code>
 *
 * To each line is associated an entry defining a key and a set of annotations
 * separated by ';'. As an example the first line XXXX1[TAB]c1;c2;c3 will lead
 * to the creation of three statements: XXXXX1 RDF.TYPE c1 <br/>
 * XXXXX1 RDF.TYPE c2 <br/>
 * XXXXX1 RDF.TYPE c3 <br/>
 * By default the semantic relationship is RDF.TYPE. Prefixes can also be
 * defined for the subjects and the objects of the statements created.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphLoader_TSVannot implements GraphLoader {

    public final static String PARAM_HEADER = "header";
    public final static String PARAM_PREFIX_SUBJECT = "prefixSubject";
    public final static String PARAM_PREFIX_OBJECT = "prefixObject";
    public final static String PARAM_PREDICATE = "predicate";
    boolean skipHeader = false;
    URIFactoryMemory uriRepo = URIFactoryMemory.getSingleton();
    Character pattern = '\t'; // the one used
    G g;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    URI predicate = RDF.TYPE;
    String prefixSubject = null;
    String prefixObject = null;
    Pattern colon = Pattern.compile(":");

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        logger.info("-------------------------------------");
        logger.info("Loading Annotations using TSV loader.");
        logger.info("-------------------------------------");

        this.g = g;

        loadConf(conf);
        loadTSV(conf.getLoc());
        logger.info("TSV specification loaded.");
        logger.info("-------------------------------------");
    }

    private void loadConf(GDataConf conf) throws SLIB_Ex_Critic {

        String header = (String) conf.getParameter(PARAM_HEADER);
        String prefixSubjectParam = (String) conf.getParameter(PARAM_PREFIX_SUBJECT);
        String prefixObjectParam = (String) conf.getParameter(PARAM_PREFIX_OBJECT);
        String predicateParam = (String) conf.getParameter(PARAM_PREDICATE);

        if (header == null || Util.stringToBoolean(header) == true) {
            skipHeader = true;
        }

        if (predicateParam != null) {
            try {
                predicate = uriRepo.getURI(predicateParam);
            } catch (IllegalArgumentException e) {
                throw new SLIB_Ex_Critic("Error in data loader, parameter " + PARAM_PREDICATE + ", cannot create an URI from " + predicateParam + "\n" + e.getMessage());
            }
        }

        if (prefixSubjectParam != null) {
            prefixSubject = prefixSubjectParam;
        }

        if (prefixObjectParam != null) {
            prefixObject = prefixObjectParam;
        }

        logger.info("file            " + conf.getLoc());
        logger.info("Skipping header " + skipHeader);
        logger.info("predicate       '" + predicate + "'");
        if (prefixSubject != null) {
            logger.info("prefix subject  '" + prefixSubject + "'");
        }
        if (prefixObject != null) {
            logger.info("prefix object   '" + prefixObject + "'");
        }

    }

    public void loadTSV(String fileLoc) throws SLIB_Ex_Critic {

        try {
            CSVReader csvReader = new CSVReader(new FileReader(fileLoc), '\t');
            String[] row;
            String subjectLocalName;
            String[] data;

            URI s, o;
            int skipped = 0;
            int statementsLoaded = 0;
            int processed = 0;

            while ((row = csvReader.readNext()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                } else if (row.length == 2) {

                    subjectLocalName = row[0];
                    data = row[1].split(";");

                    if (prefixSubject == null) {
                        s = uriRepo.getURI(subjectLocalName);
                    } else {
                        s = uriRepo.getURI(prefixSubject + subjectLocalName);
                    }

                    for (String os : data) {

                        if (prefixObject == null) {
                            o = uriRepo.getURI(buildURIString(os));
                        } else {
                            o = uriRepo.getURI(prefixObject + os);
                        }

                        E edge = new Edge(s, predicate, o);
                        g.addE(edge);
                        statementsLoaded++;
                    }
                    processed++;

                } else {
                    skipped++;
                }
            }
            csvReader.close();

            logger.info("Number of lines skipped   " + skipped);
            logger.info("Number of lines processed " + processed);
            logger.info("Number of statements loaded " + statementsLoaded);

        } catch (Exception e) {
            throw new SLIB_Ex_Critic("Error processing file " + fileLoc + "\n" + e.getMessage());
        }
    }

    private String buildURIString(String value) throws SLIB_Ex_Critic {

        String info[] = getDataColonSplit(value);

        if (info != null && info.length == 2) {

            String ns = uriRepo.getNamespace(info[0]);
            if (ns == null) {
                throw new SLIB_Ex_Critic("No namespace associated to prefix " + info[0] + ". Cannot load " + value + ", please load required namespace prefix");
            }

            return ns + info[1];
        } else {
            return value;
        }
    }

    private String[] getDataColonSplit(String value) {

        String data[] = colon.split(value);
        data[0] = data[0].trim();

        if (data.length > 1) {
            data[1] = data[1].trim();
        }
        return data;
    }
}
