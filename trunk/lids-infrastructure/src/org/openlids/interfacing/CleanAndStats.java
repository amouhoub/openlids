package org.openlids.interfacing;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;


import org.openlids.model.BGP;
import org.openlids.model.BNode;
import org.openlids.model.IRI;
import org.openlids.model.Literal;
import org.openlids.model.ServiceDescription;
import org.openlids.model.Value;
import org.openlids.model.Variable;
import org.openlids.parser.ServiceParser;
import org.openlids.parser.ServiceParserJena;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.parser.Callback;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.nx.parser.ParseException;



public class CleanAndStats implements Callback {
	
	private static final Resource SAME_AS = new Resource("http://www.w3.org/2002/07/owl#sameAs");

	private static final Resource FOAF_BASED_NEAR = new Resource("http://xmlns.com/foaf/0.1/based_near");

	private long nrTriples = 0;
	
	FileWriter statsWriter;
	
	public boolean chunkMode = true;

	private Set<Node> linkedToGeoWrap = new HashSet<Node>();
	private Set<Node> linkedToGeoWrapSources = new HashSet<Node>();

	private String btcchunk;

	private String geolinks;

	private long nrLinksWritten = 0;

	private Set<Node> wrapperLinks = new HashSet<Node>();

	private FileWriter filteredWriter;
	
	public CleanAndStats(String btcchunk, String geolinks) throws IOException {
		this.btcchunk = btcchunk;
		this.geolinks = geolinks;
		statsWriter = new FileWriter(btcchunk + ".stats.txt");
	}
	

	
	public void startDocument() {
	}
	
	public void endDocument() {
		if(chunkMode) {
			chunkMode = false;
			// Write out original sources
			FileWriter fwriter;
			try {
				fwriter = new FileWriter(btcchunk + ".geosources.txt");
				for(Node src : linkedToGeoWrapSources) {
					fwriter.write(src.toN3() + "\n");
				}
				fwriter.close();
				
				
				statsWriter.write("nChunkTriples: " + nrTriples + "\n");
				statsWriter.write("nChunkLinkedToGeoNames: " + linkedToGeoWrap.size() + "\n");
				statsWriter.write("nChunkLinkedSources: " + linkedToGeoWrapSources.size() + "\n");

				linkedToGeoWrapSources = new HashSet<Node>();
				nrTriples = 0;
				filteredWriter = new FileWriter(geolinks + "filtered.nq");
				Iterator<Node[]> parser = new NxParser(new FileInputStream(geolinks),this);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		} else {
			try {
				filteredWriter.close();
				FileWriter fwriter;
				
				fwriter = new FileWriter(geolinks + ".geosources.txt");
				for(Node src : linkedToGeoWrapSources) {
					fwriter.write(src.toN3() + "\n");
				}
				fwriter.close();
				
				fwriter = new FileWriter(geolinks + ".geowrappers.txt");
				for(Node wrap : wrapperLinks ) {
					fwriter.write(wrap.toN3() + "\n");
				}
				
				statsWriter.write("nOrigLIDSLinks: " + nrTriples + "\n");
				statsWriter.write("nFilteredLIDSLinks: " + nrLinksWritten + "\n");
				statsWriter.write("nLIDSLinkedSources: " + linkedToGeoWrapSources.size() + "\n");
				statsWriter.close();
			} catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}
	
	public void processStatement(Node[] nx) {
		nrTriples++;
		if(chunkMode) {
			if(nx[2].toN3().startsWith("<http://sws.geonames.org/")) {
				linkedToGeoWrapSources.add(nx[3]);
				if(nx[1].equals(FOAF_BASED_NEAR) || nx[1].equals(SAME_AS)) {
					linkedToGeoWrap.add(nx[0]);
				}
			}
		} else {
			// Tests if <http://sws.geonames.... we don't need them
			if(nx[0].toN3().startsWith("<http://sws.geonames.org")) {
				return;
			}
			// Already linked nodes do not need new links
			if(linkedToGeoWrap.contains(nx[0])) {
				return;
			}
			// Remove 0.00,00
			if(nx[2].toN3().matches(".*lng=0\\.0+&lat=0\\.0+.*")) {
				return;
			}
			wrapperLinks.add(nx[2]);
			linkedToGeoWrapSources.add(nx[3]);
			nrLinksWritten++;
			try {
				for(Node n : nx)
					filteredWriter.write(n.toN3() + " ");
				filteredWriter.write(".\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
	public static void main(String args[]) {
		
		//args = new String[] { "/Users/ssp/No Time Machine/btc2010/btc-2010-chunk-000.nt", "/Users/ssp/No Time Machine/btc2010/geofindnearby.rq"};

		
		if(args.length < 2) {
			System.out.println("Usage: java -jar cleanandstats.jar btc-chunk ");
			System.out.println("  btc-chunk:   source data set in nquad format");
			System.out.println("  geo-links: output of annotator in nquad format");
			System.exit(-1);
		}
		try {
			FileInputStream fin = new FileInputStream(args[0]);
						
			CleanAndStats sa = new CleanAndStats(args[0],args[1]);
			
			try {
				Iterator<Node[]> parser;
				parser = new NxParser(fin,sa);
			} catch(Exception e) {
				System.err.println("That went wrong!\n");
				e.printStackTrace();
			}
			
			
		} catch (FileNotFoundException e) {
			System.err.println("Could not read file: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

			
}
