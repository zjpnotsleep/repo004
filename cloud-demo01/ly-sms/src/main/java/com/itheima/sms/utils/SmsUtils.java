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

@Slf4j
@EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils {
    /*@Autowired
    private SmsProperties prop;*/

    public static void main(String[] args) {
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI4Fs2zXDAndV4BrL13isS", "13uMLfRdDGINxgdy0ywqtCTtpc9PnS");
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", "cn-hangzhou");
        request.putQueryParameter("PhoneNumbers", "13213019329");
        request.putQueryParameter("SignName", "乐优商城");
        request.putQueryParameter("TemplateCode", "SMS_182541633");
        request.putQueryParameter("TemplateParam", "{\"code\":\"农安公安\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());

            //发送短信日志
            log.info("[短信服务] ，发送短信验证码，手机号：{}", "15138980911");
        } catch (ServerException e) {
            log.error("[短信服务] 发送短信异常，手机号码:{}","15138980911",e);
            e.printStackTrace();
        } catch (ClientException e) {
            log.error("[短信服务] 发送短信异常，手机号码:{}","15138980911",e);
            e.printStackTrace();
        }catch (Exception e){
            log.error("[短信服务] 发送短信异常，手机号码:{}","15138980911",e);
            e.printStackTrace();
            System.out.println("..akgpaj");
        }
    }


}
