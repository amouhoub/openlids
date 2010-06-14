package org.openlids.parser;

import org.openlids.model.BGP;
import org.openlids.model.IRI;
import org.openlids.model.ServiceDescription;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.sparql.syntax.TemplateGroup;
import com.hp.hpl.jena.sparql.syntax.TemplateTriple;
import com.hp.hpl.jena.sparql.syntax.TemplateVisitor;



public class ServiceParserJena implements ServiceParser {

	public ServiceDescription parseServiceDescription(String serviceDescriptionTxt) {
		
		Query q = QueryFactory.create(serviceDescriptionTxt);
		

		
		SPARQLJenaVisitor v = new SPARQLJenaVisitor(); 
		q.getQueryPattern().visit(v);
		
		final ServiceDescription desc = v.getDescription();
		
		desc.setEndpoint(new IRI(q.getGraphURIs().get(0)));

		q.getConstructTemplate().visit(new TemplateVisitor() {
			
			public void visit(TemplateTriple arg0) {
				desc.addOutputBGP(new BGP(
						SPARQLJenaVisitor.node2Value(arg0.getTriple().getSubject()),
						SPARQLJenaVisitor.node2Value(arg0.getTriple().getPredicate()),
						SPARQLJenaVisitor.node2Value(arg0.getTriple().getObject())));
			}
			
			public void visit(TemplateGroup arg0) {
				for(Template t : arg0.getTemplates()) {
					t.visit(this);
				}
				
			}
		});
		
		return desc;
	}
		
}
