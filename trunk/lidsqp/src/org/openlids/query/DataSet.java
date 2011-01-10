package org.openlids.query;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.ParseException;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

import com.ontologycentral.ldspider.Main;

public class DataSet {
	private final static Logger _log = Logger.getLogger(Main.class.getSimpleName());


	Set<URL> resolvedURLs = new HashSet<URL>();

	public Set<Node[]> _data;

	long uriRetrievalTimes = 0;

	public boolean auto_crawl = true;

	public DataSet() {
		_data = new TreeSet<Node[]>(NodeComparator.NC);
	}

	public void add(Node[] nx) {
		_data.add(nx);
	}

	protected void crawlURIs(Node[] tp) throws ParseException, IOException {
		List<URI> uris = new LinkedList<URI>();
		for(Node n : tp) {
			if(n instanceof Resource) {
				URI u;
				try {
					u = new URI(n.toString());
					uris.add(u);
				} catch(URISyntaxException e) {

				}
			}
		}
		crawlURIs(uris);
	}

	public Iterable<Node[]> match(Node[] tp) throws ParseException, IOException {
		if(auto_crawl) {
			crawlURIs(new Node[] { tp[0] });
			// crawlURIs(tp);
		}
		TreeSet<Node[]> results = new TreeSet<Node[]>(NodeComparator.NC);

            if (tp[1].toString().equals("http://www.w3.org/2000/10/swap/log#uri")) {
                Resource urir = new Resource("http://www.w3.org/2000/10/swap/log#uri");
                if (tp[0] == null && tp[2] == null) {
                    for(Node[] nx : _data) {
                        if(nx[0] instanceof Resource) {
                            results.add(new Node[]{nx[0], urir, new Literal(nx[0].toString()) });
                        }
                        if (nx[2] instanceof Resource) {
                            results.add(new Node[]{nx[2], urir, new Literal(nx[2].toString()) });
                        }
                    }
                } else if((tp[2] == null) && tp[0] instanceof Resource) {
                    results.add(new Node[]{tp[0], urir, new Literal(tp[0].toString())});
                } else if((tp[2] instanceof Literal) && (tp[0] instanceof Resource)) {
                    if(tp[2].toString().equals(tp[0].toString())) {
                        results.add(new Node[] {tp[0], urir, tp[2]});
                    }
                }
                return results;
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

	public void crawlURIs(List<URI> uris) throws ParseException, IOException {
		crawlURIs(uris,null);
	}

	public void crawlURIs(List<URI> uris, final TripleHandler handler) throws MalformedURLException, IOException, ParseException {

		long startTime = System.currentTimeMillis();
		for(URI uri : uris) {
			URL url = uri.toURL();

			if(resolvedURLs.contains(url))
				continue;

			resolvedURLs.add(url);



			InputStream is = null;

			if(url.getProtocol().equals("file")) {
				is = url.openConnection().getInputStream();
			} else {

				HttpURLConnection conn = (HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(3000);
				conn.setInstanceFollowRedirects(false);
				conn.setRequestProperty("Accept", "application/rdf+xml");
				is = conn.getInputStream();

				if(conn.getResponseCode() == 303) {
					String newLoc = conn.getHeaderField("Location");
					is.close();
					conn.disconnect();
					url = new URL(newLoc);
					if(resolvedURLs.contains(url))
						continue;
					resolvedURLs.add(url);
					conn = (HttpURLConnection) url.openConnection();
					conn.setConnectTimeout(3000);
					conn.setRequestProperty("Accept", "application/rdf+xml");
					is = conn.getInputStream();
				}


				if (conn.getResponseCode() != 200) {
					System.err.println("response code != 200");
					is.close();
					conn.disconnect();
					continue;
				}
			}

			Callback cb = new Callback() {
				public void endDocument() {
				}
				public void processStatement(Node[] nx) {
					if(handler != null) {
						add(handler.handle(nx));
					} else {
						add(nx);
					}
				}
				public void startDocument() {
				}

			};

			try {
                            
				System.out.println("U: " + url.toString());
				RDFXMLParser rdfxml = new RDFXMLParser(is, true, true, url.toString(), cb);
			} catch(Exception e) {
				System.err.println("Error during parsing: " + url.toString());
			}
		}
		long endTime = System.currentTimeMillis();
		this.uriRetrievalTimes += (endTime - startTime); 

	}

	//	Iterable<Node[]> crawlURIsCrawler(List<URI> uris) {
	//
	//		Frontier frontier = new BasicFrontier();
	//		for(URI uri : uris) {
	//			frontier.add(uri);
	//		}
	//		
	//		CallbackSet cb = new CallbackSet();
	//
	//		ContentHandler handler = new ContentHandlerRdfXml();
	//		_c.setContentHandler(handler);
	//
	//		//Sink
	//		Sink sink = new SinkCallback(cb);
	//		_c.setOutputCallback(sink);
	//
	//		_c.setFetchFilter(new FetchFilterAllow());
	//		//_c.setLinkFilter(new FetchFilterDeny());
	//			
	//		_c.evaluateBreadthFirst(frontier, QueryEngineConstants.QE_HOPS, -1, -1, Mode.ABOX_ONLY);
	//
	//		return cb.getSet();
	//	}

	//	void maintainSameAs() {
	//
	//		for(Node[] nx : _data) {
	//			if(nx[1].equals(sameAsRes)) {
	//				if(!reachable(nx[2],nx[0])) {
	//					Set<Node> sames = sameAs.get(nx[0]);
	//					if(sames == null) {
	//						sames = new HashSet<Node>();
	//						sameAs.put(nx[0], sames);
	//					}
	//					sames.add(nx[2]);
	//				}
	//			}
	//		}
	//	}

	//	boolean reachable(Node src, Node dst) {
	//		if(src.equals(dst)) {
	//			return true;
	//		}
	//		Set<Node> sames = sameAs.get(src);
	//		if(sames == null) {
	//			return false;
	//		}
	//		boolean reached = false;
	//		for(Node same : sames) {
	//			reached |= reachable(same,dst);
	//		}
	//		return reached;
	//	}

	static boolean match(Node[] tp, Node[] nx) {
            for (int i = 0; i < tp.length; i++) {
                Node n = tp[i];
                if (n != null) { // null == variable ; !(n instanceof Variable)) {
                    if (!n.equals(nx[i])) {
                        if (n instanceof Resource && nx[i] instanceof Resource) {
                            if (n.toString().startsWith("file:/") && nx[i].toString().startsWith("file:/")) {
                                if (n.toString().startsWith("file:///") && !nx[i].toString().startsWith("file:///")) {
                                    if (n.toString().substring(7).equals(nx[i].toString().substring(5))) {
                                        continue;
                                    }
                                }
                                if (!n.toString().startsWith("file:///") && nx[i].toString().startsWith("file:///")) {
                                    if (n.toString().substring(5).equals(nx[i].toString().substring(7))) {
                                        continue;
                                    }
                                }
                            }
                        }
                        return false;
                    }
                }
            }

            return true;
    }

}
