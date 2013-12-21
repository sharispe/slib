/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
import slib.sglib.model.graph.G;
import slib.sglib.model.repo.URIFactory;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * DUMMY INDEXER USED TO DEBUG. INPUT: index.*
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
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
                    URI u = factory.createURI(graph.getURI().getNamespace() + "" + data[c + i]);
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
