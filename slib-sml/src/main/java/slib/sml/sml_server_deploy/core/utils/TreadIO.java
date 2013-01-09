package slib.sml.sml_server_deploy.core.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author seb
 */
public class TreadIO extends Thread{

	Logger logger = LoggerFactory.getLogger(this.getClass());
	InputStream inputstream;
	boolean isErrorIO;
	int b_id;
	int b_number;
	
	/**
     *
     * @param inputstream
     * @param isErrorIO
     * @param b_id
     * @param b_numbers
     */
    public TreadIO(InputStream inputstream,boolean isErrorIO,int b_id,int b_numbers){
		this.inputstream = inputstream;
		this.isErrorIO = isErrorIO;
		this.b_id 	  = b_id;
		this.b_number = b_numbers;
	}
	
	public void run() {
		
		String prefix = "[STDOUT "+b_id+":"+b_number+"]";
		if(isErrorIO)
			prefix = "[STDERR  "+b_id+":"+b_number+"]";
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
			String line = "";
			try {
				while((line = reader.readLine()) != null) {
					logger.info(prefix+" "+line);
				}
			} finally {
				reader.close();
			}
		} catch(IOException ioe) {
			ioe.printStackTrace();
			logger.info("Error : "+ioe.getMessage());
		}
	}
}
