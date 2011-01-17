/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.qp.test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openlids.linking.qp.LIDSPNode;
import org.openlids.linking.qp.Manager;
import org.openlids.linking.qp.PNode;
import org.openlids.linking.qp.Rete;
import org.openlids.linking.qp.ReteNode;
import org.openlids.linking.qp.Retriever;
import org.openlids.linking.qp.SelectQuery;
import org.openlids.util.Utils;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 *
 * @author ssp
 */
public class LIDSQueryingTest {

    public LIDSQueryingTest() {
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
    public void testFileFacewrapDiscovery() {
        Retriever retr = new Retriever();

//        String start_uri = "file://" + new File("files/test.rdf").getAbsolutePath();
        String start_uri = "http://openlids.org/test.rdf";


        retr.addURI(start_uri);

        SelectQuery sq1,sq2,sq3;
        try {
            sq1 = SelectQuery.parse("SELECT ?n1 ?n2 WHERE { ?p1 foaf:knows ?p2 . ?p1 foaf:name ?n1 . ?p2 foaf:name ?n2 }");
            sq2 = SelectQuery.parse("SELECT ?n1 ?n2 WHERE { ?n1 foaf:name ?n2 }");
            sq3 = SelectQuery.parse("SELECT ?p1 ?n2 WHERE { ?p1 foaf:name \"Sebastian Speiser\" . ?p1 foaf:knows ?p2 . ?p2 foaf:name ?n2 ");
            retr.addQuery(sq1);
            retr.addQuery(sq2);
            retr.addQuery(sq3);
        } catch (ParseException ex) {
            Logger.getLogger(LIDSQueryingTest.class.getName()).log(Level.SEVERE, null, ex);
        }
   
        List<Node[]> foafNRule = new LinkedList<Node[]>();
        foafNRule.add(new Node[]{new Variable("x"), new Resource("http://xmlns.com/foaf/0.1/name"), new Variable("name")});
        retr._rete.addProduction(foafNRule, new PNode() {
            @Override
            public void leftActivation(Node[] token) {
                System.out.println("Foaf: " + Nodes.toN3(token));
            }
        });


        try {
            Thread.sleep(6000000);
        } catch (InterruptedException ex) {
            Logger.getLogger(LIDSQueryingTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("OKAY");
    }

}