###  -- -*-Perl-*-
#
#    A back-end for al.2xml.pl converter.
#    It produces the XML output from the internal structures
#    created somewhere else (accessible in the global $::app).
#
#    senger@EBI.ac.uk
#    May 2000
#
# $Id: al.XMLOutput.pl,v 1.15 2010/08/04 12:28:37 mahmutuludag Exp $
#--------------------------------------------------------------------------

package XMLOutput;

$INDENT = 4;   # one level of indentation (in the generated output)

# --- some globals used in subroutines of 'create'
%section;
%event;
%Input_Names;   # for checking uniqueness of input parameter names
%Result_Names;  # for checking uniqueness of result names

# -----------------------------------------------------------------------------
#
# Create an XML output file (this is the main job of this package)
#    'this_filename' is the name of the created output - used just in comments
#    (the file being created follows AppLabAnalysis.dtd).
#
# -----------------------------------------------------------------------------
sub create {
    my ($this_filename) = shift;

    # --- extract and save pieces of global 'app'
    my ($r_array) = $::app{'sections'};
    %section = %{$$r_array[0]};

    $r_array = $section{'event'};
    %event = %{$$r_array[0]};


#    &split_file_parameters ($section{'mainq'});
#    &split_file_parameters ($section{'optq'});

   # --- and create an XML document
    return <<"END_OF_TEMPLATE";
@{[ &Header ($this_filename) ]}
<DsLSRAnalysis>
@{[ &Analysis (1) ]}
</DsLSRAnalysis>
END_OF_TEMPLATE
}

# -----------------------------------------------------------------------------
# XML output header
# -----------------------------------------------------------------------------
sub Header {
    my ($name) = @_;   # application name
    my ($date) = `date`;  chomp $date;

    my ($pure_bsa);
    $pure_bsa = ' (without AppLab specific extensions)' if $::opt_b;

    my ($dtd) = "<!DOCTYPE DsLSRAnalysis SYSTEM \"$Cfg::DTD\">";
    undef $dtd unless $Cfg::DTD;
    if ($::opt_b and $Cfg::BSA_DTD) {
        $dtd = "<!DOCTYPE DsLSRAnalysis SYSTEM \"$Cfg::BSA_DTD\">";
    }
    return <<"END_OF_TEMPLATE";
<?xml version = "1.0"?>
$dtd

<!--
    $name$pure_bsa

    $Cfg::NAME
    Version $Cfg::VERSION
    Contact: $Cfg::AUTHOR <$Cfg::EMAIL>

    ORIGINATED FROM:
        This file was generated from a plain config file.
        $date
-->
END_OF_TEMPLATE
}

# -----------------------------------------------------------------------------
# split 'file' parameter into several (if requested)
# -----------------------------------------------------------------------------
#sub split_file_parameters {
#    my ($r_array) = @_;
#
#    my @iodata = ();
#
#    foreach (@$r_array) {
#	next unless $$_{'qtype'} eq 'file' and $$_{'ioformat'} eq 'unspecified';
#
#	my (%direct) = %$_;
#	$direct{'qid'} .= '_direct_data';
#	$direct{'ioformat'} = 'direct';
#	$direct{'answer'} = 'false';
#	delete $direct{'default'} if &is ($options{'default_for_direct'}) eq 'false';
#	push (@iodata, \%direct);
#
#	$$_{'qid'} .= '_url';
#	$$_{'ioformat'} = 'url';
#	$$_{'answer'} = 'false';
#	delete $$_{'default'} if &is ($options{'default_for_direct'}) eq 'true';
#
#    }
#    push (@$r_array, @iodata);
#}

# -----------------------------------------------------------------------------
# tag 'analysis'
# -----------------------------------------------------------------------------
sub Analysis {
    my ($indent) = shift;
    my ($_i, $_iplus) = &ind ($indent);

    %Input_Names = ();
    %Result_Names = ();

    my ($extension) = <<"END_OF_TEMPLATE";
$_iplus<analysis_extension>
@{[ &Extension ($indent+2) ]}$_iplus</analysis_extension>
END_OF_TEMPLATE
    undef $extension if $::opt_b;

    %Input_Names = ();
    %Result_Names = ();

    return <<"END_OF_TEMPLATE";
$_i<analysis
@{[ &Analysis_attributes ($indent+1) ]}$_iplus>
@{[ &one_tag ($indent+1, 'description', $::app{'description'}) ]}
@{[ &Input ($indent+1) ]}
@{[ &Output ($indent+1) ]}
$extension
$_i</analysis>
END_OF_TEMPLATE
}

