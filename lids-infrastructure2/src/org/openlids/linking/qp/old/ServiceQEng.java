/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp.old;

import com.hp.hpl.jena.sparql.function.library.not;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class ServiceQEng {

    Map<Node[],List<Consumer>> patternsToConsumers = new TreeMap(NodeComparator.NC);

    public synchronized void addRule(Node[] head, List<Node[]> patterns, Consumer handler) {
        Set<Node[]> todo_patterns = new TreeSet<Node[]>(NodeComparator.NC);
        todo_patterns.addAll(patterns);


        Set<Node> variables = new HashSet<Node>();
        for(Node[] pattern : patterns) {
            for(Node v : pattern) {
                if(v instanceof Variable) {
                    variables.add(v);
                }
            }
        }
        int nvars = variables.size();

        int posvar = 0;
        Node[] vars = new Node[nvars];


        Consumer left = null;
        Consumer right = null;

        int i = 0;
        while (i < patterns.size() && todo_patterns.size() != 0) {
            for(Node[] pattern : patterns) {
                if(todo_patterns.contains(pattern)) {
// add pattern

                    if(posvar == 0) {
                        Join nj = new Join();
                        left = nj.getLeftConsumer();
                        right = nj.getRightConsumer();
                        nj.addConsumer(handler);
                    }

                }
            }
            i++;
        }
        if(todo_patterns.size() != 0) {
            System.err.println("There does not seem to be a connected pattern set that can be used?");
        }
    }

    private void ale(List<Node[]> patterns, int posvar, Node[] vars, Join feeder, Consumer handler) {
        if(patterns.size() == 0) {
            if(feeder != null)
                feeder.addConsumer(handler);
            return;
        } else if(feeder == null) {
            // start it, initial join or if single pattern directly to handler
            if(patterns.size() == 1) {
                directly to handler
                        return;
            }
            Node[] first_pattern = patterns.remove(0);
            feeder = new Join();

            for(Node n : first_pattern) {
                int i = 0;
                for(;i<posvar;i++) {
                    if(vars[i] == n)
                        break;
                }
                if(i == posvar) {
                    vars[posvar] = n;
                    posvar++;
                }
            }

add to first pattern            feeder.getLeftConsumer()
            ale(patterns, feeder, handler);
        } else {
        }

            add Pattern.Pattern
                    Consumer = Consumer
        } else if(patterns.size())
    }

    public synchronized void addFact(Node[] fact) {
        for(Node[] pattern : patternsToConsumers.keySet()) {
            if(pattern.matches(fact)) {
                getConsumers ... let them consume.
            }
        }
    }


}
