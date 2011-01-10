/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openlids.model.LIDSDescription;
import org.openlids.model.data.DataSet;
import org.openlids.model.data.Query;
import org.openlids.model.data.QueryEngine;
import org.openlids.model.data.jena.QueryEngineJena;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class Annotator {

    static final Resource OWL_SAME_AS = new Resource("http://www.w3.org/2002/07/owl#sameAs");

    Set<LIDSDescription> lidsList = new HashSet<LIDSDescription>();

    public void addLIDS(LIDSDescription e) {
         lidsList.add(e);
    }


    public Set<Node[]> annotate(DataSet dataSet) {
        Set<Node[]> newSameAsEs = new TreeSet<Node[]>(NodeComparator.NC);
        QueryEngine qe = new QueryEngineJena(dataSet);

        Map<LIDSDescription,Query> queries = new HashMap<LIDSDescription,Query>();

        for(LIDSDescription lids : lidsList) {
            List<Node> vars = new ArrayList<Node>();
            vars.add(lids.getInputEntity());
            vars.addAll(lids.getRequiredVars());
            queries.put(lids, qe.createQuery(vars, lids.getInputBGP()));
        }

        for(LIDSDescription lids : lidsList) {
            Query q = queries.get(lids);
//            System.out.println(q);
            Iterator<Map<Variable,Node>> results = qe.execQuery(q);
            while(results.hasNext()) {
                Map<Variable,Node> result = results.next();
//                Node[] bindings;
  //              bindings = new Node[result.length - 1];
    //            for (int i = 0; i < bindings.length; i++) {
      //              bindings[i] = result[i + 1];
        //        }
               
                newSameAsEs.add(new Node[]{ 
                            result.get(lids.getInputEntity()), //[0],
                            OWL_SAME_AS,
          //                  new Resource(lids.makeURI(bindings))
                            new Resource(lids.makeURI(result))
                        });
            }
        }
        
        return newSameAsEs;
    }



}