sub Analysis_attributes {
    my ($indent) = shift;
    my ($doc);

    $doc .= &one_attr ($indent, 'name', $::app{'name'});
    $doc .= &one_attr ($indent, 'type', $::app{'apptype'});
    $doc .= &one_attr ($indent, 'version', $::app{'version'});

    my (%options) = %{$::app{'options'}};
    $doc .= &one_attr ($indent, 'supplier', $options{'supplier'});
    $doc .= &one_attr ($indent, 'installation', $options{'installation'});
    delete ${$::app{'options'}}{'supplier'};
    delete ${$::app{'options'}}{'installation'};

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'input'
#    Generate only for those parameter which are allowed to be displayed and
#    those who are not 'group' parameters (but do generate their sub-nested
#    elements, and also create non-existing elements for parameter 'file' -
#    for direct and undirect data).
#
#    TBD: allowed(*)
# -----------------------------------------------------------------------------
sub Input {
    my ($indent) = shift;
    my ($doc);

    $doc .= &inputs ($indent, $section{'mainq'});
    $doc .= &inputs ($indent, $section{'optq'});
    return $doc;
}

sub inputs {
    my ($indent, $r_array) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, %hints, %options);

    foreach (@$r_array) {
	if ($Input_Names{ $$_{'qid'} }) {
	    print STDERR 'Duplicated input name (' . $$_{'qid'} . ') in ' . $::app{'name'} . "\n";
	    next;
	} else {
	    $Input_Names{ $$_{'qid'} } = 1;
	}
	%hints = %{$$_{'hints'}};
	%options = %{$$_{'options'}};
	next if $hints{'nodisplay'} and &is ($hints{'nodisplay'});

        # --- an 'unknown' type gives indication what to put in 'inputs'
        #     in key 'bsa_inputs'
        if (defined $$_{'bsa_inputs'}) {
#        if ($$_{'qtype'} eq 'unknown') {
	    $doc .= &inputs ($indent, $$_{'bsa_inputs'});
	    next;
	}

        # --- ignore the parent of a group, but do it for children
        if ($$_{'nested'}) {
	    $doc .= &inputs ($indent, $$_{'nested'});
            next;
	}

        # --- split 'file' parameter into several (if requested)
	if ($$_{'qtype'} eq 'file' and $$_{'ioformat'} eq 'unspecified') {
	    my (%direct) = %$_;
	        $direct{'qid'} .= '_direct_data';
                $direct{'ioformat'} = 'direct';
	        $direct{'answer'} = 'false';
	        delete $direct{'default'} if &is ($options{'default_for_direct'}) eq 'false';
	    my (%filename) = %$_;
	        $filename{'qid'} .= '_url';
                $filename{'ioformat'} = 'url';
	        $filename{'answer'} = 'false';
	        delete $filename{'default'} if &is ($options{'default_for_direct'}) eq 'true';
	    $doc .= &inputs ($indent, [ \%direct, \%filename ]);
            next;
	}

        # --- and finally a normal parameter :-)
	$$_{'default'} = &is ($$_{'default'}) if $$_{'qtype'} eq 'bool';
        my ($default) = &one_tag ($indent+1, 'default', $$_{'default'});
        $default .= "\n" if $default;

        # --- it may have 'allowed' values
	my ($allowed);
        if ($hints{'readonly'}) {
	    my ($r_list, $r_item, $tmp);
	    my ($r_array_list) = $$_{'lists'};
	    if ($r_array_list and $$_{'qtype'} =~ /^text|range|file|dummy$/) {
		foreach $r_list (@$r_array_list) {
		    next if $$r_list{'type'} eq 'slice';   # we want here only 'full' lists
		    foreach $r_item (@{$$r_list{'items'}}) {
			next if $$r_item{'type'} eq 'separator' or $$r_item{'type'} eq 'node';
			$tmp = &one_tag ($indent+1, 'allowed', $$r_item{'value'});

			# add 'shown_as' as a comment (which is not much but at least something)
			$tmp .= '  ' . &comment ($$r_item{'shown_as'})
			    if ($tmp && defined $$r_item{'shown_as'});

			$allowed .= $tmp . "\n" if $tmp;
		    }
		}
	    }
	}

	$doc .= <<"END_OF_TEMPLATE";
$_i<input
@{[ &Input_attributes ($indent+1, $_) ]}$_iplus>
$default$allowed$_i</input>
END_OF_TEMPLATE
    }

    return $doc;
}

