<xsl:stylesheet
   version="1.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 
  <xsl:strip-space elements="*"/>
  <xsl:output method="xml"/>
  
  <xsl:template match="list">
    <html>
      <head><title>LIDS Registry</title></head>
      <link rel="stylesheet" href="style.css" type="text/css"/>
      <body>
	<h1>LIDS Registry</h1>

	<form id="form" action="register" method="get">
	  <dl>
	    <dt>LIDS URI</dt>
	    <dd><input name="lidsrdf" type="text" size="40"/>(full URI including http://)</dd>
	    <dt>Homepage</dt>
	    <dd><input name="base" type="text" size="50"/>(full URI including http://)</dd>
	    <dt>Title</dt>
	    <dd><input name="title" type="text" size="20"/></dd>
	    <dt>Comment</dt>
	    <dd><input name="title" type="text" size="20"/></dd>
	  </dl>
	  <input type="submit" value="Submit"/> 
	</form>

	<dl class="li">
	  <xsl:apply-templates/>
	</dl>
      </body>
    </html>
  </xsl:template>
  
  <xsl:template match="lids">
    <dt>
      <xsl:value-of select="lidsrdf"/>
    </dt>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="title|example|comment|base">
    <dd>
      <xsl:value-of select="."/>
    </dd>
  </xsl:template>
  
  <xsl:template match="*"/>
</xsl:stylesheet>
