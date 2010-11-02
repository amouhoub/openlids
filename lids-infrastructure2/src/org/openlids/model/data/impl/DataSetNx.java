/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data.impl;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.openlids.model.data.DataSet;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;

/**
 *
 * @author ssp
 */
public class DataSetNx extends DataSet {

    TreeSet<Node[]> _data = new TreeSet<Node[]>(NodeComparator.NC);
   
    @Override
    public void addTripleImpl(Node[] nx) {
        _data.add(nx);
    }

    @Override
    public void addTriplesImpl(Set<Node[]> triples) {
        _data.addAll(triples);
    }

    @Override
    public Iterator<Node[]> match(Node[] tp) {
        final TreeSet<Node[]> results = new TreeSet<Node[]>(NodeComparator.NC);

        if (tp[1] != null && tp[1].toString().equals("http://www.w3.org/2000/10/swap/log#uri")) {
            Resource urir = new Resource("http://www.w3.org/2000/10/swap/log#uri");
            if (tp[0] == null && tp[2] == null) {
                for (Node[] nx : _data) {
                    if (nx[0] instanceof Resource) {
                        results.add(new Node[]{nx[0], urir, new Literal(nx[0].toString())});
                    }
                    if (nx[2] instanceof Resource) {
                        results.add(new Node[]{nx[2], urir, new Literal(nx[2].toString())});
                    }
                }
            } else if ((tp[2] == null) && tp[0] instanceof Resource) {
                results.add(new Node[]{tp[0], urir, new Literal(tp[0].toString())});
            } else if ((tp[2] instanceof Literal) && (tp[0] instanceof Resource)) {
                if (tp[2].toString().equals(tp[0].toString())) {
                    results.add(new Node[]{tp[0], urir, tp[2]});
                }
            }
            return results.iterator();
        }

        for (Node[] nx : _data) {
            if (match(tp, nx)) {
                results.add(nx);
            }
        }
        return results.iterator();
    }

    private static boolean match(Node[] tp, Node[] nx) {
        for (int i = 0; i < tp.length; i++) {
            Node n = tp[i];
            if (n != null) { // null == variable ; !(n instanceof Variable)) {
                if (!n.equals(nx[i])) {
                    if (n instanceof Resource && nx[i] instanceof Resource) {
                        if (n.toString().startsWith("file:/") && nx[i].toString().startsWith("file:/")) {
                            if (n.toString().startsWith("file:///") && !nx[i].toString().startsWith("file:///")) {
                                if (n.toString().substring(7).equals(nx[i].toString().substring(5))) {
                                    continue;
                                }
                            }
                            if (!n.toString().startsWith("file:///") && nx[i].toString().startsWith("file:///")) {
                                if (n.toString().substring(5).equals(nx[i].toString().substring(7))) {
                                    continue;
                                }
                            }
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

}
