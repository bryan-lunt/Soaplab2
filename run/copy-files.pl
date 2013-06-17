#!/usr/bin/perl -w
#
# It copies a file (-i) (if given) to STDOUT.
# Then it adds to STDOUT contents of files given by names
# by the -l option.
#
# Arguments: -i <input-file> -l <list-file>
# 
# $Id: copy-files.pl,v 1.2 2007/10/26 18:05:40 marsenger Exp $
# ---------------------------------------------

use strict;
use warnings;
use File::Copy;
use File::Basename;

use Getopt::Std;
my %opts;
getopt ('iol', \%opts);

exit 0 unless $opts{i} or $opts{l};

copy ($opts{i}, \*STDOUT) if $opts{i};
if ($opts{l}) {
    my $dir = dirname ($opts{l});
    $ARGV[0] = $opts{l};
    while (<>) {
	chomp;
        copy ($_, \*STDOUT) or copy ("$dir/$_", \*STDOUT) or warn "Copy of $_ failed: $!\n";
    }
}
