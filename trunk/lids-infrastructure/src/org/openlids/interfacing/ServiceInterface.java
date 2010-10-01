package org.openlids.interfacing;
import java.net.URI;
import java.util.List;
import java.util.Map;

import org.openlids.model.Variable;



public interface ServiceInterface {
	
	String call(URI uri, Map<Variable, List<String>> params);

}