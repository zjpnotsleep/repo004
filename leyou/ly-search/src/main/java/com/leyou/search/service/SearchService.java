package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.pojo.PageResult;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.xml.ws.Action;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu) {
        Long spuId = spu.getId();
        Goods goods = new Goods();
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());
        String all = spu.getTitle() + StringUtils.join(names, " ") + brand.getName();
        goods.setAll(all);//搜索字段，包含标题，分类，品牌，规格等
        goods.setBrandId(spu.getBrandId());

        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(spuId);

        Set<Long> set = new HashSet();
        List<Sku> skus = goodsClient.querySkuBySpuId(spuId);
        List<Map<String, Object>> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOND);
        }
        for (Sku sku : skus) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("price", sku.getPrice());
            map.put("image", StringUtils.substringBefore(sku.getImages(), ","));
            set.add(sku.getPrice());
            list.add(map);
        }
        goods.setPrice(set); //所有sku的价格集合

        goods.setSkus(JsonUtils.serialize(list));   //所有sku的集合的json格式
        List<SpecParam> specParams = specificationClient.querySpecParam(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(specParams)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        Map<String, Object> specs = new HashMap<>();
        SpuDetail spuDetail = goodsClient.queryDetailById(spuId);
        Map<Long, Object> genericSpec = JsonUtils.parseMap(spuDetail.getGenericSpec(), Long.class, Object.class);
        Map<Long, List<Object>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {
        });
        for (SpecParam specParam : specParams) {
            String key = specParam.getName();
            Object value = "";
            if (specParam.getGeneric()) {
                value = genericSpec.get(specParam.getId());
                //判断是否是数值类型
                if (specParam.getNumeric()) {
                    //处理成段
                    value = chooseSegment(value.toString(), specParam);
                }
            } else {
                value = specialSpec.get(specParam.getId());
            }
            specs.put(key, value);
        }
        goods.setSpecs(specs);  //所有的可搜索的规格参数
        goods.setSubTitle(spu.getSubTitle());

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        String key = request.getKey();
        //判断是否有搜索条件，如果没有，直接返回null。不允许搜索全部商品
        if (StringUtils.isBlank(key)) {
            return null;
        }
        int size = request.getSize();
        int page = request.getPage() - 1;
        //创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //分页
        queryBuilder.withPageable(PageRequest.of(page, size));
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        //过滤
        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key));
        //聚合分类和品牌
        //聚合分类
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //聚合品牌
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //查询
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //解析结果
        //解析分页结果
        long total = result.getTotalElements();
        int totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        //解析聚合结果
        Aggregations aggregations = result.getAggregations();
        List<Category> categories = parseCategoryAgg(aggregations.get(categoryAggName));
        List<Map<String, Object>> specs = new ArrayList<>();
        if (categories != null && categories.size() == 1) {
            List<SpecParam> specParams = specificationClient.querySpecParam(null, categories.get(0).getId(), true);
            for (SpecParam specParam : specParams) {
                String name = specParam.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
                AggregatedPage<Goods> result1 = template.queryForPage(queryBuilder.build(), Goods.class);
                Aggregations aggregations1 = result1.getAggregations();
                StringTerms terms1 = aggregations1.get(name);
                List<String> options = terms1.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList());
                Map<String, Object> map = new HashMap<>();
                map.put("k", name);
                map.put("options", options);
                specs.add(map);
            }
        }
        List<Brand> brands = parseBrandAgg(aggregations.get(brandAggName));

        //Page<Goods> result = goodsRepository.search(queryBuilder.build());
        return new SearchResult(total, Long.parseLong(totalPage + ""), goodsList, categories, brands, specs);
    }

    private List<Brand> parseBrandAgg(LongTerms terms) {
        try {
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        } catch (Exception e) {
            return null;
        }
    }

    private List<Category> parseCategoryAgg(LongTerms terms) {
        try {
            List<Long> cids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(cids);
            return categories;
        } catch (Exception e) {
            return null;
        }

    }

    public void createOrUpdateIndex(Long spuId) {
        Spu spu = goodsClient.querySpuBySpuId(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.save(goods);
    }

    public void deleteIndex(Long spuId) {
        Spu spu = goodsClient.querySpuBySpuId(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.delete(goods);
    }
}
