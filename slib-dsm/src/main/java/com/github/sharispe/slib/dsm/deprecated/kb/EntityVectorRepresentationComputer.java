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
package com.github.sharispe.slib.dsm.deprecated.kb;

import com.github.sharispe.slib.dsm.core.model.utils.compression.CompressionUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.conf.GraphConf;
import slib.graph.io.loader.GraphLoaderGeneric;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.impl.graph.memory.GraphMemory;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 */
public class EntityVectorRepresentationComputer {

    static int NB_EPOQUE = 1000;
    static double PROPAGATION_RATE = 0.6; // if random <= PROPAGATION_RATE the propagation is performed must be < 1. High values augment the chance of propagation

    Logger logger = LoggerFactory.getLogger(EntityVectorRepresentationComputer.class);

    public void buildVectorRepresentations(Set<URI> uris, G graph) {

        logger.info("Building index map");
        Map<URI, Integer> indexMap = new HashMap();
        int c = 0;
        for (URI u : graph.getV()) {
            indexMap.put(u, c);
            c++;
        }
        logger.info("Computing vector representation for " + uris.size());
        c = 1;
        for (URI uri : uris) {
            logger.info("Building vector representation of " + uri + " " + c + "/" + uris.size() + "\t(size=" + indexMap.size() + ")");
            c++;
            double[] vec = buildVectorRepresentation(uri, graph, indexMap);
            Map<Integer, Double> compression = CompressionUtils.compressedDoubleArrayToMap(CompressionUtils.compressDoubleArray(vec));
//            System.out.println(compression);
        }
    }

    private double[] buildVectorRepresentation(URI uri, G graph, Map<URI, Integer> indexMap) {

        double[] vector = new double[indexMap.size()];
        boolean propagate;
        Random randomGenerator = new Random();
        for (int i = 1; i <= NB_EPOQUE; i++) {
            //logger.info("Processing epoch " + i + "/" + NB_EPOQUE);
            propagate = true;
            URI currentNode = uri;

            int c = 0;

            while (propagate) {

                currentNode = selectRandomNeighbor(currentNode, graph);
                int id_currentNode = indexMap.get(currentNode);
                vector[id_currentNode] += 1;
                propagate = randomGenerator.nextDouble() <= PROPAGATION_RATE;
//                System.out.println("-> " + currentNode + " " + indexMap.get(currentNode));
                c++;
            }
//            System.out.println("-> " + c);
        }
        // Normalize the vector
        double max = vector[0];
        for (int i = 1; i < vector.length; i++) {
            if (vector[i] > max) {
                max = vector[i];
            }
        }
        for (int i = 0; i < vector.length; i++) {
            vector[i] = vector[i] / max;
        }
        return vector;

    }

    private URI selectRandomNeighbor(URI uri, G graph) {
        URI selectedURI = null;
        Set<E> edges = graph.getE(uri, Direction.BOTH);
//        System.out.println(uri + "(edges=" + edges.size() + ")");
        Random r = new Random();
        int id = r.nextInt(edges.size());
        int c = 0;
        for (E e : edges) {
            if (c == id) {
//                System.out.println(e);
                selectedURI = e.getSource().equals(uri) ? e.getTarget() : e.getSource();
//                System.out.println("sel: "+selectedURI);
            }
            c++;
        }
        return selectedURI;
    }

    private G reduceGraph(Set<URI> uris, G graph, int limit) {
        G graph_reduction = new GraphMemory(graph.getURI());
        int c = 0;
        for (URI uri : uris) {
            c++;
            logger.info("Processing " + c + "/" + uris.size() + " " + uri);
            expendGraphFrom(uri, graph, graph_reduction, limit);
            logger.info("reduction: e=" + graph_reduction.getE().size() + "/" + graph.getE().size() + "\tv=" + graph_reduction.getV().size() + "/" + graph.getV().size());
        }
        return graph_reduction;
    }

    private void expendGraphFrom(URI uri, G graph, G graph_reduction, int limit) {

        logger.info("Expending to " + uri + " (" + limit + ")");
        boolean visited;
        for (E e : graph.getE(uri, Direction.BOTH)) {

            URI destination = e.getSource().equals(uri) ? e.getTarget() : e.getSource();
            visited = graph_reduction.containsVertex(destination);
            graph_reduction.addE(e);

            if (limit > 0 && !visited) {
                expendGraphFrom(destination, graph, graph_reduction, limit - 1);
            }
        }
    }

    public static void main(String[] args) throws SLIB_Exception {

        args = new String[2];
        args[0] = "RDF_XML";
        args[1] = "/data/dbpedia/dbpedia_3.8.owl";
        // 
        if (args.length % 2 != 0) {
            throw new SLIB_Ex_Critic("Error please use <file type> <location> with file type = RDF_XML or ntriples");
        }

        URIFactory uriFactory = URIFactoryMemory.getSingleton();
        GraphConf gConf = new GraphConf(uriFactory.getURI("http://graph"));

        for (int i = 0; i < args.length; i += 2) {
            GFormat format = null;
            if (args[i].equals(GFormat.RDF_XML.name())) {
                format = GFormat.RDF_XML;
            } else if (args[i].equals(GFormat.NTRIPLES.name())) {
                format = GFormat.NTRIPLES;
            }
            gConf.addGDataConf(new GDataConf(GFormat.RDF_XML, args[i + 1]));
        }

        G graph = GraphLoaderGeneric.load(gConf);
        Set<URI> uris = new HashSet(graph.getV());
        uris.clear();
        uris.add(uriFactory.getURI("http://dbpedia.org/ontology/engineer"));
        G reducedGraph = new EntityVectorRepresentationComputer().reduceGraph(uris, graph, 3);
//        G reducedGraph = graph;
        new EntityVectorRepresentationComputer().buildVectorRepresentations(uris, reducedGraph);
    }

}
