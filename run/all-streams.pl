#!/usr/bin/perl -w
#
# It filters STDIN into both STDERR and STDOUT.
# Arguments: [-e <number>]
#    where <number> is the exit code to set
#
# $Id: all-streams.pl,v 1.1 2007/05/16 13:13:25 marsenger Exp $
# ---------------------------------------------

use strict;
use warnings;

use Getopt::Std;
my %opts;
getopt ('e', \%opts);
$opts{e} = 0 unless defined $opts{e};

while (<>) {
    print STDOUT;
    print STDERR;
}

exit $opts{e};
