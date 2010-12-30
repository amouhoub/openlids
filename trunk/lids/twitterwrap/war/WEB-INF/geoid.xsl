<xsl:stylesheet
   version="1.0"
   xmlns:tw="http://openlids.org/twitterwrap/vocab#"
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
	<rdfs:comment>Source: Twitter API (http://www.twitter.com/) via twitterwrap.</rdfs:comment>
      </rdf:Description>

      <geo:Point>
	<xsl:attribute name="rdf:about">./id/<xsl:value-of select="id"/>#id</xsl:attribute>
	<xsl:apply-templates/>
      </geo:Point>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="name">
    <foaf:name><xsl:value-of select="."/></foaf:name>
  </xsl:template>

  <xsl:template match="contained_within"/>

  <xsl:template match="geometry|bounding_box|polylines">
    <xsl:element name="tw:{local-name()}">
      <rdf:Description>
	<xsl:apply-templates/>
      </rdf:Description>
    </xsl:element>
  </xsl:template>

  <xsl:template match="e[@class='array']">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="e[@type='number']">
    <xsl:value-of select="."/>
    <xsl:text> </xsl:text>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="tw:{local-name()}">
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
