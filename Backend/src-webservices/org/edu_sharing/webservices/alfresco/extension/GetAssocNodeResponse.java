/**
 * GetAssocNodeResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.edu_sharing.webservices.alfresco.extension;

public class GetAssocNodeResponse  implements java.io.Serializable {
    private java.util.HashMap getAssocNodeReturn;

    public GetAssocNodeResponse() {
    }

    public GetAssocNodeResponse(
           java.util.HashMap getAssocNodeReturn) {
           this.getAssocNodeReturn = getAssocNodeReturn;
    }


    /**
     * Gets the getAssocNodeReturn value for this GetAssocNodeResponse.
     * 
     * @return getAssocNodeReturn
     */
    public java.util.HashMap getGetAssocNodeReturn() {
        return getAssocNodeReturn;
    }


    /**
     * Sets the getAssocNodeReturn value for this GetAssocNodeResponse.
     * 
     * @param getAssocNodeReturn
     */
    public void setGetAssocNodeReturn(java.util.HashMap getAssocNodeReturn) {
        this.getAssocNodeReturn = getAssocNodeReturn;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof GetAssocNodeResponse)) return false;
        GetAssocNodeResponse other = (GetAssocNodeResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.getAssocNodeReturn==null && other.getGetAssocNodeReturn()==null) || 
             (this.getAssocNodeReturn!=null &&
              this.getAssocNodeReturn.equals(other.getGetAssocNodeReturn())));
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
        if (getGetAssocNodeReturn() != null) {
            _hashCode += getGetAssocNodeReturn().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(GetAssocNodeResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://extension.alfresco.webservices.edu_sharing.org", ">getAssocNodeResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("getAssocNodeReturn");
        elemField.setXmlName(new javax.xml.namespace.QName("http://extension.alfresco.webservices.edu_sharing.org", "getAssocNodeReturn"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://xml.apache.org/xml-soap", "Map"));
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
