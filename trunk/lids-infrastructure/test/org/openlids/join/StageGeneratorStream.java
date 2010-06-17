package org.openlids.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.NxParser;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import com.hp.hpl.jena.sparql.engine.main.StageGenerator;

public class StageGeneratorStream implements StageGenerator {
	NxParser _it;
	
	public StageGeneratorStream(NxParser it) {
		_it = it;
	}
		
	public QueryIterator execute(BasicPattern bgp, QueryIterator qi, ExecutionContext ec) {
		List<Resource> preds = new ArrayList<Resource>();
		
		for (Triple t : bgp) {
			preds.add(new Resource(t.getPredicate().toString()));
		}
		
		Map<Node, Set<PredObj>> interm = new HashMap<Node, Set<PredObj>>();
		
		while (_it.hasNext()) {
			Node[] nx = _it.next();
			if (preds.contains(nx[1])) {
				PredObj po = new PredObj((Resource)nx[1], nx[2]);
				
				Set<PredObj> s = interm.get(nx[0]);
				if (s == null) {
					s = new HashSet<PredObj>();
				}
				s.add(po);
				interm.put(nx[0], s);
			}
		}

		List<Binding> results = new ArrayList<Binding>();

		for (Node s : interm.keySet()) {
			Set<PredObj> pos = interm.get(s);
			
			for (PredObj po : pos) {
				if (preds.contains(po._p)) {
					Binding b = new BindingMap();
					
					Var v = Var.alloc(bgp.get(0).asTriple().getObject().getName());

					if (po._o instanceof Resource) {
						b.add(v, com.hp.hpl.jena.graph.Node.createURI(po._o.toString()));					
					} else if (po._o instanceof Literal) {
						b.add(v, com.hp.hpl.jena.graph.Node.createLiteral(po._o.toString()));
					}
					
					v = Var.alloc(bgp.get(0).asTriple().getSubject().getName());

					if (s instanceof Resource) {
						b.add(v, com.hp.hpl.jena.graph.Node.createURI(s.toString()));					
					}
					
					v = Var.alloc("forum");
					b.add(v, com.hp.hpl.jena.graph.Node.createURI("http://example.org/"));
					
					results.add(b);
				}
			}
		}
		
		return new QueryIterPlainWrapper(results.iterator(), ec) ;
	}
}

