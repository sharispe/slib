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
package slib.sglib.io.plotter;
import java.util.HashMap;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import slib.indexer.IndexHash;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;

/**
 * Class used to plot graph using Graphviz.
 * @author Sébastien Harispe 
 *
 */
public class GraphPlotter_Graphviz {

    /**
     *
     * @param graph
     * @param VertexUriColored
     * @param showLabels
     * @return a DOT String representation of the graph.
     */
    public static String plot(G graph, Set<URI> VertexUriColored, boolean showLabels) {
        return plot(graph, VertexUriColored, showLabels,true, null);
    }

    /**
     *
     * @param graph
     * @param VertexColored
     * @param showLabels
     * @param index
     * @return a DOT String representation of the graph
     */
    public static String plot(G graph, Set<URI> VertexColored, boolean showLabels,boolean showSubSlassOfLabels, IndexHash index) {

        HashMap<URI, String> relColor = new HashMap<URI, String>();

        relColor.put(RDFS.SUBCLASSOF, "black");

//		relColor.put(new URI("part_of"), "red");
//		relColor.put(new URI("http://purl.org/obo/owl/OBO_REL#part_of"), "red");
//
//		relColor.put(new URI("part_of_opposite"), "orange");
//		relColor.put(new URI("http://purl.org/obo/owl/OBO_REL#part_of_opposite"), "orange");

        String defColor_v = "\"white\"";//"\"#6583DC\""; // added node  blue
        String defColor_q_v = "\"#FAAB9F\""; // query node  white
        String defColor_e = "black"; // query node  white

        String style = "\n\trankdir=BT;\n\tnode [style=filled,shape=rect]\n\n";

        String out = "digraph plottedgraph {\n";
        out += style;

        String color;

        for (URI v : graph.getV()) {

            color = defColor_v;

            if (VertexColored != null && VertexColored.contains(v)) {
                color = defColor_q_v;
            }

            if (index != null && index.containsIndexFor(v)) {
                
                String indexVal = index.valuesOf(v).getPreferredDescription()+" ["+v.stringValue()+"]";
                
                String splittedLabel = splitString(indexVal, 20);
                out += "\t\"" + splittedLabel + "\"[fillcolor=" + color + "];\n";

            } else {
                out += "\t\"" + v.stringValue() + "\"[color=" + color + "];\n";
            }
        }

        for (E e : graph.getE()) {

            URI predicate = e.getURI();
            color = defColor_e;

            if (relColor.containsKey(predicate)) {
                color = relColor.get(predicate);
            }

            String info = "";

            if (showLabels && !(showSubSlassOfLabels == false && predicate.equals(RDFS.SUBCLASSOF))) {
                info = "[label=\"" + predicate.getLocalName() + "\",color=" + color + "]";
            }
            
            URI s = e.getSource();
            URI t = e.getTarget();
            
            String source = s.stringValue();
            String target = t.stringValue();

            if (index != null) {

                if (index.containsIndexFor(s)) {
                    source = splitString(index.valuesOf(s).getPreferredDescription()+" ["+s.stringValue()+"]", 20);
                }
                if (index.containsIndexFor(e.getTarget())) {
                    target = splitString(index.valuesOf(t).getPreferredDescription()+" ["+t.stringValue()+"]", 20);
                }
            }
            out += "\t\"" + source + "\" -> \"" + target + "\" " + info + ";\n";
        }
        out += "}\n";

        return out;
    }

    /**
     *
     * @param in
     * @param max_num_per_string
     * @return a String associated to the String which has been processed.
     */
    public static String splitString(String in, int max_num_per_string) {

        String[] data = in.split(" ");
        String newLabel = "";
        int curLineLength = 0;

        for (String d : data) {



            if (curLineLength + d.length() + 1 <= max_num_per_string) {
                newLabel += d + " ";
                curLineLength += d.length() + 1;
            } else if (curLineLength == 0 && d.length() > max_num_per_string) {
                newLabel += d + "\\n";
                curLineLength = 0;
            } else if (curLineLength + d.length() + 1 > max_num_per_string) {
                newLabel += "\\n" + d + " ";
                curLineLength = d.length() + 1;
            } else if (curLineLength + d.length() + 1 > max_num_per_string) {
                newLabel += "\\n" + d + " ";
                curLineLength = d.length() + 1;
            } else if (curLineLength + d.length() + 1 > max_num_per_string) {
                newLabel += "\\n" + d + " ";
                curLineLength = d.length() + 1;
            }

        }
        return newLabel;

    }

}
