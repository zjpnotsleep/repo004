package com.leyou.common.dto;

import com.sun.istack.internal.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    //@NotNull
    private Long skuId;
    //@NotNull
    private Integer num;
}
