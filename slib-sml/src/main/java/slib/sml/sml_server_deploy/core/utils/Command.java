package slib.sml.sml_server_deploy.core.utils;

import java.util.HashMap;
import java.util.Map;

public class Command {
	
	Map<String,String> defaultVariables = new HashMap<String,String>();
	String name;
	String commandPattern;
	
	public Command(String name,String commandPattern){
		this.name = name;
		this.commandPattern = commandPattern;
	}

	public Map<String, String> getDefaultVariables() {
		return defaultVariables;
	}

	public void setDefaultVariables(Map<String, String> defaultVariables) {
		this.defaultVariables = defaultVariables;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommandPattern() {
		return commandPattern;
	}

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
