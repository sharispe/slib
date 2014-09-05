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
package slib.indexer.wordnet;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.model.graph.G;
import slib.graph.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * DUMMY INDEXER USED TO DEBUG. INPUT: index.*
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class IndexerWordNetBasic {

    Map<String, Set<URI>> stringToSynsetIndex = new HashMap<String, Set<URI>>();
    Logger logger = LoggerFactory.getLogger(this.getClass());
    G graph;

    public IndexerWordNetBasic() {
    }

    /**
     *
     * @param factory
     * @param g
     * @param file
     * @throws SLIB_Ex_Critic
     */
    public IndexerWordNetBasic(URIFactory factory, G g, String file) throws SLIB_Ex_Critic {

        graph = g;
        populateIndex(factory, file);
    }

    private void populateIndex(URIFactory factory, String filepath) throws SLIB_Ex_Critic {

        logger.info("---------------------------------");
        logger.info("Populating index from " + filepath);

        boolean inHeader = true;
        try {

            FileInputStream fstream = new FileInputStream(filepath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            String line;
            String[] data;

            while ((line = br.readLine()) != null) {

                if (inHeader) {

                    if (line.startsWith("  ")) {
                        continue;
                    }
                    inHeader = false;
                }

                line = line.trim();
                data = line.split("\\s+");
//                System.out.println( Arrays.toString(data) );

                String valString = data[0];
                String pos = data[1];
                int synset_cnt = Integer.parseInt(data[2]);
                int p_cnt = Integer.parseInt(data[3]);


                int c = 4 + p_cnt;

                int sense_cnt = Integer.parseInt(data[c]);
                c += 2; // sense_cnt + tagsense_cnt

                Set<URI> synsets = new HashSet<URI>();

                for (int i = 0; i < sense_cnt; i++) {
                    URI u = factory.getURI(graph.getURI().getNamespace() + "" + data[c + i]);
//                    System.out.println(u);

                    if (!graph.containsVertex(u)) {
                        throw new SLIB_Ex_Critic("Error cannot locate synset " + u);
                    }
                    synsets.add(u);
//                    if(!synsetIndexToString.containsKey(u)){
//                        throw new SLIB_Ex_Critic("Oooops duplicate "+u+"\t"+synsetIndexToString.get(u)+"\t"+valString);
//                    }
//                    synsetIndexToString.put(u, valString);
                }
                stringToSynsetIndex.put(valString, synsets);
            }
            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }

        logger.info("Index build");
    }

    /**
     *
     * @param query
     * @return the URIs associated to the given string
     */
    public Set<URI> get(String query) {
        return stringToSynsetIndex.get(query);
    }

    public void add(String query, URI uri) {

        if (!stringToSynsetIndex.containsKey(query)) {
            stringToSynsetIndex.put(query, new HashSet<URI>());
        }

        stringToSynsetIndex.get(query).add(uri);
    }

    public void add(String query, Collection<URI> uris) {

        if (!stringToSynsetIndex.containsKey(query)) {
            stringToSynsetIndex.put(query, new HashSet<URI>());
        }

        stringToSynsetIndex.get(query).addAll(uris);
    }

    /**
     *
     * @return the complete index.
     */
    public Map<String, Set<URI>> getIndex() {
        return stringToSynsetIndex;
    }
}
