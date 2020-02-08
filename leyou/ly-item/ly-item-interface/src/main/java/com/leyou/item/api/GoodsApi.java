package com.leyou.item.api;

import com.leyou.common.dto.CartDTO;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GoodsApi {
    @RequestMapping("/spu/detail/{id}")
    SpuDetail queryDetailById(@PathVariable("id") Long spuId);

    @RequestMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);

    @RequestMapping("/spu/page")
    PageResult<Spu> querySpu(@RequestParam(name = "key", required = false) String key,
                             @RequestParam(name = "saleable", required = false) Boolean saleable,
                             @RequestParam(name = "page", defaultValue = "1") Integer page,
                             @RequestParam(name = "rows", defaultValue = "5") Integer rows
    );

    @RequestMapping("spu")
    Spu querySpuBySpuId(@RequestParam("id") Long spuId);

    @GetMapping("sku/list/ids")
    List<Sku> querySkuBySkuIds(@RequestParam("ids") List<Long> ids);

    /**
     * 减库存接口
     */
    @PostMapping("stock/decrease")
    void decreaseStock(@RequestBody List<CartDTO> carts);
}
