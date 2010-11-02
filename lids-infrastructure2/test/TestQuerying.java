/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import org.semanticweb.yars.nx.Nodes;
import org.openlids.model.data.TripleAddObserver;
import java.util.Iterator;
import org.semanticweb.yars.nx.Node;
import org.openlids.model.data.Query;
import org.openlids.model.data.jena.QueryEngineJena;
import org.openlids.model.data.QueryEngine;
import org.openlids.model.data.impl.DataSetNxRetrieve;
import org.openlids.model.data.DataSet;
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
public class TestQuerying {

    public TestQuerying() {
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

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}

    @Test
    public void querySSPFoaf() {
        DataSet dataSet = new DataSetNxRetrieve();
        QueryEngine qe = new QueryEngineJena(dataSet);
        Query q = qe.createQuery("PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
                + "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
                + "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n"
                + "PREFIX sioc: <http://rdfs.org/sioc/ns#> \n"
                + "PREFIX foaf: <http://xmlns.com/foaf/0.1/> \n"
                + "PREFIX dc: <http://purl.org/dc/elements/1.1/> \n"
                + "PREFIX skos: <http://www.w3.org/2004/02/skos/core#> \n"
                + "PREFIX vcard: <http://www.w3.org/2001/vcard-rdf/3.0#> \n"
                + "PREFIX rss: <http://purl.org/rss/1.0/>\n"
                + "\n"
                + "SELECT DISTINCT ?name\n"
                + "WHERE { \n"
                + "	<http://speiserweb.de/sebastian/foaf.rdf#me> foaf:knows ?friendURI . ?friendURI foaf:name ?name \n"
                + "}\n"
                + "LIMIT 200");
        dataSet.addObserver(new TripleAddObserver() {
            @Override
            public void notifyAddTriple(DataSet dataSet, Node[] nx) {
                System.out.println("bb: " + Nodes.toN3(nx));
            }
        });
        Iterator<Node[]> results = qe.execQuery(q);
        for(Node[] res = results.next();results.hasNext();res = results.next()) {
            System.out.println(res[0]);
        }
    }
}