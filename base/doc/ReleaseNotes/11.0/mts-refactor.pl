#!/usr/bin/perl
# -*- Perl -*-

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
