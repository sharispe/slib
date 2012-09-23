package slib.tools.module;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.i.Conf;

public class GenericConfBuilder {
	
	static  final Pattern patternRgx = Pattern.compile("\\{([^\\{^\\}]*)\\}");
	
	/**
	 * Apply loaded patterns to a string (see {@link GlobalConfPattern} ) <br/>
	 * if a pattern {@link XmlTags#VARIABLE_TAG} HOME is set to /tmp
	 * the method will return /tmp/test/graph if {HOME}/test/graph is set as input
	 * @param v the original string
	 * @return the string impacted by the loaded pattern
	 * @see {@link GlobalConfPattern}
	 * @throws SGL_Ex_Critic if a non existing pattern is used
	 */
	public static String applyGlobalPatterns(String v) throws SGL_Ex_Critic{

		if(v == null) return null;
		
		Matcher m = patternRgx.matcher(v);

		HashMap<String, String > variables = GlobalConfPattern.getInstance().getVariables();

		String newValue = v;

		while(m.find()) {
			String vName = m.group(1);
			String value = variables.get(vName);

			if(value == null){
				String loadedPatters = "";

				if(variables.size() == 0)
					loadedPatters = " : None\n";

				for(String k : variables.keySet())
					loadedPatters += "key='"+k+"'\tvalue='"+variables.get(k)+"'\n";

				throw new SGL_Ex_Critic("Undefined pattern '"+vName+"' used in "+v+" please define a pattern (see "+XmlTags.VARIABLE_TAG+" tag)." +
						"\nLoaded patters " +
						loadedPatters +
						"If you are not aware of pattern please remove '{' and '}' characters");
			}

			newValue = newValue.replaceAll("\\{"+vName+"\\}",value);

		}
		return newValue;
	}
	
	
	public static Conf build(Element e) throws SGL_Ex_Critic {
		Conf m = new Conf();

		for(int j = 0; j< e.getAttributes().getLength();j++){
			String p = e.getAttributes().item(j).getNodeName();
			String v = e.getAttributes().item(j).getTextContent();

			v = applyGlobalPatterns(v);

			m.addParam(p, v);
		}
		return m;
	}
	
	
	public static LinkedHashSet<Conf> build(NodeList list) throws SGL_Ex_Critic {

		LinkedHashSet<Conf> gConfSet = new LinkedHashSet<Conf>();

		for(int i = 0; i < list.getLength(); i++){
			Conf m = build( (Element) list.item(i) );
			gConfSet.add(m);
		}
		return gConfSet;
	}

}
