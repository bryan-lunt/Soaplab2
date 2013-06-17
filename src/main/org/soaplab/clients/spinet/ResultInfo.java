// ResultInfo.java
//
// Created: November 2007
//
// Copyright 2007 Martin Senger
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

package org.soaplab.clients.spinet;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A simple bean-like container keeping information about a Soaplab
 * result (but not the result itself). <p>
 *
 * @author <A HREF="mailto:martin.senger@gmail.com">Martin Senger</A>
 * @version $Id: ResultInfo.java,v 1.4 2007/12/06 16:42:58 marsenger Exp $
 */

public class ResultInfo {

    private String name;
    private String displayName;
    private String type;
    private String size;
    private String length;
    private String href;
    private boolean isArray;
    private String fileExtension;
    private String contentType;

    /*************************************************************************
     * A default constructor. <p>
     *************************************************************************/
    public ResultInfo() {
    }

    /*************************************************************************
     * Return the name of this result.
     *************************************************************************/
    public String getName() {
	return name;
    }

    /*************************************************************************
     * Set a name of this result.
     *************************************************************************/
    public void setName (String name) {
	this.name = name;
    }

    /*************************************************************************
     * Return a more readable name of this result.
     *************************************************************************/
    public String getDisplayName() {
	return (displayName == null ? name : displayName);
    }

    /*************************************************************************
     * Set a more readable name of this result.
     *************************************************************************/
    public void setDisplayName (String displayName) {
	this.displayName = displayName;
    }

    /*************************************************************************
     * Return the type of this result.
     *************************************************************************/
    public String getType() {
	return type;
    }

    /*************************************************************************
     * Return the type of this result in more human-readable way.
     *************************************************************************/
    public String getDisplayType() {
	if (type == null)
	    return "unknown";
	if (type.startsWith ("String"))
	    return "text";
	if (type.startsWith ("byte"))
	    return "binary";
	return "unknown";
    }

    /*************************************************************************
     * Set a type of this result.
     *************************************************************************/
    public void setType (String type) {
	this.type = type;
    }

    /*************************************************************************
     * Return the size in bytes of this result.
     *************************************************************************/
    public String getSize() {
	return size;
    }

    /*************************************************************************
     * Set the size of this result.
     *************************************************************************/
    public void setSize (String size) {
	this.size = size;
    }

    /*************************************************************************
     * Return the number of elements of this result. Useful only if the
     * result's type is an array.
     *************************************************************************/
    public String getLength() {
	return length;
    }

    /*************************************************************************
     * Set the number of elements of this result (assuming it is of
     * type array).
     *************************************************************************/
    public void setLength (String length) {
	this.length = length;
    }

    /*************************************************************************
     * Return a URL of this result.
     *************************************************************************/
    public String getHref() {
	return href;
    }

    /*************************************************************************
     * Set a URL of this result.
     *************************************************************************/
    public void setHref (String href) {
	this.href = href;
    }

    /*************************************************************************
     * Return "true" if this result is an array; otherwise "false".
     *************************************************************************/
    public String getIsArray() {
	return (isArray ? "true" : "false");
    }

    /*************************************************************************
     *
     *************************************************************************/
    public void setIsArray (boolean value) {
	isArray = value;
    }

    /*************************************************************************
     * Return the suggested file extension for this result.
     *************************************************************************/
    public String getFileExtension() {
	return fileExtension;
    }

    /*************************************************************************
     * Set a suggested file extension for this result.
     *************************************************************************/
    public void setFileExtension (String fileExtension) {
	this.fileExtension = fileExtension;
    }

    /*************************************************************************
     * Return the content type (MIME type) suitable for this result.
     *************************************************************************/
    public String getContentType() {
	return contentType;
    }

    /*************************************************************************
     * Set a content type (MIME type) suitable for this result.
     *************************************************************************/
    public void setContentType (String contentType) {
	this.contentType = contentType;
    }

    /*************************************************************************
     *
     *************************************************************************/
    public String toString() {
	return ToStringBuilder.reflectionToString (this);
    }
}
