package org.openlids.query;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.openlids.model.BGP;
import org.openlids.model.ServiceDescription;
import org.openlids.model.Value;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.ParseException;

public class LIDSStrategyRelevant extends LIDSStrategy {
	
	List<ServiceDescription> reqServices;
		
	public static LIDSStrategyFactory getFactory() {
		return new LIDSStrategyFactory() {

			@Override
			public LIDSStrategy createLIDSStrategy(QueryObj q,
					QueryExecutor qe, Set<ServiceDescription> services) {
				return new LIDSStrategyRelevant(q, qe, services);
			}
			
		};
	}

	public LIDSStrategyRelevant(QueryObj q, QueryExecutor qe,	Set<ServiceDescription> services) {
		super(q, qe, services);
	}

	@Override
	void analyzeQuery() {
		reqServices = new LinkedList<ServiceDescription>();
		Set<ServiceDescription> avServices = new HashSet<ServiceDescription>();
		avServices.addAll(services);
		Set<Node[]> reqPatterns = new TreeSet<Node[]>(NodeComparator.NC);
		reqPatterns.addAll(q.bgps);
		Set<Node[]> chkdPatterns = new TreeSet<Node[]>(NodeComparator.NC);
		
		while(!reqPatterns.isEmpty() && !avServices.isEmpty()) {
			Set<Node[]> curPatterns = new TreeSet<Node[]>(NodeComparator.NC);
			curPatterns.addAll(reqPatterns);
			chkdPatterns.addAll(reqPatterns);
			reqPatterns = new TreeSet<Node[]>(NodeComparator.NC);
			for(Node[] t : curPatterns) {
				Set<ServiceDescription> curAvailServices = new HashSet<ServiceDescription>();
				curAvailServices.addAll(avServices);
				for(ServiceDescription l : curAvailServices) {
					boolean found = false;
					for(org.openlids.model.BGP bgp : l.getOutputBGP()) {
						if(match(t,bgp)) {
							found = true;
							break;
						}
					}
					if(found) {
						avServices.remove(l);
						reqServices.add(l);
						for(BGP bgp : l.getInput()) {
							boolean existsMap = false;
							for(Node[] cP : chkdPatterns) {
								if(match(cP,bgp)) {
									existsMap = true;
									break;
								}
							}
							if(!existsMap) {
								reqPatterns.add(new Node[] { DataModelConvUtil.value2Node(bgp.getSubject()),
															 DataModelConvUtil.value2Node(bgp.getPredicate()),
															 DataModelConvUtil.value2Node(bgp.getObject())});
							}
						}
						
					}
				}
			}
		}
		Collections.reverse(reqServices);
	}

	private boolean match(Node[] t, BGP bgp) {
		Value[] v = new Value[] { bgp.getSubject(), bgp.getPredicate(), bgp.getObject() };
		int i = 0;
		Map<Value,Node> m = new HashMap<Value,Node>();
		for(i=0;i<3;i++) {
			if(m.containsKey(v[i])) {
				Node n = m.get(v[i]);
				if(!n.equals(t[i])) {
					return false;
				}
			} else {
				if(v[i] instanceof org.openlids.model.IRI || v[i] instanceof org.openlids.model.Literal || v[i] instanceof org.openlids.model.BNode) {
					if(!v[i].getName().equals(t[i].toString()))
						return false;
				} else if(v[i] instanceof org.openlids.model.Variable) {
					m.put(v[i], t[i]);
				} else {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Set<Node[]> applyLIDS(DataSet dataSet) {
		// Repeat LIDS application until no new things are added.
		// ...
		Set<Node[]> newLIDS = new TreeSet<Node[]>(NodeComparator.NC);

		boolean newLIDSlinks = true;

		//while(newLIDSlinks) {
			newLIDSlinks = false;

			for(ServiceDescription service : reqServices) {

				QueryObj lidsQ = DataModelConvUtil.convert(service);

				Collection<Node[]> results = qe.execQuery(lidsQ.headVars, lidsQ.bgps);
				incLidsMatches();

				for(final Node[] r : results) {
					final Resource newR = new Resource(service.makeURI(DataModelConvUtil.convert(lidsQ.headVars, r)));

					Node[] newSameAs = new Node[] { r[0], newR };
					if(!newLIDS.contains(newSameAs)) {
						newLIDSlinks = true;
						newLIDS.add(newSameAs);
						List<URI> uris = new LinkedList<URI>();
						try {
							uris.add(new URI(newR.toString()));
						} catch (URISyntaxException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							dataSet.crawlURIs(uris, new TripleHandler() {
								public Node[] handle(Node[] nx) {
									Node[] n3 = new Node[] { nx[0], nx[1], nx[2] };
									if(n3[0].equals(newR)) {
										n3[0] = r[0];
									}
									return n3;
								}
							});
						} catch(Exception e) {

						}
					}
				}
			}
		//}

		return newLIDS;
	}

}
