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


  <xsl:strip-space elements="*"/>

  <xsl:output method="xml"/>

  <xsl:template match="status">
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>Source: Twitter API (http://www.twitter.com/) via twitterwrap.</rdfs:comment>
      </rdf:Description>

      <sioc:Item rdf:ID="id">
	<xsl:apply-templates/>
      </sioc:Item>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="created_at">
    <dc:date><xsl:value-of select="."/></dc:date>
  </xsl:template>

  <xsl:template match="id">
    <dc:identifier><xsl:value-of select="."/></dc:identifier>
  </xsl:template>

  <xsl:template match="text">
    <sioc:content><xsl:value-of select="."/></sioc:content>
  </xsl:template>

  <xsl:template match="user">
    <foaf:maker>
      <rdf:Description>
	<xsl:attribute name="rdf:about">../../users/show?user_id=<xsl:value-of select="id"/>#id</xsl:attribute>
	<xsl:apply-templates/>
      </rdf:Description>
    </foaf:maker>
  </xsl:template>

  <xsl:template match="profile_image_url">
    <foaf:depiction>
      <xsl:attribute name="rdf:resource"><xsl:value-of select="."/></xsl:attribute>
    </foaf:depiction>
  </xsl:template>

  <xsl:template match="url">
    <foaf:homepage>
      <xsl:attribute name="rdf:resource"><xsl:value-of select="."/></xsl:attribute>
    </foaf:homepage>
  </xsl:template>

  <xsl:template match="name">
    <foaf:name><xsl:value-of select="."/></foaf:name>
  </xsl:template>

  <xsl:template match="screen_name">
    <foaf:nick><xsl:value-of select="."/></foaf:nick>
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
