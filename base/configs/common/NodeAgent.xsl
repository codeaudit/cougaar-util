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
XSL Template for NodeAgent, which reuses most of SimpleAgent.
-->
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">

  <!-- extend SimpleAgent -->
  <xsl:import href="SimpleAgent.xsl"/>

  <!-- optional node-name filter -->
  <xsl:param name="node"/>

  <!--
  optional xsl parameters, passed by:
    -Dorg.cougaar.society.xsl.param.$name=$value
  -->
  <xsl:param name="metrics">full</xsl:param>
  <xsl:param name="mts">full</xsl:param>
  <!--
  these xsl parameters are already defined in SimpleAgent.xsl:
  <xsl:param name="servlets">true</xsl:param>
  <xsl:param name="planning">true</xsl:param>
  <xsl:param name="communities">true</xsl:param>
  -->

  <!--
  backwards compatibility for the wp server, passed by:
    -Dorg.cougaar.core.load.wp.server=true
  -->
  <xsl:param name="wpserver">true</xsl:param>

  <!--
  if a node "template" attribute is not specified, the "defaultNode"
  value is assumed, which defaults to this template file.
  -->
  <xsl:param name="defaultNode">NodeAgent.xsl</xsl:param>

  <xsl:output method="xml" indent="yes"/>

  <!-- match 'node' elements -->
  <xsl:template match="node[(@template = 'NodeAgent.xsl') or ($defaultNode = 'NodeAgent.xsl' and not(@template) and not(@type))]">
    <xsl:if test="not($node) or ($node = @name)">
      <node name="{@name}" template="">
        <!-- preserve facets, vm_parameters, etc -->
        <xsl:apply-templates select="node()[not(self::agent)]"/>
        <!-- get node-agent components -->
        <xsl:call-template name="NodeAgent_run"/>
        <!-- process child agents -->
        <xsl:apply-templates select="agent"/>
      </node>
    </xsl:if>
  </xsl:template>

  <!--
  Mix the config's componenents with both our node-agent specific
  components and the basic SimpleAgent components.
  -->
  <xsl:template name="NodeAgent_run">

    <xsl:call-template name="init_node"/>

    <xsl:call-template name="HIGH_agent_0"/>
    <xsl:call-template name="HIGH_config"/>
    <xsl:call-template name="HIGH_agent_1"/>

    <xsl:call-template name="HIGH_node_1b"/>

    <xsl:call-template name="HIGH_agent_2"/>

    <xsl:call-template name="INTERNAL_config"/>

    <xsl:call-template name="BINDER_config"/>
    
    <xsl:call-template name="BINDER_node_pre0"/>

    <xsl:call-template name="BINDER_agent_0"/>

    <xsl:call-template name="COMPONENT_agent_wp_server"/>

    <xsl:call-template name="COMPONENT_config"/>

    <xsl:call-template name="LOW_agent_0"/>
    <xsl:call-template name="LOW_config"/>
    <xsl:call-template name="LOW_agent_1"/>

    <xsl:call-template name="LOW_node_1b"/>
  </xsl:template>

  <xsl:template name="init_node">
    <!--
    Support components that are siblings of agents, e.g.:
    <component .. insertionpoint="Node.AgentManager.Binder"/>

    This is the extra special agent binder (bug 1340), possibly the cause of
    bugs 3038, 3279, and others...
    -->
    <xsl:call-template name="findAll">
      <xsl:with-param name="insertionpoint">Node.AgentManager.</xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="HIGH_node_1b">
    <!-- busy indicator -->
    <component
      name="org.cougaar.core.node.NodeBusyComponent()"
      class="org.cougaar.core.node.NodeBusyComponent"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>

    <!-- ConfigurationService -->
    <component
      name="org.cougaar.core.node.ConfigurationServiceComponent()"
      class="org.cougaar.core.node.ConfigurationServiceComponent"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>

    <!-- thread service -->
    <component
      name="org.cougaar.core.thread.ThreadServiceProvider()"
      class="org.cougaar.core.thread.ThreadServiceProvider"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component">
      <argument>isRoot=true</argument>
      <argument>BestEffortAbsCapacity=30</argument>
      <argument>WillBlockAbsCapacity=300</argument>
      <argument>CpuIntenseAbsCapacity=2</argument>
      <argument>WellBehavedAbsCapacity=2</argument>
    </component>

    <!-- wp and mts socket factory -->
    <component
      name="org.cougaar.mts.base.SocketFactorySPC()"
      class="org.cougaar.mts.base.SocketFactorySPC"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>

    <!-- wp cache -->
    <component
      name="org.cougaar.core.wp.resolver.ResolverContainer()"
      class="org.cougaar.core.wp.resolver.ResolverContainer"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient"/>
    <component
      name="org.cougaar.core.wp.bootstrap.ConfigManager()"
      class="org.cougaar.core.wp.bootstrap.ConfigManager"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.resolver.SelectManager()"
      class="org.cougaar.core.wp.resolver.SelectManager"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.resolver.ClientTransport()"
      class="org.cougaar.core.wp.resolver.ClientTransport"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.resolver.LeaseManager()"
      class="org.cougaar.core.wp.resolver.LeaseManager"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.resolver.CacheManager()"
      class="org.cougaar.core.wp.resolver.CacheManager"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.resolver.Resolver()"
      class="org.cougaar.core.wp.resolver.Resolver"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.bootstrap.DiscoveryManager()"
      class="org.cougaar.core.wp.bootstrap.DiscoveryManager"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.bootstrap.config.ConfigDiscovery()"
      class="org.cougaar.core.wp.bootstrap.config.ConfigDiscovery"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.bootstrap.multicast.MulticastDiscovery()"
      class="org.cougaar.core.wp.bootstrap.multicast.MulticastDiscovery"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.bootstrap.http.HttpDiscovery()"
      class="org.cougaar.core.wp.bootstrap.http.HttpDiscovery"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.bootstrap.rmi.RMIDiscovery()"
      class="org.cougaar.core.wp.bootstrap.rmi.RMIDiscovery"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <component
      name="org.cougaar.core.wp.bootstrap.EnsureIsFoundManager()"
      class="org.cougaar.core.wp.bootstrap.EnsureIsFoundManager"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.WPClient.Component"/>
    <xsl:call-template name="findAll">
      <xsl:with-param name="insertionpoint">Node.AgentManager.Agent.WPClient.</xsl:with-param>
    </xsl:call-template>

    <!-- metrics -->
    <xsl:choose>
      <xsl:when test="$metrics = 'trivial'">
        <!-- use trivial metrics service -->
        <component
           name="org.cougaar.core.qos.metrics.MetricsServiceProvider()"
           class="org.cougaar.core.qos.metrics.MetricsServiceProvider"
           priority="HIGH"
           insertionpoint="Node.AgentManager.Agent.MetricsServices"/>
      </xsl:when>
      <xsl:otherwise>
        <component
           name="org.cougaar.core.qos.rss.RSSMetricsServiceProvider()"
           class="org.cougaar.core.qos.rss.RSSMetricsServiceProvider"
           priority="HIGH"
           insertionpoint="Node.AgentManager.Agent.MetricsServices"/>
        <xsl:call-template name="findAll">
          <xsl:with-param name="insertionpoint">Node.AgentManager.Agent.MetricsServices.</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

    <component
      name="org.cougaar.core.node.NodeMetrics()"
      class="org.cougaar.core.node.NodeMetrics"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>

    <!-- incarnation tracker -->
    <component 
      name="org.cougaar.core.node.Incarnation()"
      class="org.cougaar.core.node.Incarnation"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>

    <!-- mts -->
    <xsl:choose>
      <xsl:when test="$mts = 'singlenode'">
        <!-- use single-node mts -->
        <component
           name="org.cougaar.core.mts.singlenode.SingleNodeMTSProvider()"
           class="org.cougaar.core.mts.singlenode.SingleNodeMTSProvider"
           priority="HIGH"
           insertionpoint="Node.AgentManager.Agent.MessageTransport"/>
      </xsl:when>
      <xsl:otherwise>
        <component
           name="org.cougaar.mts.base.MessageTransportServiceProvider()"
           class="org.cougaar.mts.base.MessageTransportServiceProvider"
           priority="HIGH"
           insertionpoint="Node.AgentManager.Agent.MessageTransport"/>
        <xsl:call-template name="findAll">
          <xsl:with-param name="insertionpoint">Node.AgentManager.Agent.MessageTransport.</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

    <!-- alarms -->
    <component
      name="org.cougaar.core.node.RealTimeComponent()"
      class="org.cougaar.core.node.RealTimeComponent"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>
    <component
      name="org.cougaar.core.node.NaturalTimeComponent()"
      class="org.cougaar.core.node.NaturalTimeComponent"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>

    <xsl:if test="$servlets = 'true'">
      <!-- servlets -->
      <component
         name="org.cougaar.lib.web.service.RootServletServiceComponent()"
         class="org.cougaar.lib.web.service.RootServletServiceComponent"
         priority="HIGH"
         insertionpoint="Node.AgentManager.Agent.Component"/>
    </xsl:if>

    <!-- SuicideService -->
    <component 
      name="org.cougaar.core.node.SuicideServiceComponent()"
      class="org.cougaar.core.node.SuicideServiceComponent"
      priority="HIGH"
      insertionpoint="Node.AgentManager.Agent.Component"/>

  </xsl:template>

  <xsl:template name="BINDER_node_pre0">
    <!-- community config reader -->
    <xsl:if test="$communities = 'true'">
      <component
         name="org.cougaar.community.init.CommunityInitializerServiceComponent()"
         class="org.cougaar.community.init.CommunityInitializerServiceComponent"
         priority="BINDER"
         insertionpoint="Node.AgentManager.Agent.Component"/>
    </xsl:if>

    <!-- asset config reader -->
    <xsl:if test="$planning = 'true'">
      <component
         name="org.cougaar.planning.ldm.AssetInitializerServiceComponent()"
         class="org.cougaar.planning.ldm.AssetInitializerServiceComponent"
         priority="BINDER"
         insertionpoint="Node.AgentManager.Agent.Component"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="LOW_node_1b">
    <!-- child agents -->
    <component
      name="org.cougaar.core.node.AgentLoader()"
      class="org.cougaar.core.node.AgentLoader"
      priority="LOW"
      insertionpoint="Node.AgentManager.Agent.Component">
      <xsl:for-each select="agent-decl">
        <argument>
          <xsl:value-of select="@name"/>
        </argument>
      </xsl:for-each>
      <xsl:for-each select="agent">
        <argument>
          <xsl:value-of select="@name"/>
        </argument>
      </xsl:for-each>
    </component>

    <!-- "." heartbeat printer -->
    <component
      name="org.cougaar.core.node.HeartbeatComponent()"
      class="org.cougaar.core.node.HeartbeatComponent"
      priority="LOW"
      insertionpoint="Node.AgentManager.Agent.Component"/>
  </xsl:template>

</xsl:stylesheet>
