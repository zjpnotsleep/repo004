package com.leyou.auth.controller;

import com.leyou.auth.config.JwtProperties;
import com.leyou.auth.service.AuthService;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.user.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@EnableConfigurationProperties(JwtProperties.class)
public class AuthController {
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtProperties prop;
    @Value("${ly.jwt.cookieName}")
    private String cookieName;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam("username") String username, @RequestParam("password") String password,
                                      HttpServletRequest request, HttpServletResponse response){
        String token = authService.login(username,password);
        CookieUtils.setCookie(request,response,cookieName,token,-1);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/verify")
    public ResponseEntity<UserInfo> verify(@CookieValue(value = "LY_TOKEN",required = false) String token, HttpServletRequest request, HttpServletResponse response){
        try {
            //String token = CookieUtils.getCookieValue(request, cookieName);
            UserInfo userInfo = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            String newToken = JwtUtils.generateToken(userInfo, prop.getPrivateKey(), 30);
            CookieUtils.setCookie(request,response,cookieName,newToken,-1);
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            throw new LyException(ExceptionEnum.UNAUTHORIZED);
        }
    }


}
