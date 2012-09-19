package slib.sglib.io.conf;

import slib.sglib.io.util.GDataValidator;
import slib.sglib.io.util.GFormat;
import slib.utils.i.CheckableValidity;
import slib.utils.impl.ParametrableImpl;


public class GDataConf extends ParametrableImpl implements CheckableValidity{

	private GFormat format;
	private String  loc;
	
	public GDataConf(GFormat format, String loc){
		this.format = format;
		this.loc = loc;
	}

	public GFormat getFormat() {
		return format;
	}

	public void setFormat(GFormat format) {
		this.format = format;
	}

	public String getLoc() {
		return loc;
	}

	public void setLoc(String loc) {
		this.loc = loc;
	}

	public boolean isValid() {
		return GDataValidator.valid(this);
	}
	
	
	
}
