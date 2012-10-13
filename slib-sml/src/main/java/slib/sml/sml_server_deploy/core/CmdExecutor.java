package slib.sml.sml_server_deploy.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sml.sml_server_deploy.core.utils.Benchmark;
import slib.sml.sml_server_deploy.core.utils.BenchmarkInput;
import slib.sml.sml_server_deploy.core.utils.CmdProfile;
import slib.sml.sml_server_deploy.core.utils.Command;
import slib.sml.sml_server_deploy.core.utils.TreadIO;
import slib.utils.ex.SLIB_Ex_Critic;

public class CmdExecutor {


	String data_directory;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	Runtime runtime;

	Map<String,String> 	  globalVariables = new HashMap<String,String>();
	LinkedList<Benchmark> benchmarks 	  = new LinkedList<Benchmark>();
	
	/**
	 * Define variable which must be defined globally in the execution configuration
	 */
	public static final Map<String, String> requiredGlobalVariables = new HashMap<String, String>(){
		private static final long serialVersionUID = 1L; 

		{
			put("DATA_DIRECTORY" , "Where to load input data temporary files");
			put("BENCHMARK_DIR"  , "Variable classicaly used to specify the benchmark directory");
		}
	};
	
	/**
	 * Define variable which must be defined locally for each benchmark
	 */
	public static final Map<String, String> requiredLocalVariables = new HashMap<String, String>(){
		private static final long serialVersionUID = 1L; 

		{
			put("HOME"	  		 , "variable defining the directory to consider as the root of the benchmark");
			put("TODEL"	  		 , "variable defining if the benchmark require to be deleted when all the benchmark have been performed");
		}
	};
	

	final Pattern patternGlobalRgx 	  = Pattern.compile("\\{([^\\{^\\}]*)\\}"); 	// {VAR}
	final Pattern patternLocalRgx  	  = Pattern.compile("\\[([^\\[^\\]]*)\\]"); 	// [VAR]
	final Pattern patternTemplateRgx  = Pattern.compile("\\|\\|\\|([^\\|]*)\\|\\|\\|"); // |||VAR|||



	private List<String> benchmark_restrictions = null; // if null all benchmark are processed, if not only those specified will be processed 
	private List<String> profile_restrictions   = null; // if null all profiles are processed, if not only those specified will be processed 

	int current_benchmark_id = 0;

	final static String DEF_HOOK_DOC 		 = "[[DOC]]";
	final static String DEF_HOOK_INPUT_FILES = "[[LOAD_INPUT_FILES]]";
	final static String DEF_HOOK_CONF 		 = "[[GENERATE_CONF]]";
	final static String DEF_HOOK_PREPROCESS  = "[[PREPROCESS]]";
	final static String DEF_HOOK_POSTPROCESS = "[[POSTPROCESS]]";

	private String[] defaultHooks = {DEF_HOOK_DOC,DEF_HOOK_INPUT_FILES,DEF_HOOK_PREPROCESS,DEF_HOOK_POSTPROCESS,DEF_HOOK_CONF};


	
	public CmdExecutor(){
		runtime = Runtime.getRuntime();
	}

	public void addBenchmark(Benchmark b){
		benchmarks.add(b);
	}


