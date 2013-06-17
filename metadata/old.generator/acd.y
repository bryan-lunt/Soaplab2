/*  -*- Perl -*-

    Parsing *.acd files of EMBOSS

    This file is a grammar description of EMBOSS configuration files.
    For generating perl code for parsing is used 'byacc' with -P option,
    or directly pbyacc (which does the same). Here is how to call it

       % pbyacc -P ACDParser -b acd acd.y

    And here is how to use the generated code (created as file ACDParser.pm):

        use ACDParser;
        use Fstream;
    
        $input = Fstream->new (\*STDIN, 'STDIN');
        $parser = ACDParser->new (\&ACDParser::yylex, \&ACDParser::yyerror, 0);

        $parser->yyparse ($input);

    The script shown above will read an 'acd' file on standard input.
    Note that Fstream is just one way how to read input file - the parser
    can get the data in any other form (like a string, for example).

    senger@EBI.ac.uk and p.ernst@dkfz-heidelberg.de
    October 2000

    $Id: acd.y,v 1.10 2011/06/28 12:51:07 mahmutuludag Exp $
*/

%start acd_file

%token ADDITIONAL AEXTENSION AFORMAT AGLOBAL ALIGNMENT ALIGNED
%token ANAME AOPENFILE APPEND APPLICATION AUSASHOW AWIDTH ARRAY
%token BATCH BORDER BOOLEAN BUTTON
%token CASESENSITIVE CHARACTERS CODE CODEDELIMITER CODON COMMENT
%token CONTINOUS CORBA CREATE CPU
%token DATA_TYPES DATAFILE DEFAULT DELIMITER DIRECTORY DIRECTORY_ATTR DIRLIST 
%token DISCRETESTATES DISTANCES DOCUMENTATION
%token ENDSECTION ENTRY EXECUTABLE EXPECTED EXTENSION EXTERNAL EMBASSY
%token FAILRANGE FEATOUT FEATURES FILELIST FOLDER FLOAT FREQUENCIES
%token GDESC GENEDATA GOUTFILE GRAPH GROUPS GSUBTITLE GTITLE GUI GXTITLE GYTITLE
%token HEADER HELP
%token INCREMENT INFILE INFORMATION INTEGER
%token KNOWNTYPE
%token LENGTH LIST LOWER
%token MATRIX MATRIXF MAXIMUM MAXLENGTH MAXREADS MAXSEQS MINIMUM MINLENGTH MINSEQS
%token MINSIZE MINTAGS MISSING MISSVAL MULTIPLE
%token NAME NONEMBOSS NNAME NULLOK NULLDEFAULT
%token OBO OBSOLETE ODIRECTORY OFORMAT OFNAME OFFORMAT
%token OSEXTENSION OSSINGLE OUTCODON OUTDIR OUTFILE OUTPUTMODIFIER
%token OUTOBO OUTRESOURCE OUTTAXON OUTTEXT OUTURL
%token PARAMETER PATTERN PATTERN_ PMISMATCH PNAME PRECISION PROMPT PROPERTIES
%token PROTEIN QUALIFIER
%token RANGE REGEXP RELATIONS REPORT RESOURCE REXTENSION
%token RFORMAT RNAME ROPENFILE RSCORESHOW RUSASHOW
%token SASK SECTION SELECTION SEQALL SEQOUT SEQOUTALL SEQOUTSET SEQSET SEQSETALL
%token SEQUENCE SIDE SIZE STANDARD STRING STYLE SUM SUMTEST SUPPLIER
%token TAGLIST TAXON TEMPLATE TEXT TOGGLE TOLERANCE TREE TRUEMINIMUM TRYDEFAULT TYPE
%token UPPER URL
%token VALID VALUE VALUES VARIABLE VERSION
%token WITHIN WARNRANGE WORD
%token XYGRAPH

%token W2HUNKNOWN

%%

acd_file:	appl_def param_defs
	;

appl_def:	APPLICATION sep VALUE '[' appl_attrs ']'
                    { $::app{'exec'} = $3 unless defined $::app{'exec'};
                      $::app{'format'} = 'acd';
                    }
	;

appl_attrs:	/* empty */
	|	appl_attrs appl_attr
	;

appl_attr:	DOCUMENTATION  sep VALUE  { $::app{'description'} = $3; }
	|	GROUPS         sep VALUE  { $::app{'module'} = $3; }
	|	GUI            sep VALUE  { $::app{'term'} = 1 unless &is_yes($3); }
	|	BATCH          sep VALUE  { $::app{'batch'} = $3; }
	|	CPU            sep VALUE  { $::app{'cpu'} = $3; }
	|	EMBASSY        sep VALUE  { $::app{'embassy'} = $3; }
	|	EXECUTABLE     sep VALUE  { $::app{'exec'} = $3; }
	|	EXTERNAL       sep VALUE  { $::app{'required_external'} = $3; }
	|	NONEMBOSS      sep VALUE  { $::app{'non-emboss'} = 'Y' if &is_yes($3); }
	|	COMMENT        sep VALUE  { &add_option ('options', $3); }
	|	SUPPLIER       sep VALUE  { $::app {'supplier'} = $3; }
	|	VERSION        sep VALUE  { $::app {'version'} = $3; }
	|	RELATIONS      sep VALUE  { &add_relation ('options', $3); }
	|	OBSOLETE       sep VALUE  { $::app {'obsolete'} = $3; }
	;

param_defs:  	/* empty */
	|	param_defs param_def
	|	param_defs param_section
	;

param_section:  section_def param_defs endsection_def
        ;

section_def:    section     sep VALUE '['  section_attrs    ']' { &end_qual ($3); }
        ;

endsection_def: ENDSECTION  sep VALUE    { &end_section ($3); }
        ;

section:	SECTION     { &start_qual ('section'); }
/* section:	SECTION     { &start_qual ('label'); } */

