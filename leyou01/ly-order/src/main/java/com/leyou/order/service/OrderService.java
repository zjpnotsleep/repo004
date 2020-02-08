package com.leyou.order.service;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.IdWorker;
import com.leyou.item.pojo.Sku;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.config.IdWorkerProperties;
import com.leyou.order.config.JwtProperties;
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
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderService {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private OrderStatusMapper orderStatusMapper;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private PayHelper payHelper;

    private static final String PREFIX_CART = "cart:uid:";

    @Transactional
    public Long createOrder(OrderDTO orderDTO) {
        UserInfo userInfo = UserInterceptor.getUser();
        String key = PREFIX_CART + userInfo.getId();
        long orderId = idWorker.nextId();
        List<CartDTO> carts = orderDTO.getCarts();
        Map<Long, Integer> map = carts.stream().collect(Collectors.toMap(CartDTO::getSkuId, CartDTO::getNum));
        //List<Long> skuIdList = carts.stream().map(CartDTO::getSkuId).collect(Collectors.toList());
        //List<Sku> skuList = goodsClient.querySkuBySkuIds(skuIdList);
        List<Sku> skuList = goodsClient.querySkuBySkuIds(new ArrayList<>(map.keySet()));
        long totalPay = 0;
        for (Sku sku : skuList) {
            totalPay += sku.getPrice()*map.get(sku.getId());

            OrderDetail orderDetail = new OrderDetail();
            //orderDetail.setId();
            orderDetail.setOrderId(orderId);
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setPrice(sku.getPrice());
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setNum(map.get(sku.getId()));
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(),","));
            orderDetailMapper.insertSelective(orderDetail);

        }
        Long addressId = orderDTO.getAddressId();
        AddressDTO addressDTO = AddressClient.findById(addressId);

        Integer paymentType = orderDTO.getPaymentType();
        Order order = new Order();
        //order.setOrderDetails();
        //order.setOrderStatus();
        order.setUserId(userInfo.getId());
        order.setTotalPay(totalPay);
        //order.setSourceType();
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setPaymentType(paymentType);
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setBuyerRate(false);
        order.setBuyerNick(addressDTO.getName());
        //order.setBuyerMessage();
        order.setActualPay(totalPay+order.getPostFee() - 0);
        //order.setInvoiceType();
        order.setPostFee(0L);
        //order.setPromotionIds(null);
        //order.setShippingCode();
        //order.setShippingName();

        int count = orderMapper.insertSelective(order);
        if(count != 1){
            throw new LyException(ExceptionEnum.CREATE_ORDER_ERROR);
        }


        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setCreateTime(order.getCreateTime());
        orderStatus.setStatus(OrderStatusEnum.UNPAY.getCode());
        orderStatus.setOrderId(orderId);
        //orderStatus.setCloseTime();
        //orderStatus.setCommentTime();
        //orderStatus.setConsignTime();
        //orderStatus.setEndTime();
        //orderStatus.setPaymentTime();
        orderStatusMapper.insertSelective(orderStatus);

        goodsClient.deceaseStock(carts);
        return orderId;
    }

    public Order queryOrder(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        if(orderStatus == null){
            throw new LyException(ExceptionEnum.ORDER_STATUS_NOT_FOND);
        }
        order.setOrderStatus(orderStatus);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        if(CollectionUtils.isEmpty(orderDetails)){
            throw new LyException(ExceptionEnum.ORDER_DETAIL_NOT_FOND);
        }
        order.setOrderDetails(orderDetails);
        if(order == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOND);
        }
        return order;
    }

    public String createPayUrl(Long orderId) {
        Order order = queryOrder(orderId);
        if(order == null){
            throw new LyException(ExceptionEnum.ORDER_NOT_FOND);
        }
        Integer status = order.getOrderStatus().getStatus();
        if(status != OrderStatusEnum.UNPAY.getCode()){
            throw new LyException(ExceptionEnum.ORDER_STATUS_ERROR);
        }
        Long actualPay = 1L;//order.getActualPay();
        String title = order.getOrderDetails().get(0).getTitle();
        String payUrl = payHelper.createPayUrl(orderId, actualPay, title);
        return payUrl;
    }

    public PayState queryOrderStatusById(Long orderId) {
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
