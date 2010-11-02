/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class LIDSDescription {

    URI endpoint;

    boolean changed = true;

    Variable input_entity;
    Set<Node[]> input;
    Set<Node[]> output;
    List<Variable> required_vars;

    public LIDSDescription() {
        input = new TreeSet<Node[]>(NodeComparator.NC);
        output = new TreeSet<Node[]>(NodeComparator.NC);
    }

    public void addInputBGP(Node[] node) {
        changed |= !input.contains(node);
        input.add(node);
    }

    public void setEndpoint(URI endpoint) {
        changed |= (endpoint != this.endpoint);
        this.endpoint = endpoint;
    }

    public URI getEndpoint() {
        return endpoint;
    }

    public void addOutputBGP(Node[] node) {
        changed |= !output.contains(node);
        output.add(node);
    }

    public Set<Node[]> getInputBGP() {
        return input;
    }

    public Set<Node[]> getOutputBGP() {
        return output;
    }

    public Variable getInputEntity() {
        analyze();
        return input_entity;
    }

    public List<Variable> getRequiredVars() {
        analyze();
        return required_vars;
    }

    public void setInputEntity(Variable input_entity) {
        changed |= (input_entity != this.input_entity);
        this.input_entity = input_entity;
    }

//    public String makeURI(Node[] bindings) {
//        analyze();
//        Map<Variable,Node> bmap = new HashMap<Variable,Node>();
//        int i = 0;
//        for(Variable v : required_vars) {
//            bmap.put(v,bindings[i++]);
//        }
//        return makeURI(bmap);
//    }

    public String makeURI(Map<Variable,Node> bindings) {
        analyze();
        if (!bindings.keySet().containsAll(getRequiredVars())) {
            return null;
            // not enough vars bound
        }
        String uri = getEndpoint().toString();
        try {
       //     if (getRequiredVars().size() == 1) {
       //         uri += "/" + URLEncoder.encode(bindings.get(getRequiredVars().iterator().next()).toString(), "UTF-8");
       //     } else {
                uri += "?";
                for (Node v : getRequiredVars()) {
                    uri += URLEncoder.encode(v.toString(), "UTF-8") + "=" + URLEncoder.encode(bindings.get(v).toString(), "UTF-8") + "&";
                }
                uri = uri.substring(0, uri.length() - 1);
       //     }
        } catch (UnsupportedEncodingException e) {

        }
        uri += "#" + this.getInputEntity().toString();
        return uri;
    }

    void analyze() {
        if (!changed) {
            return;
        }

        Set<Variable> input_vars = new TreeSet<Variable>();

        TreeSet<Variable> required_vars_set = new TreeSet<Variable>();
        required_vars = new  ArrayList<Variable>();

    	for(Node[] bgpI : input) {
            if (bgpI[2] instanceof Variable) {
                input_vars.add((Variable) bgpI[2]);
                required_vars.add((Variable) bgpI[2]);
            }
        }
        for (Node[] bgpI : input) {
            if (bgpI[0] instanceof Variable) {
                input_vars.add((Variable) bgpI[0]);
                required_vars.remove((Variable) bgpI[0]);
            }
    	}

        for(Variable v : required_vars_set) {
            required_vars.add(v);
        }

    	// variables.addAll(inputVars);
    	for(Node[] bgpO : output) {
            if (bgpO[0] instanceof Variable) {
                if (input_vars.contains((Variable) bgpO[0])) {
                    input_entity = (Variable) bgpO[0];
                }
            }
            if (bgpO[2] instanceof Variable) {
                if (input_vars.contains((Variable) bgpO[2])) {
                    input_entity = (Variable) bgpO[2];
                }
            }
        }
        changed = false;
    }



}
