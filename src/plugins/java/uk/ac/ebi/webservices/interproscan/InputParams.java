/**
 * InputParams.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.interproscan;

public class InputParams  implements java.io.Serializable {
    private java.lang.String app;

    private java.lang.Boolean crc;

    private java.lang.String seqtype;

    private java.lang.Integer trlen;

    private java.lang.Integer trtable;

    private java.lang.Boolean goterms;

    private java.lang.Boolean async;

    private java.lang.String outformat;

    private java.lang.String email;

    public InputParams() {
    }

    public InputParams(
           java.lang.String app,
           java.lang.Boolean crc,
           java.lang.String seqtype,
           java.lang.Integer trlen,
           java.lang.Integer trtable,
           java.lang.Boolean goterms,
           java.lang.Boolean async,
           java.lang.String outformat,
           java.lang.String email) {
           this.app = app;
           this.crc = crc;
           this.seqtype = seqtype;
           this.trlen = trlen;
           this.trtable = trtable;
           this.goterms = goterms;
           this.async = async;
           this.outformat = outformat;
           this.email = email;
    }


    /**
     * Gets the app value for this InputParams.
     * 
     * @return app
     */
    public java.lang.String getApp() {
        return app;
    }


    /**
     * Sets the app value for this InputParams.
     * 
     * @param app
     */
    public void setApp(java.lang.String app) {
        this.app = app;
    }


    /**
     * Gets the crc value for this InputParams.
     * 
     * @return crc
     */
    public java.lang.Boolean getCrc() {
        return crc;
    }


    /**
     * Sets the crc value for this InputParams.
     * 
     * @param crc
     */
    public void setCrc(java.lang.Boolean crc) {
        this.crc = crc;
    }


    /**
     * Gets the seqtype value for this InputParams.
     * 
     * @return seqtype
     */
    public java.lang.String getSeqtype() {
        return seqtype;
    }


    /**
     * Sets the seqtype value for this InputParams.
     * 
     * @param seqtype
     */
    public void setSeqtype(java.lang.String seqtype) {
        this.seqtype = seqtype;
    }


    /**
     * Gets the trlen value for this InputParams.
     * 
     * @return trlen
     */
    public java.lang.Integer getTrlen() {
        return trlen;
    }


    /**
     * Sets the trlen value for this InputParams.
     * 
     * @param trlen
     */
    public void setTrlen(java.lang.Integer trlen) {
        this.trlen = trlen;
    }


    /**
     * Gets the trtable value for this InputParams.
     * 
     * @return trtable
     */
    public java.lang.Integer getTrtable() {
        return trtable;
    }


    /**
     * Sets the trtable value for this InputParams.
     * 
     * @param trtable
     */
    public void setTrtable(java.lang.Integer trtable) {
        this.trtable = trtable;
    }


    /**
     * Gets the goterms value for this InputParams.
     * 
     * @return goterms
     */
    public java.lang.Boolean getGoterms() {
        return goterms;
    }


    /**
     * Sets the goterms value for this InputParams.
     * 
     * @param goterms
     */
    public void setGoterms(java.lang.Boolean goterms) {
        this.goterms = goterms;
    }


    /**
     * Gets the async value for this InputParams.
     * 
     * @return async
     */
    public java.lang.Boolean getAsync() {
        return async;
    }


    /**
     * Sets the async value for this InputParams.
     * 
     * @param async
     */
    public void setAsync(java.lang.Boolean async) {
        this.async = async;
    }


    /**
     * Gets the outformat value for this InputParams.
     * 
     * @return outformat
     */
    public java.lang.String getOutformat() {
        return outformat;
    }


    /**
     * Sets the outformat value for this InputParams.
     * 
     * @param outformat
     */
    public void setOutformat(java.lang.String outformat) {
        this.outformat = outformat;
    }


    /**
     * Gets the email value for this InputParams.
     * 
     * @return email
     */
    public java.lang.String getEmail() {
        return email;
    }


    /**
     * Sets the email value for this InputParams.
     * 
     * @param email
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof InputParams)) return false;
        InputParams other = (InputParams) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.app==null && other.getApp()==null) || 
             (this.app!=null &&
              this.app.equals(other.getApp()))) &&
            ((this.crc==null && other.getCrc()==null) || 
             (this.crc!=null &&
              this.crc.equals(other.getCrc()))) &&
            ((this.seqtype==null && other.getSeqtype()==null) || 
             (this.seqtype!=null &&
              this.seqtype.equals(other.getSeqtype()))) &&
            ((this.trlen==null && other.getTrlen()==null) || 
             (this.trlen!=null &&
              this.trlen.equals(other.getTrlen()))) &&
            ((this.trtable==null && other.getTrtable()==null) || 
             (this.trtable!=null &&
              this.trtable.equals(other.getTrtable()))) &&
            ((this.goterms==null && other.getGoterms()==null) || 
             (this.goterms!=null &&
              this.goterms.equals(other.getGoterms()))) &&
            ((this.async==null && other.getAsync()==null) || 
             (this.async!=null &&
              this.async.equals(other.getAsync()))) &&
            ((this.outformat==null && other.getOutformat()==null) || 
             (this.outformat!=null &&
              this.outformat.equals(other.getOutformat()))) &&
            ((this.email==null && other.getEmail()==null) || 
             (this.email!=null &&
              this.email.equals(other.getEmail())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getApp() != null) {
            _hashCode += getApp().hashCode();
        }
        if (getCrc() != null) {
            _hashCode += getCrc().hashCode();
        }
        if (getSeqtype() != null) {
            _hashCode += getSeqtype().hashCode();
        }
        if (getTrlen() != null) {
            _hashCode += getTrlen().hashCode();
        }
        if (getTrtable() != null) {
            _hashCode += getTrtable().hashCode();
        }
        if (getGoterms() != null) {
            _hashCode += getGoterms().hashCode();
        }
        if (getAsync() != null) {
            _hashCode += getAsync().hashCode();
        }
        if (getOutformat() != null) {
            _hashCode += getOutformat().hashCode();
        }
        if (getEmail() != null) {
            _hashCode += getEmail().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(InputParams.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSInterProScan", "inputParams"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("app");
        elemField.setXmlName(new javax.xml.namespace.QName("", "app"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("crc");
        elemField.setXmlName(new javax.xml.namespace.QName("", "crc"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("seqtype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "seqtype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("trlen");
        elemField.setXmlName(new javax.xml.namespace.QName("", "trlen"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("trtable");
        elemField.setXmlName(new javax.xml.namespace.QName("", "trtable"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("goterms");
        elemField.setXmlName(new javax.xml.namespace.QName("", "goterms"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("async");
        elemField.setXmlName(new javax.xml.namespace.QName("", "async"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outformat");
        elemField.setXmlName(new javax.xml.namespace.QName("", "outformat"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("email");
        elemField.setXmlName(new javax.xml.namespace.QName("", "email"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
