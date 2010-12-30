<?xml version="1.0"?>

<xsl:stylesheet
   xmlns:tw="http://openlids.org/twitterwrap/vocab#"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:owl="http://www.w3.org/2002/07/owl#"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:a="http://www.w3.org/2005/Atom"
   xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
   xmlns:twitter="http://api.twitter.com/"
   version="1.0">

  <xsl:strip-space elements="*"/>

  <xsl:output method="xml"/>

  <xsl:template match="a:feed">
    <rdf:RDF>
      <rdf:Description>
        <xsl:attribute name="rdf:about"/>
	<dc:title><xsl:value-of select="a:title"/></dc:title>
        <rdfs:comment>Source: Twitter Search API (http://search.twitter.com/) via twitterwrap.</rdfs:comment>
        <xsl:for-each select="a:seeAlso">
          <rdfs:seeAlso>
            <xsl:attribute name="rdf:resource">
              <xsl:value-of select="a:link"/>
            </xsl:attribute>
          </rdfs:seeAlso>
        </xsl:for-each>
      </rdf:Description>
      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="a:entry">
    <rdf:Description>
      <xsl:attribute name="rdf:ID">s<xsl:value-of select="substring(a:id, 29)"/></xsl:attribute>
      <foaf:page>
        <xsl:attribute name="rdf:resource">
          <xsl:value-of select="a:link/@href"/>
        </xsl:attribute>
      </foaf:page>
      <dc:date>
        <xsl:value-of select="a:updated"/>
      </dc:date>
      <xsl:if test="twitter:geo != ''">
        <geo:lat>
          <xsl:value-of select="substring-before(twitter:geo,'%20')"/>
        </geo:lat>
        <geo:long>
          <xsl:value-of select="substring-after(twitter:geo,'%20')"/>
        </geo:long>
      </xsl:if>
      <foaf:maker>
        <xsl:attribute name="rdf:resource">../users/show?screen_name=<xsl:value-of select="substring(a:author/a:uri, 20)"/>#id</xsl:attribute>
      </foaf:maker>
      <dc:description>
        <xsl:value-of select="a:title"/>
      </dc:description>
    </rdf:Description>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="tw:{local-name()}">
      <xsl:choose>
	<xsl:when test="starts-with(., 'http://')">
	  <xsl:attribute name="rdf:resource"><xsl:value-of select="."/></xsl:attribute>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="."/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>

</xsl:stylesheet>
