package com.leyou.sms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("ly.sms")
public class SmsProperties {
    private String accessKeyId; //JWffwFJIwada # 你自己的accessKeyId
    private String accessKeySecret; //aySRliswq8fe7rF9gQyy1Izz4MQ # 你自己的AccessKeySecret
    private String signName; //乐优商城 # 签名名称
    private String verifyCodeTemplate; //SMS_133976814 # 模板名称
}
