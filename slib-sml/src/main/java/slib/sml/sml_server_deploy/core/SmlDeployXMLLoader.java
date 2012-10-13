package slib.sml.sml_server_deploy.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import slib.sml.sml_server_deploy.core.utils.Benchmark;
import slib.sml.sml_server_deploy.core.utils.BenchmarkInput;
import slib.sml.sml_server_deploy.core.utils.CmdProfile;
import slib.sml.sml_server_deploy.core.utils.Command;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.Util;

public class SmlDeployXMLLoader {

	Logger logger = LoggerFactory.getLogger(this.getClass());


	Document document;
	CmdExecutor executor;

	LinkedList<Command> 	cmdCollection;
	LinkedList<CmdProfile>  cmdProfilesCollection;
	LinkedList<Benchmark>   benchmarksList;

	public CmdExecutor loadConf(String xmlFile) throws SLIB_Ex_Critic{

		executor = new CmdExecutor();

		cmdCollection = new LinkedList<Command>();

		try {

			// Load and validate XML configuration specification

			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder(); 
			document = parser.parse( new File(xmlFile )); 


			//------------------------------
			//	 Load Global Variables
			//------------------------------

			NodeList opt = document.getElementsByTagName("global_variables");


			if(	opt.getLength() == 1 && opt.item(0) instanceof Element ){

				LinkedHashSet<Entry<String,String>> vars = buildEntries(opt.item(0).getChildNodes());
				executor.setGlobalVariables( convertToMap(vars));

			}
			else if(opt.getLength() > 1)
				Util.error("Only one global_variables tag allowed");


			//------------------------------
			//	 Load Commands
			//------------------------------

			opt = document.getElementsByTagName("commands");


			if(	opt.getLength() == 1 && opt.item(0) instanceof Element ){
				NodeList list = ((Element)opt.item(0)).getElementsByTagName("command");
				cmdCollection = buildCommands(list);
			}
			else
				Util.error("Only one tag commands is admitted");


			//------------------------------
			//	 Load Profiles
			//------------------------------

			opt = document.getElementsByTagName("cmd_profiles");


			if(	opt.getLength() == 1 && opt.item(0) instanceof Element ){
				NodeList list = ((Element)opt.item(0)).getElementsByTagName("cmd_profile");
				cmdProfilesCollection = buildCmdProfiles(list);
			}
			else
				Util.error("Only one tag cmd_profiles is admitted");



			//-----------------------------------
			//	 build Benchmark configuration
			//-----------------------------------

			opt = document.getElementsByTagName("benchmarks");

			if(	opt.getLength() == 1 && opt.item(0) instanceof Element ){
				NodeList list = ((Element)opt.item(0)).getElementsByTagName("benchmark");
				benchmarksList = buildBenchmarksConfig(list);
			}
			else
				Util.error("Only one tag cmd_profiles is admitted");


			checkIncoherencies();
			
			executor.setBenchmarks(benchmarksList);

			return executor;

		} catch (Exception e){
			e.printStackTrace();
			throw new SLIB_Ex_Critic(e.getMessage());
		}
	}



	private void checkIncoherencies() throws SLIB_Ex_Critic {
		// Check basic incoherencies 
		// cmd id duplicate

		for(int i = 0; i < cmdCollection.size();i++){
			for(int j = i+1; j < cmdCollection.size();j++){

				if(cmdCollection.get(i).getName().equalsIgnoreCase(cmdCollection.get(j).getName())){
					throw new SLIB_Ex_Critic("Duplicate Command id (Ignore case) "+cmdCollection.get(i).getName());
				}
			}	
		}

		// cmd profiles id duplicate

		for(int i = 0; i < cmdProfilesCollection.size();i++){
			for(int j = i+1; j < cmdProfilesCollection.size();j++){


				if(cmdProfilesCollection.get(i).getName().equalsIgnoreCase(cmdProfilesCollection.get(j).getName())){
					throw new SLIB_Ex_Critic("Duplicate Command profiles id (Ignore case) "+cmdProfilesCollection.get(i).getName());
				}
			}	
		}

		// benchmarks id duplicates

		for(int i = 0; i < benchmarksList.size();i++){
			for(int j = i+1; j < benchmarksList.size();j++){

				if(benchmarksList.get(i).getName().equalsIgnoreCase(benchmarksList.get(j).getName())){
					throw new SLIB_Ex_Critic("Duplicate Benchmark id (Ignore case) "+benchmarksList.get(i).getName());
				}
			}	
		}
	}



