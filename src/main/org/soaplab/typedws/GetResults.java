//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.13 at 04:53:00 PM BST 
//


package org.soaplab.typedws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getResultsRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getResultsRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="jobId" type="{http://soaplab.org/typedws}jobId"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getResultsRequest", namespace = "http://soaplab.org/typedws", propOrder = {
    "jobId"
})
@XmlRootElement(name = "getResults", namespace = "http://soaplab.org/typedws")
public class GetResults {

    @XmlElement(required = true)
    protected JobId jobId;

    /**
     * Gets the value of the jobId property.
     * 
     * @return
     *     possible object is
     *     {@link JobId }
     *     
     */
    public JobId getJobId() {
        return jobId;
    }

    /**
     * Sets the value of the jobId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JobId }
     *     
     */
    public void setJobId(JobId value) {
        this.jobId = value;
    }

}
