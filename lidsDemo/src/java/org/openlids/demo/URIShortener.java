/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.demo;

import java.util.HashMap;
import java.util.Map;
import org.semanticweb.yars.nx.BNode;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;

/**
 *
 * @author ssp
 */
public class URIShortener {
   static Map<String, String> prefixes = new HashMap<String, String>();
    static {
        prefixes.put("http://xmlns.com/foaf/0.1/", "foaf");
        prefixes.put("http://purl.org/dc/elements/1.1/", "dc");
        prefixes.put("http://www.w3.org/2003/01/geo/wgs84_pos#", "geo");
        prefixes.put("http://www.w3.org/2002/07/owl#", "owl");
        prefixes.put("http://www.w3.org/2006/vcard/ns#", "vcard");
        prefixes.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf");
        prefixes.put("http://www.w3.org/2000/01/rdf-schema#", "rdfs");
        prefixes.put("http://ogp.me/ns#", "og");
        prefixes.put("http://rdfs.org/sioc/ns#","sioc");
        prefixes.put("http://www.w3.org/2000/10/swap/log#","log");
        prefixes.put("http://dbpedia.org/ontology/","dbpedia");
    }


    public static String shortenURIBGP(Node[] bgp) {
         String ret="";
        for (Node v : bgp) {
            ret += shorten(v) + " ";
        }
        return ret;
    }

    public static String shorten(Node node) {
        return shorten(node,null);
    }

    public static String shorten(Node node, String baseURL) {
        if (node instanceof Resource) {
            return shortenURIStr(node.toString(), baseURL);
        } else if (node instanceof BNode) {
            return "...";
        } else {
            return node.toN3();
        }
    }

    public static String shortenURIStr(String uri) {
        return shortenURIStr(uri, null);
    }

    public static String shortenURIStr(String uri, String baseURL) {
        if(baseURL != null) {
            if (uri.startsWith(baseURL)) {
                return ":" + uri.substring(baseURL.length());
            }
        }
        for (String uristr : prefixes.keySet()) {
                if(uri.startsWith(uristr)) {
                    return prefixes.get(uristr) + ":" + uri.substring(uristr.length());
                }
            }
            return uri;
    }
}