	private LinkedList<Command> buildCommands(NodeList list) throws SLIB_Ex_Critic {

		LinkedList<Command> listCmds = new LinkedList<Command>();


		for(int i = 0; i < list.getLength(); i++){

			if(list.item(i) instanceof Element){ // command tag expected

				String name, cmd_pattern = null;
				Map<String,String> vars  = new HashMap<String, String>();

				Element e =  (Element) list.item(i);

				// Load name
				name = e.getAttribute("name").trim();

				// Load default variables
				NodeList default_params = e.getElementsByTagName("default_params");

				if(	default_params.getLength() == 1 && default_params.item(0) instanceof Element ){

					NodeList listVars = ((Element) default_params.item(0)).getElementsByTagName("var");
					vars = convertToMap( buildEntries(listVars) );
				}
				if(default_params.getLength() > 1)
					Util.error("Only one default_params commands is admitted for command "+name);

				// Load command pattern
				NodeList cmd = e.getElementsByTagName("cmd");

				if(	cmd.getLength() == 1 && cmd.item(0) instanceof Element ){

					cmd_pattern = ((Element) cmd.item(0)).getAttribute("value").trim();
				}
				else 
					Util.error("Only one cmd commands is admitted");


				Command c = new Command(name, cmd_pattern);
				c.setDefaultVariables(vars);

				logger.info(""+c);

				listCmds.add(c);
			}
		}
		return listCmds;
	}


	private LinkedList<CmdProfile> buildCmdProfiles(NodeList list) throws SLIB_Ex_Critic {


		LinkedList<CmdProfile> listCmdProfiles = new LinkedList<CmdProfile>();

		for(int i = 0; i < list.getLength(); i++){



			if(list.item(i) instanceof Element){ // cmd_profile tag expected



				String name = null;
				List<String> commands_id = new ArrayList<String>();

				Element e =  (Element) list.item(i);

				// Load name
				name = e.getAttribute("name").trim();
				
				// Load type
				String type  = e.getAttribute("type").trim();

				// Load commands associated to the profile
				NodeList commands = e.getElementsByTagName("command");

				for(int j = 0; j < commands.getLength(); j++){

					if(commands.item(j) instanceof Element){
						Element cmd =  (Element) commands.item(j);
						String p = cmd.getAttribute("value").trim();
						commands_id.add(p);
					}
				}

				// build command profile
				CmdProfile profile = new CmdProfile(name,type);

				LinkedList<Command> workflow = new LinkedList<Command>();

				// Locate command object
				for(String cmd_id : commands_id){

					boolean f = false;
					for(Command c : cmdCollection){

						if(c.getName().equals(cmd_id)){
							f = true;
							workflow.add(c);
							break;
						}
					}

					if(!f)
						throw new SLIB_Ex_Critic("Cannot find command '"+cmd_id+"' used in profile  "+name);
				}
				profile.setWorkflow(workflow);
				listCmdProfiles.add(profile);
			}
		}
		return listCmdProfiles;
	}


