<?xml version="1.0"?>

<!--
<copyright>
 Copyright 2001-2004 BBNT Solutions, LLC
 under sponsorship of the Defense Advanced Research Projects Agency (DARPA).

 This program is free software; you can redistribute it and/or modify
 it under the terms of the Cougaar Open Source License as published by
 DARPA on the Cougaar Open Source Website (www.cougaar.org).

 THE COUGAAR SOFTWARE AND ANY DERIVATIVE SUPPLIED BY LICENSOR IS
 PROVIDED 'AS IS' WITHOUT WARRANTIES OF ANY KIND, WHETHER EXPRESS OR
 IMPLIED, INCLUDING (BUT NOT LIMITED TO) ALL IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE, AND WITHOUT
 ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 PERFORMANCE OF THE COUGAAR SOFTWARE.
</copyright>
-->

<!--
Parse a Cougaar XML config file to create a java command line.

This XSL file is used by the XSLNode and XSLNode.bat scripts.

Example use:
 xml=mySociety.xml
 xsl=xsl_node.xsl
 node=1AD_TINY
 args=`java org.apache.xalan.xslt.Process -in $xml -xsl $xsl $node`
 java $args
-->
<xsl:stylesheet
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 version="1.0">

 <xsl:output method="text"/>

 <xsl:param name="node">
  <!-- default to first listed node -->
  <xsl:value-of select="/society/host[1]/node[1]/@name"/>
 </xsl:param>

 <!-- allow windows to pass "java ", since it lacks an `echo -n java` -->
 <xsl:param name="java"/>

 <!-- allow windows to pass os, to fix $COUGAAR_INSTALL_PATH -->
 <xsl:param name="os"/>

 <xsl:template match="/">
   <xsl:if test="$java">
    <xsl:value-of select="$java"/>
    <xsl:text> </xsl:text>
   </xsl:if>
   <xsl:apply-templates/>
 </xsl:template>

 <xsl:template match="/society/host/node[@name=$node]">
  <!-- system properties -->
  <xsl:for-each select="vm_parameter">
   <xsl:sort/>
   <xsl:text> </xsl:text>
   <xsl:call-template name="fixCIP">
     <xsl:with-param name="string" select="normalize-space(.)"/>
   </xsl:call-template>
  </xsl:for-each>
  <!-- bootstrapper class -->
  <xsl:text> </xsl:text>
  <xsl:value-of select="normalize-space(class)"/>
  <!-- node class -->
  <xsl:text> </xsl:text>
  <xsl:value-of select="normalize-space(prog_parameter)"/>
 </xsl:template>

 <!-- ignore the rest -->
 <xsl:template match="text()"/>

 <!--
   replace $COUGAAR_INSTALL_PATH with %COUGAAR_INSTALL_PATH% 
   leave '/'s as-is, since the ConfigFinder will fix them.
 -->
 <xsl:template name="fixCIP">
   <xsl:param name="string"/>
   <xsl:param name="arg">
     <!-- awkward to make this work for any $var -->
     <xsl:text>$COUGAAR_INSTALL_PATH</xsl:text>
   </xsl:param>
   <xsl:choose>
     <xsl:when test="($os = 'windows') and contains($string, $arg)">
      <xsl:value-of select="substring-before($string, $arg)"/>
      <xsl:text>%</xsl:text>
      <xsl:value-of select="substring($arg,2,string-length($arg))"/>
      <xsl:text>%</xsl:text>
      <!-- recurse? -->
      <xsl:value-of select="substring(substring-after($string, $arg),1)"/>
     </xsl:when>
     <xsl:otherwise>
       <xsl:value-of select="."/>
     </xsl:otherwise>
   </xsl:choose>
 </xsl:template>
</xsl:stylesheet>
