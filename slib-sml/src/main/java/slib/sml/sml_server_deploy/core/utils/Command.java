package slib.sml.sml_server_deploy.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author seb
 */
public class Command {
	
	Map<String,String> defaultVariables = new HashMap<String,String>();
	String name;
	String commandPattern;
	
	/**
     *
     * @param name
     * @param commandPattern
     */
    public Command(String name,String commandPattern){
		this.name = name;
		this.commandPattern = commandPattern;
	}

	/**
     *
     * @return
     */
    public Map<String, String> getDefaultVariables() {
		return defaultVariables;
	}

	/**
     *
     * @param defaultVariables
     */
    public void setDefaultVariables(Map<String, String> defaultVariables) {
		this.defaultVariables = defaultVariables;
	}

	/**
     *
     * @return
     */
    public String getName() {
		return name;
	}

	/**
     *
     * @param name
     */
    public void setName(String name) {
		this.name = name;
	}

	/**
     *
     * @return
     */
    public String getCommandPattern() {
		return commandPattern;
	}

	/**
     *
     * @param commandPattern
     */
    public void setCommandPattern(String commandPattern) {
		this.commandPattern = commandPattern;
	}
	
	public String toString(){
		String out = "CMD "+name;
		out += "\n\t cmd pattern    : "+commandPattern;
		out += "\n\t default_params : "+defaultVariables;
		return out;
		
	}
	
	
}
