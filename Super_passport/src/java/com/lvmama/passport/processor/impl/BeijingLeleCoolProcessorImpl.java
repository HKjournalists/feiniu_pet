package com.lvmama.passport.processor.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lvmama.comm.bee.po.ord.OrdOrder;
import com.lvmama.comm.bee.po.ord.OrdOrderItemMeta;
import com.lvmama.comm.bee.po.pass.PassCode;
import com.lvmama.comm.bee.service.ord.OrderService;
import com.lvmama.comm.bee.vo.Passport;
import com.lvmama.comm.spring.SpringBeanProxy;
import com.lvmama.comm.utils.TemplateUtils;
import com.lvmama.comm.vo.PassportConstant;
import com.lvmama.passport.beijinglelecool.model.OrderRequest;
import com.lvmama.passport.processor.ApplyCodeProcessor;
import com.lvmama.passport.processor.DestroyCodeProcessor;
import com.lvmama.passport.processor.OrderPerformProcessor;
import com.lvmama.passport.processor.ResendCodeProcessor;
import com.lvmama.passport.processor.impl.util.OrderUtil;
import com.lvmama.passport.utils.HttpsUtil;
import com.lvmama.passport.utils.WebServiceConstant;

/**
 *服务商：北京乐乐酷
 *景区：常熟尚湖
 *交互方式：HTTP(get/post)
 * @author tangJing
 */
public class BeijingLeleCoolProcessorImpl implements ApplyCodeProcessor, DestroyCodeProcessor,ResendCodeProcessor, OrderPerformProcessor {
	private static final Log log = LogFactory.getLog(BeijingLeleCoolProcessorImpl.class);
	private OrderService orderServiceProxy = (OrderService) SpringBeanProxy.getBean("orderServiceProxy");
	private static String baseTemplateDir = "/com/lvmama/passport/beijinglelecool/template";
	/**
	 * 申请码
	 */
	@Override
	public Passport apply(PassCode passCode) {
		log.info("BeijingLeleCoolapply serialNo: " + passCode.getSerialNo());
		long startTime=0L;
		Passport passport = new Passport();
		passport.setSerialno(passCode.getSerialNo());
		passport.setEventType(PassportConstant.PASSCODE_TYPE.APPLAYCODE.name());
		passport.setSendSms(PassportConstant.PASSCODE_SMS_SENDER.PARTNER.name());
		try {
			OrderRequest orderBean=this.buildOrder(passCode);
			String reqXml = TemplateUtils.fillFileTemplate(baseTemplateDir, "submitOrderReq.xml", orderBean);
			String url=WebServiceConstant.getProperties("beijinglelecool.url");
			String custId=WebServiceConstant.getProperties("beijinglelecool.custId");
			String apikey=WebServiceConstant.getProperties("beijinglelecool.apikey");
			Map<String, String> requestParas = new HashMap<String, String>();
			requestParas.put("custId", custId);
			requestParas.put("apikey", apikey);
			requestParas.put("param",reqXml);
			String result = HttpsUtil.requestPostForm(url+"/api/order.jsp", requestParas);
			startTime = System.currentTimeMillis();
			log.info("BeijingLeleCoolapplay serialNo :"+ passCode.getSerialNo() + " UseTime:"+ (System.currentTimeMillis() - startTime) / 1000);
			if (result.startsWith(HttpsUtil.HTTP_ERROR_PREFIX)) {
				passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
				passport.setComLogContent("供应商返回异常："+ result.substring(HttpsUtil.HTTP_ERROR_PREFIX.length()));
				this.reapplySet(passport, passCode.getReapplyCount());
			} else {
				log.info("BeijingLeleCoolapplay saveOrder resXml: " + result);
				String status = TemplateUtils.getElementValue(result,"//result/status");
				if (StringUtils.equals(status, "1")) {
					 //保存订单成功后调用支付接口
					String order_id = TemplateUtils.getElementValue(result,"//result/order_id");
					Map<String, String> reqParas = buildParams(order_id);
					String paymentResult = HttpsUtil.requestPostForm(url+"/api/pay.jsp", reqParas);
					log.info("BeijingLeleCoolapplay pay resXml:"+paymentResult);
					String payStatus = TemplateUtils.getElementValue(paymentResult,"//result/status");
					if(StringUtils.equals(payStatus,"1")){
						passport.setCode(order_id);
						passport.setStatus(PassportConstant.PASSCODE_STATUS.SUCCESS.name());
					}else{
						String msg=TemplateUtils.getElementValue(paymentResult,"//result/msg");
						passport.setComLogContent("供应商返回异常："+payStatus+ "|"+ msg);
						passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
						this.reapplySet(passport, passCode.getReapplyCount());
						log.info("BeijingLeleCoolapply fail message: "+ msg);
						this.reapplySet(passport, passCode.getReapplyCount());
					}	
				
				} else {
					String msg=TemplateUtils.getElementValue(result,"//result/msg");
					passport.setComLogContent("供应商返回异常："+status+ "|" +msg);
					passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
					this.reapplySet(passport, passCode.getReapplyCount());
					log.info("BeijingLeleCoolapply fail message: "+ msg);
					this.reapplySet(passport, passCode.getReapplyCount());
				}
			}
		} catch (Exception e) {
			log.error("BeijingLeleCoolapplay serialNo Error :"+ passCode.getSerialNo() + " UseTime:"+ (System.currentTimeMillis() - startTime) / 1000);
			passport.setComLogContent(e.getMessage());
			passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
			this.reapplySet(passport, passCode.getReapplyCount());
			log.error("BeijingLeleCoolapplay message", e);
		}
		return passport;
	}
	
