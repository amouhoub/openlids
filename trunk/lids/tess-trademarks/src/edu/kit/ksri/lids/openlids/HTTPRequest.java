package edu.kit.ksri.lids.openlids;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class HTTPRequest {
	public enum HTTPMethod {
		POST, GET
	}
	HTTPMethod method = HTTPRequest.HTTPMethod.GET;
	
	Map<String,String> headers = new HashMap<String,String>();
	Map<String,List<String>> params = new HashMap<String,List<String>>();
	URL url;
	
	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}

	public void setGetMethod() {
		this.method = HTTPRequest.HTTPMethod.GET;
	}
	
	public void setPostMethod() {
		this.method = HTTPRequest.HTTPMethod.POST;
	}
	
	public void setMethod(HTTPMethod method) {
		this.method = method;
	}
	
	public HTTPMethod getMethod() {
		return method;
	}
	
	public void addHeader(String key, String value) {
		headers.put(key, value);
	}
	
	public void addParam(String key, String value) {
		List<String> values = params.get(key);
		if(values == null) {
			values = new LinkedList<String>();
			params.put(key, values);
		}
		values.add(value);
	}
	
	public Map<String,String> getHeaders() {
		return headers;
	}
	
	public Map<String,List<String>> getParams() {
		return params;
	}
	
	
}
