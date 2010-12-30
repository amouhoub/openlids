<xsl:stylesheet
   version="1.0"
   xmlns:tw="http://openlids.org/twitterwrap/vocab#"
   xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
   xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
   xmlns:foaf="http://xmlns.com/foaf/0.1/"
   xmlns:owl="http://www.w3.org/2002/07/owl#"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:sioc="http://rdfs.org/sioc/ns#"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="statuses">
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>Source: Twitter API (http://www.twitter.com/) via twitterwrap.</rdfs:comment>
      </rdf:Description>

      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="status">
    <sioc:Item>
      <xsl:attribute name="rdf:about">./show/<xsl:value-of select="id"/>#id</xsl:attribute>
      <foaf:page>
	<xsl:attribute name="rdf:resource">http://twitter.com/<xsl:value-of select="user/screen_name"/>/status/<xsl:value-of select="id"/></xsl:attribute>
      </foaf:page>
      <xsl:apply-templates/>
    </sioc:Item>
  </xsl:template>

  <xsl:template match="text">
    <dc:description><xsl:value-of select="."/></dc:description>
  </xsl:template>

  <xsl:template match="user">
    <foaf:maker>
      <rdf:Description>
	<xsl:attribute name="rdf:about">../users/show?user_id=<xsl:value-of select="id"/>#id</xsl:attribute>
      </rdf:Description>
    </foaf:maker>
  </xsl:template>

  <xsl:template match="created_at">
    <dc:date><xsl:value-of select="."/></dc:date>
  </xsl:template>

  <xsl:template match="*">
    <xsl:element name="tw:{local-name()}">
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
