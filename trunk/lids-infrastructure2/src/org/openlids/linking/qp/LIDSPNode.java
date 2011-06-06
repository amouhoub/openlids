/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openlids.linking.qp;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.openlids.util.Utils;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

/**
 *
 * @author ssp
 */
public class LIDSPNode extends PNode {

    Logger _log = Logger.getLogger(this.getClass().getName());

    private int _descfield = 0;
    private int _servicefield = 0;
    private int _entityfield = 0;
    private int _ibgpfield = 0;
    private int _obgpfield = 0;
    private int _reqvarsfield = 0;
    private int _epfield = 0;

    final Rete _rete;
    final Manager _manager;

    //    List<LIDSDescription> lidsDescriptions = new ArrayList<LIDSDescription>();

    final static String LIDS_NS = "http://openlids.org/vocab#";

    public final static List<Node[]> PATTERNS = Utils.parseNxSetNoException(
//              "?service <" + LIDS_NS + "lids_description> ?desc . "
//            + "?desc <" + LIDS_NS + "endpoint> ?endpoint . "
//            + "?desc <" + LIDS_NS + "service_entity> ?service_entity . "
//            + "?desc <" + LIDS_NS + "input_bgp> ?ibgp . "
//            + "?desc <" + LIDS_NS + "output_bgp> ?obgp . "
//            + "?desc <" + LIDS_NS + "required_vars> ?reqvars . ");
//    Utils.parseNxSetNoException (
              "?service lids:lids_description ?desc . "
            + "?desc lids:endpoint ?endpoint . "
            + "?desc lids:service_entity ?service_entity . "
            + "?desc lids:input_bgp ?ibgp . "
            + "?desc lids:output_bgp ?obgp . "
            + "?desc lids:required_vars ?reqvars . ");


    public LIDSPNode(Rete rete, Manager manager) {
        this._rete = rete;
        this._manager = manager;
    }

    @Override
    public void leftActivation(Node[] token) {

        Node service = token[_servicefield];
        Node desc = token[_descfield];
        Node ep = token[_epfield];
        String ibgp = token[_ibgpfield].toString();
        String obgp = token[_obgpfield].toString();
        String entity = token[_entityfield].toString().replaceAll("\\?", "");
        String reqvars = token[_reqvarsfield].toString().replaceAll(" ", "").replaceAll("\\?", "");

        _log.info("Found LIDS Description: " + desc.toN3() + "\n"
                + "Service: " + service.toN3() + "\n"
                + "Endpoint: " + ep.toN3() + "\n"
                + "Entity: " + entity + "\n"
                + "iBGP: " + ibgp + "\n"
                + "oBGP: " + obgp + "\n"
                + "ReqVars: " + reqvars + "\n");


        final LIDSDescription lids = new LIDSDescription();
        try {
            lids.setEndpoint(new URI(ep.toString()));
        } catch (URISyntaxException ex) {
            _log.log(Level.SEVERE, null, ex);
        }
        lids.setInputEntity(new Variable(entity));
        lids.setInputBGP(Utils.parseNxSetNoException(ibgp));
        lids.setOutputBGP(Utils.parseNxSetNoException(obgp));
        for(String var : reqvars.split(",")) {
            lids.addRequiredVar(new Variable(var.trim()));
        }
        lids.setChanged(false);

        List<Node[]> patterns = new LinkedList<Node[]>();
        patterns.addAll(lids.getInputBGP());



        lids.setChanged(false);
/*
        @@@@
        Either make LIDSCallPNode Class or move to Manager
                However Manager should have special calls for Service Calls compared to normal URI retrievals
  */

        _rete.addProduction(patterns, new PNode() {
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
                for (Variable v : lids.getRequiredVars()) {
                    if (varToNode.get(v) instanceof BNode)
                        return;
                }

                String service_call = lids.makeURI(varToNode);
                System.out.println("Service Call: " + service_call);
                if(varToNode.containsKey(lids.getInputEntity())) {
                    _manager.addTriple(new Node[]{varToNode.get(lids.getInputEntity()), NS.OWL_SAMEAS, new Resource(service_call)});
                }
                _manager.addURI(service_call);

            }
        });
    }

  

    @Override
    public void setFields(Node[] fields) {
        this.fields = fields;
        int i = 0;

        Variable vdesc = new Variable("desc");
        Variable vservice = new Variable("service");
        Variable ventity = new Variable("service_entity");
        Variable vreqvars = new Variable("reqvars");
        Variable vibgp = new Variable("ibgp");
        Variable vobgp = new Variable("obgp");
        Variable vep = new Variable("endpoint");

        for (Node field : fields) {
            if (field.equals(vdesc)) {
                _descfield = i;
            } else if(field.equals(vservice)) {
                _servicefield = i;
            } else if(field.equals(ventity)) {
                _entityfield = i;
            } else if(field.equals(vreqvars)) {
                _reqvarsfield = i;
            } else if(field.equals(vibgp)) {
                _ibgpfield = i;
            } else if(field.equals(vobgp)) {
                _obgpfield = i;
            } else if(field.equals(vep)) {
                _epfield = i;
            }
            i++;
        }
    }
}
