package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadData(Long spuId) {
        Map<String, Object> map = new HashMap<>();
        Spu spu = goodsClient.querySpuBySpuId(spuId);
        Brand brand = brandClient.queryBrandByBid(spu.getBrandId());
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //List<SpecParam> specParams = specificationClient.querySpecParam(spu.getCid3(), true);      incorrect idea
        List<SpecGroup> specGroups = specificationClient.queryGroupByCid(spu.getCid3());
        map.put("categories",categories);
        map.put("brand",brand);
        map.put("title",spu.getTitle());
        map.put("subTitle",spu.getSubTitle());
        map.put("detail",spu.getSpuDetail());
        map.put("skus",spu.getSkus());
        map.put("specs",specGroups);
        return map;
    }

    public void createHtml(Long spuId) {
        Context context = new Context();
        context.setVariables(loadData(spuId));
        File dest = new File("E:\\java-2018\\09 微服务电商【黑马乐优商城】·\\资料\\thymeleaf",spuId+".html");
        if(dest.exists()){
            dest.delete();
        }
        try (PrintWriter printWriter = new PrintWriter(dest,"UTF-8")) {
            templateEngine.process("item", context, printWriter);
        }catch (Exception e){
            log.error("[静态页服务] 生成静态页异常！", e);
        }
    }

    public void deleteHtml(Long spuId) {
        File dest = new File("E:\\java-2018\\09 微服务电商【黑马乐优商城】·\\资料\\thymeleaf",spuId+".html");
        if(dest.exists()){
            dest.delete();
        }
    }
}
