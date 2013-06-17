#!/usr/local/bin/perl -w
#  
#  Delete the old jobs from the database...
#

#  WORK IN PROGRESSS!!! Not Complete... !!!!

# -----------------------------------------------------------------------------

BEGIN {
    # add the current directory to @INC
    ($_lib = $0) =~ s|/[^/]+$||;
    unshift @INC, $_lib;
}

use Getopt::Std;            # access to the command line
use DBI;                    # access to the database
use strict;
use vars qw/ $opt_h $opt_v $opt_p $opt_d $opt_u $opt_c $opt_e /;

# -----------------------------------------------------------------------------
# small configuration section
# -----------------------------------------------------------------------------
my $helptext="
  Usage:   soaplab-cleaner.pl [options] 

   where options are

   -c <password>              (no default)
   -d <database>              (default: soaplab)
   -u <user>                  (default: soaplab)
   -e <expire-time-in-hours>  (default: 72)
   -h <hostname>              (default: localhost)
   -p <port>                  (default: 3306)

   -v ... verbose mode        (default: no)
";

# -----------------------------------------------------------------------------
# main program
# -----------------------------------------------------------------------------

if ( $ARGV[0] eq "" ) {
    print $helptext;
    exit;
}

# --- process the command line
getopts ('d:u:p:e:c:h:p:v');

my $hostname = ( $opt_h ? $opt_h : 'localhost' );
my $port     = ( $opt_p ? $opt_p : '3306' );
my $database = ( $opt_d ? $opt_d : 'soaplab' );
my $user     = ( $opt_u ? $opt_u : 'soaplab' );
my $passwd   = $opt_c if $opt_c;
my $verbose  = ($opt_v ? 1 : 0);
my $expired  = ( $opt_e ? $opt_e : '72' );
my $expireSecs = 60 * 60 * $expired;

# --- set the statements for accessing the database

my $dbh = DBI->connect ("DBI:mysql:soaplab;mysql_read_default_group=client;
              mysql_read_default_file=/homes/senger/.my.cnf", $user, $passwd) ||
    die ("Can't connect to $database/$user/$passwd");


#} else {
#    $dbh = DBI->connect ("dbi:mysql:$database:$hostname:$port") || 
#	die ("Can't connect to $database");
#}

print "Connected to $database/$user; expire = $expired\n";

__END__
$dbh = DBI->connect("DBI:mysql:$database:$hostname",
                          $user, $password);
      $dbh = DBI->connect("DBI:mysql:$database:$hostname:$port",
                          $user, $password);

# --- prepare statements
# --- select jobs to be deleted
my ($sthSJ) = $dbh->prepare ("SELECT id FROM jobs WHERE unix_timestamp(last_accessed) < unix_timestamp(now()) - ?"); 
# --- delete selected job
my ($sthDJ) = $dbh->prepare ("DELETE FROM jobs WHERE id = ?"); 
# --- delete other results
my ($sthDOR) = $dbh->prepare ("DELETE FROM other_results WHERE result_id = ?");     
# --- select the results to be deleted
my ($sthSR) = $dbh->prepare ("SELECT id, other_results FROM results WHERE job_id = ?"); 
# --- delete other results
my ($sthDR) = $dbh->prepare ("DELETE FROM results WHERE job_id = ?");

# --- first select the jobs to be deleted
$sthSJ->execute ($expireSecs);
# --- loop over all jobs to be deleted
while ( @rowSJ = $sthSJ->fetchrow_array ) {
    print "Job: " . join ('; ', @rowSJ) . "\n" if $verbose;
    # --- select results to the specified job
    print "looking for the results for: $rowSJ[0]\n" if $verbose;
    $sthSR->execute ($rowSJ[0]);
    while ( @rowSR = $sthSR->fetchrow_array ) {
	print "Results: " . join (': ', @rowSR) . "\n" if $verbose;
	# --- delete their 'other' results (if they exist)  
	if ( $rowSR[1] eq 'y' ) {
	    $sthDOR->execute ($rowSR[0]);
	    $rc = $sthDOR->rows;
	    print "$rc row(s) from other results deleted\n" if $verbose;
	}
    }
    # --- delete results
    $sthDR->execute ($rowSJ[0]);
    if ($verbose) {
	$rc = $sthDR->rows;
	print "$rc row(s) from results deleted\n";
    }
    # --- delete job
    $sthDJ->execute ($rowSJ[0]);
    if ($verbose) {
	$rc = $sthDR->rows;
	print "$rc row(s) from jobs deleted\n";
    }
}

# --- disconnect from the database
$dbh->disconnect;


__END__

