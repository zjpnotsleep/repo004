package com.leyou.cart.service;

import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.UserInfo;
import com.leyou.common.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class CartService {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PREFIX_CART = "cart:uid:";

    public void addCart(Cart cart) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = PREFIX_CART + user.getId();
        String hashKey = cart.getSkuId().toString();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        //原来的数量
        Integer num = cart.getNum();
        //判断当前商品购物车商品是否存在
        if(operations.hasKey(hashKey)){
            //是，修改数量
            cart = JsonUtils.parse(operations.get(hashKey).toString(), Cart.class);
            //cart = (Cart) operations.get(hashKey);
            cart.setNum(num + cart.getNum());
        }
        //写回redis
        operations.put(hashKey,JsonUtils.toString(cart));

    }

    public List<Cart> queryCart() {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = PREFIX_CART + user.getId();
        List<Cart> carts = JsonUtils.parseList(redisTemplate.boundHashOps(key).values().toString(), Cart.class);
        if(CollectionUtils.isEmpty(carts)){
            throw new LyException(ExceptionEnum.CART_NOT_FOND);
        }
        return carts;
    }

    public void updateCartGoodsNum(String skuId, Integer num) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = PREFIX_CART + user.getId();
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        Cart cart = JsonUtils.parse(operations.get(skuId).toString(), Cart.class);
        cart.setNum(num);
        //写回redis
        operations.put(skuId,JsonUtils.toString(cart));
    }

    public void deleteCartGoodsBySkuId(String skuId) {
        //获取登录用户
        UserInfo user = UserInterceptor.getUser();
        String key = PREFIX_CART + user.getId();
        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOND);
        }
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        operations.delete(skuId);
    }
}
