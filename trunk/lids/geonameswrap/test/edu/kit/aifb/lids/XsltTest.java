package edu.kit.aifb.lids;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

public class XsltTest extends TestCase {
	public void testXstl() throws Exception {
		TransformerFactory tf = TransformerFactory.newInstance(); //"org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader() ); 

		Transformer t = tf.newTransformer(new StreamSource("war/WEB-INF/earthquakes.xsl"));
	}
}
