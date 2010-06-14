package org.openlids.wrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openlids.interfacing.ServiceAnnotator;
import org.openlids.interfacing.ServiceInterface;
import org.openlids.interfacing.ServiceWrapper;
import org.openlids.model.ServiceDescription;
import org.openlids.parser.ServiceParser;
import org.openlids.parser.ServiceParserJena;

import com.hp.hpl.jena.rdf.model.Model;


@SuppressWarnings("serial")
public class Open_lidsServlet extends HttpServlet {
	
	Set<ServiceAnnotator> annotators;
	Map<String,ServiceWrapper> handlers;
	
	String classNames[] = {
		"edu.kit.ksri.lids.openlids.TESSCompanyInterface"	
	};

	public String readResource(String path) {
		BufferedReader r = null;
		String buf;
		try {
			InputStream in = this.getServletContext().getResourceAsStream(path);
			r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			StringBuffer pC = new StringBuffer();
			while((buf = r.readLine()) != null) {
				pC.append(buf).append("\n");
			}
			buf = pC.toString();
		} catch (IOException ex) {
			Logger.getLogger(Open_lidsServlet.class.getName()).log(Level.SEVERE, null, ex);
			return null;
		} finally {
			try {
				r.close();
			} catch (IOException ex) {
				Logger.getLogger(Open_lidsServlet.class.getName()).log(Level.SEVERE, null, ex);
				return null;
			} 
		}
		return buf;
    }
	
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Set<String> serviceDescsFiles = (Set<String>) config.getServletContext().getResourcePaths("/data/hosting/");
        Set<String> annsDescsFiles = (Set<String>) config.getServletContext().getResourcePaths("/data/annotations/");

        ServiceParser sp = new ServiceParserJena();
		
        Map<String,ServiceDescription> serviceDescs = new HashMap<String,ServiceDescription>();
        for(String fn : serviceDescsFiles) {
        	String d = readResource(fn);
        	if(d != null) {
        		try {
        			ServiceDescription desc = sp.parseServiceDescription(d);
        			serviceDescs.put(desc.getEndpoint().getName(), desc);
        		} catch(Exception e) {
        			Logger.getLogger("Open_lidsServlet").warning("Could not read: " + fn + "\n" + e.toString());
        		}
        	}
        }
        
        Map<String,Class> interfaces = new HashMap<String,Class>();
        for(String c : classNames) {
        	try {
        		Class cs = this.getClass().getClassLoader().loadClass(c);
        		Method m = cs.getMethod("getEndpoint", new Class[0]);
        		if(m != null) {
        			String defp = (String) m.invoke(cs);
        			if(defp != null) {
        				interfaces.put(defp, cs);
        			}
        		}
        	} catch(Exception e) {
        		Logger.getLogger("Open_lidsServlet").warning("Could not load class: " + c + "\n" + e.toString());
        	}
        }
        
        handlers = new HashMap<String,ServiceWrapper>();
        for(String ep : serviceDescs.keySet()) {
        	try {
        		Class c = interfaces.get(ep);
        		if(c != null) {
        			Constructor cc = c.getConstructor(new Class[0]);
        			ServiceInterface inf = (ServiceInterface) cc.newInstance();
        			if(inf != null) {
        				ServiceWrapper w = new ServiceWrapper(serviceDescs.get(ep),inf);
        				handlers.put(ep, w);
        			}
        		}
        	} catch(Exception e) {
        		Logger.getLogger("Open_lidsServlet").warning("Could not add handler for " + ep + "\n" + e.toString());
        	}
        	
        }
        
        
		// Same for annotations
        // But directly put them in annotations
        annotators = new HashSet<ServiceAnnotator>();
        for(String fn : annsDescsFiles) {
        	String d = readResource(fn);
        	if(d != null) {
        		try {
        			ServiceDescription desc = sp.parseServiceDescription(d);
        			annotators.add(new ServiceAnnotator(desc));
        		} catch(Exception e) {
        			Logger.getLogger("Open_lidsServlet").warning("Could not read: " + fn + "\n" + e.toString());
        		}
        	}
        }
    }
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		processRequest(req, resp);
	}
	
	protected void processRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		String uri = req.getRequestURL().toString();
		
		String endpoint = "";
		if(req.getQueryString() != null && req.getQueryString().length() > 0 && !req.getQueryString().equals("null")) {
			uri += "?" + req.getQueryString();
			endpoint = req.getRequestURL().toString();
		} else {
			// have to remove last parameter
			endpoint = uri.substring(0,uri.lastIndexOf("/"));
		}
		
		ServiceWrapper wrapper = handlers.get(endpoint);
		
		if(wrapper == null) {
			resp.sendError(404);
			return;
		}
		
		String result = wrapper.invoke(URI.create(uri));
				
		if(annotators.size() > 0) {
			ServiceAnnotator nullator = new ServiceAnnotator(null);
			Model m = nullator.annotate(result,uri,"RDF/XML");
			for(ServiceAnnotator a : annotators) {
				m = a.annotate(m);
			}
			m.write(resp.getOutputStream(),"RDF/XML");
		} else {
			resp.setContentType("application/rdf+xml");
			resp.getWriter().println(result);
		}
		
	}
}
