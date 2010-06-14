package edu.kit.ksri.lids.interfacing;

import java.net.URI;
import java.util.List;
import java.util.Map;

import edu.kit.ksri.lids.model.Variable;

public interface Transformator {

	public String transform(URI uri, String input, Map<Variable, List<String>> params);
	
}
