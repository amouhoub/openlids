package edu.kit.ksri.lids.openlids;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;

import edu.kit.ksri.lids.interfacing.ServiceInterface;
import edu.kit.ksri.lids.interfacing.Transformator;
import edu.kit.ksri.lids.model.Variable;


public abstract class HTTPServiceInterface implements ServiceInterface {

	Transformator trans;
	
	
	public Transformator getTransformator() {
		return trans;
	}


	public void setTransformator(Transformator trans) {
		this.trans = trans;
	}


	public abstract HTTPRequest createRequest(Map<Variable, List<String>> params);
	
	
	
	public String call(URI uri, Map<Variable, List<String>> params) {
		String result = "";
		
		HTTPRequest req = createRequest(params);
		
		com.google.appengine.api.urlfetch.HTTPMethod method = HTTPMethod.GET;
		if(req.getMethod() == HTTPRequest.HTTPMethod.POST) {
			method = HTTPMethod.POST;
		}
		
		URL url = req.getUrl();

		StringBuffer body = new StringBuffer();
		try {
			for(String key : req.getParams().keySet()) {
				List<String> values = req.getParams().get(key);
		
				if(values != null && values.size() > 0) {
					for(String value : values) {
						body.append(URLEncoder.encode(key, "UTF-8"));
						body.append("=");
						body.append(URLEncoder.encode(value, "UTF-8"));
						body.append("&");
					}
				} else {
					body.append(URLEncoder.encode(key, "UTF-8"));
					body.append("&");
				}
			}
		} catch(Exception e) { }
		
		if(method == HTTPMethod.GET) {
			try {
				url = new URL(url.toString() + "?" + body);
			} catch(Exception e) { }
		}
			
		com.google.appengine.api.urlfetch.HTTPRequest appReq = new com.google.appengine.api.urlfetch.HTTPRequest(url,method);
		
		if(method == HTTPMethod.POST) {
			appReq.setPayload(body.substring(0,body.length() - 1).toString().getBytes());
		} 
		
		for(String key : req.getHeaders().keySet()) {
			appReq.addHeader(new HTTPHeader(key,req.getHeaders().get(key)));
		}
					
		URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
		HTTPResponse resp;
		try {
			resp = fetchService.fetch(appReq);
			result = new String(resp.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		if(trans != null)
			result = trans.transform(uri,result, params);
		return result;
	}

}
