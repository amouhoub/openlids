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

  <xsl:template match="o">
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>Source: GeoNames API (http://www.geonames.org/) via geonameswrap.</rdfs:comment>
      </rdf:Description>

      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="earthquakes">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="e">
    <rdf:Description>
      <xsl:apply-templates/>
    </rdf:Description>
  </xsl:template>

  <xsl:template match="datetime">
    <dc:date><xsl:value-of select="."/></dc:date>
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
</xsl:stylesheet>
