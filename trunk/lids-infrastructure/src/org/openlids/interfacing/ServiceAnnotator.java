package org.openlids.interfacing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;

import org.openlids.model.BGP;
import org.openlids.model.ServiceDescription;
import org.openlids.model.Variable;
import org.openlids.parser.ServiceParser;
import org.openlids.parser.ServiceParserJena;


import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;


public class ServiceAnnotator {
	
	ServiceDescription desc;
	
	public ServiceAnnotator(ServiceDescription desc) {
		this.desc = desc;
	}
	
	public Model annotate(Model m) {
		if(desc == null)
			return m;
		String queryStr = "SELECT ";
		queryStr += "?" + desc.getExposedVar().getName() + " ";
		for(Variable v : desc.getRequiredVars()) {
			queryStr += "?" + v.getName() + " ";
		}
		queryStr += " WHERE {";
		for(BGP bgp : desc.getInput()) {
			queryStr += " " + bgp.toString() + " .";
		}
		
		queryStr = queryStr.substring(0, queryStr.length() - 1) + " } ";
		
				
		Query q = QueryFactory.create(queryStr);
		QueryExecution exec = QueryExecutionFactory.create(q,m);
		List<Statement> l = new LinkedList<Statement>();
		try {
			ResultSet rs = exec.execSelect();
			while(rs.hasNext()) {
				QuerySolution soln = rs.nextSolution();
				String uri = desc.getEndpoint().getName();
				if(desc.getRequiredVars().size() == 1) {
					uri += "/" + URLEncoder.encode(soln.get(desc.getRequiredVars().iterator().next().getName()).toString(),"UTF-8");
				} else {
					uri += "?";
					for(Variable v : desc.getRequiredVars()) {
						uri += URLEncoder.encode(v.getName(),"UTF-8") + "=" + URLEncoder.encode(soln.get(v.getName()).toString(),"UTF-8") + "&";
					}
					uri = uri.substring(0,uri.length()-1);
				}
				uri += "#" + desc.getExposedVar().getName();
				Resource same = m.createResource(uri);
				Statement stmt = m.createStatement(
						soln.get(desc.getExposedVar().getName()).asResource(),
						m.createProperty("http://www.w3.org/2002/07/owl#sameAs"),
						same);
				
				l.add(stmt);
				
			}
		} catch(UnsupportedEncodingException ue) {
			System.err.println("" + ue.toString());
		} finally { exec.close(); }
		m.add(l);

		return m;
	}
	
	public Model annotate(InputStream input, String base, String lang) {
		Model m = ModelFactory.createDefaultModel();
		m.read(input, base, lang);
		return annotate(m);
		
	}
	
	public Model annotate(String input, String base, String lang) {
		return this.annotate(new ByteArrayInputStream(input.getBytes()), base, lang);
		
	}

	public static void main(String args[]) {
		
		if(args.length < 2) {
			System.out.println("Usage: inputfile servicedesc1 servicedesc2 ...");
			System.exit(-1);
		}
		try {
			FileInputStream fin = new FileInputStream(args[0]);
						
			String sparqls[] = new String[args.length - 1];
			for(int i=0;i<sparqls.length;i++) {
				FileReader reader = new FileReader(args[i+1]);
				char buf[] = new char[(int) new File(args[i+1]).length()];
				int off = 0;
				int l = 0;
				while((l = reader.read(buf, off, buf.length - off)) > -1 && off < buf.length) {
					off += l;
				}
				sparqls[i] = new String(buf);
			}
			
			ServiceParser sp = new ServiceParserJena();
									
			ServiceAnnotator sas[] = new ServiceAnnotator[sparqls.length];
			for(int i=0;i<sas.length;i++) {
				sas[i] = new ServiceAnnotator(sp.parseServiceDescription(sparqls[i]));
			}
			
			Model m = ModelFactory.createDefaultModel();
			m.read(fin, args[0], "N3");
			
			for(ServiceAnnotator sa : sas) {
				m = sa.annotate(m);
			}
			
			m.write(System.out,"N-TRIPLE");
			
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not read file: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
			
}
