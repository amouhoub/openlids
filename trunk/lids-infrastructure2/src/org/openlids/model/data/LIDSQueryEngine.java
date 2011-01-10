/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openlids.model.LIDSDescription;
import org.openlids.model.data.impl.DataSetNxRetrieve;
import org.openlids.model.data.jena.QueryEngineJena;
import org.openlids.model.parser.LIDSParser;
import org.openlids.model.parser.arq.LIDSParserARQ;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class LIDSQueryEngine {

    public static String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
            + "PREFIX sioc: <http://rdfs.org/sioc/ns#> \n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
            + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
            + "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> \n"
            + "PREFIX rss: <http://purl.org/rss/1.0/>\n"
            + "\n";


    LIDSParser lidsParser = new LIDSParserARQ();
    List<LIDSDescription> lidsDescriptions = new ArrayList<LIDSDescription>();

    public static void main(String args[]) {
        LIDSQueryEngine lidsqe = new LIDSQueryEngine();
        Set<Node[]> results = lidsqe.execQuery(prefixes + "SELECT ?name WHERE { <http://speiserweb.de/sebastian/foaf.rdf#me> foaf:knows ?p . ?p foaf:name ?name }");
        for(Node[] r : results) {
            System.out.println(Nodes.toN3(r));
        }
    }

    public void addLIDS(String lidsDescriptionStr) {
        addLIDS(lidsParser.parseLIDSDescription(lidsDescriptionStr));
    }

    public void addLIDS(LIDSDescription lidsDescription) {
        lidsDescriptions.add(lidsDescription);
    }

    public Set<Node[]> execQuery(String queryStr) {
        final DataSet dataSet = new DataSetNxRetrieve();
        QueryEngine qe = new QueryEngineJena(dataSet);

        Query q = qe.createQuery(queryStr);

        Map<LIDSDescription,Query> lidsQueries = new HashMap<LIDSDescription,Query>();
        for(LIDSDescription lids : lidsDescriptions) {
            List<Node> head = new LinkedList<Node>();
            head.addAll(lids.getRequiredVars());
            head.add(lids.getInputEntity());
            Query lidsQuery = qe.createQuery(head, lids.getInputBGP());
            lidsQueries.put(lids, lidsQuery);
        }

        Variable[] head_vars = new Variable[q.getHeadVars().size()];
        int i = 0;
        for(Node n : q.getHeadVars()) {
            head_vars[i] = (Variable) n;
            i++;
        }
        Set<Node[]> results = new TreeSet(NodeComparator.NC);
        boolean new_results = true;
        Set<Node> checkedServiceInstances = new HashSet<Node>();

        while(new_results) {
            new_results = false;

            Iterator<Map<Variable, Node>> cur_results = qe.execQuery(q);
            while (cur_results.hasNext()) {
                Map<Variable, Node> r = cur_results.next();
                Node[] tuple = new Node[head_vars.length];
                i = 0;
                for(Variable hv : head_vars) {
                    tuple[i] = r.get(hv);
                    i++;
                }
                if(results.add(tuple)) {
                    new_results = true;
                }
            }

            boolean threaded = true;
            List<Thread> threads = new ArrayList<Thread>();

            for(LIDSDescription lids : lidsDescriptions) {
                Query lidsQuery = lidsQueries.get(lids);

                Iterator<Map<Variable, Node>> lidsResults = qe.execQuery(lidsQuery);
                while (lidsResults.hasNext()) {

                    
                    Map<Variable,Node> r = lidsResults.next();
                    // @@@ Add sameAs Statements
                    //                     new Resource(lids.makeURI(r)) owl:sameAs match for input entity
                    // But as we don't have sameAs, we resolve it and replace the service uri by the input entity
                    final Node newR = new Resource(lids.makeURI(r));

                    if (checkedServiceInstances.add(newR)) {
                        new_results = true;
                    } else {
                        continue;
                    }

                    final Node inputR = r.get(lids.getInputEntity());

                    if(threaded) {
                        Thread t = new Thread() {
                            public void run() {
                                TripleTransformer sameAsReplacer = new TripleTransformer() {
                                    @Override
                                    public Node[] transformTriple(DataSet dataSet, Node[] nx) {
                                        for (int j = 0; j < nx.length; j++) {
                                            if (nx[j].equals(newR)) {
                                                nx[j] = inputR;
                                            }
                                        }
                                        return nx;
                                    }
                                };
                                dataSet.addTransformer(sameAsReplacer);

                                dataSet.crawlURI(newR);

                                dataSet.removeTransformer(sameAsReplacer);
                            }
                        };
                        t.start();
                        threads.add(t);
                    } else {
                        TripleTransformer sameAsReplacer = new TripleTransformer() {
                            @Override
                            public Node[] transformTriple(DataSet dataSet, Node[] nx) {
                                for (int j = 0; j < nx.length; j++) {
                                    if (nx[j].equals(newR)) {
                                        nx[j] = inputR;
                                    }
                                }
                                return nx;
                            }
                        };
                        dataSet.addTransformer(sameAsReplacer);

                        dataSet.crawlURI(newR);

                        dataSet.removeTransformer(sameAsReplacer);
                    }
                }

            }
            if(threaded) {
                for (Thread t : threads) {
                    try {
                        t.join();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(LIDSQueryEngine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }


        return results;
    }

}
