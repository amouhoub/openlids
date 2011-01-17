/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openlids.linking.qp;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openlids.model.LIDSDescription;
import org.openlids.model.parser.LIDSParser;
import org.openlids.model.parser.arq.LIDSParserARQ;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

/**
 *
 * @author ssp
 */
public class LIDSPNodeConstruct extends PNode {

    int descfield = 0;
    final Rete rete;
    final Manager manager;
    LIDSParser lidsParser = new LIDSParserARQ();
//    List<LIDSDescription> lidsDescriptions = new ArrayList<LIDSDescription>();


    public LIDSPNodeConstruct(Rete rete, Manager manager) {
        this.rete = rete;
        this.manager = manager;
    }

    @Override
    public void leftActivation(Node[] token) {
        System.out.println("LIDSDesc:\n" + token[descfield] + "---");
        final LIDSDescription lids = lidsParser.parseLIDSDescription(token[descfield].toString());
        System.out.println("Discovered Service at endpoint: " + lids.getEndpoint());
        List<Node[]> patterns = new LinkedList<Node[]>();
        patterns.addAll(lids.getInputBGP());
        rete.addProduction(patterns, new PNode() {
            @Override
            public void leftActivation(Node[] token) {
                Map<Variable, Node> varToNode = new HashMap<Variable, Node>();
                int i = 0;
                for(Node f : fields) {
                    if(f instanceof Variable) {
                        varToNode.put((Variable) f, token[i]);
                    }
                    i++;
                }
                String service_call = lids.makeURI(varToNode);
                System.out.println("Service Call: " + service_call);
                manager.addURI(service_call);
//                try {
//                    URL url = new URL(service_call);
//                    InputStream is = url.openStream();
//                    Callback cb = new Callback() {
//                        @Override
//                        public void endDocument() {
//                        }
//                        @Override
//                        public void processStatement(Node[] nx) {
//                            rete.addTriple(new Node[]{nx[0], nx[1], nx[2]});
//                        }
//                        @Override
//                        public void startDocument() {
//                        }
//                    };
//
//                    RDFXMLParser rdfxml = new RDFXMLParser(is, true, true, url.toString(), cb);
//
//                } catch (Exception e) {
//                    System.err.println("Error during parsing:");
//                }
            }
        });
    }

  

    @Override
    public void setFields(Node[] fields) {
        this.fields = fields;
        int i = 0;
        for (Node field : fields) {
            if (field.equals(new Variable("desc"))) {
                descfield = i;
                break;
            }
            i++;
        }
    }
}
