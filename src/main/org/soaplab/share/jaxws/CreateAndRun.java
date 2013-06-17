
package org.soaplab.share.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "createAndRun", namespace = "http://org.soaplab.2")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "createAndRun", namespace = "http://org.soaplab.2")
public class CreateAndRun {

    @XmlElement(name = "arg0", namespace = "")
    private org.soaplab.share.SoaplabMap arg0;

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

}