	/**
	 * 重新申码
	 */
	public void reapplySet(Passport passport, long times) {
		OrderUtil.init().reapplySet(passport, times);
	}
	
	private static  Map<String, String> buildParams(String orderId){
		String custId=WebServiceConstant.getProperties("beijinglelecool.custId");
		String apikey=WebServiceConstant.getProperties("beijinglelecool.apikey");
		Map<String, String> reqParas = new HashMap<String, String>();
		reqParas.put("custId", custId);
		reqParas.put("apikey", apikey);
		reqParas.put("orderId", orderId);
		return reqParas;
	}
	
	/**
	 * 废码
	 */
	@Override
	public Passport destroy(PassCode passCode) {
		long startTime=0L;
		log.info("BeijingLeleCooldestroy serialNo: " + passCode.getSerialNo());
		Passport passport = new Passport();
		passport.setSerialno(passCode.getSerialNo());
		passport.setEventType(PassportConstant.PASSCODE_TYPE.DESTROYCODE.name());
		try {
			String url=WebServiceConstant.getProperties("beijinglelecool.url");
			Map<String, String> reqParas = buildParams(passCode.getCode());
			String result = HttpsUtil.requestPostForm(url+"/api/chancelOrder.jsp", reqParas);
			log.info("BeijingLeleCooldestroy serialNo :" +passCode.getSerialNo() +" UseTime:"+ (System.currentTimeMillis() - startTime)/1000);
			if(result.startsWith(HttpsUtil.HTTP_ERROR_PREFIX)){
				passport.setErrorNO(PassportConstant.PASSCODE_ERROR.DESTROY.name());
				passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
				passport.setComLogContent("供应商返回异常："+result.substring(HttpsUtil.HTTP_ERROR_PREFIX.length()));
			}else{
				log.info("BeijingLeleCooldestroy resXml: " + result);
				String status = TemplateUtils.getElementValue(result, "//result/status");			
				if (StringUtils.equals(status,"1")) {
					passport.setStatus(PassportConstant.PASSCODE_STATUS.SUCCESS.name());
				} else {
					String msg = TemplateUtils.getElementValue(result, "//result/msg");
					passport.setComLogContent("供应商返回异常："+msg);
					passport.setErrorNO(PassportConstant.PASSCODE_ERROR.DESTROY.name());
					passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
					log.info("BeijingLeleCooldestroy fail message: " +msg);
				}
			}
		} catch (Exception e) {
			log.error("BeijingLeleCooldestroy serialNo Error :" +passCode.getSerialNo() +" UseTime:"+ (System.currentTimeMillis() - startTime)/1000);
			passport.setComLogContent(e.getMessage());
			passport.setErrorNO(PassportConstant.PASSCODE_ERROR.DESTROY.name());
			passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
			log.error("BeijingLeleCooldestroy error message", e);
		}
		return passport;
	}
	/**
	 * 重发短信
	 * @param passCode
	 * @return
	 */
	@Override
	public Passport resend(PassCode passCode) {
		log.info("BeijingLeleCool resend serialNo: " + passCode.getSerialNo());
		Passport passport = new Passport();
		passport.setEventType(PassportConstant.PASSCODE_TYPE.RESEND.name());
		passport.setSerialno(passCode.getSerialNo());
		try {
			String url=WebServiceConstant.getProperties("beijinglelecool.url");
			Map<String, String> resendRequestParas=buildParams(passCode.getCode());
			resendRequestParas.put("mobile",passCode.getMobile());
			String resXml = HttpsUtil.requestPostForm(url+"/api/resend.jsp", resendRequestParas);
			log.info("BeijingLeleCool resend  resXml: " + resXml);
			if (resXml.startsWith(HttpsUtil.HTTP_ERROR_PREFIX)) {
				passport.setErrorNO(PassportConstant.PASSCODE_ERROR.RESEND.name());
				passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
				passport.setComLogContent("供应商返回异常：" + resXml.substring(HttpsUtil.HTTP_ERROR_PREFIX.length()));
			} else {
				String status = TemplateUtils.getElementValue(resXml, "//result/status");
				if (StringUtils.equals(status, "1")) {
						passport.setStatus(PassportConstant.PASSCODE_STATUS.SUCCESS.name());
					}else{
					String msg = TemplateUtils.getElementValue(resXml, "//result/msg");
					passport.setComLogContent("供应商返回异常：" + msg);
					passport.setErrorNO(PassportConstant.PASSCODE_ERROR.RESEND.name());
					passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
					log.info("BeijingLeleCool resend fail message: "+msg);
				}
			}
		} catch (Exception e) {
			passport.setComLogContent(e.getMessage());
			passport.setErrorNO(PassportConstant.PASSCODE_ERROR.RESEND.name());
			passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
			log.error("BeijingLeleCool resend error message", e);
		}
		return passport;
	}
	
