<?xml version="1.0" encoding="UTF-8"?>
<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.

-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="1.0">

  <xsl:template match="reportlet[@class='org.apache.syncope.core.report.UserReportlet']">
   
    <fo:block font-size="16pt" font-weight="bold" space-after="0.5cm" space-before="5mm">Reportlet: <xsl:value-of select="@name"/></fo:block>
        
    <xsl:for-each select="user">
      <fo:block font-size="14pt" font-weight="bold" space-before="15mm" space-after="5mm" background-color="(#8888ff)">User <xsl:value-of select="@username"/></fo:block>
      <fo:table table-layout="fixed" space-after="7mm">
        <fo:table-column/>
        <fo:table-column/>
        <fo:table-body>
          <fo:table-row background-color="(#ccccff)">
            <fo:table-cell>
              <fo:block>Status:</fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block font-style="italic">
                <xsl:value-of select="@status"/>
              </fo:block>
            </fo:table-cell>
          </fo:table-row>
          <xsl:if test="string-length(@creationDate) &gt; 0">
            <fo:table-row background-color="(#ccccff)">
              <fo:table-cell>
                <fo:block>Creation Date:</fo:block>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block font-style="italic">
                  <xsl:value-of select="@creationDate"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:if>
          <xsl:if test="string-length(@lastLoginDate) &gt; 0">
            <fo:table-row background-color="(#ccccff)">
              <fo:table-cell>
                <fo:block>Last Login Date:</fo:block>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block font-style="italic">
                  <xsl:value-of select="@lastLoginDate"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:if>
          <xsl:if test="string-length(@changePwdDate) &gt; 0">
            <fo:table-row background-color="(#ccccff)">
              <fo:table-cell>
                <fo:block>Change Password Date:</fo:block>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block font-style="italic">
                  <xsl:value-of select="@changePwdDate"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:if>
          <xsl:if test="string-length(@passwordHistorySize) &gt; 0">
            <fo:table-row background-color="(#ccccff)">
              <fo:table-cell>
                <fo:block>Password History Size:</fo:block>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block font-style="italic">
                  <xsl:value-of select="@passwordHistorySize"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:if>
          <xsl:if test="string-length(@failedLoginCount) &gt; 0">
            <fo:table-row background-color="(#ccccff)">
              <fo:table-cell>
                <fo:block>Number of Failed Login Attempts:</fo:block>
              </fo:table-cell>
              <fo:table-cell>
                <fo:block font-style="italic">
                  <xsl:value-of select="@failedLoginCount"/>
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:if>
        </fo:table-body>
      </fo:table>
      <xsl:call-template name="attributes">
        <xsl:with-param name="label">Attributes</xsl:with-param>
        <xsl:with-param name="node" select="attributes/attribute"/>
      </xsl:call-template>
      <xsl:choose>
        <xsl:when test="string-length(derivedAttributes/derivedAttribute) &gt; 0">
          <xsl:call-template name="attributes">
            <xsl:with-param name="label">Derived Attributes</xsl:with-param>
            <xsl:with-param name="node" select="derivedAttributes/derivedAttribute"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <fo:block color="red" font-size="9pt" space-after="3mm">THIS USER HASN'T ANY DERIVED ATTRIBUTE</fo:block>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="string-length(virtualAttributes/virtualAttribute) &gt; 0">
          <xsl:call-template name="attributes">
            <xsl:with-param name="label">Virtual Attributes</xsl:with-param>
            <xsl:with-param name="node" select="virtualAttributes/virtualAttribute"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <fo:block color="red" font-size="9pt" space-after="3mm">THIS USER HASN'T ANY VIRTUAL ATTRIBUTE</fo:block>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="string-length(memberships/membership) &gt; 0">
          <fo:block font-size="11pt" font-weight="bold">Memberships</fo:block>
          <xsl:for-each select="memberships/membership">
            <fo:block font-size="10pt" font-weight="bold" space-before="2mm">Role: <xsl:value-of select="@roleName"/>(<xsl:value-of select="@roleId"/>)</fo:block>
            <fo:block start-indent="1cm" space-before="3mm" space-after="0.5cm">
              <xsl:choose>
                <xsl:when test="string-length(attributes/attribute) &gt; 0">
                  <xsl:call-template name="attributes">
                    <xsl:with-param name="label">Attributes</xsl:with-param>
                    <xsl:with-param name="node" select="attributes/attribute"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <fo:block color="red" font-size="8pt" space-after="2mm">THIS ROLE HASN'T ANY ATTRIBUTE</fo:block>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:choose>
                <xsl:when test="string-length(derivedAttributes/derivedAttribute) &gt; 0">
                  <xsl:call-template name="attributes">
                    <xsl:with-param name="label">Derived Attributes</xsl:with-param>
                    <xsl:with-param name="node" select="derivedAttributes/derivedAttribute"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <fo:block color="red" font-size="8pt" space-after="2mm">THIS ROLE HASN'T ANY DERIVED ATTRIBUTE</fo:block>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:choose>
                <xsl:when test="string-length(virtualAttributes/virtualAttribute) &gt; 0">
                  <xsl:call-template name="attributes">
                    <xsl:with-param name="label">Virtual Attributes</xsl:with-param>
                    <xsl:with-param name="node" select="virtualAttributes/virtualAttribute"/>
                  </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                  <fo:block color="red" font-size="8pt" space-after="2mm">THIS ROLE HASN'T ANY VIRTUAL ATTRIBUTE</fo:block>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:call-template name="resources">
                <xsl:with-param name="node" select="resources/resource"/>
              </xsl:call-template>
            </fo:block>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <fo:block color="red" font-size="9pt" space-after="3mm">THIS USER HASN'T BEEN ASSIGNED TO ANY ROLE</fo:block>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="resources">
        <xsl:with-param name="node" select="resources/resource"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>
  <xsl:template name="attributes">
    <xsl:param name="label"/>
    <xsl:param name="node"/>
    <fo:block font-size="11pt" font-weight="bold" space-after="2mm">
      <xsl:value-of select="$label"/>
    </fo:block>
    <fo:table table-layout="fixed" space-after="7mm">
      <fo:table-column/>
      <fo:table-column/>
      <fo:table-header>
        <fo:table-row height="7mm" background-color="(#ccccba)">
          <fo:table-cell>
            <fo:block font-weight="bold">Schema name</fo:block>
          </fo:table-cell>
          <fo:table-cell>
            <fo:block font-weight="bold">Value(s)</fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-header>
      <fo:table-body>
        <xsl:for-each select="$node">
          <xsl:if test="string-length(value/text()) &gt; 0">
            <fo:table-row height="4mm" background-color="(#ccccff)">
              <fo:table-cell>
                <fo:block>
                  <xsl:value-of select="@name"/>
                </fo:block>
              </fo:table-cell>
              <fo:table-cell>
                <xsl:for-each select="value">
                  <fo:block></fo:block><!--                        <fo:block>&#x2022;</fo:block>-->
                  <fo:block font-style="italic">
                    <xsl:value-of select="text()"/>
                  </fo:block>
                </xsl:for-each>
              </fo:table-cell>
            </fo:table-row>
          </xsl:if>
          <fo:table-row>
            <fo:table-cell>
              <fo:block></fo:block>
            </fo:table-cell>
            <fo:table-cell>
              <fo:block></fo:block>
            </fo:table-cell>
          </fo:table-row>
        </xsl:for-each>
      </fo:table-body>
    </fo:table>
  </xsl:template>
  <xsl:template name="resources">
    <xsl:param name="node"/>
    <fo:block font-size="11pt" font-weight="bold" space-after="3mm">Resources</fo:block>
    <xsl:for-each select="$node">
      <fo:block></fo:block> <!--            <fo:block>&#x2022;</fo:block>-->
      <fo:block background-color="(#ccccff)">
        <xsl:value-of select="@name"/>
      </fo:block>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>