package com.lvmama.passport.processor.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;

import com.lvmama.comm.bee.po.ord.OrdOrder;
import com.lvmama.comm.bee.po.ord.OrdOrderItemMeta;
import com.lvmama.comm.bee.po.ord.OrdOrderItemProd;
import com.lvmama.comm.bee.po.ord.OrdPerson;
import com.lvmama.comm.bee.po.pass.PassCode;
import com.lvmama.comm.bee.service.ord.OrderService;
import com.lvmama.comm.bee.vo.Passport;
import com.lvmama.comm.spring.SpringBeanProxy;
import com.lvmama.comm.vo.Constant;
import com.lvmama.comm.vo.PassportConstant;
import com.lvmama.passport.processor.ApplyCodeProcessor;
import com.lvmama.passport.processor.DestroyCodeProcessor;
import com.lvmama.passport.processor.impl.client.zhiyoubao.ResponseUtil;
import com.lvmama.passport.processor.impl.client.zhiyoubao.ZhiyoubaoClient;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.FamilyOrder;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.Header;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.IdentityInfo;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.Order;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.OrderRequest;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.PWBRequest;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.PWBResponse;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.PayMethod;
import com.lvmama.passport.processor.impl.client.zhiyoubao.model.ScenicOrder;
import com.lvmama.passport.processor.impl.util.OrderUtil;
import com.lvmama.passport.utils.WebServiceConstant;

/**
 * 溪口对接--接口实现票务宝
 * @author gaoxin
 *
 */
public class XikouProcessorImpl implements ApplyCodeProcessor, DestroyCodeProcessor {
	private OrderService orderServiceProxy = (OrderService) SpringBeanProxy.getBean("orderServiceProxy");
	private static final Log LOG = LogFactory.getLog(XikouProcessorImpl.class);
	private static final String SUCCESS = "0";
	@Override
	public Passport apply(PassCode passCode) {
		LOG.info("Xikou Apply Code: " + passCode.getSerialNo());
		long startTime = 0L;
		Passport passport = new Passport();
		passport.setEventType(PassportConstant.PASSCODE_TYPE.APPLAYCODE.name());
		passport.setSendSms(PassportConstant.PASSCODE_SMS_SENDER.LVMAMA.name());
		PWBRequest request = this.getPwbRequest(passCode);
		String extId = passCode.getExtId();
		if (extId == null || "".equals(extId)) {
			try {
				startTime=System.currentTimeMillis();
				String xmlResponse = ZhiyoubaoClient.request(request.toSendCodeRequestXml(), WebServiceConstant.getProperties("zhiyoubao.key"),WebServiceConstant.getProperties("zhiyoubao.url"));
				LOG.info("Xikou Apply serialNo :" +passCode.getSerialNo() +" UseTime:"+ (System.currentTimeMillis() - startTime)/1000);
				LOG.info("Xikou xmlResponse:" + xmlResponse);
				PWBResponse pwbResponse = ResponseUtil.getPWBResponse(xmlResponse);
				String code = pwbResponse.getCode();
				String description = pwbResponse.getDescription();
				if (code.equals(SUCCESS)) {
					passport.setExtId(pwbResponse.getOrderResponse().getOrder().getOrderCode());
					passport.setCode("BASE64");
					passport.setAddCode(pwbResponse.getOrderResponse().getOrder().getAssistCheckNo());
					sendImg(passport, request, passCode);
				} else {
					passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
					passport.setComLogContent("供应商返回异常："+description);
					this.reapplySet(passport, passCode.getReapplyCount());
					LOG.info("Xikou Apply Error message:" + description);
				}
			} catch (Exception e) {
				LOG.error("xikou Apply serialNo Error :" +passCode.getSerialNo() +" UseTime:"+ (System.currentTimeMillis() - startTime)/1000);
				passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
				passport.setComLogContent(e.getMessage());
				this.reapplySet(passport, passCode.getReapplyCount());
				LOG.error("Xikou Apply Error message:", e);
			}
		}else{
			sendImg(passport,request,passCode);
		}
		return passport;
	}
	/**
	 * 重新申请码处理
	 * 
	 * @param passport
	 * @param error
	 */
	public void reapplySet(Passport passport, long times) {
		OrderUtil.init().reapplySet(passport, times);
	}

