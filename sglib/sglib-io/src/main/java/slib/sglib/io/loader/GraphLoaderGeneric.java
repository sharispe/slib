/*

 Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

 This software is a computer program whose purpose is to 
 process semantic graphs.

 This software is governed by the CeCILL  license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 */
package slib.sglib.io.loader;

import java.util.Collection;

import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.graph.utils.GraphActionExecutor;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.bio.gaf2.GraphLoader_GAF_2;
import slib.sglib.io.loader.bio.mesh.GraphLoader_MESH_XML;
import slib.sglib.io.loader.bio.obo.GraphLoader_OBO_1_2;
import slib.sglib.io.loader.bio.snomedct.GraphLoaderSnomedCT_RF2;
import slib.sglib.io.loader.csv.GraphLoader_CSV;
import slib.sglib.io.loader.rdf.RDFLoader;
import slib.sglib.io.loader.slibformat.GraphLoader_SLIB;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.GraphRepositoryMemory;
import slib.sglib.model.repo.URIFactory;
import slib.sglib.model.impl.repo.URIFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * Graph Loader is a class used to facilitate graph loading.
 *
 * @author Harispe Sébastien
 */
public class GraphLoaderGeneric {

    public static Logger logger = LoggerFactory.getLogger(GraphLoaderGeneric.class);
    /**
     * The formats currently supported by the generic loader.
     */
    public static GFormat[] supportedFormat = {
        GFormat.OBO, GFormat.GAF2, GFormat.NTRIPLES, GFormat.RDF_XML,
        GFormat.RDF_XML, GFormat.SNOMED_CT_RF2, GFormat.MESH_XML, GFormat.CSV
    };

    /**
     * Populate a given graph considering a configuration.
     *
     * @param dataConf the object defining the configuration of the data to load
     * @param g the graph to populate with the data
     * @return the graph which have been populated. The treatment is performed
     * in place, i.e. no extra graph is created and the reference to the graph
     * returned will be the same as the one passed in parameter.
     *
     * @throws SLIB_Exception if an error is encountered during loading.
     */
    public static G populate(GDataConf dataConf, G g) throws SLIB_Exception {

        logger.debug("Populate " + g.getURI() + " based on " + dataConf.getLoc());

        GraphLoader gLoader = getLoader(dataConf);

        gLoader.populate(dataConf, g);

        return g;
    }

    /**
     * Create a graph and register it.
     *
     * @param uri the URI of the graph
     * @return the created graph.
     */
    public static G createGraph(URI uri) {

        logger.debug("Create graph " + uri);

        G g = new GraphMemory(uri);
        GraphRepositoryMemory.getSingleton().registerGraph(g);
        return g;
    }

    /**
     * Build the graph considering the given configuration.
     *
     * Add the loaded graph to the {@link DataRepository}.
     *
     * @param graphConf the graph configuration
     * @return the graph which as been build form the configuration
     *
     * @throws SLIB_Exception
     */
    public static G load(GraphConf graphConf) throws SLIB_Exception {

        logger.info("Loading Graph " + graphConf.getUri());

        G g = createGraph(graphConf.getUri());

        return load(graphConf, g);
    }

    /**
     * Impact the given graph considering the given configuration. The graph
     * will be populated by the data and actions will be performed on it if any
     * exist.
     *
     *
     * @param graphConf the graph configuration
     * @return the graph which as been build form the configuration
     *
     * @throws SLIB_Exception
     */
    public static G load(GraphConf graphConf, G g) throws SLIB_Exception {
        for (GDataConf dataConf : graphConf.getData()) {
            populate(dataConf, g);
        }

        URIFactory factory = URIFactoryMemory.getSingleton();

        GraphActionExecutor.applyActions(factory, graphConf.getActions(), g);
        return g;
    }

    /**
     * Load the collection of configurations.
     *
     * @param graphConfs the collection of configurations
     *
     * @throws SLIB_Exception
     */
    public static void load(Collection<GraphConf> graphConfs) throws SLIB_Exception {

        for (GraphConf conf : graphConfs) {
            load(conf);
        }
    }

    /**
     * Retrieve the loader associated to a specific data configuration.
     *
     * @param data the data configuration
     * @return the corresponding Graph loader if any
     *
     * @throws SLIB_Ex_Critic
     */
    private static GraphLoader getLoader(GDataConf data) throws SLIB_Ex_Critic {

        if (data.getFormat() == GFormat.OBO) {
            return new GraphLoader_OBO_1_2();
        } else if (data.getFormat() == GFormat.GAF2) {
            return new GraphLoader_GAF_2();
        } else if (data.getFormat() == GFormat.RDF_XML) {
            return new RDFLoader(RDFFormat.RDFXML);
        } else if (data.getFormat() == GFormat.NTRIPLES) {
            return new RDFLoader(RDFFormat.NTRIPLES);
        } else if (data.getFormat() == GFormat.TURTLE) {
            return new RDFLoader(RDFFormat.TURTLE);
        } else if (data.getFormat() == GFormat.CSV) {
            return new GraphLoader_CSV();
        } else if (data.getFormat() == GFormat.SNOMED_CT_RF2) {
            return new GraphLoaderSnomedCT_RF2();
        } else if (data.getFormat() == GFormat.SLIB) {
            return new GraphLoader_SLIB();
        } else if (data.getFormat() == GFormat.MESH_XML) {
            return new GraphLoader_MESH_XML();
        } else {
            throw new SLIB_Ex_Critic("Unknown Graph format " + data.getFormat());
        }
    }

    /**
     * Check if the generic loader support the given format.
     *
     * @param format the format
     * @return true if the format is supported.
     */
    public static boolean supportFormat(String format) {
        for (GFormat f : supportedFormat) {
            if (f.toString().equals(format)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Access to the set of supported format.
     * @return
     */
    public static GFormat[] getSupportedFormat() {
        return supportedFormat;
    }
}
