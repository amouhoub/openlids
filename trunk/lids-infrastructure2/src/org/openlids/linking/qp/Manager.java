/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

/**
 *
 * @author ssp
 */
public class Manager {
    final Rete rete;

    Set<String> retrieved_urls = new HashSet<String>();

    final Queue<String> uris = new LinkedList<String>();

    final Queue<Thread> free_loader = new LinkedList<Thread>();
    final Set<Thread> used_loader = new HashSet<Thread>();

    public Manager(Rete rete) {
        this.rete = rete;

        final Manager manager = this;
        Thread loader = new Thread() {
            @Override
            public void run() {
                while (true) {
                    final String uri = manager.pullURI();
                    if(uri != null) {
                        Thread uriloader = new Thread() {
                            @Override
                            public void run() {
                                Callback cb = new Callback() {
                                    @Override
                                    public void endDocument() {
                                    }
                                    @Override
                                    public void processStatement(Node[] nx) {
                                        manager.rete.addTriple(new Node[]{nx[0], nx[1], nx[2]});
                                    }
                                    @Override
                                    public void startDocument() {
                                    }
                                };


                                try {
                                    URL url = new URL(uri);
                                    InputStream is = url.openStream();
                                    RDFXMLParser rdfxml = new RDFXMLParser(is, true, true, url.toString(), cb);
                                } catch (Exception e) {
                                    System.err.println("Error during parsing: " + uri);
                                }
                            }
                        };
                        uriloader.start();
                    }
                    try {
                        Thread.sleep(250);
                    } catch(Exception ex) {

                    }
                }
            }
        };
        loader.start();
    }

    public synchronized void addURI(String uri) {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException ex) {
            return;
        }
        if (retrieved_urls.add(url.toString())) {
            uris.add(uri);
        }
    }

    public synchronized String pullURI() {
        return uris.poll();
    }

    public synchronized void addTriple(Node[] triple) {

    }

}
