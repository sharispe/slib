package slib.utils.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class OProperty {
	
	protected Map<String,Object> properties = null;
	
	public Object getProperty(String key) {
		if(properties == null)
			return null;
		else
			return properties.get(key);
	}

	public Set<String> getPropertyKeys() {
		if(properties == null)
			return Collections.emptySet();
		else
			return properties.keySet();
	}

	public void setProperty(String key, Object value) {
		if(properties == null)
			properties = new HashMap<String, Object>();
		else
			properties.put(key, value);
	}

	public Object removeProperty(String key) {
		if(properties == null)
			return null;
		else
			return properties.remove(key);
	}

}
