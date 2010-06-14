package edu.kit.ksri.lids.interfacing;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


import java.util.logging.Logger;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;



import edu.kit.ksri.lids.model.BGP;
import edu.kit.ksri.lids.model.ServiceDescription;
import edu.kit.ksri.lids.model.Variable;
import edu.kit.ksri.lids.parser.ServiceParserJena;


public class ServiceWrapper {
	ServiceDescription desc;
	ServiceInterface interf;
	
	public ServiceWrapper(ServiceDescription desc, ServiceInterface interf) {
		this.desc = desc;
		this.interf = interf;
	}
	
	public String invoke(URI uri) {
		if(!uri.toString().startsWith(desc.getEndpoint().getName())) {
			System.err.println("Wrong endpoint: " + uri);
			return null; // "Wrong endpoint: " + uri;
		}
				
		Map<Variable,List<String>> params = getParams(uri.getRawQuery());
		if(params == null || params.size() == 0) {
			if(desc.getRequiredVars().size() != 1) {
				return null;
			}
			String parts[] = uri.toString().split("/");
			String singlePar = parts[parts.length-1];
			params = new HashMap<Variable,List<String>>();
			List<String> parlist = new LinkedList<String>();
			parlist.add(singlePar);
			params.put(desc.getRequiredVars().iterator().next(), parlist);
		}
		
		// Retrieval ... but first in Service Description, the required params and so forth must be determined
		
		Set<Variable> requiredVars = new HashSet<Variable>();
		for(Variable reqVar : desc.getRequiredVars()) {
			if(!params.containsKey(reqVar)) {
				requiredVars.add(reqVar);
			}
		}

		// just flood it
		// For  every given URI, retrieve it and saturate
		Set<Variable> retrievedParams = new HashSet<Variable>();
		final Set<Node[]> retrievedStmts = new HashSet<Node[]>();
		while(!requiredVars.isEmpty() && !retrievedParams.containsAll(params.keySet())) {
			for(Variable param : params.keySet()) {
				if(retrievedParams.contains(params)) continue;
				
				// See if it is bound to an URI
				if(params.get(param).size() < 1) {
					retrievedParams.add(param);
					continue;
				}
				String value = params.get(param).get(0);
				URI retrUri;
				try { 
					retrUri = new URI(value);
					if(!retrUri.isAbsolute() || (retrUri.getScheme() == null) || !retrUri.getScheme().startsWith("http"))
						throw new URISyntaxException("", "");
				} catch(URISyntaxException ux) {
					retrievedParams.add(param);
					continue;
				}
				URL retrURL;
				try {
					retrURL = retrUri.toURL();
				} catch (MalformedURLException e) {
					retrievedParams.add(param);
					continue;
				}
				URLConnection conn;
				String contentType;
				InputStream in;
				try {
					conn = retrURL.openConnection();
					contentType = conn.getContentType();
					in = conn.getInputStream();
					
					Callback cb = new Callback() {
						public void endDocument() {
						}
						public void processStatement(Node[] arg0) {
							retrievedStmts.add(arg0);
						}

						public void startDocument() {

						}
						
					};
					
					
					if(contentType.contains("xml")) {
						Iterator<Node[]> parser;
						parser = new RDFXMLParser(in,false,true,retrURL.toString(),cb);
					} else {
						Iterator<Node[]> parser;
						parser = new NxParser(in,cb);
					}
		
				} catch (IOException e) {
					retrievedParams.add(param);
					continue;
				} catch (ParseException e) {
					retrievedParams.add(param);
					continue;
				}
				
				// then evaluate providing patterns until we run out
				List<BGP> pbgps = desc.getProvides(param);
				
				for(Node[] stmt : retrievedStmts) {
				
					if(stmt[0].toString().equals(value)) {
						for(BGP bgp : pbgps) {
				
							if(stmt[1].toString().equals(bgp.getPredicate().getName())) {
								if(params.containsKey(bgp.getObject()) || !(bgp.getObject() instanceof Variable))
									continue;
								List<String> binding = new LinkedList<String>();
				
								binding.add(stmt[2].toString());
								params.put((Variable) bgp.getObject(), binding);
							}
						}
					}
					
					
				}
				retrievedParams.add(param);				
				// in any case of errors, mark it as retrieved
			}
		}
		
		
		
		for(Variable key : params.keySet()) {
			System.out.print(key + " => ");
			for(String value : params.get(key)) {
				System.out.print(" " + value);
			}
			System.out.println();
		}
		
		return interf.call(uri,params);
		
	}

	Map<Variable, List<String>> getParams(String query) {
		Map<Variable, List<String>> params = new HashMap<Variable, List<String>>();
		if(query == null || query.length() == 0 || query.equals("null")) return params;
		if(query.startsWith("?")) {
			query = query.substring(1);
		}
		for (String param : query.split("&")) {
			String[] pair = param.split("=");
	        String key;
	        String value;
			try {
				key = URLDecoder.decode(pair[0], "UTF-8");
				value = URLDecoder.decode(pair[1], "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			} catch(NullPointerException npe) {
				return null;
			} catch(ArrayIndexOutOfBoundsException aie) {
				Logger.getLogger("getParams").warning("Query: " + query + "\nPair: " + param);
				return null;
			}
	        List<String> values = params.get(key);
	        if (values == null) {
	            values = new ArrayList<String>();
	            Variable v = new Variable();
	            v.setName(key);
	            params.put(v, values);
	        }
	        values.add(value);
	    }
		return params;
	}	
}