param_def:	align   sep VALUE '['  alignment_attrs  ']' { &end_qual ($3); }
    |	array       sep VALUE '['  array_attrs      ']' { &end_qual ($3); }
    | 	boolean     sep VALUE '['  boolean_attrs    ']' { &end_qual ($3); }
	|	codon       sep VALUE '['  codon_attrs      ']' { &end_qual ($3); }
	|	datafile    sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	directory   sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	dirlist     sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	discretestates  sep VALUE '[' file_attrs    ']' { &end_qual ($3); }
	|	distances   sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
    |	featout     sep VALUE '['  features_attrs   ']' { &end_qual ($3); }
	|	features    sep VALUE '['  features_attrs   ']' { &end_qual ($3); }
	|	filelist    sep VALUE '['  flist_attrs      ']' { &end_qual ($3); }
	|	float       sep VALUE '['  float_attrs      ']' { &end_qual ($3); }
	|	frequencies sep VALUE '['  frequencies_attrs']' { &end_qual ($3); }
    |	graph       sep VALUE '['  graph_attrs      ']' { &end_qual ($3); }
	|	infile      sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	integer     sep VALUE '['  integer_attrs    ']' { &end_qual ($3); }
	|	list        sep VALUE '['  list_attrs       ']' { &end_list ($3); }
	|	matrix      sep VALUE '['  matrix_attrs     ']' { &end_qual ($3); }
	|	matrixf     sep VALUE '['  matrix_attrs     ']' { &end_qual ($3); }
	|	obo         sep VALUE '['  obo_attrs        ']' { &end_qual ($3); }
	|	outcodon    sep VALUE '['  outcodon_attrs   ']' { &end_qual ($3); }
	|	outdir      sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	outfile     sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	outobo      sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	outresource sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	outtaxon    sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	outtext     sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	outurl      sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
	|	pattern     sep VALUE '['  pattern_attrs    ']' { &end_qual ($3); }
	|	properties  sep VALUE '['  file_attrs       ']' { &end_qual ($3); }
    |	range       sep VALUE '['  range_attrs      ']' { &end_qual ($3); }
	|	report      sep VALUE '['  report_attrs     ']' { &end_qual ($3); }
	|	resource    sep VALUE '['  resource_attrs   ']' { &end_qual ($3); }
	|	selection   sep VALUE '['  selection_attrs  ']' { &end_list ($3); }
	|	sequence    sep VALUE '['  sequence_attrs   ']' { &end_sequence ($3, 1); }
	|	seqset      sep VALUE '['  sequence_attrs   ']' { &end_sequence ($3, 3); }
	|	seqsetall   sep VALUE '['  sequence_attrs   ']' { &end_sequence ($3, 3); }
	|	seqall      sep VALUE '['  sequence_attrs   ']' { &end_sequence ($3, 3); }
	|	seqout      sep VALUE '['  seq_out_attrs    ']' { &end_seqout ($3); }
	|	seqoutset   sep VALUE '['  seq_out_attrs    ']' { &end_seqout ($3); }
	|	seqoutall   sep VALUE '['  seq_out_attrs    ']' { &end_seqout ($3); }
	|	regexp      sep VALUE '['  regexp_attrs     ']' { &end_qual ($3); }
	|	string      sep VALUE '['  string_attrs     ']' { &end_qual ($3); }
	|	taxon       sep VALUE '['  taxon_attrs      ']' { &end_qual ($3); }
	|	text        sep VALUE '['  text_attrs       ']' { &end_qual ($3); }
	|	toggle      sep VALUE '['  toggle_attrs     ']' { &end_qual ($3); }
	|	tree        sep VALUE '['  tree_attrs       ']' { &end_qual ($3); }
	|	url         sep VALUE '['  url_attrs        ']' { &end_qual ($3); }
	|	VARIABLE    sep VALUE VALUE
	|	VARIABLE    sep VALUE sep VALUE
	|	xygraph     sep VALUE '['  xygraph_attrs    ']' { &end_qual ($3); }
	|	W2HUNKNOWN  sep VALUE '['  unknown_attrs    ']' /* unknown parameter */
	;

align:	ALIGNMENT   { &start_qual ('file'); &add_item ('datatype', 'alignment');
			                            &add_item ('filetype', 'output');   }
array:	   ARRAY       { &start_qual ('array');}
boolean:	   BOOLEAN     { &start_qual ('bool'); }
codon:		CODON       { &start_qual ('file'); &add_item ('datatype', 'codon');    }
datafile:	DATAFILE    { &start_qual ('file'); &add_item ('datatype', 'datafile'); }
directory:	DIRECTORY   { &start_qual ('file'); &add_item ('datatype', 'filelist');    }
dirlist:	DIRLIST     { &start_qual ('file'); &add_item ('datatype', 'directory');    }
discretestates:	DISCRETESTATES  { &start_qual ('file'); &add_item ('datatype', 'input');}
distances:	DISTANCES   { &start_qual ('file'); &add_item ('datatype', 'input');}
features:	FEATURES    { &start_qual ('file'); &add_item ('datatype', 'features'); }
featout:	   FEATOUT     { &start_qual ('text'); &add_item ('datatype', 'featout');  }
filelist:	FILELIST    { &start_qual ('file'); &add_item ('datatype', 'filelist'); }
float:		FLOAT       { &start_qual ('range'); &add_item ('numformat', 'float');  }
frequencies:FREQUENCIES { &start_qual ('file'); &add_item ('datatype', 'input');}
graph:		GRAPH       { &start_qual ('text'); &add_item ('datatype', 'graph');    }
infile:		INFILE      { &start_qual ('file'); &add_item ('datatype', 'input');    }
integer:	INTEGER     { &start_qual ('range'); &add_item ('numformat', 'int');    }
list:		LIST        { &start_qual ('list');  }
matrix:		MATRIX      { &start_qual ('file'); &add_item ('datatype', 'matrix');   }
matrixf:	MATRIXF     { &start_qual ('file'); &add_item ('datatype', 'matrixf');  }
obo:	    OBO         { &start_qual ('file'); &add_item ('datatype', 'obo'); }
outcodon:	OUTCODON    { &start_qual ('file'); &add_item ('datatype', 'outcodon');
			                            &add_item ('filetype', 'output');   }
outdir:	    OUTDIR      { &start_qual ('file'); &add_item ('datatype', 'filelist');
			                            &add_item ('filetype', 'output');   }
outfile:	OUTFILE     { &start_qual ('file'); &add_item ('datatype', 'outfile');
			                            &add_item ('filetype', 'output');   }
outobo:     OUTOBO      { &start_qual ('file'); &add_item ('datatype', 'outfile');
			                            &add_item ('filetype', 'output');   }
outresource:OUTRESOURCE { &start_qual ('file'); &add_item ('datatype', 'outfile');
			                            &add_item ('filetype', 'output');   }
outtaxon:   OUTTAXON    { &start_qual ('file'); &add_item ('datatype', 'outfile');
			                            &add_item ('filetype', 'output');   }
outtext:    OUTTEXT     { &start_qual ('file'); &add_item ('datatype', 'outfile');
			                            &add_item ('filetype', 'output');   }
outurl:     OUTURL      { &start_qual ('file'); &add_item ('datatype', 'outfile');
			                            &add_item ('filetype', 'output');   }
pattern:	PATTERN_    { &start_qual ('text'); }
properties:	PROPERTIES  { &start_qual ('file'); &add_item ('datatype', 'input');}
range:		RANGE       { &start_qual ('text'); }
report:		REPORT      { &start_qual ('file'); &add_item ('datatype', 'outfile');
			                            &add_item ('filetype', 'output');   }
regexp:		REGEXP      { &start_qual ('text'); }
resource:	RESOURCE    { &start_qual ('file'); &add_item ('datatype', 'resource'); }
selection:	SELECTION   { &start_qual ('selection');}
seqall:		SEQALL      { &start_qual ('seq'); }
seqout:		SEQOUT      { &start_qual ('text'); &add_item ('datatype', 'seq');      }
seqoutall:	SEQOUTALL   { &start_qual ('text'); &add_item ('datatype', 'seqoutall');}
seqoutset:	SEQOUTSET   { &start_qual ('text'); &add_item ('datatype', 'seqout');   }
seqset:		SEQSET      { &start_qual ('seq'); }
seqsetall:	SEQSETALL   { &start_qual ('seqsetall'); }
sequence:	SEQUENCE    { &start_qual ('seq'); }
string:		STRING      { &start_qual ('text'); }
taxon:	    TAXON       { &start_qual ('file'); &add_item ('datatype', 'taxon'); }
text:	    TEXT        { &start_qual ('file'); &add_item ('datatype', 'text'); }
toggle:		TOGGLE      { &start_qual ('bool'); }
tree:	    TREE        { &start_qual ('file'); &add_item ('datatype', 'input');}
url:	    URL         { &start_qual ('file'); &add_item ('datatype', 'url'); }
xygraph:	XYGRAPH     { &start_qual ('text'); &add_item ('datatype', 'graph');    }
	;