sub Input_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'name', $$r_hash{'qid'});

    my (%options) = %{$$r_hash{'options'}};
    my ($bsa_type) = $options{'bsa_type'};
    unless ($bsa_type) {

#	# TBD: add 'datatype' to the real type if the datatype has some
#	#      semantic value
#	my ($datatype) = $options{'datatype'};
#	$datatype = undef if $datatype =~ /outfile|datafile|input|graph/;

	if    ($$r_hash{'qtype'} eq 'bool')        { $bsa_type = 'boolean'; }
	elsif ($options{'datatype'} eq 'filelist') { $bsa_type = 'byte[][]'; }
	else                                       { $bsa_type = 'string'; }
    }
    $doc .= &one_attr ($indent, 'type', $bsa_type);
    $doc .= &one_attr ($indent, 'mandatory', &is ($$r_hash{'answer'}));

    return $doc;
}

sub comment {
    my ($comment) = @_;
    return '' unless $comment;
    $comment =~ s/<!--/ /g; $comment =~ s/-->/ /g;
    return '<!-- ' . $comment . ' -->';
}

# -----------------------------------------------------------------------------
# tag 'output'
#    Generate 'output' tags representing results. Each output parameter can
#    contain several results (of various types). Also the whole application
#    has a general results 'report' and 'detailed_status'.
# -----------------------------------------------------------------------------
sub Output {
    my ($indent) = shift;
    my ($doc);
    $doc .= &one_result ($indent, { name=>'report' });
    $doc .= &one_result ($indent, { name=>'detailed_status', type=>'long' });
    foreach (@{$section{'mainq'}}, @{$section{'optq'}}) {

	foreach my $result (@{$$_{'results'}}) {
	    if ($Result_Names{ $$result{'name'} }) {
		print STDERR 'Duplicated output name ' . $$result{'name'} . ' in ' . $::app{'name'} . "\n";
		next;
	    } else {
		$Result_Names{ $$result{'name'} } = 1;
	    }
            $doc .= &one_result ($indent, $result);
	}
    }
    return $doc;
}

sub one_result {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, $result);

    $doc .= <<"END_OF_TEMPLATE";
$_i<output
@{[ &Output_attributes ($indent+1, $r_hash) ]}$_iplus>
$_i</output>
END_OF_TEMPLATE

    return $doc;
}

sub Output_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'name', $$r_hash{'name'});

    #my (%options) = %{$$r_hash{'options'}};    
    #if ($options{'datatype'} eq 'filelist')
    if ($$r_hash{'name'} eq 'outdir')
    {
    	 $doc .= &one_attr ($indent, 'type', 'byte[][]');
    }
    else
    {
    	 $doc .= &one_attr ($indent, 'type', ($$r_hash{'type'} ? $$r_hash{'type'} : 'string'));    	
    }

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'extension'
#    Generate all AppLab-specific metadata...
# -----------------------------------------------------------------------------
sub Extension {
    my ($indent) = shift;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    $::app{'style'} = $section{'style'};

    %Input_Names = ();

    $doc .= <<"END_OF_TEMPLATE";
$_i<app_info
@{[ &AppInfo_attributes ($indent+1) ]}$_iplus/>
$_i<event
@{[ &Event_attributes ($indent+1) ]}$_iplus>
@{[ &Actions ($indent+1, $event{'action'}) ]}
@{[ &Conditions ($indent+1, $event{'condition'}) ]}$_i</event>
@{[ &Parameters ($indent) ]}
@{[ &Options ($indent, \%::app) ]}
END_OF_TEMPLATE
    return $doc;
}

