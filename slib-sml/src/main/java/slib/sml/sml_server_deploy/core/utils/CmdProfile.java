package slib.sml.sml_server_deploy.core.utils;

import java.util.LinkedList;

public class CmdProfile {

	String name;
	String type;
	LinkedList<Command> workflow = new LinkedList<Command>();
	
	public CmdProfile(String name,String type){
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addCmd(Command cmd){
		workflow.add(cmd);
	}

	public LinkedList<Command> getWorkflow() {
		return workflow;
	}

	public void setWorkflow(LinkedList<Command> workflow) {
		this.workflow = workflow;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
