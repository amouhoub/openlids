/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Variable;


class AlphaMemory {

    Set<Node[]> triples = new TreeSet<Node[]>(NodeComparator.NC);
    Set<ReteNode> successors = new HashSet<ReteNode>();
    
    public void add(Node[] triple) {
        if(triples.add(triple)) {
            for(ReteNode jn : successors) {
                ((JoinNode) jn).rightActivation(triple);
            }
        }
    }

    List<Node[]> get(int posAMem, Node node) {
        List<Node[]> results = new LinkedList<Node[]>();
        for(Node[] triple : triples) {
            if(triple[posAMem].equals(node)) {
                results.add(triple);
            }
        }
        return results;
    }

    void addChild(ReteNode aThis) {
        for(Node[] triple : triples) {
            ((JoinNode) aThis).rightActivation(triple);
        }
        successors.add(aThis);
    }
    
}

/**
 *
 * @author ssp
 */
public class AlphaNetwork {

    Set<Node[]> triples = new TreeSet<Node[]>(NodeComparator.NC);

    Map<Node[], AlphaMemory> fff = new TreeMap(NodeComparator.NC);
    Map<Node[], AlphaMemory> ffb = new TreeMap(NodeComparator.NC);
    Map<Node[], AlphaMemory> fbf = new TreeMap(NodeComparator.NC);
    Map<Node[], AlphaMemory> fbb = new TreeMap(NodeComparator.NC);
    Map<Node[], AlphaMemory> bff = new TreeMap(NodeComparator.NC);
    Map<Node[], AlphaMemory> bfb = new TreeMap(NodeComparator.NC);
    Map<Node[], AlphaMemory> bbf = new TreeMap(NodeComparator.NC);
    Map<Node[], AlphaMemory> bbb = new TreeMap(NodeComparator.NC);

    public AlphaMemory getAlphaMemory(Node[] pattern) {
        if(pattern[0] instanceof Variable) {
            if(pattern[1] instanceof Variable) {
                if(pattern[2] instanceof Variable) {
                    AlphaMemory amem = fff.get(new Node[0]);
                    if(amem == null) {
                        amem = new AlphaMemory();
                        for(Node[] triple : triples) {
                            amem.add(triple);
                        }
                        fff.put(new Node[0],amem);
                    }
                    return amem;
                } else {
                    Node[] sp = new Node[]{pattern[2]};
                    AlphaMemory amem = ffb.get(sp);
                    if(amem == null) {
                        amem = new AlphaMemory();
                        for (Node[] triple : triples) {
                            if(pattern[2].equals(triple[2])) {
                                amem.add(triple);
                            }
                        }
                        ffb.put(sp, amem);
                    }
                    return amem;
                }
            } else {
                if(pattern[2] instanceof Variable) {
                     Node[] sp = new Node[]{pattern[1]};
                    AlphaMemory amem = fbf.get(sp);
                    if(amem == null) {
                        amem = new AlphaMemory();
                        for (Node[] triple : triples) {
                            if(pattern[1].equals(triple[1])) {
                                amem.add(triple);
                            }
                        }
                        fbf.put(sp, amem);
                    }
                    return amem;
                } else {
                    Node[] sp = new Node[]{pattern[1], pattern[2]};
                    AlphaMemory amem = fbb.get(sp);
                    if(amem == null) {
                        amem = new AlphaMemory();
                         for (Node[] triple : triples) {
                            if(pattern[1].equals(triple[1]) && pattern[2].equals(triple[2])) {
                                amem.add(triple);
                            }
                        }
                        fbb.put(sp, amem);
                    }
                    return amem;
                }
            }
        } else {
            if(pattern[1] instanceof Variable) {
                if(pattern[2] instanceof Variable) {
                    Node[] sp = new Node[]{pattern[0]};
                    AlphaMemory amem = bff.get(sp);
                    if(amem == null) {
                        amem = new AlphaMemory();
                         for (Node[] triple : triples) {
                            if(pattern[0].equals(triple[0])) {
                                amem.add(triple);
                            }
                        }
                        bff.put(sp, amem);
                    }
                    return amem;
                } else {
                    Node[] sp = new Node[]{pattern[0],pattern[2]};
                    AlphaMemory amem = bfb.get(sp);
                    if(amem == null) {
                        amem = new AlphaMemory();
                        for (Node[] triple : triples) {
                            if(pattern[0].equals(triple[0]) && pattern[2].equals(triple[2])) {
                                amem.add(triple);
                            }
                        }
                        bfb.put(sp, amem);
                    }
                    return amem;
                }
            } else {
                if(pattern[2] instanceof Variable) {
                    Node[] sp = new Node[]{pattern[0], pattern[1]};
                    AlphaMemory amem = bbf.get(sp);
                    if(amem == null) {
                        amem = new AlphaMemory();
                        for (Node[] triple : triples) {
                            if(pattern[0].equals(triple[0]) && pattern[1].equals(triple[1])) {
                                amem.add(triple);
                            }
                        }
                        bbf.put(sp, amem);
                    }
                    return amem;
                } else {
                    Node[] sp = new Node[]{pattern[0],pattern[1],pattern[2]};
                    AlphaMemory amem = bbb.get(sp);
                    if(amem == null) {
                        amem = new AlphaMemory();
                        for (Node[] triple : triples) {
                            if(pattern[0].equals(triple[0]) && pattern[1].equals(triple[1]) && pattern[2].equals(triple[2])) {
                                amem.add(triple);
                            }
                        }
                        bbb.put(sp, amem);
                    }
                    return amem;
                }
            }
        }
    }

    public void addTriple(Node[] triple) {

        if(triples.add(triple)) {
            List<AlphaMemory> memories = new LinkedList<AlphaMemory>();
            memories.add(fff.get(new Node[0]));
            memories.add(ffb.get(new Node[]{triple[2]}));
            memories.add(fbf.get(new Node[]{triple[1]}));
            memories.add(bff.get(new Node[]{triple[0]}));
            memories.add(fbb.get(new Node[]{triple[1], triple[2]}));
            memories.add(bbf.get(new Node[]{triple[0], triple[1]}));
            memories.add(bfb.get(new Node[]{triple[0], triple[2]}));
            memories.add(bbb.get(new Node[]{triple[0], triple[1], triple[2]}));
            for (AlphaMemory am : memories) {
                if (am != null) {
                    am.add(triple);
                }
            }
        }
    }

}
