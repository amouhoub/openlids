/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.util.Set;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Node;
import org.openlids.model.data.LIDSQueryEngine;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ssp
 */
public class TestLIDSQuerying {

    static String prefixes = "PREFIX foaf: <http://xmlns.com/foaf/0.1/> "
            + "PREFIX dc: <http://purl.org/dc/elements/1.1/> "
            + "PREFIX geo: <http://www.w3.org/2003/01/geo/wgs84_pos#> "
            + "PREFIX owl: <http://www.w3.org/2002/07/owl#> "
            + "PREFIX v: <http://www.w3.org/2006/vcard/ns#> "
            + "PREFIX og: <http://ogp.me/ns#> ";

    static String baseServer = "http://km.aifb.kit.edu/services/";


    // @@@ broken not LIDS conform!
/*    static String findNearbyLIDS = prefixes + "CONSTRUCT { ?point foaf:based_near ?p } "
            + "FROM <" + baseServer + "geonameswrap/findNearby> "
            + "WHERE { ?point geo:lat ?lat . ?point geo:long ?lng }";

    static String alsoNearbyLIDS = prefixes + "CONSTRUCT { ?spatial_entity foaf:based_near ?p } "
            + "FROM <" + baseServer + "alsoNearby> "
            + "WHERE { ?spatial_entity foaf:based_near ?point . ?point geo:lat ?lat . ?point geo:long ?lng }";
*/
    static String geocodeLIDS = prefixes + "CONSTRUCT { ?entity foaf:based_near ?p . ?p geo:lat ?lat . ?p geo:long ?lng } "
            +
	  									   "FROM <" + baseServer + "geocode> " +
	  									   "WHERE { ?entity v:adr ?adr . ?adr v:street-address ?street . ?adr v:locality ?town . ?adr v:country-name ?country }";

    static String facebookLIDS = prefixes + "CONSTRUCT { ?thing foaf:name ?name } "
            + "FROM <" + baseServer + "facewrap/thing> "
            + "WHERE { ?thing og:id ?facebookid } ";

/*    static String dbpediaStudNRLIDS = prefixes + "CONSTRUCT { ?university <http://dbpedia.org/ontology/numberOfStudents> ?n } "
            + "FROM <" + baseServer + "dbpediaStudNR> "
            + "WHERE { ?university foaf:homepage ?homepage } ";
*/


    public TestLIDSQuerying() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void queryFacebook() {
        String queryStr = prefixes + "SELECT ?name ?likes WHERE { <http://openlids.org/universities.rdf#list> foaf:topic ?u . ?u foaf:name ?name . ?u og:likes ?likes }";
        LIDSQueryEngine lidsqe = new LIDSQueryEngine();
        lidsqe.addLIDS(facebookLIDS);
        Set<Node[]> results = lidsqe.execQuery(queryStr);
        for(Node[] r : results) {
            System.out.println(Nodes.toN3(r));
        }
    }


}