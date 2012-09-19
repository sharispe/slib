package slib.sml.sml_server_deploy.core.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Benchmark {

	
	String name;
	String type;
	LinkedList<CmdProfile> cmdsProfile;
	
	Set<BenchmarkInput> inputFiles 	  = new HashSet<BenchmarkInput>();
	Map<String,String> localVariables = new HashMap<String,String>();
	
	
	LinkedList<Command> preProcessCmd = new LinkedList<Command>();
	LinkedList<Command> postProcessCmd = new LinkedList<Command>();
	
	public LinkedList<Command> getPreProcessCmd() {
		return preProcessCmd;
	}
	
	public LinkedList<Command> getPostProcessCmd() {
		return postProcessCmd;
	}

	public void setPreProcessCmd(LinkedList<Command> preProcessCmd) {
		this.preProcessCmd = preProcessCmd;
	}
	
	public void setPostProcessCmd(LinkedList<Command> postProcessCmd) {
		this.postProcessCmd = postProcessCmd;
	}

	
	
	public Benchmark(String name){
		this.name = name;
	}
	
	public void addLocalVariable(String key,String value){
		localVariables.put(key, value);
	}

	public Map<String, String> getLocalVariables() {
		return localVariables;
	}

	public void setLocalVariables(Map<String, String> localVariables) {
		this.localVariables = localVariables;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public LinkedList<CmdProfile> getCmdsProfile() {
		return cmdsProfile;
	}

	public void setCmdsProfile(LinkedList<CmdProfile> cmdsProfile) {
		this.cmdsProfile = cmdsProfile;
	}
	
	public void addInputFile(BenchmarkInput i){
		inputFiles.add(i);
	}

	public Set<BenchmarkInput> getInputFiles() {
		return inputFiles;
	}

	public void setInputFiles(Set<BenchmarkInput> inputFiles) {
		this.inputFiles = inputFiles;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
	
	
	
	
}
