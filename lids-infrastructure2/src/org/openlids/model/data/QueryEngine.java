/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Variable;

/**
 *
 * @author ssp
 */
public abstract class QueryEngine {
    private final DataSet dataSet;

    public QueryEngine(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public abstract Query createQuery(List<Node> head, Set<Node[]> body);
    public abstract Query createQuery(String sparqlString);
    public abstract Iterator<Map<Variable,Node>> execQuery(Query query);
}
