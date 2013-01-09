package slib.sml.sml_server_deploy.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author seb
 */
public class Benchmark {

	
	String name;
	String type;
	LinkedList<CmdProfile> cmdsProfile;
	
	Set<BenchmarkInput> inputFiles 	  = new HashSet<BenchmarkInput>();
	Map<String,String> localVariables = new HashMap<String,String>();
	
	
	LinkedList<Command> preProcessCmd = new LinkedList<Command>();
	LinkedList<Command> postProcessCmd = new LinkedList<Command>();
	
	/**
     *
     * @return
     */
    public LinkedList<Command> getPreProcessCmd() {
		return preProcessCmd;
	}
	
	/**
     *
     * @return
     */
    public LinkedList<Command> getPostProcessCmd() {
		return postProcessCmd;
	}

	/**
     *
     * @param preProcessCmd
     */
    public void setPreProcessCmd(LinkedList<Command> preProcessCmd) {
		this.preProcessCmd = preProcessCmd;
	}
	
	/**
     *
     * @param postProcessCmd
     */
    public void setPostProcessCmd(LinkedList<Command> postProcessCmd) {
		this.postProcessCmd = postProcessCmd;
	}

	
	
	/**
     *
     * @param name
     */
    public Benchmark(String name){
		this.name = name;
	}
	
	/**
     *
     * @param key
     * @param value
     */
    public void addLocalVariable(String key,String value){
		localVariables.put(key, value);
	}

	/**
     *
     * @return
     */
    public Map<String, String> getLocalVariables() {
		return localVariables;
	}

	/**
     *
     * @param localVariables
     */
    public void setLocalVariables(Map<String, String> localVariables) {
		this.localVariables = localVariables;
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
    public LinkedList<CmdProfile> getCmdsProfile() {
		return cmdsProfile;
	}

	/**
     *
     * @param cmdsProfile
     */
    public void setCmdsProfile(LinkedList<CmdProfile> cmdsProfile) {
		this.cmdsProfile = cmdsProfile;
	}
	
	/**
     *
     * @param i
     */
    public void addInputFile(BenchmarkInput i){
		inputFiles.add(i);
	}

	/**
     *
     * @return
     */
    public Set<BenchmarkInput> getInputFiles() {
		return inputFiles;
	}

	/**
     *
     * @param inputFiles
     */
    public void setInputFiles(Set<BenchmarkInput> inputFiles) {
		this.inputFiles = inputFiles;
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
