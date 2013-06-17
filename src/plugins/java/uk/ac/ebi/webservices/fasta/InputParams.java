/**
 * InputParams.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.3 Oct 05, 2005 (05:23:37 EDT) WSDL2Java emitter.
 */

package uk.ac.ebi.webservices.fasta;

public class InputParams  implements java.io.Serializable {
    private java.lang.String program;

    private java.lang.String database;

    private java.lang.String moltype;

    private java.lang.Boolean histogram;

    private java.lang.Boolean nucleotide;

    private java.lang.Boolean topstrand;

    private java.lang.Boolean bottomstrand;

    private java.lang.Integer gapopen;

    private java.lang.Integer gapext;

    private java.lang.Integer scores;

    private java.lang.Integer alignments;

    private java.lang.Integer ktup;

    private java.lang.String matrix;

    private java.lang.Float eupper;

    private java.lang.Float elower;

    private java.lang.String dbrange;

    private java.lang.String seqrange;

    private java.lang.String outformat;

    private java.lang.Boolean async;

    private java.lang.String email;

    public InputParams() {
    }

    public InputParams(
           java.lang.String program,
           java.lang.String database,
           java.lang.String moltype,
           java.lang.Boolean histogram,
           java.lang.Boolean nucleotide,
           java.lang.Boolean topstrand,
           java.lang.Boolean bottomstrand,
           java.lang.Integer gapopen,
           java.lang.Integer gapext,
           java.lang.Integer scores,
           java.lang.Integer alignments,
           java.lang.Integer ktup,
           java.lang.String matrix,
           java.lang.Float eupper,
           java.lang.Float elower,
           java.lang.String dbrange,
           java.lang.String seqrange,
           java.lang.String outformat,
           java.lang.Boolean async,
           java.lang.String email) {
           this.program = program;
           this.database = database;
           this.moltype = moltype;
           this.histogram = histogram;
           this.nucleotide = nucleotide;
           this.topstrand = topstrand;
           this.bottomstrand = bottomstrand;
           this.gapopen = gapopen;
           this.gapext = gapext;
           this.scores = scores;
           this.alignments = alignments;
           this.ktup = ktup;
           this.matrix = matrix;
           this.eupper = eupper;
           this.elower = elower;
           this.dbrange = dbrange;
           this.seqrange = seqrange;
           this.outformat = outformat;
           this.async = async;
           this.email = email;
    }


    /**
     * Gets the program value for this InputParams.
     * 
     * @return program
     */
    public java.lang.String getProgram() {
        return program;
    }


    /**
     * Sets the program value for this InputParams.
     * 
     * @param program
     */
    public void setProgram(java.lang.String program) {
        this.program = program;
    }


    /**
     * Gets the database value for this InputParams.
     * 
     * @return database
     */
    public java.lang.String getDatabase() {
        return database;
    }


    /**
     * Sets the database value for this InputParams.
     * 
     * @param database
     */
    public void setDatabase(java.lang.String database) {
        this.database = database;
    }


    /**
     * Gets the moltype value for this InputParams.
     * 
     * @return moltype
     */
    public java.lang.String getMoltype() {
        return moltype;
    }


    /**
     * Sets the moltype value for this InputParams.
     * 
     * @param moltype
     */
    public void setMoltype(java.lang.String moltype) {
        this.moltype = moltype;
    }


    /**
     * Gets the histogram value for this InputParams.
     * 
     * @return histogram
     */
    public java.lang.Boolean getHistogram() {
        return histogram;
    }


    /**
     * Sets the histogram value for this InputParams.
     * 
     * @param histogram
     */
    public void setHistogram(java.lang.Boolean histogram) {
        this.histogram = histogram;
    }


    /**
     * Gets the nucleotide value for this InputParams.
     * 
     * @return nucleotide
     */
    public java.lang.Boolean getNucleotide() {
        return nucleotide;
    }


    /**
     * Sets the nucleotide value for this InputParams.
     * 
     * @param nucleotide
     */
    public void setNucleotide(java.lang.Boolean nucleotide) {
        this.nucleotide = nucleotide;
    }


    /**
     * Gets the topstrand value for this InputParams.
     * 
     * @return topstrand
     */
    public java.lang.Boolean getTopstrand() {
        return topstrand;
    }


    /**
     * Sets the topstrand value for this InputParams.
     * 
     * @param topstrand
     */
    public void setTopstrand(java.lang.Boolean topstrand) {
        this.topstrand = topstrand;
    }


    /**
     * Gets the bottomstrand value for this InputParams.
     * 
     * @return bottomstrand
     */
    public java.lang.Boolean getBottomstrand() {
        return bottomstrand;
    }


    /**
     * Sets the bottomstrand value for this InputParams.
     * 
     * @param bottomstrand
     */
    public void setBottomstrand(java.lang.Boolean bottomstrand) {
        this.bottomstrand = bottomstrand;
    }


