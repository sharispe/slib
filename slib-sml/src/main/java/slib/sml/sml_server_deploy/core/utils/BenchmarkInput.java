package slib.sml.sml_server_deploy.core.utils;

/**
 *
 * @author seb
 */
public class BenchmarkInput {
	
	String path;
	String type;
	
	/**
     *
     * @param path
     * @param type
     */
    public BenchmarkInput(String path,String type){
		this.path = path;
		this.type = type;
	}

	/**
     *
     * @return
     */
    public String getPath() {
		return path;
	}

	/**
     *
     * @param path
     */
    public void setPath(String path) {
		this.path = path;
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
