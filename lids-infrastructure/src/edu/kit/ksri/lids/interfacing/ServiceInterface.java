package edu.kit.ksri.lids.interfacing;
import java.net.URI;
import java.util.List;
import java.util.Map;

import edu.kit.ksri.lids.model.Variable;


public interface ServiceInterface {
	
	String call(URI uri, Map<Variable, List<String>> params);

}
