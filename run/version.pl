#!/usr/bin/perl -w
#
# Prints the Perl's version.
# This is used to check that Perl is available.
#
# $Id: version.pl,v 1.1 2007/10/07 09:57:45 marsenger Exp $
# ---------------------------------------------
use strict;
use warnings;
printf "Perl version is v%vd\n", $^V;
