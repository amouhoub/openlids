package org.openlids.registry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.stream.StreamSource;

public class Listener implements ServletContextListener {
	public static String T = "tranformer";
	
	public void contextInitialized(ServletContextEvent event) {
		ServletContext ctx = event.getServletContext();
       
		javax.xml.transform.TransformerFactory tf =
			javax.xml.transform.TransformerFactory.newInstance(); //"org.apache.xalan.processor.TransformerFactoryImpl")); //, this.getClass() ); 
		
		try {
			Transformer t = tf.newTransformer(new StreamSource(ctx.getRealPath("/WEB-INF/list.xsl")));
			ctx.setAttribute(T, t);
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
