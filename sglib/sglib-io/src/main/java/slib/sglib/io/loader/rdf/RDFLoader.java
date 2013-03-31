package slib.sglib.io.loader.rdf;

import java.io.FileReader;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.GraphLoader;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author seb
 */
public class RDFLoader implements GraphLoader {

    RDFParser parser = null;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     */
    public RDFLoader() {
    }

    /**
     *
     * @param format
     * @throws SLIB_Ex_Critic
     */
    public RDFLoader(RDFFormat format) throws SLIB_Ex_Critic {

        loadFormat(format);

    }

    public G load(GraphConf conf) throws SLIB_Exception {
        return GraphLoaderGeneric.load(conf);
    }

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        loadConf(conf);


        logger.info("Populate graph " + g.getURI()+" from "+conf.getLoc());
        load(g, conf.getLoc());
        logger.info("Graph " + g.getURI() + " populated by RDF data ");

    }

    private void loadConf(GDataConf conf) throws SLIB_Ex_Critic {

        GFormat format = conf.getFormat();
        if (format == GFormat.RDF_XML) {
            loadFormat(RDFFormat.RDFXML);
        } else if (format == GFormat.NTRIPLES) {
            loadFormat(RDFFormat.NTRIPLES);
        } 
        else if (format == GFormat.TURTLE) {
            loadFormat(RDFFormat.TURTLE);
        }
        else {
            throw new SLIB_Ex_Critic("Unsupported RDF format " + format);
        }
    }

    private void loadFormat(RDFFormat format) throws SLIB_Ex_Critic {
        if (format.equals(RDFFormat.NTRIPLES)) {
            parser = new NTriplesParser(new MemValueFactory());
        } else if (format.equals(RDFFormat.RDFXML)) {
            parser = new RDFXMLParser(new MemValueFactory());
            parser.setStopAtFirstError(false);
        } 
        else if (format.equals(RDFFormat.TURTLE)) {
            parser = new TurtleParser(new MemValueFactory());
            //parser.setStopAtFirstError(false);
        }
        else {
            throw new SLIB_Ex_Critic("Unsupported RDF format " + format);
        }
    }

    /**
     *
     * @param g
     * @param file
     * @throws SLIB_Ex_Critic
     */
    public void load(G g, String file) throws SLIB_Ex_Critic {


        RDFHandler rdfHandler = new SlibRdfHandler(g);
        try {
            parser.setRDFHandler(rdfHandler);
            FileReader reader = new FileReader(file);
            //BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file))));
            logger.info("Parsing RDF file...");
            parser.parse(reader, "");
        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     *
     * @param g
     * @param file
     * @param format
     * @throws SLIB_Ex_Critic
     */
    public void load(G g, String file, RDFFormat format) throws SLIB_Ex_Critic {

        loadFormat(format);
        load(g, file);
    }

    /**
     *
     * @param g
     * @param rdfFileConf
     * @throws SLIB_Ex_Critic
     */
    public void load(G g, Map<String, RDFFormat> rdfFileConf) throws SLIB_Ex_Critic {

        for (Entry<String, RDFFormat> e : rdfFileConf.entrySet()) {
            load(g, e.getKey(), e.getValue());
        }
    }
}