	private void run(Benchmark benchmark) throws SLIB_Ex_Critic, IOException, InterruptedException {


		logger.info("------------------------------------------------");
		logger.info("--- Benchmark "+benchmark.getName());
		logger.info("------------------------------------------------");

		logInfo("Local Variables "+benchmark.getLocalVariables());

		checkRequiredLocalVariables(benchmark);
		
		// Apply default hook
		defaultHooks(benchmark);

		if(noRestrictionOnProfile(DEF_HOOK_INPUT_FILES)){
			logInfo("Copying files");
			for(BenchmarkInput i : benchmark.getInputFiles())
				loadToDataDirectory(benchmark,i);
		}
		else
			logInfo("Skipping hook "+DEF_HOOK_INPUT_FILES);

		if(noRestrictionOnProfile(DEF_HOOK_PREPROCESS)){
			logInfo("#Preprocessing Commands "+benchmark.getPreProcessCmd().size());

			for(Command c : benchmark.getPreProcessCmd())
				run(benchmark,c);
		}
		else
			logInfo("Skipping hook "+DEF_HOOK_PREPROCESS);

		LinkedList<CmdProfile> profiles = benchmark.getCmdsProfile();

		for(CmdProfile profile : profiles){

			if(noRestrictionOnProfile(profile.getType())){

				logInfo("Executing Profile "+profile.getName()+"  type "+profile.getType());
				int c = 1, size = profile.getWorkflow().size();

				for(Command cmd : profile.getWorkflow()){
					logInfo("Command "+c+"/"+size);
					run(benchmark,cmd);
					c++;
				}
			}
			else
				logInfo("Skipping hook "+profile.getType());
		}

		if(noRestrictionOnProfile(DEF_HOOK_POSTPROCESS)){
			logInfo("#Postprocessing Commands "+benchmark.getPostProcessCmd().size());

			for(Command cc : benchmark.getPostProcessCmd())
				run(benchmark,cc);
		}
		else
			logInfo("Skipping hook "+DEF_HOOK_POSTPROCESS);
	}

	private void defaultHooks(Benchmark b) throws SLIB_Ex_Critic {

		logInfo("Apply default hooks for benchmarks type "+b.getType());

		mkdir(applyPatterns(b,getVariable(b,"HOME")));
		mkdir(applyPatterns(b,getVariable(b,"HOME")+"/conf"));
		mkdir(applyPatterns(b,getVariable(b,"HOME")+"/results"));




		if(		b.getType().equals("GO_SM_SMBB") || 
				b.getType().equals("GO_SMBB")){

			if(noRestrictionOnProfile(DEF_HOOK_DOC))
				generateConfFromTemplate(b,"README_TEMPLATE","README_FILE");

			if(noRestrictionOnProfile(DEF_HOOK_CONF))
				generateConfFromTemplate(b,"SMBB_XML_TEMPLATE","SML_SMBB_XML_CONF");

		}

		if		(b.getType().equals("GO_SM") ||
				b.getType().equals("GO_SM_SMBB")){

			if(noRestrictionOnProfile(DEF_HOOK_DOC))
				generateConfFromTemplate(b,"README_TEMPLATE","README_FILE");

			if(noRestrictionOnProfile(DEF_HOOK_CONF)){
				generateConfFromTemplate(b,"XML_WIKI_TEMPLATE","XML_CONF");
				generateConfFromTemplate(b,"SM_XML_TEMPLATE","SML_SM_XML_CONF");
			}
		}

	}

	private boolean noRestrictionOnProfile(String string) {

		if(profile_restrictions == null || profile_restrictions.size() == 0)
			return true;
		else{

			for (String p : profile_restrictions) {
				if(string.equalsIgnoreCase(p))
					return true;
			}
			return false;
		}

	}

