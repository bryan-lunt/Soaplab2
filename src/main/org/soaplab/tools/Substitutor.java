// Substitutor.java
//
// Created: March 1998, Refreshed: November 2006
//
// Copyright 2006 Martin Senger
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package org.soaplab.tools;

import org.apache.commons.lang.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/** The class is an engine replacing variables in an expression by their
 *  values.
 *<P>
 *   The only public method of this class is <tt>process</tt>. It goes
 *   through a given template and tries to locate and substitute there some
 *   expressions (described below). The substitutions may be single values
 *   or arrays of values. After that, the whole result is separated into
 *   an array of values, using default or given separator.
 *<P>
 *   This class was developed for one particular project but later became
 *   more general - and now may be used independently. The original project
 *   needed it (and still needs it) for preparing command-line arguments -
 *   therefore the last step - separation into an array.
 *<P>
 *   It uses term <b>template</b> for a string which is processed and whose
 *   parts are being substituted.
 *<P>
 *   It uses term <b>parameter</b> to identify what should be the new, substituted
 *   value. A <em>parameter</em> is an object having a name (also called a tag, or a
 *   qualifier) and one or more values (more values are also called repetitive
 *   values). It also has a unique identifier - this identifier is used in the
 *   templates to identify parameters, and is replaced by parameter value,
 *   or parameter values, or by a parameter name. Any object can be a <em>parameter</em>
 *   as long as it implements the interface {@link ParameterAccessor}.
 *<P>
 *   The following table contains all recognized constructs in templates. Note
 *   that plural <em>values</em> is used when a parameter can have repetitive
 *   values. The terms <em>pieces</em> and <em>separator</em> are related to the
 *   final step after substitutions are done - they are described later.
 *<blockquote>
 *<table border=3 cellpading=5 cellspacing=5>
 *<tr> <td><b>construct</b></td> <td><b>will be replaced by</b></td> <td><b>notes</b></td> </tr>
 *
 *<tr> <td align=center colspan=3><em>Parameter values</em></td> </tr>
 *
 *<tr>
 * <td valign=top><b>$id</b></td>
 * <td valign=top>value(s) of parameter "id"</td>
 * <td valign=top>&nbsp;</td>
 *</tr>
 *
 *<tr>
 * <td valign=top><b>${id}</b></td>
 * <td valign=top>value(s) of parameter "id"</td>
 * <td valign=top>used when the construct is followed immediately by a letter (like in <tt>${id}A</tt>)</td>
 *</tr>
 *
 *<tr>
 * <td valign=top><b>$"id"</b></td>
 * <td valign=top>value(s) of parameter "id"</td>
 * <td valign=top>new value(s) will not be separated into pieces even if they contain separators</td>
 *</tr>
 *
 *<tr>
 * <td valign=top><b>${"id"}</b></td>
 * <td valign=top>value(s) of parameter "id"</td>
 * <td valign=top>the same as above</td>
 *</tr>
 *
 *<tr> <td align=center colspan=3><em>Parameter name</em></td> </tr>
 *
 *<tr>
 * <td valign=top><b>&id</b></td>
 * <td valign=top>name of parameter "id"</td>
 * <td valign=top>&nbsp;</td>
 *</tr>
 *
 *<tr>
 * <td valign=top><b>&{id}</b></td>
 * <td valign=top>name of parameter "id"</td>
 * <td valign=top>the same as <tt>&id</tt> but this one protect from being mixed with the subsequent letters</td>
 *</tr>
 *
 *<tr> <td align=center colspan=3><em>Counts of repetitive values</em></td> </tr>
 *
 *<tr>
 * <td valign=top><b>#</b></td>
 * <td valign=top>number of reoeated values</td>
 * <td valign=top>when used <em>outside</em> of <tt>foreach()</tt> constructs</td>
 *</tr>
 *
 *<tr>
 * <td valign=top><b>#</b></td>
 * <td valign=top>"one-based" index of the current value</td>
 * <td valign=top>when used <em>inside</em> of <tt>foreach()</tt> constructs</td>
 *</tr>
 *
 *<tr> <td align=center colspan=3><em>Functions</em></td> </tr>
 *
 *<tr>
 * <td valign=top><b>foreach (id, expr)</b></td>
 * <td valign=top>an array of expressions, each of them created from a sub-template "expr",
 *                the length of the array depends on the number of repetitive values of parameter "id"</td>
 * <td valign=top>a family of <tt>foreach</tt> functions makes sense only for parameters with repetitive values,<P>
 *                any sub-template "expr" can contain any of the constructs above (but not more <tt>forech</tt>
 *                functions)</td>
 *</tr>
 *
 *<tr>
 * <td valign=top><b>foreachs (id, delimiter, expr)</b></td>
 * <td valign=top>as above but now it concatenates the resulting array using "delimiter"</td>
 * <td valign=top>&nbsp;</td>
 *</tr>
 *
 *<tr>
 * <td valign=top><b>foreachsq (id, delimiter, expr)</b></td>
 * <td valign=top>as above</td>
 * <td valign=top>the resulting string is here treated as a quoted string, which means that it is not
 *                cat into individual pieces (even if it contains separator characters)</td>
 *</tr>
 *
 *</table>
 *</blockquote>
 *
 * Now about the separation into pieces.
 *<P>
 * Every instance of Substitutor knows about a string called <b>separator</b>.
 * By default it is a space, but it can be anything and can be longer than one character.
 * A template, after being substituted, is divided into pieces by
 * this separator (and separators disappear). Be aware that not only
 * separators found in the original template are used, but also separators
 * appearing in newly substituted values of parameters are used. For example:
 * <PRE>
 *    Template:        -par $abc
 *    Value of $abc:   Mary Poppins
 *    The resulting array has three elements: -par | Mary | Poppins
 * </PRE>
 *
 * But there are three exceptions from this rule:
 * <OL TYPE="a">
 *  <LI> separators found in substitution of $"id" construct are not used as separators
 *  <LI> separators found inside of quoted parts of template are not used as separators
 *  <LI> when <tt>$id</tt> returns more than one value (that can be for repetitive values)
 *       then this returned array of values is considered as already separated into pieces
 *       and is not separated any more, for example:
 *       <blockquote>
 *       Template: -par ($abc)<BR>
 *       Values of $abc has two elements: foo | bar<BR>
 *       The resulting array has five elements: -par | ( | foo | bar | )
 *       </blockquote>
 * </OL>
 *<P>
 * Here is a table with examples how separation is done (all examples use
 * default separator):
 * <blockquote>
 * <table border=3 cellspacing=5 cellpadding=5>
 * <tr> <td><b>template</b></td> <td><b>values of parameters</b></td> <td><b>resulting argument list</b></td> </tr>
 *
 * <tr>
 *   <td valign=top>-par $abc</td>
 *   <td valign=top>$abc: Mary Poppins</td>
 *   <td valign=top><OL><LI>-par<LI>Mary<LI>Poppins</OL></td>
 * </tr>
 *
 * <tr>
 *   <td valign=top>$a$b$c</td>
 *   <td valign=top> $a: My name<BR> $b: is<BR> $c: Kim TheBig</td>
 *   <td valign=top><OL><LI>My<LI>nameisKim<LI>TheBig</OL></td>
 * </tr>
 *
 * <tr>
 *   <td valign=top>"$a$b$c"</td>
 *   <td valign=top>as above</td>
 *   <td valign=top><OL><LI>My nameisKim TheBig</OL></td>
 * </tr>
 *
 * <tr>
 *   <td valign=top>$"a" $b $"c"</td>
 *   <td valign=top>as above</td>
 *   <td valign=top><OL><LI>My name<LI>is<LI>Kim TheBig</OL></td>
 * </tr>
 *
 * <tr>
 *   <td valign=top>-par ($abc)</td>
 *   <td valign=top>$abc is an array with two values<BR> <OL><LI>Mary<LI>Poppins</OL></td>
 *   <td valign=top><OL><LI>-par<LI>(<LI>Mary<LI>Poppins<LI>)</OL></td>
 * </tr>
 *
 * </table>
 * </blockquote>
 *
 * <P>
 * Now few more comments on the <b>foreach</b> constructs.
 *<P>
 * If a <em>foreach</em> construct ("function") is noticed, then the whole construct is
 * extracted from the template, the function is evaluated and return values are put back
 * (see examples below).
 *<P>
 * The signatures and the meaning of these functiuons are:
 * <blockquote>
 * <DL>
 *   <DT><tt>String[] foreach (String id, String expr)</tt>
 *   <DD> Returns an array of substituted expressions, each created from 'expr'.
 *        Number of repetitions is given according the parametr 'id'.
 *   <P>
 *   <DT><tt>String foreachs (String id, String delimiter, String expr)</tt>
 *   <DD> the same as 'foreach' above, plus it concatenates elements of the
 *       resulting array using 'delimiter' (I use here term 'delimiter' to make
 *       it different from 'separator' which is used to specify where the results
 *       will be cut to pieces. Delimiter can be different from the separator.)
 *   <P>
 *   <DT><tt>String foreachsq (String id, String delimiter, String expr)</tt>
 *   <DD> The same as 'foreachs' above but the resulting string is treated as
 *       quoted string, i.e it is not separated into individual pieces.
 * </DL>
 * </blockquote>
 * <P>
 * Now several examples with repetitive values and <tt>foreach</tt> constructs:
 * <blockquote>
 * <table border=3 cellspacing=5 cellpadding=5>
 * <tr> <td><b>template</b></td> <td><b>values of parameters</b></td> <td><b>resulting argument list</b></td> </tr>
 *
 * <tr>
 *   <td valign=top>-fields $x</td>
 *   <td valign=top>$x is an array with three values<BR> <OL><LI>value1<LI>value2<LI>value3</OL></td>
 *   <td valign=top><OL><LI>-fields<LI>value1<LI>value2<LI>value3</OL></td>
 * </tr>
 *
 * <tr>
 *   <td valign=top>-fields # $x</td>
 *   <td valign=top>as above</td>
 *   <td valign=top><OL><LI>-fields<LI>3<LI>value1<LI>value2<LI>value3</OL></td>
 * </tr>
 *
 * <tr>
 *   <td valign=top>foreach (x, "-fields# $x")</td>
 *   <td valign=top>as above</td>
 *   <td valign=top><OL><LI>-fields1 value1<LI>-fields2 value2<LI>-fields3 value3</OL></td>
 * </tr>
 *
 * <tr>
 *   <td valign=top>foreachs (x, " ", "-fields# $x"</td>
 *   <td valign=top>as above</td>
 *   <td valign=top><OL><LI>-fields1<LI>value1<LI>-fields2<LI>value2<LI>-fields3<LI>value3</OL></td>
 * </tr>
 *
 * </table>
 * </blockquote>
 * <P>
 * 
 * Finally, in template some <b>escaped characters</b> can be recognized and substituted:
 * <blockquote>
 * <table border=3 cellpadding=5 cellspacing=5>
 * <tr> <td><b>these two characters</b></td> <td><b>are replaced by a</b></td> </tr>
 *
 * <tr> <td>\n</td> <td>newline</td> </tr>
 * <tr> <td>\r</td> <td>carriage return</td> </tr>
 * <tr> <td>\t</td> <td>tabelator</td> </tr>
 * <tr> <td>\s</td> <td>space</td> </tr>
 * <tr> <td>\<em>any character</em></td> <td><em>this charater</em></td> </tr>
 *
 * </table>
 * </blockquote>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Substitutor.java,v 1.3 2007/04/19 22:17:06 marsenger Exp $
 */

