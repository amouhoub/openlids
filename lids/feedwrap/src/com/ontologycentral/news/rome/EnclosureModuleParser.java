package com.ontologycentral.news.rome;

import java.net.URI;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;

import com.sun.syndication.feed.module.Module;
import com.sun.syndication.feed.module.mediarss.MediaEntryModuleImpl;
import com.sun.syndication.feed.module.mediarss.types.MediaContent;
import com.sun.syndication.feed.module.mediarss.types.Reference;
import com.sun.syndication.feed.module.mediarss.types.UrlReference;
import com.sun.syndication.io.ModuleParser;

public class EnclosureModuleParser implements ModuleParser {
	private static final Logger LOG = Logger.getLogger(EnclosureModuleParser.class.getName());

	private static final String URI = "http://purl.oclc.org/net/rss_2.0/enc#";
	private static final String RDF_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	
	private static final Namespace ENC_NS = Namespace.getNamespace("enc", URI);
	private static final Namespace RDF_NS = Namespace.getNamespace(RDF_URI);
	 
	public String getNamespaceUri() {
		return URI;
	}

	public Module parse(Element root) {
		boolean found = false;
		
		MediaEntryModuleImpl em = new MediaEntryModuleImpl();

		//Element e = root.getChild("enclosure", ENC_NS);
		
		List<Element> li = (List<Element>)root.getChildren("enclosure", ENC_NS);
		if (li.size() > 0) {
			try {
				for (Element e : li) {
					String uri = null;
					String type = null;
					long length = 0;
					
					Attribute a = e.getAttribute("resource", RDF_NS);
					if (a != null) {
						uri = a.getValue();
					}
					
					a = e.getAttribute("length", ENC_NS);
					if (a != null) {
						length = Long.parseLong(a.getValue());
					}
					
					a = e.getAttribute("type", ENC_NS);
					if (a != null) {
						type = a.getValue();
					}

					Reference ref = new UrlReference(new URI(uri));
					MediaContent mc = new MediaContent(ref);
					mc.setType(type);
					mc.setFileSize(length);
					em.setMediaContents(new MediaContent[] { mc } );
					
					found = true;
				} 
			} catch (Exception ex) {
				LOG.log(Level.WARNING, "Exception parsing content tag.", ex);
			}
        }
		
		return (found) ? em : null;
	}
	
    protected final String getURI(Element desc) {
        String d = null;
        Element enc = desc.getChild("enclosure", ENC_NS);
        if (enc != null) {
            Attribute a = enc.getAttribute("resource", RDF_NS);
            if (a != null) {
                d = a.getValue();
            }
        }
        return d;
    }
}
