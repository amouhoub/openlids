/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.parser.arq;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.ElementVisitorBase;

import org.openlids.model.LIDSDescription;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public class SPARQLVisitor extends ElementVisitorBase {

    LIDSDescription desc;

    public SPARQLVisitor(LIDSDescription desc) {
        this.desc = desc;
    }

    public SPARQLVisitor() {
        desc = new LIDSDescription();
    }

    public LIDSDescription getDescription() {
        return desc;
    }

    public void setDescription(LIDSDescription desc) {
        this.desc = desc;
    }

    @Override
    public void visit(ElementGroup eg) {
        for (Element elem : eg.getElements()) {
            elem.visit(this);
        }
    }


    @Override
    public void visit(ElementTriplesBlock arg0) {
        for (Triple triple : arg0.getPattern().getList()) {
            desc.addInputBGP(new Node[] {
                    JenaToNx.convert(triple.getSubject()),
                    JenaToNx.convert(triple.getPredicate()),
                    JenaToNx.convert(triple.getObject())});


        }
    }

	
}
