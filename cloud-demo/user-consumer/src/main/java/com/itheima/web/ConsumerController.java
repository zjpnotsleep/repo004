package com.itheima.web;

import com.itheima.client.UserClient;
import com.itheima.pojo.User;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RestController
@RequestMapping("consumer")
public class ConsumerController {
//    @Autowired
//    private RestTemplate restTemplate;
    /*@Autowired
    private DiscoveryClient discoveryClient;*/
    /*@Autowired
    private RibbonLoadBalancerClient client;*/
    @Autowired
    private UserClient userClient;

    @RequestMapping("test/{id}")
    //@HystrixCommand(fallbackMethod = "queryByIdFallback")
    public User test(@PathVariable("id") Integer id){

        /*String url = "http://user-service/user/findById?id="+id;
        String user = restTemplate.getForObject(url, String.class);*/
        User user = userClient.findUserById(id);
        return user;
    }

    /*public String queryByIdFallback(Integer id){

        *//*String url = "http://user-service/user/findById?id="+id;
        User user = restTemplate.getForObject(url, User.class);*//*
        User user = userClient.findUserById(id);
        return "不好意思，服务器太拥挤了！";
    }*/

    /*@RequestMapping("test/{id}")
    public User test(@PathVariable("id") Integer id){
        *//*List<ServiceInstance> instances = discoveryClient.getInstances("user-service");
        ServiceInstance instance = instances.get(0);
        //ServiceInstance instance = client.choose("user-service");
        String url = "http://"+instance.getHost()+":"+instance.getPort()+"/user/findById?id="+id;
        System.out.println(url);*//*

        String url = "http://user-service/user/findById?id="+id;
        //String url = "http://localhost:8080/user/findById?id="+id;
        User user = restTemplate.getForObject(url, User.class);
        return user;
    }*/
}
