package org.openlids.dbpwrap;

import java.util.Collections;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Listener implements ServletContextListener {
	public static String CACHE = "c";
	
	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();

	    Cache cache = null;

	    try {
	    	CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory(); 
	        cache = cacheFactory.createCache(Collections.emptyMap());
			ctx.setAttribute(CACHE, cache);
	    } catch (CacheException e) {
	    	e.printStackTrace();
	    }
	}
	
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
	}
}
