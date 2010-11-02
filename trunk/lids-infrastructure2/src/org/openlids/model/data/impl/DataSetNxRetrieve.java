/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openlids.model.data.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars2.rdfxml.RDFXMLParser;

/**
 *
 * @author ssp
 */
public class DataSetNxRetrieve extends DataSetNx {
   

    public Iterator<Node[]> match(Node[] tp) {
        crawlURI(tp[0]);
        return super.match(tp);
    }

  
  
}