general_attr:	DEFAULT     sep VALUE { &add_item ('default', $3); }
	|	PROMPT      sep VALUE { &add_item ('realprompt', $3); }
	|	INFORMATION sep VALUE { &add_item ('prompt', $3); }
	|	CODE        sep VALUE
	|	HELP        sep VALUE { &add_item ('help', $3); }
	|	VALID       sep VALUE
	|	EXPECTED    sep VALUE
	|	STANDARD    sep VALUE { &add_boolean_item ('required', $3); }
	|	ADDITIONAL  sep VALUE { &add_boolean_item ('optional', $3); }
	|	PARAMETER   sep VALUE { if ($3 =~ /y/i) { if ($::app{'non-emboss'} eq 'Y') { &add_item ('method', '$"$"'); } ; &add_boolean_item ('required', 'y'); } }
	|	MISSING     sep VALUE
	|	OUTPUTMODIFIER     sep VALUE { &add_boolean_item ('outputmodifier', $3); }
	|	CORBA       sep VALUE { &add_option ('options', $3); }
	|	STYLE       sep VALUE { &add_option ('style', $3); }
	|	COMMENT     sep VALUE { &add_option ('options', $3); }
	|	TEMPLATE    sep VALUE { &add_item ('method', $3); }
	|	QUALIFIER   sep VALUE { &add_option ('options', 'qualifier ' . $3); }
	|	KNOWNTYPE   sep VALUE { &add_option ('options', 'knowntype ' . $3); }
	|	W2HUNKNOWN  sep VALUE /* unknown attribute */
	|	RELATIONS   sep VALUE { &add_relation ('options', $3); }
	|	FAILRANGE   sep VALUE { &add_boolean_item ('required', $3); }
	|	TRUEMINIMUM sep VALUE { &add_boolean_item ('required', $3); }
	;


alignment_attrs:	/* empty */
	|	alignment_attrs alignment_attr
	;

alignment_attr:	general_attr
	|	align_gen_attr
	|	NAME	   sep VALUE { &add_item('name', $3); }
	|	TYPE	   sep VALUE { &add_item('type', $3); }
	|	TAGLIST	   sep VALUE { &add_item('taglist', $3); }
	|	MINSEQS	   sep VALUE { &add_item('minseqs', $3); }
	|	MAXSEQS	   sep VALUE { &add_item('maxseqs', $3); }
	|	MULTIPLE   sep VALUE { &add_boolean_item('multiple', $3); }
	|	NULLOK     sep VALUE { &add_boolean_item ('nullok', $3); }
	|	NULLDEFAULT sep VALUE { &add_item ('nulldefault', $3); }
	;

align_gen_attr:	AFORMAT	    sep VALUE { &add_item('aformat', $3); }
	| 	AOPENFILE   sep VALUE { &add_item('aopenfile', $3); }
	| 	AEXTENSION  sep VALUE { &add_item('aextension', $3); }
	| 	ANAME	    sep VALUE { &add_item('aname', $3); }
	| 	AWIDTH	    sep VALUE { &add_item('awidth', $3); }
	| 	AUSASHOW    sep VALUE { &add_boolean_item('ausashow', $3); }
	| 	AGLOBAL	    sep VALUE { &add_boolean_item('aglobal', $3); }
	;

boolean_attrs:	/* empty */
	|	boolean_attrs general_attr
	;

codon_attrs:	/* empty */
	|	codon_attrs codon_attr
	;

codon_attr:	general_attr
   |	NAME    sep VALUE { &add_item ('label', $3); }
   |	NULLOK  sep VALUE { &add_boolean_item ('nullok', $3); }
   ;


features_attrs:	/* empty */
	|	features_attrs features_attr
	;

features_attr:	general_attr
	|	NAME       sep VALUE { &add_item ('label', $3); }
	|	EXTENSION  sep VALUE { &add_item ('extension', $3); }
	|	OFNAME     sep VALUE { &add_item ('ofname', $3); }
	|	OFFORMAT   sep VALUE { &add_item ('offormat', $3); }
	|	TYPE       sep VALUE { &add_item ('sequence_type', $3); }
    |	NULLOK     sep VALUE { &add_boolean_item ('nullok', $3); }
    |	NULLDEFAULT sep VALUE { &add_boolean_item ('nulldefault', $3); }
    |	MAXREADS	sep VALUE { &add_item('maxreads', $3); }
    |	ENTRY	    sep VALUE { &add_item('entry', $3); }
	;
	
file_attrs:	/* empty */
	|	file_attrs file_attr
	;

file_attr:	general_attr
	|	TYPE       sep VALUE { &add_item ('type', $3); }
	|	NAME       sep VALUE { &add_item ('label', $3); }
	|	EXTENSION  sep VALUE { &add_item ('extension', $3); }
	|	DIRECTORY_ATTR  sep VALUE { &add_item ('directory', $3); }
	|   CHARACTERS sep VALUE { &add_item ('characters', $3); }
	|   LENGTH     sep VALUE { &add_item ('length', $3); }
	|   SIZE       sep VALUE { &add_item ('size', $3); }
	|	NULLOK     sep VALUE { &add_boolean_item ('nullok', $3); }
    |	NULLDEFAULT sep VALUE { &add_boolean_item ('nulldefault', $3); }
	|	APPEND      sep VALUE { &add_boolean_item ('append', $3); }
	|	CREATE      sep VALUE { &add_boolean_item ('create', $3); }
	|	TRYDEFAULT  sep VALUE { &add_boolean_item ('trydefault', $3); }
	;


outcodon_attrs:	/* empty */
	|	outcodon_attrs outcodon_attr
	;

outcodon_attr:	general_attr
	|	NAME        sep VALUE { &add_item ('label', $3); }
	|	EXTENSION   sep VALUE { &add_item ('extension', $3); }
	|	NULLDEFAULT sep VALUE { &add_boolean_item ('nulldefault', $3); }
	|	NULLOK      sep VALUE { &add_boolean_item ('nullok', $3); }
   |	ODIRECTORY  sep VALUE { &add_item ('odirectory', $3); }
	|	OFORMAT     sep VALUE { &add_boolean_item ('oformat', $3); }
	;

flist_attrs:	/* empty */
	|	flist_attrs flist_attr
	;

flist_attr:	general_attr
	;


float_attrs:	/* empty */
	|	float_attrs float_attr
	;

float_attr:	integer_attr
	|	PRECISION  sep VALUE { &add_item ('precision', $3); }
	;

graph_attrs:	/* empty */
	|	graph_attrs graph_attr
	;

graph_attr:	general_attr
	|	TYPE       sep VALUE { &add_item ('type', $3); }
	|	GTITLE     sep VALUE { &add_item ('gtitle', $3); }
	|	GSUBTITLE  sep VALUE { &add_item ('gsubtitle', $3); }
	|	GXTITLE    sep VALUE { &add_item ('gxtitle', $3); }
	|	GYTITLE    sep VALUE { &add_item ('gytitle', $3); }
	|   GDESC      sep VALUE { &add_item ('gdesc', $3); }
	|	NULLOK     sep VALUE { &add_boolean_item ('nullok', $3); }
	|	NULLDEFAULT sep VALUE { &add_boolean_item ('nulldefault', $3); }
	;

integer_attrs:	/* empty */
	|	integer_attrs integer_attr
	;

integer_attr:	general_attr
	|	MINIMUM    sep VALUE { &add_item ('hardmin', $3); }
	|	MAXIMUM    sep VALUE { &add_item ('hardmax', $3); }
	|	INCREMENT  sep VALUE { &add_item ('scaleinc', $3); }
	|	WARNRANGE  sep VALUE
	;

list_attrs:	/* empty */
	|	list_attrs list_attr
	;

list_attr:	selection_attr
#	|	CODEDELIMITER  sep VALUE { &add_item ('codedelim', $3); }
	;

matrix_attrs:	/* empty */
	|	matrix_attrs matrix_attr
	;

matrix_attr:	general_attr
	|	PNAME    sep VALUE { &add_item ('pname', $3); }
	|	NNAME    sep VALUE { &add_item ('nname', $3); }
	|	PROTEIN  sep VALUE { &add_boolean_item ('protein', $3); }
	;

