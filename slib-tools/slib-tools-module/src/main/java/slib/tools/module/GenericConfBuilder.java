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
package slib.tools.module;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GenericConfBuilder {

    static final Pattern patternRgx = Pattern.compile("\\{([^\\{^\\}]*)\\}");

    /**
     * Apply loaded patterns to a string (see {@link GlobalConfPattern} ) <br/>
     * if a pattern {@link XmlTags#VARIABLE_TAG} HOME is set to /tmp the method
     * will return /tmp/test/graph if {HOME}/test/graph is set as input
     *
     * @param v the original string
     * @return the string impacted by the loaded pattern
     * @throws SLIB_Ex_Critic
     */
    public static String applyGlobalPatterns(String v) throws SLIB_Ex_Critic {

        if (v == null) {
            return null;
        }

        Matcher m = patternRgx.matcher(v);

        HashMap<String, String> variables = GlobalConfPattern.getInstance().getVariables();

        String newValue = v;

        while (m.find()) {
            String vName = m.group(1);
            String value = variables.get(vName);

            if (value == null) {
                String loadedPatters = "";

                if (variables.size() == 0) {
                    loadedPatters = " : None\n";
                }

                for (String k : variables.keySet()) {
                    loadedPatters += "key='" + k + "'\tvalue='" + variables.get(k) + "'\n";
                }

                throw new SLIB_Ex_Critic("Undefined pattern '" + vName + "' used in " + v + " please define a pattern (see " + XmlTags.VARIABLE_TAG + " tag)."
                        + "\nLoaded patters "
                        + loadedPatters
                        + "If you are not aware of pattern please remove '{' and '}' characters");
            }

            newValue = newValue.replaceAll("\\{" + vName + "\\}", value);

        }
        return newValue;
    }

    /**
     *
     * @param e
     * @return the configuration build from the element
     * @throws SLIB_Ex_Critic
     */
    public static Conf build(Element e) throws SLIB_Ex_Critic {
        Conf m = new Conf();

        for (int j = 0; j < e.getAttributes().getLength(); j++) {
            String p = e.getAttributes().item(j).getNodeName();
            String v = e.getAttributes().item(j).getTextContent();

            v = applyGlobalPatterns(v);

            m.addParam(p, v);
        }
        return m;
    }

    /**
     *
     * @param list
     * @return a linked set of configurations
     * @throws SLIB_Ex_Critic
     */
    public static LinkedHashSet<Conf> build(NodeList list) throws SLIB_Ex_Critic {

        LinkedHashSet<Conf> gConfSet = new LinkedHashSet<Conf>();

        for (int i = 0; i < list.getLength(); i++) {
            Conf m = build((Element) list.item(i));
            gConfSet.add(m);
        }
        return gConfSet;
    }
}
