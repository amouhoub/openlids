package org.openlids.registry;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.google.appengine.api.datastore.Key;

@Entity
public class LidsDescription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

    private String[] PARAM = { "base", "lidsrdf", "example", "title", "comment" };
    
    int BASE = 0;
    int LIDSRDF = 1;
    int EXAMPLE = 2;
    int TITLE = 3;
    int COMMENT = 4;

    private String[] _value = new String[PARAM.length];
    
    public void parse(Map<String, String[]> map) throws LidsParseException {
    	for (String k : map.keySet()) {
    		String[] vs = map.get(k);
    		
    		String v = vs[0];
    		for (int i = 0; i < PARAM.length; i++) {
    			if (PARAM[i].equals(k)) {
    				_value[i] = v;
    			}
    		}
    	}
    	
    	if (_value[EXAMPLE] != null) {
    		try {
				URI u = new URI(_value[EXAMPLE]);
			} catch (URISyntaxException e) {
	    		throw new LidsParseException("cannot parse " + _value[EXAMPLE] + " " + e.getMessage());
			}
    	}
    	
    	if (_value[LIDSRDF] != null) {
    		try {
				URI u = new URI(_value[LIDSRDF]);
			} catch (URISyntaxException e) {
	    		throw new LidsParseException("cannot parse " + _value[LIDSRDF] + " " + e.getMessage());
			}
    	} else {
    		throw new LidsParseException("need to specify at least a LIDS description URI (parameter lidsrdf)");
    	}
    }

    public Key getKey() {
        return key;
    }
    
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	
		for (int i = 0; i < PARAM.length; i++) {
			sb.append(PARAM[i]);
			sb.append(": ");
			sb.append(_value[i]);
			sb.append("\n");
		}
		
    	return sb.toString();
    }
    
    public String toXml() {
    	StringBuilder sb = new StringBuilder();

    	for (int i = 0; i < PARAM.length; i++) {
    		if (_value[i] != null) {
    			sb.append("<");
    			sb.append(PARAM[i]);
    			sb.append(">");
    			sb.append(_value[i]);
    			sb.append("</");
    			sb.append(PARAM[i]);
    			sb.append(">");
    			sb.append("\n");
    		}
		}
		
    	return sb.toString();
    }
}