sub AppInfo_attributes {
    my ($indent) = shift;
    my ($doc);

    $doc .= &one_attr ($indent, 'category',      $::app{'module'});
    $doc .= &one_attr ($indent, 'contact',       $::app{'contact'});
    $doc .= &one_attr ($indent, 'copyright',     $::app{'copyright'});
    $doc .= &one_attr ($indent, 'creation_date', $::app{'created'});
    $doc .= &one_attr ($indent, 'relations',     $::app{'relation'});
    if ($::app{'help'}) {
	$doc .= &one_attr ($indent, 'help_URL',      $::app{'help'});
    } else {
#	if (defined $Cfg::HELP_ROOT) {
	if ($Cfg::HELP_ROOT) {
	    my ($helpfile) = "\L$::app{'name'}\E.html";
	    if (defined $Cfg::YA_HELP_ROOT and -e "$Cfg::YA_HELP_ROOT/$helpfile" or !defined $Cfg::YA_HELP_ROOT) {
		$doc .= &one_attr ($indent, 'help_URL', "$Cfg::HELP_ROOT/$helpfile");
	    }
	}
    }

    my (%options) = %{$::app{'options'}};
    $doc .= &one_attr ($indent, 'report_adaptor', $options{'report_adaptor'});
    delete ${$::app{'options'}}{'report_adaptor'};

    # no needed in the resulting XML
##    delete ${$::app{'options'}}{'emboss'};

    # change 'default' to 'use_defaults'
    if (exists $options{'defaults'}) {
	${$::app{'options'}}{'use_defaults'} = ${$::app{'options'}}{'defaults'};
	delete ${$::app{'options'}}{'defaults'};
    }

    return $doc;
}

sub Event_attributes {
    my ($indent) = shift;
    my ($doc);

    $doc .= &one_attr ($indent, 'id', $event{'id'});

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'action'
# -----------------------------------------------------------------------------
sub Actions {
    my ($indent, $r_array) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    foreach (@$r_array) {
	$doc .= <<"END_OF_TEMPLATE";
$_i<action
@{[ &Action_attributes ($indent+1, $_) ]}$_iplus>
@{[ &Options ($indent+1, $_) ]}$_i</action>
END_OF_TEMPLATE
    }
    return $doc;
}

sub Action_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'type',     $$r_hash{'atype'});
    $doc .= &one_attr ($indent, 'file',     $$r_hash{'exec'});
    $doc .= &one_attr ($indent, 'launcher', $$r_hash{'launcher'});
    $doc .= &one_attr ($indent, 'method',   $$r_hash{'method'});

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'conditions'
# -----------------------------------------------------------------------------
sub Conditions {
    my ($indent, $r_array) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, %cond);

    foreach (@$r_array) {
	%cond = %$_;
	$doc .= <<"END_OF_TEMPLATE";
$_i<condition
@{[ &Condition_attributes ($indent+1, $_) ]}$_iplus>
@{[ &Options ($indent+1, $_) ]}$_i</condition>
END_OF_TEMPLATE
    }
    return $doc;
}

sub Condition_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'evaluator', $$r_hash{'evaluator'});
    $doc .= &one_attr ($indent, 'regexp',    $$r_hash{'regexp'});
    $doc .= &one_attr ($indent, 'boolexp',   $$r_hash{'boolexp'});
    $doc .= &one_attr ($indent, 'owner',     $$r_hash{'owner'});
    $doc .= &one_attr ($indent, 'verlevel',  $$r_hash{'verlevel'});

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'options'
#    Given 'r_hash' can contain 'options', 'privoptions', 'style' and 'hints' -
#    and all of them are put into a result 'option' tag (with appropriate 'type').
# -----------------------------------------------------------------------------
sub Options {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_option_set ($indent, $$r_hash{'options'},     'normal');
    $doc .= &one_option_set ($indent, $$r_hash{'privoptions'}, 'private');
    $doc .= &one_option_set ($indent, $$r_hash{'hints'},       'style');
    $doc .= &one_option_set ($indent, $$r_hash{'style'},       'style');
    return $doc;
}

sub one_option_set {
    my ($indent, $r_hash, $type) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, $attrs, $value);

    # ignore the following keys (this is the last resort))
    my (%ignored) = ( bsa_type=>1 );  

    my (%options) = %{$$r_hash{'options'}};
    foreach (sort keys %$r_hash) {
        next if defined $ignored{$_};
	$value = $$r_hash{$_};
        $value = 'true' unless $value;
	$attrs  = &one_attr ($indent+1, 'name', $_);
	$attrs .= &one_attr ($indent+1, 'value', $value);
	$attrs .= &one_attr ($indent+1, 'type', $type);

	$doc .= <<"END_OF_TEMPLATE";
$_i<option
$attrs$_iplus/>
END_OF_TEMPLATE
    }
    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'repeatable'
