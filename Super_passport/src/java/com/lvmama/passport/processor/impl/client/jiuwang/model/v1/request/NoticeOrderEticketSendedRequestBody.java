//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.04.25 at 03:35:22 ���� CST 
//


package com.lvmama.passport.processor.impl.client.jiuwang.model.v1.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 【OTA提供】【订单】用户发码通知（noticeOrderEticketSended）
 * 
 * <p>Java class for NoticeOrderEticketSendedRequestBody complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="NoticeOrderEticketSendedRequestBody">
 *   &lt;complexContent>
 *     &lt;extension base="{http://tour.ectrip.com/2014/QMRequestDataSchema}RequestBody">
 *       &lt;sequence>
 *         &lt;element name="orderInfo">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="partnerorderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="eticketNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                   &lt;element name="eticketSended" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NoticeOrderEticketSendedRequestBody", propOrder = {
    "orderInfo"
})
public class NoticeOrderEticketSendedRequestBody
    extends RequestBody
{

    @XmlElement(required = true)
    protected NoticeOrderEticketSendedRequestBody.OrderInfo orderInfo;

    /**
     * Gets the value of the orderInfo property.
     *
     * @return
     *     possible object is
     *     {@link NoticeOrderEticketSendedRequestBody.OrderInfo }
     *
     */
    public NoticeOrderEticketSendedRequestBody.OrderInfo getOrderInfo() {
        return orderInfo;
    }

    /**
     * Sets the value of the orderInfo property.
     *
     * @param value
     *     allowed object is
     *     {@link NoticeOrderEticketSendedRequestBody.OrderInfo }
     *
     */
    public void setOrderInfo(NoticeOrderEticketSendedRequestBody.OrderInfo value) {
        this.orderInfo = value;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="partnerorderId" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="eticketNo" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *         &lt;element name="eticketSended" type="{http://www.w3.org/2001/XMLSchema}string"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "partnerorderId",
        "eticketNo",
        "eticketSended"
    })
    public static class OrderInfo {

        @XmlElement(required = true)
        protected String partnerorderId;
        @XmlElement(required = true)
        protected String eticketNo;
        @XmlElement(required = true)
        protected String eticketSended;

        /**
         * Gets the value of the partnerorderId property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getPartnerorderId() {
            return partnerorderId;
        }

        /**
         * Sets the value of the partnerorderId property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setPartnerorderId(String value) {
            this.partnerorderId = value;
        }

        /**
         * Gets the value of the eticketNo property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEticketNo() {
            return eticketNo;
        }

        /**
         * Sets the value of the eticketNo property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEticketNo(String value) {
            this.eticketNo = value;
        }

        /**
         * Gets the value of the eticketSended property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getEticketSended() {
            return eticketSended;
        }

        /**
         * Sets the value of the eticketSended property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setEticketSended(String value) {
            this.eticketSended = value;
        }

    }

}
