/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.parser.arq;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.syntax.TemplateVisitor;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openlids.model.LIDSDescription;
import org.openlids.model.parser.LIDSParser;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public class LIDSParserARQ implements LIDSParser {

    public LIDSDescription parseLIDSDescription(String lidsDescriptionStr) {
        try {
            Query q = QueryFactory.create(lidsDescriptionStr);
            SPARQLVisitor v = new SPARQLVisitor();
            q.getQueryPattern().visit(v);
            final LIDSDescription desc = v.getDescription();
            desc.setEndpoint(new URI(q.getGraphURIs().get(0)));
            q.getConstructTemplate().visit(new TemplateVisitor() {

                public void visit(TemplateTriple arg0) {
                    desc.addOutputBGP(new Node[] {
                        JenaToNx.convert(arg0.getTriple().getSubject()),
                        JenaToNx.convert(arg0.getTriple().getPredicate()),
                        JenaToNx.convert(arg0.getTriple().getObject()) });
                }

                public void visit(TemplateGroup arg0) {
                    for (Template t : arg0.getTemplates()) {
                        t.visit(this);
                    }
                }
            });
            return desc;
        } catch (URISyntaxException ex) {
            Logger.getLogger(LIDSParserARQ.class.getName()).log(Level.SEVERE, "Error in lidsDescription: \"" + lidsDescriptionStr + "\"", ex);
            return null;
        }
    }

}
