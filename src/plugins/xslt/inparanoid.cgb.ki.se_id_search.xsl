<?xml version="1.0" encoding="utf-8"?>

<!-- ===================================================================== -->
<!-- This was created for Soaplab/Gowlab project. See project details at   -->
<!--    http://industry.ebi.ac.uk/soaplab/Gowlab.html                      -->
<!-- The HTML source page parsed by this stylesheet is:                    -->
<!--    http://inparanoid.cgb.ki.se/id_search.html                         -->
<!-- ===================================================================== -->

<!-- Luke Hakes 2004/04/26 13:45 -->

<xsl:stylesheet version = '1.0'
    xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>
    <xsl:output method="xml" indent="yes"/>
    
<xsl:template match="/html">
    <xsl:choose>
        <xsl:when test="count (//body/hr[@noshade='noshade'][@size='1'])=0">
            <inparanoid summary="no hits"/>
        </xsl:when>
        <xsl:otherwise>
            <inparanoid summary="success">
                <xsl:call-template name="process">
                </xsl:call-template>
            </inparanoid>      
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name ="process">
    <search_species name="{//p/i}"/>
        <search_protein name="{//p/b}"/>
            <orthologs>
                <xsl:for-each select="//body/i">
                    <xsl:variable name="iposition">
                        <xsl:value-of select="position()"/>
                    </xsl:variable>
                    <xsl:if test="(($iposition*2) &lt; = 12) and (($iposition*2) &lt; = last())">
                        <xsl:variable name="species">
                            <xsl:value-of select="//body/i[$iposition*2]"/>
                        </xsl:variable>
                        <species name="{$species}">      
                            <xsl:for-each select="//body/pre/i">	
                                <xsl:variable name="prebodyi">
                                    <xsl:value-of select="normalize-space(.)"/>
                                </xsl:variable>
                                 <xsl:if test="$prebodyi = $species">
                                    <protein id="{preceding::*[1]}"/>
                                </xsl:if>
                            </xsl:for-each>
                        </species>
                    </xsl:if>
                </xsl:for-each>
            </orthologs>
</xsl:template>


</xsl:stylesheet>