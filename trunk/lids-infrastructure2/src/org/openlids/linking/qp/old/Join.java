/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openlids.linking.qp.old;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.semanticweb.yars.nx.Node;

/**
 *
 * @author ssp
 */
public class Join {

    enum Position {
        LEFT, RIGHT
    };
    int left_join_pos = 0;
    int right_join_pos = 0;
    Map<Node, List<Node[]>> left_nodes = new HashMap<Node, List<Node[]>>();
    Map<Node, List<Node[]>> right_nodes = new HashMap<Node, List<Node[]>>();
    List<Consumer> consumers = new LinkedList<Consumer>();

    public void consume(Node[] tuple, Position position) {
        if (position == Position.LEFT) {
            List<Node[]> lefts = left_nodes.get(tuple[left_join_pos]);
            if(lefts == null) {
                lefts = new LinkedList<Node[]>();
                left_nodes.put(tuple[left_join_pos], lefts);
            }
            List<Node[]> rights = right_nodes.get(tuple[left_join_pos]);
            if(rights != null) {
                for(Node[] right : rights) {
                    Node[] joined = new Node[tuple.length + right.length];
                    int i = 0;
                    for(Node l : tuple) {
                        joined[i] = l;
                        i++;
                    }
                    for(Node r : right) {
                        joined[i] = r;
                        i++;
                    }
                    for(Consumer consumer : consumers) {
                        consumer.consume(joined);
                    }
                }
            }
        } else if (position == Position.RIGHT) {
            List<Node[]> rights = right_nodes.get(tuple[right_join_pos]);
            if(rights == null) {
                rights = new LinkedList<Node[]>();
                right_nodes.put(tuple[right_join_pos], rights);
            }
            List<Node[]> lefts = left_nodes.get(tuple[right_join_pos]);
            if(lefts != null) {
                for(Node[] left : lefts) {
                    Node[] joined = new Node[tuple.length + left.length];
                    int i = 0;
                    for(Node l : left) {
                        joined[i] = l;
                        i++;
                    }
                    for(Node r : tuple) {
                        joined[i] = r;
                        i++;
                    }
                    for(Consumer consumer : consumers) {
                        consumer.consume(joined);
                    }
                }
            }

        }
    }

    public Consumer getLeftConsumer() {
        return new Consumer() {

            @Override
            public void consume(Node[] tuple) {
                consume(tuple, Position.LEFT);
            }
        };
    }

    public Consumer getRightConsumer() {
        return new Consumer() {

            @Override
            public void consume(Node[] tuple) {
                consume(tuple, Position.RIGHT);
            }
        };
    }

    public void setLeft_join_pos(int left_join_pos) {
        this.left_join_pos = left_join_pos;
    }

    public void setRight_join_pos(int right_join_pos) {
        this.right_join_pos = right_join_pos;
    }

    public void addConsumer(Consumer consumer) {
        consumers.add(consumer);
    }
}
