package slib.sglib.io.loader.csv;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.GraphLoader;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.impl.graph.elements.Vertex;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.Util;

/**
 *
 * @author seb
 */
public class GraphLoader_CSV implements GraphLoader {

    boolean skipHeader = false;
    DataFactoryMemory dataRepo = DataFactoryMemory.getSingleton();
    HashMap<Integer, CSV_Mapping> mappings = new HashMap<Integer, CSV_Mapping>();
    HashMap<Integer, CSV_StatementTemplate> statementTemplates = new HashMap<Integer, CSV_StatementTemplate>();
    Pattern pattern = null; // the one used
    G g;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * @param id
     * @param type
     * @param prefix
     */
    public void addMapping(int id, VType type, String prefix) {

        if (prefix == null) {
            prefix = "";
        }

        mappings.put(id, new CSV_Mapping(id, type, prefix));
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

    public G load(GraphConf conf) throws SLIB_Exception {
        return GraphLoaderGeneric.load(conf);
    }

    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        logger.debug("Loading CSV.");

        this.g = g;

        loadConf(conf);
        loadCSV(conf.getLoc());
        logger.debug("CSV specification loaded.");
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

        HashMap<Integer, CSV_Mapping> mappings = (HashMap<Integer, CSV_Mapping>) conf.getParameter("mappings");
        HashMap<Integer, CSV_StatementTemplate> statementTemplates = (HashMap<Integer, CSV_StatementTemplate>) conf.getParameter("statementTemplates");



        if (mappings != null) {
            this.mappings.putAll(mappings);
        }

        if (statementTemplates != null) {
            this.statementTemplates.putAll(statementTemplates);
        }

        if (this.mappings.isEmpty()) {
            throw new SLIB_Ex_Critic("Please specify a mapping for CSV loader");
        }

        if (this.statementTemplates.size() == 0) {
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



        V subject = buildVertex(t.src_id, data);
        V object = buildVertex(t.target_id, data);

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
            g.addE(subject, object, t.predicate);
            return true;
        }
        return false;
    }

    private V buildVertex(int id, String[] data) throws SLIB_Ex_Critic {

        CSV_Mapping vmap = mappings.get(id);

        if (vmap == null || data.length - 1 < id) {
            throw new SLIB_Ex_Critic("Cannot load statement considering the given configuration. Error parsing " + Arrays.toString(data));
        }

        String uriAsString = data[id];

        if (vmap.prefix != null) {
            uriAsString = vmap.prefix + uriAsString;
        }

        V v = new Vertex(dataRepo.createURI(uriAsString), vmap.type);

        return v;
    }
}
