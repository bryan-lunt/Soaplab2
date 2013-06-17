<?xml version="1.0" encoding="utf-8"?>

<!-- ===================================================================== -->
<!-- This was created for Soaplab/Gowlab project. See project details at   -->
<!--    http://industry.ebi.ac.uk/soaplab/Gowlab.html                      -->
<!-- The HTML source page parsed by this stylesheet is:                    -->
<!--    http://biodata.mshri.on.ca/yeast_grid/servlet/SearchPage           -->
<!-- ===================================================================== -->


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:my="http://www.w3.org/1999/xhtml"
    version="1.0">
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="/">
    <xsl:apply-templates select="//my:html"/>
  </xsl:template>
  <xsl:template match="my:html">
    <xsl:choose>
            <xsl:when test="count (my:body/my:h2)>=1">
                <sgd summary="Found no entries-invaild ID"/>
            </xsl:when>
            <xsl:when test="count (my:body/my:h4)>=1">
                <sgd summary="Found ambibuous entry-using gene name">
                    <xsl:call-template name="ambiguous">
                        <xsl:with-param name="row" select="my:body/my:table/my:tr[2]"/>
                    </xsl:call-template>
                </sgd>
            </xsl:when>
            <xsl:when test="count (my:body/my:table/my:tr/my:td/my:h1)>=1">
                <sgd summary="Found standard entry">
                    <orf id="{substring-after(my:body/my:table/my:tr/my:td/my:h1, '/')}"/>
                </sgd>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="ambiguous">
        <xsl:param name="row"/>
            <xsl:variable name="match">
                <xsl:value-of select="$row/my:td/my:b/my:a"/>
            </xsl:variable>
            <xsl:variable name="gene">
                <xsl:value-of select="$row/my:td/my:b"/>
            </xsl:variable>
                <xsl:if test="$match=$gene">
                    <orf id="{$row/td[3]}"/>
                </xsl:if>
    </xsl:template>
        
    
</xsl:stylesheet>
    
   
