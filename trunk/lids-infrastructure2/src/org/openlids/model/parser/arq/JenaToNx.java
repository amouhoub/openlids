/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.parser.arq;

import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class JenaToNx {
    public static Node convert(com.hp.hpl.jena.graph.Node node) {
        if (node instanceof Node_Variable) {
            Variable v = new Variable(((Node_Variable) node).getName());
            return v;
        } else if (node instanceof Node_Literal) {
            Literal l = new Literal(((Node_Literal) node).getLiteral().getLexicalForm());
            return l;
        } else if (node instanceof Node_Blank) {
            BNode b = new BNode(((Node_Blank) node).getBlankNodeLabel());
            return b;
        } else if (node instanceof Node_URI) {
            Resource r = new Resource(((Node_URI) node).getURI());
            return r;
        } else {
            Logger.getLogger("JenaToNx").log(Level.SEVERE, "Unknown node class: {0}", node.getClass());
        }
        return null;
    }
}
