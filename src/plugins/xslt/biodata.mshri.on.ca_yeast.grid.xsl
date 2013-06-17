<?xml version="1.0" encoding="utf-8"?>

<!-- ===================================================================== -->
<!-- This was created for Soaplab/Gowlab project. See project details at   -->
<!--    http://industry.ebi.ac.uk/soaplab/Gowlab.html                      -->
<!-- The HTML source page parsed by this stylesheet is:                    -->
<!--    http://biodata.mshri.on.ca/yeast_grid/servlet/SearchPage           -->
<!-- ===================================================================== -->

<!-- $Id: biodata.mshri.on.ca_yeast.grid.xsl,v 1.1.1.1 2006/11/03 09:15:02 marsenger Exp $ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="/html">
    <xsl:choose>
      <xsl:when test="contains (body/p[@class='headingforsummary'], 'Did Not Return Any Exact Hits')">
        <yeast_grid summary="no exact hits"/>
      </xsl:when>
      <xsl:when test="count (body/table/tr[@class='gridoutputbody'])=0">
        <yeast_grid summary="no hits"/>
      </xsl:when>
      <xsl:otherwise>
        <yeast_grid summary="success">
          <xsl:for-each select="body/table/tr[@class='gridoutputbody']">
            <xsl:if test="position()=1">
              <xsl:call-template name="process_first_row">
                <xsl:with-param name="row" select="."/>
              </xsl:call-template>
            </xsl:if>
            <xsl:if test="position()!=1">
                <xsl:call-template name="process_other_rows">
                    <xsl:with-param name="row" select="."/>
                </xsl:call-template>
            </xsl:if>      
          </xsl:for-each>
        </yeast_grid>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="process_first_row">
    <xsl:param name="row"/>
        <search_protein name="{$row/td[1]}">
        <xsl:for-each select="$row/td[2]/ul/li">
            <alias><xsl:value-of select="i"/></alias>
        </xsl:for-each>
            <description><xsl:value-of select="normalize-space($row/td[3])"/></description>
            <go_annotations>
                <function>
                <xsl:for-each select="$row/td[4]/ul/li">
                    <go id="{substring-after(a/@href,'GO:')}"/>
                    <go desciption="{./a}"/>
                </xsl:for-each>
                </function>
                <process>
                <xsl:for-each select="$row/td[5]/ul/li">
                    <go id="{substring-after(a/@href,'GO:')}"/>
                    <go description="{./a}"/>
                </xsl:for-each>
                </process>
                <component>
                <xsl:for-each select="$row/td[6]/ul/li">
                    <go id="{substring-after(a/@href,'GO:')}"/>
                    <go description="{./a}"/>
                </xsl:for-each>
                </component>
            </go_annotations>
        </search_protein>
  </xsl:template>

  <xsl:template name="process_other_rows">
    <xsl:param name="row"/>
    <xsl:if test="./td[@align='center']">
    <interacting_proteins>
        <protein name="{$row/td[1]/a/b}">
        <xsl:for-each select="$row/td[2]/ul/li">
            <alias><xsl:value-of select="i/a/b"/></alias>
        </xsl:for-each>
            <description><xsl:value-of select="normalize-space($row/td[3])"/></description>
            <go_annotations>
                <function>
                <xsl:for-each select="$row/td[4]/ul/li">
                    <go id="{substring-after(a/@href,'GO:')}"/>
                    <go description="{./a}"/>
                </xsl:for-each>
                </function>
                <process>
                <xsl:for-each select="$row/td[5]/ul/li">
                    <go id="{substring-after(a/@href,'GO:')}"/>
                    <go description="{./a}"/>
                </xsl:for-each>
                </process>
                <component>
                <xsl:for-each select="$row/td[6]/ul/li">
                    <go id="{substring-after(a/@href,'GO:')}"/>
                    <go description="{./a}"/>
                </xsl:for-each>
                </component>
            </go_annotations>
            <system>
                <experiment type="{$row/td[7]}"/>
            </system>
            <sources>
                <provider id="{$row/td[8]}"/>
                <pubmed pmids="{substring-before(substring-after($row/td[8]/a/@href,'list_uids='), '&amp;')}"/>
                <xsl:if test="contains (following::*/td/a/@href, 'www.ncbi.nlm.nih.gov')">
                    <provider id="{following::*/td/a}"/>
                    <pubmed pmids="{substring-before(substring-after(following::*/td/a/@href,'list_uids='), '&amp;')}"/>
                </xsl:if>
            </sources>
        </protein>
        </interacting_proteins>
        </xsl:if>
  </xsl:template>
</xsl:stylesheet>
