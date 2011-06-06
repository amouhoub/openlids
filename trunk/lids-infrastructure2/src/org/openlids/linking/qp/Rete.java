/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openlids.linking.qp;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Variable;


/*
 * @@@@ Manager does never end
 * @@@@ sameAs Handling
 * @@@@ Especially Service Input Entity equivalence Expression
 * @@@@ Everything synchronised? Deadlockfree?
 * @@@@ URI Prioritizing
 * @@@@ Credentials of Services
 * @@@@ Inefficiencies like List Traversing, no indices
 * @@@@ Reasoning: Inverse Properties; subClassOf; functional properties; ...
 */
/**
 *
 * @author ssp
 */

class Production {
    List<Node[]> patterns;
    PNode pnode;
}



public class Rete extends Thread {
    public ReteStatistics getStatistics() {
        ReteStatistics rs = new ReteStatistics();
        rs._prods_queue = _prods_queue;
        rs._rete_nodes = _rete_nodes;
        rs._sameAs = _sameAs;
        rs._triples_queue = _triples_queue;
        rs._triples = triples;
        return rs;
    }

    AlphaNetwork anet = new AlphaNetwork();
    Set<Node[]> triples = new TreeSet<Node[]>(NodeComparator.NC);

    final BlockingQueue<Node[]> _triples_queue = new LinkedBlockingQueue<Node[]>();
    final BlockingQueue<Production> _prods_queue = new LinkedBlockingQueue<Production>();
    
    final Map<Node, LinkedList<Node>> _sameAs = new ConcurrentHashMap<Node, LinkedList<Node>>();
    final Set<ReteNode> _rete_nodes = Collections.synchronizedSet(new HashSet<ReteNode>());
    Boolean _running = true;

    private void actuallyAddTriple(Node[] triple) {
        if (triple.length < 3) {
            return;
        }
        if (triple[1].equals(NS.OWL_SAMEAS)) {
            Set<Node[]> newPairs = new TreeSet<Node[]>(NodeComparator.NC);
            synchronized (_sameAs) {
                LinkedList<Node> leftList = _sameAs.get(triple[0]);
                LinkedList<Node> rightList = _sameAs.get(triple[2]);
                if (leftList == null) {
                    if (rightList == null) {
                        leftList = new LinkedList<Node>();
                        leftList.add(triple[0]);
                        leftList.add(triple[2]);
                        rightList = leftList;
                        _sameAs.put(triple[0], leftList);
                        _sameAs.put(triple[2], rightList);
                        newPairs.add(new Node[]{triple[0], triple[2]});
                    } else {
                        for (Node right : rightList) {
                            newPairs.add(new Node[]{triple[0], right});
                        }
                        leftList = rightList;
                        leftList.addFirst(triple[0]);
                        _sameAs.put(triple[0], leftList);
                    }
                } else {
                    if (rightList == null) {
                        // leftList != null && rightList == null
                        for (Node left : leftList) {
                            newPairs.add(new Node[]{left, triple[2]});
                        }
                        rightList = leftList;
                        rightList.add(triple[2]);
                        _sameAs.put(triple[2], rightList);
                    } else {
                        if (rightList == leftList) {
                            return;
                        } else {
                            // none is null; add all to left;
                            for (Node left : leftList) {
                                for (Node right : rightList) {
                                    newPairs.add(new Node[]{left, right});
                                }
                            }
                            leftList.addAll(rightList);
                            for (Node right : rightList) {
                                _sameAs.put(right, leftList);
                            }
                        }
                    }
                }
            }
            synchronized (_rete_nodes) {
                for (ReteNode rn : _rete_nodes) {
                    if (rn instanceof JoinNode) {
                        ((JoinNode) rn).notifyNewSameAsPairs(newPairs);
                    }
                }
            }
        } else {
            if (triples.add(triple)) {
                anet.addTriple(triple);
            }
        }
    }

    @Override
    public void run() {
        while (_running) {
            Node[] triple = null;
            try {
                List<Production> prods = new LinkedList<Production>();
                synchronized (_prods_queue) {
                    while(!_prods_queue.isEmpty()) {
                        Production prod = _prods_queue.poll();
                        if(prod != null) {
                            prods.add(prod);
                        }
                    }

                }
                for(Production prod : prods) {
                    actuallyAddProduction(prod.patterns, prod.pnode);
                }

                synchronized (_triples_queue) {
                    triple = _triples_queue.poll(200, TimeUnit.MILLISECONDS);
                }
                if (triple != null) {
                    actuallyAddTriple(triple);
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Rete.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void shutdown() throws InterruptedException {
        _running = false;
        this.join();
    }
   
    public void addTriple(Node[] triple) {
        synchronized(_triples_queue) {
            _triples_queue.add(triple);
        }
//        if(triples.add(triple)) {
//            anet.addTriple(triple);
//        }
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
        Production prod = new Production();
        prod.patterns = patterns;
        prod.pnode = pnode;
        synchronized (_prods_queue) {
            _prods_queue.add(prod);
        }
    }

    public void actuallyAddProduction(List<Node[]> patterns, PNode pnode) {
        if (patterns.isEmpty()) {
            // Do nothing
            // @@@ or react to every triple?
            return;
        }

//    sort patterns into joinable order
        List<Node[]> patterns_copy = new LinkedList<Node[]>();
        patterns_copy.addAll(patterns);
        List<Node[]> ordered_patterns = new LinkedList<Node[]>();
        ordered_patterns.add(patterns_copy.remove(0));
        while (!patterns_copy.isEmpty()) {
            Node[] found_p = null;
            for (Node[] p : patterns_copy) {
                if (is_joinable(p, ordered_patterns)) {
                    found_p = p;
                    break;
                }
            }
            if (found_p == null) {
                System.err.println("Error. Rule is not connected.");
                return;
            }
            ordered_patterns.add(found_p);
            patterns_copy.remove(found_p);
        }

        Node[] fields = new Node[patterns.size() * 3];
        int i = 0;
        for (Node[] p : ordered_patterns) {
            for (Node n : p) {
                fields[i] = n;
                i++;
            }
        }

        ReteNode current = new DummyNode();
        synchronized (_rete_nodes) {
            _rete_nodes.add(current);
        }

        int varpos = 0;

        // @@@@ No Reuse of Join Nodes / BetaMemories

        for (Node[] pattern : ordered_patterns) {
            JoinNode join = new JoinNode(_sameAs);
            AlphaMemory amem = anet.getAlphaMemory(pattern);
            join.setParent(current);
            join.setAMem(amem);
            if (varpos == 0) {
                join.posAMem = 0;
                join.posBMem = 0;
            } else {
                int j = 0;
                for (Node f : fields) {
                    if (f instanceof Variable) {
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

            synchronized (_rete_nodes) {
                _rete_nodes.add(join);
                _rete_nodes.add(amem);
                _rete_nodes.add(bmem);
            }

            current = bmem;
            varpos += 3;
        }

        pnode.setFields(fields);
        current.addChild(pnode);
        synchronized (_rete_nodes) {
            _rete_nodes.add(pnode);
        }

        // for each condition get amem and join


    }
    // @@@@ No cyclic patterns
}
