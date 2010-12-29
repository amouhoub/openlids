<xsl:stylesheet version="1.0"
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
	<dc:date><xsl:value-of select="created_at"/></dc:date>
	<dc:identifier><xsl:value-of select="id"/></dc:identifier>
	<sioc:content><xsl:value-of select="text"/></sioc:content>
	<xsl:apply-templates/>
      </sioc:Item>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="user">
    <foaf:maker>
      <rdf:Description>
	<xsl:attribute name="rdf:about">../../users/show?user_id=<xsl:value-of select="id"/>#id</xsl:attribute>
	<dc:identifier><xsl:value-of select="id"/></dc:identifier>
	<foaf:name><xsl:value-of select="name"/></foaf:name>
	<foaf:nick><xsl:value-of select="screen_name"/></foaf:nick>
	<foaf:depiction>
	  <xsl:attribute name="rdf:resource"><xsl:value-of select="profile_image_url"/></xsl:attribute>
	</foaf:depiction>
	<foaf:homepage>
	  <xsl:attribute name="rdf:resource"><xsl:value-of select="url"/></xsl:attribute>
	</foaf:homepage>
      </rdf:Description>
    </foaf:maker>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>
