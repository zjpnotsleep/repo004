package com.itheima.web;

import com.itheima.client.UserClient;
import com.itheima.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/consumer")
public class UserController {
    @Autowired
    private UserClient userClient;

    @GetMapping("/test/{id}")
    public User test(@PathVariable("id") Integer uid){
        return userClient.findUserById(uid);
    }
}
