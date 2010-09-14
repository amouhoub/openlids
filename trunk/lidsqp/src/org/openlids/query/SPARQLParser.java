package org.openlids.query;


import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.syntax.TemplateVisitor;

import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

public class SPARQLParser {

	QueryObj parseQuery(String queryStr) {
		Query q = QueryFactory.create(queryStr);
		QueryObj ret = new QueryObj();
		
		final List<Node[]> bgps = new LinkedList<Node[]>();
				
		q.getQueryPattern().visit(new ElementVisitorBase() {
			public void visit(ElementGroup eg) {
				for(Element elem : eg.getElements()) {
					elem.visit(this);
				}
			}
			@Override
			public void visit(ElementTriplesBlock arg0) {
				for(Triple triple : arg0.getPattern().getList()) {
					bgps.add(new Node[] { node2Node(triple.getSubject()), node2Node(triple.getPredicate()), node2Node(triple.getObject()) } );
				}
			}
		});
		
		final List<Variable> vars = new LinkedList<Variable>();
		for(Var v : q.getProject().getVars()) {
			vars.add(new Variable(v.getName()));
		}
		
		ret.bgps = bgps;
		ret.headVars = vars.toArray(new Variable[0]);
		
		return ret;
	}
	
	public static Node node2Node(com.hp.hpl.jena.graph.Node node) {
		if(node instanceof Node_Variable) {
			Variable v = new Variable(((Node_Variable) node).getName());
			return v;
		} else if(node instanceof Node_Literal) {
			Literal l = new Literal(((Node_Literal) node).getLiteral().getLexicalForm());
			return l;
		} else if(node instanceof Node_Blank) {
			BNode b = new BNode(((Node_Blank) node).getBlankNodeLabel());
			return b;
		} else if(node instanceof Node_URI) {
			Resource r = new Resource(((Node_URI) node).getURI());
			return r;
		} else {
			Logger.getLogger("TTT").severe("Unknown node class: " + node.getClass());
			System.err.println("Unknown node class: " + node.getClass());
		}
		return null;
	}
	
}