	private static Set<PassCode> unusedList = new HashSet<PassCode>();
	private static Set<PassCode> usedList = new HashSet<PassCode>();
	/**
	 * 更新订单履行状态
	 */
	@Override
	public Passport perform(PassCode passCode) {
		log.info("BeijingLeleCoolperform serialNo: " + passCode.getSerialNo());
		Passport passport = null;
		if (isNeedCheckout(passCode)) {
			try {
				String url=WebServiceConstant.getProperties("beijinglelecool.url");
				Map<String, String> reqParas = buildParams(passCode.getCode());
				String result = HttpsUtil.requestPostForm(url+"/api/orderDetail.jsp", reqParas);
				if(result.startsWith(HttpsUtil.HTTP_ERROR_PREFIX)){
					log.info(result.substring(HttpsUtil.HTTP_ERROR_PREFIX.length()));
				}else{
					log.info("BeijingLeleCoolperform resXml: " + result);
					String status = TemplateUtils.getElementValue(result,"//result/orders/order/orderState");
						if (StringUtils.equals(status,"4")) {//0 新订单 1已确认 2 已成功 3 已取消 4 已完成
							passport = new Passport();
							passport.setChild("0");
							passport.setAdult("0");
							passport.setUsedDate(new Date());
							passport.setDeviceId("BeijingLeleCool");
							stopCheckout(passCode);
						}
				}
			} catch(Exception e) {
				log.error("BeijingLeleCoolperform error message", e);
			}
		}
		return passport;
	}
	