	private void generateConfFromTemplate(Benchmark b,String templateVarName, String produceConfVarName) throws SLIB_Ex_Critic {

		logInfo("Generating configuration file "+templateVarName);

		String smbb_xml_template_file_path = getVariable(b,templateVarName);

		if(smbb_xml_template_file_path == null)
			throw new SLIB_Ex_Critic("Cannot state required variable "+templateVarName);


		smbb_xml_template_file_path = applyPatterns(b,smbb_xml_template_file_path);

		logInfo(templateVarName+" --> "+smbb_xml_template_file_path);


		String output = buildConfFromTemplate(b,smbb_xml_template_file_path);


		String output_template = getVariable(b,produceConfVarName);

		if(output_template == null)
			throw new SLIB_Ex_Critic("Cannot state required variable "+produceConfVarName);

		output_template = applyPatterns(b,output_template);

		logInfo("Output --> "+output_template);

		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(output_template));
			out.write(output);
			out.close();
		} catch (IOException e) {
			throw new SLIB_Ex_Critic(e);
		}

		logInfo("Conf build from template");
	}

	private void mkdir(String path) {
		File dirConf = new File(path);
		if(!dirConf.exists())
			dirConf.mkdir();
	}

	private String buildConfFromTemplate(Benchmark b,String templatepath) throws SLIB_Ex_Critic {

		String out = "";
		try{
			FileInputStream fstream = new FileInputStream(templatepath);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String l;

			while ((l = br.readLine()) != null)   {


				Matcher m = patternTemplateRgx.matcher(l);

				String line = l;

				while(m.find()) {

					System.out.println(l);

					String vName = m.group(1);
					String value = applyPatterns(b,getVariable(b,vName));


					System.out.println("Detect pattern "+vName);
					System.out.println("Value "+value);

					if(value == null){

						throw new SLIB_Ex_Critic("Undefined pattern '"+vName+"' used in template "+templatepath+" please define the corresponding pattern.");
					}

					line = line.replaceAll("\\|\\|\\|"+vName+"\\|\\|\\|", value);

				}

				//				System.out.println(line);
				out += line+"\n";
			}
			System.out.println("~~~~~~~~~~~~");
			System.out.println(out);


			in.close();
		}catch (Exception e){
			throw new SLIB_Ex_Critic(e);
		}
		return out;
	}

	private String getVariable(Benchmark benchmark,String var) {

		if(benchmark.getLocalVariables().containsKey(var))
			return benchmark.getLocalVariables().get(var);
		else
			return globalVariables.get(var);

	}

	private void loadToDataDirectory(Benchmark b,BenchmarkInput i) throws SLIB_Ex_Critic, IOException, InterruptedException {

		String filepath = applyPatterns(b,i.getPath());
		File f = new File(filepath);

		String[] tmp  = filepath.split("/");
		String 	fname = tmp[tmp.length-1];

		File ftarget = new File(data_directory+"/"+fname);

		if(!f.exists())
			throw new SLIB_Ex_Critic("Cannot locate file '"+filepath+"'");


		if(!ftarget.exists()){
			logInfo("Copying file "+filepath);
			run("cp "+filepath+" "+data_directory);

			if(i.getType().equalsIgnoreCase("ZIP")){
				filepath = data_directory+"/"+fname;
				logInfo("Unzip "+filepath+" to "+data_directory);
				run("unzip "+filepath+" -d "+data_directory);
			}

		}
	}

	private void run(Benchmark b,Command command) throws SLIB_Ex_Critic, IOException, InterruptedException {
		logInfo("Executing command "+command.getName());
		run(applyPatterns(b,command));

	}

	private void run(String cmd) throws IOException, InterruptedException, SLIB_Ex_Critic{

		cmd = cmd.trim();

		String[] args = buildCmd(cmd);
		logInfo("Execute "+Arrays.toString(args));
		final Process process = runtime.exec(args);

		TreadIO tOut   = new TreadIO(process.getInputStream(), false,current_benchmark_id,benchmarks.size());
		TreadIO tError = new TreadIO(process.getErrorStream(), true, current_benchmark_id,benchmarks.size());

		tOut.start();
		tError.start();

		tOut.join();
		tError.join();

		int status = process.waitFor();
		logInfo("CMD status "+status);
		logInfo("Done 		"+cmd);

		if(status != 0)
			throw new SLIB_Ex_Critic("Error abnormal exit status executing command '"+cmd+"', consult log");
	}

	private String[] buildCmd(String cmd) {
		logInfo("Build command line from '"+cmd+"'");
		return cmd.split("\\s");

	}

	public void addGlobalVariable(String key,String value){
		globalVariables.put(key, value);
	}



	private String applyPatterns(boolean isGlobalPattern,Map<String,String> variables, String v) throws SLIB_Ex_Critic{


		Pattern p = patternGlobalRgx;
		String scopeInfo = "GLOBAL";

		if(!isGlobalPattern){
			p = patternLocalRgx;
			scopeInfo = "LOCAL";
		}

		Matcher m = p.matcher(v);

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

				throw new SLIB_Ex_Critic("Undefined "+scopeInfo+" pattern '"+vName+"' used in "+v+" please define the corresponding pattern." +
						"\nLoaded patters " + loadedPatters);
			}

			if(isGlobalPattern)
				newValue = newValue.replaceAll("\\{"+vName+"\\}",value);
			else
				newValue = newValue.replaceAll("\\["+vName+"\\]",value);
		}
		return newValue;
	}


	private String applyPatterns(Benchmark b, Command cmd) throws SLIB_Ex_Critic {

		String cmd_build = cmd.getCommandPattern();


		logInfo("Aplying patterns : "+cmd_build);
		int attemps = 0;
		
		boolean GLOBAL_PATTERN = true;
		boolean LOCAL_PATTERN  = false;

		while(existsPattern(cmd_build)){

			cmd_build = applyPatterns(GLOBAL_PATTERN,globalVariables, cmd_build);

			Map<String, String> cmdDefaultANDLocalVars = new HashMap<String, String>(cmd.getDefaultVariables());
			cmdDefaultANDLocalVars.putAll(b.getLocalVariables());

			cmd_build = applyPatterns(LOCAL_PATTERN, cmdDefaultANDLocalVars,   cmd_build);

			logInfo("Patterns applied  : "+cmd_build);
			attemps++;

			if(attemps == 10)
				throw new SLIB_Ex_Critic("Unable to compile the following command pattern using 10 iterations. " +
						"Note that recursive definitions are not allowed.\n"+cmd.getCommandPattern());
		}
		return cmd_build;	
	}

	private String applyPatterns(Benchmark b,String cmd) throws SLIB_Ex_Critic {

		//		logInfo("Applying patterns : "+cmd);
		int attemps = 0;
		if(cmd == null)
			return null;
		
		boolean GLOBAL_PATTERN = true;
		boolean LOCAL_PATTERN  = false;

		while(existsPattern(cmd)){
			cmd = applyPatterns(GLOBAL_PATTERN,globalVariables, cmd);

			if(b!= null)
				cmd = applyPatterns(LOCAL_PATTERN,b.getLocalVariables(),cmd);
			//			logInfo("Patterns applied  : "+cmd);
			attemps++;

			if(attemps == 10)
				throw new SLIB_Ex_Critic("Unable to compile the following command pattern using 10 iterations. " +
						"Note that recursive definitions are not allowed.\n"+cmd);
		}

		return cmd;	
	}

	private boolean existsPattern(String cmd_build) {

		if(patternGlobalRgx.matcher(cmd_build).find() || patternLocalRgx.matcher(cmd_build).find())
			return true;
		return false;
	}


	public void runAllBenchmarks() throws SLIB_Ex_Critic, IOException, InterruptedException {

		applyBenchmarkRestrictions();

		int b_size = benchmarks.size();
		
		logger.info("Running "+b_size+" benchmarks");


		checkRequiredGlobalVariables();
		
		logger.info("Checking benchmarks' local variables");
		for(Benchmark b : benchmarks)
			checkRequiredLocalVariables(b);
			
		preprocessing();


		int benchmark_errors = 0;

		for (int i = 0; i < b_size; i++) {

			current_benchmark_id = i+1;

			logInfo("Running benchmark "+current_benchmark_id+"/"+benchmarks.size());

			try{
				run(benchmarks.get(i));
			}
			catch(Exception e){
				e.printStackTrace();
				System.exit(0);
				benchmark_errors++;
				logInfo(e.getMessage());
				logInfo("Error executing benchmark "+benchmarks.get(i).getName()+", process skipped consult log for error checking");
			}
		}

		logger.info("Remove tempory directories");

		Iterator<Benchmark> it = benchmarks.iterator();

		while(it.hasNext()){
			Benchmark b = it.next();

			if(b.getLocalVariables().containsKey("TODEL") && b.getLocalVariables().get("TODEL").equalsIgnoreCase("TRUE")){
				logger.info("Remove : "+b.getName());
				run("rm -r "+applyPatterns(b,b.getLocalVariables().get("HOME")));
				it.remove();
			}
			else{
				logger.info("Keep : "+b.getName());
			}
		}

		generateFinalXMLinfo();


		if(benchmark_errors == 0){
			logger.info("Removing data directory");
			run("rm -r "+data_directory);

			logger.info("Everything seems fine ;)");
		}
		else{
			logger.info("Data directory not removed "+data_directory);
			logger.info("!!! Critical Warning : Errors have been detected please check logs before deploying results");
		}

		logger.info("------------------------------------------------");
		logger.info("Treatment performed : "+b_size+" processed");
		logger.info("Errors "+benchmark_errors+"/"+b_size);
		logger.info("------------------------------------------------");
	}


	/**
	 * Remove benchmarks not specified in the restriction from the current object.
	 * The treatment is performed only if the restriction is not empty
	 */
	private void applyBenchmarkRestrictions() {
		
		// Remove benchmarks considering benchmark restrictions
		if(benchmark_restrictions != null && benchmark_restrictions.size() != 0){

			LinkedList<Benchmark> newList = new LinkedList<Benchmark>();
			for(String s: benchmark_restrictions){

				Benchmark c =null;
				Iterator<Benchmark> it = benchmarks.iterator();
				while(it.hasNext()){
					c = it.next();
					if(c.getName().equals(s)){
						break;
					}
					c =null;
				}
				if(c!=null){
					newList.add(c);
					logger.info("benchmark "+s+" will be processed");
					break;
				}
			}
			benchmarks  = newList;
		}
	}

	/**
	 * Generate the XML file containing information about all benchmarks
	 * @throws SGL_Ex_Critic
	 */
	private void generateFinalXMLinfo() throws SLIB_Ex_Critic {

		logger.info("Generate Final XML information file considering "+benchmarks.size()+" benchmark(s)");

		Set<String> taxonIds  = new HashSet<String>();
		for (Benchmark b : benchmarks) {

			if(b.getLocalVariables().containsKey("TAXON_ID")){
				taxonIds.add(b.getLocalVariables().get("TAXON_ID"));
			}
		}

		String xml_out = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n";
		xml_out += "<info>\n";
		xml_out += "\t<taxons>\n";

		try{

			for (String s : taxonIds) {

				String name 	= "";
				if(globalVariables.containsKey("TAXON_NAME_"+s))
					name 	= "name=\""+globalVariables.get("TAXON_NAME_"+s)+"\"";
				else
					logger.info("Add a global variable "+"TAXON_NAME_"+s+" to specify a name for the taxon "+s);

				String weblink = "";
				if(globalVariables.containsKey("TAXON_LINK_"+s)){

					String url = java.net.URLEncoder.encode(globalVariables.get("TAXON_LINK_"+s), "ISO-8859-1");
					weblink 	= "weblink=\""+url+"\"";
				}
				else
					logger.info("Add a global variable "+"TAXON_LINK_"+s+" to specify a web link for the taxon "+s);

				xml_out += "\t\t<taxon "+name+" id=\""+s+"\" "+weblink+" />\n";
			}
			xml_out += "\t</taxons>\n";

			xml_out += "\t<benchmarks>\n";



			for(Benchmark b : benchmarks){
				if(b.getLocalVariables().containsKey("XML_CONF")){


					FileInputStream fstream = new FileInputStream(applyPatterns(b, b.getLocalVariables().get("XML_CONF")));
					DataInputStream in = new DataInputStream(fstream);
					BufferedReader br = new BufferedReader(new InputStreamReader(in));
					String strLine;
					while ((strLine = br.readLine()) != null)
						xml_out += "\t\t\t"+strLine+"\n";
					in.close();


				}
			}
			xml_out += "\t</benchmarks>\n";
			xml_out += "</info>\n";
			System.out.println(xml_out);

			if(globalVariables.containsKey("XML_INFO")){
				String fpath = applyPatterns(null,globalVariables.get("XML_INFO"));
				FileWriter fstream = new FileWriter(fpath);
				BufferedWriter out = new BufferedWriter(fstream);
				out.write(xml_out);
				out.close();

				logger.info("Information flushed in "+fpath);
			}
			else{
				logger.info("Information not flushed because global variable XML_INFO was not found");
			}

		}catch (Exception e){//Catch exception if any
			throw new SLIB_Ex_Critic(e);
		}
	}

	/**
	 * Perform the treatment required as pre-processing
	 * @throws SGL_Ex_Critic
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void preprocessing() throws SLIB_Ex_Critic, IOException, InterruptedException {

		// Create the data directory if required
		File dataDir = new File(data_directory);
		dataDir.mkdir();
		
		// Clear (if required) and create the benchmark directory
		String benchdir = applyPatterns(null,globalVariables.get("BENCHMARK_DIR"));
		File dir = new File(benchdir);
		if(dir.exists())
			run("rm -r "+benchdir);

		mkdir(benchdir);

	}
	

	
	

	/**
	 * Check if required global variables are define and process some treatments
	 * @throws SGL_Ex_Critic
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void checkRequiredGlobalVariables() throws SLIB_Ex_Critic, IOException, InterruptedException {

		for(Entry<String, String> e : requiredGlobalVariables.entrySet()){
			if(!globalVariables.containsKey(e.getKey()))
				throw new SLIB_Ex_Critic("Missing GLOBAL variable : "+e.getKey()+"\n information : "+e.getValue());
		}

		data_directory = globalVariables.get("DATA_DIRECTORY");
	}
	
	private void checkRequiredLocalVariables(Benchmark b) throws SLIB_Ex_Critic, IOException, InterruptedException {

		for(Entry<String, String> e : requiredLocalVariables.entrySet()){
			if(!b.getLocalVariables().containsKey(e.getKey()))
				throw new SLIB_Ex_Critic("Missing LOCAL variable : "+e.getKey()+" in '"+b.getName()+"' benchmark configuration  \n information : "+e.getValue());
		}

		data_directory = globalVariables.get("DATA_DIRECTORY");
		
	}




	public LinkedList<Benchmark> getBenchmarks() {
		return benchmarks;
	}

	public void setBenchmarks(LinkedList<Benchmark> benchmarks) {
		this.benchmarks = benchmarks;
	}

	public Map<String, String> getGlobalVariables() {
		return globalVariables;
	}

	public void setGlobalVariables(Map<String, String> globalVariables) {
		this.globalVariables = globalVariables;
	}

	public String toString(){

		String out = "Global Variables \n"+globalVariables;
		return out;
	}

	public void logInfo(String s){
		logger.info("["+current_benchmark_id+":"+benchmarks.size()+"] "+s);
	}

	public void addBenchmarkRestriction(String benchmarkid){
		if(benchmark_restrictions == null)
			benchmark_restrictions = new LinkedList<String>();
		benchmark_restrictions.add(benchmarkid);
	}

	public List<String> getBenchmark_restrictions() {
		return benchmark_restrictions;
	}

	public void setBenchmarkRestrictions(List<String> benchmark_restrictions) {
		this.benchmark_restrictions = benchmark_restrictions;
	}

	public void setProfileRestrictions(List<String> profile_restrictions) {
		this.profile_restrictions = profile_restrictions;
	}

	public void addProfileRestriction(String profileFlag){
		if(profile_restrictions == null)
			profile_restrictions = new LinkedList<String>();
		profile_restrictions.add(profileFlag);
	}

	public String[] getDefaultHooksNames() {
		return defaultHooks ;

	}




}