obo_attr:	general_attr
	|	ENTRY    sep VALUE { &add_item ('entry', $3); }
	|	MAXREADS sep VALUE { &add_item ('maxreads', $3); }
	;

obo_attrs:
	|	obo_attrs obo_attr
	;

pattern_attrs:	/* empty */
	|	pattern_attrs pattern_attr
	;

pattern_attr:	general_attr
	|	MINLENGTH  sep VALUE { &add_item ('hardmin', $3); }
	|	MAXLENGTH  sep VALUE { &add_item ('hardmax', $3); }
	|	TYPE       sep VALUE { &add_item ('type', $3); }
	|	PMISMATCH  sep VALUE { &add_item ('pmismatch', $3); }
	|	LOWER      sep VALUE { &add_boolean_item ('tolowercase', $3); }
	|	UPPER      sep VALUE { &add_boolean_item ('touppercase', $3); }
	;

range_attrs:	/* empty */
	|	range_attrs range_attr
	;

range_attr:	general_attr
	|	MINIMUM  sep VALUE { &add_item ('minimum', $3); }
	|	MAXIMUM  sep VALUE { &add_item ('maximum', $3); }
	|	MINSIZE  sep VALUE { &add_boolean_item ('minsize', $3); }
	|	SIZE     sep VALUE { &add_boolean_item ('size', $3); }
	;

regexp_attrs:	/* empty */
	|	regexp_attrs regexp_attr
	;

regexp_attr:	general_attr
	|	MINLENGTH  sep VALUE { &add_item ('hardmin', $3); }
	|	MAXLENGTH  sep VALUE { &add_item ('hardmax', $3); }
	|	LOWER      sep VALUE { &add_boolean_item ('tolowercase', $3); }
	|	UPPER      sep VALUE { &add_boolean_item ('touppercase', $3); }
	;

report_attrs:	/* empty */
	|	report_attrs report_attr
	;

report_attr:	general_attr
	|	MINTAGS	   sep VALUE { &add_item('mintags', $3); }
	|	MULTIPLE   sep VALUE { &add_boolean_item('multiple', $3); }
	|	NAME	   sep VALUE { &add_item('name', $3); }
	|	NULLOK     sep VALUE { &add_boolean_item ('nullok', $3); }
	|	NULLDEFAULT sep VALUE { &add_boolean_item ('nulldefault', $3); }
	|	PRECISION  sep VALUE { &add_item ('precision', $3); }
	|	REXTENSION sep VALUE { &add_item('rextension', $3); }
	|	RFORMAT	   sep VALUE { &add_item('rformat', $3); }
	|	RNAME	   sep VALUE { &add_item('rname', $3); }
	|	ROPENFILE  sep VALUE { &add_item('ropenfile', $3); }
	|	RSCORESHOW sep VALUE { &add_boolean_item('rscoreshow', $3); }
	|	RUSASHOW   sep VALUE { &add_boolean_item('rusashow', $3); }
	|	TAGLIST	   sep VALUE { &add_item('taglist', $3); }
	|	TYPE	   sep VALUE { &add_item('type', $3); }
        ;

resource_attr:	general_attr
	|	ENTRY     sep VALUE { &add_boolean_item ('entry', $3); }
	;

resource_attrs:
	|	resource_attrs resource_attr
	;
	
section_attrs:	/* empty */
	|	section_attrs section_attr
	;

section_attr:	general_attr
	|	TYPE      sep VALUE { &add_item ('type', $3); }
	|	BORDER    sep VALUE { &add_item ('border', $3); }
	|	SIDE      sep VALUE { &add_item ('side', $3); }
	|	FOLDER    sep VALUE { &add_item ('folder', $3); }
	;

selection_attrs:	/* empty */
	|	selection_attrs selection_attr
	;

selection_attr:	general_attr
	|	CODEDELIMITER  sep VALUE { &add_item ('codedelim', $3); }
	|	MINIMUM        sep VALUE { &add_item ('min', $3); }
	|	MAXIMUM        sep VALUE { &add_item ('max', $3); }
	|	BUTTON         sep VALUE { &add_item ('button', $3); }
	|	CASESENSITIVE  sep VALUE
	|	HEADER         sep VALUE { &add_item ('header', $3); }
	|	DELIMITER      sep VALUE { &add_item ('delim', $3); }
	|	VALUES         sep VALUE { &add_item ('values', $3); }
	;

sequence_attrs:	/* empty */
	|	sequence_attrs sequence_attr
	;

sequence_attr:	general_attr
	|	TYPE      sep VALUE { &add_item ('type', $3); }
	|	FEATURES  sep VALUE { &add_boolean_item ('features', $3); }
	|	ENTRY     sep VALUE { &add_boolean_item ('entry', $3); }
	|	NULLOK    sep VALUE { &add_boolean_item ('nullok', $3); }
	|	ALIGNED   sep VALUE { &add_boolean_item ('aligned', $3); }
	|	MINSEQS   sep VALUE { &add_boolean_item ('minseqs', $3); }
	|	SASK      sep VALUE { &add_boolean_item ('sask', $3); }
	;

seq_out_attrs:	/* empty */
	|	seq_out_attrs seq_out_attr
	;

seq_out_attr:	general_attr
	|	NAME        sep VALUE { &add_item ('label', $3); }
	|	EXTENSION   sep VALUE { &add_item ('extension', $3); }
	|	FEATURES    sep VALUE { &add_item ('features', $3); }
	|	OSSINGLE    sep VALUE { &add_item ('ossingle', $3); }
	|   OSEXTENSION sep VALUE { &add_item ('osextension', $3); }
	|   NULLOK      sep VALUE { &add_item ('nullok', $3); }
	|   NULLDEFAULT sep VALUE { &add_item ('nulldefault', $3); }
	|	TYPE        sep VALUE { &add_item ('type', $3); }
	|	ALIGNED     sep VALUE { &add_boolean_item ('aligned', $3); }
	;

string_attrs:	/* empty */
	|	string_attrs string_attr
	;

string_attr:	regexp_attr
	|	WORD     sep VALUE { &add_item ('word', $3); }
	|	PATTERN  sep VALUE { &add_item ('pattern', $3); }
	;

taxon_attr:	general_attr
	|	ENTRY    sep VALUE { &add_item ('entry', $3); }
	|	MAXREADS sep VALUE { &add_item ('maxreads', $3); }
	;

taxon_attrs:
	|	taxon_attrs taxon_attr
	;

text_attr:	general_attr
	|	ENTRY    sep VALUE { &add_item ('entry', $3); }
	|	MAXREADS sep VALUE { &add_item ('maxreads', $3); }
	;

text_attrs:
	|	text_attrs text_attr
	;

unknown_attrs:	/* empty */
	|	unknown_attrs general_attr
	;

url_attr:	general_attr
	|	ENTRY    sep VALUE { &add_item ('entry', $3); }
	|	MAXREADS sep VALUE { &add_item ('maxreads', $3); }
	;

url_attrs:
	|	url_attrs url_attr
	;

xygraph_attrs:	/* empty */
	|	xygraph_attrs xygraph_attr
	;

xygraph_attr:	graph_attr
	|	MULTIPLE  sep VALUE { &add_item ('multiple', $3); }
	|	GOUTFILE  sep VALUE { &add_item ('goutfile', $3); }
	;

sep:		':'
	|	'='
	;

toggle_attrs:	/* empty */
	|	toggle_attrs general_attr
	;

array_attrs:	/* empty */
	|	array_attrs array_attr
	;

