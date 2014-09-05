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
package slib.graph.io.loader.wordnet;

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
import slib.graph.io.conf.GDataConf;
import slib.graph.io.loader.GraphLoader;
import slib.graph.io.util.GFormat;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.impl.graph.elements.Edge;
import slib.graph.model.impl.repo.URIFactoryMemory;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphLoader_Wordnet implements GraphLoader {

    private G graph;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    Map<String, PointerToEdge> pointerSymbolToURIsMap;
    URIFactoryMemory dataRepo = URIFactoryMemory.getSingleton();

    @Override
    public void populate(GDataConf conf, G g) throws SLIB_Exception {

        logger.info("-------------------------------------");
        logger.info(" WordNet Loader");
        logger.info("-------------------------------------");
        logger.info("Loading from Wordnet data");

        if (conf.getFormat() != GFormat.WORDNET_DATA) {
            throw new SLIB_Ex_Critic("Cannot use " + this.getClass() + " to load file format " + conf.getFormat() + ", required format is " + GFormat.WORDNET_DATA);
        }

        initPointerToURImap();

        graph = g;
        boolean inHeader = true;
        String filepath = conf.getLoc();

        logger.info("From " + filepath);
        logger.info("-----------------------------------------------------------");
        
        

        String uriPrefix = g.getURI().getNamespace();
        if (conf.getParameter("prefix") != null) {
            uriPrefix = (String) conf.getParameter("prefix");
        }
        try {
            
            if(filepath == null)
                throw new SLIB_Ex_Critic("Error please precise a  file to load.");

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

                String synset_offset = data[0];
                String lex_filenum = data[1];
                String ss_type = data[2];

                URI synset = dataRepo.getURI(uriPrefix + synset_offset);
                graph.addV(synset);
                
                int w_cnt = Integer.parseInt(data[3], 16);// hexa  
                
                
                

//                logger.info(synset_offset);

                //System.out.println(synset_offset + "\t" + w_cnt);

                Word[] words = extractWords(data, 4, w_cnt);

                int c = 3 + w_cnt * 2 + 1;

                int p_cnt = Integer.parseInt(data[c]);

                Pointer[] pointers = extractPointers(data, c + 1, p_cnt);

                for (Pointer p : pointers) {

                    if (pointerSymbolToURIsMap.containsKey(p.pointerSymbol)) {

//                        logger.info("\t " + p.synsetOffset + " \t " + p.pointerSymbol);

                        URI s = dataRepo.getURI(uriPrefix + synset_offset);
                        URI o = dataRepo.getURI(uriPrefix + p.synsetOffset);

                        graph.addV(s);
                        graph.addV(o);

                        E e = pointerSymbolToURIsMap.get(p.pointerSymbol).createEdge(s, o);
                        
//                        logger.info("\t"+e.toString());

                        g.addE(e);


                    } else {
                       //logger.debug("\tExclude Pointer symbol: " + p.pointerSymbol);
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            throw new SLIB_Ex_Critic("Error loading the file: "+e.getMessage());
        }

        logger.info(graph.toString());
        logger.info("Wordnet Loading ok.");
        logger.info("-------------------------------------");
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

        PointerToEdge hypernym = new PointerToEdge(RDFS.SUBCLASSOF, true);
        PointerToEdge hyponym = new PointerToEdge(RDFS.SUBCLASSOF, false);

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

        @Override
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

        public E createEdge(URI srcPointer, URI targetPointer) {

            E e;
            if (fromSourceToTarget) {
                e = new Edge(srcPointer, rel, targetPointer);
            } else {
                e = new Edge(targetPointer, rel, srcPointer);
            }
            return e;
        }
    }
}
