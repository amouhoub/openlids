package org.openlids.feedwrap;

import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.sun.syndication.feed.synd.SyndCategory;
import com.sun.syndication.feed.synd.SyndEnclosure;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndPerson;

public class OutputFeed {
	transient Logger _log = Logger.getLogger(this.getClass().getName());

	public static SimpleDateFormat ISO8601FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
	public static SimpleDateFormat RFC822DATEFORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);

	public OutputFeed() {
		;
	}
	
	public void output(SyndFeed feed, Writer out, boolean striphtml) throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter ch;

		ch = factory.createXMLStreamWriter(out);

		ch.writeStartDocument("utf-8", "1.0");

		ch.writeStartElement("rdf:RDF");
		ch.writeDefaultNamespace("http://purl.org/rss/1.0/");
		ch.writeNamespace("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		ch.writeNamespace("sioc", "http://rdfs.org/sioc/ns#");
		ch.writeNamespace("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		ch.writeNamespace("dcterms", "http://purl.org/dc/terms/");
		ch.writeNamespace("skos", "http://www.w3.org/2008/05/skos#");
		ch.writeNamespace("foaf", "http://xmlns.com/foaf/0.1/");
		ch.writeNamespace("enc", "http://purl.oclc.org/net/rss_2.0/enc#");

		ch.writeStartElement("rdf:Description");
		ch.writeAttribute("rdf:about", "");
		ch.writeStartElement("rdfs:comment");
		ch.writeCharacters("Content via feedwrap (http://feedwrap.openlids.org/).");
		ch.writeEndElement();
		ch.writeEndElement();
		
		ch.writeStartElement("sioc:Forum");
		ch.writeAttribute("rdf:ID", "forum");
		
		//if (feed.getUri() != null) {
		//	ch.writeAttribute("rdf:about", feed.getUri());
		//}

		if (feed.getTitle() != null) {
			ch.writeStartElement("dcterms:title");
			ch.writeCharacters(stripHTML(feed.getTitle()));
			ch.writeEndElement();
		}

		String channel = feed.getLink();
		if (channel == null) {
			channel = feed.getUri();
		}
		if (channel == null) {
			channel = "";
		}

		ch.writeStartElement("rdfs:seeAlso");
		ch.writeAttribute("rdf:resource", channel);
		ch.writeEndElement();

		String desc = feed.getDescription();
		if (desc == null) {
			desc = feed.getTitle();
		}
		if (desc == null) {
			desc = "";
		}

		ch.writeStartElement("dcterms:description");
		ch.writeCharacters(stripHTML(desc));
		ch.writeEndElement();

		List li = feed.getEntries();
		for (int i=0; i<li.size(); i++) {
			SyndEntry sei = (SyndEntry)li.get(i);

			try {
				// check here if sei.getUri is a tag: uri, if so, try to find a http uri
				URI u = new URI("foo:bar");

				if (sei.getUri() != null) {
					u = new URI(sei.getUri());
				}

				if (!("http".equals(u.getScheme()) || "https".equals(u.getScheme()))) {
					if (sei.getLink() != null) {
						u = new URI(sei.getLink());
					} else {
						_log.info(sei.getUri() + " " + sei.getLink() + " wtf");
					}
				}					
				
				ch.writeStartElement("sioc:container_of");
				ch.writeAttribute("rdf:resource", u.toString());
				ch.writeEndElement();
			} catch (URISyntaxException e) {
				_log.info(sei.getUri() + " " + e.getMessage());
			} catch (NullPointerException e) {
				_log.info(sei.getUri() + " " + e.getMessage());
				e.printStackTrace();
			}
		}

		// Forum
		ch.writeEndElement();

		li = feed.getEntries();
		for (int i=0; i<li.size(); i++) {
			SyndEntry sei = (SyndEntry)li.get(i);

			Date d = null;

			if (sei.getPublishedDate() == null && sei.getUpdatedDate() == null) {
				continue;
			} else {
				if (sei.getPublishedDate() != null) {
					d = sei.getPublishedDate();
				} else {
					d = sei.getUpdatedDate();
				}
			}

			ch.writeStartElement("sioc:Post");

			try {
				URI u = new URI("foo:bar");

				if (sei.getUri() != null) {
					u = new URI(sei.getUri());
				}

				if (!("http".equals(u.getScheme()) || "https".equals(u.getScheme()))) {
					if (sei.getLink() != null) {
						u = new URI(sei.getLink());
					} else {
						_log.info(sei.getUri() + " " + sei.getLink() + " wtf");
					}
				}	

				ch.writeAttribute("rdf:about", u.toString());									

			} catch (URISyntaxException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			Calendar c = Calendar.getInstance();
			c.setTime(d);

			if (sei.getTitle() != null) {
				ch.writeStartElement("dcterms:title");
				ch.writeCharacters(stripHTML(sei.getTitle()));
				ch.writeEndElement();
			}

			if (sei.getLink() != null) {
				ch.writeStartElement("rdfs:seeAlso");
				ch.writeAttribute("rdf:resource", sei.getLink());
				ch.writeEndElement();
			}

			if (sei.getDescription() != null) {
				// @@@ here, tokenise the description into sentences and use max. 2 sentences as description
				if (striphtml) {
					ch.writeStartElement("sioc:content");
					ch.writeCharacters(stripHTML(sei.getDescription().getValue()));
					ch.writeEndElement();
				} else {
//					ch.writeStartElement("sioc:content");
//					ch.writeCharacters(sei.getDescription().getValue());		
//					ch.writeEndElement();

					ch.writeStartElement("sioc:content");
					ch.writeCharacters(stripHTML(sei.getDescription().getValue()));
					ch.writeEndElement();
				}
			}

			if (sei.getPublishedDate() != null) {				
				ch.writeStartElement("dcterms:date");
				//ch.writeAttribute("rdf:datatype", "http://www.w3.org/2001/XMLSchema#dateTime");
				ch.writeCharacters(RFC822DATEFORMAT.format(sei.getPublishedDate()));
				ch.writeEndElement();
			} else if (sei.getUpdatedDate() != null) {
				ch.writeStartElement("dcterms:date");
				//ch.writeAttribute("rdf:datatype", "http://www.w3.org/2001/XMLSchema#dateTime");
				ch.writeCharacters(RFC822DATEFORMAT.format(sei.getUpdatedDate()));
				ch.writeEndElement();
			}

			//			System.out.println(sei.getAuthors());

			try {
				if (sei.getCategories() != null) {
					List<SyndCategory> cats = sei.getCategories();
					for (SyndCategory cat : cats) {
						if (cat.getTaxonomyUri() != null) {
							ch.writeStartElement("sioc:topic");
							//ch.writeCharacters(cat.getTaxonomyUri());
							ch.writeAttribute("rdf:resource", cat.getTaxonomyUri());
						} else {
							ch.writeStartElement("dcterms:subject");
							ch.writeCharacters(stripHTML(cat.getName()));
						}
						ch.writeEndElement();
					}
				}
			} catch (ClassCastException e) {
				_log.info(e.getMessage());
				//e.printStackTrace();
			}

			try {
				if (sei.getAuthors() != null) {
					List<SyndPerson> auths = sei.getAuthors();
					for (SyndPerson auth : auths) {
						ch.writeStartElement("foaf:maker");
						ch.writeStartElement("foaf:Agent");
						if (auth.getUri() != null) {
							ch.writeAttribute("rdf:about", sei.getUri());
						}

						if (auth.getName() != null) {
							ch.writeStartElement("foaf:name");
							ch.writeCharacters(stripHTML(auth.getName()));
							ch.writeEndElement();
						}

						if (auth.getEmail() != null) {
							ch.writeStartElement("foaf:mbox");
							ch.writeAttribute("rdf:resource", "mailto:" + auth.getEmail().trim());
							ch.writeEndElement();
						}

						ch.writeEndElement();
						ch.writeEndElement();
					}
				}
			} catch (ClassCastException e) {
				_log.info(e.getMessage());
				//e.printStackTrace();
			}

			if (sei.getEnclosures() != null) {
				List<SyndEnclosure> lie = sei.getEnclosures();

				for (SyndEnclosure se : lie) {
					ch.writeStartElement("enc:enclosure");
					ch.writeAttribute("rdf:resource", se.getUrl());
					ch.writeAttribute("enc:length", Long.toString(se.getLength()));
					ch.writeAttribute("enc:type", se.getType());
					ch.writeEndElement();
				}
			}

			ch.writeEndElement();
		}

		ch.writeEndElement();

		ch.writeEndDocument();

		ch.close();
	}

	public static String stripHTML(String s) {
		return s.replaceAll("\n", " ").replaceAll("\\<.*?\\>","").trim();			
	}	
}
