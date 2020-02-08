package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ExceptionEnum {
    CATEGORY_NOT_FOND(404, "商品分类没查到"),
    PRICE_CANNOT_BE_NULL(400, "价格不能为空!"),
    BRAND_NOT_FOND(404, "品牌找不到"),
    UPLOAD_FILE_FAIL(500, "上传文件失败"),
    INVALID_FILE_TYPE(400, "无效的文件类型"),
    SPEC_GROUD_NOT_FOND(404, "商品规格组不存在"),
    GOODS_NOT_FOND(404, "商品不存在"),
    SPEC_PARAM_NOT_FOND(404, "商品规格参数不存在"),
    BRAND_SAVE_ERROR(500, "新增品牌失败"),
    GOODS_SAVE_ERROR(500, "新增商品失败"),
    GOODS_UPDATE_ERROR(500, "更新商品失败"),
    GOODS_ID_CANNOT_BE_NULL(400, "商品id不能为空"),
    GOODS_STOCK_NOT_FOND(404, "商品库存不存在"),
    INVALID_USER_DATA_TYPE(400, "用户数据类型无效"),
    INVALID_VERIFY_CODE(400, "无效的验证码"),
    INVALID_USERNAME_PASSWORD(400, "无效的用户名密码"),
    CREATE_TOKEN_ERROR(500, "创建用户凭证失败"),
    UNAUTHORIZED(403, "未授权"),
    CART_NOT_FOND(404, "购物车为空"),
    GOODS_SKU_NOT_FOND(404, "sku不存在"),
    CREATE_ORDER_ERROR(500, "创建订单失败"),
    STOCK_NOT_ENOUGH(500, "库存不足"),
    ORDER_NOT_FOND(404, "订单不存在"),
    ORDER_DETAIL_NOT_FOND(404, "订单详情不存在"),
    ORDER_STATUS_NOT_FOND(404, "订单状态不存在"),
    ORDER_STATUS_ERROR(400, "订单状态不正确"),
    INVALID_SIGN_ERROR(400, "无效的签名异常"),
    INVALID_ORDER_PARAM(400, "订单参数异常"),
    WX_PAY_ORDER_FAIL(500, "微信下单失败"),
    UPDATE_ORDER_STATUS_ERROR(500, "更新订单失败"),
    ;
    private int code;
    private String msg;

}