	private boolean isPassCodeUnused(PassCode passCode) {
		if (!usedList.contains(passCode)) {
			unusedList.add(passCode);
			return true;
		}
		return false;
	}
	
	private boolean isNeedCheckout(PassCode passCode) {
		return "SUCCESS".equals(passCode.getStatus()) && isPassCodeUnused(passCode);
	}
	
	private void stopCheckout(PassCode passCode) {
		unusedList.remove(passCode);
		usedList.add(passCode);
	}
	
	private OrderRequest buildOrder(PassCode passCode)throws Exception{
			OrdOrder ordOrder = orderServiceProxy.queryOrdOrderByOrderId(passCode.getOrderId());
			OrdOrderItemMeta itemMeta = OrderUtil.init().getItemMeta(ordOrder, passCode);
			Long count = itemMeta.getProductQuantity() * itemMeta.getQuantity();
			String productIdSupplier = itemMeta.getProductIdSupplier();
			String visiteTime = DateFormatUtils.format(ordOrder.getVisitTime(),"yyyy-MM-dd");
			if (StringUtils.isBlank(productIdSupplier)) {
				throw new IllegalArgumentException("代理产品编号不能空");
			}
			String custId=WebServiceConstant.getProperties("beijinglelecool.custId");
			OrderRequest order=new OrderRequest();
			order.setTravelDate(visiteTime);
			order.setInfoId(productIdSupplier);
			order.setLinkMan(ordOrder.getContact().getName());
			order.setLinkPhone(ordOrder.getContact().getMobile());
			order.setNum(String.valueOf(count));
			order.setOrderSourceId(passCode.getSerialNo());
			order.setCustId(custId);
		return order;
	}
	public static void main(String[] args) {
		try{
//		String url=WebServiceConstant.getProperties("beijinglelecool.url");
//		String custId=WebServiceConstant.getProperties("beijinglelecool.custId");
//		String apikey=WebServiceConstant.getProperties("beijinglelecool.apikey");
//		OrderRequest orderBean=new OrderRequest();
//		orderBean.setTravelDate("2014-03-25");
//		orderBean.setInfoId("7135894");
//		orderBean.setLinkMan("小汤测试单");
//		orderBean.setLinkPhone("15026847838");
//		orderBean.setNum(String.valueOf(1));
//		orderBean.setOrderSourceId("20140318141202");
//		orderBean.setCustId(custId);
//		String reqXml = TemplateUtils.fillFileTemplate(baseTemplateDir, "submitOrderReq.xml", orderBean);
//		Map<String, String> requestParas = new HashMap<String, String>();
//		requestParas.put("custId", custId);
//		requestParas.put("apikey", apikey);
//		requestParas.put("param",reqXml);
//		String result = HttpsUtil.requestPostForm(url+"/api/order.jsp", requestParas);
//		System.out.println("=====下单：====="+result);
//		String status = TemplateUtils.getElementValue(result,"//result/status");
//		if(StringUtils.equals(status,"1")){
//		//保存订单成功后调用支付接口
//		String order_id = TemplateUtils.getElementValue(result,"//result/order_id");
//		Map<String, String> reqParas = buildParams(order_id);
//		String paymentResult = HttpsUtil.requestPostForm(url+"/api/pay.jsp", reqParas);
//		System.out.println(paymentResult);
//		}
//		Map<String, String> reqParas = buildParams("1382716");
//		String result = HttpsUtil.requestPostForm(url+"/api/chancelOrder.jsp", reqParas);
//		System.out.println(result);
//		Map<String, String> reqParas1 = buildParams("1382716");
//		String result = HttpsUtil.requestPostForm(url+"/api/orderDetail.jsp", reqParas1);
//		System.out.println(result);
//		Map<String, String> resendRequestParas=buildParams("1392056");
//		resendRequestParas.put("mobile","15026847838");
//		String resXml = HttpsUtil.requestPostForm(url+"/api/resend.jsp", resendRequestParas);
//		System.out.println(resXml);
//		String status = TemplateUtils.getElementValue(resXml.trim(), "//result/status");
//		System.out.println(status);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	
}