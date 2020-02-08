package com.itheima.sms.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/wxpay")
public class NotifyController {
    @Autowired
    private OrderService orderService;

    @PostMapping(value = "/notify",produces = "application/xml")
    public Map<String, String> hello(@RequestBody Map<String,String> result){
        orderService.handleNotify(result);

        log.info("[支付回调] 接收微信支付回调,结果：{}",result);
        Map<String,String> map = new HashMap<>();
        map.put("return_code","SUCCESS");
        map.put("return_msg","OK");
        return map;
    }
}
