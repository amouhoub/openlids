<?xml version="1.0" encoding="utf-8"?>

<xsl:stylesheet
   version="1.0"
   xmlns:gw="http://openlids.org/geonameswrap/vocab#"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:owl="http://www.w3.org/2002/07/owl#"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:sioc="http://rdfs.org/sioc/ns#"
   xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:output method="xml"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="geonames">
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>Source: GeoNames API (http://www.geonames.org/) via geonameswrap.</rdfs:comment>
      </rdf:Description>

      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="totalResultsCount"/>

  <xsl:template match="geoname|entry|code">
      <rdf:Description rdf:ID="point">
          <foaf:based_near>
    <rdf:Description>
      <xsl:if test="geonameId">
	<xsl:attribute name="rdf:about">http://sws.geonames.org/<xsl:value-of select="geonameId"/>/</xsl:attribute>
      </xsl:if>
      <xsl:if test="wikipediaUrl">
	<!-- should be much easier with replace() but that's an xslt 2.0 function :( -->

	<xsl:variable name="str" select="wikipediaUrl"/>
	<xsl:variable name="newstr">
	  <xsl:call-template name="replaceCharsInString">
	    <xsl:with-param name="stringIn" select="string($str)"/>
	    <xsl:with-param name="charsIn" select="'http://en.wikipedia.org/wiki/'"/>
	    <xsl:with-param name="charsOut" select="'http://dbpedia.org/resource/'"/>
	  </xsl:call-template>
	</xsl:variable>

	<xsl:attribute name="rdf:about"><xsl:value-of select="$newstr"/></xsl:attribute>
	  <!-- string-replace(wikipediaUrl, 'http://en.wikipedia.org/wiki/', 'http://dbpedia.org/resource')"-->
	<foaf:page>
	  <xsl:attribute name="rdf:resource"><xsl:value-of select="wikipediaUrl"/></xsl:attribute>
	</foaf:page>
      </xsl:if>
      <xsl:apply-templates/>
    </rdf:Description>
    </foaf:based_near>
    </rdf:Description>
  </xsl:template>

  <xsl:template match="ptoponymName|name">
    <foaf:name><xsl:value-of select="."/></foaf:name>
  </xsl:template>

  <xsl:template match="lat">
    <geo:lat><xsl:value-of select="."/></geo:lat>
  </xsl:template>

  <xsl:template match="lng">
    <geo:long><xsl:value-of select="."/></geo:long>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="gw:{local-name()}">
      <xsl:choose>
	<xsl:when test="starts-with(., 'http://')">
	  <xsl:attribute name="rdf:resource"><xsl:value-of select="."/>/</xsl:attribute>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:value-of select="."/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>


<!-- here is the template that does the replacement -->
<xsl:template name="replaceCharsInString">
  <xsl:param name="stringIn"/>
  <xsl:param name="charsIn"/>
  <xsl:param name="charsOut"/>
  <xsl:choose>
    <xsl:when test="contains($stringIn,$charsIn)">
      <xsl:value-of select="concat(substring-before($stringIn,$charsIn),$charsOut)"/>
      <xsl:call-template name="replaceCharsInString">
        <xsl:with-param name="stringIn" select="substring-after($stringIn,$charsIn)"/>
        <xsl:with-param name="charsIn" select="$charsIn"/>
        <xsl:with-param name="charsOut" select="$charsOut"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$stringIn"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
