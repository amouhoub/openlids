/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.List;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public class JoinNode extends ReteNode {

    AlphaMemory amem;
    int posAMem = 0;
    int posBMem = 0;


    
    public void leftActivation() {
        for(ReteNode child : children) {
            for (Node[] token : amem.getTriples()) {
                child.leftActivation(token);
            }
        }
    }

    public void leftActivation(Node[] token) {
        List<Node[]> items = amem.get(posAMem, token[posBMem]);
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
        List<Node[]> tokens = ((BetaMemory) parent).get(posBMem, item[posAMem]);
        for (Node[] token : tokens) {
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

    void setParent(ReteNode current) {
        this.parent = current;
    }

    void setAMem(AlphaMemory alphaMemory) {
        this.amem = alphaMemory;
    }
}
