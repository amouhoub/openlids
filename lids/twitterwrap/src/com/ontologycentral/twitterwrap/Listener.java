package com.ontologycentral.twitterwrap;

import java.util.Collections;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

public class Listener implements ServletContextListener {
	public static String T = "tranformer";
	public static String CACHE = "c";
	
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
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/status.xsl")));
			ctx.setAttribute(T, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
		
	public void contextDestroyed(ServletContextEvent event) {
		//ServletContext ctx = event.getServletContext();
	}
}