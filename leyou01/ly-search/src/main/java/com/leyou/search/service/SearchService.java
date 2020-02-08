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
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SearchService {
    @Autowired
    private BrandClient brandClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu){
        Long spuId = spu.getId();
        Brand brand = brandClient.queryBrandByBid(spu.getBrandId());
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOND);
        }
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categories)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        String catetoryNames = categories.stream().map(Category::getName).collect(Collectors.joining(" "));
        String all = "";
        all = spu.getTitle()+catetoryNames+brand.getName();
        Goods goods = new Goods();
        goods.setAll(all);//搜索字段，包含标题，分类，品牌，规格等
        goods.setSubTitle(spu.getSubTitle());
        goods.setId(spu.getId());
        goods.setCreateTime(spu.getCreateTime());
        goods.setCid3(spu.getCid3());
        goods.setCid2(spu.getCid2());
        goods.setCid1(spu.getCid1());
        goods.setBrandId(spu.getBrandId());

        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        List<Map<String,Object>> list = new ArrayList<>();
        Set<Long> set = new HashSet<>();
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("image",sku.getImages());
            map.put("price",sku.getPrice());
            set.add(sku.getPrice());
            list.add(map);
        }
        String skus = JsonUtils.toString(list);
        goods.setSkus(skus);
        goods.setPrice(set);
        Map<String,Object> specs = new HashMap<>();
        SpuDetail spuDetail = goodsClient.querySpuDetailBySpuId(spuId);
        Map<Long, Object> genericSpec = JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, Object.class);
        Map<Long, List<Object>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<Object>>>() {
        });
        List<SpecParam> specParams = specificationClient.querySpecParam(spu.getCid3(),true);
        if (CollectionUtils.isEmpty(specParams)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        for (SpecParam specParam : specParams) {
            String key = specParam.getName();
            Object value = "";
            if(specParam.getGeneric()){
                value = genericSpec.get(specParam.getId());
                if(specParam.getNumeric()){
                    //处理成段
                    value = chooseSegment(value.toString(), specParam);
                }
            }else {
                value = specialSpec.get(specParam.getId());
            }
            specs.put(key,value);
        }
        goods.setSpecs(specs);
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

    public SearchResult search(SearchRequest searchRequest) {
        String key = searchRequest.getKey();
        if(StringUtils.isBlank(key)){
            return null;
        }
        int page = searchRequest.getPage() - 1;
        int size = searchRequest.getSize();
        String categoryAgg = "category_agg";
        String brandAgg = "brand_agg";
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        queryBuilder.withPageable(PageRequest.of(page,size));
        queryBuilder.withQuery(QueryBuilders.matchQuery("all",key));
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("cid3"));
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));
        AggregatedPage<Goods> aggregatedPage = template.queryForPage(queryBuilder.build(), Goods.class);
        List<Goods> goodsList = aggregatedPage.getContent();
        long total = aggregatedPage.getTotalElements();
        long totalPages = Long.valueOf(aggregatedPage.getTotalPages());
        List<Map<String,Object>> specList = new ArrayList<>();

        Aggregations aggregations = aggregatedPage.getAggregations();
        //LongTerms cterms = aggregations.get(categoryAgg);
        List<Category> categories = parseCategoryAgg(aggregations.get(categoryAgg));
        if(categories != null && categories.size() == 1){
            List<SpecParam> specParams = specificationClient.querySpecParam(categories.get(0).getId(), true);
            for (SpecParam specParam : specParams) {
                String name = specParam.getName();
                queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
                AggregatedPage<Goods> goods = template.queryForPage(queryBuilder.build(), Goods.class);
                Aggregations aggregations1 = goods.getAggregations();
                StringTerms stringTerms = aggregations1.get(name);
                List<String> options = stringTerms.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList());
                Map<String, Object> map = new HashMap<>();
                map.put("k",name);
                map.put("options",options);
                specList.add(map);
            }

        }

        //LongTerms bterms = aggregations.get(brandAgg);
        List<Brand> brands = parseBrandAgg(aggregations.get(brandAgg));

        /*Page<Goods> pageGoods = goodsRepository.search(QueryBuilders.matchQuery("all", key), PageRequest.of(page, size));
        List<Goods> goodsList = pageGoods.getContent();
        List<Brand> brandList = new ArrayList<>();
        List<Map<String,Object>> specList = new ArrayList<>();

        for (Goods goods : goodsList) {
            Brand brand = brandClient.queryBrandByBid(goods.getBrandId());
            brandList.add(brand);

            Map<String, Object> specs = goods.getSpecs();
            specList.add(specs);
        }
        long total = pageGoods.getTotalElements();
        long totalPages = Long.valueOf(pageGoods.getTotalPages());*/

        SearchResult result = new SearchResult(total,totalPages,goodsList,categories,brands,specList);


        return result;
    }

    private List<Brand> parseBrandAgg(LongTerms bterms) {
        List<LongTerms.Bucket> bbuckets = bterms.getBuckets();
        List<Long> bids = bbuckets.stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
        return brandClient.queryBrandByBids(bids);
    }

    private List<Category> parseCategoryAgg(LongTerms cterms) {
        List<LongTerms.Bucket> cbuckets = cterms.getBuckets();
        List<Long> cids = cbuckets.stream().map(c -> c.getKeyAsNumber().longValue()).collect(Collectors.toList());
        return categoryClient.queryCategoryByIds(cids);
    }

    public void createOrUpdateIndex(Long spuId){
        Spu spu = goodsClient.querySpuBySpuId(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.save(goods);
    };

    public void deleteIndex(Long spuId){
        Spu spu = goodsClient.querySpuBySpuId(spuId);
        Goods goods = buildGoods(spu);
        goodsRepository.delete(goods);
    };
}