array_attr:	general_attr
	|	MINIMUM     sep VALUE { &add_item ('minimum', $3); }
	|	MAXIMUM     sep VALUE { &add_item ('maximum', $3); }
   |	INCREMENT   sep VALUE { &add_item ('increment', $3); }
	|	PRECISION   sep VALUE { &add_item ('precision', $3); }
	|	WARNRANGE   sep VALUE { &add_item ('warnrange', $3); }
	|	SIZE        sep VALUE { &add_item ('size', $3); }
	|	SUM         sep VALUE { &add_item ('sum', $3); }
	|	SUMTEST     sep VALUE { &add_item ('sumtest', $3); }
	|	TOLERANCE   sep VALUE { &add_item ('tolerance', $3); }
	;

frequencies_attrs:	/* empty */
	|	frequencies_attrs frequencies_attr
	;

frequencies_attr:	general_attr
	|	LENGTH     sep VALUE { &add_item ('length', $3); }
	|	SIZE       sep VALUE { &add_item ('size', $3); }
   |	CONTINOUS  sep VALUE { &add_item ('continous', $3); }
	|	GENEDATA   sep VALUE { &add_item ('genedata', $3); }
   |	WITHIN     sep VALUE { &add_item ('within', $3); }
	|	NULLOK     sep VALUE { &add_item ('nullok', $3); }
	;

tree_attrs:	/* empty */
	|	tree_attrs tree_attr
	;

tree_attr:	general_attr
	|	SIZE       sep VALUE { &add_item ('size', $3); }
	|	NULLOK     sep VALUE { &add_item ('nullok', $3); }
	;

%%

# -----------------------------------------------------------------------------
# processing a qualifier (can deal with arbitrarily nested qualifiers)
# -----------------------------------------------------------------------------
$Order = 0;
sub start_qual {
    my ($qtype) = @_;
    push (@q, { qtype => $qtype });
    &add_item ('ordering', ++$Order);
}

