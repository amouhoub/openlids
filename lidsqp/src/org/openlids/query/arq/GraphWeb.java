package org.openlids.query.arq;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.util.CallbackSet;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.graph.TripleMatch;
import com.hp.hpl.jena.graph.impl.GraphBase;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.Main;
import com.ontologycentral.ldspider.frontier.BasicFrontier;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.content.ContentHandler;
import com.ontologycentral.ldspider.hooks.content.ContentHandlerRdfXml;
import com.ontologycentral.ldspider.hooks.fetch.FetchFilterAllow;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;

public class GraphWeb extends GraphBase {
	private final static Logger _log = Logger.getLogger(Main.class.getSimpleName());

	Crawler _c;
	Set<Node[]> _data;
	
	public GraphWeb() {
		_c = new Crawler(QueryEngineConstants.QE_THREADS);
		_data = new HashSet<Node[]>();
	}
	
	protected ExtendedIterator<Triple> graphBaseFind(TripleMatch tm) {
		Triple t = tm.asTriple();

		System.out.println(t);
		
		Node[] bgp = new Node[3];
		
		if (t.getSubject().isURI()) {
			bgp[0] = new Resource(t.getSubject().getURI());
		} else if (t.getSubject().isBlank()) {
			bgp[0] = new BNode(t.getSubject().getBlankNodeLabel());
		} else if (t.getSubject().isVariable()) {
			bgp[0] = new Variable(t.getSubject().getName());
		}
		
		if (t.getPredicate().isURI()) {
			bgp[1] = new Resource(t.getPredicate().getURI());
		} else if (t.getPredicate().isVariable()) {
			bgp[1] = new Variable(t.getPredicate().getName());
		}
		
		if (t.getObject().isURI()) {
			bgp[2] = new Resource(t.getObject().getURI());
		} else if (t.getObject().isVariable()) {
			bgp[2] = new Variable(t.getObject().getName());
		}

		_log.info("evaluating " + bgp[0] + " " + bgp[1] + " " + bgp[2]);

		Frontier frontier = new BasicFrontier();

		boolean bnodesubject = false;
		
		if (bgp[0] instanceof Resource) {
			URI u;
			try {
				u = new URI(bgp[0].toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			frontier.add(u);
		} else if (bgp[0] instanceof Literal) {
			_log.info("literal " + bgp[0] + " on subject position");
		} else if (bgp[0] instanceof BNode) {
			_log.info("bnode " + bgp[0] + " on subject position - @@@ how to handle that?");			
			bnodesubject = true;
		} else if (bgp[2] instanceof Resource) {
			URI u;
			try {
				u = new URI(bgp[2].toString());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			frontier.add(u);
		} else {
			throw new UnsupportedOperationException("only allows for bgp's with subject specified");
			//return new MatchIterator(new ArrayList<Node[]>().iterator());
		}
		
		List<Node[]> results = new ArrayList<Node[]>();
		
		if (bnodesubject == false) {
			CallbackSet cb = new CallbackSet();

			ContentHandler handler = new ContentHandlerRdfXml();
			_c.setContentHandler(handler);

			//Sink
			Sink sink = new SinkCallback(cb);
			_c.setOutputCallback(sink);

			_c.setFetchFilter(new FetchFilterAllow());
			//_c.setLinkFilter(new FetchFilterDeny());
			_c.evaluateBreadthFirst(frontier, QueryEngineConstants.QE_HOPS, -1, -1, false, false);

			for (Node[] nx : cb.getSet()) {
				if (match(bgp, nx)) {
					results.add(nx);
				}
			}

			_data.addAll(cb.getSet());
		} else {
			for (Node[] nx : _data) {
				if (match(bgp, nx)) {
					results.add(nx);
				}
			}
		}

		return new MatchIterator(results.iterator());
	}
	
	static boolean match(Node[] bgp, Node[] nx) {
		for (int i = 0; i < bgp.length; i++) {
			Node n = bgp[i];
			if (n != null) { // null == variable ; !(n instanceof Variable)) {
				if (!n.equals(nx[i])) {
					return false;
				}
			}
		}

		return true;
	}
}
