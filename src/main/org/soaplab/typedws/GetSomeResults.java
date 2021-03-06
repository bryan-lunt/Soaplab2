//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-793 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.04.27 at 03:28:05 PM BST 
//


package org.soaplab.typedws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getSomeResultsRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getSomeResultsRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="jobId" type="{http://soaplab.org/typedws}jobId"/>
 *         &lt;element name="resultName" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getSomeResultsRequest", namespace = "http://soaplab.org/typedws", propOrder = {
    "jobId",
    "resultNames"
})
@XmlRootElement(name = "getSomeResults", namespace = "http://soaplab.org/typedws")
public class GetSomeResults {

    @XmlElement(required = true)
    protected JobId jobId;
    @XmlElement(name = "resultName", required = true)
    protected List<String> resultNames;

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

    /**
     * Gets the value of the resultNames property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resultNames property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResultNames().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getResultNames() {
        if (resultNames == null) {
            resultNames = new ArrayList<String>();
        }
        return this.resultNames;
    }

}
