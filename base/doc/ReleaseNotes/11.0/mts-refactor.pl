#!/usr/bin/perl
# -*- Perl -*-

# <copyright>
#  
#  Copyright 2004 BBNT Solutions, LLC
#  under sponsorship of the Defense Advanced Research Projects
#  Agency (DARPA).
# 
#  You can redistribute this software and/or modify it under the
#  terms of the Cougaar Open Source License as published on the
#  Cougaar Open Source Website (www.cougaar.org).
# 
#  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
#  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
#  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
#  A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
#  OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
#  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
#  LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
#  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
#  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
#  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
#  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#  
# </copyright>


# usage:
# perl mts-refactor.pl file1.java file2.java ...
#
# This script will perform most of the required changes to port
# 10.* MTS integrated-code to 11.0
# Note that this has only been tested on unix, and only on simple
# cases - at minimum, you should compare original to new code (cvs diff)
# before committing any changes.

$tmp = "/tmp/mvmts.$$";

%repl;
setup();

while ($file = shift) {
  $found = 0;
  open(IN, "<$file");
  unlink($tmp);
  open(OUT, ">$tmp");
  while (<IN>) {
    if (/org\.cougaar\.core\.mts\.(\w+)/) {
      my ($o) = $1;
      if ($repl{$o}) {
	s/org\.cougaar\.core\.mts\.(\w+)/org.cougaar.mts.std.$o/;
	$found++;
      }
    }
    print OUT $_;
    # handle wildcard imports - ugly but
    if (/import org\.cougaar\.core\.mts.\*/) {
      $found++;
      print OUT "import org.cougaar.mts.std.*;\n";
    }
  }
  close(OUT);
  close(IN);

  if ($found) {
    # made changes
    unlink($file);
    open(IN, "<$tmp");
    open(OUT, ">$file");
    while(<IN>)  {
      print OUT "$_";
    }
    close(OUT);
    close(IN);
    print "Changed $file\n";
  }
}
unlink($tmp);


sub setup {
  my ($s) = <<EOF;
AbstractLinkSelectionPolicy
AbstractSocketControlPolicy
AgentFlowAspect
AgentLocalStatusServlet
AgentRemoteStatusServlet
AgentStatusAspect
AgentStatusServlet
AspectFactory
AspectSupport
AspectSupportImpl
AttributedMessage
BoundComponent
BufferedStreamsAspect
CachingStreamsAspect
ChecksumStreamsAspect
CommFailureException
CompressingStreamsAspect
CougaarIOException
CountBytesStreamsAspect
DeliveryVerificationAspect
DestinationLink
DestinationLinkDelegateImplBase
DestinationQueue
DestinationQueueDelegateImplBase
DestinationQueueFactory
DestinationQueueImpl
DestinationQueueMonitorPlugin
DestinationQueueMonitorService
DestinationQueueMonitorServlet
DestinationQueueProviderService
DontRetryException
FlushAspect
ForwardMessageLoggingAspect
FuseServerSocket
FutileSerializingLinkProtocol
Gossip
GossipAspect
GossipPropagation
GossipQualifierService
GossipServlet
GossipStatisticsService
GossipStatisticsServiceAspect
GossipSubscription
GossipTrafficRecord
HeartBeatAspect
KeyGossip
LinkProtocol
LinkProtocolFactory
LinkProtocolService
LinkSelectionPolicy
LinkSelectionPolicyServiceProvider
LinkSelectionProvision
LinkSelectionProvisionService
LoopbackLinkProtocol
MT
MTImpl
MessageDeliverer
MessageDelivererDelegateImplBase
MessageDelivererFactory
MessageDelivererImpl
MessageProtectionAspect
MessageProtectionServiceImpl
MessageQueue
MessageReader
MessageReaderDelegateImplBase
MessageReaderImpl
MessageReply
MessageSecurityException
MessageSendTimeAspect
MessageSerializationException
MessageStreamsFactory
MessageTimeoutAspect
MessageTransportAspect
MessageTransportException
MessageTransportRegistry
MessageTransportRegistryService
MessageTransportServiceProvider
MessageTransportServiceProxy
MessageWatcherServiceImpl
MessageWriter
MessageWriterDelegateImplBase
MessageWriterImpl
MetricsBlastTestAspect
MetricsTestAspect
MinCostLinkSelectionPolicy
MisdeliveredMessageException
MulticastAspect
MulticastMessageAddress
NameLookupException
NameSupport
NameSupportDelegateImplBase
NameSupportImpl
PreserializingStreamsAspect
PrioritizedThreadsAspect
RMILinkProtocol
RMIRemoteObjectDecoder
RMIRemoteObjectEncoder
RMISocketControlAspect
RMISocketControlService
RMITestAspect
ReceiveLink
ReceiveLinkDelegateImplBase
ReceiveLinkFactory
ReceiveLinkImpl
ReceiveLinkProviderService
Router
RouterDelegateImplBase
RouterFactory
RouterImpl
SSLRMILinkProtocol
SampleSocketControlPolicy
ScrambleAspect
SecurityAspect
SendLink
SendLinkDelegateImplBase
SendLinkImpl
SendQueue
SendQueueDelegateImplBase
SendQueueFactory
SendQueueImpl
SequenceAspect
SerializationAspect
SerializedMT
SerializedMTImpl
SerializedRMILinkProtocol
ServerSocketWrapper
ServiceTestAspect
SimpleGossipQualifierComponent
SocketControlPolicy
SocketControlProvision
SocketControlProvisionService
SocketDelegateImplBase
SocketFactory
StandardAspect
StatisticsAspect
StatisticsPlugin
StatisticsServlet
StepService
StepperAspect
StepperControlExampleAspect
StubDumperAspect
TraceAspect
TransientIOException
UnregisteredNameException
UnresolvableReferenceException
ValueGossip
WasteCPUAspect
WatcherAspect
EOF
  foreach $_ (split(/\n/,$s)) {
    $repl{$_} = 1;
  }
}
