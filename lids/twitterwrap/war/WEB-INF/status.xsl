<xsl:stylesheet version="1.0"
		xmlns="http://ontologycentral.com/2010/06/jobs/vocab#"
		xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
		xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"
		xmlns:foaf="http://xmlns.com/foaf/0.1/"
		xmlns:owl="http://www.w3.org/2002/07/owl#"
		xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="uri"/>

  <xsl:output method="xml"/>

  <xsl:strip-space elements="*"/>

  <xsl:template match="statuses">
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>Source: Twitter API (http://www.twitter.com/) via twitterwrap (http://twitterwrap.ontologycentral.com/).</rdfs:comment>
      </rdf:Description>

      <rdf:Description rdf:ID="id">
	<owl:sameAs>
	  <xsl:attribute name="rdf:resource">http://semantictweet.com/<xsl:value-of select="status/user/screen_name"/>#me</xsl:attribute>
	</owl:sameAs>
      </rdf:Description>

      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="status">
    <rdf:Description>
      <xsl:attribute name="rdf:ID">s<xsl:value-of select="id"/></xsl:attribute>
      <foaf:page>
	<xsl:attribute name="rdf:resource">http://twitter.com/<xsl:value-of select="user/screen_name"/>/status/<xsl:value-of select="id"/></xsl:attribute>
      </foaf:page>
      <xsl:apply-templates/>
    </rdf:Description>
  </xsl:template>

  <xsl:template match="text">
    <dc:description><xsl:value-of select="."/></dc:description>
  </xsl:template>

  <xsl:template match="user">
    <foaf:maker rdf:resource="#id"/>
  </xsl:template>

  <xsl:template match="created_at">
    <dc:date><xsl:value-of select="."/></dc:date>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>
