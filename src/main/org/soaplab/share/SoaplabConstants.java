// SoaplabConstants.java
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

package org.soaplab.share;

/**
 * A list of constants used to describe and to invoke analyses. <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: SoaplabConstants.java,v 1.17 2011/05/13 10:14:47 marsenger Exp $
*/

public interface SoaplabConstants {

    //
    // Pre-defined constants for fault strings
    //

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOT_FOUND = "Soaplab::NotFound";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOT_RUNNABLE = "Soaplab::NotRunnable";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOT_RUNNING = "Soaplab::NotRunning";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOT_VALID_INPUTS = "Soaplab::NotValidInputs";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOT_ACCEPTABLE_INPUTS = "Soaplab::NotAcceptibleInputs";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOT_TERMINATED = "Soaplab::NotTerminated";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_UNKNOWN_NAME = "Soaplab::UnknownName";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOTIFY_UNACCEPTED = "Soaplab::NotifyUnaccepted";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_NOT_IMPLEMENTED = "Soaplab::NotImplemented";

    /**
     * A fault string.
     * @see <a href="{@docRoot}/overview-summary.html#faults">Defining exceptions</a>
     */
    static final String FAULT_INTERNAL_ERROR = "Soaplab::InternalError";

    //
    // Pre-defined constants for analysis properties
    //

     /** A property name for an analysis type. */
     static final String ANALYSIS_TYPE = "type";

     /** A property name for an analysis name. */
     static final String ANALYSIS_NAME = "name";

     /** A property name for an analysis module. */
     static final String ANALYSIS_MODULE = "module";

     /** A property name for an analysis supplier. */
     static final String ANALYSIS_SUPPLIER = "supplier";

     /** A property name for an analysis. Boolean value. */
     static final String ANALYSIS_EMBOSS = "emboss";

     /** A property name for an analysis version. */
     static final String ANALYSIS_VERSION = "version";

     /** A property name for an analysis installation. */
     static final String ANALYSIS_INSTALLATION = "installation";

     /** A property name for an analysis description. */
     static final String ANALYSIS_DESCRIPTION = "description";

    /** A property name for an analysis URL with a detailed help. */
    static final String ANALYSIS_HELP_URL = "help_URL";

    //
    // Pre-defined constants for input data properties
    //

    /** A property name of a data input. */
    static final String INPUT_TYPE = "type";

    /** A property name of a data input. */
    static final String INPUT_NAME = "name";

    /** A property name of a data input. */
    static final String INPUT_ALLOWED_VALUES = "allowed_values";

    /** A property name of a data input. */
    static final String INPUT_DEFAULT_VALUE = "default";

    /** A property name of a data input. */
    static final String INPUT_MANDATORY = "mandatory";   // boolean

    /** A property name of a data input. */
    static final String DEFAULT_FOR_DIRECT = "default_for_direct";  // boolean

    /** A property name of a data input. */
    static final String INPUT_FORMAT = "format";   // e.g. %3f or %d


    //
    // Pre-defined constants for input/output data properties
    //

    /** A property name of a parameter attribute. */
    static final String DATA_TYPE = "datatype";

    /** A property name of a parameter attribute. */
    static final String DATA_HELP = "help";

    /** A property name of a data attribute. */
    static final String DATA_PROMPT = "prompt";

    /** A property name of a data output. */
    static final String FILE_EXT = "extension";

    /** A property name of a data output. */
    static final String MIME_TYPE = "mimetype";

//     /** A property name of a data attribute. */
//     static final String DATA_SEMANTIC_TYPE = "semantic_type";

//     /** A property name of a data attribute. */
//     static final String DATA_SEMANTIC_TYPE_2 = "known_type";

//     /** A property name of a data attribute. */
//     static final String DATA_SYNTAX_TYPE = "syntax_type";

    //
    // Pre-defined constants for output data properties
    //

    /** A property name of a data output (a result). */
    static final String RESULT_NAME = "name";

    /** A property name of a data output (a result). */
    static final String RESULT_TYPE = "type";

    /** A property name of a data output (a result). Its value
     * indicates the resut size in bytes. */
    static final String RESULT_SIZE = "size";

    /** A property name of a data output (a result). Its value - used
     * only for results holding an array - indicates the number of
     * elements of the resuting array. */
    static final String RESULT_LENGTH = "length";

    /** A property name of a data output (a result). */
    static final String IGNORED_RESULT = "ignore";

    //
    // Pre-defined constants for job status
    //

    /** A job status: An unknown one. */
    static final String JOB_UNKNOWN = "UNKNOWN";

