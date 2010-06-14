package org.openlids.parser;


import java.util.logging.Logger;

import org.openlids.model.BGP;
import org.openlids.model.BNode;
import org.openlids.model.IRI;
import org.openlids.model.Literal;
import org.openlids.model.ServiceDescription;
import org.openlids.model.Value;
import org.openlids.model.Variable;



import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;


public class SPARQLJenaVisitor extends ElementVisitorBase {

	ServiceDescription desc;
	
	public SPARQLJenaVisitor(ServiceDescription desc) {
		this.desc = desc;
	}
	
	public SPARQLJenaVisitor() {
		desc = new ServiceDescription();
	}
	
	public ServiceDescription getDescription() {
		return desc;
	}

	public void setDescription(ServiceDescription desc) {
		this.desc = desc;
	}

	public void visit(ElementGroup eg) {
		for(Element elem : eg.getElements()) {
			elem.visit(this);
		}
	}
	
	@Override
	public void visit(ElementTriplesBlock arg0) {
		for(Triple triple : arg0.getPattern().getList()) {
			desc.addInputBGP(new BGP(node2Value(triple.getSubject()),
									 node2Value(triple.getPredicate()),
									 node2Value(triple.getObject())));
			
		}
	}
	
	public static Value node2Value(Node node) {
		if(node instanceof Node_Variable) {
			Variable v = new Variable();
			v.setName(((Node_Variable) node).getName());
			return v;
		} else if(node instanceof Node_Literal) {
			Literal l = new Literal();
			l.setName(((Node_Literal) node).getLiteral().getLexicalForm());
			return l;
		} else if(node instanceof Node_Blank) {
			BNode b = new BNode();
			b.setName(((Node_Blank) node).getBlankNodeLabel());
			return b;
		} else if(node instanceof Node_URI) {
			IRI iri = new IRI();
			iri.setName(((Node_URI) node).getURI());
			return iri;
		} else {
			Logger.getLogger("TTT").severe("Unknown node class: " + node.getClass());
			System.err.println("Unknown node class: " + node.getClass());
		}
		return null;
	}
}