# -----------------------------------------------------------------------------
sub Repeatable {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    my ($attrs) = &Repeatable_attributes ($indent+1, $r_hash);
    return "" unless $attrs;

    $doc .= <<"END_OF_TEMPLATE";
$_i<repeatable
$attrs$_iplus>
@{[ &one_option_set ($indent+1, $$r_hash{'repeatopt'}, 'style') ]}$_i</repeatable>
END_OF_TEMPLATE

    return $doc;
}
sub Repeatable_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'min', $$r_hash{'repeatmin'});
    $doc .= &one_attr ($indent, 'max', $$r_hash{'repeatmax'});

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'list'
# -----------------------------------------------------------------------------
sub Lists {
    my ($indent, $r_array) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    foreach (@$r_array) {
	$doc .= <<"END_OF_TEMPLATE";
$_i<list
@{[ &List_attributes ($indent+1, $_) ]}$_iplus>
@{[ &ListItems ($indent+1, $$_{'items'}) ]}$_i</list>
END_OF_TEMPLATE
    }
    return $doc;
}

sub List_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'name',  $$r_hash{'name'});
    $doc .= &one_attr ($indent, 'type',  $$r_hash{'type'});
    $doc .= &one_attr ($indent, 'slice', $$r_hash{'slice'});

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'list_item'
# -----------------------------------------------------------------------------
sub ListItems {
    my ($indent, $r_array) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    foreach (@$r_array) {
	$doc .= <<"END_OF_TEMPLATE";
$_i<list_item
@{[ &ListItem_attributes ($indent+1, $_) ]}$_iplus/>
END_OF_TEMPLATE
    }
    return $doc;
}

sub ListItem_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'value',    $$r_hash{'value'});
    $doc .= &one_attr ($indent, 'type',     $$r_hash{'type'});
    $doc .= &one_attr ($indent, 'shown_as', $$r_hash{'shown_as'});
    $doc .= &one_attr ($indent, 'level',    $$r_hash{'level'});

    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'parameter'
#    This is like the parameters are stored in the XML:
#
#    <parameter>               <!-- basic parameter ('bool' type) -->
#       <base>....</base>      <!-- general properties -->
#    </parameter>
#
#    <parameter>               <!-- standard, 'text' parameter -->
#       <base>....</base>
#       <standard>...</standard>
#    </parameter>
#
#    <parameter>               <!-- range parameter -->
#       <base>....</base>
#       <range>...</range>
#    </parameter>
#
#    <parameter>               <!-- input/output 'file' parameter -->
#       <base>....</base>
#       <data>...</data>
#    </parameter>
#
#    <parameter>               <!-- choice, 'radio' parameter -->
#       <base>....</base>
#       <choice>
#           <base>...</base>   <!-- nested 'bool' parameters -->
#           <base>...</base>
#           ...
#       </choice>
#    </parameter>
#
#    <parameter>               <!-- choice list, 'group' parameter -->
#       <base>....</base>
#       <choice_list>
#           <base>...</base>   <!-- nested 'bool' parameters -->
#           <base>...</base>
#           ...
#       </choice_list>
#    </parameter>
#
# -----------------------------------------------------------------------------
sub Parameters {
    my ($indent) = shift;
    my ($doc);

    $doc .= &parameters ($indent, $section{'mainq'});
    $doc .= &parameters ($indent, $section{'optq'});
    return $doc;
}

sub parameters {
    my ($indent, $r_array) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, $spec);

    foreach (@$r_array) {

	if ($Input_Names{ $$_{'qid'} }) {
	    next;
	} else {
	    $Input_Names{ $$_{'qid'} } = 1;
	}

        undef $spec;
	$spec = &Standard_parameter ($indent+1, $_)    if $$_{'qtype'} eq 'text';
	$spec = &Range_parameter ($indent+1, $_)       if $$_{'qtype'} eq 'range';
	$spec = &File_parameter ($indent+1, $_)        if $$_{'qtype'} eq 'file';
	$spec = &File_parameter ($indent+1, $_)        if $$_{'qtype'} =~ /^seq|seqsetall$/;
	$spec = &Choice_parameter ($indent+1, $_)      if $$_{'qtype'} eq 'radio';
	$spec = &Choice_list_parameter ($indent+1, $_) if $$_{'qtype'} eq 'group';
	$doc .= <<"END_OF_TEMPLATE";
$_i<parameter>
@{[ &Base_parameter ($indent+1, $_) ]}
$spec$_i</parameter>
END_OF_TEMPLATE
    }
    return $doc;
}

