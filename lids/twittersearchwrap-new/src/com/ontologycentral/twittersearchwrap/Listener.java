package com.ontologycentral.twittersearchwrap;

import java.io.File;
import java.util.Collections;


import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

import net.sf.jsr107cache.Cache;
import net.sf.jsr107cache.CacheException;
import net.sf.jsr107cache.CacheFactory;
import net.sf.jsr107cache.CacheManager;



public class Listener implements ServletContextListener {
	public static String T = "tranformer";
	public static String CACHE = "c";
	public static String LANG_RESOURCES_FOLDER = "langResFolder";
	
	
	
	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();

		//TransformerFactory tf = TransformerFactory.newInstance();
		
	    Cache cache = null;

	    try {
	        CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
	        cache = cacheFactory.createCache(Collections.emptyMap());
			ctx.setAttribute(CACHE, cache);
	    } catch (CacheException e) {
	    	e.printStackTrace();
	    }
        
		javax.xml.transform.TransformerFactory tf =
			javax.xml.transform.TransformerFactory.newInstance("org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader() ); 
		
		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/search.xsl")));
			ctx.setAttribute(T, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		
		File[] langFiles = new File[LanguageDetector.langFileNames.length];
		for( int i= 0 ; i < langFiles.length ; i++ ){
			langFiles[i] = new File(ctx.getRealPath("WEB-INF/europarl/" + LanguageDetector.langFileNames[i]));
		}
		ctx.setAttribute(LANG_RESOURCES_FOLDER, langFiles);
	}
		
	public void contextDestroyed(ServletContextEvent event) {
		//ServletContext ctx = event.getServletContext();
	}
}