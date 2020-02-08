package com.itheima.sms.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderService {
    @Autowired
    private PayHelper payHelper;

    public void handleNotify(Map<String, String> result) {

        payHelper.isSuccess(result);
        payHelper.isValidSign(result);
    }

    public PayState queryOrderStatusById(Long orderId) {
        /*OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if(status != OrderStatusEnum.UNPAY.getCode()){
            return PayState.SUCCESS;
        }*/

        return payHelper.queryPayState(orderId);
    }
}
