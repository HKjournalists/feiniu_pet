//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.06.21 at 05:21:00 PM CST 
//


package com.lvmama.elong.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EnumIncrType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EnumIncrType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Inventory"/>
 *     &lt;enumeration value="Rate"/>
 *     &lt;enumeration value="Order"/>
 *     &lt;enumeration value="State"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "EnumIncrType")
@XmlEnum
public enum EnumIncrType {

    @XmlEnumValue("Inventory")
    Inventory("Inventory"),
    @XmlEnumValue("Rate")
    Rate("Rate"),
    @XmlEnumValue("Order")
    Order("Order"),
    @XmlEnumValue("State")
    State("State");
    private final String value;

    EnumIncrType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EnumIncrType fromValue(String v) {
        for (EnumIncrType c: EnumIncrType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
