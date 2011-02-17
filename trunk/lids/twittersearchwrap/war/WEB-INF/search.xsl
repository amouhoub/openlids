<xsl:stylesheet version="1.0" xmlns="http://ontologycentral.com/2010/06/jobs/vocab#" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:rdfs="http://www.w3.org/2000/01/rdf-schema#"  xmlns:foaf="http://xmlns.com/foaf/0.1/" xmlns:owl="http://www.w3.org/2002/07/owl#" xmlns:georss="http://www.georss.org/georss" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:twitter="http://api.twitter.com/" xmlns:a="http://www.w3.org/2005/Atom" xmlns:geo="http://www.w3.org/2003/01/geo/wgs84_pos#">
 
  <xsl:strip-space elements="*"/>
  <xsl:output method="xml"/>
  
  <xsl:template match="a:feed">
  	<xsl:element name="rdf:RDF">
  	<xsl:element name="rdf:Description">
  	  <xsl:attribute name="rdf:about"></xsl:attribute>
	  <dc:title><xsl:value-of select="a:title"/></dc:title>
  	  <xsl:element name="rdfs:comment">Source: Twitter Search API (http://search.twitter.com/) via twittersearchwrap</xsl:element>	
	  <xsl:for-each select="a:seeAlso">
		<xsl:element name="rdfs:seeAlso">
 	       <xsl:attribute name="rdf:resource"><xsl:value-of select="a:link"/></xsl:attribute>
 	    </xsl:element>
 	  </xsl:for-each>
  	</xsl:element>
	<xsl:apply-templates/>
  	</xsl:element>
  </xsl:template>

  <xsl:template match="a:entry">
  		
      <xsl:element name="rdf:Description">
      	<xsl:attribute name="rdf:about">http://km.aifb.kit.edu/services/twitterwrap/statuses/show/<xsl:value-of select="substring(a:id, 29)"/>#id</xsl:attribute>
      	<xsl:element name="foaf:page"> 
          <xsl:attribute name="rdf:resource"><xsl:value-of select="a:link/@href"/></xsl:attribute>
        </xsl:element> 
        <xsl:element name="dc:date"> 
          <xsl:value-of select="a:updated"/>
        </xsl:element>
        <xsl:if test="twitter:geo != ''">
          <xsl:element name="geo:lat"> 
            <xsl:value-of select="substring-before(twitter:geo/georss:point,' ')"/>
          </xsl:element> 
          <xsl:element name="geo:long"> 
            <xsl:value-of select="substring-after(twitter:geo/georss:point,' ')"/>
          </xsl:element> 
        </xsl:if>
        <xsl:element name="foaf:maker"> 
            <xsl:attribute name="rdf:resource">http://km.aifb.kit.edu/services/twitterwrap/users/show?screen_name=<xsl:value-of select="substring(a:author/a:uri, 20)"/>#id</xsl:attribute>
        </xsl:element> 
        <xsl:element name="dc:description"> 
          <xsl:value-of select="a:title"/>
        </xsl:element>
      </xsl:element>
      
  </xsl:template>
  
  <xsl:template match="*"/>
    
</xsl:stylesheet>
