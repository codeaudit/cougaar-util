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
 ANY WARRANTIES AS TO NON-INFRINGEMENT.  IN NO EVENT SHALL COPYRIGHT
 HOLDER BE LIABLE FOR ANY DIRECT, SPECIAL, INDIRECT OR CONSEQUENTIAL
 DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE OF DATA OR PROFITS,
 TORTIOUS CONDUCT, ARISING OUT OF OR IN CONNECTION WITH THE USE OR
 PERFORMANCE OF THE COUGAAR SOFTWARE.
</copyright>
-->

<!--
Parse a Cougaar XML config file to create a java command line.

Example use:
  xml=mySociety.xml
  xsl=linux_cmd.xsl
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

  <xsl:template match="/society/host/node[@name=$node]">
    <!-- system properties -->
    <xsl:for-each select="vm_parameter">
      <xsl:sort/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="normalize-space(.)"/>
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
</xsl:stylesheet>

