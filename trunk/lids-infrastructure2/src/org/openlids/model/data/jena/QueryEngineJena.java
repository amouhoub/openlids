/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data.jena;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.openlids.model.data.DataSet;
import org.openlids.model.data.Query;
import org.openlids.model.data.QueryEngine;
import org.openlids.model.parser.arq.JenaToNx;
import org.openlids.model.parser.arq.NxToJena;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public class QueryEngineJena extends QueryEngine {

    DataSetGraph _dsgraph;
    private final Model _model;

    public QueryEngineJena(DataSet dataSet) {
        super(dataSet);
        _dsgraph = new DataSetGraph(dataSet);
        _model = ModelFactory.createModelForGraph(_dsgraph);
    }

    @Override
    public Query createQuery(List<Node> head, Set<Node[]> body) {
        QueryJena query = new QueryJena();

        com.hp.hpl.jena.query.Query jenaQuery = new com.hp.hpl.jena.query.Query();
        for(Node hv : head) {
            if(!(hv instanceof Variable))
                continue;
            com.hp.hpl.jena.graph.Node v = NxToJena.convert(hv);
            query.addMap(v,(Variable) hv);
            jenaQuery.addResultVar(v);
        }
        query.setHeadVars(head);
        jenaQuery.setQuerySelectType();
        jenaQuery.setDistinct(true);
        ElementTriplesBlock elt = new ElementTriplesBlock();

        for(Node[] bgp : body) {
            elt.addTriple(NxToJena.convert(bgp));
        }
        jenaQuery.setQueryPattern(elt);

        query.setJenaQuery(jenaQuery);

        return query;
    }

    @Override
    public Query createQuery(String sparqlStr) {
        QueryJena query = new QueryJena();
        query.setJenaQuery(QueryFactory.create(sparqlStr));
        return query;
    }

    @Override
    public Iterator<Map<Variable,Node>> execQuery(Query queryIn) {
        if(!(queryIn instanceof QueryJena)) {
            return new Iterator<Map<Variable,Node>>() {
                public boolean hasNext() {
                   return false;
                }
                public Map<Variable,Node> next() {
                    return null;
                }

                public void remove() {
                }
            };
        }
        final QueryJena query = (QueryJena) queryIn;

        final QueryExecution qexec = QueryExecutionFactory.create(query.getJenaQuery(), _model);

        final ResultSet results = qexec.execSelect();

        final List<Node> headvars = query.getHeadVars();

        return new Iterator<Map<Variable,Node>>() {

            public boolean hasNext() {
                return results.hasNext();
            }

            public Map<Variable,Node> next() {
                Binding qb = results.nextBinding();
                Iterator<Var> it = qb.vars();
                Map<Variable,Node> map = new HashMap<Variable,Node>();
                while(it.hasNext()) {
                    Var v = it.next();
                    map.put(query.getMapping(v), JenaToNx.convert(qb.get(v)));
                }
                if (!results.hasNext()) {
                    qexec.close();
                }
          
                return map;
//                Node[] binding = new Node[headvars.size()];
  //              int i = 0;
    //            for(Node hv : headvars) {
      //              binding[i] = (Node) JenaToNx.convert(qb.get((Var) query.getMapping(hv)));
        //        }
//                int i = 0;
//                Iterator<Var> it = qb.vars();
//                while(it.hasNext()) {
//                    Var v = it.next();
//                    binding[i] = JenaToNx.convert(qb.get(v));
//                    i++;
//                }
          //      return binding;
            }

            public void remove() {
                results.remove();
            }

        };
    }

}