
package org.soaplab.share.jaxws;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "getInputSpecResponse", namespace = "http://org.soaplab.2")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getInputSpecResponse", namespace = "http://org.soaplab.2")
public class GetInputSpecResponse {

    @XmlElement(name = "return", namespace = "", nillable = true)
    private org.soaplab.share.SoaplabMap[] _return;

    /**
     * 
     * @return
     *     returns SoaplabMap[]
     */
    public org.soaplab.share.SoaplabMap[] getReturn() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void setReturn(org.soaplab.share.SoaplabMap[] _return) {
        this._return = _return;
    }

}
