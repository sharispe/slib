package slib.sml.sml_server_deploy.core.utils;

import java.util.LinkedList;

/**
 *
 * @author seb
 */
public class CmdProfile {

	String name;
	String type;
	LinkedList<Command> workflow = new LinkedList<Command>();
	
	/**
     *
     * @param name
     * @param type
     */
    public CmdProfile(String name,String type){
		this.name = name;
		this.type = type;
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
     * @param cmd
     */
    public void addCmd(Command cmd){
		workflow.add(cmd);
	}

	/**
     *
     * @return
     */
    public LinkedList<Command> getWorkflow() {
		return workflow;
	}

	/**
     *
     * @param workflow
     */
    public void setWorkflow(LinkedList<Command> workflow) {
		this.workflow = workflow;
	}

	/**
     *
     * @return
     */
    public String getType() {
		return type;
	}

	/**
     *
     * @param type
     */
    public void setType(String type) {
		this.type = type;
	}
}