	private LinkedList<Benchmark> buildBenchmarksConfig(NodeList list) throws SLIB_Ex_Critic {


		LinkedList<Benchmark> listBenchmarks = new LinkedList<Benchmark>();

		for(int i = 0; i < list.getLength(); i++){

			if(list.item(i) instanceof Element){ // benchmark tag expected



				String name = null, type = null;
				Map<String,String> vars  = new HashMap<String, String>();
				Set<BenchmarkInput> inputFiles = new HashSet<BenchmarkInput>();

				Element e =  (Element) list.item(i);

				// Load name
				name = e.getAttribute("name").trim();
				type = e.getAttribute("type").trim();


				// Load parameters associated to the benchmarks -----------------------------------------------
				NodeList params = e.getElementsByTagName("params");


				if(	params.getLength() == 1 && params.item(0) instanceof Element ){

					NodeList listVars = ((Element) params.item(0)).getElementsByTagName("var");
					vars = convertToMap( buildEntries(listVars) );
				}
				else
					Util.error("Only one params commands is admitted on benchmark definition "+name);


				// Load files associated to the benchmarks -----------------------------------------------
				NodeList files = e.getElementsByTagName("input_files");


				if(	files.getLength() == 1 && files.item(0) instanceof Element ){

					NodeList listVars = ((Element) files.item(0)).getElementsByTagName("file");
					inputFiles = buildBenchmarkInput(listVars) ;
				}
				else
					Util.error("Only one params files is admitted on benchmark definition "+name);
				
				
				// Load preprocessing command -----------------------------------------------
				LinkedList<Command> preprocessingCmd = new LinkedList<Command>();
				
				NodeList preprocessCmd = e.getElementsByTagName("preprocess_cmds");
				
				if(	preprocessCmd.getLength() == 1 && preprocessCmd.item(0) instanceof Element ){

					NodeList listCmd = ((Element) preprocessCmd.item(0)).getElementsByTagName("command");
					
					for(int j = 0; j < listCmd.getLength(); j++){

						if(listCmd.item(j) instanceof Element){
							Element cmd =  (Element) listCmd.item(j);
							String cmdPattern = cmd.getAttribute("value").trim();
							Command c = new Command("preprocessing cmd "+j, cmdPattern);
							preprocessingCmd.add(c);
						}
					}
				}
				else if(preprocessCmd.getLength() > 1)
					Util.error("Only one params preprocessCmd is admitted on benchmark definition "+name);

				// Load postprocessing command -----------------------------------------------
				LinkedList<Command> postprocessingCmd = new LinkedList<Command>();
				
				NodeList postprocessCmd = e.getElementsByTagName("postprocess_cmds");
				
				if(	postprocessCmd.getLength() == 1 && postprocessCmd.item(0) instanceof Element ){

					NodeList listCmd = ((Element) postprocessCmd.item(0)).getElementsByTagName("command");
					
					for(int j = 0; j < listCmd.getLength(); j++){

						if(listCmd.item(j) instanceof Element){
							Element cmd =  (Element) listCmd.item(j);
							String cmdPattern = cmd.getAttribute("value").trim();
							Command c = new Command("preprocessing cmd "+j, cmdPattern);
							postprocessingCmd.add(c);
						}
					}
				}
				else if(postprocessCmd.getLength() > 1)
					Util.error("Only one params postprocessCmd is admitted on benchmark definition "+name);


				if(	files.getLength() == 1 && files.item(0) instanceof Element ){

					NodeList listVars = ((Element) files.item(0)).getElementsByTagName("file");
					inputFiles = buildBenchmarkInput(listVars) ;
				}
				else
					Util.error("Only one params files is admitted on benchmark definition "+name);


				// Load profile associated to the benchmark -----------------------------------------------
				NodeList profile = e.getElementsByTagName("profile");
				LinkedList<CmdProfile> profiles = null;

				if(	profile.getLength() == 1 && profile.item(0) instanceof Element ){

					NodeList cmd_profile = ((Element) profile.item(0)).getElementsByTagName("cmd_profile");
					profiles = buildProfile(cmd_profile);
				}
				else
					Util.error("Only one params profiles is admitted on benchmark definition "+name);


				Benchmark b = new Benchmark(name);
				b.setType(type);
				b.setLocalVariables(vars);
				b.setInputFiles(inputFiles);
				b.setCmdsProfile(profiles);
				b.setPreProcessCmd(preprocessingCmd);
				b.setPostProcessCmd(postprocessingCmd);
				listBenchmarks.add(b);
			}
		}
		return listBenchmarks;

	}