public class Substitutor {

    private static final org.apache.commons.logging.Log log =
       org.apache.commons.logging.LogFactory.getLog (Substitutor.class);

    String template;              // where to make substitutions
    String separator = " ";       // how to separate results after substitutions
    ParameterAccessor accessor;   // access to parameter values and qualifiers names

    // used by process() when a request for parameter value is encountered:
    // if 'idx' == -1 then an entire array of repeated values is asked for,
    // otherwise only an index 'idx' of a parameter value is used
    int idx = -1;

    /******************************************************************************
     * static initializer
     ******************************************************************************/

    // for identifying a value of a parameter
    static Pattern re_value1;          // $id
    static Pattern re_value2;          // ${id}
    static Pattern re_quotedValue1;    // $"id"
    static Pattern re_quotedValue2;    // ${"id"}

    // for identifying a qualifier name of a parameter
    static Pattern re_qualifier1;      // &id
    static Pattern re_qualifier2;      // &{id}

    static {
	re_value1 = Pattern.compile ("^\\$[a-zA-Z0-9_.]+");
	re_value2 = Pattern.compile ("^\\$\\{[a-zA-Z0-9_.]+\\}");
	re_quotedValue1 = Pattern.compile ("^\\$\"[a-zA-Z0-9_.]+\"");
	re_quotedValue2 = Pattern.compile ("^\\$\\{\"[a-zA-Z0-9_.]+\"\\}");
	re_qualifier1 = Pattern.compile ("^\\&[a-zA-Z0-9_.]+");
	re_qualifier2 = Pattern.compile ("^\\&\\{[a-zA-Z0-9_.]+\\}");
    }

