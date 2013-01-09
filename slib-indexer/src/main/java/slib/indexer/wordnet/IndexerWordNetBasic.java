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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.repo.DataFactory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.UtilDebug;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class IndexerWordNetBasic{
    
    Map<String, Set<V>> stringToSynsetIndex = new HashMap<String, Set<V>>();
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    G graph;
    
    /**
     *
     * @param factory
     * @param g
     * @param file
     * @throws SLIB_Ex_Critic
     */
    public IndexerWordNetBasic(DataFactory factory, G g, String file) throws SLIB_Ex_Critic{
        
        graph = g;
        populateIndex(factory,file);
    } 

    private void populateIndex(DataFactory factory,String filepath) throws SLIB_Ex_Critic {
        
        logger.info("Populating index from "+filepath);
        
        boolean inHeader = true;
        try {

            FileInputStream fstream = new FileInputStream(filepath);
            DataInputStream in      = new DataInputStream(fstream);
            BufferedReader br       = new BufferedReader(new InputStreamReader(in));

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
                //System.out.println( Arrays.toString(data) );
                
                String valString  = data[0];
                String pos        = data[1];
                int synset_cnt = Integer.parseInt(data[2]);
                int p_cnt      = Integer.parseInt(data[3]);
                
//                System.out.println(valString+"\t"+p_cnt);
                int c = 4 + p_cnt;
                
                int sense_cnt = Integer.parseInt(data[c]);
                c+= 2; // sense_cnt + tagsense_cnt
                
                Set<V> synsets = new HashSet<V>();
                
                for (int i = 0; i < sense_cnt; i++) {
                    URI u = factory.createURI(graph.getURI().getNamespace()+""+data[c+i]);
//                    System.out.println(u);
                    
                    V synset= graph.getV(u);
                    
                    if(synset == null){
                        System.out.println("Error cannot locate synset "+u);
                        UtilDebug.exit(this);
                    }
                    synsets.add(synset);
                }
                stringToSynsetIndex.put(valString, synsets);
            }
            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }
        
        logger.info("Index build" );
    }
    
    /**
     *
     * @param query
     * @return
     */
    public Set<V> get(String query) {
        return stringToSynsetIndex.get(query);
    }
    
    /**
     *
     * @return
     */
    public Map<String, Set<V>> getIndex() {
        return stringToSynsetIndex;
    }
    
    
}