    /**
     * Gets the gapopen value for this InputParams.
     * 
     * @return gapopen
     */
    public java.lang.Integer getGapopen() {
        return gapopen;
    }


    /**
     * Sets the gapopen value for this InputParams.
     * 
     * @param gapopen
     */
    public void setGapopen(java.lang.Integer gapopen) {
        this.gapopen = gapopen;
    }


    /**
     * Gets the gapext value for this InputParams.
     * 
     * @return gapext
     */
    public java.lang.Integer getGapext() {
        return gapext;
    }


    /**
     * Sets the gapext value for this InputParams.
     * 
     * @param gapext
     */
    public void setGapext(java.lang.Integer gapext) {
        this.gapext = gapext;
    }


    /**
     * Gets the scores value for this InputParams.
     * 
     * @return scores
     */
    public java.lang.Integer getScores() {
        return scores;
    }


    /**
     * Sets the scores value for this InputParams.
     * 
     * @param scores
     */
    public void setScores(java.lang.Integer scores) {
        this.scores = scores;
    }


    /**
     * Gets the alignments value for this InputParams.
     * 
     * @return alignments
     */
    public java.lang.Integer getAlignments() {
        return alignments;
    }


    /**
     * Sets the alignments value for this InputParams.
     * 
     * @param alignments
     */
    public void setAlignments(java.lang.Integer alignments) {
        this.alignments = alignments;
    }


    /**
     * Gets the ktup value for this InputParams.
     * 
     * @return ktup
     */
    public java.lang.Integer getKtup() {
        return ktup;
    }


    /**
     * Sets the ktup value for this InputParams.
     * 
     * @param ktup
     */
    public void setKtup(java.lang.Integer ktup) {
        this.ktup = ktup;
    }


    /**
     * Gets the matrix value for this InputParams.
     * 
     * @return matrix
     */
    public java.lang.String getMatrix() {
        return matrix;
    }


    /**
     * Sets the matrix value for this InputParams.
     * 
     * @param matrix
     */
    public void setMatrix(java.lang.String matrix) {
        this.matrix = matrix;
    }


    /**
     * Gets the eupper value for this InputParams.
     * 
     * @return eupper
     */
    public java.lang.Float getEupper() {
        return eupper;
    }


    /**
     * Sets the eupper value for this InputParams.
     * 
     * @param eupper
     */
    public void setEupper(java.lang.Float eupper) {
        this.eupper = eupper;
    }


    /**
     * Gets the elower value for this InputParams.
     * 
     * @return elower
     */
    public java.lang.Float getElower() {
        return elower;
    }


    /**
     * Sets the elower value for this InputParams.
     * 
     * @param elower
     */
    public void setElower(java.lang.Float elower) {
        this.elower = elower;
    }


    /**
     * Gets the dbrange value for this InputParams.
     * 
     * @return dbrange
     */
    public java.lang.String getDbrange() {
        return dbrange;
    }


    /**
     * Sets the dbrange value for this InputParams.
     * 
     * @param dbrange
     */
    public void setDbrange(java.lang.String dbrange) {
        this.dbrange = dbrange;
    }


    /**
     * Gets the seqrange value for this InputParams.
     * 
     * @return seqrange
     */
    public java.lang.String getSeqrange() {
        return seqrange;
    }


