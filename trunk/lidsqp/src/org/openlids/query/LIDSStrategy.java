package org.openlids.query;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openlids.model.ServiceDescription;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.ParseException;


public abstract class LIDSStrategy {

	QueryObj q;
	QueryExecutor qe;
	Set<ServiceDescription> services;
	
	int lidsMatches = 0;
	
	public int getLidsMatches() {
		return lidsMatches;
	}
	
	void incLidsMatches() {
		lidsMatches++;
	}

	public LIDSStrategy(QueryObj q, QueryExecutor qe, Set<ServiceDescription> services) {
		this.services = services;
		this.q = q;
		this.qe = qe;
		analyzeQuery();
	}
	
	abstract void analyzeQuery();
	
	public abstract Set<Node[]> applyLIDS(DataSet dataSet);
	
}

