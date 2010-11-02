/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.NodeComparator;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

/**
 *
 * @author ssp
 */
public abstract class DataSet {

    Set<URL> resolvedURLs = new HashSet<URL>();
    Set<TripleAddObserver> observers = new HashSet<TripleAddObserver>();

    public void addObserver(TripleAddObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(TripleAddObserver observer) {
        observers.remove(observer);
    }

    public void addTriple(Node[] nx) {
        addTripleImpl(nx);
        for(TripleAddObserver observer : observers) {
            observer.notifyAddTriple(this, nx);
        }
    }

    public void addTriples(Set<Node[]> triples) {
        addTriplesImpl(triples);
        for(TripleAddObserver observer : observers) {
            observer.notifyAddTriples(this, triples);
        }
    }

    public Set<Node[]> getTriples() {
        TreeSet<Node[]> triples = new TreeSet<Node[]>(NodeComparator.NC);
        Iterator<Node[]> it = match(new Node[]{null, null, null});
        while(it.hasNext()) {
            triples.add(it.next());
        }
        return triples;
    }

    protected abstract void addTripleImpl(Node[] nx);

    protected abstract void addTriplesImpl(Set<Node[]> triples);

    public abstract Iterator<Node[]> match(Node[] pattern);

    public void crawlURI(Node n) {
        if(n instanceof Resource) {
            crawlURI(n.toString());
        }
    }

    public void crawlURI(String uri) {
        URL url;
        try {
            url = new URL(uri);
        } catch (MalformedURLException ex) {
            return;
        }

        if (resolvedURLs.contains(url))
            return;

        resolvedURLs.add(url);



        InputStream is = null;
        try {
            if (url.getProtocol().equals("file")) {
                is = url.openConnection().getInputStream();
            } else {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestProperty("Accept", "application/rdf+xml");
                is = conn.getInputStream();
                if (conn.getResponseCode() == 303) {
                    String newLoc = conn.getHeaderField("Location");
                    is.close();
                    conn.disconnect();
                    url = new URL(newLoc);
                    if (resolvedURLs.contains(url)) {
                        return;
                    }
                    resolvedURLs.add(url);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(3000);
                    conn.setRequestProperty("Accept", "application/rdf+xml");
                    is = conn.getInputStream();
                }
                if (conn.getResponseCode() != 200) {
                    System.err.println("response code != 200");
                    is.close();
                    conn.disconnect();
                    return;
                }
            }
            Callback cb = new Callback() {
                @Override
                public void endDocument() {
                }
                @Override
                public void processStatement(Node[] nx) {
                    addTriple(nx);
                }
                @Override
                public void startDocument() {
                }

            };

            try {
                System.out.println("U: " + url.toString());
                RDFXMLParser rdfxml = new RDFXMLParser(is, true, true, url.toString(), cb);
            } catch (Exception e) {
                System.err.println("Error during parsing: " + url.toString());
            }
        } catch (IOException ex) {
            return;
        }
    }

}
