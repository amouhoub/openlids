/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.parser.arq;

import org.openlids.model.parser.arq.*;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Literal;
import com.hp.hpl.jena.graph.Node_URI;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.AnonId;
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
public class NxToJena {
    public static com.hp.hpl.jena.graph.Node convert(Node node) {
        if (node instanceof Resource) {
            return com.hp.hpl.jena.graph.Node.createURI(((Resource) node).toString());
        } else if (node instanceof Literal) {
            return com.hp.hpl.jena.graph.Node.createLiteral(((Literal) node).toString());
        } else if (node instanceof BNode) {
            return com.hp.hpl.jena.graph.Node.createAnon(new AnonId(((BNode) node).toString()));
        } else if (node instanceof Variable) {
            return Var.alloc(((Variable) node).toString());
//            return com.hp.hpl.jena.graph.Node.createVariable(((Variable) node).toString());
        }
        System.err.println(node.getClass().toString() + " unknown?");
        return null;
    }

    public static Triple convert(Node[] nx) {
        return new Triple(convert(nx[0]), convert(nx[1]), convert(nx[2]));
    }
}