sub add_item {
    my ($key, $val) = @_;
    if ($val =~ /^\s*[\@\$][\{\(]/) {
	&add_option ('options', "calculated_$key=$val");
	$val = '';
    }
#    $val = '' if $val =~ /^\s*[\@\$][\{\(]/; # currently no eval
    ${$q[$#q]}{$key} = $val;
}

sub add_boolean_item {
    my ($key, $val) = @_;
    $val = '' if $val =~ /^\s*[\@\$][\{\(]/; # currently no eval
    $val = ("\U$val" eq "Y") ? "+" : "-";
    ${$q[$#q]}{$key} = $val;
}


# --- any option has value in the form KEY = VALUE, and we want to add it
#     into an appropriate hash table as part of a separate hash table
#     which reference is stored under 'name' in a top-level, section or qualifier
#     hash
sub add_option {              # an option
    my ($name, $val) = @_;
    my ($key, $value) = split (/\s*[=: ]\s*/, $val, 2);   # separator can be :, =, or a spce
    $value = 'true' if $key and !$value;
    my ($rh);
    if (@q > 0) {
        ${$q[$#q]}{$name} = {} unless ${$q[$#q]}{$name};
        $rh = ${$q[$#q]}{$name};
    } else {
        $::app{$name} = {} unless $::app{$name};
        $rh = $::app{$name};
    }
    $$rh{"\L$key"} = $value;
}

# --- TBD: any option has value in the form KEY = VALUE, and we want to add it
#     into an appropriate hash table as part of a separate hash table
#     which reference is stored under 'name' in a top-level, section or qualifier
#     hash
sub add_relation {              # a relation
    my ($name, $val) = @_;
    my ($key, $value);
    if (substr ($val, 0, 1) eq '/') {
	# syntax for EMBOSS version 6.3.1 and prior
	# e.g. /edam/data/0000849 Sequence record
	($key, $value) = split (/\s*[=: ]\s*/, $val, 2);
	my (@parts) = split ('/', $key);
	$key = uc ($parts[1]) . ':' . $parts[-1];  # EDAM:0000849
	$value = $parts[-2] . ' ' . $value;        # data Sequence record
    } else {
	# syntax for EMBOSS version above 6.3.1
	# e.g. EDAM:0000849 data Sequence record
	($key, $value) = split (/\s*[= ]\s*/, $val, 2);
    }

    # the rest is copied from 'add_option'
    $value = 'true' if $key and !$value;
    my ($rh);
    if (@q > 0) {
        ${$q[$#q]}{$name} = {} unless ${$q[$#q]}{$name};
        $rh = ${$q[$#q]}{$name};
    } else {
        $::app{$name} = {} unless $::app{$name};
        $rh = $::app{$name};
    }
#    $$rh{"\L$key"} = $value;
    $$rh{$key} = $value;
}

sub end_qual {
    my ($id) = @_;
    my ($refnewq) = &finalize_qual ($id);

    if (@q > 0) {  # this qual is nested into the previous one
        my ($refprev) = $q[$#q];
        $$refprev{'nested'} = [] unless defined $$refprev{'nested'};
        push (@{$$refprev{'nested'}}, $refnewq);
    } else {       # this qual is on the highest level
        my ($qgroup) = ( $refnewq->{'required'} eq '+' ? 'mainq' : 'optq' );
        $qgroup = 'mainq' if $$refnewq{'qtype'} eq 'text' and defined $$refnewq{'datatype'};
        push (@{$::app{$qgroup}}, $refnewq);
    }
}


sub end_seqout {
    my ($id) = @_;
    my ($refnewq) = &finalize_qual ($id);
        
    $$refnewq{'help'} = "Returns sequence.";
    
    push (@{$::app{'optq'}}, $refnewq);
}


sub end_sequence {
    my ($id) = @_;
    my ($refnewq) = &finalize_qual ($id);

    # --- sequence parameters
    $$refnewq{'nfile'} = $id;
    $$refnewq{'nbegin'} = 'sbegin';
    $$refnewq{'nend'} = 'send';
    $$refnewq{'ntype'} = 'type=protein';
    $$refnewq{'nstrand'} = 'sreverse';

    # --- this appl may be only for one sequence type
    if ($::app{'type'} eq "P") {
	$::app{'type'} = "" unless $$refnewq{'type'} =~ /protein/i;
    } elsif ($::app{'type'} eq "N") {
	$::app{'type'} = "" unless $$refnewq{'type'} =~ /dna|rna|nucl/i;
    } elsif (!defined $::app{'type'}) {
	$::app{'type'} = "P" if $$refnewq{'type'} =~ /protein/i;
	$::app{'type'} = "N" if $$refnewq{'type'} =~ /dna|rna|nucl/i;
    }

    # --- store the qualifier where it belongs
    $::app{'input'} = [] unless defined $::app{'input'};
    push (@{$::app{'input'}}, $refnewq);

    # --- remember how many input sequences we have
    $::app{'numinput'} ++;
}

sub end_list {
	my ($id) = @_;
    my ($refnewq) = &finalize_qual ($id);

    # --- make two lists from 'values': the real values (will be used
    #     as qualifiers on the command line) and their descriptions
    #     (will be used as prompts)
    my ($delim) = $$refnewq{'delim'};
    $delim = ';' unless defined $delim;
    my (@prompts) = ();
    my (@quals) = ();
    my (@values) = split (/\s*\U$delim\E\s*/, $$refnewq{'values'});
    if ($$refnewq{'qtype'} eq 'list' or $$refnewq{'qtype'} eq 'selection' ) {
		my ($qual, $prompt);
		my ($codedelim) = $$refnewq{'codedelim'};
		$codedelim = ':' unless defined $codedelim;
        map { ($qual, $prompt) = split (/\s*\U\Q$codedelim\E\E\s*/);
	       push (@quals,  $qual);
	       push (@prompts, $prompt);
	  	} @values;
    } else {
		my ($order);
		map { push (@quals, ++$order);
              push (@prompts, $_);
	  	} @values;
    }

    # --- extract individual qualifier values from 'default'
    #     to be able to enable some bools elements
    my (%dflt) = {};
    map { $dflt{$_} = 1; } split (/\s*\U$delim\E\s*|\s+/, $$refnewq{'default'});

    # --- make from the current qualifier a 'parent' element
    #     which is either TEXT (represented actually by a radio dropdown button)
    #     or GROUP (aka MENU)
    my ($i);
    my ($radio) = ($$refnewq{'max'} == 1 and $$refnewq{'min'} == 1);
    if ($radio) {
		$$refnewq{'qtype'} = 'text';

        # --- for radio type we prepare 'rawlist' in the current qualifier
		$$refnewq{'rawlist'} = [];
        foreach $i (0..$#quals) {
	    push ( @{$$refnewq{'rawlist'}},
		   { 'value' => $quals[$i],
		     'shown_as' => $prompts[$i] } );
	    $$refnewq{'default'} = $quals[$i] if defined $dflt{$quals[$i]};
	}

    } else {
		$$refnewq{'qtype'} = 'group';
		$$refnewq{'separator'} = ',';

		# --- try to be clever and estimate how many 'columns' would
		#     be appropriate for the lengths of our prompts
		my ($maxlen) = 1;
		map { $maxlen = length ($_) if $maxlen < length ($_); } @prompts;
		my ($cols) = int (80/$maxlen);   # 80 is just my guess :-)
	        $cols = 5 if $cols > 5;
		$cols-- while ($cols > 1 and (@prompts % $cols) > 0 and ((@prompts % $cols) + 1) < $cols);
		$$refnewq{'columns'} = $cols;

        # --- for menu type put this qualifier back for a moment
        #     and build all its nested 'bools'
		push (@q, $refnewq);
		foreach $i (0..$#quals) {
		    &start_qual ('bool');
		    &add_item ('prompt', $prompts[$i]);
		    &add_item ('default', '+') if defined $dflt{$quals[$i]};
		    &add_item ('name', $quals[$i]);
		    &add_item ('qid', $$refnewq{'name'} . '_' . $quals[$i]);
		    &end_qual (undef);
		}
		$refnewq = pop (@q);
	}
    
    # --- a bit of cleaning
    delete $$refnewq{'values'};
    delete $$refnewq{'button'};
    delete $$refnewq{'delim'};
    delete $$refnewq{'codedelim'};

    # --- and finally put it where it belongs
    my ($qgroup) = ( $refnewq->{'required'} eq '+' ? 'mainq' : 'optq' );
    push (@{$::app{$qgroup}}, $refnewq);

}

sub end_section {
    my ($id) = @_;

    my $optq = $::app{'optq'};

    if (@$optq > 0 and $optq->[@$optq -1]->{'qid'} eq $id) {
       # --- empty section, last qualifier was begin section
       #     => don't show
       pop(@$optq);
    } else {
       &start_qual ('endsection');
       &end_qual ($id . "_END");
    }

    ## use Data::Dumper;
    ## my $dump =  "warn: endsection $id\n" .
    ## 		   join (", ", map { $_->{'name'} } @$optq);
    ## warn $dump;
}


# --- put ID and few other general attrs into the last qualifier,
#     remove it from the stack of qualifiers (@q), and return it
sub finalize_qual {
    my ($id) = @_;

    # --- remember parameter name
    if (defined $id) {
	&add_item ('qid', $id);
	&add_item ('name', $id);
    }

    my ($refnewq) = pop (@q);

    # --- primarily 'prompt' is made from 'header' or 'info' attributes
    #     (in this order), but if they do not exist, it can be made from
    #     the attribute 'prompt' (temporary stored in 'realprompt')
    #     or even from the attribute 'help'
    $refnewq->{'prompt'} = $refnewq->{'header'} if $refnewq->{'header'};
    unless ($refnewq->{'prompt'}) {
#        $refnewq->{'prompt'} = $refnewq->{'help'} if $refnewq->{'help'};
        $refnewq->{'prompt'} = $refnewq->{'realprompt'} if $refnewq->{'realprompt'};
    }
    delete $refnewq->{'realprompt'};

    return $refnewq;
}

sub is_yes {
    my ($val) = @_;

    return $val =~ /^\s*y/i;
}

# -----------------------------------------------------------------------------
# print an error message (called from parser, and lexer)
# -----------------------------------------------------------------------------
sub yyerror {
    my ($msg, $input) = @_;
    if (length ($Last_returned_token) > 40) {
	$Last_returned_token = substr ($Last_returned_token, 0, 40) . '...';
    }
    my ($err) = "File:" . $input->name .
	        ", line " .  $Token_line_number .
		" Token ==> " . $Last_returned_token . ", $msg";
    print STDERR "$err\n";
    @read_tokens = ();   # to start reading the next definition
}

# -----------------------------------------------------------------------------
#
# ACD Lexical analyzer.
# It uses globals: @all_tokens, @read_tokens,
#                  $Current_datatype,
#                  $Last_returned_token,
#                  $Token_line_number,
#                  %Opposite_delimiters
          
# -----------------------------------------------------------------------------
@read_tokens = ();

@general_attrs = qw (
		     DEFAULT PROMPT INFORMATION CODE HELP VALID EXPECTED STANDARD
		     ADDITIONAL PARAMETER MISSING CORBA STYLE COMMENT KNOWNTYPE
		     QUALIFIER TEMPLATE RELATIONS OUTPUTMODIFIER
		     NULLOK NULLDEFAULT FAILRANGE TRUEMINIMUM
		     );
@align_gen_attrs = qw (
		     AFORMAT AOPENFILE AEXTENSION ANAME AWIDTH AUSASHOW AGLOBAL
		     );
%all_tokens = (
    $DATA_TYPES   => [ qw( APPLICATION ALIGNMENT BOOLEAN DATAFILE
               DIRECTORY DIRLIST ENDSECTION FILELIST
			   INTEGER FLOAT RANGE REGEXP PATTERN_ STRING VARIABLE
			   INFILE MATRIX MATRIXF CODON SEQUENCE SEQSET SEQALL SEQSETALL FEATURES 
			   LIST SELECTION OUTCODON OUTDIR OUTFILE
			   OBO OUTOBO OUTRESOURCE OUTTAXON OUTTEXT OUTURL
			   REPORT RESOURCE SECTION SEQOUT SEQOUTSET
			   SEQOUTALL FEATOUT GRAPH XYGRAPH TOGGLE ARRAY DISCRETESTATES PROPERTIES
			   TAXON TEXT TREE FREQUENCIES DISTANCES URL
			)
		],
    $APPLICATION  => [ qw( BATCH CPU DOCUMENTATION EXECUTABLE EXTERNAL GROUPS
        COMMENT GUI NONEMBOSS SUPPLIER VERSION EMBASSY RELATIONS OBSOLETE) ],
    $ALIGNMENT    => [ @general_attrs, @align_gen_attrs, qw(NAME TYPE TAGLIST MINSEQS MAXSEQS MULTIPLE) ],
    $ARRAY        => [ @general_attrs, qw(MINIMUM MAXIMUM INCREMENT PRECISION WARNRANGE SIZE SUM SUMTEST TOLERANCE) ],
    $BOOLEAN      => [ @general_attrs ],
    $DATAFILE     => [ @general_attrs, qw(TYPE NAME EXTENSION DIRECTORY_ATTR NULLOK) ],
    $DIRECTORY    => [ @general_attrs, qw(NULLOK EXTENSION) ],
    $DIRLIST      => [ @general_attrs, qw(NULLOK EXTENSION) ],
    $DISCRETESTATES   => [ @general_attrs, qw(LENGTH, SIZE, CHARACTERS, NULLOK) ],
    $DISTANCES    => [ @general_attrs, qw(SIZE, NULLOK, MISSVAL) ],
    $ENDSECTION   => [ ],
    $FREQUENCIES  => [ @general_attrs, qw(LENGTH, SIZE, CONTINOUS, GENEDATA, WITHIN, NULLOK) ],
    $INTEGER      => [ @general_attrs, qw(MINIMUM MAXIMUM INCREMENT WARNRANGE) ],
    $FILELIST     => [ @general_attrs ],
    $FLOAT        => [ @general_attrs, qw(MINIMUM MAXIMUM INCREMENT WARNRANGE PRECISION) ],
    $RANGE        => [ @general_attrs, qw(MINIMUM MAXIMUM MINSIZE SIZE) ],
    $PATTERN_     => [ @general_attrs, qw(MINLENGTH MAXLENGTH LOWER UPPER TYPE PMISMATCH) ],
    $REGEXP       => [ @general_attrs, qw(MINLENGTH MAXLENGTH LOWER UPPER) ],
    $STRING       => [ @general_attrs, qw(MINLENGTH MAXLENGTH LOWER UPPER PATTERN WORD) ],
    $VARIABLE     => [ @general_attrs ],
    $INFILE       => [ @general_attrs, qw(TYPE NAME EXTENSION NULLOK TRYDEFAULT) ],
    $MATRIX       => [ @general_attrs, qw(PNAME NNAME PROTEIN) ],
    $MATRIXF      => [ @general_attrs, qw(PNAME NNAME PROTEIN) ],
    $CODON        => [ @general_attrs, qw(NAME NULLOK) ],
    $PROPERTIES   => [ @general_attrs, qw(LENGTH, SIZE, CHARACTERS, NULLOK) ],
    $SEQUENCE     => [ @general_attrs, qw(TYPE FEATURES ENTRY ALIGNED NULLOK SASK) ],
    $SEQSET       => [ @general_attrs, qw(TYPE FEATURES ENTRY ALIGNED MINSEQS MAXSEQS NULLOK MINSEQS) ],
    $SEQSETALL    => [ @general_attrs, qw(TYPE FEATURES ALIGNED MINSEQS MAXSEQS MINSETS MAXSETS NULLDEFAULT NULLOK) ],
    $SEQALL       => [ @general_attrs, qw(TYPE FEATURES ENTRY ALIGNED NULLOK) ],
    $FEATURES     => [ @general_attrs, qw(NAME EXTENSION TYPE MAXREADS ENTRY) ],
    $LIST         => [ @general_attrs, qw(MINIMUM MAXIMUM BUTTON CASESENSITIVE HEADER DELIMITER VALUES CODEDELIMITER) ],
    $REPORT       => [ @general_attrs, qw(NAME TYPE MINTAGS MULTIPLE TAGLIST REXTENSION RFORMAT RNAME ROPENFILE RSCORESHOW RUSASHOW PRECISION NULLOK NULLDEFAULT) ],
    $RESOURCE     => [ @general_attrs, qw(TYPE NAME ENTRY EXTENSION DIRECTORY_ATTR NULLOK) ],
    $SELECTION    => [ @general_attrs, qw(MINIMUM MAXIMUM BUTTON CASESENSITIVE HEADER CODEDELIMITER DELIMITER VALUES) ],
    $OBO          => [ @general_attrs, qw(TYPE NAME ENTRY EXTENSION DIRECTORY_ATTR NULLOK MAXREADS) ],
    $OUTCODON     => [ @general_attrs, qw(NAME EXTENSION NULLDEFAULT NULLOK ODIRECTORY OFORMAT) ],
    $OUTDIR       => [ @general_attrs, qw(NULLOK EXTENSION CREATE) ],
    $OUTFILE      => [ @general_attrs, qw(TYPE NAME EXTENSION NULLOK NULLDEFAULT APPEND) ],
    $OUTOBO       => [ @general_attrs, qw(TYPE NAME EXTENSION NULLOK NULLDEFAULT APPEND) ],
    $OUTTAXON     => [ @general_attrs, qw(TYPE NAME EXTENSION NULLOK NULLDEFAULT APPEND) ],
    $OUTTEXT      => [ @general_attrs, qw(TYPE NAME EXTENSION NULLOK NULLDEFAULT APPEND) ],
    $OUTRESOURCE  => [ @general_attrs, qw(TYPE NAME EXTENSION NULLOK NULLDEFAULT APPEND) ],
    $OUTURL       => [ @general_attrs, qw(TYPE NAME EXTENSION NULLOK NULLDEFAULT APPEND) ],
    $SECTION      => [ @general_attrs, qw(TYPE BORDER SIDE FOLDER) ],
    $SEQOUT       => [ @general_attrs, qw(NAME EXTENSION FEATURES TYPE NULLOK) ],
    $SEQOUTSET    => [ @general_attrs, qw(NAME EXTENSION FEATURES TYPE ALIGNED) ],
    $SEQOUTALL    => [ @general_attrs, qw(NAME EXTENSION FEATURES TYPE OSEXTENSION OSSINGLE NULLOK ALIGNED) ],
    $TAXON        => [ @general_attrs, qw(TYPE NAME ENTRY EXTENSION DIRECTORY_ATTR NULLOK MAXREADS) ],
    $TEXT         => [ @general_attrs, qw(TYPE NAME ENTRY EXTENSION DIRECTORY_ATTR NULLOK MAXREADS) ],
    $TREE         => [ @general_attrs, qw(SIZE, NULLOK) ],
    $FEATOUT      => [ @general_attrs, qw(NAME EXTENSION OFNAME OFFORMAT TYPE NULLOK NULLDEFAULT) ],
    $GRAPH        => [ @general_attrs, qw(TYPE GDESC GTITLE GXTITLE GYTITLE GSUBTITLE NULLOK) ],
    $XYGRAPH      => [ @general_attrs, qw(TYPE GDESC GTITLE GXTITLE GYTITLE GSUBTITLE GOUTFILE MULTIPLE NULLOK NULLDEFAULT) ],
    $TOGGLE       => [ @general_attrs ],
    $NULLOK       => [ @general_attrs ],
    $URL          => [ @general_attrs, qw(TYPE NAME ENTRY EXTENSION DIRECTORY_ATTR NULLOK MAXREADS) ]
    );

%Opposite_delimiters = ( '[' => ']', '<' => '>', '{' => '}', '"' => '"', "'" => "'" );

#
# This lex reads one "definition" in a time (either application definition or
# parameter definition), The definition ends by a non-escaped "]" (if it is
# not inside a quoted token). It stores it in @read_tokens - and returns a
# token each time it is called.
#
# It recognizes these types of tokens:
#
# - Single character separators: '=', ':', '[', and ']', they are returned
#   as they are, with no value attached.
#
# - "Real" tokens  (here belong acd datatypes and 'appl' token). They can be
#   abbreviated - therefore the lex must first find what acd token they really
#   represent. The lex considers anything _before_ ':' or '=' as a "real"
#   token.
#
# - Everything else is returned as token VALUE.
#
# During reading of one definition into @read_tokens the lex takes care about:
# - escaped characters,
# - quoted parts which represent always just a single token (quotation
#   pairs are "", '', {}, <>, [])
# - perl-like comments
#
sub yylex {
    my ($input) = @_;

    # --- get next definition from $input into @read_tokens
    #     (unless we still have some tokens from the previous visit)
    if ($#read_tokens == -1) {
	my ($c, $val);
	my ($outside_token) = 1;
	my ($current_delimiter);
	my ($square_bracket_opened) = 0;
        my ($reading_value) = 0;
        $Newline_significant = 0;
    
	while (($c = $input->getc) ne '') {

    	    #
	    # --- we are outside of a token
	    #
	    if ($outside_token) {

		# --- comment
		if ($c eq '#') {
		    &ignore_till ($input, "\n");
		    next; 
		}

		# --- escape sequence
		#     (actually it makes no sense to use escape
		#      sequence outside of a token...)
		if ($c eq '\\') {
		    $c = $input->getc;
		    if ($c eq '') {
			&yyerror ("Unexpected EOF in an escape sequence.", $input);
			last;   # eof with pending escape delimiter
		    }
		}

		# --- newline is usually ignored - but it may also indicates
		#     an end of this definition - see &is_defend
                last if &is_defend ($c);
    
		# --- ignore white-spaces (even those escaped above:-))
		next if $c =~ /\s/;

		# --- square brackets can be both token delimiters and
		#     a special separator in definitions
		if ($c eq '[' and $square_bracket_opened == 0) {
		    push (@read_tokens, [ord ($c), $c, $input->lineno]);
		    $square_bracket_opened = 1;
		    next;
		}
		if ($c eq ']' and $square_bracket_opened) {
		    push (@read_tokens, [ord ($c), $c, $input->lineno]);
		    last;   # end of a definition reached
		}

		# --- special separators are treated as individual tokens
		#     (also if they are escaped... which makes no sense probably)
		if ($c =~ /[:=]/) {
    		    push (@read_tokens, [ord ($c), $c, $input->lineno]);
                    $reading_value = 1;
		    next;
		}

                # we need special treatment for token 'variable'
                if ($#read_tokens > -1 and ${$read_tokens[0]}[1] =~ /^(v|ends)/i) {
                    $Newline_significant = 1;
                    $reading_value = 1;
                }
    
		# --- now we know that a token starts here
		#     (if it starts with a delimiter, remember it to
		#      be able to match with the ending delimiter)
		$outside_token = 0;
		if ($c =~ /["'{<\[]/) {
		    $current_delimiter = $Opposite_delimiters{$c};
		    next;
		}
		$val = $c;
    		next;
	    }

	    #
	    # --- we are inside a token
	    #

	    # --- escape sequence
	    if ($c eq '\\') {
		$c = $input->getc;
		if ($c eq '') {
		    &yyerror ("Unexpected EOF.", $input);
		    last;   # eof with pending escape delimiter
			    # AND inside a token - surely an error
		}
		$val .= $c;
		next;
	    }

	    # --- are we inside a token with delimiters?
	    if ($current_delimiter) {

		# --- end of a delimited token?        
		if ($c eq $current_delimiter) {
		    undef $current_delimiter;
		    $outside_token = 1;
    		    push (@read_tokens, [($reading_value ? $VALUE : undef), $val, $input->lineno]);
		    undef $val;
                    undef $reading_value;
		    next;
		}

		# --- replace each non-escaped newline by a single space
###		$c = ' ' if $c eq "\n";    # 15.10.00 - Alan said not to ignore them

		$val .= $c;
		next;
	    }

	    # --- comment (a bit unusual because it is not separated
	    #     from the current token by a whitespace, but valid)
	    if ($c eq '#') {
		$outside_token = 1;
		push (@read_tokens, [($reading_value ? $VALUE : undef), $val, $input->lineno]);
		undef $val;
                undef $reading_value;
		&ignore_till ($input, "\n");
                last if &is_defend ($c);
		next;
	    }

	    # --- end of a non-delimited token?
	    if (($reading_value and $c =~ /[\s\[\]]/) or
                (!$reading_value and $c =~ /[\s:=\[\]]/)) {
		$outside_token = 1;
                $input->ungetc;
		push (@read_tokens, [ ($reading_value ? $VALUE : undef), $val, $input->lineno]);
                undef $reading_value;
		undef $val;
                last if &is_defend ($c);
    		next;
	    }
    
            # --- finally, we know that we have just a piece of token
            $val .= $c;
	}

        # --- for each definition we will have to find its datatype
        undef $Current_datatype;

###        print ("ALL---\n\t" . join ("\n\t", map { $$_[1] } @read_tokens) . "\n------------\n");
    }

    # --- use @read_tokens
    #     (elements of @read_tokens are references to 3-values arrays:
    #      [0] is a token, or undef if we still do not know,
    #      [1] is a value for this token, and
    #      [2] is a line number where the token was read from)
    return 0 if $#read_tokens < 0;    # EOF reached
    my ($ra) = shift @read_tokens;

    # --- remember for better error messages
    $Last_returned_token = $$ra[1];
    $Token_line_number = $$ra[2];

###    print ("\nTOKEN ==> " . $$ra[1] . "\n");

    # --- non-attributes tokens are already fully prepared
    return ($$ra[0], $$ra[1]) if defined $$ra[0];
    
    # --- the first token from a definition?
    #     (if yes, it represents a data type which we will store in
    #      $Current_datatype to be able later find only valid
    #      tokens for this data type)
    unless (defined $Current_datatype) {
        $Current_datatype = &match_token ($$ra[1], $input, $DATA_TYPES);
        return $Current_datatype;
    }

    # --- everything what remains can be only a attribute name
    #     but it may be abbreviated, therefore we must call
    #     &match_token with the $Current_datatype to find the full
    #     token name
    return &match_token ($$ra[1], $input, $Current_datatype);
}

# -----------------------------------------------------------------------------
# Newline is usually ignored - but it may also indicates
# an end of an application/parameter definition. In these cases:
#    - if the current definition has not (yet) have an opening bracket '['
#      (which usually means that it is a definition for a variable)
# -----------------------------------------------------------------------------
sub is_defend {
    my ($c) = @_;
    return undef unless $c eq "\n";
    my ($newline_significant) = $Newline_significant;
    $Newline_significant = 0;
    return undef unless $#read_tokens > -1;
    local ($_);
    foreach (@read_tokens) {
        return undef if $$_[0] eq ord ('[');
    }
    return ($newline_significant ? 1 : undef);
}

# -----------------------------------------------------------------------------
# Ignore characters read from $input until a character
# $till appears (that one ignore also), or untill EOF
# -----------------------------------------------------------------------------
sub ignore_till {
    my ($input, $till) = @_;
    my ($c);
    while (($c = $input->getc) ne '') {
        if ($c eq $till) {
	    $input->ungetc;
	    return;
	}
    }
}
    
# -----------------------------------------------------------------------------
# Find (in the list %all_tokens) a proper token and return it;
# be prepared to match also 'shorter' names (e.g. this subroutine
# can get "def" and it is expected to return token $DEFAULT);
# if not found, call the error routine.
#
# Each token is matched with its own list defined by $datatype
# (which is a key to the hash %all_tokens).
# -----------------------------------------------------------------------------
sub match_token {
    my ($token, $input, $datatype) = @_;
    $token =~ tr/a-z/A-Z/;
    local ($_);
    my ($ra_my_tokens) = $all_tokens{$datatype};
    foreach (@$ra_my_tokens) {
	return eval ('$' . $_)  if $_ =~ /^\Q$token\E/;
    }
#    if ( $ENV{'YYDEBUG'} ) {
       local (@read_tokens); # keep value of @read_tokens (modified in yyerror)
       &yyerror ("Unrecognized token.", $input);
#    }
    return ($W2HUNKNOWN);
}

# ----------------------- that's all for parsing stuff ------------------------

1;
