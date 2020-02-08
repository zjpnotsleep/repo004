package com.itheima.client;

import com.itheima.pojo.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "user-service")
public interface UserClient {
    @GetMapping("user/findById")
    User findUserById(@RequestParam("id") Integer uid);
}