# -----------------------------------------------------------------------------
# 'bool' / general properties for all parameter types
# -----------------------------------------------------------------------------
sub Base_parameter {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    $$r_hash{'default'} = &is ($$r_hash{'default'}) if $$r_hash{'qtype'} eq 'bool';

    my ($default) = &one_tag ($indent+1, 'default', $$r_hash{'default'});

    $default .= "\n" if $default;
    my ($prompt) = &one_tag ($indent+1, 'prompt', $$r_hash{'prompt'});
    $prompt .= "\n" if $prompt;
    my ($help) = &one_tag ($indent+1, 'help', $$r_hash{'help'});
    $help .= "\n" if $help;

    $doc .= <<"END_OF_TEMPLATE";
$_i<base
@{[ &Base_attributes ($indent+1, $r_hash) ]}$_iplus>
$default$prompt$help@{[ &Conditions ($indent+1, $$r_hash{'condition'}) ]}@{[ &Options ($indent+1, $r_hash) ]}$_i</base>
END_OF_TEMPLATE

    return $doc;
}

sub Base_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'ordering', $$r_hash{'ordering'});
    $doc .= &one_attr ($indent, 'name', $$r_hash{'qid'});
    $doc .= &one_attr ($indent, 'qualifier', $$r_hash{'name'});
    $doc .= &one_attr ($indent, 'method', $$r_hash{'method'});
    $doc .= &one_attr ($indent, 'mandatory', &is ($$r_hash{'answer'}));

    my (%options) = %{$$r_hash{'options'}};
    my ($bsa_type) = $options{'bsa_type'};
    unless ($bsa_type) {
	$bsa_type = ($$r_hash{'qtype'} eq 'bool' ? "boolean" : "string");
    }
    $doc .= &one_attr ($indent, 'type', $bsa_type);
    $doc .= &one_attr ($indent, 'input_adaptor', $options{'input_adaptor'});
    $doc .= &one_attr ($indent, 'tagsepar', $options{'tagsepar'});
    if (defined $options{'defaults'}) {
	$doc .= &one_attr ($indent, 'use_defaults', &is ($options{'defaults'}));
    }
    if (&is ($options{'envar'}) eq 'true') {
        $doc .= &one_attr ($indent, 'envar', 'true');
    }

    delete ${$$r_hash{'options'}}{'input_adaptor'};
    delete ${$$r_hash{'options'}}{'envar'};
    delete ${$$r_hash{'options'}}{'tagsepar'};
    delete ${$$r_hash{'options'}}{'defaults'};

    # this one must not be deleted because it is used also in producing
    # of BSA tags (so we ignore this option in '&one_option_set')
    ###delete ${$$r_hash{'options'}}{'bsa_type'};

    return $doc;
}

# -----------------------------------------------------------------------------
# 'text'
# -----------------------------------------------------------------------------
sub Standard_parameter {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    $doc .= <<"END_OF_TEMPLATE";
$_i<standard
@{[ &Standard_attributes ($indent+1, $r_hash) ]}$_iplus>
@{[ &Repeatable ($indent+1, $r_hash) ]}@{[ &Lists ($indent+1, $$r_hash{'lists'}) ]}$_i</standard>
END_OF_TEMPLATE

    return $doc;
}

sub Standard_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'format', $$r_hash{'format'});

    return $doc;
}

# -----------------------------------------------------------------------------
# 'range'
# -----------------------------------------------------------------------------
sub Range_parameter {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    $doc .= <<"END_OF_TEMPLATE";
$_i<range
@{[ &Range_attributes ($indent+1, $r_hash) ]}$_iplus>
@{[ &Repeatable ($indent+1, $r_hash) ]}@{[ &Lists ($indent+1, $$r_hash{'lists'}) ]}$_i</range>
END_OF_TEMPLATE

    return $doc;
}

sub Range_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'min',    $$r_hash{'hardmin'});
    $doc .= &one_attr ($indent, 'max',    $$r_hash{'hardmax'});
    $doc .= &one_attr ($indent, 'format', $$r_hash{'format'});

    return $doc;
}

