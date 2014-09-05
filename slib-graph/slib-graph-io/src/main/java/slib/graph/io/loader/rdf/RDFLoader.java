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
package slib.graph.io.loader.rdf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import org.openrdf.rio.ParserConfig;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.ntriples.NTriplesParser;
import org.openrdf.rio.rdfxml.RDFXMLParser;
import org.openrdf.rio.turtle.TurtleParser;
import org.openrdf.sail.memory.model.MemValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
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

        buildRDFparser(format);

    }

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        loadConf(conf);

        logger.info("-------------------------------------");
        logger.info(" RDF Loader");
        logger.info("-------------------------------------");
        logger.info("Populate graph " + g.getURI() + " from " + conf.getLoc());
        load(g, conf.getLoc());
        logger.info("Graph " + g.getURI() + " populated by RDF data ");
        logger.info("-------------------------------------");

    }

    private void loadConf(GDataConf conf) throws SLIB_Ex_Critic {

        GFormat format = conf.getFormat();
        if (format == GFormat.RDF_XML) {
            buildRDFparser(RDFFormat.RDFXML);
        } else if (format == GFormat.NTRIPLES) {
            buildRDFparser(RDFFormat.NTRIPLES);
        } else if (format == GFormat.TURTLE) {
            buildRDFparser(RDFFormat.TURTLE);
        } else {
            throw new SLIB_Ex_Critic("Unsupported RDF format " + format);
        }
    }

    private void buildRDFparser(RDFFormat format) throws SLIB_Ex_Critic {
        if (format.equals(RDFFormat.NTRIPLES)) {
            parser = new NTriplesParser(new MemValueFactory());
        } else if (format.equals(RDFFormat.RDFXML)) {
            parser = new RDFXMLParser(new MemValueFactory());
        } else if (format.equals(RDFFormat.TURTLE)) {
            parser = new TurtleParser(new MemValueFactory());
        } else {
            throw new SLIB_Ex_Critic("Unsupported RDF format " + format);
        }

        ParserConfig config = new ParserConfig();

//        Set<RioSetting<?>> set = new HashSet();
//        set.add(BasicParserSettings.VERIFY_DATATYPE_VALUES);
//        set.add(BasicParserSettings.VERIFY_RELATIVE_URIS);
//        config.setNonFatalErrors(set);
        parser.setParserConfig(config);
    }

    /**
     *
     * @param g
     * @param file
     * @throws SLIB_Ex_Critic
     */
    public void load(G g, String file) throws SLIB_Ex_Critic {

        try {
            load(g, new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        } catch (SLIB_Ex_Critic e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    
    public void load(G g, InputStream inputStream) throws SLIB_Ex_Critic {

        RDFHandler rdfHandler = new SlibRdfHandler(g);
        
        try {
            logger.info("Parser loaded for: " + parser.getRDFFormat());
            parser.setRDFHandler(rdfHandler);
            logger.info("Parsing RDF file...");
            parser.parse(inputStream, "");

        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        } catch (RDFParseException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        } catch (RDFHandlerException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }

    /**
     * @param g
     * @param file
     * @param format
     * @throws SLIB_Ex_Critic
     */
    public void load(G g, String file, RDFFormat format) throws SLIB_Ex_Critic {
        buildRDFparser(format);
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
