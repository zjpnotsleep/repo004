package com.leyou.page.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PageService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadData(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        Spu spu = goodsClient.querySpuBySpuId(spuId);
        model.put("title", spu.getTitle());
        model.put("subTitle", spu.getSubTitle());
        model.put("skus", spu.getSkus());
        model.put("detail", spu.getSpuDetail());
        model.put("brand", brandClient.queryBrandById(spu.getBrandId()));
        model.put("categories", categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3())));
        /*List<SpecGroup> specGroups = specificationClient.queryGroupByCid(spu.getCid3());
        if(CollectionUtils.isEmpty(specGroups)){
            throw new LyException(ExceptionEnum.SPEC_GROUD_NOT_FOND);
        }
        for (SpecGroup specGroup : specGroups) {
            specGroup.setSpecs(specificationClient.querySpecParam(specGroup.getId(),null,false));
        }*/
        List<SpecGroup> specs = specificationClient.queryGroupByCid(spu.getCid3());
        model.put("specs", specs);



        /*Spu spu = goodsClient.querySpuBySpuId(spuId);
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        spu.setCname(StringUtils.join(categories.stream().map(c -> c.getName()).collect(Collectors.toList()), "/"));

        spu.setBname(brandClient.queryBrandById(spu.getBrandId()).getName());
        
        spu.setSpuDetail(goodsClient.queryDetailById(spuId));
        List<SpecGroup> specGroups = specificationClient.queryGroupByCid(spu.getCid3());
        if(CollectionUtils.isEmpty(specGroups)){
            throw new LyException(ExceptionEnum.SPEC_GROUD_NOT_FOND);
        }
        for (SpecGroup specGroup : specGroups) {
            specGroup.setSpecs(specificationClient.querySpecParam(specGroup.getId(),null,false));
        }
        spu.setSpecGroups(specGroups);
        spu.setSkus(goodsClient.querySkuBySpuId(spuId));*/
        return model;
    }

    public void createHtml(Long spuId) {
        Context context = new Context();
        context.setVariables(loadData(spuId));
        File dest = new File("E:\\java-2018\\09 微服务电商【黑马乐优商城】·\\资料\\thymeleaf", spuId + ".html");
        if (dest.exists()) {
            dest.delete();
        }
        try (PrintWriter writer = new PrintWriter(dest, "UTF-8")) {
            templateEngine.process("item", context, writer);

        } catch (Exception e) {
            log.error("[静态页服务] 生成静态页异常！", e);
        }
    }

    public void deleteHtml(Long spuId) {
        File dest = new File("E:\\java-2018\\09 微服务电商【黑马乐优商城】·\\资料\\thymeleaf", spuId + ".html");
        if (dest.exists()) {
            dest.delete();
        }
    }
}
