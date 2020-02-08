package com.itheima.sms.utils;

import com.github.wxpay.sdk.WXPay;
import static com.github.wxpay.sdk.WXPayConstants.*;

import com.github.wxpay.sdk.WXPayUtil;
import com.itheima.sms.config.PayConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class PayHelper {
    @Autowired
    private PayConfig payConfig;
    @Autowired
    private WXPay wxPay;
    /*private String appID; // 公众账号 ID

    private String mchID; // 商户号

    private String key; // 生成签名的密钥

    private int httpConnectTimeoutMs; // 连接超时时间

    private int httpReadTimeoutMs;// 读取超时时间

    private String tradeType; // 交易类型
    private String spbillCreateIp;// 本地 ip
    private String notifyUrl;// 回调地址
    */

    public String createPayUrl(String body,long totalFee,String orderId){
        /*MyConfig config = new MyConfig();
        WXPay wxpay = new WXPay(config);*/

        Map<String, String> data = new HashMap<String, String>();
        data.put("body", body);
        data.put("out_trade_no", orderId);
        //data.put("device_info", "");
        //data.put("fee_type", "CNY");
        data.put("total_fee", totalFee+"");
        data.put("spbill_create_ip", payConfig.getSpbillCreateIp());
        data.put("notify_url", payConfig.getNotifyUrl());
        data.put("trade_type", payConfig.getTradeType());  // 此处指定为扫码支付
        //data.put("product_id", "12");

        try {
            Map<String, String> resp = wxPay.unifiedOrder(data);
            System.out.println(resp);
            //校验状态
            isSuccess(resp);
            //校验签名
            isValidSign(resp);
            String code_url = resp.get("code_url");
            return code_url;
        } catch (Exception e) {
            log.error("【微信下单】下单失败，订单号:{}", orderId, e);
            e.printStackTrace();
        }
        return null;
    }

    public void isSuccess(Map<String, String> resp) {
        if(FAIL.equals(resp.get("return_code"))){
            log.error("【微信下单】下单通信失败, 原因：{}", resp.get("return_msg"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
        if(FAIL.equals(resp.get("result_code"))){
            log.error("【微信下单】下单失败, 错误码：{}， 错误原因：{}", resp.get("err_code"),
                    resp.get("err_code_des"));
            throw new LyException(ExceptionEnum.WX_PAY_ORDER_FAIL);
        }
    }

    public void isValidSign(Map<String, String> resp){
        try {
            String sign1 = WXPayUtil.generateSignature(resp, payConfig.getKey(), SignType.MD5);
            String sign2 = WXPayUtil.generateSignature(resp, payConfig.getKey(), SignType.HMACSHA256);
            String sign = resp.get("sign");
            if(!StringUtils.equals(sign1,sign) && !StringUtils.equals(sign2,sign)){
                throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
            }

        } catch (Exception e) {
            log.error("【微信下单】签名验证出错 ", e);
            throw new LyException(ExceptionEnum.INVALID_SIGN_ERROR);
        }


    }

    public PayState queryPayState(Long orderId){
        Map<String, String> data = new HashMap<>();
        data.put("out_trade_no",orderId.toString());
        try {
            Map<String, String> result = wxPay.orderQuery(data);
            //校验状态
            isSuccess(result);
            //校验签名
            isValidSign(result);
            //校验金额
            /*String totalFeeStr = result.get("total_fee");
            String tradeNoStr = result.get("out_trade_no");
            if (StringUtils.isBlank(tradeNoStr) || StringUtils.isBlank(totalFeeStr)) {
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            Long totalFee = Long.valueOf(totalFeeStr);
            Order order = orderMapper.selectByPrimaryKey(orderId);
            if (order == null) {
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }
            // FIXME 这里应该是不等于实际金额
            if (totalFee != *//*totalFee*//* 1L) {
                // 金额不符
                throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
            }*/
            //校验
            String tradeState = result.get("trade_state");
            if(SUCCESS.equals(tradeState)){
                // 修改订单状态
                /*OrderStatus status = new OrderStatus();
                status.setStatus(OrderStatusEnum.PAYED.getCode());
                status.setOrderId(orderId);
                status.setPaymentTime(new Date());
                int count = statusMapper.updateByPrimaryKeySelective(status);
                if (count != 1) {
                    throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
                }*/
                return PayState.SUCCESS;
            }
            if("NOTPAY".equals(tradeState) || "USERPAYING".equals(tradeState)){
                return PayState.NOT_PAY;
            }
            return PayState.FAIL;
        } catch (Exception e) {
            log.error("[微信支付], 调用微信接口查询支付状态失败", e);
            return PayState.NOT_PAY;
        }

    }
}
