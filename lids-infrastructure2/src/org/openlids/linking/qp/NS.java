/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.linking.qp;

import java.util.HashMap;
import java.util.Map;
import org.semanticweb.yars.nx.Resource;

/**
 *
 * @author ssp
 */

public class NS {
    public final static String LIDS = "http://openlids.org/vocab#";
    public final static String RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    public final static String RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    public final static String OWL = "http://www.w3.org/2002/07/owl#";
    public final static String FOAF = "http://xmlns.com/foaf/0.1/";
    public final static String SIOC = "http://rdfs.org/sioc/ns#";
    public final static String OG = "http://ogp.me/ns#";
    public final static String DC = "http://purl.org/dc/elements/1.1/";
    public final static String GEO = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    public final static String GW = "http://openlids.org/geonameswrap/vocab#";
    public final static String GMW = "http://openlids.org/googlemapsapiwrap/vocab#";
    public final static String TW = "http://openlids.org/twitterwrap/vocab#";
    public final static String FW = "http://openlids.org/facewrap/vocab#";
    public final static String LDIN = "http://openlids.org/linkedinwrap/vocab#";
    public final static String V = "http://www.w3.org/2006/vcard/ns#";
    
    private final static Map<String, String> prefixes = new HashMap<String, String>();
    static {
        prefixes.put("lids", LIDS);
        prefixes.put("rdf", RDF);
        prefixes.put("rdfs", RDFS);
        prefixes.put("owl", OWL);
        prefixes.put("foaf", FOAF);
        prefixes.put("sioc", SIOC);
        prefixes.put("og", OG);
        prefixes.put("dc", DC);
        prefixes.put("geo", GEO);
        prefixes.put("gw", GW);
        prefixes.put("gmw", GMW);
        prefixes.put("tw", TW);
        prefixes.put("fw", FW);
        prefixes.put("ldin", LDIN);
        prefixes.put("v", V);
   }
    public final static Resource OWL_SAMEAS = new Resource(OWL + "sameAs");
    public final static Resource RDF_TYPE = new Resource(RDF + "type");

    public static String prefixToNS(String prefix) {
        return prefixes.get(prefix);
    }
}
