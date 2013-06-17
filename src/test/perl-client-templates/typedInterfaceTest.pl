#!/usr/bin/perl

use XML::Compile::SOAP11;
use XML::Compile::WSDL11;
use XML::Compile::Transport::SOAPHTTP;
use strict;

####

eval {

	my $inputs = {
		sequence => { usa => 'asis:actttggg' }
	};

	my $serviceendpoint =
	  'http://tc-test-3:9800/soaplab/typed/services/edit.seqret';
	  #'http://www.ebi.ac.uk/soaplab/typed/services/edit.seqret';

	print "Retriving and processing the WSDL and the XSDs imported\n";
	my $wsdl  = XML::LibXML->new->parse_file( $serviceendpoint . '?wsdl' );
	my $proxy = XML::Compile::WSDL11->new($wsdl);
	foreach my $schema ( 1, 2, 3 ) {
		my $xsdXml =
		  XML::LibXML->new->parse_file( $serviceendpoint . '?xsd=' . $schema );
		if($XML::Compile::SOAP::VERSION < 2.0)
		{
			$proxy->schemas->importDefinitions($xsdXml);
		}
		else
		{
			$proxy->importDefinitions($xsdXml);
		}
	}

	print "Preparing caller object for runAndWaitFor operation\n";
	my $runAndWaitFor = $proxy->compileClient('runAndWaitFor');

	print "Calling the service and getting the response\n";
	my ( $answer, $trace ) = $runAndWaitFor->( $inputs );

	# &printTrace($trace);

	my %answer = %{$answer};

	my %answer_;
	if ($answer{'runAndWaitForResponse'} != 0)
	{
	  %answer_ = %{ $answer{'runAndWaitForResponse'} };
	}
	else
	{
	  %answer_ = %{ $answer{'parameters'} };
	}	
	#my %answer_ = %{ $answer{'runAndWaitForResponse'} };

	my $report = $answer_{'report'};
	my $detailed_status = $answer_{'detailed_status'};
	my $outseq = $answer_{'outseq'};

	print "\nSoaplab report received:\n----------------------------\n\n$report\n";

	if ( $detailed_status == 0 ) {
		print "Passed\n";
		exit 0;
	}

	print "Failed\n";
	exit 1;
};

if ($@) {
	print "Caught an exception\n" . $@;
	exit 1;
}

# Print request/response trace
sub printTrace($) {
	my $trace = shift;
	$trace->printTimings;
	$trace->printRequest;
	$trace->printResponse;
}

# File to String
sub file2String($) {
	my $fname = shift;
	open FILE, $fname or die "Couldn't open file: $!";
	my $fcontent = join( "", <FILE> );
	close FILE;
	return $fcontent;
}
