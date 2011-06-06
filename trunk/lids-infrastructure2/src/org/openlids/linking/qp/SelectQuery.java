/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.openlids.util.Utils;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Nodes;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 *
 * @author ssp
 */
public class SelectQuery {
    private List<Node> _hvs;
    private List<Node[]> _patterns;

    private Set<Node> _constants;

    private int[] _hvs_pos;

    private org.openlids.qp.rete.Rete _rete;

//    public List<Node[]> _results = Collections.synchronizedList(new ArrayList<Node[]>());
    public Set<Node[]> _results = Collections.synchronizedSet(new TreeSet<Node[]>(NodeComparator.NC));

//    QueryDisplayingForm _qdf;

    public static SelectQuery parse(String line) throws ParseException {
        List<Node> hvs = new LinkedList<Node>();
        List<Node[]> patterns = null;


        String value = line.trim();
        if (value.endsWith("}")) {
            value = value.substring(0,value.length()-1);
        }

        if(value.toUpperCase().startsWith("SELECT")) {
            value = value.substring(6).trim();
            int firstIndex = 0;
            int lastIndex = 0;
            while (true) {
                value = value.substring(firstIndex).trim();
                if(value.length() <= 0) {
                    break;
                }
                if(value.startsWith("WHERE")) {
                    break;
                }
                if (value.startsWith("?")) {
                    // variable
                    int pos_blank = value.indexOf(" ");
                    int pos_dot = value.indexOf(".");
                    int pos_sem = value.indexOf(";");
                    if (pos_dot >= 0) {
                        pos_blank = Math.min(pos_blank, pos_dot);
                    }
                    if (pos_sem >= 0) {
                        pos_blank = Math.min(pos_blank, pos_sem);
                    }
                    lastIndex = pos_blank;
                    hvs.add(new Variable(value.substring(1, lastIndex)));
                } else {
                    throw new ParseException("Only variables in SELECT Head allowed.");
                }
                firstIndex = lastIndex + 1;
            }
            if(value.startsWith("WHERE")) {
                value = value.substring(5).trim();
                if(value.startsWith("{")) {
                    value = value.substring(1).trim();
                }
                patterns = Utils.parseNxSet(value);
            } else {
                throw new ParseException("Expected WHERE Clause");
            }
        } else {
            throw new ParseException("Select query should start with SELECT");
        }

        SelectQuery sq = new SelectQuery(hvs, patterns);
        return sq;
    }

    public SelectQuery(List<Node> hvs, List<Node[]> patterns) {
        _hvs = hvs;
        _patterns = patterns;

        _constants = new HashSet<Node>();
        for(Node[] p : patterns) {
            if(p[0] instanceof Resource) {
                _constants.add(p[0]);
            }
            if(!p[1].equals(NS.RDF_TYPE)) {
                if(p[2] instanceof Resource) {
                    _constants.add(p[2]);
                }
            }
        }

//        _qdf = new QueryDisplayingForm(this);
//        _qdf.setVisible(true);
    }

    public boolean containsConstant(Node n) {
        return _constants.contains(n);
    }

    public void setRete(org.openlids.qp.rete.Rete rete) {
        _rete = rete;
        _rete._anet.addQuery(this);

        _rete.addProduction(_patterns, new org.openlids.qp.rete.PNode() {
            @Override
            public void leftActivation(Node[] token) {
                Node[] result = new Node[_hvs.size()];
                int i = 0;
                for(int pos : _hvs_pos) {
                    result[i] = token[pos];
                    i++;
                }
                addResult(result);
            }

            @Override
            public void setFields(Node[] fields) {
                _hvs_pos = new int[_hvs.size()];
                int fpos = 0;
                for(Node f : fields) {
                    if(f instanceof Variable) {
                        int i = 0;
                        for(Node hv : _hvs) {
                            if (f.equals(hv)) {
                                _hvs_pos[i] = fpos;
                            }
                            i++;
                        }
                    }
                    fpos++;
                }
//                _qdf.updateTableStructure();
            }
        }, this);
    }

    public void addResult(Node[] result) {
        synchronized (_results) {
            _results.add(result);
        }
        System.out.println("QRes: " + Nodes.toN3(result));
//        _qdf.updateTableResults();

    }

    public int getNHVs() {
        return _hvs.size();
    }

    public int getNResults() {
        int nresults = 0;
        synchronized(_results) {
            nresults = _results.size();
        }
        return nresults;
    }
/*
    public Object getResult(int row, int col) {
        if(row == 0) {
            return _hvs.get(col).toString();
        }
        Object o = null;
        synchronized(_results) {
            o = _results.get(row - 1)[col];
        }
        return o;
    }
*/
    public List<Node[]> getPatterns() {
        return _patterns;
    }


}
