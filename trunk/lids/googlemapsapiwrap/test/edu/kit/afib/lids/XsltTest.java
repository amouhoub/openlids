package edu.kit.afib.lids;

import junit.framework.TestCase;

public class XsltTest extends TestCase {
	public static String XSLT = "<?xml version='1.0' encoding='utf-8'?>\n" +
	"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>\n"+
	"<xsl:output method='text' encoding='utf-8'/>\n" +
	"<xsl:template match='GeocodeResponse'><xsl:apply-templates/></xsl:template>\n" +
	"<xsl:template match='result'><xsl:value-of select='geometry/location/lat'/>,<xsl:value-of select='geometry/location/lng'/></xsl:template>\n" +
	"<xsl:template match='*'/>\n" +
	"</xsl:stylesheet>";
	
	public void testXslt() {
		System.out.println(XSLT);
	}
}
