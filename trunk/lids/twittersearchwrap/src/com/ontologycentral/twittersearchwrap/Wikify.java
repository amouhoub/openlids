package com.ontologycentral.twittersearchwrap;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.DC;

public class Wikify {
	
	public static void main(String[] args){
		
		startWikify(args[0]);
		
	};
	
	public static String cleanTweets (String input){
       return input;
	}

	public static Set<String> startWikify (String input){
		
		String uri = "http://km.aifb.kit.edu/services/wpmservlet-en/web/service";
	    Wikifier w = new Wikifier(uri, "david's bot");
			               
	    Node[] nx = new Node[] { DC.TITLE, new Literal(input) } ;
			               
		Set<Node[]> set = new HashSet<Node[]>();
		set.add(nx);
			    
		input = cleanTweets(input);
		
		return w.wikify(new Resource("http://example.org/"), set);
	}
	
}