# -----------------------------------------------------------------------------
# 'file'
# -----------------------------------------------------------------------------
sub File_parameter {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, $results);

    foreach (@{$$r_hash{'results'}}) {
	$results .= &Result ($indent+1, $_);
    }

    $doc .= <<"END_OF_TEMPLATE";
$_i<data
@{[ &File_attributes ($indent+1, $r_hash) ]}$_iplus>
$results@{[ &Repeatable ($indent+1, $r_hash) ]}@{[ &Lists ($indent+1, $$r_hash{'lists'}) ]}$_i</data>
END_OF_TEMPLATE

    return $doc;
}

sub File_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    my ($filetype) = $$r_hash{'filetype'};
    $filetype = $$r_hash{'iotype'}
        unless $filetype =~ /^input|stdin|output|stdout|stderr$/;

    $doc .= &one_attr ($indent, 'iotype',  $filetype);
    $doc .= &one_attr ($indent, 'ioformat',  $$r_hash{'ioformat'});
    $doc .= &one_attr ($indent, 'mimetype',  $$r_hash{'mimetype'});
    $doc .= &one_attr ($indent, 'extension', $$r_hash{'extension'});

    return $doc;
}

sub Result {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, $result);

    $doc .= <<"END_OF_TEMPLATE";
$_i<result
@{[ &Result_attributes ($indent+1, $r_hash) ]}$_iplus>
@{[ &Options ($indent+1, $r_hash) ]}$_i</result>
END_OF_TEMPLATE

    return $doc;
}

sub Result_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'name', $$r_hash{'name'});
    if ($$r_hash{'name'} eq 'outdir')
    {
         $doc .= &one_attr ($indent, 'type', 'byte[][]');
    }
    else
    {
    $doc .= &one_attr ($indent, 'type', ($$r_hash{'type'} ? $$r_hash{'type'} : 'string'));
    }
    $doc .= &one_attr ($indent, 'output_adaptor', $$r_hash{'adaptor'});
    $doc .= &one_attr ($indent, 'special_type',   $$r_hash{'special_type'});

    return $doc;
}

# -----------------------------------------------------------------------------
# 'radio'
# -----------------------------------------------------------------------------
sub Choice_parameter {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, $children);

    foreach (@{$$r_hash{'nested'}}) {
        $children .= &Base_parameter ($indent+1, $_);
    }

    $doc .= <<"END_OF_TEMPLATE";
$_i<choice
@{[ &Choice_attributes ($indent+1, $r_hash) ]}$_iplus>
$children$_i</choice>
END_OF_TEMPLATE

    return $doc;
}

sub Choice_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'radiotype', $$r_hash{'radiotype'});

    return $doc;
}

# -----------------------------------------------------------------------------
# 'group'
# -----------------------------------------------------------------------------
sub Choice_list_parameter {
    my ($indent, $r_hash) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc, $children);

    foreach (@{$$r_hash{'nested'}}) {
        $children .= &Base_parameter ($indent+1, $_);
    }

    $doc .= <<"END_OF_TEMPLATE";
$_i<choice_list
@{[ &Choice_list_attributes ($indent+1, $r_hash) ]}$_iplus>
$children$_i</choice_list>
END_OF_TEMPLATE

    return $doc;
}

sub Choice_list_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    $doc .= &one_attr ($indent, 'grouptype', $$r_hash{'grouptype'});
    $doc .= &one_attr ($indent, 'separator', $$r_hash{'separator'});

    return $doc;
}

# -----------------------------------------------------------------------------
#
# Create a list of generated analysis (again as an XML output file)
#    (it follows AppLabAdmin.dtd).
#    It takes data from a global %::prg_list (use option '-D' to se
#    how this structure looks like).
#
# -----------------------------------------------------------------------------
sub create_list {

    return <<"END_OF_TEMPLATE";
@{[ &Header_list ]}
<Analyses>
@{[ &Modules (1) ]}</Analyses>
END_OF_TEMPLATE

}

