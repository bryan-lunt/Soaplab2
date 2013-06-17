#!/usr/bin/perl

use warnings;
use strict;
use XML::Compile::SOAP11;
use XML::Compile::WSDL11;
use XML::Compile::Transport::SOAPHTTP;

# read WSDL, compile in into a client who will call the service
my $service_endpoint =
    'http://localhost:8080/soaplab2/typed/services/edit.seqret';
#    'http://localhost:8080/soaplab2-axis/typed/services/edit.seqret';
#    'http://www.ebi.ac.uk/soaplab/typed/services/edit.seqret';
my $wsdl  = XML::LibXML->new->parse_file ($service_endpoint . '?wsdl');
my $proxy = XML::Compile::WSDL11->new ($wsdl);
foreach my $schema ( 1, 2, 3 ) {
    my $xsdXml =
	XML::LibXML->new->parse_file( $service_endpoint . '?xsd=' . $schema );
    if ($XML::Compile::SOAP::VERSION < 2.0) {
	$proxy->schemas->importDefinitions ($xsdXml);
    } else {
	$proxy->importDefinitions ($xsdXml);
    }
}

my $call = $proxy->compileClient ('runAndWaitFor');

# prepare inputs
my $inputs = {
    sequence => { usa => 'asis:actttggg' }
};

# call the service
my ($answer, $trace) = $call->($inputs);

# check the response
die 'ERROR: ' . $answer->{Fault}->{'faultstring'} . "\n"
    if defined $answer and defined $answer->{Fault};
die "Sorry, no response from the service\n\n"
    unless defined $answer;

#use Data::Dumper;
#print Dumper ($answer);

# print the results
for my $result (keys %{ $answer->{parameters} }) {
    print "$result:\n" . '=' x length ($result) . "=\n";
    print $answer->{parameters}->{$result};
    print "\n";
}
