
package org.soaplab.share.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "createAndRunNotifiable", namespace = "http://org.soaplab.2")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createAndRunNotifiable", namespace = "http://org.soaplab.2", propOrder = {
    "arg0",
    "arg1"
})
public class CreateAndRunNotifiable {

    @XmlElement(name = "arg0", namespace = "")
    private org.soaplab.share.SoaplabMap arg0;
    @XmlElement(name = "arg1", namespace = "")
    private String arg1;

    /**
     * 
     * @return
     *     returns SoaplabMap
     */
    public org.soaplab.share.SoaplabMap getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(org.soaplab.share.SoaplabMap arg0) {
        this.arg0 = arg0;
    }

    /**
     * 
     * @return
     *     returns String
     */
    public String getArg1() {
        return this.arg1;
    }

    /**
     * 
     * @param arg1
     *     the value for the arg1 property
     */
    public void setArg1(String arg1) {
        this.arg1 = arg1;
    }

}