    /** A job status: Job has been created but not yet executed. */
    static final String JOB_CREATED = "CREATED";

    /** A job status: Job is running. */
    static final String JOB_RUNNING = "RUNNING";

    /** A job status: Job has completed execution. */
    static final String JOB_COMPLETED = "COMPLETED";

    /** A job status: Job was terminated by user request. */
    static final String JOB_TERMINATED_BY_REQUEST = "TERMINATED_BY_REQUEST";

    /** A job status: Job was terminated due to an error. */
    static final String JOB_TERMINATED_BY_ERROR = "TERMINATED_BY_ERROR";

    /** A job status: Job was removed. */
    static final String JOB_REMOVED = "REMOVED";


    //
    // Pre-defined names of the specialized results
    //

    /** A name for a specialized result. It represents a report result
     * - which is a result containing logs, provenance data and other
     * information about the environment and conditions of a
     * job/analysis invocation. */
    static final String RESULT_REPORT = "report";

    /** A name for a specialized result. It represents very short
     * (usually just a number) information how was a job/analysis
     * completed. */
    static final String RESULT_DETAILED_STATUS = "detailed_status";

    /** A suffix to a result name. Such result is accesible via a
     * URL. */
    static final String URL_RESULT_SUFFIX = "_url";

    //
    // Pre-defined names of the input/result types
    //

    /** A name of an input or a result type. It represents a binary
     * type. */
    static final String TYPE_BINARY_SIMPLE = "byte[]";

    /** A name of an input or a result type. It represents a text
     * (string) type. */
    static final String TYPE_TEXT_SIMPLE = "String";

    /** A name of an input or a result type. It represents an array of
     * binary inputs or results. */
    static final String TYPE_BINARY_ARRAY = "byte[][]";

    /** A name of an input or a result type. It represents an array of
     * text inputs or results. */
    static final String TYPE_TEXT_ARRAY = "String[]";

    /** A name of an input type. It represents a boolean
     * type. */
    static final String TYPE_BOOLEAN_SIMPLE = "boolean";


    //
    // Pre-defined constants for the date-time
    //

    /**
     * A string defining a full date-time format, used in reports
     */
    static final String DT_FORMAT = "yyyy-MMM-dd HH:mm:ss (z)";

    /** A name of a property returned by
     * <tt>AnalysisService.getCharacteristics()</tt>.
     */
    static final String TIME_CREATED = "created";

    /** A name of a property returned by
     * <tt>AnalysisService.getCharacteristics()</tt>.
     */
    static final String TIME_STARTED = "started";

    /** A name of a property returned by
     * <tt>AnalysisService.getCharacteristics()</tt>.
     */
    static final String TIME_ENDED = "ended";

    /** A name of a property returned by
     * <tt>AnalysisService.getCharacteristics()</tt>.
     */
    static final String TIME_ELAPSED = "elapsed";

    //
    // Pre-defined constants for suffixes of input property names
    //

    /**
     *  A suffix added to the input property representing direct data.
     */
    static final String DIRECT_DATA_SUFFIX = "_direct_data";

    /**
     *  A suffix added to the input property representing reference
     *  data.
     */
    static final String URL_SUFFIX         = "_url";

    /**
     *  A suffix added to the input property representing reference
     *  and EMBOSS specific data.
     */
    static final String USA_SUFFIX         = "_usa";


    //
    // Pre-defined constants for the rest...
    //

    /**
     * A string that delimits category name from a service name.
     */
    static final String SOAPLAB_SERVICE_NAME_DELIMITER = ".";

    /**
     * A Soaplab2 namespace used in WSDL etc.
     */
    static final String SOAPLAB_NAMESPACE = "http://org.soaplab.2";

    /**
     * A Soaplab2 list service name used in WSDL etc.
     */
    static final String LIST_SERVICE_NAME = "AnalysisList";

    /**
     * A Soaplab2 port name for the list service used in WSDL etc.
     */
    static final String LIST_SERVICE_PORT_NAME = "AnalysisList";

    /**
     * A Soaplab2 analysis service name used in WSDL etc.
     */
    static final String SERVICE_NAME = "Analysis";

    /**
     * A Soaplab2 port name for the analsis service used in WSDL etc.
     */
    static final String SERVICE_PORT_NAME = "Analysis";
    
    /**
     * Maximum waiting time for job terminate calls to return, 20 secs.
     */
    static final int JOB_TIMEOUT_FOR_TERMINATE_CALLS = 20000;

    /**
     * IP address of the client
     */
    static final String REMOTE_IP = "remote IP";

    /**
     * interface used by the client
     */
    static final String INTERFACE = "interface used";

}
