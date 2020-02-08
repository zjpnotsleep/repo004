package com.itheima.sms.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/state/{id}")
    public String queryOrderStateById(@PathVariable("id") Long orderId){
        PayState payState = orderService.queryOrderStatusById(orderId);
        return "";
    }
}
