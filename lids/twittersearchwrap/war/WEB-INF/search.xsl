<xsl:stylesheet version="1.0" xmlns="http://ontologycentral.com/2010/06/jobs/vocab#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"  xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:twitter="http://api.twitter.com/" xmlns:a="http://www.w3.org/2005/Atom">
 
  <xsl:strip-space elements="*"/>
  <xsl:output method="xml"/>
  
  <xsl:template match="a:feed">
  	<xsl:element name="rdf:RDF">
  	<xsl:element name="rdf:Description">
  	  <xsl:attribute name="rdf:about"></xsl:attribute>
  	<xsl:element name="rdfs:comment">Source: Twitter Search API (http://search.twitter.com/) via twittersearchwrap (http://twittersearchwrap.ontologycentral.com/).</xsl:element>
  	</xsl:element>
  	<xsl:apply-templates/>
  	</xsl:element>
  </xsl:template>
  
  <xsl:template match="a:entry">
  		
      <xsl:element name="rdf:Description">
        <xsl:attribute name="rdf:ID"><xsl:value-of select="substring(a:author/a:uri, 20)"/></xsl:attribute>
          <xsl:element name="owl:sameAs">
            <xsl:attribute name="rdf:resource">http://semantictweet.com/<xsl:value-of select="substring(a:author/a:uri, 20)"/>#me</xsl:attribute>
	      </xsl:element>
      </xsl:element>
      
      <xsl:element name="rdf:Description">
      	<xsl:attribute name="rdf:ID">s<xsl:value-of select="substring(a:id, 29)"/></xsl:attribute>
      	<xsl:element name="foaf:page"> 
          <xsl:attribute name="rdf:ressource"><xsl:value-of select="a:link/@href"/></xsl:attribute>
        </xsl:element> 
        <xsl:element name="dc:date"> 
          <xsl:value-of select="a:updated"/>
        </xsl:element>
        <xsl:element name="dc:location"> 
          <xsl:value-of select="twitter:geo"/>
        </xsl:element> 
        <xsl:element name="foaf:maker"> 
          <xsl:attribute name="rdf:ressource">#<xsl:value-of select="substring(a:author/a:uri, 20)"/></xsl:attribute> 
        </xsl:element> 
        <xsl:element name="dc:description"> 
          <xsl:value-of select="a:title"/>
        </xsl:element>
      </xsl:element>
      
  </xsl:template>
  
  <xsl:template match="*"/>
    
</xsl:stylesheet>