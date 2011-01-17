/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;


/*
 * @@@@ Manager does never end
 * @@@@ sameAs Handling
 * @@@@ Especially Service Input Entity equivalence Expression
 * @@@@ URI Monitor (find all URIs to retrieve)
 * @@@@ Everything synchronised? Deadlockfree?
 * @@@@ URI Prioritizing
 * @@@@ Credentials of Services
 * @@@@ Testing Conditions consisting of several patterns.
 * @@@@ Inefficiencies like List Traversing, no indices
 * @@@@ Reasoning: Inverse Properties; subClassOf; functional properties; ...
 */


/**
 *
 * @author ssp
 */
public class Rete {

    AlphaNetwork anet = new AlphaNetwork();
    Set<Node[]> triples = new TreeSet<Node[]>(NodeComparator.NC);

    public void addTriple(Node[] triple) {
        if(triples.add(triple)) {
            anet.addTriple(triple);
        }
    }

    private boolean is_joinable(Node[] p, List<Node[]> ordered_patterns) {
        for (Node[] op : ordered_patterns) {
            for (Node v1 : p) {
                for (Node v2 : op) {
                    if (v1 instanceof Variable && v2 instanceof Variable && v1.equals(v2)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void addProduction(List<Node[]> patterns, PNode pnode) {
        if(patterns.size() == 0) {
            // Do nothing
            // @@@ or react to every triple?
            return;
        }
     
//    sort patterns into joinable order
        List<Node[]> patterns_copy = new LinkedList<Node[]>();
        patterns_copy.addAll(patterns);
        List<Node[]> ordered_patterns = new LinkedList<Node[]>();
        ordered_patterns.add(patterns_copy.remove(0));
        while(!patterns_copy.isEmpty()) {
            Node[] found_p = null;
            for(Node[] p : patterns_copy) {
                if (is_joinable(p, ordered_patterns)) {
                    found_p = p;
                    break;
                }
            }
            if(found_p == null) {
                System.err.println("Error. Rule is not connected.");
                return;
            }
            ordered_patterns.add(found_p);
            patterns_copy.remove(found_p);
        }

        Node[] fields = new Node[patterns.size() * 3];
        int i = 0;
        for(Node[] p : ordered_patterns) {
            for(Node n : p) {
                fields[i] = n;
                i++;
            }
        }

        ReteNode current = new DummyNode();

        int varpos = 0;

        // @@@@ No Reuse of Join Nodes / BetaMemories

        for(Node[] pattern : ordered_patterns) {
            JoinNode join = new JoinNode();
            AlphaMemory amem = anet.getAlphaMemory(pattern);
            join.setParent(current);
            join.setAMem(amem);
            if(varpos == 0) {
                join.posAMem = 0;
                join.posBMem = 0;
            } else {
                int j = 0;
                for (Node f : fields) {
                    if(f instanceof Variable) {
                        if (f.equals(pattern[0])) {
                            join.posAMem = 0;
                            join.posBMem = j;
                            break;
                        }
                        if (f.equals(pattern[1])) {
                            join.posAMem = 1;
                            join.posBMem = j;
                            break;
                        }
                        if (f.equals(pattern[2])) {
                            join.posAMem = 2;
                            join.posBMem = j;
                            break;
                        }
                    }
                    j++;
                }

            }
            // The order of these is essential for the flow of notifications!
            BetaMemory bmem = new BetaMemory();
            bmem.setParent(join);
            join.addChild(bmem);
            current.children.add(join);
            amem.addChild(join);

            current = bmem;
            varpos += 3;
        }
        
        pnode.setFields(fields);
        current.addChild(pnode);

        // for each condition get amem and join


    }

            // @@@@ No cyclic patterns
}