# -----------------------------------------------------------------------------
# XML list output header
# -----------------------------------------------------------------------------
sub Header_list {
    my ($date) = `date`;  chomp $date;

    return <<"END_OF_TEMPLATE";
<?xml version = "1.0"?>
<!DOCTYPE Analyses [
<!ELEMENT Analyses (category*)>

<!ELEMENT category (app+)>
<!ATTLIST category
    name     CDATA #REQUIRED>

<!ELEMENT app EMPTY>
<!ATTLIST app
    name     CDATA #REQUIRED
    module   CDATA "sowa"
    xml      CDATA #IMPLIED
    desc     CDATA #IMPLIED>
]>

<!--
    $Cfg::NAME
    Version $Cfg::VERSION
    Contact: $Cfg::AUTHOR <$Cfg::EMAIL>

    This file was generated: $date

    This file contains a list of applications which are available
    from a Soaplab2 server.
-->
END_OF_TEMPLATE
}

# -----------------------------------------------------------------------------
# tag 'category'
# -----------------------------------------------------------------------------
sub Modules {
    my ($indent) = shift;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    foreach (sort keys %::prg_list) {
	$doc .= <<"END_OF_TEMPLATE";
$_i<category
@{[ &one_attr ($indent+1, 'name', $_) ]}$_iplus>
@{[ &Apps ($indent+1, $::prg_list{$_}) ]}$_i</category>
END_OF_TEMPLATE
    }
    return $doc;
}

# -----------------------------------------------------------------------------
# tag 'app'
# -----------------------------------------------------------------------------
sub Apps {
    my ($indent, $r_array) = @_;
    my ($_i, $_iplus) = &ind ($indent);
    my ($doc);

    foreach (@$r_array) {
	$doc .= <<"END_OF_TEMPLATE";
$_i<app
@{[ &App_attributes ($indent+1, $_) ]}$_iplus/>
END_OF_TEMPLATE
    }
    return $doc;
}

sub App_attributes {
    my ($indent, $r_hash) = @_;
    my ($doc);

    my ($start) = ($$r_hash{'start'} or 'yes');
    $start = 'no' unless $start =~ /^ref|no|yes$/;

    $doc .= &one_attr ($indent, 'name',    $$r_hash{'name'});
    $doc .= &one_attr ($indent, 'xml',     $$r_hash{'xml'});
    $doc .= &one_attr ($indent, 'desc',    $$r_hash{'desc'});
    $doc .= &one_attr ($indent, 'module',  $$r_hash{'class'});

    return $doc;
}

# -----------------------------------------------------------------------------
#
# Common subroutines
#
# -----------------------------------------------------------------------------

# -----------------------------------------------------------------------------
# returns 1 or 0, depending on the given parameter
# -----------------------------------------------------------------------------
sub is { $_[0] =~ /1|true|yes|\+/i ? 'true' : 'false'; }
sub isempty { !defined $_[0] or $_[0] =~ /^\s*$/; }

# -----------------------------------------------------------------------------
# return a formatted attribute named 'dest_name' containing 'value'
# -----------------------------------------------------------------------------
sub one_attr {
    my ($indent, $dest_name, $value) = @_;
    return "" unless defined $value;
    my ($_i) = ' ' x ($indent * $INDENT);
    return "$_i$dest_name = " . '"' . &esc_attr ($value) . '"' . "\n";
}

# -----------------------------------------------------------------------------
# return a tag 'tag_name' (without any attributes attached) containing 'value'
# -----------------------------------------------------------------------------
sub one_tag {
    my ($indent, $tag_name, $value) = @_;
    return "" if &isempty ($value);
    my ($_i) = ' ' x ($indent * $INDENT);
    return "$_i<$tag_name>" . &esc ($value) . "</$tag_name>";
}

# -----------------------------------------------------------------------------
# escape characters which must be escaped in ab XML file
# -----------------------------------------------------------------------------
sub esc {
    local ($_) = @_;
    s/&/&amp;/g;
    s/</&lt;/g;
    s/>/&gt;/g;
    $_;
}

# -----------------------------------------------------------------------------
# as 'esc' plus quote characters escaped
# -----------------------------------------------------------------------------
sub esc_attr {
    local ($_) = &esc (@_);
    s/"/&quot;/g;  # quote " inside attribute value
    $_;
}

# -----------------------------------------------------------------------------
# convert level 'indent' (which is a number) into proper number of spaces;
# return this indentation and, for convenience, also one deeper indentation:
#    my ($i, $iplus) = &ind (3);
# -----------------------------------------------------------------------------
sub ind {
    my ($indent) = shift;
    return (' ' x ($indent * $INDENT),
            ' ' x (($indent+1) * $INDENT));
}

# ----------------------------------------------------------------------------
1;


__END__
