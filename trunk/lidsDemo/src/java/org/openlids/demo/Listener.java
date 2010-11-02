/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openlids.demo;

import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.openlids.model.LIDSDescription;
import org.openlids.model.parser.LIDSParser;
import org.openlids.model.parser.arq.LIDSParserARQ;

/**
 * Web application lifecycle listener.
 * @author ssp
 */
public class Listener implements ServletContextListener {

    static String prefixes = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
            + "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
            + "PREFIX v: <http://www.w3.org/2006/vcard/ns#> "
            + "PREFIX og: <http://ogp.me/ns#> ";
    static String baseServer = "http://localhost:8889/";
//	static String findNearbyLIDS = prefixes + "CONSTRUCT { ?point foaf:based_near ?p } " +
//	"FROM <" + baseServer + "findNearby> " +
//	"WHERE { ?point geo:lat ?lat . ?point geo:long ?lng }";
    static String alsoNearbyLIDS = prefixes + "CONSTRUCT { ?spatial_entity foaf:based_near ?p } "
            + "FROM <" + baseServer + "alsoNearby> "
            + "WHERE { ?spatial_entity foaf:based_near ?point . ?point geo:lat ?lat . ?point geo:long ?lng }";
    static String geocodeLIDS = prefixes + "CONSTRUCT { ?entity foaf:based_near ?p . ?p geo:lat ?lat . ?p geo:long ?lng } "
            + "FROM <" + baseServer + "geocode> "
            + "WHERE { ?entity v:adr ?adr . ?adr v:street-address ?street . ?adr v:locality ?town . ?adr v:country-name ?country }";
    static String facebookLIDS = prefixes + "CONSTRUCT { ?page foaf:name ?name } "
            + "FROM <" + "http://localhost:8888/" + "facewrap> "
            + "WHERE { ?page og:id ?facebookid } ";
    static String dbpediaStudNRLIDS2 = prefixes + "CONSTRUCT { ?university <http://dbpedia.org/ontology/numberOfStudents> ?n } "
            + "FROM <http://dbpwrap.openlids.org/DBPediaStudNR> "
            + "WHERE { ?person foaf:schoolHomepage ?university } ";
//	static String dbpediaStudNRLIDS = prefixes + "CONSTRUCT { ?university <http://dbpedia.org/ontology/numberOfStudents> ?n } " +
//	"FROM <http://dbpwrap.openlids.org/DBPediaStudNR> " +
//	"WHERE { ?university foaf:homepage ?homepage } ";
    static String dbpediaHPSearch = prefixes + "CONSTRUCT { ?entity owl:sameAs ?dbpedia_resource } "
            + "FROM <http://localhost:8887/DBPediaHPSearch> "
            + "WHERE { ?entity foaf:homepage ?homepage } ";
    static String feedwrapLIDS = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
            + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
            + "PREFIX foaf: <http://xmlns.com/foaf/0.1/>\n"
            + "PREFIX sioc: <http://rdfs.org/sioc/ns#>\n"
            + "PREFIX log: <http://www.w3.org/2000/10/swap/log#>\n"
            + "CONSTRUCT { ?forum rdf:type sioc:Forum . } FROM <http://feedwrap.openlids.org/feedwrap> WHERE {\n"
            + "?agent foaf:weblog ?forum . ?forum log:uri ?uri . }";

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();

        Set<LIDSDescription> lidsList = new HashSet<LIDSDescription>();
        System.out.println("d1");
        LIDSParser sp = new LIDSParserARQ();
//		lidsList.add(sp.parseServiceDescription(findNearbyLIDS));
        System.out.println("d2");
        lidsList.add(sp.parseLIDSDescription(alsoNearbyLIDS));
        System.out.println("d3");
        lidsList.add(sp.parseLIDSDescription(geocodeLIDS));
        lidsList.add(sp.parseLIDSDescription(facebookLIDS));
        lidsList.add(sp.parseLIDSDescription(dbpediaHPSearch));
        lidsList.add(sp.parseLIDSDescription(dbpediaStudNRLIDS2));
        lidsList.add(sp.parseLIDSDescription(feedwrapLIDS));
        ctx.setAttribute("lidsList", lidsList);
    }

    public void contextDestroyed(ServletContextEvent sce) {
    }
}
