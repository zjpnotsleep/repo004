package com.itheima.ssm.controller;

import com.github.pagehelper.PageInfo;
import com.itheima.ssm.dao.OrdersDao;
import com.itheima.ssm.domain.Orders;
import com.itheima.ssm.service.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @RequestMapping("findAll.do")
    public ModelAndView findAll(@RequestParam(name = "page",required = true,defaultValue = "1") Integer page,
                          @RequestParam(name = "pageSize",required = true,defaultValue = "4") Integer pageSize){
        ModelAndView mv = new ModelAndView();
        List<Orders> ordersList = ordersService.findAll(page,pageSize);
        PageInfo<Orders> info = new PageInfo<>(ordersList);
        mv.addObject("pageInfo",info);
        mv.setViewName("orders-page-list");
        return mv;
    }

    @RequestMapping("findById.do")
    public String findById(@RequestParam(name = "id",required = true) String id, Model model){

        Orders orders = ordersService.findById(id);

        model.addAttribute("orders",orders);
        return "orders-show";
    }
}
