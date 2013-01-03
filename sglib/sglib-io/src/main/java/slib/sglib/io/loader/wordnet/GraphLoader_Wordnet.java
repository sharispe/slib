/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.sglib.io.loader.wordnet;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.io.conf.GDataConf;
import slib.sglib.io.conf.GraphConf;
import slib.sglib.io.loader.GraphLoaderGeneric;
import slib.sglib.io.loader.GraphLoader;
import slib.sglib.io.util.GFormat;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.impl.graph.elements.EdgeTyped;
import slib.sglib.model.impl.graph.elements.VertexTyped;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.impl.graph.memory.GraphMemory;
import slib.sglib.model.impl.repo.DataFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * 
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class GraphLoader_Wordnet implements GraphLoader {

    private G graph;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<String, PointerToEdge> pointerSymbolToURIsMap;
    
    
    DataFactoryMemory dataRepo = DataFactoryMemory.getSingleton();

    @Override
    public G load(GraphConf conf) throws SLIB_Exception {
        return GraphLoaderGeneric.load(conf);
    }

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {
        
        logger.info("Loading from Wordnet data");
        
        if(conf.getFormat() != GFormat.WORDNET_DATA){
            throw new SLIB_Ex_Critic("Cannot use "+this.getClass()+" to load file format "+conf.getFormat()+", required format is "+GFormat.WORDNET_DATA);
        }

        initPointerToURImap();

        graph = g;
        boolean inHeader = true;
        String filepath = conf.getLoc();
        
        logger.info("From "+filepath);

        String uriPrefix = g.getURI().getNamespace();
        if (conf.getParameter("prefix") != null) {
            uriPrefix = (String) conf.getParameter("prefix");
        }
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

                String synset_offset = data[0];
                String lex_filenum = data[1];
                String ss_type      = data[2];

                int w_cnt = Integer.parseInt(data[3], 16);// hexa  


                //System.out.println(synset_offset + "\t" + w_cnt);



                Word[] words = extractWords(data, 4, w_cnt);

                int c = 3 + w_cnt * 2 + 1;

                int p_cnt = Integer.parseInt(data[c]);

                Pointer[] pointers = extractPointers(data, c + 1, p_cnt);

                for (Pointer p : pointers) {

                    if (pointerSymbolToURIsMap.containsKey(p.pointerSymbol)) {

                        //System.out.println("\t " + p.synsetOffset + " \t " + p.pointerSymbol);
                        
                        URI s = dataRepo.createURI(uriPrefix + synset_offset);
                        URI o = dataRepo.createURI(uriPrefix + p.synsetOffset);
                        
                        V vs = graph.addV(new VertexTyped(graph, s, VType.CLASS));
                        V vo = graph.addV(new VertexTyped(graph, o, VType.CLASS));
                        
                        E e = pointerSymbolToURIsMap.get(p.pointerSymbol).createEdge(vs, vo);
                        
                        g.addE(e);
                        

                    } else {
                        //System.out.println("\tExclude Pointer symbol: " + p.pointerSymbol);
                    }
                }
                

            }
            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic(e.getMessage());
        }

        logger.info(graph.toString());
        logger.info("Wordnet Loading ok.");
    }

    private Word[] extractWords(String[] data, int start_id, int w_cnt) {

        int c = 0;

        Word[] words = new Word[w_cnt];

        for (int i = start_id; c < w_cnt; i += 2) {
            words[c] = new Word(data[i], Integer.parseInt(data[i + 1], 16));
            c++;
        }

        return words;
    }

    private Pointer[] extractPointers(String[] data, int start_id, int p_cnt) {

        int c = 0;

        Pointer[] pointers = new Pointer[p_cnt];

        for (int i = start_id; c < p_cnt; i += 4) {

            String pointerSymbol = data[i];
            String synsetOffset = data[i + 1];
            String pos = data[i + 2];
            String src_target = data[i + 3];

            Pointer p = new Pointer(pointerSymbol, synsetOffset, pos, src_target);
            pointers[c] = p;
            c++;
        }

        return pointers;

    }

    private void initPointerToURImap() {
        pointerSymbolToURIsMap = new HashMap<String, PointerToEdge>();

        PointerToEdge hypernym = new PointerToEdge(RDFS.SUBCLASSOF, false);
        PointerToEdge hyponym = new PointerToEdge(RDFS.SUBCLASSOF, true);

        // @ Hypernym / @i instance hypernym
        pointerSymbolToURIsMap.put("@", hypernym);
        pointerSymbolToURIsMap.put("@i", hypernym);

        // @ Hyponym / @i instance hyponym
        pointerSymbolToURIsMap.put("~", hyponym);
        pointerSymbolToURIsMap.put("~i", hyponym);
    }

    private class Word {

        String word;
        int lex_id;

        Word(String word, int lex_id) {
            this.word = word;
            this.lex_id = lex_id;
        }

        @Override
        public String toString() {
            return this.word + "(" + lex_id + ")";
        }
    }

    private class Pointer {

        String pointerSymbol;
        String synsetOffset;
        String pos;
        String src_target;

        private Pointer(String pointerSymbol, String synsetOffset, String pos, String src_target) {

            this.pointerSymbol = pointerSymbol;
            this.synsetOffset = synsetOffset;
            this.src_target = src_target;
            this.pos = pos;
        }

        public String toString() {
            return "symbol: " + this.pointerSymbol + "  "
                    + "synsetOffset: " + this.synsetOffset + "  "
                    + "src_target: " + this.src_target + "  "
                    + "pos: " + this.pos + "  ";
        }
    }

    private class PointerToEdge {

        URI rel;
        boolean fromSourceToTarget;

        public PointerToEdge(URI rel, boolean fromSourceToTarget) {
            this.rel = rel;
            this.fromSourceToTarget = fromSourceToTarget;
        }

        public E createEdge(V srcPointer, V targetPointer) {

            E e = null;
            if (fromSourceToTarget) {
                e = new EdgeTyped(srcPointer, targetPointer, rel);
            } else {
                e = new EdgeTyped(targetPointer, srcPointer, rel);
            }
            return e;
        }
    }

    public static void main(String[] args) throws Exception {


        URI guri = DataFactoryMemory.getSingleton().createURI("http://graph/wordnet/");
        G g = new GraphMemory(guri);

        GraphLoader_Wordnet loader = new GraphLoader_Wordnet();
        
        String dataloc = "/home/seb/desktop/WordNet-3.0/dict/";
        String data_noun = dataloc+"data.noun";
        String data_verb = dataloc+"data.verb";
        String data_adj  = dataloc+"data.adj";
        String data_adv  = dataloc+"data.adv";
        
        GDataConf dataNoun = new GDataConf(GFormat.WORDNET_DATA, data_noun);
        GDataConf dataVerb = new GDataConf(GFormat.WORDNET_DATA, data_verb);
        GDataConf dataAdj = new GDataConf(GFormat.WORDNET_DATA, data_adj);
        GDataConf dataAdv = new GDataConf(GFormat.WORDNET_DATA, data_adv);

        loader.populate(dataNoun, g);
        loader.populate(dataVerb, g);
        loader.populate(dataAdj, g);
        loader.populate(dataAdv, g);
    }
}
