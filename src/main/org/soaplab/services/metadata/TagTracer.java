// TagTracer.java
//
// Created: October 2006
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

package org.soaplab.services.metadata;

import java.util.Stack;

/**
 * The class represents a stack of visited tags when parsing an XML
 * source. It helps when the same named tags appear in various parts
 * of the parsed source. <P>
 *
 * One can <tt>push()</tt> (at the beginning of every
 * <tt>DocumentHandler.startElement()</tt>) and <tt>pop()</tt>
 * (at the end of each <tt>DocumentHandler.endElement()</tt>) and
 * thus keep track of the currently processed tag and its path. <P>
 *
 * The TagTracer can also show the whole path, or just compare if a
 * given tag path is a current one. <P>
 *
 * Here is an example how to use it:
 *<PRE>
 *    <B>TagTracer tt</B>;
 *
 *    private static final String[] ANALYSIS_PATH = new String[] { "DsLSRAnalysis", "analysis" };
 *    private static final String[] EXT_PATH      = UUtils.join (ANALYSIS_PATH, new String[] { "analysis_extension" });
 *    private static final String[] APP_INFO_PATH = UUtils.join (EXT_PATH,      new String[] { "app_info" });
 *    private static final String[] EVENT_PATH    = UUtils.join (EXT_PATH,      new String[] { "event" });
 *    ...
 *
 *    public void startElement (String name, AttributeList attrs) {
 *        <B>tt.push (name);</B>
 *
 *    if (<B>tt.is (ANALYSIS_PATH)</B>) {
 *            // do something for "analysis"
 *            // ...
 *        }
 *
 *    public void endElement (String name) {
 *        // ...
 *        <B>tt.pop()</B>;
 *    }
 *</PRE>
 * <P>
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: TagTracer.java,v 1.1.1.1 2006/11/03 09:15:01 marsenger Exp $
 */
public class TagTracer {

    /** Version and date of last update of this class. */
    public static final String VERSION = "$Id: TagTracer.java,v 1.1.1.1 2006/11/03 09:15:01 marsenger Exp $";

    // here we keep the stack of visited tags (a "tag path")
    private Stack<String> stack = new Stack<String>();

    /*********************************************************************
     * A default constructor.
     ********************************************************************/
    public TagTracer() { }

    /********************************************************************
     * Pushes a tag name onto the top of this stack.
     * <P>
     * @param name a tag name (usually of the currently visited tag)
     *             to be put on the stact
     ********************************************************************/
    public void push (String name) {
        stack.push (name);
    }

    /********************************************************************
     * Removes the tag name from the top of this stack and returns it.
     * <P>
     * @return the removed tag name
     * @throws EmptyStackException if there is no element on the stack
     ********************************************************************/
    public String pop() {
        return stack.pop();
    }

    /*********************************************************************
     * Looks at the tag name at the top of this stack without removing it
     * from the stack.
     * <P>
     * @return the currently last tag name on the stac
     * @throws EmptyStackException if there is no element on the stack
     ********************************************************************/
    public String peek() {
        return stack.peek();
    }

    /*********************************************************************
     * Compares the contents of the current stack with the given list
     * of tag names.
     * <P>
     * <EM>TBD: to allow '*' in <tt>tagPath></tt> to represents 'anything'
     *         - even more elements in a sequence (but then again - I do
     *         not want to simulate here the whole XPath :-) )</EM>
     * <P>
     * @param tagPath a list of tag names to be compared with the contents
     *                of the stack
     * @return true if the contents of the current stack is the same
     *         as the given <tt>tagPath</tt>.
     ********************************************************************/
    public boolean is (String[] tagPath) {
        synchronized (stack) {
	    if (stack.size() != tagPath.length) return false;
            for (int i = tagPath.length-1; i >= 0; i--)
                if (! tagPath[i].equals (stack.elementAt (i)))
		    return false;
	    return true;
	}
    }

    /*********************************************************************
     * Gets contents of the whole stack.
     * <P>
     * @return list of all tag names currently being in the stack
     ********************************************************************/
    public String[] getPath() {
	synchronized (stack) {
	    String[] path = new String [stack.size()];
	    stack.copyInto (path);
	    return path;
	}
    }

    /*********************************************************************
     * Removes all elements fom the stack.
     ********************************************************************/
    public void reset() {
        stack.removeAllElements();
    }

}

