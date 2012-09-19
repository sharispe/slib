package slib.sml.sml_server_deploy.core.utils;

public class BenchmarkInput {
	
	String path;
	String type;
	
	public BenchmarkInput(String path,String type){
		this.path = path;
		this.type = type;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	

}
