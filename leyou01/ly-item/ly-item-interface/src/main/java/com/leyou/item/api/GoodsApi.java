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
    @GetMapping("/sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("id") Long spuId);

    @GetMapping("/spu/detail/{spuId}")
    SpuDetail querySpuDetailBySpuId(@PathVariable("spuId") Long spuId);

    @GetMapping("/spu/page")
    PageResult<Spu> findGoodsByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows
    );

    @RequestMapping("spu")
    Spu querySpuBySpuId(@RequestParam("id") Long spuId);

    @GetMapping("/sku/list/ids")
    List<Sku> querySkuBySkuIds(@RequestParam("ids") List<Long> ids);

    @DeleteMapping("/stock/decease")
    Void deceaseStock(@RequestBody List<CartDTO> carts);
}
