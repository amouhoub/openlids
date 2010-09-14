package org.openlids.query;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openlids.model.ServiceDescription;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.ParseException;

public class LIDSStrategyBruteForce extends LIDSStrategy {
	
	public static LIDSStrategyFactory getFactory() {
		return new LIDSStrategyFactory() {

			@Override
			public LIDSStrategy createLIDSStrategy(QueryObj q,
					QueryExecutor qe, Set<ServiceDescription> services) {
				return new LIDSStrategyBruteForce(q, qe, services);
			}
			
		};
	}

	public LIDSStrategyBruteForce(QueryObj q, QueryExecutor qe,	Set<ServiceDescription> services) {
		super(q, qe, services);
	}

	@Override
	void analyzeQuery() {

	}

	@Override
	public Set<Node[]> applyLIDS(DataSet dataSet) {
		// Repeat LIDS application until no new things are added.
		// ...
		Set<Node[]> newLIDS = new TreeSet<Node[]>(NodeComparator.NC);

		boolean newLIDSlinks = true;

		while(newLIDSlinks) {
			newLIDSlinks = false;

			for(ServiceDescription service : services) {

				QueryObj lidsQ = DataModelConvUtil.convert(service);

				Collection<Node[]> results = qe.execQuery(lidsQ.headVars, lidsQ.bgps);

				int nrHeadVar = 0;
				for(nrHeadVar=0;nrHeadVar < lidsQ.headVars.length; nrHeadVar++) {
					if(lidsQ.headVars[nrHeadVar].toString().equals(service.getExposedVar().getName()))
						break;
				}

				for(Node[] r : results) {
					Resource newR = new Resource(service.makeURI(DataModelConvUtil.convert(lidsQ.headVars, r)));

					Node[] newSameAs = new Node[] { r[nrHeadVar], DataSet.sameAsRes, newR };
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
						Iterable<Node[]> triples = null;
						try {
							triples = dataSet.crawlURIs(uris);
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if(triples != null) {
							for(Node[] nx : triples) { 
								Node[] n3 = new Node[] { nx[0], nx[1], nx[2] };
								if(n3[0].equals(newR)) {
									n3[0] = r[0];
								}
								if(!n3[1].toString().contains("http://www.w3.org/2006/http#") && !n3[1].toString().contains("http://code.google.com/p/ldspider/ns#headerInfo")) {
									dataSet._data.add(n3);
								}
							}
						}
					}
				}
			}
		}

		return newLIDS;
	}

}
