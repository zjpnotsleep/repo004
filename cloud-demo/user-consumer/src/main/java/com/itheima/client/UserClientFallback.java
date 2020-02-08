package com.itheima.client;

import com.itheima.pojo.User;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient{
    @Override
    public User findUserById(Integer uid) {
        User user = new User();
        user.setUsername("未知用户");
        return user;
    }
}
