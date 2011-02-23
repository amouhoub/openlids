package com.ontologycentral.twittersearchwrap;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.yars.nx.Literal;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.Resource;
import org.semanticweb.yars.nx.namespace.DC;

public class Wikify {
	
	public static void main(String[] args){
		
		startWikify(args[0], "en");
		
	};
	
	public static String cleanTweets (String input){
	   input = input.replaceAll("http://.*?\\s", "");
	   input = input.trim();
	   input = input.replaceAll("\\r|\\n", "");
	   input = input.replaceAll("\\\\", "");
	   input = input.replace("/", "");
	   input = input.replace(";", "");
	   input = input.replace("<", "");
	   input = input.replace(">", "");
       return input;
	}

	public static Set<String> startWikify (String input, String lang){
		String uri = "http://km.aifb.kit.edu/services/wpmservlet-en/web/service";
		
		if(lang.equals("de")){
			uri = "http://km.aifb.kit.edu/services/wpmservlet-de/web/service";
		}
		
	    Wikifier w = new Wikifier(uri, "david's bot");
	    
	    input = cleanTweets(input);

//	    System.out.println("WIKIFY Input:");
//		System.out.println(input);
	    Node[] nx = new Node[] { DC.TITLE, new Literal(input) } ;
			               
		Set<Node[]> set = new HashSet<Node[]>();
		set.add(nx);
			    
		Set<String> ret = null;
	
		
		ret = w.wikify(new Resource("http://example.org/"), set);

		
		return ret;
	}
	
}
