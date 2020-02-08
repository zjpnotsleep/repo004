package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface BrandApi  {
    @GetMapping("/brand/{bid}")
    Brand queryBrandByBid(@PathVariable(value = "bid") Long bid);

    @PostMapping("/brand/list")
    List<Brand> queryBrandByBids(@RequestParam("bids") List<Long> bids);
}
