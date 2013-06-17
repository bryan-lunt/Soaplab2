// Tokenizer.java
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

import java.io.StringReader;
import java.io.StreamTokenizer;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * An extension to the Java standard <tt>StreamTokenizer</tt> that
 * also provides an array of all tokens from a given string. <p>
 *
 * This class is not thread-safe. Rather do not use the same instance
 * in more threads. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: Tokenizer.java,v 1.1 2006/11/24 01:30:39 marsenger Exp $
 */
public class Tokenizer
    extends StreamTokenizer {

    // characters used as delimiters separating tokens,
    // 'null' means that whitespace chars (0 - space) will be used
    char[] delimiters = null;

    /**************************************************************************
     * The main constructor. <p>
     *
     * @param str string to be tokenized
     **************************************************************************/
    public Tokenizer (String str) {
	super (new StringReader (str));
    }

    /**************************************************************************
     * Create an array of tokens found in a given string (given in
     * constructor). <p>
     *
     * @param delimiters an array of delimiting characters
     * @return an array of tokens
     **************************************************************************/
    public String[] tokenize (char[] delimiters) {
	this.delimiters = delimiters;
	return tokenize();
    }

    /**************************************************************************
     * Create an array of tokens found in a given string (given in
     * constructor). <p>
     *
     * @param delimiters a string of delimiting characters
     * @return an array of tokens
     **************************************************************************/
    public String[] tokenize (String delimiters) {
	this.delimiters = new char [delimiters.length()];
	delimiters.getChars (0, delimiters.length(), this.delimiters, 0);
	return tokenize();
    }

    /**************************************************************************
     * Create an array of tokens found in a given string (given in
     * constructor).  Delimitesr are whitespaces. <p>
     *
     * @return an array of tokens
     **************************************************************************/
    public String[] tokenize() {

	List<String> v = new ArrayList<String>();    // here build results

	resetSyntax();
	//    wordChars ('\u0021', 255);  // make almost all chars word chars
	wordChars (0, 255);  // make almost all chars word chars
	eolIsSignificant (false);
  	quoteChar ('"');            // " is quoted string delimiter
	setDelimiters();            // set delimiters (according to 'delimiters')

	try {
	    int c;
	    out:
	    while (true) {
		switch (c = nextToken()) {
		case StreamTokenizer.TT_EOF:
		    break out;
		case StreamTokenizer.TT_WORD:
		    v.add (sval);
		    break;
		case '"':
		    v.add (sval);
		    break;
		default:
		    v.add (String.valueOf (c));  // should not happen
		}
	    }
	} catch (IOException e) {
	    e.printStackTrace();   // should not happen from strings
	}

	String[] results = new String [v.size()];
	return v.toArray (results);
    }

    /**************************************************************************
     * setDelimiters()
     *   Using member field 'delimiters' it set delimiters to be used as separators
     *   between tokens.
     **************************************************************************/
    void setDelimiters () {
	if (delimiters == null || delimiters.length == 0)
	    whitespaceChars (0, ' ');   // default
	else
	    for (int i = 0; i < delimiters.length; i++)
		whitespaceChars (delimiters[i], delimiters[i]);
    }

}