	private LinkedList<CmdProfile> buildProfile(NodeList list) throws SLIB_Ex_Critic {


		LinkedList<CmdProfile> profiles = new LinkedList<CmdProfile>();
		
		for(int i = 0; i < list.getLength(); i++){

			if(list.item(i) instanceof Element){ // command tag expected


				Element e =  (Element) list.item(i);

				String cmd_profile_id   = e.getAttribute("name").trim();
				String cmd_profile_type = e.getAttribute("type").trim();
				
				CmdProfile p = new CmdProfile(cmd_profile_id, cmd_profile_type);

				boolean f = false;
				for(CmdProfile pp : cmdProfilesCollection){
					if(cmd_profile_id.equalsIgnoreCase(pp.getName())){
						f = true;
						p.setWorkflow(pp.getWorkflow());
						break;
					}

				}
				// search on default cmd_profile (hook)
				if(!f){
					for(String hook : executor.getDefaultHooksNames()){
						
						if(cmd_profile_type.equalsIgnoreCase(hook)){
							f = true;
							break;
						}
					}
				}
				
				if(!f)
					throw new SLIB_Ex_Critic("Cannot find cmd_profile id "+cmd_profile_id+"  type "+cmd_profile_type+" in benchmarks ");

				profiles.add(p);
			}
		}
		return profiles;
	}
	

	private Set<BenchmarkInput> buildBenchmarkInput(NodeList list) {

		Set<BenchmarkInput> inputs = new HashSet<BenchmarkInput>();


		for(int i = 0; i < list.getLength(); i++){

			if(list.item(i) instanceof Element){ // command tag expected


				Element e =  (Element) list.item(i);

				String path = null, type = null;

				path = e.getAttribute("path").trim();
				type = e.getAttribute("type").trim();

				BenchmarkInput input = new BenchmarkInput(path, type);
				inputs.add(input);

			}
		}
		return inputs;
	}



	private Map<String,String> convertToMap( LinkedHashSet<Entry<String, String>> buildEntries) {
		Map<String,String> vars  = new HashMap<String, String>();
		for(Entry<String,String> c : buildEntries)
			vars.put(c.getKey(),c.getValue());

		return vars;
	}

	private LinkedHashSet<Entry<String, String>> buildEntries(NodeList list) throws SLIB_Ex_Critic {

		LinkedHashSet<Entry<String, String>> gConfSet = new LinkedHashSet<Entry<String, String>>();

		for(int i = 0; i < list.getLength(); i++){

			if(list.item(i) instanceof Element){
				Element e =  (Element) list.item(i);
				String p = e.getAttribute("key").trim();
				String v = e.getAttribute("value").trim();

				gConfSet.add(new MyEntry(p, v));
			}
		}
		return gConfSet;
	}

	public String getAttValue(Element e, String a){

		String value = null;

		if(e.hasAttribute(a)){
			value   = e.getAttribute(a);
		}
		return value;
	}

	public static void main(String[] args) throws SLIB_Ex_Critic, IOException, InterruptedException {

		SmlDeployXMLLoader loader = new SmlDeployXMLLoader();


		CmdExecutor cmdExecutor = loader.loadConf("/home/seb/desktop/benchmarks/auto/conf_benchmarks.xml");
		cmdExecutor.runAllBenchmarks();
	}

}



final class MyEntry implements Map.Entry<String, String> {

	private final String key;
	private String value;

	public MyEntry(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

	public String setValue(String value) {
		String old = this.value;
		this.value = value;
		return old;
	}

}
