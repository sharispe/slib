package slib.utils.i;

import java.util.Map;

public interface Parametrable{

	public void addParameter(String name, Object o);
	public Object getParameter(String name);
	public boolean existsParam(String pname);
	public void removeParameter(String name);
	public void clear();
	
	public Map<String,Object> getParams();
}
