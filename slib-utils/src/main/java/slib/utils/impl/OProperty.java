package slib.utils.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Sébastien Harispe
 */
public class OProperty {

    /**
     *
     */
    protected Map<String, Object> properties = null;

    /**
     *
     * @param key
     * @return the property associated to the key.
     */
    public Object getProperty(String key) {
        if (properties == null) {
            return null;
        } else {
            return properties.get(key);
        }
    }

    /**
     *
     * @return the set of keys
     */
    public Set<String> getPropertyKeys() {
        if (properties == null) {
            return Collections.emptySet();
        } else {
            return properties.keySet();
        }
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<String, Object>();
        } else {
            properties.put(key, value);
        }
    }

    /**
     *
     * @param key
     * @return the value associated to the removed key.
     */
    public Object removeProperty(String key) {
        if (properties == null) {
            return null;
        } else {
            return properties.remove(key);
        }
    }
}
