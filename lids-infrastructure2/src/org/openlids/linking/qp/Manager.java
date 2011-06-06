/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import com.ontologycentral.ldspider.Crawler;
import com.ontologycentral.ldspider.frontier.Frontier;
import com.ontologycentral.ldspider.hooks.error.ErrorHandler;
import com.ontologycentral.ldspider.hooks.links.LinkFilter;
import com.ontologycentral.ldspider.hooks.sink.Sink;
import com.ontologycentral.ldspider.hooks.sink.SinkCallback;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.Callback;

class RankingFrontier extends Frontier {

    final Map<URI, URISource> _data;
    Comparator<URI> _comparator;

    public RankingFrontier() {
        super();
        _data = Collections.synchronizedMap(new HashMap<URI, URISource>());
        _comparator = new Comparator<URI>() {

            public int compare(URI u0, URI u1) {
                if(u0 == null || u1 == null)
                    return 0;
                URISource s0 = _data.get(u0);
                URISource s1 = _data.get(u1);
                if(s0 != null && s1 != null)
                    return s0.compareTo(s1);
                return 0;
            }

        };
    }


    @Override
    public void add(URI uri) {
        add(uri, URISource.DEFAULT);
    }

    public void add(URI uri, URISource src) {
        URISource o_src = _data.get(uri);
        if(o_src != null && o_src.compareTo(src) <= 0) {
            return;
        }
        _data.put(uri, src);
    }

    @Override
    public void remove(URI uri) {
        _data.remove(uri);
    }

    @Override
    public void removeAll(Collection<URI> clctn) {
        for(URI uri : clctn) {
            remove(uri);
        }
    }

    @Override
    public Iterator<URI> iterator() {
        if(_data.size() > 0) {
            System.out.println("PRESIZE: " + _data.size());
        }
        List<URI> li = new ArrayList<URI>();
        li.addAll(_data.keySet());
        Collections.sort(li, _comparator);
        if (_data.size() > 0 && li.isEmpty()) {
            System.out.println("PRESIZE: " + _data.size());
        }

        System.out.println("SIZE: " + li.size());

        return li.iterator();
    }

}


/**
 *
 * @author ssp
 */
public class Manager {
    static final Logger _log = Logger.getLogger(Manager.class.getName());


    public ManagerStatistics getStatistics() {
        ManagerStatistics ms = new ManagerStatistics();
        ms._retrieved_urls = retrieved_urls;
        ms._uris = uris;
        return ms;
    }

    final Rete _rete;
    final Crawler _crawler;
    final Frontier _frontier;

    Set<String> retrieved_urls = new HashSet<String>();
    final Queue<String> uris = new LinkedList<String>();

    Thread _loader;
    Boolean _loader_running = true;

    
    public void start() {
        _loader.start();
    }

    public void shutdown() throws InterruptedException {
        _loader_running = false;
        _rete.shutdown();
        _loader.join();
    }

