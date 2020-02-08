package com.leyou.order.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDTO;
import com.leyou.order.enums.OrderStatusEnum;
import com.leyou.order.enums.PayState;
import com.leyou.order.interceptors.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private PayHelper payHelper;

    @Transactional
    public long createOrder(OrderDTO orderDTO) {
        long orderId = idWorker.nextId();
        UserInfo userInfo = UserInterceptor.getUser();

        Order order = new Order();
        order.setOrderId(orderId);
        order.setUserId(userInfo.getId());
        order.setCreateTime(new Date());
        order.setPaymentType(orderDTO.getPaymentType());
        List<CartDTO> carts = orderDTO.getCarts();
        Map<Long, Integer> map = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));

        List<Sku> skuList = goodsClient.querySkuBySkuIds(new ArrayList<>(map.keySet()));
        long totalPay = 0;
        for (Sku sku : skuList) {
           totalPay += sku.getPrice()*map.get(sku.getId());

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetail.setNum(map.get(sku.getId()));
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetailMapper.insertSelective(orderDetail);
        }

        order.setTotalPay(totalPay);
        order.setActualPay(totalPay + order.getPostFee() - 0);
        //order.setBuyerMessage();
        order.setBuyerNick(userInfo.getUsername());
        order.setBuyerRate(false);

        AddressDTO addressDTO = AddressClient.findById(orderDTO.getAddressId());
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverState(addressDTO.getState());
        order.setReceiverZip(addressDTO.getZipCode());

        int count = orderMapper.insertSelective(order);
        if(count != 1){
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }

        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.UNPAY.getCode());
        orderStatusMapper.insertSelective(orderStatus);

        goodsClient.decreaseStock(carts);

        return orderId;
    }

    public Order queryOrderById(Long id) {
        Order order = orderMapper.selectByPrimaryKey(id);
        if(order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOND);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(id);
        List<OrderDetail> details = orderDetailMapper.select(orderDetail);
        if(CollectionUtils.isEmpty(details)) {
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOND);
        }
        order.setOrderDetails(details);
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(id);
        if(orderStatus == null) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOND);
        }
        order.setOrderStatus(orderStatus);
        return order;
    }

    public String createPayUrl(Long orderId) {
        Order order = queryOrderById(orderId);
        //Long actualPay = order.getActualPay();
        Integer status = order.getOrderStatus().getStatus();
        if(status != OrderStatusEnum.UNPAY.getCode()){
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        Long actualPay = 1L;
        OrderDetail orderDetail = order.getOrderDetails().get(0);
        String payUrl = payHelper.createPayUrl(orderId, actualPay, orderDetail.getTitle());
        return payUrl;
    }

    public PayState queryOrderState(Long orderId) {
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        Integer status = orderStatus.getStatus();
        if(status != OrderStatusEnum.UNPAY.getCode()){
            return PayState.SUCCESS;
        }

        return payHelper.queryPayState(orderId);
    }

    public void handlerNotify(Map<String, String> result) {
        //数据校验
        //判断通信和业务标识
        payHelper.isSuccess(result);
        //校验签名
        payHelper.isValidSign(result);
        //校验金额
        String totalFeeStr = result.get("total_fee");
        String tradeNo = result.get("out_trade_no");
        if(StringUtils.isEmpty(totalFeeStr)){
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        Long totalFee = Long.valueOf(totalFeeStr);
        Long orderId = Long.valueOf(tradeNo);
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if(totalFee != /*order.getActualPay()*/ 1){
            //金额不符
            throw new LyException(ExceptionEnum.INVALID_ORDER_PARAM);
        }
        //修改订单状态
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.PAYED.getCode());
        orderStatus.setCreateTime(new Date());
        int count = orderStatusMapper.updateByPrimaryKeySelective(orderStatus);
        if(count != 1){
            throw new LyException(ExceptionEnum.UPDATE_ORDER_STATUS_ERROR);
        }
        log.info("[订单回调] 订单支付成功!订单编号：{}",orderId);

    }
}
