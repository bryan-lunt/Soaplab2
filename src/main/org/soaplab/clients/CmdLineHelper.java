// CmdLineHelper.java
//
// Created: November 2006
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

package org.soaplab.clients;

import org.soaplab.services.GenUtils;
import org.tulsoft.tools.BaseCmdLine;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * An abstract parent of various command-line tools. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: CmdLineHelper.java,v 1.11 2008/02/03 16:18:24 marsenger Exp $
 */
abstract public class CmdLineHelper {

    protected static boolean verbose = true;
    protected static boolean printStack = false;
    protected static boolean doNotExit = false;

    /*************************************************************************
     * Return a help text how to use a class 'helpOwner'. See the code
     * for the rules how to name the help file and where to put it.
     *************************************************************************/
    public static String getUsage (Class helpOwner) {
	String helpFile = "help/" + helpOwner.getSimpleName() + "_usage.txt";
	try {
	    return GenUtils.readResource (helpFile, helpOwner);
	} catch (IOException e) {
	    return
		"Sorry but an error occured.\n" +
		"I cannot find or read the help file: " +
		e.getMessage() + "\n";
	}
    }

    /*************************************************************************
     * Print and underline message 'title' - but only in the verbose
     * mode.
     *************************************************************************/
    protected static void title (String title) {
	title (title, null);
    }

    /*************************************************************************
     * Print and underline message 'title' to the given writer (which
     * can be null) - but only in the verbose mode.
     *************************************************************************/
    protected static void title (String title, PrintWriter pw) {
	if (! verbose)
	    return;
	msgln (title, pw);
	for (int i = 0; i < title.length(); i++)
	    msg ("-", pw);
	msgln ("", pw);
    }

    /*************************************************************************
     * Print 'message'.
     *************************************************************************/
    protected static void msg (String message) {
	System.out.print (message);
    }

    /*************************************************************************
     * Print 'message' and a newline.
     *************************************************************************/
    protected static void msgln (String message) {
	System.out.println (message);
    }

    /*************************************************************************
     * Print 'message' - but only in the verbose mode.
     *************************************************************************/
    protected static void qmsg (String message) {
	if (verbose)
	    msg (message);
    }

    /*************************************************************************
     * Print 'message' and a newline - but only in the verbose mode.
     *************************************************************************/
    protected static void qmsgln (String message) {
	if (verbose)
	    msgln (message);
    }

    /*************************************************************************
     * Print 'message' and a newline to the standard error output.
     *************************************************************************/
    protected static void emsgln (String message) {
	System.err.println (message);
    }

    /*************************************************************************
     * Print 'message' to the given writer, or pass it to a normal msg().
     *************************************************************************/
    protected static void msg (String message, PrintWriter pw) {
	if (pw == null) {
	    System.out.print (message);
	} else {
	    pw.print (message);
	}
    }

    /*************************************************************************
     * Print 'message' and a newline to the given writer, or pass it
     * to a normal msgln().
     *************************************************************************/
    protected static void msgln (String message, PrintWriter pw) {
	if (pw == null) {
	    System.out.println (message);
	} else {
	    pw.println (message);
	}
    }

    /*************************************************************************
     * Return an instance representing command-line parameters. If
     * command-line contains a '-h' option, print the help and exit.
     *************************************************************************/
    protected static BaseCmdLine getCmdLine (String[] args, Class classOwner) {
	BaseCmdLine cmd = new BaseCmdLine (args, true);
	if (cmd.hasParam ("-help") || cmd.hasParam ("-h")) {
	    msg (getUsage (classOwner));
	    System.exit (0);
	}
	verbose = ! cmd.hasOption ("-q");
	printStack = cmd.hasOption ("-stack");
	doNotExit = cmd.hasOption ("-donotexit");
	return cmd;
    }

    /*************************************************************************
     * Decorate and print given exception, perhaps also with a stack
     * trace.
     *************************************************************************/
    protected static void processErrorAndExit (Throwable e) {
	System.err.println ("===ERROR===");
	boolean seriousError =
	    (e instanceof java.lang.RuntimeException);
	if (seriousError || printStack) {
	    e.printStackTrace();
	} else {
	    if (e.getMessage() == null)
		System.err.println (e.toString());
	    else
		System.err.println (e.getMessage());
	}
	System.err.println ("===========");
	exit (1);
    }

    /*************************************************************************
     * Just exit with the given exit code - unless exiting is disabled
     * by the <tt>-donotexit</tt> option.
     *************************************************************************/
    protected static void exit (int exitCode) {
	if (!doNotExit)
	    System.exit (exitCode);
    }

}
