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
package slib.sml.sm.core.metrics.vector;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;

import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.Direction;
import slib.sml.sm.core.engine.SM_Engine;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class VectorWeight_Chabalier_2007 {

    /**
     *
     * @param engine
     * @return the vector weights
     */
    public static Map<URI, Double> compute(SM_Engine engine) {

        Logger logger = LoggerFactory.getLogger(VectorWeight_Chabalier_2007.class);

        logger.info("Computing IDF chabalier 2007");

        long nb_annotated_entities = 0;

        Set<URI> instances = engine.getInstances();
        G g = engine.getGraph();

        Map<URI, Long> nbOcc = new HashMap<URI, Long>();

        for (URI v : engine.getClasses()) {
            nbOcc.put(v, new Long(0));
        }


        for (URI o : instances) {
            for (URI v : g.getV(o, RDF.TYPE, Direction.OUT)) {
                nb_annotated_entities++;
                nbOcc.put(v, nbOcc.get(v) + 1);
            }
        }

        Map<URI, Double> stack = new HashMap<URI, Double>();



        for (URI v : engine.getClasses()) {

            // Add 1 to original formula to avoid 0 division
            double idf = Math.log(nb_annotated_entities / (nbOcc.get(v) + 1));

            stack.put(v, idf);

            //logger.debug(v+"\t\t"+nbOcc.get(v)+"\t"+idf);

        }
        return stack;
    }
}
