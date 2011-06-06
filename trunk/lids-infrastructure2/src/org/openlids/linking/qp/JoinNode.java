/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public class JoinNode extends ReteNode {

    AlphaMemory amem;
    int posAMem = 0;
    int posBMem = 0;

    final Map<Node,LinkedList<Node>> _sameAs;

    public JoinNode(Map<Node,LinkedList<Node>> sameAs) {
        _sameAs = sameAs;
    }



    
    public void leftActivation() {
        for(ReteNode child : children) {
            for (Node[] token : amem.getTriples()) {
                child.leftActivation(token);
            }
        }
    }

    public void leftActivation(Node[] token) {
        List<Node[]> items = new LinkedList<Node[]>();


        List<Node> sameAsEs;
        synchronized (_sameAs) {
            sameAsEs = _sameAs.get(token[posBMem]);
        }
        if (sameAsEs == null) {
            sameAsEs = new LinkedList<Node>();
            sameAsEs.add(token[posBMem]);
        }
        for (Node joinNode : sameAsEs) {
            items.addAll(amem.get(posAMem, joinNode));
        }
        for(Node[] item : items) {
            Node[] new_token = new Node[token.length + item.length];
            int i = 0;
            for(Node n : token) {
                new_token[i++] = n;
            }
            for(Node n : item) {
                new_token[i++] = n;
            }
            for(ReteNode child : children) {
                child.leftActivation(new_token);
            }
        }
    }

    public void rightActivation(Node[] item) {
        if(parent instanceof DummyNode) {
            for(ReteNode child : children) {
                child.leftActivation(item);
            }
        } else {
            List<Node[]> tokens = new LinkedList<Node[]>();
            List<Node> sameAsEs;
            synchronized (_sameAs) {
                sameAsEs = _sameAs.get(item[posAMem]);
            }
            if (sameAsEs == null) {
                sameAsEs = new LinkedList<Node>();
                sameAsEs.add(item[posAMem]);
            }
            for (Node joinNode : sameAsEs) {
                tokens.addAll(((BetaMemory) parent).get(posBMem, joinNode));
            }
            
//            List<Node[]> tokens = ((BetaMemory) parent).get(posBMem, item[posAMem]);
            for (Node[] token : tokens) {
                Node[] new_token = new Node[token.length + item.length];
                int i = 0;
                for (Node n : token) {
                    new_token[i++] = n;
                }
                for (Node n : item) {
                    new_token[i++] = n;
                }
                for (ReteNode child : children) {
                    child.leftActivation(new_token);
                }
            }
        }
    }

    void setParent(ReteNode current) {
        this.parent = current;
    }

    void setAMem(AlphaMemory alphaMemory) {
        this.amem = alphaMemory;
    }

    void notifyNewSameAsPairs(Set<Node[]> newPairs) {
        if(parent instanceof DummyNode) {
            return;
        } else if ( 4 < 9 - 4*2) {
//            return;
        }

        Node left = null;
        Node right = null;
        List<Node[]> leftAMem = null;
        List<Node[]> rightAMem = null;
        List<Node[]> leftBMem = null;
        List<Node[]> rightBMem = null;
        for(Node[] np : newPairs) {
            right = np[1];
            if(left != null && left.equals(np[0])) {

            } else {
                left = np[0];
                leftAMem = amem.get(posAMem, left);
                leftBMem = ((BetaMemory) parent).get(posBMem, left);
            }
            rightAMem = amem.get(posBMem, right);
            rightBMem = ((BetaMemory) parent).get(posBMem, right);

            for(Node[] item : leftAMem) {
                for (Node[] token : rightBMem) {
                    Node[] new_token = new Node[token.length + item.length];
                    int i = 0;
                    for (Node n : token) {
                        new_token[i++] = n;
                    }
                    for (Node n : item) {
                        new_token[i++] = n;
                    }
                    for (ReteNode child : children) {
                        child.leftActivation(new_token);
                    }
                }
            }
            for(Node[] item : rightAMem) {
                for (Node[] token : rightBMem) {
                    Node[] new_token = new Node[token.length + item.length];
                    int i = 0;
                    for (Node n : token) {
                        new_token[i++] = n;
                    }
                    for (Node n : item) {
                        new_token[i++] = n;
                    }
                    for (ReteNode child : children) {
                        child.leftActivation(new_token);
                    }
                }
            }
        }
    }
}
