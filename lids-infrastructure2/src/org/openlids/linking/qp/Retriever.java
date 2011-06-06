/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.openlids.util.Utils;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class Retriever {

    static final Logger _log = Logger.getLogger(Retriever.class.getName());

    public final Rete _rete;
    public final Manager _manager;

    public Retriever() {
        _rete = new Rete();
        _manager = new Manager(_rete);

        List<Node[]> lidsDescRule = new LinkedList<Node[]>();
        lidsDescRule = LIDSPNode.PATTERNS;
        _rete.addProduction(lidsDescRule, new LIDSPNode(_rete, _manager));

        List<Node[]> crawler_rule = Utils.parseNxSetNoException("?s ?p ?o");
        _rete.addProduction(crawler_rule, new PNode() {
            @Override
            public void leftActivation(Node[] token) {
                int i = 0;
                for(Node n : token) {
                    if(i == 1) continue;
                    if(n instanceof Resource) {
                        _manager.addURI(n.toString());
                    }
                    i++;
                }
            }
        });
    }

    public void start() {
        _manager.start();
        _rete.start();
    }

    public void shutdown() throws InterruptedException {
        _rete.shutdown();
    }

    public void addURI(String start_uri) {
        _manager.addURI(start_uri);
    }

    public void addQuery(SelectQuery sq) {
        Map<Node, Node> varToBNode = new HashMap<Node, Node>();
        int nvar = 0;
        for (Node[] nx : sq.getPatterns()) {
            Node[] nnx = new Node[nx.length];
            int i = 0;
            for(Node n : nx) {
                if(n instanceof Variable) {
                    Node bnode = varToBNode.get(n);
                    if(bnode == null) {
                        bnode = new BNode("query" + sq.hashCode() + "-var" + nvar);
                        nvar++;
                        varToBNode.put(n,bnode);
                    }
                    nnx[i] = bnode;
                } else {
                    nnx[i] = n;
                }
                i++;
            }
            _rete.addTriple(nnx);
        }

       // sq.setRete(_rete);
    }

}
