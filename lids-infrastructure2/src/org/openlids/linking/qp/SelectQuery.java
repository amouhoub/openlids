/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.openlids.util.Utils;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Variable;
import org.semanticweb.yars.nx.parser.ParseException;

/**
 *
 * @author ssp
 */
public class SelectQuery {
    private List<Node> _hvs;
    private List<Node[]> _patterns;

    private int[] _hvs_pos;

    private Rete _rete;

    private List<Node[]> _results = new ArrayList<Node[]>();

    QueryDisplayingForm _qdf;

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

        _qdf = new QueryDisplayingForm(this);
        _qdf.setVisible(true);
    }

    public void setRete(Rete rete) {
        _rete = rete;
        _rete.addProduction(_patterns, new PNode() {
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
                _qdf.updateTableStructure();
            }
        });
    }

    public void addResult(Node[] result) {
        _results.add(result);
        _qdf.updateTableResults();

    }

    public int getNHVs() {
        return _hvs.size();
    }

    public int getNResults() {
        return _results.size();
    }

    public Object getResult(int row, int col) {
        if(row == 0) {
            return _hvs.get(col).toString();
        }
        return _results.get(row-1)[col];
    }

    public List<Node[]> getPatterns() {
        return _patterns;
    }


}
