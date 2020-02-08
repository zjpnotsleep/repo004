package com.itheima.ssm.service;

import com.itheima.ssm.domain.Orders;

import java.util.List;

public interface OrdersService {
    List<Orders> findAll(int page, int pageSize);

    Orders findById(String id);
}