    public Manager(Rete rete) {
        _rete = rete;
        final Manager manager = this;

        _crawler = new Crawler(20);
        _frontier = new RankingFrontier();
        
        LinkFilter linkFilter = new LinkFilter() {
            public void setErrorHandler(ErrorHandler eh) {
            }

            public void setFollowABox(boolean bln) {
            }

            public void setFollowTBox(boolean bln) {
            }

            public void startDocument() {
            }

            public void endDocument() {
            }

            public void processStatement(Node[] nodes) {
            }
        };
        _crawler.setLinkFilter(linkFilter);

        Sink sink = new SinkCallback(new Callback() {

            public void startDocument() {
            }

            public void endDocument() {
            }

            public void processStatement(Node[] nodes) {
                Node[] triple = nodes;
                if (nodes.length > 3) {
                    triple = new Node[] { nodes[0], nodes[1], nodes[2] };
                } if (nodes.length < 3) {
                    return;
                }
                _rete.addTriple(triple);
            }
        }, false);

        _crawler.setOutputCallback(sink);

//        _crawler.setQueue(new SpiderQueue(_crawler.getTldManager()) {
//
//            @Override
//            public URI poll() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//            @Override
//            public int size() {
//                throw new UnsupportedOperationException("Not supported yet.");
//            }
//
//        });
//
        _loader = new Thread() {
            @Override
            public void run() {
                while (_loader_running.booleanValue()) {
                    _crawler.evaluateBreadthFirst(_frontier, 5, 100, 100);
                    try {
                        Thread.sleep(250);
                    } catch (Exception ex) {

                    }
                }
            }
        };



//        _loader = new Thread() {
//            @Override
//            public void run() {
//                while (true) {
//                    final String uri = manager.pullURI();
//                    if(uri != null) {
//                        Thread uriloader = new Thread() {
//                            @Override
//                            public void run() {
//                                Callback cb = new Callback() {
//                                    @Override
//                                    public void endDocument() {
//                                    }
//                                    @Override
//                                    public void processStatement(Node[] nx) {
//                                        manager._rete.addTriple(new Node[]{nx[0], nx[1], nx[2]});
//                                    }
//                                    @Override
//                                    public void startDocument() {
//                                    }
//                                };
//
//
//                                try {
//                                    URL url = new URL(uri);
//
//// Use http commons. Avoid redirects to parse them again and again.
//// Why not just use ldspider?
//                                    URLConnection conn = url.openConnection();
//
//                                    if(url.toString().startsWith("http://km.aifb.kit.edu/services/facewrap/")) {
//                                        String fb_token = "2227470867|2.cdJzz1vKjLf9MshlMR8iDQ__.3600.1295024400-1424828568|JVbc_Xmm-dMotXdObT_ESJAm5h8";
//                                        conn.addRequestProperty("Authorization", "OAuth2 oauth2_access_token=\"" + fb_token + "\"");
//                                    }
//
//                                    InputStream is = conn.getInputStream();
//                                    RDFXMLParser rdfxml = new RDFXMLParser(is, true, true, url.toString(), cb);
//                                    _log.info("Successfully parsed " + uri + "\nThis is " + increase_successful_uris() + " out of " + retrieved_urls.size());
//                                } catch (Exception e) {
//                                    System.err.println("Error during parsing: " + uri);
//                                }
//                                synchronized(_loaders) {
//                                    _loaders.remove(this);
//                                }
//                            }
//                        };
//                        synchronized(_loaders) {
//                            _loaders.add(uriloader);
//                        }
//                        uriloader.start();
//                    }
//                    try {
//                        Thread.sleep(250);
//                    } catch(Exception ex) {
//
//                    }
//                }
//            }
//        };
//        _loader.start();
    }

    public synchronized void addURI(String uri) {
        URL url;
        try {
            url = new URL(uri);
            if (url.getRef() != null) {
                uri = uri.substring(0,uri.length() - url.getRef().length() - 1);
            }
        } catch (MalformedURLException ex) {
            return;
        }
        if(uri.startsWith("http://km.aifb.kit.edu/services/facewrap/")) {
            String fb_token = "";
            try {
                fb_token = URLEncoder.encode("2227470867|2.e1EoELFViwqx6rRqRbUP2Q__.3600.1295298000-1424828568|MdXzhcHqiAe_msIq4CPu_4bAMe4",
                        "utf-8");
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(Manager.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(uri.lastIndexOf("#") < 0) {
                uri = uri + (uri.contains("?") ? "&" : "?") + "access_token=" + fb_token;
            } else {
                uri = uri.substring(0, uri.lastIndexOf("#") - 1) + (uri.contains("?") ? "&" : "?") + "access_token=" + fb_token + uri.substring(uri.lastIndexOf("#"));
            }
        }
        try {
            if(this.retrieved_urls.add(uri)) {
                _frontier.add(new URI(uri));
            }
        } catch (URISyntaxException ex) {
            _log.log(Level.SEVERE, null, ex);
        }
    }

    public synchronized String pullURI() {
        return uris.poll();
    }

    public synchronized void addTriple(Node[] node) {
        _rete.addTriple(node);
    }

   
}
