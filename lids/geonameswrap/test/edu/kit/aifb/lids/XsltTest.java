package edu.kit.aifb.lids;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

public class XsltTest extends TestCase {
	public void testXstl() throws Exception {
		TransformerFactory tf = TransformerFactory.newInstance(); //"org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader() ); 

		Transformer t = tf.newTransformer(new StreamSource("war/WEB-INF/earthquakes.xsl"));
	}
	
	public void testXstl2() throws Exception {
		TransformerFactory tf = TransformerFactory.newInstance(
		"net.sf.saxon.TransformerFactoryImpl",
	    		  Thread.currentThread().getContextClassLoader()); 

//		TransformerFactory tf = TransformerFactory.newInstance(); //"org.apache.xalan.processor.TransformerFactoryImpl", this.getClass().getClassLoader() ); 

		Transformer t = tf.newTransformer(new StreamSource("sparql2kml-twitter.xsl"));
		
		//StreamSource ss = new StreamSource(new ByteArrayInputStream(streamToByteArray(new FileInputStream("outpu_savt.xml"))));
		
		StreamSource ss = new StreamSource(new FileInputStream("outpu_savt.xml"));

		t.transform(ss, new StreamResult(System.out));
	}
	
	public static byte[] streamToByteArray(InputStream is) throws IOException {
		ArrayList<Byte> al = new ArrayList<Byte>();
		int input = -1;
		while ((input = is.read()) != -1) {
			al.add(Byte.valueOf((byte) input));
		}
		byte[] ret = new byte[al.size()];
		for (int i = 0; i < ret.length; ++i)
			ret[i] = al.get(i);
		return ret;

	}
}
