package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.leyou.item.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    @GetMapping("page")
    public ResponseEntity<PageResult<Brand>> findBrandsByPage(
            @RequestParam(value = "key",required = false) String key,
            @RequestParam(value = "page",defaultValue = "1") Integer page,
            @RequestParam(value = "rows",defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy",required = false) String sortBy,
            @RequestParam(value = "desc",defaultValue = "false") Boolean desc
    ){
        PageResult<Brand> result = brandService.findBrandsByPage(key,page,rows,sortBy,desc);
        if(result == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<Void> saveBrand(@RequestParam(value = "cids") List<Long> cids,Brand brand){
        brandService.saveBrand(brand,cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryBrandCategoriesByBid(@PathVariable(value = "bid") Long bid){
        return ResponseEntity.ok(brandService.queryBrandCategoriesByBid(bid));
    }

    @GetMapping("cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable(value = "cid") Long cid){
        return ResponseEntity.ok(brandService.queryBrandByCid(cid));
    }

    @GetMapping("{bid}")
    public ResponseEntity<Brand> queryBrandByBid(@PathVariable(value = "bid") Long bid){
        return ResponseEntity.ok(brandService.queryBrandByBid(bid));
    }

    @PostMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByBids(@RequestParam("bids") List<Long> bids){
        return ResponseEntity.ok(brandService.queryBrandByBids(bids));
    }

}
