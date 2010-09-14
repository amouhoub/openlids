package org.openlids.query;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.openlids.query.arq.QueryEngineConstants;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars.util.CallbackSet;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.Crawler.Mode;
import com.ontologycentral.ldspider.Main;
import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.hooks.content.ContentHandlerRdfXml;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterAllow;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;

public class DataSet {
	private final static Logger _log = Logger.getLogger(Main.class.getSimpleName());
	
	final static Resource sameAsRes = new Resource("http://www.w3.org/2002/07/owl#sameAs");

	Set<URL> resolvedURLs = new HashSet<URL>();
	
	Crawler _c;
	Set<Node[]> _data;
	
	Map<Node,Set<Node>> sameAs = new HashMap<Node,Set<Node>>();

	
	
	public DataSet() {
		_c = null; // new Crawler(QueryEngineConstants.QE_THREADS);
		
		_data = new TreeSet<Node[]>(NodeComparator.NC);
	}
	
	protected Iterable<Node[]> match(Node[] tp) {
		// TriplePattern tp
		
		_log.info("evaluating " + tp[0] + " " + tp[1] + " " + tp[2]);
		
		

		List<URI> uris = new LinkedList<URI>();
		
		if (tp[0] instanceof Resource) {
			URI u;
			try {
				u = new URI(tp[0].toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			uris.add(u);
		}
		if (tp[2] instanceof Resource) {
			URI u;
			try {
				u = new URI(tp[2].toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			uris.add(u);
		}
		
		TreeSet<Node[]> results = new TreeSet<Node[]>(NodeComparator.NC);
		
		
		Iterable<Node[]> newTriples = null;
		try {
			newTriples = crawlURIs(uris);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(newTriples != null) {
			for(Node[] nx : newTriples) {
				Node[] n3 = new Node[] { nx[0], nx[1], nx[2] };
				if(!n3[1].toString().contains("http://www.w3.org/2006/http#") && !n3[1].toString().contains("http://code.google.com/p/ldspider/ns#headerInfo")) {
					_data.add(n3);
				}
			}
		}
		
	//	maintainSameAs();
		
		for(Node[] nx : _data) {
		  if(match(tp,nx)) {
			  results.add(nx);
		  }
		}
		
//		Set<Node> samesSubj = new HashSet<Node>();
//		if(sameAs.containsKey(tp[0])) {
//			samesSubj.addAll(sameAs.get(tp[0]));
//		}
//		samesSubj.add(tp[0]);
//		Set<Node> samesObj = new HashSet<Node>();
//		if(sameAs.containsKey(tp[2])) {
//			samesObj.addAll(sameAs.get(tp[2]));
//		}
//		samesObj.add(tp[2]);
//		for(Node subj : samesSubj) {
//			for(Node obj : samesObj) {
//				Node[] pattern = new Node[] { subj, tp[1], obj };
//				if((subj == tp[0]) && (obj == tp[2])) {
//					for(Node[] nx : _data) {
//						if(match(pattern,nx)) {
//							results.add(nx);
//						}
//					}
//				} else {
//					for(Node[] nx : match(pattern)) {
//						results.add(nx);
//					}
//				}
//			}
//		}
		return results;
	}
	
	Iterable<Node[]> crawlURIs(List<URI> uris) throws ParseException, IOException {
		CallbackSet cb = new CallbackSet();

		// URI uri = new URI("http://sws.geonames.org/2892794/");

		for(URI uri : uris) {
			URL url = uri.toURL();

			if(resolvedURLs.contains(url))
				continue;
			
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();
			conn.setConnectTimeout(3000);
			conn.setRequestProperty("Accept", "application/rdf+xml");
			InputStream is = conn.getInputStream();

			if (conn.getResponseCode() != 200) {
				System.err.println("response code != 200");
				return null;
			}

			String encoding = conn.getContentEncoding();
			if (encoding == null) {
				encoding = "ISO_8859-1";
			}


			RDFXMLParser rdfxml = new RDFXMLParser(is, true, true, url.toString(), cb);
		}
		return cb.getSet();
	}
	
	Iterable<Node[]> crawlURIsCrawler(List<URI> uris) {

		Frontier frontier = new BasicFrontier();
		for(URI uri : uris) {
			frontier.add(uri);
		}
		
		CallbackSet cb = new CallbackSet();

		ContentHandler handler = new ContentHandlerRdfXml();
		_c.setContentHandler(handler);

		//Sink
		Sink sink = new SinkCallback(cb);
		_c.setOutputCallback(sink);

		_c.setFetchFilter(new FetchFilterAllow());
		//_c.setLinkFilter(new FetchFilterDeny());
			
		_c.evaluateBreadthFirst(frontier, QueryEngineConstants.QE_HOPS, -1, -1, Mode.ABOX_ONLY);

		return cb.getSet();
	}

	void maintainSameAs() {

		for(Node[] nx : _data) {
			if(nx[1].equals(sameAsRes)) {
				if(!reachable(nx[2],nx[0])) {
					Set<Node> sames = sameAs.get(nx[0]);
					if(sames == null) {
						sames = new HashSet<Node>();
						sameAs.put(nx[0], sames);
					}
					sames.add(nx[2]);
				}
			}
		}
	}
	
	boolean reachable(Node src, Node dst) {
		if(src.equals(dst)) {
			return true;
		}
		Set<Node> sames = sameAs.get(src);
		if(sames == null) {
			return false;
		}
		boolean reached = false;
		for(Node same : sames) {
			reached |= reachable(same,dst);
		}
		return reached;
	}
	
	static boolean match(Node[] tp, Node[] nx) {
		for (int i = 0; i < tp.length; i++) {
			Node n = tp[i];
			if (n != null) { // null == variable ; !(n instanceof Variable)) {
				if (!n.equals(nx[i])) {
					return false;
				}
			}
		}

		return true;
	}
}
