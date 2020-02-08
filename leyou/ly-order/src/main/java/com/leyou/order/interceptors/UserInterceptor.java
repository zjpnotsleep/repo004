package com.leyou.order.interceptors;

import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JwtUtils;
import com.leyou.order.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {
    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> tl = new ThreadLocal();
    public UserInterceptor(JwtProperties prop){
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String token = CookieUtils.getCookieValue(request, prop.getCookieName());
            UserInfo info = JwtUtils.getInfoFromToken(token, prop.getPublicKey());
            tl.set(info);
            return true;
        }catch (Exception e){
            log.error("[购物车服务] 解析用户身份失败",e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 最后用完数据, 一定要清空
        tl.remove();
    }

    public static UserInfo getUser(){
        return tl.get();
    }
}
