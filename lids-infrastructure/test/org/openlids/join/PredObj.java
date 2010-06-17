package org.openlids.join;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

public class PredObj {
	Resource _p;
	Node _o;
	
	public PredObj(Resource p, Node o) {
		_p = p;
		_o = o;
	}
}
