package com.itheima.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.itheima.sms.config.SmsProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils01 {
    @Autowired
    private SmsProperties prop;

    public void sendSms(String phoneNumbers,String signName,String templateCode,String templateParam){
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", prop.getAccessKeyId(), prop.getAccessKeySecret());
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", phoneNumbers);
        request.putQueryParameter("SignName", signName);
        request.putQueryParameter("TemplateCode", templateCode);
        //模板参数为json结构"{"code":"136656"}"
        request.putQueryParameter("TemplateParam", templateParam);
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
            Map<String, Object> map = JsonUtils.toMap(response.getData(), String.class, Object.class);
            String msg = (String) map.get("Message");
            String code = (String) map.get("Code");
            if(!"OK".equals(code)){
                log.info("[短信服务] 发送短信失败，phoneNumber:{},原因：{}",phoneNumbers,msg);
            }
            //发送短信日志
            log.info("[短信服务] ，发送短信验证码，手机号：{}", phoneNumbers);
        } catch (ServerException e) {
            log.error("[短信服务] 发送短信异常，手机号码:{}",phoneNumbers,e);
            e.printStackTrace();
        } catch (ClientException e) {
            log.error("[短信服务] 发送短信异常，手机号码:{}",phoneNumbers,e);
            e.printStackTrace();
        }
    }
}
