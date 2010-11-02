package org.openlids.dbpwrap;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONExtractor {


	public static void main(String args[]) throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();

		Map<String,String> hpToNr = new HashMap<String,String>();

		String fileNames[] = {"dbpediadata/nrstuds.json","dbpediadata/nrunder.json"};
		for(String fileName : fileNames) {
			JSONObject res = (JSONObject) parser.parse(new FileReader(fileName));

			List bindings = (List) ((JSONObject) res.get("results")).get("bindings");



			for(Object b : bindings) {
				JSONObject binding = (JSONObject) b;
				String uni = (String) ((JSONObject) binding.get("s")).get("value");
				String nr = (String) ((JSONObject) binding.get("n")).get("value");
				String hp = (String) ((JSONObject) binding.get("h")).get("value");
				if(hpToNr.containsKey(hp)) {
					if(Integer.parseInt(hpToNr.get(hp)) < Integer.parseInt(nr)) {
						hpToNr.put(hp, nr);
					}
				} else {
					hpToNr.put(hp,nr);
				}
			}
		}

		FileWriter out = new FileWriter("src/org/openlids/dbpwrap/NRHash.java");

		out.write("package org.openlids.dbpwrap;\n");
		out.write("import java.util.Map;\n");
		out.write("import java.util.HashMap;\n");
		
		out.write("public class NRHash { \n");
		out.write("static Map<String,String> hpToNr = new HashMap<String,String>(); \n");
		out.write("static { \n");
		for(String hp : hpToNr.keySet()) {
			out.write("hpToNr.put(\"" + hp + "\", \"" + hpToNr.get(hp) + "\");\n");
			System.out.println(hp + " => " + hpToNr.get(hp));
		}
		out.write("}\n}\n");
		out.close();
	}
}
