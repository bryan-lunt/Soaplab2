###  -- -*-Perl-*-
#
#    Subroutines used by generators.
#
#    senger@EBI.ac.uk
#    January, 1998
#
# $Id: al.lib.pl,v 1.2 2007/03/23 10:21:25 marsenger Exp $
#--------------------------------------------------------------------------

package l;

# -----------------------------------------------------------------------------
# Return a list of all available applications, or empty array if an error occured.
# If $tolower is not empty then application names are converted to lower cases.
# -----------------------------------------------------------------------------
sub applist {
    my ($tolower) = @_;
    local ($_);
    my ($try_again);
    my ($key, $value, @app);
    my ($app_menu, $disabled);
    foreach (split (/\s*[, ]\s*/, $Cfg::DISABLED_APPS)) {
        $disabled{"\L$_"} = 1;
    }
    foreach $app_menu (split (/\s*[, ]\s*/, $Cfg::APPMENU)) {
        open (APPMENU, $app_menu) || return ();
        while (<APPMENU>) {
            last if /^\s*\.\.\s*$/;   # skip until '..'
            $try_again = 1 if eof();
        }
        seek (APPMENU, 0, 0) if $try_again;

        while (<APPMENU>) {
    	    chomp;
            s/^\s*//; s/\s*$//; # ignore leading and trailing ws
            next if /^!/;	# ignore comments
	    next if /^\s*$/;    # ignore blank lines
	    ($key, $value) = /^(\w+)\s*(.*)/;
	
	    if ($key =~ /PROG/io) {
                unless ($disabled{"\L$value"}) {
	            $value =~ tr/A-Z/a-z/ if $tolower;
	            push (@app, $value);
		}
	    } 
        }
        close (APPMENU);
    }
    return (@app);
}
   
#------------------------------------------------------------------------------
# Return $file if $file exists, otherwise searches all directories given in
# $dirs (separated by commmas) to find the $file. On sucess returns path of the
# found file, otherwise return ''.
# -----------------------------------------------------------------------------
sub find_file {
    my ($file, $dirs) = @_;
    return $file if -e $file;
    my (@dirs) = split (/\s*,\s*/, $dirs);
    my ($path);
    foreach (@dirs) {
        $path = "$_/$file";
        return $path if -e $path;
    }
    return '';
}

#------------------------------------------------------------------------------
# find default config file name, return its full name with path ($opt_f is a
# name of a wanted config file given on the command line)
#------------------------------------------------------------------------------
sub find_default_config {
    my ($opt_f) = @_;
    my ($def_config) = $Cfg::DEFCFG if -e $Cfg::DEFCFG;
    if ($opt_f) {
        if (-e $opt_f) {
            $def_config = $opt_f;
	} else {
            my ($dir);
            my (@dirs) = split (/\s*,\s*/, $Cfg::ALCFG);
            foreach $dir (@dirs) {
                if (-e "$dir/$opt_f") {
                    $def_config = "$dir/$opt_f";
		    last;
                }
	    }
	}
    }
    return $def_config;
}

#------------------------------------------------------------------------------
# Return 1 if $file has a path attached, otherwise 0.
#------------------------------------------------------------------------------
sub is_path_attached {
    my ($file) = shift;
    return $file =~ m#^(/|\./|\.\./)#;
}

# ----------------------------------------------------------------------------
1;


__END__
