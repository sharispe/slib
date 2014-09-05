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
package slib.graph.io.loader.slibformat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * TODO Comment DO not Support transitivity and inverse definitions anymore
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class GraphLoader_SLIB implements GraphLoader {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    URIFactory factory;
    String filepath;
    G g;



    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {
        process(conf, g);
    }

    /**
     *
     * @param conf
     * @param graph
     * @throws SLIB_Exception
     */
    public void process(GDataConf conf, G graph) throws SLIB_Exception {

        this.g = graph;
        factory = URIFactoryMemory.getSingleton();


        this.filepath = conf.getLoc();
        logger.info("-------------------------------------");
        logger.info(" SLIB loader");
        logger.info("-------------------------------------");
        logger.info("Loading graph from SLIB formatted file" + filepath);

        try {

            FileInputStream fstream = new FileInputStream(filepath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;

            String[] dataTMP;


            while ((line = br.readLine()) != null) {

                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                dataTMP = line.split("\t");
                if (dataTMP.length == 1) { // vertex
                    URI vURI = factory.getURI(dataTMP[0]);
                    g.addV(vURI);
                } else if (dataTMP.length == 3) {
                    URI sURI = factory.getURI(dataTMP[0]);
                    URI pURI = factory.getURI(dataTMP[1]);
                    URI oURI = factory.getURI(dataTMP[2]);

                    g.addV(sURI);
                    g.addV(oURI);
                    g.addE(sURI, pURI, oURI);
                } else {
                    throw new SLIB_Ex_Critic("Cannot process the following line " + line);
                }
            }
            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        logger.info("-------------------------------------");
    }

    /**
     *
     * @param g
     * @param outfile
     * @throws SLIB_Ex_Critic
     */
    public void flush(G g, String outfile) throws SLIB_Ex_Critic {

        try {
            FileWriter fstream = new FileWriter(outfile);
            BufferedWriter out = new BufferedWriter(fstream);

            out.write("# vertices " + g.getV().size() + "\n");
            out.write("# edges " + g.getE().size() + "\n");

            for (URI v : g.getV()) {
                out.write(v.stringValue() + "\n");
            }

            for (E e : g.getE()) {
                out.write(e.getSource().stringValue() + "\t" + e.getURI().stringValue() + "\t" + e.getTarget().stringValue() + "\n");
            }
            out.close();
        } catch (Exception e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
    }
}
