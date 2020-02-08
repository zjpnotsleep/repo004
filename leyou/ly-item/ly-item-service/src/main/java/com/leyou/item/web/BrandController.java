package com.leyou.item.web;

import com.github.pagehelper.PageHelper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("brand")
public class BrandController {
    //?key=&page=1&rows=5&sortBy=id&desc=false
    @Autowired
    private BrandService brandService;

    @RequestMapping("page")
    public ResponseEntity<PageResult<Brand>> getBrandsByPage(@RequestParam(value = "key", required = false) String key,
                                                             @RequestParam(value = "page", defaultValue = "1") Integer page,
                                                             @RequestParam(value = "rows", defaultValue = "5") Integer rows,
                                                             @RequestParam(value = "sortBy", required = false) String sortBy,
                                                             @RequestParam(value = "desc", defaultValue = "false") Boolean desc) {


        PageResult<Brand> result = brandService.getBrandsByPage(key, page, rows, sortBy, desc);

        return ResponseEntity.ok(result);
    }

    @RequestMapping
    public ResponseEntity<Void> saveBrand(Brand brand, @RequestParam("cids") List<Long> cids) {
        brandService.saveBrand(brand, cids);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据cid查询品牌
     *
     * @param cid
     * @return
     */
    //http://api.leyou.com/api/item/brand/cid/77
    @RequestMapping("/cid/{cid}")
    public ResponseEntity<List<Brand>> queryBrandByCid(@PathVariable("cid") Long cid) {
        List<Brand> list = brandService.queryBrandByCid(cid);

        return ResponseEntity.ok(list);
    }

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @RequestMapping("{id}")
    public ResponseEntity<Brand> queryBrandById(@PathVariable("id") Long id) {
        Brand brand = brandService.queryById(id);

        return ResponseEntity.ok(brand);
    }

    @RequestMapping("list")
    public ResponseEntity<List<Brand>> queryBrandByIds(@RequestParam("ids") List<Long> ids) {
        return ResponseEntity.ok(brandService.queryByIds(ids));
    }
}
