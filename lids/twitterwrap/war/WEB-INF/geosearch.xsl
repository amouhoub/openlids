<xsl:stylesheet
   version="1.0"
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

      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="result">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="places">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="e">
    <geo:Point>
      <xsl:attribute name="rdf:about">./id/<xsl:value-of select="id"/>#id</xsl:attribute>
      <geo:country><xsl:value-of select="country"/></geo:country>
      <geo:country_code><xsl:value-of select="country_code"/></geo:country_code>
      <geo:full_name><xsl:value-of select="full_name"/></geo:full_name>
      <geo:id><xsl:value-of select="id"/></geo:id>
      <foaf:name><xsl:value-of select="name"/></foaf:name>
      <geo:place_type><xsl:value-of select="place_type"/></geo:place_type>
    </geo:Point>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>
