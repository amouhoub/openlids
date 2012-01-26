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

  <xsl:template match="id_list">
    <rdf:RDF>
      <rdf:Description rdf:about="">
	<rdfs:comment>Source: Twitter API (http://www.twitter.com/) via twitterwrap (http://km.aifb.kit.edu/services/twitterwrap/).</rdfs:comment>
      </rdf:Description>

      <xsl:apply-templates/>
    </rdf:RDF>
  </xsl:template>

  <xsl:template match="ids">
    <rdf:Description rdf:about="#id">
      <xsl:apply-templates/>
    </rdf:Description>
  </xsl:template>

  <xsl:template match="id">
    <rdfs:seeAlso>
      <xsl:attribute name="rdf:resource">../users/show?user_id=<xsl:value-of select="."/>#id</xsl:attribute>
    </rdfs:seeAlso>
  </xsl:template>

  <xsl:template match="*"/>
</xsl:stylesheet>
