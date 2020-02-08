package com.itheima.service;

import com.itheima.mapper.UserMapper;
import com.itheima.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public List<User> findAll() {
        List<User> users = userMapper.selectAll();

        return users;
    }

    public User findUserById(Integer uid) {
        User user = new User();
        user.setId(uid);
        return userMapper.selectByPrimaryKey(uid);
    }
}
