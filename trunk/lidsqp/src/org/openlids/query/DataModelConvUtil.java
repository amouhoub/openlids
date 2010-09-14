package org.openlids.query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.openlids.model.BGP;
import org.openlids.model.ServiceDescription;
import org.openlids.model.Value;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

public class DataModelConvUtil {

	public static QueryObj convert(ServiceDescription service) {
		QueryObj qo = new QueryObj();
		
		Set<org.openlids.model.Variable> reqVars = service.getRequiredVars();
		qo.headVars = new Variable[reqVars.size() + 1];
		qo.headVars[0] = (Variable) value2Node(service.getExposedVar());
		int i = 1;
		for(org.openlids.model.Variable v : reqVars) {
			qo.headVars[i++] = (Variable) value2Node(v);
		}
		qo.bgps = new LinkedList<Node[]>();
		for(BGP bgp : service.getInput()) {
			qo.bgps.add(new Node[] { value2Node(bgp.getSubject()), value2Node(bgp.getPredicate()), value2Node(bgp.getObject()) } );
		}
		
		return qo;
	}

	static Node value2Node(org.openlids.model.Value val) {
		if(val instanceof org.openlids.model.BNode ) {
			return new BNode(val.getName());
		}
		if(val instanceof org.openlids.model.IRI) {
			return new Resource(val.getName());
		}
		if(val instanceof org.openlids.model.Literal) {
			return new Literal(val.getName());
		}
		if(val instanceof org.openlids.model.Variable){
			return new Variable(val.getName());
		}
		
		return null;
	}

	public static Map<org.openlids.model.Variable, Value> convert(Variable[] headVars, Node[] r) {
		Map<org.openlids.model.Variable, Value> ret = new HashMap<org.openlids.model.Variable, Value>();
		for(int i=0;i<headVars.length;i++) {
			ret.put((org.openlids.model.Variable) node2Value(headVars[i]),node2Value(r[i]));
		}
		return ret;
	}

	private static org.openlids.model.Value node2Value(Node n) {
		if(n instanceof BNode) {
			return new org.openlids.model.BNode(n.toString());
		}
		if(n instanceof Resource) {
			return new org.openlids.model.IRI(n.toString());
		}
		if(n instanceof Literal) {
			return new org.openlids.model.Literal(n.toString());
		}
		if(n instanceof Variable) {
			return new org.openlids.model.Variable(n.toString());
		}
		return null;
	}

}
