package org.openlids.query;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openlids.model.ServiceDescription;
import org.openlids.parser.ServiceParser;
import org.openlids.parser.ServiceParserJena;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.ParseException;



public class QueryExecutor {
	
	DataSet dataSet = new DataSet();

	
	Set<ServiceDescription> services = new HashSet<ServiceDescription>();

	private LIDSStrategyFactory lidsStrategyF;

	public void addLIDS(String lidsDesc) {
		ServiceParser sp = new ServiceParserJena();
		ServiceDescription sd = sp.parseServiceDescription(lidsDesc);
		if(sd != null) {
			services.add(sd);
		}
	}
	
	public QueryExecutor() {
		this(null);
	}
	
	public QueryExecutor(LIDSStrategyFactory lidsStrategyF) {
		this.lidsStrategyF = lidsStrategyF;
	}
	
	public Collection<Node[]> execQueryWithLIDS(String sparqlStr) {
		SPARQLParser sp = new SPARQLParser();
		QueryObj q = sp.parseQuery(sparqlStr);
		return execQueryWithLIDS(q.headVars, q.bgps);
	}
	
	public Collection<Node[]> execQueryWithLIDS(Variable[] headVars, List<Node[]> bgps) {
		Set<Node[]> ret = new TreeSet<Node[]>(NodeComparator.NC);
		
		Collection<Node[]> newResults = new LinkedList<Node[]>();
		boolean newLIDSlinks = false;
		Set<Node[]> LIDSlinks = new TreeSet<Node[]>(NodeComparator.NC);
		
		QueryObj qObj = new QueryObj();
		qObj.headVars = headVars;
		qObj.bgps = bgps;
		
		LIDSStrategy lidsStrategy = null;
		if(lidsStrategyF != null) 
			lidsStrategy = lidsStrategyF.createLIDSStrategy(qObj, this, services);
		
		
		if(lidsStrategy != null) {
			do {
				ret.addAll(newResults);
				newResults = execQuery(headVars, bgps);
				// 	apply LIDS
				newLIDSlinks = false;
			
				Set<Node[]> newLIDS = lidsStrategy.applyLIDS(dataSet);
				newLIDSlinks = !LIDSlinks.containsAll(newLIDS);
				LIDSlinks.addAll(newLIDS);
			
			} while(!ret.containsAll(newResults) || newLIDSlinks);
			
		} else {
			ret.addAll(execQuery(headVars,bgps));
		}
		
		return ret;
	}
	
	public Collection<Node[]> execQuery(Variable[] headVars, List<Node[]> bgps) {
		return execQuery(headVars,bgps,new HashMap<Variable,Node>());
	}
	public Collection<Node[]> execQuery(Variable[] headVars, List<Node[]> bgps, Map<Variable,Node> qbindings) {
		List<Node[]> ret = new LinkedList<Node[]>();
		
		if(bgps.size() == 0) {
			// return result
			Node[] result = new Node[headVars.length];
			int i = 0;
			for(Variable headV : headVars) {
				result[i] = qbindings.get(headV);
				if(result[i] == null) {
					break;
				}
				i++;
			}
			if(i == headVars.length) {
				ret.add(result);
			}
		} else {
    		// Take one bgp, evaluate, for each binding do the rest
			List<Node[]> restBGPs = new LinkedList<Node[]>();
			Iterator<Node[]> it = bgps.iterator();
			Node[] thisBGP = it.next().clone();
			while(it.hasNext()) {
				restBGPs.add(it.next());
			}
			
			for(int i=0;i<thisBGP.length;i++) {
				if(qbindings.containsKey(thisBGP[i])) {
					thisBGP[i] = qbindings.get(thisBGP[i]);
				}
			}
			
			Node[] bgp = new Node[thisBGP.length];
			boolean isVar[] = new boolean[thisBGP.length];
			for(int i=0;i<thisBGP.length;i++) {
				if(thisBGP[i] instanceof Variable) {
					isVar[i] = true;
					bgp[i] = null;
				} else {
					bgp[i] = thisBGP[i];
					isVar[i] = false;
				}
			}
			
			Iterable<Node[]> results = dataSet.match(bgp);
			
			for(Node[] res : results) {
				Map<Variable,Node> newBindings = new HashMap<Variable,Node>();
				newBindings.putAll(qbindings);
				for(int i=0;i<bgp.length;i++) {
					if(isVar[i]) {
						newBindings.put((Variable) thisBGP[i], res[i]);
					}
				}
				Collection<Node[]> intermed = execQuery(headVars,restBGPs,newBindings);
				ret.addAll(intermed);
			}
		}
		return ret;
	}

}
