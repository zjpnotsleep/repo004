package com.leyou.cart.service;

import com.leyou.cart.config.JwtProperties;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.CookieUtils;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@EnableConfigurationProperties(JwtProperties.class)
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private JwtProperties prop;

    private static final String PREFIX_CART = "cart:uid:";
    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo info = UserInterceptor.getUser();

        String key = PREFIX_CART + info.getId();
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //原来的数量
        Integer num = cart.getNum();
        //判断当前商品购物车商品是否存在
        if(operations.hasKey(hashKey)){
            //是，修改数量
            cart = JsonUtils.parse(operations.get(hashKey).toString(), Cart.class);
            cart.setNum(num+cart.getNum());
        }
        //写回redis
        operations.put(hashKey,JsonUtils.serialize(cart));

    }

    public List<Cart> queryCartList() {
        //获取登录用户
        UserInfo info = UserInterceptor.getUser();
        String key = PREFIX_CART + info.getId();
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        List<Object> values = operations.values();
        List<Cart> carts = JsonUtils.parseList(values.toString(), Cart.class);

        return carts;
    }

    public void updateCartGoodsNum(Long skuId, Integer num) {
        //获取登录用户
        UserInfo info = UserInterceptor.getUser();

        String key = PREFIX_CART + info.getId();
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        Cart cart = JsonUtils.parse(operations.get(skuId.toString()).toString(), Cart.class);
        cart.setNum(num);
        operations.put(skuId.toString(),JsonUtils.serialize(cart));

    }

    public void deleteCartGoods(Long skuId) {
        //获取登录用户
        UserInfo info = UserInterceptor.getUser();

        String key = PREFIX_CART + info.getId();
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        operations.delete(skuId.toString());
    }
}