    /**
     * Sets the seqrange value for this InputParams.
     * 
     * @param seqrange
     */
    public void setSeqrange(java.lang.String seqrange) {
        this.seqrange = seqrange;
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
            ((this.program==null && other.getProgram()==null) || 
             (this.program!=null &&
              this.program.equals(other.getProgram()))) &&
            ((this.database==null && other.getDatabase()==null) || 
             (this.database!=null &&
              this.database.equals(other.getDatabase()))) &&
            ((this.moltype==null && other.getMoltype()==null) || 
             (this.moltype!=null &&
              this.moltype.equals(other.getMoltype()))) &&
            ((this.histogram==null && other.getHistogram()==null) || 
             (this.histogram!=null &&
              this.histogram.equals(other.getHistogram()))) &&
            ((this.nucleotide==null && other.getNucleotide()==null) || 
             (this.nucleotide!=null &&
              this.nucleotide.equals(other.getNucleotide()))) &&
            ((this.topstrand==null && other.getTopstrand()==null) || 
             (this.topstrand!=null &&
              this.topstrand.equals(other.getTopstrand()))) &&
            ((this.bottomstrand==null && other.getBottomstrand()==null) || 
             (this.bottomstrand!=null &&
              this.bottomstrand.equals(other.getBottomstrand()))) &&
            ((this.gapopen==null && other.getGapopen()==null) || 
             (this.gapopen!=null &&
              this.gapopen.equals(other.getGapopen()))) &&
            ((this.gapext==null && other.getGapext()==null) || 
             (this.gapext!=null &&
              this.gapext.equals(other.getGapext()))) &&
            ((this.scores==null && other.getScores()==null) || 
             (this.scores!=null &&
              this.scores.equals(other.getScores()))) &&
            ((this.alignments==null && other.getAlignments()==null) || 
             (this.alignments!=null &&
              this.alignments.equals(other.getAlignments()))) &&
            ((this.ktup==null && other.getKtup()==null) || 
             (this.ktup!=null &&
              this.ktup.equals(other.getKtup()))) &&
            ((this.matrix==null && other.getMatrix()==null) || 
             (this.matrix!=null &&
              this.matrix.equals(other.getMatrix()))) &&
            ((this.eupper==null && other.getEupper()==null) || 
             (this.eupper!=null &&
              this.eupper.equals(other.getEupper()))) &&
            ((this.elower==null && other.getElower()==null) || 
             (this.elower!=null &&
              this.elower.equals(other.getElower()))) &&
            ((this.dbrange==null && other.getDbrange()==null) || 
             (this.dbrange!=null &&
              this.dbrange.equals(other.getDbrange()))) &&
            ((this.seqrange==null && other.getSeqrange()==null) || 
             (this.seqrange!=null &&
              this.seqrange.equals(other.getSeqrange()))) &&
            ((this.outformat==null && other.getOutformat()==null) || 
             (this.outformat!=null &&
              this.outformat.equals(other.getOutformat()))) &&
            ((this.async==null && other.getAsync()==null) || 
             (this.async!=null &&
              this.async.equals(other.getAsync()))) &&
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
        if (getProgram() != null) {
            _hashCode += getProgram().hashCode();
        }
        if (getDatabase() != null) {
            _hashCode += getDatabase().hashCode();
        }
        if (getMoltype() != null) {
            _hashCode += getMoltype().hashCode();
        }
        if (getHistogram() != null) {
            _hashCode += getHistogram().hashCode();
        }
        if (getNucleotide() != null) {
            _hashCode += getNucleotide().hashCode();
        }
        if (getTopstrand() != null) {
            _hashCode += getTopstrand().hashCode();
        }
        if (getBottomstrand() != null) {
            _hashCode += getBottomstrand().hashCode();
        }
        if (getGapopen() != null) {
            _hashCode += getGapopen().hashCode();
        }
        if (getGapext() != null) {
            _hashCode += getGapext().hashCode();
        }
        if (getScores() != null) {
            _hashCode += getScores().hashCode();
        }
        if (getAlignments() != null) {
            _hashCode += getAlignments().hashCode();
        }
        if (getKtup() != null) {
            _hashCode += getKtup().hashCode();
        }
        if (getMatrix() != null) {
            _hashCode += getMatrix().hashCode();
        }
        if (getEupper() != null) {
            _hashCode += getEupper().hashCode();
        }
        if (getElower() != null) {
            _hashCode += getElower().hashCode();
        }
        if (getDbrange() != null) {
            _hashCode += getDbrange().hashCode();
        }
        if (getSeqrange() != null) {
            _hashCode += getSeqrange().hashCode();
        }
        if (getOutformat() != null) {
            _hashCode += getOutformat().hashCode();
        }
        if (getAsync() != null) {
            _hashCode += getAsync().hashCode();
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
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://www.ebi.ac.uk/WSFasta", "inputParams"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("program");
        elemField.setXmlName(new javax.xml.namespace.QName("", "program"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("database");
        elemField.setXmlName(new javax.xml.namespace.QName("", "database"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("moltype");
        elemField.setXmlName(new javax.xml.namespace.QName("", "moltype"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("histogram");
        elemField.setXmlName(new javax.xml.namespace.QName("", "histogram"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("nucleotide");
        elemField.setXmlName(new javax.xml.namespace.QName("", "nucleotide"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("topstrand");
        elemField.setXmlName(new javax.xml.namespace.QName("", "topstrand"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("bottomstrand");
        elemField.setXmlName(new javax.xml.namespace.QName("", "bottomstrand"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("gapopen");
        elemField.setXmlName(new javax.xml.namespace.QName("", "gapopen"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("gapext");
        elemField.setXmlName(new javax.xml.namespace.QName("", "gapext"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("scores");
        elemField.setXmlName(new javax.xml.namespace.QName("", "scores"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("alignments");
        elemField.setXmlName(new javax.xml.namespace.QName("", "alignments"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ktup");
        elemField.setXmlName(new javax.xml.namespace.QName("", "ktup"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("matrix");
        elemField.setXmlName(new javax.xml.namespace.QName("", "matrix"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("eupper");
        elemField.setXmlName(new javax.xml.namespace.QName("", "eupper"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("elower");
        elemField.setXmlName(new javax.xml.namespace.QName("", "elower"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "float"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("dbrange");
        elemField.setXmlName(new javax.xml.namespace.QName("", "dbrange"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("seqrange");
        elemField.setXmlName(new javax.xml.namespace.QName("", "seqrange"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("outformat");
        elemField.setXmlName(new javax.xml.namespace.QName("", "outformat"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("async");
        elemField.setXmlName(new javax.xml.namespace.QName("", "async"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "boolean"));
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
