
package org.soaplab.typedws;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getResultsInfoResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getResultsInfoResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="resultsInfoList" type="{http://soaplab.org/typedws}resultInfo" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getResultsInfoResponse", namespace = "http://soaplab.org/typedws", propOrder = {
    "resultsInfoLists"
})
@XmlRootElement(name = "getResultsInfoResponse", namespace = "http://soaplab.org/typedws")
public class GetResultsInfoResponse {

    @XmlElement(name = "resultsInfoList")
    protected List<ResultInfo> resultsInfoLists;

    /**
     * Gets the value of the resultsInfoLists property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the resultsInfoLists property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getResultsInfoLists().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ResultInfo }
     * 
     * 
     */
    public List<ResultInfo> getResultsInfoLists() {
        if (resultsInfoLists == null) {
            resultsInfoLists = new ArrayList<ResultInfo>();
        }
        return this.resultsInfoLists;
    }

}
