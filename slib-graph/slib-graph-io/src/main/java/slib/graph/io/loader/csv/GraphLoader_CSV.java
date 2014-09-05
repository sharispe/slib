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
package slib.graph.io.loader.csv;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.model.graph.G;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Util;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphLoader_CSV implements GraphLoader {

    boolean skipHeader = false;
    URIFactoryMemory dataRepo = URIFactoryMemory.getSingleton();
    Map<Integer, CSV_Mapping> mappings = new HashMap<Integer, CSV_Mapping>();
    Map<Integer, CSV_StatementTemplate> statementTemplates = new HashMap<Integer, CSV_StatementTemplate>();
    Pattern pattern = null; // the one used
    G g;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * @param id
     * @param prefix
     */
    public void addMapping(int id, String prefix) {

        if (prefix == null) {
            prefix = "";
        }

        mappings.put(id, new CSV_Mapping(id, prefix));
    }

    /**
     *
     * @param src_id
     * @param target_id
     * @param predicate_URI
     */
    public void addStatementTemplate(int src_id, int target_id, URI predicate_URI) {

        assert predicate_URI != null;

        statementTemplates.put(src_id, new CSV_StatementTemplate(src_id, target_id, predicate_URI));
    }

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        logger.info("-------------------------------------");
        logger.info("Loading CSV.");
        logger.info("-------------------------------------");

        this.g = g;

        loadConf(conf);
        loadCSV(conf.getLoc());
        logger.info("CSV specification loaded.");
        logger.info("-------------------------------------");
    }

    private void loadConf(GDataConf conf) throws SLIB_Ex_Critic {

        String header = (String) conf.getParameter("header");

        if (header == null || Util.stringToBoolean(header) == true) {
            skipHeader = true;
        }

        logger.info("Skipping header " + skipHeader);


        String separator = (String) conf.getParameter("separator");

        if (separator == null) {
            pattern = Pattern.compile("\\t");
        } else {
            pattern = Pattern.compile(separator);
        }

        HashMap<Integer, CSV_Mapping> mappingsLocal = (HashMap<Integer, CSV_Mapping>) conf.getParameter("mappings");
        HashMap<Integer, CSV_StatementTemplate> statementTemplatesLocal = (HashMap<Integer, CSV_StatementTemplate>) conf.getParameter("statementTemplates");



        if (mappingsLocal != null) {
            this.mappings.putAll(mappingsLocal);
        }

        if (statementTemplatesLocal != null) {
            this.statementTemplates.putAll(statementTemplatesLocal);
        }

        if (this.mappings.isEmpty()) {
            throw new SLIB_Ex_Critic("Please specify a mapping for CSV loader");
        }

        if (this.statementTemplates.isEmpty()) {
            throw new SLIB_Ex_Critic("Please specify a statement template for CSV loader");
        }

    }

    private void loadCSV(String filepath) throws SLIB_Exception {

        long evaluated = 0; // number of statements evaluated according to the templates defined
        long rejected = 0; // those excluded due to specified constraints.

        try {

            FileInputStream fstream = new FileInputStream(filepath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));



            String line;

            String[] data;

            while ((line = br.readLine()) != null) {

                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }

                line = line.trim();
                data = pattern.split(line);

                for (CSV_StatementTemplate t : statementTemplates.values()) {

                    if (!buildStatement(t, data)) {
                        rejected++;
                    }
                    evaluated++;
                }


            }
            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }

        logger.info("Number of statements rejected due to constraint: " + rejected + "/" + evaluated);
        logger.info("CSV Loading ok.");
    }

    /**
     *
     * @param t
     * @param data
     * @return boolean success.
     * @throws SGL_Ex_Critic
     */
    private boolean buildStatement(CSV_StatementTemplate t, String[] data) throws SLIB_Ex_Critic {



        URI subject = buildURI(t.src_id, data);
        URI object = buildURI(t.target_id, data);

        boolean valid = true;

        for (CSV_StatementTemplate_Constraint c : t.constraints) {

            // Check existence of an element of the statement
            if (c.type == StatementTemplate_Constraint_Type.EXISTS) {

                if (c.onElement == StatementTemplateElement.SUBJECT && !g.containsVertex(subject)) {
                    valid = false;
                }

                if (c.onElement == StatementTemplateElement.OBJECT && !g.containsVertex(object)) {
                    valid = false;
                }

            }
            if (!valid) {
                break;
            }
        }
        if (valid) {
            g.addE(subject, t.predicate, object);
            return true;
        }
        return false;
    }

    private URI buildURI(int id, String[] data) throws SLIB_Ex_Critic {

        CSV_Mapping vmap = mappings.get(id);

        if (vmap == null || data.length - 1 < id) {
            throw new SLIB_Ex_Critic("Cannot load statement considering the given configuration. Error parsing " + Arrays.toString(data));
        }

        String uriAsString = data[id];

        if (vmap.prefix != null) {
            uriAsString = vmap.prefix + uriAsString;
        }

        return dataRepo.getURI(uriAsString);
    }
}
