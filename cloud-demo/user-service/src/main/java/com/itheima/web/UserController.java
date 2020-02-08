package com.itheima.web;

import com.itheima.pojo.User;
import com.itheima.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("findById")
    public User findUserById(@RequestParam("id") Integer uid){
        /*try {
            Thread.sleep(2000L);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        User user = userService.findUserById(uid);
        return user;
    }
}