	@Override
	public Passport destroy(PassCode passCode) {
		LOG.info("Xikou Destoy Code: " + passCode.getSerialNo());
		long startTime=0L;
		Passport passport = new Passport();
		passport.setEventType(PassportConstant.PASSCODE_TYPE.DESTROYCODE.name());
		passport.setSerialno(passCode.getSerialNo());
		try {
			PWBRequest request = getPwbRequest(passCode);
			startTime=System.currentTimeMillis();
			String responseStr = ZhiyoubaoClient.request(request.toSendCodeCancelRequestXml(), WebServiceConstant.getProperties("zhiyoubao.key"),WebServiceConstant.getProperties("zhiyoubao.url"));
			LOG.info("Xikou Destoy serialNo :" +passCode.getSerialNo() +" UseTime:"+ (System.currentTimeMillis() - startTime)/1000);
			PWBResponse response = ResponseUtil.getCodeCancelRes(responseStr);
			String description = response.getDescription();
			if (response.getCode().equals(SUCCESS)) {
				passport.setStatus(PassportConstant.PASSCODE_STATUS.SUCCESS.name());
				LOG.info("Xikou Destoy Success message:" + description);
			} else {
				passport.setErrorNO(PassportConstant.PASSCODE_ERROR.DESTROY.name());
				passport.setComLogContent("供应商返回异常："+description);
				passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
				LOG.info("Xikou Destoy Error message:" + description);
			}
		} catch (Exception e) {
			LOG.error("xikou destroy serialNo Error :" +passCode.getSerialNo() +" UseTime:"+ (System.currentTimeMillis() - startTime)/1000);
			passport.setStatus(PassportConstant.PASSCODE_STATUS.FAILED.name());
			passport.setErrorNO(PassportConstant.PASSCODE_ERROR.DESTROY.name());
			passport.setComLogContent(e.getMessage());
			LOG.error("Xikou Destoy Exception:", e);
		}
		return passport;
	}
	/**
	 * 申码图
	 * @param passport
	 * @param request
	 * @param passCode
	 */
	private void sendImg(Passport passport,PWBRequest request,PassCode passCode){
		try {
			byte[] imagebt = getImageCode(request);
			if (imagebt != null) {
				passport.setCodeImage(imagebt);
				passport.setStatus(PassportConstant.PASSCODE_STATUS.SUCCESS.name());
			} else {
				passport.setComLogContent("XikouProcessorImpl_sendImg_Reading image error");
				passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
				this.reapplySet(passport, passCode.getReapplyCount());
			}
		} catch (Exception e) {
			passport.setComLogContent(e.getMessage());
			passport.setErrorNO(PassportConstant.PASSCODE_ERROR.APPLY.name());
			this.reapplySet(passport, passCode.getReapplyCount());
		}
		
	}
	
	/**
	 * 申请发码
	 * 
	 * @param request
	 * @return
	 * @throws Exception 
	 */
	private byte[] getImageCode(PWBRequest request) throws Exception {
		byte[] imageByte = null;
		try {
			String xmlResponse = ZhiyoubaoClient.request(request.toSendCodeImgRequestXml(), WebServiceConstant.getProperties("zhiyoubao.key") ,WebServiceConstant.getProperties("zhiyoubao.url"));
			PWBResponse pwbResponse = ResponseUtil.getPWBResponse(xmlResponse);
			if (pwbResponse.getCode().equals(SUCCESS)) {
				String imgStr = pwbResponse.getImg();
				imageByte = new BASE64Decoder().decodeBuffer(imgStr);
			}
		} catch (Exception e) {
			LOG.error("Xikou getImage Error message:", e);
			throw e;
		}
		return imageByte;
	}

	/**
	 * 
	 * @param passCode
	 * @return
	 */
	private PWBRequest getPwbRequest(PassCode passCode) {
		PWBRequest request = new PWBRequest();
		String requestTime = DateFormatUtils.format(new Date(), "yyyy-MM-dd");
		Header h = new Header("SendCode", requestTime);
		IdentityInfo idinfo = new IdentityInfo(WebServiceConstant.getProperties("zhiyoubao.corpcode"), WebServiceConstant.getProperties("zhiyoubao.username"));
		request.setHeader(h);
		request.setIdentityInfo(idinfo);
		OrdOrder ordorder = orderServiceProxy.queryOrdOrderByOrderId(passCode.getOrderId());
		String useTime = DateFormatUtils.format(ordorder.getVisitTime(), "yyyy-MM-dd");
		OrdPerson ordperson = OrderUtil.init().getContract(ordorder);
		float price = 0.0f;
		OrdOrderItemMeta item = OrderUtil.init().getItemMeta(ordorder, passCode);
		for (OrdOrderItemProd itemProd : ordorder.getOrdOrderItemProds()) {
			if (itemProd.getOrderItemProdId().longValue()==item.getOrderItemId()) {
				price=itemProd.getPriceYuan();
				break;
			}
		}
		Long num = item.getProductQuantity()*item.getQuantity();
		float totalPrice = price * item.getQuantity();
		String payMethodstr = "";
		String paymentTarget = ordorder.getPaymentTarget();
		if (Constant.PAYMENT_TARGET.TOLVMAMA.name().equals(paymentTarget)) {
			payMethodstr = PayMethod.on_line.name();
		} else {
			payMethodstr = PayMethod.spot.name();
		}
		List<ScenicOrder> scenicList = new ArrayList<ScenicOrder>();
		List<FamilyOrder> familyList = new ArrayList<FamilyOrder>();
		String productTypeSupplier = item.getProductTypeSupplier();
		productTypeSupplier = productTypeSupplier ==null?"":productTypeSupplier.trim();
		if("0".equals(productTypeSupplier)||("".equals(productTypeSupplier))){//单票
			ScenicOrder scenicOrder = new ScenicOrder(String.valueOf(item.getOrderItemMetaId()), String.valueOf(price), String.valueOf(num), String.valueOf(totalPrice), useTime,
					item.getProductIdSupplier(), item.getProductName());
			scenicList.add(scenicOrder);
		}else if("1".equals(item.getProductTypeSupplier())){//套票
			FamilyOrder familyOrder = new FamilyOrder(String.valueOf(item.getOrderItemMetaId()), String.valueOf(price), String.valueOf(num), String.valueOf(totalPrice), useTime,
					item.getProductIdSupplier(), item.getProductName());
			familyList.add(familyOrder);
		}
		Order od = new Order(ordperson.getCertNo(), ordperson.getName(), ordperson.getMobile(), passCode.getSerialNo(), String.valueOf(totalPrice), ordorder.getTravelGroupCode(), payMethodstr,
				scenicList, null, null, familyList);
		OrderRequest orequest = new OrderRequest();
		orequest.setOrder(od);
		request.setOrderRequest(orequest);
		return request;
	}

}
