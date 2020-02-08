package com.leyou.item.web;

import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.item.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GoodsController {
    @Autowired
    private GoodsService goodsService;

    //http://api.leyou.com/api/item/spu/page?key=&saleable=true&page=1&rows=5
    @RequestMapping("/spu/page")
    public ResponseEntity<PageResult<Spu>> querySpu(@RequestParam(name = "key", required = false) String key,
                                                    @RequestParam(name = "saleable", required = false) Boolean saleable,
                                                    @RequestParam(name = "page", defaultValue = "1") Integer page,
                                                    @RequestParam(name = "rows", defaultValue = "5") Integer rows
    ) {

        List<Spu> list = goodsService.querySpu(key, saleable, page, rows);
        PageInfo<Spu> info = new PageInfo<>(list);
        //System.out.println(info.getTotal());
        //System.out.println(list.size());
        PageResult<Spu> spuPageResult = new PageResult<>(info.getTotal(), list);
        return ResponseEntity.ok(spuPageResult);
    }

    @PostMapping("goods")
    public ResponseEntity<Void> saveGoods(@RequestBody Spu spu) {
        goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("goods")
    public ResponseEntity<Void> updateGoods(@RequestBody Spu spu) {
        goodsService.updateGoods(spu);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @RequestMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> queryDetailById(@PathVariable("id") Long spuId) {
        SpuDetail spuDetail = goodsService.queryDetailById(spuId);
        return ResponseEntity.ok(spuDetail);
    }

    @RequestMapping("sku/list")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@RequestParam("id") Long spuId) {
        List<Sku> list = goodsService.querySkuBySpuId(spuId);
        return ResponseEntity.ok(list);
    }
    @GetMapping("sku/list/ids")
    public ResponseEntity<List<Sku>> querySkuBySkuIds(@RequestParam("ids") List<Long> ids) {
        List<Sku> list = goodsService.querySkuBySkuIds(ids);
        return ResponseEntity.ok(list);
    }

    @RequestMapping("spu")
    public ResponseEntity<Spu> querySpuBySpuId(@RequestParam("id") Long spuId) {
        Spu spu = goodsService.querySpuBySpuId(spuId);
        return ResponseEntity.ok(spu);
    }

    @PostMapping("stock/decrease")
    public ResponseEntity<Void> decreaseStock(@RequestBody List<CartDTO> carts) {
        goodsService.decreaseStock(carts);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
