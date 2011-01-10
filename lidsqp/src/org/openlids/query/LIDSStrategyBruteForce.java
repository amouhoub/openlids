package org.openlids.query;

import java.io.IOException;
import java.net.MalformedURLException;
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
			public LIDSStrategy createLIDSStrategy(QueryObj q, QueryExecutor qe, Set<ServiceDescription> services) {
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
				incLidsMatches();
                                System.err.println("Found " + results.size() + " results for " + service.toString());
				int nrHeadVar = 0;
				for(nrHeadVar=0;nrHeadVar < lidsQ.headVars.length; nrHeadVar++) {
					if(lidsQ.headVars[nrHeadVar].toString().equals(service.getExposedVar().getName()))
						break;
				}

				for(final Node[] r : results) {
					final Resource newR = new Resource(service.makeURI(DataModelConvUtil.convert(lidsQ.headVars, r)));
                                        System.err.println("newSameAs: + " + r[nrHeadVar] + " = " + newR);

					Node[] newSameAs = new Node[] { r[nrHeadVar], newR };
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
									if(n3[0].toString().contains("dbpedia")) {
										n3[0] = n3[0];
									}
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
		}


		return newLIDS;
	}

}