    /******************************************************************************
     * @param template a string to be parsed and substituted
     * @param separator used for cutting the substituted string into pieces
     * @param accessor an access to the parameter values
     ******************************************************************************/
    public Substitutor (String template, String separator, ParameterAccessor accessor) {
	this.template = template;
	if (StringUtils.isNotEmpty (separator))
	    this.separator = separator;
	this.accessor = accessor;
    }

    /******************************************************************************
     * Using default separator (spaces).
     * <P>
     * @param template a string to be parsed and substituted
     * @param accessor an access to the parameter values
     ******************************************************************************/
    public Substitutor (String template, ParameterAccessor accessor) {
	this.template = template;
	this.accessor = accessor;
    }

    /******************************************************************************
     *   Parses given 'template'
     *   and tries to locate and substitute there some expressions using rules
     *   described at the beginning of this class.
     *<P>
     * @return substituted string
     ******************************************************************************/
    public synchronized String[] process() {

	List<String> results = new ArrayList<String>();
	StringBuffer result = new StringBuffer (100);
	int sepLen = separator.length();
	String[] paramValue = null;
	Token token;
	boolean insideQuotes = false;

	for (int i = 0; i < template.length(); i++) {

	    // first process escape characters
	    if ((token = getTokenEscape (i)) != null) {
	        result.append (token.id);
	        i += token.length - 1;


	    // we might change the mode: inside quotes vs. outside quotes
	    // (note that if you need a real quotes, you must escape it)
	    } else if (template.charAt(i) == '"') {
		insideQuotes = !insideQuotes;

	    // do we have a counter of repetitions?
	    } else if (template.charAt(i) == '#') {
		log.debug ("Found counter at position: " + i);
		if (idx == -1)
		    result.append (accessor.getCount());
		else
		    result.append (idx + 1);   // because we want the output to be "one-based"

	    // do we have an identifier (candidate for a value substitution) ?
	    } else if ((token = getTokenValue (i)) != null ||
		       (token = getFunction (i))   != null) {
		if (log.isDebugEnabled())
		    log.debug ("Found: " + token.id + " of length " + token.length + " (" + 
			       (token.quoted ? "quoted" : "non-quoted") + ")");

	        // we found an identifier or a function, and get their value
	        // (actually the value can be an array of values, already separated into
	        // individual command line arguments)
		paramValue = token.replaceBy;

	        // the found value can contain separators, so we might make several values from it
		if (paramValue != null && paramValue.length > 0) {

	            // if we have got an array of values we send it to the output exactly the same,
	            // so we do not try to concatenate it with the previous and/or the next text
	            // (even if the value is not marked as quoted)
		    if (paramValue.length > 1) {
			if (result.length() > 0)                     // process the previous buffer
			    results.add (result.toString());
			for (int j = 0; j < paramValue.length; j++)  // then send all values to output
			    results.add (paramValue [j]);
			result = new StringBuffer (100);             // and then clean the whole buffer

	            // if we have got only one value we must look if it is marked as quoted or not;
	            // a non-quoted value is simply concatenated to the current buffer, and we go
	            // through it looking for separators
		    } else  if (!token.quoted && !insideQuotes) {
			int lastPos = result.length();   // everything before lastPos was already processed
			result.append (paramValue[0]);
			String tmpStr = result.toString();
			boolean firstSeparator = true;
			int delayedPos = 0;      // always at the beginning of a not-yet-processed parameter
			for (int j = lastPos; j < tmpStr.length(); j++)
			    if (tmpStr.regionMatches (j, separator, 0, sepLen)) {
				if (firstSeparator) {
				    firstSeparator = false;
				    if (delayedPos < j)
					results.add (tmpStr.substring (delayedPos, j));
				}
				j += sepLen - 1;
				delayedPos = j + 1;
			    } else {
				firstSeparator = true;
			    }
			result = new StringBuffer (tmpStr.substring (delayedPos));

	            // and here we have got again only one value, but it is marked as quoted,
	            // so we do not search for separators inside the value and transfer it directly
		    } else {
			result.append (paramValue[0]);
		    }
		}

		i += token.length - 1;


	    // or, do we have a plea for a qualifier name ?
	    } else if ((token = getTokenName  (i)) != null) {
		if (log.isDebugEnabled()) 
		    log.debug ("Found qualifier: " + token.id + " of length " + token.length);
		result.append (accessor.getQualifier (token.id));
		i += token.length - 1;


	    // or, do we have a separator ?
	    } else if (template.regionMatches (i, separator, 0, sepLen) && !insideQuotes) {
		if (result.length() > 0) {
		    results.add (result.toString());
		    result = new StringBuffer (100);
		}
		i += sepLen - 1;


	    // or, finally, do we have something else ?
	    } else {
		result.append (template.charAt (i));
	    }
	}

	// do not forget the last piece
	if (result.length() > 0)
	    results.add (result.toString());

	// make resulting strings and return them
	String[] paramList = new String [results.size()];
	return results.toArray (paramList);
    }

