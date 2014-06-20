<?xml version='1.0' encoding='utf-8'?>

<xsl:stylesheet
   xmlns:gmw="http://openlids.org/googlemapsapiwrap/vocab#"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:owl="http://www.w3.org/2002/07/owl#"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:sioc="http://rdfs.org/sioc/ns#"
   xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#"
xmlns:dbo="http://dbpedia.org/ontology/"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   version='1.0'>

  <xsl:output method="xml" encoding="utf-8"/>

  <xsl:param name="address"/>

  <xsl:template match="GeocodeResponse">
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>Source: Google Maps API (http://code.google.com/apis/maps/documentation/webservices/) via googlemapsapiwrap.</rdfs:comment>
      </rdf:Description>

    <geo:Point rdf:ID="point">
      <dbo:address><xsl:value-of select="$address"/></dbo:address>
      <xsl:apply-templates/>
    </geo:Point>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="address_component"/>

  <xsl:template match="status"/>

  <xsl:template match="viewport|bounds"/>

  <xsl:template match="geometry">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="location">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="lat">
    <geo:lat><xsl:value-of select="."/></geo:lat>
  </xsl:template>

  <xsl:template match="lng">
    <geo:long><xsl:value-of select="."/></geo:long>
  </xsl:template>

  <xsl:template match="result">
        <foaf:based_near>
                <geo:Point>
                    <xsl:apply-templates/>
      <!--<xsl:value-of select="geometry/location/lat"/>,<xsl:value-of select="geometry/location/lng"/>-->
                </geo:Point>
            </foaf:based_near>
  </xsl:template>
  
  <xsl:template match="*">
    <xsl:element name="gmw:{local-name()}">
      <xsl:choose>
	<xsl:when test="starts-with(., 'http://')">
	  <xsl:attribute name="rdf:resource"><xsl:value-of select="."/></xsl:attribute>
	</xsl:when>
	<xsl:when test="text()">
	  <xsl:value-of select="."/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