    /******************************************************************************
     * getTokenValue()
     ******************************************************************************/
    private Token getTokenValue (int pos) {
	if (template.charAt (pos) != '$') return null;
	String substr = template.substring (pos);
	String tokenId;
	String matched;
	Matcher matcher;

	matcher = re_value1.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    tokenId = matched.substring (1);
	    return new Token (matched.length(),
			      tokenId,
			      false,
			      getValues (tokenId));
	}

	matcher = re_quotedValue1.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    tokenId = matched.substring (2, matched.length()-1);
	    return new Token (matched.length(),
			      tokenId,
			      true,
			      getValues (tokenId));
	}

	matcher = re_value2.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    tokenId = matched.substring (2, matched.length()-1);
	    return new Token (matched.length(),
			      tokenId,
			      false,
			      getValues (tokenId));
	}

	matcher = re_quotedValue2.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    tokenId = matched.substring (3, matched.length()-2);
	    return new Token (matched.length(),
			      tokenId,
			      true,
			      getValues (tokenId));
	}
	return null;
    }

    //
    // getValues()
    //
    String[] getValues (String id) {
	if (idx == -1)
	    return accessor.getValues (id);
	else
	    return new String[] { accessor.getValue (id, idx) };
    }

    /******************************************************************************
     * getFunction()
     *   If a function invocation is noticed, then the whole function call is
     *   extracted from the template, the function is called and return values
     *   of the function are returned in 'replaceBy' field.
     *
     *   Recognized functions are:
     *
     *     String[] foreach (String id, String expr)
     *       Returns an array of substituted expressions, each created from 'expr'.
     *       Number of repetitions is given according the parametr 'id'.
     *
     *     String   foreachs  (String id, String delimiter, String expr)
     *       the same as 'foreach' above, plus it concatenates elements of the
     *       resulting array using 'delimiter' (I use here term 'delimiter' to make
     *       it different from 'separator' which is used to specify where the results
     *       will be cut to create the individual command-line parameters. Delimiter
     *       can be different from that separator.)
     *
     *     String   foreachsq (String id, String delimiter, String expr)
     *       The same as 'foreachs' above but the resulting string is treated as
     *       quoted string, i.e it is not separated into individual command-line
     *       parameter (even if it contains separator characters).
     *
     ******************************************************************************/

    // supported functions
    static final int FOREACH      =  1;
    static final int FOREACHS     =  2;
    static final int FOREACHSQ    =  3;

    // static initializer
    static Pattern re_foreach;      // foreach (id, expr)
    static Pattern re_foreachs;     // foreachs (id, delimiter, expr)
    static Pattern re_foreachsq;    // foreachsq (id, delimiter, expr)

    static {
	re_foreach = Pattern.compile ("^foreach\\s*\\(");
	re_foreachs = Pattern.compile ("^foreachs\\s*\\(");
	re_foreachsq = Pattern.compile ("^foreachsq\\s*\\(");
    }

    private Token getFunction (int pos) {
	if (template.charAt (pos) != 'f') return null;   // optimalisation

	String substr = template.substring (pos);
	String matched;
	Matcher matcher;

	matcher = re_foreach.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    return callFunction (FOREACH, pos, pos + matched.length(), 2);
	}

	matcher = re_foreachs.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    return callFunction (FOREACHS, pos, pos + matched.length(), 3);
	}

	matcher = re_foreachsq.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    return callFunction (FOREACHSQ, pos, pos + matched.length(), 3);
	}

	return null;
    }

    /******************************************************************************
     * Given name of a function ('fce'), its starting position in
     * 'template' ('startPos'), starting position of its parameters in
     * 'template' ('posOfParams') and expected number of parameters
     * for this function ('expectedParamCount'), it calls the function
     * and returns a token created from the given values and from the
     * results returned by the function.
     ******************************************************************************/
    private Token callFunction (int fce, int startPos, int posOfParams, int expectedParamCount) {

	// first extract an array of function's parameters
	int lastPos = indexOf (template, posOfParams, ')');
	if (lastPos == -1) {
	    if (log.isDebugEnabled())
		log.debug ("No enclosing parenthesis found for: " + template.substring (startPos));
	    return null;    // no enclosing parenthesis found
	}
	String paramStr = template.substring (posOfParams, lastPos);
	log.debug ("To be tokenized: " + paramStr);
	Tokenizer tokenizer = new Tokenizer (paramStr);
	String[] params = tokenizer.tokenize (" ,");
	if (params.length != expectedParamCount) {
	    if (log.isDebugEnabled()) {
		log.debug ("Wrong number of parameters for: " +
			   template.substring (startPos, lastPos + 1) +
			   "\n\texpected: " + expectedParamCount +
			   "\n\tfound: " + params.length);
	    }
	    return null;    // wrong numner of parameters
	}

	// then call the function
	String[] results;
	boolean quoted = false;
	switch (fce) {
	case FOREACH:
	    results = doForeach (params[0], params[1]);
	    break;
	case FOREACHSQ:   // intentionally no 'break'
	    quoted = true;
	case FOREACHS:
	    StringBuffer buffer = new StringBuffer (100);
	    String pieces[] = doForeach (params[0], params[2]);
	    if (pieces.length > 0)
		buffer.append (pieces[0]);
	    for (int i = 1; i < pieces.length; i++)
		buffer.append (params[1] + pieces[i]);
	    results = new String[] { new String (buffer) };
	    break;
	default:
	    results = new String[0];    // should not happen
	}

	// finally create and retur a token
	return new Token (lastPos - startPos + 1,
			  template.substring (startPos, lastPos + 1),
			  quoted,
			  results);
    }

    /******************************************************************************
     * doForeach()
     *   Implementation of individual functions.
     ******************************************************************************/
    private String[] doForeach (String id, String expr) {
	String saveTemplate = template;
	template = "\"" + expr + "\"";
	int saveIdx = idx;

	List<String> v = new ArrayList<String>();
	int count = accessor.getCount (id);
	for (int i = 0; i < count; i++) {
	    idx = i;
	    String[] results = process();
	    if (results.length > 0)
		v.add (results[0]);
	}
	idx = saveIdx;
	template = saveTemplate;
	String[] results = new String [v.size()];
	return v.toArray (results);
    }

    /******************************************************************************
     * getTokenName()
     *   (Note that field 'replaceBy' is not used in the returned token.)
     ******************************************************************************/
    private Token getTokenName (int pos) {
	if (template.charAt (pos) != '&') return null;
	String substr = template.substring (pos);
	String matched;
	Matcher matcher;

	matcher = re_qualifier1.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    return new Token (matched.length(),
			      matched.substring (1),
			      true,
			      null);
	}

	matcher = re_qualifier2.matcher (substr);
	if (matcher.lookingAt()) {
	    matched = matcher.group();
	    return new Token (matched.length(),
			      matched.substring (2, matched.length()-1),
			      true,
			      null);
	}

	return null;
    }

    /******************************************************************************
     * getTokenEscape()
     *   (Note that 'replaceBy' is not used in the returned token - instead the field
     *   'id' contains the new value.)
     ******************************************************************************/
    private Token getTokenEscape (int pos) {
	if (template.charAt (pos) != '\\') return null;
	if (template.length() == pos + 1) return null;   // buffer is not long enought
	char escaped = template.charAt (pos + 1);
	if (escaped == 'n')
	    return new Token (2, "\n", false, null);
	else if (escaped == 's')
	    return new Token (2, " ", false, null);
	else if (escaped == 'r')
	    return new Token (2, "\r", false, null);
	else if (escaped == 't')
	    return new Token (2, "\t", false, null);
	else
	    return new Token (2, "" + escaped, false, null);
    }

    /******************************************************************************
     * indexOf()
     *   Return a position of character 'searchFor' in string 'str' (after starting
     *   position 'startPos'). Or -1 if not found.
     *   If differs from String.indexOf() by takeing into account quoted and
     *   escaped characters.
     ******************************************************************************/
    private static int indexOf (String str, int startPos, char searchedFor) {

	boolean insideQuotes = false;
	char c;
	for (int i = startPos; i < str.length(); i++) {
	    c = str.charAt (i);
	    if (c == '\\')   // ignore any escaped characters
		i++;
	    else if (c == '"')
		insideQuotes = !insideQuotes;
	    else if (c == ')' && !insideQuotes)
		return i;
	}
	return -1;
    }

    /******************************************************************************
     * 
     * class Token
     *
     *   For returning an internal structure containing info about found token
     *   (its length, name and indicator if the token value should be quoted or
     *   not (it means if the value can be separated in case it contains separators).
     *   It can also contain a new value which will be used to replace the token.
     *
     ******************************************************************************/
    class Token {
	int      length;
	String   id;
	boolean  quoted;
	String[] replaceBy;

	public Token (int length, String id, boolean quoted, String[] replaceBy) {
	    this.length = length;
	    this.id = id;
	    this.quoted = quoted;
	    this.replaceBy = replaceBy;
	}

    }

}
